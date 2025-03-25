package org.demacia.value;

import cn.hutool.core.util.StrUtil;
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
        String[] keys = StrUtil.splitToArray(source, ".");
        Object o = getValue(sourceMap, keys, 0);
        return o == null ? rule.getDefaultValue() : o;
    }

    private Object getValue(Map<String, Object> map, String[] keys, int index) {
        if (index == keys.length - 1) {
            return map.get(keys[index]);
        } else {
            Object nextMap = map.get(keys[index]);
            if (nextMap instanceof Map) {
                return getValue((Map<String, Object>) nextMap, keys, index + 1);
            } else {
                return null;
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        ValueStrategyFactory.register(ValueType.MAP.getType(), this);
    }
}
