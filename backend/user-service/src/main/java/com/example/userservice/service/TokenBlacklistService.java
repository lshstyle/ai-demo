package com.example.userservice.service;

import com.example.userservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

/**
 * Token 黑名单服务：logout 时将 Token 加入黑名单，直到原 Token 过期
 * Redis key：auth:token-blacklist:{token}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "auth:token-blacklist:";

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    /**
     * 将 Token 加入黑名单，过期时间与 Token 自身剩余有效期一致
     */
    public void blacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        Claims claims = jwtUtil.parse(token);
        if (claims == null) {
            // 无效 Token 无需处理
            return;
        }
        Date expiration = claims.getExpiration();
        long ttl = expiration == null ? 0 : expiration.getTime() - System.currentTimeMillis();
        if (ttl <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + token, "1", Duration.ofMillis(ttl));
        log.info("[TokenBlacklist] token 已加入黑名单，剩余 ttl={}ms", ttl);
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + token));
    }
}
