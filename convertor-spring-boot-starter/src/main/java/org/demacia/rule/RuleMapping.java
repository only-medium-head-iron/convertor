package org.demacia.rule;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 转换规则类，用于定义数据转换的规则和条件。
 * @author hepenglin
 * @since 2024-07-20 11:11
 **/
@Accessors(chain = true)
@Data
public class RuleMapping {

    /**
     * 规则的唯一标识符。
     * <p>
     * 用于唯一标识一条规则。
     */
    private Long id;

    /**
     * 规则的ID，用于唯一标识一条规则。
     * <p>
     * 该ID是规则的唯一标识符。
     */
    private String ruleId;

    /**
     * 规则的条件，描述了规则执行的前提条件。
     * <p>
     * 该条件用于判断是否满足规则执行的条件。
     */
    private String condition;

    /**
     * 规则的来源，指明规则应用的对象或上下文。
     * <p>
     * 该来源描述了规则的上下文环境。
     */
    private String source;

    /**
     * 规则的目标，指明规则执行后的影响对象。
     * <p>
     * 该目标描述了规则执行后影响的对象或结果。
     */
    private String target;

    /**
     * source没取到时默认值。
     * <p>
     * source没取到时默认值。
     */
    private String defaultValue;

    /**
     * 规则的类型，用于区分不同类型的规则。
     * <p>
     * 0-默认值，1-MAP取值，2-Aviator取值，3-JS脚本取值，4-SQL取值。
     */
    private String type;

    /**
     * 需要执行的脚本。
     * <p>
     * 该声明或实现细节描述了规则的具体内容。
     */
    private String script;

    /**
     * 校验表达式，对源数据进行校验。
     * <p>
     * 校验表达式。
     */
    private String validateExpression;

    /**
     * 必填选项，0-非必填，1-必填。
     * <p>
     * 该声明或实现细节描述了必填选项。
     */
    private String required;

    /**
     * 校验提示信息。
     * <p>
     * 该声明或实现细节描述了校验提示信息。
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
