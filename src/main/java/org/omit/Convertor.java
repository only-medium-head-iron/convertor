package org.omit;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.googlecode.aviator.AviatorEvaluator;

import lombok.extern.slf4j.Slf4j;
import org.omit.validate.ValidateProcessor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hepenglin
 * @since 2024-07-20 09:47
 **/
@Slf4j
@Service
public class Convertor {

    @Resource
    private ApiMapper apiMapper;

    /**
     * 根据规则ID和参数将字符串转换为指定类型的对象
     * 此方法首先使用规则ID和参数将字符串转换为一个对象，然后将该对象转换为由clazz参数指定的类型
     *
     * @param ruleId 规则ID，用于确定转换规则
     * @param params 转换过程中使用的参数
     * @param clazz 要转换的目标类型
     * @param <T> 泛型参数，表示转换后对象的类型
     * @return 转换后的对象
     */
    public <T> T convert(String ruleId, Map<String, Object> params, Class<T> clazz) {
        Object object = convert(ruleId, params);
        return BeanUtil.toBean(object, clazz);
    }

    /**
     * 根据规则ID和参数将数据转换为相应的对象格式
     *
     * @param ruleId 规则ID，用于确定转换规则
     * @param params 输入参数映射，包含转换所需的数据
     * @return 转换后的对象，如果没有相应的转换规则或转换失败，则返回null
     */
    public Object convert(String ruleId, Map<String, Object> params) {
        // 获取转换规则
        List<RuleConvert> ruleConverts = apiMapper.getConvertRules(ruleId);
        if (CollUtil.isEmpty(ruleConverts)) {
            log.warn("没有找到转换规则：{}", ruleId);
            return null;
        }

        String className = ruleConverts.get(0).getClassName();
        Class<?> clazz;
        if (StrUtil.isNotBlank(className)) {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ConvertException("类信息没有找到: " + className);
            }
        } else {
            clazz = Map.class;
        }
        // 解析转换规则
        LinkedHashMap<String, Object> populatedMap = new LinkedHashMap<>(16);
        Map<Long, List<RuleConvert>> groupedConvertRules = ruleConverts.stream()
                .filter(ruleConvert -> ruleConvert.getParentId() != 0)
                .collect(Collectors.groupingBy(RuleConvert::getParentId, Collectors.toList()));
        List<RuleConvert> parentRuleConverts = ruleConverts.stream()
                .filter(ruleConvert -> ruleConvert.getParentId() == 0)
                .collect(Collectors.toList());
        List<String> convertIds = ruleConverts.stream().map(ruleConvert -> String.valueOf(ruleConvert.getId())).collect(Collectors.toList());
        List<RuleMapping> ruleMappings = apiMapper.getMappingRules(Const.RuleType.DTO, convertIds);
        List<RuleMerge> ruleMerges = apiMapper.getMergeRules(convertIds);
        Map<String, List<RuleMapping>> groupedMappingRules = ruleMappings.stream().collect(Collectors.groupingBy(RuleMapping::getRuleId));
        Map<String, List<RuleMerge>> groupedMergeRules = ruleMerges.stream().collect(Collectors.groupingBy(RuleMerge::getRuleId));
        for (RuleConvert parentRuleConvert : parentRuleConverts) {
            if (mismatchConvertCondition(parentRuleConvert, params)) {
                // 条件不为空且不满足条件
                continue;
            }
            parseConvertRule(groupedConvertRules, groupedMappingRules, groupedMergeRules, populatedMap, parentRuleConvert, params, params);
        }
        Object object = BeanUtil.toBean(populatedMap, clazz);
        parseValidateRules(ruleId, object);
        return object;
    }

    /**
     * 执行 Aviator 表达式并返回计算结果
     * @param rule 规则，错误时打印结果
     * @param expression 表达式
     * @param params 参数
     * @return 执行结果
     */
    private Object aviatorExecute(Object rule, String expression, Map<String, Object> params) {
        if (StrUtil.isBlank(expression)) {
            return params;
        }

        Object o;
        try {
            o = AviatorEvaluator.execute(expression, params, true);
        } catch (Exception e) {
            log.error("rule = {}, sourceMap = {} 没有找到属性: {} ", rule, params, expression, e);
            throw new ConvertException(String.format("没有找到属性: %s", expression));
        }
        return o;
    }

    /**
     * 校验给定规则ID下的数据是否满足设定的校验规则
     *
     * @param ruleId 规则ID，用于识别具体的校验规则
     * @param object 转换后对象
     */
    private void parseValidateRules(String ruleId, Object object) {
        // 获取校验规则
        List<RuleValidate> ruleValidates = apiMapper.getValidateRules(ruleId);
        if (CollUtil.isEmpty(ruleValidates)) {
            log.warn("没有找到校验规则：{}", ruleId);
            return;
        }
        for (RuleValidate ruleValidate : ruleValidates) {
            String condition = ruleValidate.getCondition();
            Map<String, Object> sourceMap = BeanUtil.beanToMap(object);
            if (StrUtil.isNotBlank(condition)) {
                Object isMatchCondition = aviatorExecute(ruleValidate, condition, sourceMap);
                if (!Boolean.TRUE.equals(isMatchCondition)) {
                    continue;
                }
            }

            // 配置了自定义数据校验处理器则使用自定义处理器
            String validateProcessorBeanName = ruleValidate.getValidateProcessor();
            if (StrUtil.isNotBlank(validateProcessorBeanName)) {
                ValidateProcessor validateProcessor;
                try {
                    validateProcessor = SpringUtil.getBean(StrUtil.lowerFirst(validateProcessorBeanName), ValidateProcessor.class);
                } catch (NoSuchBeanDefinitionException e) {
                    throw new ConvertException(String.format("没有找到校验处理器，请确认是否配置正确: %s", validateProcessorBeanName));
                }
                validateProcessor.validate(ruleValidate, object);
            }

            String expression = ruleValidate.getExpression();
            if (StrUtil.isBlank(expression)) {
                continue;
            }
            Object isPass = aviatorExecute(ruleValidate, expression, sourceMap);
            if (Boolean.FALSE.equals(isPass)) {
                String message = ruleValidate.getMessage();
                throw new ConvertException(StrUtil.isBlank(message) ? "不满足条件" + expression : StringFormatter.format(message, sourceMap));
            }
        }
    }

    /**
     * 解析并应用转换规则
     *
     * @param populatedMap 待填充的映射，将转换结果存入此Map
     * @param ruleConvert 转换规则，指导如何进行数据转换
     * @param params 参数Map，作为转换的数据源
     */
    private void parseConvertRule(Map<Long, List<RuleConvert>> groupedConvertRules, Map<String, List<RuleMapping>> groupedMappingRules, Map<String, List<RuleMerge>> groupedMergeRules, LinkedHashMap<String, Object> populatedMap, RuleConvert ruleConvert, Map<String, Object> sourceMap, Map<String, Object> params) {
        sourceMap.put(Const.TARGET, populatedMap);
        Object o = aviatorExecute(ruleConvert, ruleConvert.getSource(), sourceMap);
        List<RuleMapping> ruleMappings = groupedMappingRules.get(String.valueOf(ruleConvert.getId()));
        if (o instanceof Map) {
            Map<String, Object> newSourceMap = new HashMap<>(params);
            newSourceMap.putAll(BeanUtil.beanToMap(o));
            LinkedHashMap<String, Object> parseResult = parseMappingRules(newSourceMap, ruleMappings);
            parseSubConvertRules(groupedConvertRules, groupedMappingRules, groupedMergeRules, parseResult, ruleConvert, newSourceMap, params);
            populate(populatedMap, ruleConvert.getTarget(), parseResult);
        } else if (o instanceof List) {
            List<Map<String, Object>> list = new ArrayList<>();
            // 转换成 List
            List<Map<String, Object>> mapList = ((List<?>) o).stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
            for (int i = 0; i < mapList.size(); i++) {
                Map<String, Object> newSourceMap = new HashMap<>(params);
                newSourceMap.putAll(mapList.get(i));
                newSourceMap.put(Const.AUTO_INCREMENT_LINE_NO, i + 1);
                LinkedHashMap<String, Object> parseResult = parseMappingRules(newSourceMap, ruleMappings);
                parseSubConvertRules(groupedConvertRules, groupedMappingRules, groupedMergeRules, parseResult, ruleConvert, newSourceMap, params);
                list.add(parseResult);
            }
            RuleMerge ruleMerge = matchMergeRule(ruleConvert, groupedMergeRules, params);
            List<Map<String, Object>> mergedList = parseMergeRule(ruleMerge, list);
            populate(populatedMap, ruleConvert.getTarget(), mergedList);
        }
        String dmlType = ruleConvert.getDmlType();
        String script = ruleConvert.getScript();
        if (StrUtil.isBlank(dmlType) || StrUtil.isBlank(script)) {
            return;
        }
        if (Const.DmlType.SPECIFIC.equals(dmlType)) {
            try {
                Map<String, Object> executeMap = new HashMap<>(params);
                executeMap.put("SQL", script);
                apiMapper.execute(executeMap);
            } catch (Exception e) {
                log.error("执行SQL失败：{}", e.getMessage(), e);
                throw new ConvertException("执行SQL失败");
            }
        }
    }

    /**
     * 解析子转换规则
     * <p>
     * 根据给定的规则集合和源映射，解析当前规则下的所有子规则，并将结果添加到解析结果中
     *
     * @param groupedConvertRules 所有规则的集合，映射为父规则ID与其子规则列表
     * @param populatedMap 将子规则的转换结果存储在此映射中
     * @param ruleConvert 当前正在解析的规则对象
     * @param sourceMap 原始数据源映射，用于查找和替换规则中的变量
     * @param params 其他参数，可能影响解析过程的额外参数
     */
    private void parseSubConvertRules(Map<Long, List<RuleConvert>> groupedConvertRules, Map<String, List<RuleMapping>> groupedMappingRules, Map<String, List<RuleMerge>> groupedMergeRules, LinkedHashMap<String, Object> populatedMap, RuleConvert ruleConvert, Map<String, Object> sourceMap, Map<String, Object> params) {
        Long id = ruleConvert.getId();
        List<RuleConvert> subRuleConverts = groupedConvertRules.get(id);
        if (CollUtil.isEmpty(subRuleConverts)) {
            return;
        }
        for (RuleConvert subRuleConvert : subRuleConverts) {
            if (mismatchConvertCondition(subRuleConvert, sourceMap)) {
                // 条件不为空且不满足条件
                continue;
            }
            parseConvertRule(groupedConvertRules, groupedMappingRules, groupedMergeRules, populatedMap, subRuleConvert, sourceMap, params);
        }
    }

    /**
     * 根据给定的规则转换条件判断是否不匹配
     * 此方法用于评估一个规则的条件是否不满足，主要用于规则引擎中筛选或判断逻辑
     *
     * @param ruleConvert 规则转换对象，包含规则的条件表达式
     * @param params 用于执行条件表达式的一组参数
     * @return 返回一个布尔值，如果条件表达式不匹配则为true，否则为false
     */
    private boolean mismatchConvertCondition(RuleConvert ruleConvert, Map<String, Object> params) {
        String condition = ruleConvert.getCondition();
        if (StrUtil.isNotBlank(condition)) {
            Object isMatchCondition = aviatorExecute(ruleConvert, condition, params);
            boolean flag = !Boolean.TRUE.equals(isMatchCondition);
            if (flag) {
                log.info("转换规则条件不为空且不满足条件：rule = {}, params = {}", ruleConvert, params);
            }
            return flag;
        }
        return false;
    }

    /**
     * 根据提供的对象填充或更新映射
     * 如果传入的映射为空，则使用对象的属性值填充映射
     * 否则，仅更新映射中与目标属性相关的值
     *
     * @param populatedMap 待填充或更新的映射
     * @param target 目标属性名，用于更新映射
     * @param obj 用于填充映射的源对象
     */
    private void populate(Map<String, Object> populatedMap, String target, Object obj) {
        if (MapUtil.isEmpty(populatedMap) && StrUtil.isBlank(target)) {
            populatedMap.putAll(BeanUtil.beanToMap(obj));
            return;
        }
        populateMap(populatedMap, target, obj);
    }

    /**
     * 根据合并规则合并数据，配置了自定义数据合并处理器则使用自定义处理器，否则使用默认合并逻辑
     *
     * @param ruleMerge 合并规则
     * @param mapList 待合并的数据列表，每个Map代表一条数据记录
     * @return 合并后的数据列表
     *
     * 该方法首先根据ruleId获取合并规则，如果规则或待合并的数据列表为空，则直接返回原数据列表
     */
    private List<Map<String, Object>> parseMergeRule(RuleMerge ruleMerge, List<Map<String, Object>> mapList) {
        if (CollUtil.isEmpty(mapList)) {
            return mapList;
        }

        if (ruleMerge == null) {
            return mapList;
        }

        // 配置了自定义数据合并处理器则使用自定义处理器，否则使用默认合并逻辑
        String mergeProcessorBeanName = ruleMerge.getMergeProcessor();
        if (StrUtil.isNotBlank(mergeProcessorBeanName)) {
            MergeProcessor mergeProcessor;
            try {
                mergeProcessor = SpringUtil.getBean(StrUtil.lowerFirst(mergeProcessorBeanName), MergeProcessor.class);
            } catch (NoSuchBeanDefinitionException e) {
                throw new ConvertException(String.format("没有找到合并处理器，请确认是否配置正确: %s", mergeProcessorBeanName));
            }
            return mergeProcessor.merge(ruleMerge, mapList);
        }

        return defaultMergeProcessing(mapList, ruleMerge);
    }

    /**
     * 默认数据合并逻辑
     * @param mapList 数据列表
     * @param ruleMerge 数据合并规则
     * @return 合并后的数据列表
     *
     * 合并规则包括分组字段（groupField）和合并字段（mergeField），如果这两个字段任意一个为空，方法会记录警告日志并返回原数据列表
     * <p>
     * 数据合并过程：
     * 1. 根据groupField将数据列表进行分组，groupField可以是多个字段以逗号分隔
     * 2. 对每个分组内的数据，将mergeField字段的值进行累加，并将累加结果存放在一个新的Map中
     * 3. 将所有分组的累加结果Map添加到mergedList列表中，并返回该列表作为合并后的数据
     */
    private List<Map<String, Object>> defaultMergeProcessing(List<Map<String, Object>> mapList, RuleMerge ruleMerge) {
        List<Map<String, Object>> mergedList = new ArrayList<>();

        String groupFields = ruleMerge.getGroupField();
        String mergeFields = ruleMerge.getMergeField();
        String sortFields = ruleMerge.getSortField();

        if (StrUtil.hasBlank(groupFields, mergeFields)) {
            log.warn("{} 分组合并字段配置为空", ruleMerge);
            return mapList;
        }
        List<String> mergeFieldList = StrUtil.splitTrim(mergeFields, ",");
        List<String> sortFieldList = StrUtil.splitTrim(sortFields, ",");

        mapList.stream().collect(Collectors.groupingBy(map -> {
                    StringBuilder sb = new StringBuilder();
                    StrUtil.splitTrim(groupFields, ",").forEach(key -> sb.append(map.get(key)));
                    return sb.toString();
                }, Collectors.toList()))
                .forEach((k, v) -> {
                    // 排序
                    if (CollUtil.isNotEmpty(sortFieldList)) {
                        Comparator<Map<String, Object>> comparator = buildComparator(sortFieldList);
                        v.sort(comparator);
                    }

                    // 合并
                    v.stream().reduce((a, b) -> {
                        for (String mergeField : mergeFieldList) {
                            BigDecimal value1 = new BigDecimal(MapUtil.getStr(a, mergeField, "0"));
                            BigDecimal value2 = new BigDecimal(MapUtil.getStr(b, mergeField, "0"));
                            a.put(mergeField, value1.add(value2));
                        }
                        return a;
                    }).ifPresent(mergedList::add);
                });
        return mergedList;
    }

    /**
     * 构建用于比较 Map<String, Object> 的Comparator
     * 该 Comparator 根据提供的排序字段列表对Map进行排序
     * 每个排序字段可以指定为升序或降序
     * 排序字段格式：productDate,lineNo desc 不配置排序方式默认升序
     *
     * @param sortFieldList 排序字段列表，每个元素是一个包含字段名和可选排序顺序（"asc"或"desc"）的字符串
     * @return 一个Comparator<Map<String, Object>>，用于比较Map对象
     * @throws ConvertException 如果排序字段配置错误
     */
    private Comparator<Map<String, Object>> buildComparator(List<String> sortFieldList) {
        Comparator<Map<String, Object>> comparator = (map1, map2) -> 0;

        for (String sortField : sortFieldList) {
            List<String> sortFieldWithType = StrUtil.splitTrim(sortField, StrUtil.SPACE);

            if (CollUtil.size(sortFieldWithType) <= 0 || CollUtil.size(sortFieldWithType) > 2) {
                throw new ConvertException("排序字段配置错误，请确认是否配置正确");
            }

            String field = sortFieldWithType.get(0);
            @SuppressWarnings("unchecked")
            Comparator<Map<String, Object>> fieldComparator = Comparator.comparing(
                    map -> (Comparable<Object>) map.get(field),
                    Comparator.nullsLast(Comparator.naturalOrder())
            );

            if (CollUtil.size(sortFieldWithType) == 2) {
                String sortType = sortFieldWithType.get(1);
                if ("desc".equalsIgnoreCase(sortType)) {
                    fieldComparator = fieldComparator.reversed();
                }
            }

            comparator = comparator.thenComparing(fieldComparator);
        }

        return comparator;
    }

    /**
     * 根据规则ID和参数获取匹配的合并规则
     * @param ruleConvert 规则ID
     * @param params 参数
     * @return 匹配的规则
     */
    private RuleMerge matchMergeRule(RuleConvert ruleConvert, Map<String, List<RuleMerge>> groupedMergeRules, Map<String, Object> params) {
        Long id = ruleConvert.getId();
        List<RuleMerge> ruleMerges = groupedMergeRules.get(String.valueOf(id));
        if (CollUtil.isEmpty(ruleMerges)) {
            log.info("没有匹配到合并规则：{}", ruleConvert);
            return null;
        }
        for (RuleMerge ruleMerge : ruleMerges) {
            String condition = ruleMerge.getCondition();
            if (StrUtil.isBlank(condition)) {
                return ruleMerge;
            } else {
                Object isMatchCondition = aviatorExecute(ruleMerge, condition, params);
                if (Boolean.TRUE.equals(isMatchCondition)) {
                    return ruleMerge;
                }
            }
        }

        log.info("没有匹配到合并规则：{}", ruleConvert);
        return null;
    }

    /**
     * 根据映射规则解析输入的映射对象
     * <p>
     * 本函数解析输入的映射对象sourceMap，根据一系列映射规则rules，
     * 将符合条件的值转换并存储到新的映射对象targetMap中
     *
     * @param sourceMap 原始映射对象，包含待转换的值
     * @param targetMap 目标映射对象，包含转换后的值
     * @param rules 映射规则列表，用于定义如何转换值
     */
    public void parseMappingRules(Map<String, Object> sourceMap, LinkedHashMap<String, Object> targetMap, List<RuleMapping> rules) {
        doParseMappingRules(sourceMap, targetMap, rules);
    }

    /**
     * 根据映射规则解析输入的映射对象
     * <p>
     * 本函数解析输入的映射对象sourceMap，根据一系列映射规则rules，
     * 将符合条件的值转换并存储到新的映射对象targetMap中
     *
     * @param sourceMap 原始映射对象，包含待转换的值
     * @param rules 映射规则列表，用于定义如何转换值
     * @return 转换后的映射对象
     */
    public LinkedHashMap<String, Object> parseMappingRules(Map<String, Object> sourceMap, List<RuleMapping> rules) {
        LinkedHashMap<String, Object> targetMap = new LinkedHashMap<>(16);
        if (CollUtil.isEmpty(rules)) {
            return targetMap;
        }
        doParseMappingRules(sourceMap, targetMap, rules);
        return targetMap;
    }

    /**
     * 根据映射规则解析输入的映射对象
     * <p>
     * 本函数解析输入的映射对象sourceMap，根据一系列映射规则rules，
     * 将符合条件的值转换并存储到新的映射对象targetMap中
     *
     * @param sourceMap 原始映射对象，包含待转换的值
     * @param rules 映射规则列表，用于定义如何转换值
     */
    private void doParseMappingRules(Map<String, Object> sourceMap, LinkedHashMap<String, Object> targetMap, List<RuleMapping> rules) {
        for (RuleMapping rule : rules) {
            String condition = rule.getCondition();
            if (StrUtil.isNotBlank(condition)) {
                Object isMatchCondition = aviatorExecute(rule, condition, sourceMap);
                if (!Boolean.TRUE.equals(isMatchCondition)) {
                    log.warn("{} 规则不满足条件", rule);
                    continue;
                }
            }
            // 校验表达式是否正确
            validateExpression(sourceMap, rule);
            String target = rule.getTarget();
            String type = rule.getType();
            Object value;

            ValueStrategy valueStrategy = ValueStrategyFactory.get(type);
            if (valueStrategy == null) {
                log.error("不支持的取值类型：{}", type);
                throw new ConvertException("不支持的取值类型：" + type);
            }

            value = valueStrategy.getValue(sourceMap, rule);

            if (ObjectUtil.isEmpty(value)) {
                // 校验必填字段
                validateRequiredFields(sourceMap, rule);
                log.warn("{} 字段转换后值为空", rule);
                continue;
            }
            if (StrUtil.isBlank(target)) {
                log.warn("{} 目标字段为空", rule);
                continue;
            }
            target = determineWhetherToStoreToContext(target, value);
            populateMap(targetMap, target, value);
        }
    }

    /**
     * 校验表达式是否满足条件
     * 该方法用于执行一个给定的规则表达式，并根据表达式的执行结果决定是否抛出异常
     *
     * @param sourceMap 包含源数据的映射，用于表达式中变量的替换
     * @param ruleMapping 映射规则
     */
    private void validateExpression(Map<String, Object> sourceMap, RuleMapping ruleMapping) {
        String expression = ruleMapping.getValidateExpression();
        if (StrUtil.isBlank(expression)) {
            return;
        }
        Object isPass = aviatorExecute(ruleMapping, expression, sourceMap);
        if (!Boolean.TRUE.equals(isPass)) {
            String message = ruleMapping.getMessage();
            throw new ConvertException(StrUtil.isBlank(message) ? "不满足条件" + expression : StringFormatter.format(message, sourceMap));
        }
    }

    /**
     * 校验必填字段
     * @param sourceMap 数据源
     * @param rule 映射规则
     */
    private void validateRequiredFields(Map<String, Object> sourceMap, RuleMapping rule) {
        if (Const.Requirement.REQUIRED.equals(rule.getRequired())) {
            String message = StrUtil.isBlank(rule.getMessage()) ? rule.getTarget() + "字段为空，但字段配置为必填" : StringFormatter.format(rule.getMessage(), sourceMap);
            throw new ConvertException(message);
        }
    }

    private void validateRequiredFields(List<RuleMapping> rules, Map<String, Object> sourceMap, Map<String, Object> targetMap) {
        rules.stream().filter(rule -> Const.Requirement.REQUIRED.equals(rule.getRequired())).forEach(rule -> {
            String target = StrUtil.replace(rule.getTarget(), "#", "");
            String message = StrUtil.isBlank(rule.getMessage()) ? rule.getTarget() + "字段为空，但字段配置为必填" : StringFormatter.format(rule.getMessage(), sourceMap);
            if (!targetMap.containsKey(target)) {
                throw new ConvertException(message);
            }
            Object value = targetMap.get(target);
            if (value == null) {
                throw new ConvertException(message);
            }
            if (value instanceof String && StrUtil.isBlank((String) value)) {
                throw new ConvertException(message);
            }
        });
    }

    /**
     * 将值填充到嵌套的映射中
     * 本方法通过一个点分隔的键字符串将值填充到嵌套的映射（Map）中
     * 例如，对于键"key1.key2"，如果key1不存在，则会创建一个空的Map，并将key2映射到给定的值
     *
     * @param nestedMap 嵌套的映射，其中将填充键和值
     * @param target 包含嵌套键的字符串，使用点分隔
     * @param value 要填充的值
     */
    public static void populateMap(Map<String, Object> nestedMap, String target, Object value) {
        String[] keys = StrUtil.splitToArray(target, StrUtil.DOT);
        populateMap(nestedMap, keys, value, 0);
    }

    /**
     * 填充嵌套的映射
     * <p>
     * 本方法通过递归的方式填充嵌套的映射，直到到达最后一个键
     *
     * @param currentMap 当前的嵌套映射，其中将填充键和值
     * @param keys 包含嵌套键的字符串数组，使用点分隔
     * @param value 要填充的值
     * @param index 当前递归的索引，用于跟踪当前处理的键
     */
    @SuppressWarnings("unchecked")
    private static void populateMap(Map<String, Object> currentMap, String[] keys, Object value, int index) {
        if (index == keys.length - 1) {
            currentMap.put(keys[index], value);
        } else {
            Map<String, Object> nextMap = (Map<String, Object>) currentMap.computeIfAbsent(keys[index], k -> new HashMap<>(16 ));
            populateMap(nextMap, keys, value, index + 1);
        }
    }

    /**
     * 判断是否需要存储到上下文
     * <p>
     * 本方法判断给定的目标字符串是否以"#"结尾，如果是，则将目标字符串转换为"#"后面的字符串，并将值存储到上下文
     *
     * @param target 目标字符串，可能以"#"结尾
     * @param value 要存储的值
     * @return 如果需要存储到上下文，则返回"#"后面的字符串，否则返回原始的目标字符串
     */
    private String determineWhetherToStoreToContext(String target, Object value) {
        if (StrUtil.contains(target, StrPool.HASH)) {
            target = StrUtil.replace(target, StrPool.HASH, "");
            Context context = ContextHolder.get();
            if (ObjectUtil.isEmpty(context)) {
                return target;
            }
            Map<String, Object> temp = context.getTemp();
            if (temp == null) {
                temp = new HashMap<>(16);
            }
            temp.put(target, value);
            context.setTemp(temp);
        }
        return target;
    }
}
