package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信扫码登录状态
 * status:
 *   PENDING   - 等待扫码
 *   SCANNED   - 已扫码，等待确认
 *   CONFIRMED - 已确认，返回 loginResult
 *   EXPIRED   - 已过期
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatLoginStatus {
    private String status;
    private LoginResponse loginResult;
}
