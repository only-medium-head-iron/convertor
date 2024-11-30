package org.demacia.rule;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 合并规则类
 * 用于定义在特定条件下如何合并数据对象的规则。
 * 通过规则ID、分组字段和合并字段，确定如何将数据集中的多个对象合并为一个对象。
 * @author hepenglin
 * @since 2024-07-28 20:34
 **/
@Accessors(chain = true)
@Data
public class RuleMerge {

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
     * 自定义合并处理类。
     * <p>
     * 该字段描述了需要自定义合并处理类。
     */
    private String mergeProcessor;

    /**
     * 分组字段，用于指定数据分组的依据。
     * <p>
     * 该字段用于确定如何对数据进行分组。
     */
    private String groupField;

    /**
     * 合并字段，用于指定需要合并的数据字段。
     * <p>
     * 该字段描述了哪些数据字段需要进行合并操作。
     */
    private String mergeField;

    /**
     * 排序字段，用于指定需要排序的数据字段。
     * <p>
     * 格式：productDate,lineNo asc 不配置排序方式默认升序
     */
    private String sortField;

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
