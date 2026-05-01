package com.example.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Gateway JWT 配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private String header = "Authorization";
    private String tokenPrefix = "Bearer ";
    /** 白名单路径，支持 Ant 风格通配符 */
    private List<String> whitelist = new ArrayList<>();
}
