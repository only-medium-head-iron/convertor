package org.omit.function;

import cn.hutool.core.util.StrUtil;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * @author hepenglin
 * @since 2024-08-04 16:16
 **/
public class In extends AbstractFunction {

    /**
     * 判断一个对象是否存在于给定的逗号分隔的字符串列表中
     * <p>
     * 示例：
     * - 如果 env 中的 arg1 是 "apple" 并且 arg2 是 "banana,apple,orange"，inC(arg1, 'banana,apple,orange') 则返回 AviatorBoolean.TRUE。
     * - 如果 env 中的 arg1 是 "grape" 并且 arg2 是 "banana,apple,orange"，inC(arg1, 'banana,apple,orange') 则返回 AviatorBoolean.FALSE。
     *
     * @param env 环境变量映射表，用于执行上下文相关的操作
     * @param arg1 要检查的对象
     * @param arg2 包含潜在匹配项的逗号分隔的字符串
     * @return 如果对象存在于字符串列表中，则返回 AviatorBoolean.TRUE，否则返回 AviatorBoolean.FALSE
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        Object target = arg1.getValue(env);
        boolean in = StrUtil.split(arg2.toString(), ",").contains(target.toString());
        return AviatorBoolean.valueOf(in);
    }

    @Override
    public String getName() {
        return "inC";
    }
}
