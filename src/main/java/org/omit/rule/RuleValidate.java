package org.omit.rule;

import lombok.Data;

/**
 * @author hepenglin
 * @since 2024-08-13 14:37
 **/
@Data
public class RuleValidate {

    /**
     * 规则的唯一标识符。
     * <p>
     * 用于唯一标识一条规则。
     */
    private Long id;

    /**
     * 规则的ID，用于标识一条规则。
     * <p>
     * 该ID是规则的标识符。
     */
    private String ruleId;

    /**
     * 规则的条件，描述了规则执行的前提条件。
     * <p>
     * 该条件用于判断是否满足规则执行的条件。
     */
    private String condition;

    /**
     * 自定义校验处理类。
     * <p>
     * 该字段描述了需要自定义校验处理类。
     */
    private String validateProcessor;

    /**
     * 校验表达式。
     * <p>
     * 校验表达式。
     */
    private String expression;

    /**
     * 校验不通过返回信息。
     * <p>
     * 校验不通过返回信息。
     */
    private String message;

    /**
     * 规则的排序顺序，用于确定规则执行的先后顺序。
     * <p>
     * 该排序顺序决定了规则执行时的优先级。
     */
    private Integer sort;

    /**
     * 备注。
     * <p>
     * 备注。
     */
    private String remark;
}
