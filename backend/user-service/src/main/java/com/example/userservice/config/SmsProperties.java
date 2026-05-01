package com.example.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 短信验证码配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {
    /** 验证码有效期（秒） */
    private long codeExpireSeconds = 300;
    /** 同一手机号发送间隔（秒） */
    private long sendIntervalSeconds = 60;
    /** 演示环境固定验证码；生产环境置空则随机生成 6 位数字 */
    private String mockCode;
}
