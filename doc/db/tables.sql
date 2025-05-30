-- 创建数据库
CREATE DATABASE IF NOT EXISTS `converter_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `converter_db`;

-- 应用配置表
DROP TABLE IF EXISTS app_config;
CREATE TABLE app_config
(
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    app_code     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '应用编码',
    app_name     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '应用名称',
    app_key      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '应用密钥KEY',
    app_secret   VARCHAR(128) NOT NULL DEFAULT '' COMMENT '应用密钥SECRET',
    base_url     VARCHAR(256) NOT NULL DEFAULT '' COMMENT '基础URL',
    enabled      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '启用状态:1启用 0禁用',
    remark       VARCHAR(512) NOT NULL DEFAULT '' COMMENT '备注',
    created_by   VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '创建人',
    created_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by   VARCHAR(64)  NOT NULL DEFAULT 'system' COMMENT '更新人',
    updated_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX unq_app_code (app_code)
) COMMENT '第三方应用配置表';

-- 接口配置表
DROP TABLE IF EXISTS api_config;
CREATE TABLE api_config
(
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    app_id         BIGINT UNSIGNED NOT NULL COMMENT '应用ID',
    api_code       VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '接口编码',
    api_name       VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '接口名称',
    api_path       VARCHAR(256)    NOT NULL DEFAULT '' COMMENT '接口路径',
    direction      TINYINT         NOT NULL COMMENT '方向:1接收 2发送',
    message_format TINYINT         NOT NULL DEFAULT 1 COMMENT '报文格式：1=JSON 2=XML',
    http_method    TINYINT         NOT NULL DEFAULT 1 COMMENT '请求方式：1=POST 2=GET 3=PUT',
    enabled        TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '启用状态：1启用 0禁用',
    remark         VARCHAR(512)    NOT NULL DEFAULT '' COMMENT '备注',
    created_by     VARCHAR(64)     NOT NULL DEFAULT 'system' COMMENT '创建人',
    created_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by     VARCHAR(64)     NOT NULL DEFAULT 'system' COMMENT '更新人',
    updated_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX unq_api_code (api_code),
    INDEX idx_app_id (app_id)
) COMMENT '接口配置表';

-- 接口日志表
DROP TABLE IF EXISTS api_log;
CREATE TABLE api_log
(
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    biz_no        VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '业务编号',
    app_code      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '应用编码',
    app_name      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '应用名称',
    api_code      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '接口编码',
    api_name      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '接口名称',
    request_body  TEXT         NOT NULL COMMENT '请求报文',
    response_body TEXT         NOT NULL COMMENT '响应报文',
    retry_params  VARCHAR(512) NOT NULL DEFAULT '' COMMENT '重试参数',
    status        TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：1成功 0失败',
    error_msg     VARCHAR(512) NOT NULL DEFAULT '' COMMENT '错误信息',
    created_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_app_code (app_code),
    INDEX idx_api_code (api_code),
    INDEX idx_created_time (created_time)
) COMMENT '接口调用日志表';