package org.demacia.value;

import org.demacia.rule.RuleMapping;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * @author hepenglin
 * @since 2024-07-27 23:00
 **/
public interface ValueStrategy extends InitializingBean {

    /**
     * 获取值
     * @param sourceMap 源数据
     * @param rule 映射规则
     * @return 值
     */
    Object getValue(Map<String, Object> sourceMap, RuleMapping rule);
}
