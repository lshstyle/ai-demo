package com.example.userservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.userservice.common.BusinessException;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.LoginResponse;
import com.example.userservice.dto.PhoneLoginRequest;
import com.example.userservice.entity.User;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SmsCodeService smsCodeService;

    /**
     * 账号密码登录
     */
    public LoginResponse loginByPassword(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        return buildLoginResponse(user);
    }

    /**
     * 手机号验证码登录：验证码由 Redis 存取
     */
    public LoginResponse loginByPhone(PhoneLoginRequest request) {
        if (!smsCodeService.verifyCode(request.getPhone(), request.getCode())) {
            throw new BusinessException(400, "验证码无效或已过期");
        }
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (user == null) {
            throw new BusinessException(404, "该手机号未注册");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }
        return buildLoginResponse(user);
    }

    /**
     * 发送短信验证码（Redis 存储 + 限流）
     */
    public void sendSmsCode(String phone) {
        smsCodeService.sendCode(phone);
    }

    public LoginResponse buildLoginResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarUrl()
        );
    }
}
