-- 创建数据库
create
    database if not exists `convertor_db` default character set utf8mb4 collate utf8mb4_general_ci;
use
    `convertor_db`;

-- 应用配置表
drop table if exists app_config;
create table app_config
(
    id          bigint unsigned auto_increment primary key comment '自增主键',
    app_code    varchar(64)  not null comment '应用编码',
    app_name    varchar(64)  not null comment '应用名称',
    app_key     varchar(64)  not null default '' comment '应用键名',
    app_secret  varchar(128) not null default '' comment '应用密钥',
    base_url    varchar(256) not null default '' comment '基础地址',
    enabled     bit(1)       not null default 1 comment '是否启用',
    remark      varchar(512) not null default '' comment '备注说明',
    create_by   bigint                default null comment '创建人主键',
    create_name varchar(64)           default null comment '创建人名称',
    create_time datetime     not null default current_timestamp comment '创建时间',
    update_by   bigint                default null comment '更新人主键',
    update_name varchar(64)           default null comment '更新人名称',
    update_time datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    deleted     bit(1)       not null default 0 comment '是否删除',
    unique index unq_app_code (app_code)
) comment '应用配置表';

-- 接口配置表
drop table if exists api_config;
create table api_config
(
    id             bigint unsigned auto_increment primary key comment '自增主键',
    app_id         bigint unsigned not null comment '应用主键',
    api_code       varchar(256)    not null comment '接口编码',
    api_name       varchar(256)    not null comment '接口名称',
    api_path       varchar(256)    not null default '' comment '接口路径',
    handler_class  varchar(256)    not null default '' comment '处理器类',
    direction      tinyint         not null comment '方向：1-接收 2-发送',
    message_format varchar(8)      not null default 'JSON' comment '报文格式：JSON/XML',
    http_method    varchar(8)      not null default 'POST' comment '请求方式：POST/GET/PUT',
    enabled        bit(1)          not null default 1 comment '是否启用',
    remark         varchar(512)    not null default '' comment '备注说明',
    create_by      bigint                   default null comment '创建人主键',
    create_name    varchar(64)              default null comment '创建人名称',
    create_time    datetime        not null default current_timestamp comment '创建时间',
    update_by      bigint                   default null comment '更新人主键',
    update_name    varchar(64)              default null comment '更新人名称',
    update_time    datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
    deleted        bit(1)          not null default 0 comment '是否删除',
    unique index unq_api_code (api_code),
    index idx_app_id (app_id)
) comment '接口配置表';

-- 接口日志表
drop table if exists api_log;
create table api_log
(
    id             bigint unsigned auto_increment primary key comment '自增主键',
    biz_no         varchar(64)     not null default '' comment '业务编号',
    app_code       varchar(64)     not null default '' comment '应用编码',
    app_name       varchar(64)     not null default '' comment '应用名称',
    api_code       varchar(64)     not null default '' comment '接口编码',
    api_name       varchar(64)     not null default '' comment '接口名称',
    request_body   text            not null comment '请求报文',
    response_body  text            not null comment '响应报文',
    retry_params   text            not null comment '重试参数',
    request_result tinyint(1)      not null default 1 comment '请求结果（1-成功 2-失败）',
    error_message  varchar(512)    not null default '' comment '错误信息',
    cost           bigint unsigned not null default 0 comment '接口耗时（单位：毫秒）',
    create_time    datetime        not null default current_timestamp comment '创建时间',
    index idx_biz_no (biz_no),
    index idx_app_code (app_code),
    index idx_api_code (api_code),
    index idx_create_time (create_time)
) comment '接口日志表';

drop table if exists rule_mapping;
CREATE TABLE `rule_mapping`
(
    id                  bigint unsigned not null auto_increment primary key comment '自增主键',
    rule_type           varchar(64)     not null default '' comment '规则类型：REQ-请求，PRE-前置，DTO-对象，RHD-请求头，RSP-响应',
    rule_code           varchar(64)     not null default '' comment '规则编码',
    `condition`         varchar(256)    not null default '' comment '触发条件',
    source              varchar(256)    not null default '' comment '数据来源',
    target              varchar(64)     not null default '' comment '目标字段',
    default_value       varchar(64)              default '' comment '默认值，没取到值时使用',
    value_type          varchar(2)               default '0' comment '取值类型，0-默认值，1-MAP取值，2-Aviator取值，3-JS脚本取值，4-SQL取值',
    script              text comment '脚本内容',
    validate_expression varchar(256)             default '' comment '校验表达式',
    required            bit(1)          not null default 0 comment '是否必填',
    message             varchar(256)             default '' comment '执行错误提示信息',
    priority            int             not null default 0 comment '执行顺序',
    remark              varchar(256)             default '' comment '备注说明',
    create_by           bigint                   default null comment '创建人主键',
    create_name         varchar(64)              default null comment '创建人名称',
    create_time         datetime        not null default current_timestamp comment '创建时间',
    update_by           bigint                   default null comment '更新人主键',
    update_name         varchar(64)              default null comment '更新人名称',
    update_time         datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
    deleted             bit(1)          not null default 0 comment '是否删除',
    index idx_rule_type_rule_code (rule_type, rule_code)
) comment ='映射规则表';

