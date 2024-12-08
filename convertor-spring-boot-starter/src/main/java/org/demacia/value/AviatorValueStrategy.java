package org.demacia.value;

import cn.hutool.core.util.StrUtil;
import com.googlecode.aviator.AviatorEvaluator;

import lombok.extern.slf4j.Slf4j;
import org.demacia.enums.ValueType;
import org.demacia.ConvertException.ConvertException;
import org.demacia.rule.RuleMapping;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Aviator 执行策略
 * 没取到则返回映射规则配置的默认值
 *
 * @author hepenglin
 * @since 2024-07-27 23:03
 **/
@Slf4j
@Service
public class AviatorValueStrategy implements ValueStrategy {

    @Override
    public Object getValue(Map<String, Object> sourceMap, RuleMapping rule) {
        String source = rule.getSource();
        if (StrUtil.isBlank(source)) {
            return rule.getDefaultValue();
        }
        try {
            Object o = AviatorEvaluator.execute(source, sourceMap, true);
            return o == null ? rule.getDefaultValue() : o;
        } catch (ConvertException e) {
            throw e;
        } catch (Exception e) {
            log.error("rule = {}, sourceMap = {} 没有找到属性: {}", rule, sourceMap, e.getMessage(), e);
            throw new ConvertException(String.format("没有找到属性: %s", source));
        }
    }

    @Override
    public void afterPropertiesSet() {
        ValueStrategyFactory.register(ValueType.AVIATOR.getType(), this);
    }
}