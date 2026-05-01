package com.example.userservice.util;

import com.example.userservice.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtUtil 单元测试
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("ai-demo-jwt-secret-key-please-change-in-production-environment-2026");
        props.setAccessTokenExpire(3_600_000L);
        props.setRefreshTokenExpire(7_200_000L);
        props.setHeader("Authorization");
        props.setTokenPrefix("Bearer ");
        jwtUtil = new JwtUtil(props);
    }

    @Test
    void shouldGenerateAndParseAccessToken() {
        String token = jwtUtil.generateAccessToken(42L, "alice");
        assertThat(token).isNotBlank();

        Claims claims = jwtUtil.parse(token);
        assertThat(claims).isNotNull();
        assertThat(claims.get("uid", Long.class)).isEqualTo(42L);
        assertThat(claims.get("username", String.class)).isEqualTo("alice");
        assertThat(claims.get("type", String.class)).isEqualTo("access");
    }

    @Test
    void shouldReturnNullForInvalidToken() {
        assertThat(jwtUtil.parse("invalid.token.value")).isNull();
        assertThat(jwtUtil.isValid("invalid.token.value")).isFalse();
    }

    @Test
    void shouldExpireImmediately() throws InterruptedException {
        JwtProperties props = new JwtProperties();
        props.setSecret("ai-demo-jwt-secret-key-please-change-in-production-environment-2026");
        props.setAccessTokenExpire(1L); // 1ms
        props.setRefreshTokenExpire(1L);
        JwtUtil shortLived = new JwtUtil(props);
        String token = shortLived.generateAccessToken(1L, "bob");
        Thread.sleep(20);
        assertThat(shortLived.parse(token)).isNull();
    }

    @Test
    void shouldExtractUserIdAndUsername() {
        String token = jwtUtil.generateAccessToken(99L, "carol");
        assertThat(jwtUtil.getUserId(token)).isEqualTo(99L);
        assertThat(jwtUtil.getUsername(token)).isEqualTo("carol");
    }
}
