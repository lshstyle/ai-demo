package com.example.userservice.service;

import com.example.userservice.common.BusinessException;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.LoginResponse;
import com.example.userservice.dto.PhoneLoginRequest;
import com.example.userservice.entity.User;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private SmsCodeService smsCodeService;

    @InjectMocks
    private AuthService authService;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPasswordHash("hashed-pw");
        admin.setPhone("13800000000");
        admin.setStatus(1);
        admin.setDeleted(0);
        admin.setNickname("管理员");

        when(jwtUtil.generateAccessToken(anyLong(), anyString())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(anyLong(), anyString())).thenReturn("refresh-token");
    }

    @Test
    void loginByPassword_success() {
        when(userMapper.selectOne(any())).thenReturn(admin);
        when(passwordEncoder.matches("123456", "hashed-pw")).thenReturn(true);

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("123456");

        LoginResponse res = authService.loginByPassword(req);

        assertThat(res.getAccessToken()).isEqualTo("access-token");
        assertThat(res.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(res.getUsername()).isEqualTo("admin");
    }

    @Test
    void loginByPassword_userNotFound() {
        when(userMapper.selectOne(any())).thenReturn(null);

        LoginRequest req = new LoginRequest();
        req.setUsername("ghost");
        req.setPassword("123456");

        assertThatThrownBy(() -> authService.loginByPassword(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void loginByPassword_disabled() {
        admin.setStatus(0);
        when(userMapper.selectOne(any())).thenReturn(admin);

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("123456");

        assertThatThrownBy(() -> authService.loginByPassword(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账号已被禁用");
    }

    @Test
    void loginByPassword_wrongPassword() {
        when(userMapper.selectOne(any())).thenReturn(admin);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrong");

        assertThatThrownBy(() -> authService.loginByPassword(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void loginByPhone_success() {
        when(smsCodeService.verifyCode("13800000000", "123456")).thenReturn(true);
        when(userMapper.selectOne(any())).thenReturn(admin);

        PhoneLoginRequest req = new PhoneLoginRequest();
        req.setPhone("13800000000");
        req.setCode("123456");

        LoginResponse res = authService.loginByPhone(req);
        assertThat(res.getUserId()).isEqualTo(1L);
        assertThat(res.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void loginByPhone_invalidCode() {
        when(smsCodeService.verifyCode(anyString(), anyString())).thenReturn(false);

        PhoneLoginRequest req = new PhoneLoginRequest();
        req.setPhone("13800000000");
        req.setCode("000000");

        assertThatThrownBy(() -> authService.loginByPhone(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("验证码无效或已过期");
    }

    @Test
    void loginByPhone_phoneNotRegistered() {
        when(smsCodeService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userMapper.selectOne(any())).thenReturn(null);

        PhoneLoginRequest req = new PhoneLoginRequest();
        req.setPhone("13900000000");
        req.setCode("123456");

        assertThatThrownBy(() -> authService.loginByPhone(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("该手机号未注册");
    }

    @Test
    void sendSmsCode_delegatesToSmsCodeService() {
        when(smsCodeService.sendCode("13800000000")).thenReturn("123456");
        authService.sendSmsCode("13800000000");
        verify(smsCodeService).sendCode("13800000000");
    }
}
