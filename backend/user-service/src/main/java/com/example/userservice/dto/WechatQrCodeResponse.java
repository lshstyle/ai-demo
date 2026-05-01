package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信扫码二维码响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatQrCodeResponse {
    /** 本次扫码登录的唯一标识，前端轮询用 */
    private String state;
    /** 二维码图片 URL（真实场景一般为微信开放平台 URL 或后端生成的二维码图片） */
    private String qrCodeUrl;
    /** 跳转到微信授权页的完整 URL（备用，前端也可直接 window.open） */
    private String authorizeUrl;
    /** state 有效期（秒） */
    private long expireSeconds;
}
