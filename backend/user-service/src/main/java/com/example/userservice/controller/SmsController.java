package com.example.userservice.controller;

import com.example.userservice.common.Result;
import com.example.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短信验证码接口
 */
@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final AuthService authService;

    @PostMapping("/send")
    public Result<Void> send(@RequestParam String phone) {
        authService.sendSmsCode(phone);
        return Result.success("验证码已发送，请注意查收", null);
    }
}
