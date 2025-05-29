create database if not exists `convertor` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `convertor`;

drop table if exists convertor.application;
create table convertor.application
(
    id          bigint unsigned auto_increment primary key comment '主键id',
    app_code    varchar(64)  not null default '' comment '应用编码',
    app_name    varchar(64)  not null default '' comment '应用名称',
    app_key     varchar(64)  not null default '' comment '应用密钥',
    app_secret  varchar(64)  not null default '' comment '应用密钥',
    app_url     varchar(64)  not null default '' comment '应用url',
    enable      bit(1)       not null default 1 comment '是否启用：1-启用，0-禁用',
    remark      varchar(512) not null default '' comment '备注',
    create_by   varchar(64)  not null default '',
    create_time datetime     not null default current_timestamp,
    update_by   varchar(64)  not null default '',
    update_time datetime     not null default current_timestamp on update current_timestamp
) comment '应用表';


drop table if exists convertor.interface;
create table convertor.interface
(
    id             bigint unsigned auto_increment primary key comment '主键id',
    app_id         bigint unsigned comment '应用id',
    interface_code varchar(64)  not null default '' comment '接口编码',
    interface_name varchar(64)  not null default '' comment '接口名称',
    interface_url  varchar(256) not null default '' comment '接口url',
    direction      tinyint      not null comment '接口方向：1-接收，2-发送',
    message_format tinyint      not null default 1 comment '报文格式：1-json，2-xml',
    http_method    tinyint      not null default 1 comment '请求方式：1-post，2-get, 3-put',
    enable         bit(1)       not null default 1 comment '是否启用：1-启用，0-禁用',
    remark         varchar(512) not null default '' comment '备注',
    create_by      varchar(64)  not null default '',
    create_time    datetime     not null default current_timestamp,
    update_by      varchar(64)  not null default '',
    update_time    datetime     not null default current_timestamp on update current_timestamp
) comment '接口表';


drop table if exists convertor.interface_log;
create table convertor.interface_log
(
    id               bigint unsigned auto_increment primary key comment '主键id',
    app_code         varchar(64)  not null default '' comment '应用编码',
    app_name         varchar(64)  not null default '' comment '应用名称',
    interface_code   varchar(64)  not null default '' comment '接口编码',
    interface_name   varchar(64)  not null default '' comment '接口名称',
    request_message  text         not null default '' comment '请求报文',
    response_message text         not null default '' comment '响应报文',
    retry_params     text         not null default '' comment '重试参数',
    result           bit(1)       not null default 1 comment '请求结果：1-成功，0-失败',
    remark           varchar(512) not null default '' comment '失败原因',
    create_time      datetime     not null default current_timestamp
) comment '接口日志表';
create index idx_app_code on convertor.interface_log (app_code);