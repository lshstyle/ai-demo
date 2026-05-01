package com.example.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * 微信开放平台配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties {
    private String appId;
    private String appSecret;
    private String redirectUri;
    private String scope = "snsapi_login";
    private long stateExpireSeconds = 300;
    /** 是否启用模拟模式（真实 API 未配置时使用） */
    private boolean mock = true;

    @Bean
    public RestTemplate wechatRestTemplate() {
        return new RestTemplate();
    }
}
