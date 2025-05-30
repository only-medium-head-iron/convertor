package org.demacia.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.demacia.domain.Column;
import org.demacia.rule.RuleConvert;
import org.demacia.rule.RuleMapping;
import org.demacia.rule.RuleMerge;
import org.demacia.rule.RuleValidate;

import java.util.List;
import java.util.Map;

/**
 * @author hepenglin
 * @since 2024/12/8 8:02
 */
@Mapper
public interface RuleMapper {

    List<RuleMapping> getMappingRulesByRuleCode(String ruleType, String ruleCode);

    /**
     * 执行SQL语句
     * @param map SQL Map
     */
    void execute(Map<String, Object> map);

    /**
     * 查询SQL语句
     * @param map SQL Map
     * @return 结果集
     */
    List<Map<String, Object>> select(Map<String, Object> map);

    /**
     * 获取表字段信息
     * @param tableName 表名
     * @return 字段信息
     */
    List<Column> getColumns(String tableName);

    /**
     * 获取映射规则
     * @param ruleType 规则类型
     * @param ruleCode 规则标识
     * @return 映射规则列表
     */
    List<RuleMapping> getMappingRulesByRuleId(@Param("ruleType") String ruleType, @Param("ruleCode") String ruleCode);

    /**
     * 获取映射规则
     * @param ruleType 规则类型
     * @param ruleCodes 规则标识
     * @return 映射规则列表
     */
    List<RuleMapping> getMappingRules(@Param("ruleType") String ruleType, @Param("ruleCodes") List<String> ruleCodes);

    /**
     * 获取转换规则
     * @param ruleCode 规则标识
     * @return 转换规则列表
     */
    List<RuleConvert> getConvertRules(String ruleCode);

    /**
     * 获取合并规则
     * @param ruleCodes 规则标识
     * @return 合并规则列表
     */
    List<RuleMerge> getMergeRules(@Param("ruleCodes") List<String> ruleCodes);

    /**
     * 获取校验规则
     * @param ruleCode 规则标识
     * @return 校验规则列表
     */
    List<RuleValidate> getValidateRules(String ruleCode);
}
