package org.omit.value;

import org.omit.enums.ValueType;
import org.omit.rule.RuleMapping;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 取映射规则 source 字段固定值
 *
 * @author hepenglin
 * @since 2024-07-27 23:03
 **/
@Component
public class FixedValueStrategy implements ValueStrategy {

    @Override
    public Object getValue(Map<String, Object> sourceMap, RuleMapping rule) {
        return rule.getSource();
    }

    @Override
    public void afterPropertiesSet() {
        ValueStrategyFactory.register(ValueType.FIXED.getType(), this);
    }
}
