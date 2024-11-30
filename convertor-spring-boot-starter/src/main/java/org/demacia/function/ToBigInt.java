package org.demacia.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBigInt;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * @author lining
 * @since 2024-08-11 15:39
 **/
public class ToBigInt extends AbstractFunction {

    /**
     * 判断给定的参数转换为 Integer 类型
     *
     * @param env  执行环境，包含所有变量及其值
     * @param arg1 要检查的参数，可以是任何AviatorObject类型
     * @return 返回一个AviatorInteger对象，表示arg1是否为 整数 类型
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object value = arg1.getValue(env);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            Number number = (Number) value;
            if (number instanceof BigInteger) {
                return AviatorBigInt.valueOf(number);
            } else if (number instanceof Long) {
                return AviatorBigInt.valueOf((Long) number);
            } else if (number instanceof Integer) {
                return AviatorBigInt.valueOf((Integer) number);
            } else if (number instanceof Double && ((Double) number).doubleValue() == ((Double) number).longValue()) {
                return AviatorBigInt.valueOf(((Double) number).longValue());
            } else if (number instanceof Float && ((Float) number).floatValue() == (((Float) number).longValue())) {
                return AviatorBigInt.valueOf(((Float) number).longValue());
            } else if (value instanceof BigDecimal) {
                BigDecimal valBigDecimal = (BigDecimal) value;
                if (valBigDecimal.scale() <= 0 || valBigDecimal.stripTrailingZeros().scale() <= 0) {
                    return AviatorBigInt.valueOf(valBigDecimal.longValue());
                }
            }
        }
        throw new IllegalArgumentException("Argument is not an integer: " + value);
    }

    @Override
    public String getName() {
        return "toBigInt";
    }
}
