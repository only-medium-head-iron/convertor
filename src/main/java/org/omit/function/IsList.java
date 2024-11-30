package org.omit.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.List;
import java.util.Map;

/**
 * @author hepenglin
 * @since 2024-08-11 15:39
 **/
public class IsList extends AbstractFunction {

    /**
     * 判断给定的参数是否为列表类型
     *
     * @param env 执行环境，包含所有变量及其值
     * @param arg1 要检查的参数，可以是任何AviatorObject类型
     * @return 返回一个AviatorBoolean对象，表示arg1是否为 List 类型
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object value = arg1.getValue(env);
        if (value instanceof List<?>) {
            return AviatorBoolean.valueOf(true);
        }
        return AviatorBoolean.valueOf(false);
    }

    @Override
    public String getName() {
        return "isList";
    }
}
