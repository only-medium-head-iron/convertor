package org.demacia.value;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.demacia.enums.ValueType;
import org.demacia.exception.ConvertException;
import org.demacia.rule.RuleMapping;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 执行 SQL 脚本获取，并将结果放入源 MAP
 * 如果配置了 source 则返回 source 对应的值，否则将整个结果放入 sourceMap，以供后续取值
 * @author hepenglin
 * @since 2024-07-27 23:03
 **/
@Slf4j
@Component
public class SqlValueStrategy implements ValueStrategy {

    @Resource
    private ApiMapper apiMapper;

    @Override
    public Object getValue(Map<String, Object> sourceMap, RuleMapping rule) {
        sourceMap.put("SQL", rule.getScript());
        List<Map<String, Object>> resultMapList = apiMapper.select(sourceMap);
        if (CollUtil.isEmpty(resultMapList)) {
            String message = StrUtil.isBlank(rule.getMessage()) ? String.format("rule = %s, script = %s, error: %s", rule, rule.getScript(), "解析SQL值为空") : StringFormatter.format(rule.getMessage(), sourceMap);
            throw new ConvertException(message);
        }
        if (resultMapList.size() > 1) {
            String message = StrUtil.isBlank(rule.getMessage()) ? String.format("rule = %s, script = %s, error: %s", rule, rule.getScript(), "解析SQL值存在多个") : StringFormatter.format(rule.getMessage(), sourceMap);
            throw new ConvertException(message);
        }
        Map<String, Object> objectMap = resultMapList.get(0);

        // 如果配置了 source，则直接返回 source 对应的值
        String source = rule.getSource();
        if (StrUtil.isNotBlank(source)) {
            return objectMap.get(source);
        }

        // 没配置 source，则将整个结果放入 sourceMap，以供后续取值
        sourceMap.put(rule.getTarget(), MapUtil.toCamelCaseMap(objectMap));
        return null;
    }

    @Override
    public void afterPropertiesSet() {
        ValueStrategyFactory.register(ValueType.SQL.getType(), this);
    }
}
