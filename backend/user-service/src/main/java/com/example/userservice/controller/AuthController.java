package com.example.userservice.controller;

import com.example.userservice.common.Result;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.LoginResponse;
import com.example.userservice.dto.PhoneLoginRequest;
import com.example.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证控制器：登录、退出、短信验证码
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 账号密码登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.loginByPassword(request));
    }

    /**
     * 手机号验证码登录
     */
    @PostMapping("/login/phone")
    public Result<LoginResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request) {
        return Result.success("登录成功", authService.loginByPhone(request));
    }

    /**
     * 退出登录（JWT 无状态，前端清除 Token 即可；此处可用于记录日志或加入黑名单）
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}

/**
 * 短信验证码接口（独立路径 /sms/send）
 */
@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
class SmsController {

    private final AuthService authService;

    @PostMapping("/send")
    public Result<Void> send(@RequestParam String phone) {
        authService.sendSmsCode(phone);
        return Result.success("验证码已发送，请注意查收", null);
    }
}
