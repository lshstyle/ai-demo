package com.example.userservice.service;

import com.example.userservice.common.BusinessException;
import com.example.userservice.config.SmsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 短信验证码服务
 * Redis key：
 *   sms:code:{phone}        -> 验证码值（有效期 codeExpireSeconds）
 *   sms:send-limit:{phone}  -> 限流标记（有效期 sendIntervalSeconds）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeService {

    private static final String CODE_KEY_PREFIX = "sms:code:";
    private static final String LIMIT_KEY_PREFIX = "sms:send-limit:";

    private final StringRedisTemplate redisTemplate;
    private final SmsProperties smsProperties;

    /**
     * 发送验证码（带限流）
     * @return 实际下发的验证码（便于测试/日志，生产环境不应暴露）
     */
    public String sendCode(String phone) {
        String limitKey = LIMIT_KEY_PREFIX + phone;
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(limitKey, "1",
                Duration.ofSeconds(smsProperties.getSendIntervalSeconds()));
        if (Boolean.FALSE.equals(ok)) {
            throw new BusinessException(429, "发送过于频繁，请稍后再试");
        }
        String code = generateCode();
        redisTemplate.opsForValue().set(CODE_KEY_PREFIX + phone, code,
                Duration.ofSeconds(smsProperties.getCodeExpireSeconds()));
        // TODO: 接入真实短信服务商
        log.info("[SMS] 向 {} 下发验证码 {}（有效期 {}s）", phone, code, smsProperties.getCodeExpireSeconds());
        return code;
    }

    /**
     * 校验验证码：成功后删除，防止重复使用
     */
    public boolean verifyCode(String phone, String code) {
        if (phone == null || code == null) {
            return false;
        }
        String key = CODE_KEY_PREFIX + phone;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached == null) {
            return false;
        }
        if (Objects.equals(cached, code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private String generateCode() {
        String mock = smsProperties.getMockCode();
        if (mock != null && !mock.isEmpty()) {
            return mock;
        }
        int n = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("%06d", n);
    }
}
