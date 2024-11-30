package org.omit.model;

import lombok.Data;

/**
 * @author hepenglin
 * @since 2024-08-02 15:42
 **/
@Data
public class Req {

    /**
     * 接收报文格式。
     * <p>
     * 接收报文格式。
     */
    private String format = "json";

    /**
     * 接收的签名。
     * <p>
     * 接收的签名。
     */
    private String rcvSign;

    /**
     * OMS生成的签名。
     * <p>
     * OMS生成的签名。
     */
    private String genSign;

    /**
     * 请求id。
     * <p>
     * 请求id，用于校验是否重复请求。
     */
    private String reqId;

    /**
     * 请求时间戳。
     * <p>
     * 请求时间戳，用于校验是否过期请求。
     */
    private Long timestamp;

    /**
     * 应用代码，用于标识应用系统。
     * <p>
     * 该代码用于唯一标识应用系统。
     */
    private String appCode;

    /**
     * 服务代码，用于标识服务。
     * <p>
     * 该代码用于唯一标识服务。
     */
    private String serviceCode;
}
