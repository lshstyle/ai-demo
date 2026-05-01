package com.example.userservice.util;

import com.example.userservice.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类：生成 / 解析 / 验证 Token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** 生成 Access Token */
    public String generateAccessToken(Long userId, String username) {
        return buildToken(userId, username, jwtProperties.getAccessTokenExpire(), "access");
    }

    /** 生成 Refresh Token */
    public String generateRefreshToken(Long userId, String username) {
        return buildToken(userId, username, jwtProperties.getRefreshTokenExpire(), "refresh");
    }

    private String buildToken(Long userId, String username, long expireMillis, String type) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("username", username);
        claims.put("type", type);
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 解析 Token，若无效或过期返回 null */
    public Claims parse(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    public boolean isValid(String token) {
        return parse(token) != null;
    }

    /** 从 Token 中提取用户ID */
    public Long getUserId(String token) {
        Claims claims = parse(token);
        return claims == null ? null : claims.get("uid", Long.class);
    }

    public String getUsername(String token) {
        Claims claims = parse(token);
        return claims == null ? null : claims.get("username", String.class);
    }
}
