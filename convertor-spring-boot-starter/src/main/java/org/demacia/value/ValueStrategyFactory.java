package org.demacia.value;

import cn.hutool.core.util.StrUtil;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ValueStrategy工厂类，用于注册和获取ValueStrategy实例。
 *
 * @author hepenglin
 * @since 2024-07-28 17:15
 **/
public class ValueStrategyFactory {

    private final static ConcurrentHashMap<String, ValueStrategy> VALUE_STRATEGY_MAP = new ConcurrentHashMap<>();

    /**
     * 注册ValueStrategy实例。
     *
     * @param type          ValueStrategy的类型，用于标识和检索。
     * @param valueStrategy 要注册的ValueStrategy实例，不能为空。
     * @throws IllegalArgumentException 如果type或valueStrategy为null，则抛出此异常。
     */
    public static void register(String type, ValueStrategy valueStrategy) {

        // 检查 type 是否为空字符串
        if (StrUtil.isBlank(type)) {
            throw new IllegalArgumentException("参数'type'不能为空或空字符串。");
        }
        // 检查 valueStrategy 是否为空
        if (valueStrategy == null) {
            throw new IllegalArgumentException("参数'valueStrategy'不能为空。");
        }

        // 检查 type 是否已存在于 VALUE_STRATEGY_MAP 中
        ValueStrategy existingStrategy = VALUE_STRATEGY_MAP.get(type);
        if (existingStrategy != null) {
            throw new IllegalArgumentException("策略'valueStrategy'已经存在。");
        }

        // 可以在这里添加更复杂的注册逻辑，例如检查重复的type等。
        VALUE_STRATEGY_MAP.put(type, valueStrategy);
    }

    /**
     * 根据类型获取ValueStrategy实例。
     *
     * @param type ValueStrategy的类型，用于检索对应的实例。
     * @return 对应type的ValueStrategy实例，如果未找到，则返回null。
     */
    public static ValueStrategy get(String type) {
        return VALUE_STRATEGY_MAP.get(type);
    }

    /**
     * 移除指定类型的ValueStrategy实例。
     *
     * @param type 要移除的ValueStrategy的类型。
     * @return 如果成功移除，则返回true；否则返回false，例如当type不存在时。
     */
    public static boolean remove(String type) {
        return VALUE_STRATEGY_MAP.remove(type) != null;
    }
}
