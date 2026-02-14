package org.demacia.domain;

import cn.hutool.json.JSONUtil;
import lombok.Data;

/**
 * @author hepenglin
 * @since 2026/2/14 15:50
 **/
@Data
public class DefaultResponse {

    /**
     * 请求结果
     * <p>
     * 默认值为 {@code false}。
     */
    private boolean success;

    /**
     * 错误编码
     */
    private String code;

    /**
     * 错误信息
     */
    private String message;

    public static DefaultResponse failure(String code, String message) {
        DefaultResponse defaultResponse = new DefaultResponse();
        defaultResponse.setSuccess(false);
        defaultResponse.setCode(code);
        defaultResponse.setMessage(message);
        return defaultResponse;
    }
}
