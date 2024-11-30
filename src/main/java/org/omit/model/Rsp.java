package org.omit.model;


import lombok.Data;

/**
 * @author hepenglin
 * @since 2024-08-05 09:45
 **/
@Data
public class Rsp {

    /**
     * 是否直接调用。
     * <p>
     * 默认值为 {@code false}，用于区分是否记录日志。
     */
    private boolean success;

    /**
     * 返回码。
     * <p>
     * 返回码。
     */
    private String code;

    /**
     * 记录详细错误信息，供内部展示。
     * <p>
     * 记录详细错误信息，供内部展示。
     */
    private String message;

    /**
     * 记录模糊错误信息，供外部展示。
     * <p>
     * 记录模糊错误信息，供外部展示。
     */
    private String outerMessage;
}
