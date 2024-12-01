package org.demacia.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.*;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Options;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

/**
 * @author hepenglin
 * @since 2024/8/13 23:10
 */
@Component
public class AviatorEvaluatorConfigurer {

    @PostConstruct
    public void init() throws Exception {
        AviatorEvaluator.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
        AviatorEvaluator.getInstance().useLRUExpressionCache(5000);
        AviatorEvaluator.addStaticFunctions("LocalDateTime", LocalDateTime.class);
        AviatorEvaluator.addStaticFunctions("LocalDateTimeUtil", LocalDateTimeUtil.class);
        AviatorEvaluator.addStaticFunctions("DateUtil", DateUtil.class);
        AviatorEvaluator.addStaticFunctions("IdUtil", IdUtil.class);
        IdUtil.fastSimpleUUID();
        AviatorEvaluator.addStaticFunctions("StrUtil", StrUtil.class);
        AviatorEvaluator.addStaticFunctions("ObjectUtil", ObjectUtil.class);
        AviatorEvaluator.addStaticFunctions("NumberUtil", NumberUtil.class);
        AviatorEvaluator.addStaticFunctions("CollUtil", CollUtil.class);
        AviatorEvaluator.addStaticFunctions("JSONUtil", JSONUtil.class);
        AviatorEvaluator.addStaticFunctions("XmlUtil", XmlUtil.class);
        AviatorEvaluator.addStaticFunctions("DigestUtil", DigestUtil.class);
    }
}
