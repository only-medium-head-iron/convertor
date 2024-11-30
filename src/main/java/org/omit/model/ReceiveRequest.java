package org.omit.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Map;

/**
 * @author hepenglin
 * @since 2024-09-05 19:50
 **/
@Data
public class ReceiveRequest {

    /**
     * 是否直接调用。
     * <p>
     * 默认值为 {@code true}，用于区分是否记录日志和清除上下文。
     */
    @JSONField(serialize = false)
    private boolean directCall = true;

    /**
     * 标志是否内部调用。
     * <p>
     * 默认值为 {@code false}，表示外部接口调用。
     */
    @JSONField(serialize = false)
    private boolean internalRetry = false;

    /**
     * 应用编码。
     * <p>
     * 应用编码。
     */
    private String appCode;

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
