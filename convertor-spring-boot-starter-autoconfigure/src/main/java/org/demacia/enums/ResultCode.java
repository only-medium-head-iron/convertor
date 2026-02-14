package org.demacia.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author hepenglin
 * @since 2024/11/20 11:00
 **/
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS("0", "成功"),
    FAILURE("500", "失败"),

    // 请求相关 (1000-1999)
    INVALID_REQUEST("1000", "无效请求"),
    API_CODE_REQUIRED("1001", "API编码不能为空"),
    APP_CODE_REQUIRED("1002", "应用编码不能为空"),
    INVALID_MESSAGE_FORMAT("1003", "无效的消息格式"),
    REQUEST_VALIDATION_FAILED("1004", "请求验证失败"),

    // 应用相关 (2000-2999)
    APP_NOT_FOUND("2000", "应用不存在"),
    APP_QUERY_ERROR("2001", "查询应用失败"),

    // API相关 (3000-3999)
    API_NOT_FOUND("3000", "API不存在"),
    API_QUERY_ERROR("3001", "查询API失败"),

    // 处理器相关 (4000-4999)
    HANDLER_NOT_FOUND("4000", "处理器不存在"),

    // 映射相关 (5000-5999)
    REQ_MAPPING_ERROR("5000", "请求映射失败"),
    PRE_MAPPING_ERROR("5001", "前置映射失败"),
    RSP_MAPPING_ERROR("5002", "响应映射失败"),

    // 签名相关 (6000-6999)
    SIGNATURE_MISSING("6000", "签名缺失"),
    SIGNATURE_INVALID("6001", "签名无效"),
    ;

    private final String code;

    private final String message;
}
