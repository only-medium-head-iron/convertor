package org.demacia.rule;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 输出规则类
 * 用于定义数据转换和输出的规则配置信息。
 *
 * @author hepenglin
 * @since 2024-07-21 10:58
 **/
@Accessors(chain = true)
@Data
public class RuleConvert {

    /**
     * 规则的唯一标识符。
     * <p>
     * 用于唯一标识一条规则。
     */
    private Long id;

    /**
     * 父规则的唯一标识符。
     * <p>
     * 用于唯一标识一条规则。
     */
    private Long parentId;

    /**
     * 规则编码。
     * <p>
     * 规则编码。
     */
    private String ruleCode;

    /**
     * 规则名称。
     * <p>
     * 规则名称。
     */
    private String ruleName;

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
     * 规则的目标，指明目标字段的对象。
     * <p>
     * 该目标描述了指明目标字段的对象。
     */
    private String target;

    /**
     * 数据操作语言（DML）类型，用于指定操作类型。
     * <p>
     * 该类型描述了数据操作的类型，如插入、更新等。
     */
    private String dmlType;

    /**
     * 类名，用于指定处理数据的类。
     * <p>
     * 该类名描述了哪个类负责处理数据。
     */
    private String className;

    /**
     * 表名，用于指定数据库表。
     * <p>
     * 该表名描述了数据操作的目标表。
     */
    private String tableName;

    /**
     * 需要执行的脚本。
     * <p>
     * 该声明或实现细节描述了规则的具体内容。
     */
    private String script;

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