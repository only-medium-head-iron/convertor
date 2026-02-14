package org.demacia.domain;

import lombok.Data;

/**
 * @author hepenglin
 * @since 2024/12/8 7:42
 */
@Data
public class App {

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 应用编码
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用键名
     */
    private String appKey;

    /**
     * 应用密钥
     */
    private String appSecret;

    /**
     * 基础地址
     */
    private String baseUrl;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 是否校验签名
     */
    private Boolean signRequired;
}
