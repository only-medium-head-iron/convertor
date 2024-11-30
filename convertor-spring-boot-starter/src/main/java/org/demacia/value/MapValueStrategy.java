package org.demacia.value;

import org.demacia.enums.ValueType;
import org.demacia.rule.RuleMapping;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Map取值策略，直接从源 Map 中取值
 * {@link AviatorValueStrategy} 支持从 Map 中取值和表达式取值
 *
 * @author hepenglin
 * @since 2024-07-27 23:01
 **/
@Component
public class MapValueStrategy implements ValueStrategy {

    @Override
    public Object getValue(Map<String, Object> sourceMap, RuleMapping rule) {
        String source = rule.getSource();
        Object o = sourceMap.get(source);
        return o == null ? rule.getDefaultValue() : o;
    }

    @Override
    public void afterPropertiesSet() {
        ValueStrategyFactory.register(ValueType.MAP.getType(), this);
    }
}
