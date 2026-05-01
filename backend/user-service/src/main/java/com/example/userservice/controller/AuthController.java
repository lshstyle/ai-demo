package com.example.userservice.controller;

import com.example.userservice.common.Result;
import com.example.userservice.config.JwtProperties;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.LoginResponse;
import com.example.userservice.dto.PhoneLoginRequest;
import com.example.userservice.dto.WechatLoginStatus;
import com.example.userservice.dto.WechatQrCodeResponse;
import com.example.userservice.service.AuthService;
import com.example.userservice.service.TokenBlacklistService;
import com.example.userservice.service.WechatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证控制器：登录、退出、微信扫码
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final WechatService wechatService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtProperties jwtProperties;

    /** 账号密码登录 */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.loginByPassword(request));
    }

    /** 手机号验证码登录 */
    @PostMapping("/login/phone")
    public Result<LoginResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request) {
        return Result.success("登录成功", authService.loginByPhone(request));
    }

    /**
     * 退出登录：将 Token 加入 Redis 黑名单，直到其自然过期
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith(jwtProperties.getTokenPrefix())) {
            String token = authHeader.substring(jwtProperties.getTokenPrefix().length()).trim();
            tokenBlacklistService.blacklist(token);
        }
        return Result.success();
    }

    /** 微信扫码登录 - 获取二维码 */
    @GetMapping("/wechat/qrcode")
    public Result<WechatQrCodeResponse> wechatQrCode() {
        return Result.success(wechatService.generateQrCode());
    }

    /** 微信扫码登录 - 前端轮询登录状态 */
    @GetMapping("/wechat/status")
    public Result<WechatLoginStatus> wechatStatus(@RequestParam String state) {
        return Result.success(wechatService.pollStatus(state));
    }

    /**
     * 微信回调（open.weixin.qq.com redirect_uri 回调到此；演示场景由前端主动调用）
     */
    @GetMapping("/wechat/callback")
    public Result<Void> wechatCallback(@RequestParam String code, @RequestParam String state) {
        wechatService.handleCallback(code, state);
        return Result.success();
    }
}
