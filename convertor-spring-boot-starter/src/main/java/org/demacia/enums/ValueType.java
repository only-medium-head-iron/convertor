package org.demacia.enums;

import lombok.Getter;

/**
 * @author hepenglin
 * @since 2024-08-26 16:30
 **/
@Getter
public enum ValueType {

    /**
     * 固定值
     */
    FIXED("0", "固定值"),

    /**
     * MAP取值
     */
    MAP("1", "MAP取值"),

    /**
     * Aviator取值
     */
    AVIATOR("2", "Aviator取值"),

    /**
     * JS脚本取值
     */
    JS_SCRIPT("3", "JS脚本取值"),

    /**
     * SQL取值
     */
    SQL("4", "SQL取值");

    private final String type;
    private final String desc;

    ValueType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

}
