-- 创建数据库
create
database if not exists `convertor_db` default character set utf8mb4 collate utf8mb4_unicode_ci;
use
`convertor_db`;

-- 应用配置表
drop table if exists app_config;
create table app_config
(
    id           bigint unsigned auto_increment primary key comment '自增主键',
    app_code     varchar(64)  not null default '' comment '应用编码',
    app_name     varchar(64)  not null default '' comment '应用名称',
    app_key      varchar(64)  not null default '' comment '应用键名',
    app_secret   varchar(128) not null default '' comment '应用密钥',
    base_url     varchar(256) not null default '' comment '基础URL',
    enabled      tinyint(1)   not null default 1 comment '启用状态：1-启用 0-禁用',
    remark       varchar(512) not null default '' comment '备注',
    created_by   varchar(64)  not null default 'system' comment '创建人',
    created_time datetime     not null default current_timestamp comment '创建时间',
    updated_by   varchar(64)  not null default 'system' comment '更新人',
    updated_time datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    unique index unq_app_code (app_code)
) comment '应用配置表';

-- 接口配置表
drop table if exists api_config;
create table api_config
(
    id             bigint unsigned auto_increment primary key comment '主键id',
    app_id         bigint unsigned not null comment '应用ID',
    api_code       varchar(256) not null default '' comment '接口编码',
    api_name       varchar(256) not null default '' comment '接口名称',
    api_path       varchar(256) not null default '' comment '接口路径',
    handler_class  varchar(256) not null default '' comment '处理器类',
    direction      tinyint      not null comment '方向：1-接收 2-发送',
    message_format tinyint      not null default 1 comment '报文格式：1-json 2-xml',
    http_method    tinyint      not null default 1 comment '请求方式：1-post 2-get 3-put',
    enabled        tinyint(1)      not null default 1 comment '启用状态：1-启用 0-禁用',
    remark         varchar(512) not null default '' comment '备注',
    created_by     varchar(64)  not null default 'system' comment '创建人',
    created_time   datetime     not null default current_timestamp comment '创建时间',
    updated_by     varchar(64)  not null default 'system' comment '更新人',
    updated_time   datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    unique index unq_api_code (api_code),
    index          idx_app_id (app_id)
) comment '接口配置表';

-- 接口日志表
drop table if exists api_log;
create table api_log
(
    id            bigint unsigned auto_increment primary key comment '主键id',
    biz_no        varchar(64)  not null default '' comment '业务编号',
    app_code      varchar(64)  not null default '' comment '应用编码',
    app_name      varchar(64)  not null default '' comment '应用名称',
    api_code      varchar(64)  not null default '' comment '接口编码',
    api_name      varchar(64)  not null default '' comment '接口名称',
    request_body  text         not null comment '请求报文',
    response_body text         not null comment '响应报文',
    retry_params  varchar(512) not null default '' comment '重试参数',
    status        tinyint(1)   not null default 1 comment '状态：1-成功 0-失败',
    error_message varchar(512) not null default '' comment '错误信息',
    created_time  datetime     not null default current_timestamp comment '创建时间',
    index         idx_app_code (app_code),
    index         idx_api_code (api_code),
    index         idx_created_time (created_time)
) comment '接口日志表';