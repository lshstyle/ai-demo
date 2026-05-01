package com.example.userservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.userservice.common.BusinessException;
import com.example.userservice.config.WechatProperties;
import com.example.userservice.dto.LoginResponse;
import com.example.userservice.dto.WechatLoginStatus;
import com.example.userservice.dto.WechatQrCodeResponse;
import com.example.userservice.entity.User;
import com.example.userservice.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * 微信扫码登录服务
 *
 * 设计：
 * 1. {@link #generateQrCode()} 生成一个 state（UUID），存入 Redis，返回授权 URL
 *    - 真实场景：前端直接 window.open(authorizeUrl) 或把 authorizeUrl 编码为二维码图
 *    - 演示场景：前端轮询 /auth/wechat/status，3 秒后自动置 CONFIRMED（模拟扫码完成）
 * 2. {@link #handleCallback(String, String)} 接收微信回调的 code + state，调用微信接口换取 openid/unionid
 * 3. {@link #pollStatus(String)} 前端轮询扫码状态
 *
 * Redis keys:
 *   wechat:state:{state}          -> PENDING / SCANNED / CONFIRMED
 *   wechat:login-result:{state}   -> LoginResponse JSON（CONFIRMED 时）
 *   wechat:state:{state}:created  -> 创建时间戳（用于演示自动确认）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatService {

    private static final String AUTHORIZE_URL = "https://open.weixin.qq.com/connect/qrconnect";
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
    private static final String USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo";
    private static final String STATE_KEY_PREFIX = "wechat:state:";
    private static final String RESULT_KEY_PREFIX = "wechat:login-result:";
    private static final String CREATED_KEY_SUFFIX = ":created";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SCANNED = "SCANNED";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    private final WechatProperties wechatProperties;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;
    private final UserMapper userMapper;
    private final AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成二维码（state）
     */
    public WechatQrCodeResponse generateQrCode() {
        String state = UUID.randomUUID().toString().replace("-", "");
        long ttl = wechatProperties.getStateExpireSeconds();
        redisTemplate.opsForValue().set(STATE_KEY_PREFIX + state, STATUS_PENDING, Duration.ofSeconds(ttl));
        redisTemplate.opsForValue().set(STATE_KEY_PREFIX + state + CREATED_KEY_SUFFIX,
                String.valueOf(System.currentTimeMillis()), Duration.ofSeconds(ttl));

        String authorizeUrl = buildAuthorizeUrl(state);
        // 演示环境：后端生成一张占位二维码图；真实环境直接用该 URL 编码为二维码
        String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=220x220&data="
                + URLEncoder.encode(authorizeUrl, StandardCharsets.UTF_8);

        log.info("[Wechat] 生成二维码 state={} mock={}", state, wechatProperties.isMock());
        return new WechatQrCodeResponse(state, qrCodeUrl, authorizeUrl, ttl);
    }

    /**
     * 轮询扫码状态（演示模式下，state 存在 5s 后自动置为 CONFIRMED）
     */
    public WechatLoginStatus pollStatus(String state) {
        String stateKey = STATE_KEY_PREFIX + state;
        String status = redisTemplate.opsForValue().get(stateKey);
        if (status == null) {
            return new WechatLoginStatus(STATUS_EXPIRED, null);
        }

        // 演示模式：state 创建 5 秒后自动切换为 CONFIRMED 并写入默认 demo 用户登录结果
        if (wechatProperties.isMock() && STATUS_PENDING.equals(status)) {
            String createdStr = redisTemplate.opsForValue().get(stateKey + CREATED_KEY_SUFFIX);
            long created = createdStr == null ? 0L : Long.parseLong(createdStr);
            if (System.currentTimeMillis() - created > 5000) {
                autoConfirmForMock(state);
                status = STATUS_CONFIRMED;
            }
        }

        WechatLoginStatus result = new WechatLoginStatus(status, null);
        if (STATUS_CONFIRMED.equals(status)) {
            String json = redisTemplate.opsForValue().get(RESULT_KEY_PREFIX + state);
            if (json != null) {
                try {
                    result.setLoginResult(objectMapper.readValue(json, LoginResponse.class));
                } catch (Exception e) {
                    log.error("[Wechat] 反序列化 LoginResult 失败", e);
                }
            }
            // 登录结果被消费后立即清理
            redisTemplate.delete(stateKey);
            redisTemplate.delete(stateKey + CREATED_KEY_SUFFIX);
            redisTemplate.delete(RESULT_KEY_PREFIX + state);
        }
        return result;
    }

    /**
     * 微信回调处理：code + state → access_token → userinfo → 本地用户 → Token
     */
    public void handleCallback(String code, String state) {
        String stateKey = STATE_KEY_PREFIX + state;
        String status = redisTemplate.opsForValue().get(stateKey);
        if (status == null) {
            throw new BusinessException(400, "二维码已过期，请刷新");
        }

        String openId;
        String nickname = null;
        String avatarUrl = null;

        if (wechatProperties.isMock()) {
            openId = "mock-openid-" + state.substring(0, 8);
        } else {
            // 1. code 换 access_token
            JsonNode tokenJson = restTemplate.getForObject(
                    UriComponentsBuilder.fromHttpUrl(ACCESS_TOKEN_URL)
                            .queryParam("appid", wechatProperties.getAppId())
                            .queryParam("secret", wechatProperties.getAppSecret())
                            .queryParam("code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build().toUriString(),
                    JsonNode.class);
            if (tokenJson == null || tokenJson.has("errcode")) {
                throw new BusinessException(401, "微信授权失败");
            }
            String accessToken = tokenJson.get("access_token").asText();
            openId = tokenJson.get("openid").asText();

            // 2. 拉取用户信息
            JsonNode userJson = restTemplate.getForObject(
                    UriComponentsBuilder.fromHttpUrl(USERINFO_URL)
                            .queryParam("access_token", accessToken)
                            .queryParam("openid", openId)
                            .build().toUriString(),
                    JsonNode.class);
            if (userJson != null && !userJson.has("errcode")) {
                nickname = userJson.has("nickname") ? userJson.get("nickname").asText() : null;
                avatarUrl = userJson.has("headimgurl") ? userJson.get("headimgurl").asText() : null;
            }
        }

        // 3. 查询或创建本地用户（演示：以 openid 为 username；生产应使用 unionid 并与现有账号绑定）
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, openId));
        if (user == null) {
            user = new User();
            user.setUsername(openId);
            user.setNickname(nickname != null ? nickname : "微信用户");
            user.setAvatarUrl(avatarUrl);
            user.setStatus(1);
            user.setDeleted(0);
            userMapper.insert(user);
        }

        LoginResponse loginResponse = authService.buildLoginResponse(user);

        // 4. 写回 Redis：CONFIRMED + 登录结果
        try {
            long ttl = wechatProperties.getStateExpireSeconds();
            redisTemplate.opsForValue().set(stateKey, STATUS_CONFIRMED, Duration.ofSeconds(ttl));
            redisTemplate.opsForValue().set(RESULT_KEY_PREFIX + state,
                    objectMapper.writeValueAsString(loginResponse), Duration.ofSeconds(ttl));
        } catch (Exception e) {
            throw new BusinessException(500, "序列化登录结果失败");
        }
    }

    private void autoConfirmForMock(String state) {
        User demo = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
        if (demo == null) {
            log.warn("[Wechat][Mock] 未找到 admin 用户，无法完成模拟登录");
            return;
        }
        LoginResponse loginResponse = authService.buildLoginResponse(demo);
        try {
            long ttl = wechatProperties.getStateExpireSeconds();
            redisTemplate.opsForValue().set(STATE_KEY_PREFIX + state, STATUS_CONFIRMED, Duration.ofSeconds(ttl));
            redisTemplate.opsForValue().set(RESULT_KEY_PREFIX + state,
                    objectMapper.writeValueAsString(loginResponse), Duration.ofSeconds(ttl));
        } catch (Exception e) {
            log.error("[Wechat][Mock] 自动确认失败", e);
        }
    }

    private String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder.fromHttpUrl(AUTHORIZE_URL)
                .queryParam("appid", wechatProperties.getAppId())
                .queryParam("redirect_uri", URLEncoder.encode(wechatProperties.getRedirectUri(), StandardCharsets.UTF_8))
                .queryParam("response_type", "code")
                .queryParam("scope", wechatProperties.getScope())
                .queryParam("state", state)
                .fragment("wechat_redirect")
                .build(true).toUriString();
    }
}
