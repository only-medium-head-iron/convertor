package org.demacia.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author hepenglin
 * @since 2024-07-28 20:29
 **/
@Slf4j
@Service
public class GetTime extends AbstractFunction {

    /**
     * 获取时间戳毫秒值
     * 用法：getTime(sysdate())
     *
     * @param env 取值环境
     * @param arg1 java.util.Date 类型日期
     * @return 时间戳毫秒值
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object value = arg1.getValue(env);
        if (value instanceof Date) {
            Date date = (Date) value;
            return AviatorNumber.valueOf(date.getTime());
        }
        return null;
    }

    @Override
    public String getName() {
        return "getTime";
    }
}
