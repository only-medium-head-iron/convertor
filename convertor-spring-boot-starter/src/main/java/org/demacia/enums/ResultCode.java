package org.demacia.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author hepenglin
 * @since 2024/11/20 11:00
 **/
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS("0", "成功"),

    /**
     * 失败
     */
    FAILURE("500", "失败");

    private final String code;

    private final String message;
}
