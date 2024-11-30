package org.omit.value;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.omit.enums.ValueType;
import org.omit.rule.RuleMapping;
import org.springframework.stereotype.Component;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Map;

/**
 * JavaScript 脚本执行策略
 *
 * @author hepenglin
 * @since 2024-08-11 12:45
 **/
@Slf4j
@Component
public class ScriptValueStrategy implements ValueStrategy {

    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("nashorn");

    @Override
    public Object getValue(Map<String, Object> sourceMap, RuleMapping rule) {
        // JavaScript 脚本
        String script = rule.getScript();
        if (StrUtil.isBlank(script)) {
            return null;
        }
        // 绑定参数
        Bindings bindings = SCRIPT_ENGINE.createBindings();
        bindings.putAll(sourceMap);

        try {
            Object o = SCRIPT_ENGINE.eval(script, bindings);
            String defaultValue = rule.getDefaultValue();
            return o == null ? defaultValue : o;
        } catch (Exception e) {
            log.error("映射规则：{} 脚本执行异常：", rule.getRuleId(), e);
            throw new RuntimeException(StrUtil.format("映射规则：{} 脚本执行异常：" + rule.getRuleId()), e);
        }
    }

    @Override
    public void afterPropertiesSet() {
        ValueStrategyFactory.register(ValueType.JS_SCRIPT.getType(), this);
    }
}
