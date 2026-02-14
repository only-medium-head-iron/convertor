package org.demacia.exception;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demacia.enums.ResultCode;

/**
 * @author hepenglin
 * @since 2024/11/20 11:03
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class ConvertException extends RuntimeException {

    private final String code;
    private final String message;

    public ConvertException(String message) {
        this.code = ResultCode.FAILURE.getCode();
        this.message = message;
    }

    public ConvertException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ConvertException(ResultCode resultCode, Object... params) {
        this.code = resultCode.getCode();
        this.message = StrUtil.format(resultCode.getMessage(), params);
    }
}
