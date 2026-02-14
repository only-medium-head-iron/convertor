package org.demacia.domain;

import lombok.Data;

/**
 * @author hepenglin
 * @since 2024/12/8 9:29
 */
@Data
public class Log {

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 业务编号
     */
    private String bizNo;

    /**
     * 应用编码
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 接口编码
     */
    private String apiCode;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 请求报文
     */
    private String requestMessage;

    /**
     * 响应报文
     */
    private String responseMessage;

    /**
     * 重试参数
     */
    private String retryParams;

    /**
     * 请求结果
     */
    private Boolean requestResult;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 接口耗时（单位：毫秒）
     */
    private Long cost;
}
