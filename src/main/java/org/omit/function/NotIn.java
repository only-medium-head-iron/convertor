package org.omit.function;

import cn.hutool.core.util.StrUtil;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * @author hepenglin
 * @since 2024-08-04 16:25
 **/
public class NotIn extends AbstractFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        Object target = arg1.getValue(env);
        boolean in = StrUtil.split(arg2.toString(), ",").contains(target.toString());
        return AviatorBoolean.valueOf(!in);
    }

    @Override
    public String getName() {
        return "notIn";
    }
}
