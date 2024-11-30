package org.omit.util;

import com.googlecode.aviator.AviatorEvaluator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hepenglin
 * @since 2024-08-26 23:04
 **/
@Slf4j
public class StrFormatter {

    private static final Pattern PATTERN = Pattern.compile("\\{([^\\}]+)\\}");

    /**
     * 格式化字符串，替换其中的 {key} 占位符为实际的值。
     *
     * @param template 需要格式化的模板字符串
     * @param sourceMap 包含 key-value 对应关系的 Map
     * @return 格式化后的字符串
     */
    public static String format(String template, Map<String, Object> sourceMap) {
        Matcher matcher = PATTERN.matcher(template);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            // 获取占位符名称
            String key = matcher.group(1);
            // 使用 AviatorEvaluator 获取对应变量的值
            Object value = null;
            try {
                value = AviatorEvaluator.execute(key, sourceMap, true);
            } catch (Exception e) {
                log.warn("格式化字符串时，获取变量值失败，变量名：{}，错误信息：{}", key, e.getMessage(), e);
            }
            if (value != null) {
                // 替换占位符
                matcher.appendReplacement(sb, value.toString());
            } else {
                // 如果没有找到对应的值，则保留原样
                matcher.appendReplacement(sb, matcher.group());
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
