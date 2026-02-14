package org.demacia.domain;

import lombok.Data;

/**
 * @author hepenglin
 * @since 2024/12/8 7:58
 */
@Data
public class Api {

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 应用主键
     */
    private Long appId;

    /**
     * 接口编码
     */
    private String apiCode;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 接口路径
     */
    private String apiPath;

    /**
     * 处理器类
     */
    private String handlerClass;

    /**
     * 方向：1-接收 2-发送
     */
    private Integer direction;

    /**
     * 报文格式：JSON/XML
     */
    private String messageFormat;

    /**
     * 请求方式：POST/GET/PUT
     */
    private String httpMethod;

    /**
     * 是否启用
     */
    private Boolean enabled;
}