drop table if exists rule_convert;
CREATE TABLE `rule_convert`
(
    id          bigint unsigned not null auto_increment primary key comment '自增主键',
    parent_id   bigint unsigned not null default 0 comment '父级规则配置的唯一标识',
    rule_code   varchar(64)     not null default '' comment '规则编码',
    rule_name   varchar(64)     not null default '' comment '规则名称',
    `condition` varchar(256)    not null default '' comment '条件表达式，用于指定规则生效的条件',
    source      varchar(256)             default '' comment '数据源位置',
    target      varchar(256)             default '' comment '目标位置',
    class_name  varchar(64)     not null default '' comment '生成对象全路径名',
    dml_type    char(1)         not null default '' comment '1-insert, 2-specific（表名存在时生效）',
    table_name  varchar(64)     not null default '' comment '规则作用的表名',
    script      text comment '脚本内容',
    template    text comment '模板内容',
    priority    int             not null default 0 comment '排序规则，用于指定数据输出的顺序',
    remark      varchar(256)             default '' comment '备注说明',
    create_by   bigint                   default null comment '创建人主键',
    create_name varchar(64)              default null comment '创建人名称',
    create_time datetime        not null default current_timestamp comment '创建时间',
    update_by   bigint                   default null comment '更新人主键',
    update_name varchar(64)              default null comment '更新人名称',
    update_time datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
    deleted     bit(1)          not null default 0 comment '是否删除',
    index idx_rule_code (rule_code)
) comment ='转换规则表';

drop table if exists rule_merge;
create table `rule_merge`
(
    id              bigint unsigned not null auto_increment primary key comment '自增主键',
    rule_code       varchar(64)     not null default '' comment '规则编码',
    `condition`     varchar(256)    not null default '' comment '规则的条件表达式，用于定义规则的触发条件',
    merge_processor varchar(256)    not null default '' comment '合并处理器',
    group_field     varchar(256)    not null default '' comment '分组字段',
    merge_field     varchar(256)    not null default '' comment '合并字段',
    sort_field      varchar(256)    not null default '' comment '排序字段',
    priority        int             not null default 0 comment '规则执行顺序（从小到大）',
    remark          varchar(256)             default '' comment '备注说明',
    create_by       bigint                   default null comment '创建人主键',
    create_name     varchar(64)              default null comment '创建人名称',
    create_time     datetime        not null default current_timestamp comment '创建时间',
    update_by       bigint                   default null comment '更新人主键',
    update_name     varchar(64)              default null comment '更新人名称',
    update_time     datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
    deleted         bit(1)          not null default 0 comment '是否删除',
    index idx_rule_code (rule_code)
) comment ='合并规则表';

drop table if exists rule_validate;
create table `rule_validate`
(
    id                 bigint unsigned not null auto_increment primary key comment '自增主键',
    rule_code          varchar(64)     not null default '' comment '规则编码',
    `condition`        varchar(256)    not null default '' comment '条件表达式，用于指定规则生效的条件',
    validate_processor varchar(256)    not null default '' comment '校验处理器',
    expression         varchar(256)    not null default '' comment '表达式，校验的表达式',
    message            varchar(256)             default '' comment '规则的提示信息',
    priority           int             not null default 0 comment '排序规则',
    remark             varchar(256)             default '' comment '备注说明',
    create_by          bigint                   default null comment '创建人主键',
    create_name        varchar(64)              default null comment '创建人名称',
    create_time        datetime        not null default current_timestamp comment '创建时间',
    update_by          bigint                   default null comment '更新人主键',
    update_name        varchar(64)              default null comment '更新人名称',
    update_time        datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
    deleted            bit(1)          not null default 0 comment '是否删除',
    index idx_rule_code (rule_code)
) comment ='校验规则表';
