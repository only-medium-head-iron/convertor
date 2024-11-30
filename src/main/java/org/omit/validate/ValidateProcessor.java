package org.omit.validate;

import org.omit.rule.RuleValidate;

/**
 * @author hepenglin
 * @since 2024-10-21 15:41
 **/
public interface ValidateProcessor {

    /**
     * 校验数据
     * @param ruleValidate 校验规则
     * @param object 转换后对象
     */
    void validate(RuleValidate ruleValidate, Object object);
}
