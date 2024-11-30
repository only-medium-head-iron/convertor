package org.omit.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.List;
import java.util.Map;

/**
 * @author hepenglin
 * @since 2024-08-11 15:27
 **/
public class Sum extends AbstractFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        Object value = arg1.getValue(env);
        if (value instanceof List) {
            // TODO 遇到类似场景实现
        }

        return super.call(env, arg1);
    }

    @Override
    public String getName() {
        return "sum";
    }
}
