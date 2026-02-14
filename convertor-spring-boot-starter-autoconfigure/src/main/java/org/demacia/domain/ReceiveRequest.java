package org.demacia.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author hepenglin
 * @since 2024-09-05 19:50
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveRequest {

    /**
     * 标志是否内部调用。
     * <p>
     * 默认值为 {@code false}，表示外部接口调用。
     */
    @JsonIgnore
    private boolean internalRetry = false;

    /**
     * 应用编码。
     * <p>
     * 应用编码。
     */
    private String appCode;

    /**
     * 应用KEY。
     * <p>
     * 应用KEY。
     */
    private String appKey;

    /**
     * 路径参数。
     * <p>
     * 采用 Map 形式存储，便于灵活处理各类请求参数。
     */
    private Map<String, String> queryParams;

    /**
     * 请求头。
     * <p>
     * 采用 Map 形式存储，便于灵活处理各类请求参数。
     */
    private Map<String, String> headers;

    /**
     * 请求报文。
     * <p>
     * 采用 String 形式存储，便于灵活处理各类请求参数。
     */
    private String reqMsg;
}
