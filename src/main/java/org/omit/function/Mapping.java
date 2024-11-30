package org.omit.function;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义Aviator函数，用于根据提供的映射规则将输入值转换为目标值。
 * <p>
 * 此函数接受两个参数：
 * - 第一个参数是需要转换的原始值。
 * - 第二个参数是映射规则字符串，格式为：key1,key2,...->value1|key3,key4,...->value2|...
 *   其中，'|' 分隔不同的映射规则，',' 分隔同一规则下的多个键，'->' 指示键值对。
 * <p>
 * 函数将根据第二个参数中的映射规则，查找第一个参数对应的键，并返回相应的值。
 * 如果未找到对应的键，则返回 null。
 *
 * @author hepenglin
 * @since 2024-08-11 16:30
 */
public class Mapping extends AbstractFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        Object obj1 = arg1.getValue(env);
        Object obj2 = arg2.getValue(env);

        // 检查obj1和obj2是否为null
        if (obj1 == null || obj2 == null) {
            throw new IllegalArgumentException(String.format("mapping函数入参不能为空: %s, %s", obj1, obj2));
        }

        // 确保obj2转换成字符串后不为空
        String obj2Str = obj2.toString();
        if (StrUtil.isBlank(obj2Str)) {
            throw new IllegalArgumentException("mapping表达式不能为空");
        }

        Map<String, String> map = new HashMap<>(16);
        List<String> keyValues = StrUtil.split(obj2Str, "|");

        // 处理空列表的情况
        if (CollUtil.isEmpty(keyValues)) {
            throw new IllegalArgumentException("mapping表达式中没有有效的键值对");
        }

        for (String s : keyValues) {
            String[] keyValue = StrUtil.splitToArray(s, "->");

            if (keyValue.length != 2) {
                throw new IllegalArgumentException(String.format("mapping表达式格式错误: %s (期望格式: key1,key2->value)", obj2));
            }

            List<String> keys = StrUtil.split(keyValue[0], ",");

            if (CollUtil.isEmpty(keys)) {
                throw new IllegalArgumentException("mapping表达式中的键列表不能为空");
            }

            String value = keyValue[1];
            for (String key : keys) {
                map.put(key.trim(), value);
            }
        }

        String value1 = obj1.toString();
        return new AviatorString(map.get(value1));
    }

    @Override
    public String getName() {
        return "mapping";
    }
}
