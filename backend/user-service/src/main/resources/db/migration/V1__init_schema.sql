-- ============================================================
-- AI Demo 数据库初始化脚本
-- 创建时间：2026-04-26
-- 说明：创建 ai_demo 数据库及核心表
-- ============================================================

CREATE DATABASE IF NOT EXISTS `ai_demo` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ai_demo`;

-- ============================================================
-- 用户表
-- ============================================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`      VARCHAR(50)  NOT NULL                COMMENT '用户名',
    `password_hash` VARCHAR(255) NOT NULL                COMMENT 'BCrypt 密码哈希',
    `phone`         VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `email`         VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `nickname`      VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
    `avatar_url`    VARCHAR(255) DEFAULT NULL            COMMENT '头像URL',
    `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：0-禁用 1-正常',
    `deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                     COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone`    (`phone`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 登录日志表（审计用）
-- ============================================================
DROP TABLE IF EXISTS `login_logs`;
CREATE TABLE `login_logs` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id`     BIGINT       DEFAULT NULL            COMMENT '用户ID（失败时可能为空）',
    `username`    VARCHAR(50)  DEFAULT NULL            COMMENT '登录用户名/手机号',
    `login_type`  VARCHAR(20)  NOT NULL                COMMENT '登录方式：password/phone/wechat',
    `ip_address`  VARCHAR(50)  DEFAULT NULL            COMMENT '登录IP',
    `user_agent`  VARCHAR(500) DEFAULT NULL            COMMENT 'User-Agent',
    `result`      TINYINT      NOT NULL                COMMENT '结果：0-失败 1-成功',
    `message`     VARCHAR(255) DEFAULT NULL            COMMENT '结果描述',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id`    (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ============================================================
-- 初始化种子数据
-- 默认用户：admin / 123456（BCrypt 加密）
-- ============================================================
INSERT INTO `users` (`username`, `password_hash`, `phone`, `email`, `nickname`, `status`)
VALUES ('admin',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        '13800000000',
        'admin@example.com',
        '管理员',
        1);
