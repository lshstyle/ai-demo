package com.example.userservice.service;

import com.example.userservice.common.BusinessException;
import com.example.userservice.config.SmsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsCodeServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;

    private SmsProperties smsProperties;
    private SmsCodeService smsCodeService;

    @BeforeEach
    void setUp() {
        smsProperties = new SmsProperties();
        smsProperties.setCodeExpireSeconds(300);
        smsProperties.setSendIntervalSeconds(60);
        smsProperties.setMockCode("123456");
        smsCodeService = new SmsCodeService(redisTemplate, smsProperties);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void sendCode_success_whenNotThrottled() {
        when(valueOps.setIfAbsent(eq("sms:send-limit:13800000000"), anyString(), any(Duration.class)))
                .thenReturn(true);

        String code = smsCodeService.sendCode("13800000000");
        assertThat(code).isEqualTo("123456");
        verify(valueOps).set(eq("sms:code:13800000000"), eq("123456"), any(Duration.class));
    }

    @Test
    void sendCode_throws_whenThrottled() {
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        assertThatThrownBy(() -> smsCodeService.sendCode("13800000000"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("发送过于频繁");
    }

    @Test
    void verifyCode_success_deletesKey() {
        when(valueOps.get("sms:code:13800000000")).thenReturn("123456");

        boolean ok = smsCodeService.verifyCode("13800000000", "123456");
        assertThat(ok).isTrue();
        verify(redisTemplate).delete("sms:code:13800000000");
    }

    @Test
    void verifyCode_fail_whenMismatched() {
        when(valueOps.get(anyString())).thenReturn("111111");
        assertThat(smsCodeService.verifyCode("13800000000", "123456")).isFalse();
    }

    @Test
    void verifyCode_fail_whenMissing() {
        when(valueOps.get(anyString())).thenReturn(null);
        assertThat(smsCodeService.verifyCode("13800000000", "123456")).isFalse();
    }
}
