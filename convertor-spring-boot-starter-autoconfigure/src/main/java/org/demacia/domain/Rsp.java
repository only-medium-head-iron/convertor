package org.demacia.domain;


import lombok.Data;
import org.demacia.enums.ResultCode;

/**
 * @author hepenglin
 * @since 2024-08-05 09:45
 **/
@Data
public class Rsp {

    /**
     * 是否直接调用。
     * <p>
     * 默认值为 {@code false}。
     */
    private boolean success;

    /**
     * 返回码。
     */
    private String code;

    /**
     * 记录详细错误信息，供内部展示。
     */
    private String message;

    /**
     * 记录模糊错误信息，供外部展示。
     */
    private String messageExternal;

    /**
     * 返回数据。
     */
    private Object data;

    public static Rsp success() {
        Rsp rsp = new Rsp();
        rsp.setSuccess(true);
        rsp.setCode(ResultCode.SUCCESS.getCode());
        rsp.setMessage(ResultCode.SUCCESS.getMessage());
        return rsp;
    }
}
