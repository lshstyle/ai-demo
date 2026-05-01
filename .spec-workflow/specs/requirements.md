# 需求文档

## 简介

本项目为一个基于 Vue 3 + Spring Cloud + MySQL 的全栈应用，首阶段核心功能为**用户登录系统**，后续扩展完整业务功能。

## 对齐产品愿景

- 采用主流前后端分离架构，前端 Vue 3 提供良好用户体验
- 后端 Spring Cloud 微服务架构支撑高并发与水平扩展
- 登录系统作为入口模块，需支持多方式登录以满足不同用户场景

---

## 需求

### 需求 1：登录页面前端开发

**用户故事：** 作为用户，我希望通过美观的登录页面进入系统，以便安全地访问我的账户。

#### 接受标准

1. WHEN 用户访问登录页 THEN 系统 SHALL 展示包含左侧轮播图与右侧登录区的页面
2. WHEN 页面加载 THEN 左侧 SHALL 展示轮播图（支持多张图片自动切换）
3. WHEN 用户查看登录区 THEN 系统 SHALL 提供**账号/密码登录**入口
4. WHEN 用户选择其他方式 THEN 系统 SHALL 支持**微信扫码登录**
5. WHEN 用户选择其他方式 THEN 系统 SHALL 支持**手机号验证码登录**
6. WHEN 用户在登录区操作 THEN 页面布局 SHALL 在不同屏幕尺寸下自适应（响应式设计）

#### UI 细节

- **左侧轮播图**：占页面约 50% 宽度，支持自动轮播、指示器、切换动画
- **右侧登录区**：占页面约 50% 宽度，顶部显示 Logo/系统名称
- **登录方式切换**：Tab 或按钮组切换账号密码 / 微信扫码 / 手机号登录
- **账号密码登录**：包含用户名输入框、密码输入框（支持显示/隐藏密码）、记住密码复选框、登录按钮
- **微信扫码登录**：展示二维码区域，提示"请使用微信扫一扫登录"
- **手机号登录**：手机号输入框、验证码输入框、获取验证码按钮（带倒计时）

---

### 需求 2：登录后端 API 开发

**用户故事：** 作为前端登录页面，我需要稳定的后端接口支持，以便完成用户认证流程。

#### 接受标准

1. WHEN 前端提交账号密码 THEN 后端 SHALL 校验用户名密码并返回 JWT Token
2. WHEN 用户名或密码错误 THEN 后端 SHALL 返回 401 并携带明确错误信息
3. WHEN 前端提交手机号和验证码 THEN 后端 SHALL 校验验证码有效性并返回 JWT Token
4. WHEN 验证码错误或过期 THEN 后端 SHALL 返回 400 并提示验证码无效
5. WHEN 微信扫码完成授权 THEN 后端 SHALL 接收微信回调并返回 JWT Token
6. WHEN Token 有效 THEN 后续请求 SHALL 通过网关校验并放行
7. WHEN Token 过期或无效 THEN 网关 SHALL 返回 401 未授权

#### 接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 账号密码登录 |
| POST | `/api/auth/login/phone` | 手机号验证码登录 |
| POST | `/api/auth/login/wechat` | 微信登录回调 |
| POST | `/api/auth/refresh` | 刷新 Token |
| POST | `/api/auth/logout` | 退出登录 |
| GET | `/api/auth/captcha` | 获取图片验证码 |
| POST | `/api/sms/send` | 发送短信验证码 |

---

### 需求 3：数据库设计

**用户故事：** 作为系统，我需要持久化存储用户信息与认证数据，以便支持登录与后续业务。

#### 接受标准

1. WHEN 系统初始化 THEN SHALL 创建用户表（users），包含用户ID、用户名、密码哈希、手机号、邮箱、状态、创建时间等字段
2. WHEN 用户注册 THEN SHALL 在用户表中插入记录，密码 SHALL 使用 BCrypt 加密存储
3. WHEN 用户登录 THEN SHALL 支持通过用户名或手机号查询用户信息
4. WHEN 需要审计 THEN SHALL 记录登录日志表（login_logs），包含用户ID、登录方式、IP地址、登录时间、登录结果

#### 核心表结构

**users 表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 用户ID |
| username | VARCHAR(50) UNIQUE | 用户名 |
| password_hash | VARCHAR(255) | BCrypt 密码哈希 |
| phone | VARCHAR(20) UNIQUE | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| avatar_url | VARCHAR(255) | 头像URL |
| status | TINYINT | 状态：0-禁用 1-正常 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

---

## 非功能性需求

### 代码架构与模块化
- 前端组件按功能模块拆分，登录相关组件置于 `src/views/login/` 目录
- 后端接口分层：Controller -> Service -> Repository/Mapper
- 认证逻辑封装为独立模块，便于后续扩展 OAuth2

### 性能
- 登录接口响应时间 < 500ms（P95）
- 轮播图图片懒加载，首屏加载时间 < 2s

### 安全
- 密码必须使用 BCrypt 加密，禁止明文存储
- JWT Token 设置合理过期时间（Access Token 2小时，Refresh Token 7天）
- 登录接口增加速率限制，防止暴力破解
- 敏感接口使用 HTTPS

### 可靠性
- 后端服务异常时返回统一错误格式，前端友好提示
- 短信验证码发送失败时给出明确错误信息

### 可用性
- 登录页面支持键盘操作（Tab 切换、Enter 提交）
- 表单输入实时校验并给出错误提示
- 加载状态有明确反馈（按钮 Loading、二维码加载中）
