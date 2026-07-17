package com.siact.hydrocore.common.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-11-28 14:35
 * @className : CollectionUtils
 * @description : 集合工具类
 * <p>
 * 提供类似 JDK 9 的 {@code Map.of()} 工厂方法实现，用于创建不可变的 Map 实例。
 * <p>
 * 这些方法创建包含指定键值对的 Map 实例，具有以下特性：
 * <ul>
 *   <li>创建的 Map 是<b>不可修改</b>的</li>
 *   <li>不允许 {@code null} 键或值</li>
 *   <li>键必须是唯一的，重复键会抛出 {@code IllegalArgumentException}</li>
 *   <li>迭代顺序与参数顺序一致</li>
 * </ul>
 *
 * <p>示例用法：
 * <pre>{@code
 * Map<String, Integer> emptyMap = MapUtils.emptyMap();
 * Map<String, Integer> singleMap = MapUtils.of("one", 1);
 * Map<String, Integer> multiMap = MapUtils.of("one", 1, "two", 2, "three", 3);
 * }</pre>
 */
public final class MapUtils {

    private MapUtils() {
    }


    /**
     * 返回一个空的不可修改的 Map
     *
     * @param <K> Map 键的类型
     * @param <V> Map 值的类型
     * @return 一个空的不可修改的 Map
     */
    public static <K, V> java.util.Map<K, V> emptyMap() {
        return Collections.emptyMap();
    }

    /**
     * 返回包含单个键值对的不可修改的 Map。
     *
     * @param k1  第一个键
     * @param v1  第一个值
     * @param <K> Map 键的类型
     * @param <V> Map 值的类型
     * @return 包含指定键值对的不可修改的 Map
     * @throws NullPointerException 如果键或值为 {@code null}
     */
    public static <K, V> java.util.Map<K, V> of(K k1, V v1) {
        requireNonNull(k1, v1);
        java.util.Map<K, V> map = new HashMap<>(1);
        map.put(k1, v1);
        return Collections.unmodifiableMap(map);
    }

    /**
     * 返回包含两个键值对的不可修改的 Map。
     *
     * @param k1  第一个键
     * @param v1  第一个值
     * @param k2  第二个键
     * @param v2  第二个值
     * @param <K> Map 键的类型
     * @param <V> Map 值的类型
     * @return 包含指定键值对的不可修改的 Map
     * @throws NullPointerException     如果任何键或值为 {@code null}
     * @throws IllegalArgumentException 如果有重复的键
     */
    public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2) {
        requireNonNull(k1, v1, k2, v2);
        java.util.Map<K, V> map = new HashMap<>(2);
        putAndCheckDuplicate(map, k1, v1);
        putAndCheckDuplicate(map, k2, v2);
        return Collections.unmodifiableMap(map);
    }

    /**
     * 返回包含三个键值对的不可修改的 Map。
     *
     * @param k1  第一个键
     * @param v1  第一个值
     * @param k2  第二个键
     * @param v2  第二个值
     * @param k3  第三个键
     * @param v3  第三个值
     * @param <K> Map 键的类型
     * @param <V> Map 值的类型
     * @return 包含指定键值对的不可修改的 Map
     * @throws NullPointerException     如果任何键或值为 {@code null}
     * @throws IllegalArgumentException 如果有重复的键
     */
    public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        requireNonNull(k1, v1, k2, v2, k3, v3);
        java.util.Map<K, V> map = new HashMap<>(3);
        putAndCheckDuplicate(map, k1, v1);
        putAndCheckDuplicate(map, k2, v2);
        putAndCheckDuplicate(map, k3, v3);
        return Collections.unmodifiableMap(map);
    }

    /**
     * 返回包含四个键值对的不可修改的 Map。
     *
     * @param k1  第一个键
     * @param v1  第一个值
     * @param k2  第二个键
     * @param v2  第二个值
     * @param k3  第三个键
     * @param v3  第三个值
     * @param k4  第四个键
     * @param v4  第四个值
     * @param <K> Map 键的类型
     * @param <V> Map 值的类型
     * @return 包含指定键值对的不可修改的 Map
     * @throws NullPointerException     如果任何键或值为 {@code null}
     * @throws IllegalArgumentException 如果有重复的键
     */
    public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        requireNonNull(k1, v1, k2, v2, k3, v3, k4, v4);
        java.util.Map<K, V> map = new HashMap<>(4);
        putAndCheckDuplicate(map, k1, v1);
        putAndCheckDuplicate(map, k2, v2);
        putAndCheckDuplicate(map, k3, v3);
        putAndCheckDuplicate(map, k4, v4);
        return Collections.unmodifiableMap(map);
    }

    /**
     * 返回包含五个键值对的不可修改的 Map。
     *
     * @param k1  第一个键
     * @param v1  第一个值
     * @param k2  第二个键
     * @param v2  第二个值
     * @param k3  第三个键
     * @param v3  第三个值
     * @param k4  第四个键
     * @param v4  第四个值
     * @param k5  第五个键
     * @param v5  第五个值
     * @param <K> Map 键的类型
     * @param <V> Map 值的类型
     * @return 包含指定键值对的不可修改的 Map
     * @throws NullPointerException     如果任何键或值为 {@code null}
     * @throws IllegalArgumentException 如果有重复的键
     */
    public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        requireNonNull(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
        java.util.Map<K, V> map = new HashMap<>(5);
        putAndCheckDuplicate(map, k1, v1);
        putAndCheckDuplicate(map, k2, v2);
        putAndCheckDuplicate(map, k3, v3);
        putAndCheckDuplicate(map, k4, v4);
        putAndCheckDuplicate(map, k5, v5);
        return Collections.unmodifiableMap(map);
    }

    /**
     * 返回包含六个键值对的不可修改的 Map。
     *
     * @param k1  第一个键
     * @param v1  第一个值
     * @param k2  第二个键
     * @param v2  第二个值
     * @param k3  第三个键
     * @param v3  第三个值
     * @param k4  第四个键
     * @param v4  第四个值
     * @param k5  第五个键
     * @param v5  第五个值
     * @param k6  第六个键
     * @param v6  第六个值
     * @param <K> Map 键的类型
     * @param <V> Map 值的类型
     * @return 包含指定键值对的不可修改的 Map
     * @throws NullPointerException     如果任何键或值为 {@code null}
     * @throws IllegalArgumentException 如果有重复的键
     */
    public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        requireNonNull(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
        java.util.Map<K, V> map = new HashMap<>(6);
        putAndCheckDuplicate(map, k1, v1);
        putAndCheckDuplicate(map, k2, v2);
        putAndCheckDuplicate(map, k3, v3);
        putAndCheckDuplicate(map, k4, v4);
        putAndCheckDuplicate(map, k5, v5);
        putAndCheckDuplicate(map, k6, v6);
        return Collections.unmodifiableMap(map);
    }

    /**
     * 返回包含七个键值对的不可修改的 Map。
     *
     * @param k1  第一个键
     * @param v1  第一个值
     * @param k2  第二个键
     * @param v2  第二个值
     * @param k3  第三个键
     * @param v3  第三个值
     * @param k4  第四个键
     * @param v4  第四个值
     * @param k5  第五个键
     * @param v5  第五个值
     * @param k6  第六个键
     * @param v6  第六个值
     * @param k7  第七个键
     * @param v7  第七个值
     * @param <K> Map 键的类型
     * @param <V> Map 值的类型
     * @return 包含指定键值对的不可修改的 Map
     * @throws NullPointerException     如果任何键或值为 {@code null}
     * @throws IllegalArgumentException 如果有重复的键
     */
    public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        requireNonNull(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
        java.util.Map<K, V> map = new HashMap<>(7);
        putAndCheckDuplicate(map, k1, v1);
        putAndCheckDuplicate(map, k2, v2);
        putAndCheckDuplicate(map, k3, v3);
        putAndCheckDuplicate(map, k4, v4);
        putAndCheckDuplicate(map, k5, v5);
        putAndCheckDuplicate(map, k6, v6);
        putAndCheckDuplicate(map, k7, v7);
        return Collections.unmodifiableMap(map);
    }

    /**
     * 返回包含八个键值对的不可修改的 Map。
     *
     * @param k1  第一个键
     * @param v1  第一个值
     * @param k2  第二个键
     * @param v2  第二个值
     * @param k3  第三个键
     * @param v3  第三个值
     * @param k4  第四个键
     * @param v4  第四个值
     * @param k5  第五个键
     * @param v5  第五个值
     * @param k6  第六个键
     * @param v6  第六个值
     * @param k7  第七个键
     * @param v7  第七个值
     * @param k8  第八个键
     * @param v8  第八个值
     * @param <K> Map 键的类型
     * @param <V> Map 值的类型
     * @return 包含指定键值对的不可修改的 Map
     * @throws NullPointerException     如果任何键或值为 {@code null}
     * @throws IllegalArgumentException 如果有重复的键
     */
    public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        requireNonNull(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
        java.util.Map<K, V> map = new HashMap<>(8);
        putAndCheckDuplicate(map, k1, v1);
        putAndCheckDuplicate(map, k2, v2);
        putAndCheckDuplicate(map, k3, v3);
        putAndCheckDuplicate(map, k4, v4);
        putAndCheckDuplicate(map, k5, v5);
        putAndCheckDuplicate(map, k6, v6);
        putAndCheckDuplicate(map, k7, v7);
        putAndCheckDuplicate(map, k8, v8);
        return Collections.unmodifiableMap(map);
    }

    /**
     * 返回包含九个键值对的不可修改的 Map。
     *
     * @param k1  第一个键
     * @param v1  第一个值
     * @param k2  第二个键
     * @param v2  第二个值
     * @param k3  第三个键
     * @param v3  第三个值
     * @param k4  第四个键
     * @param v4  第四个值
     * @param k5  第五个键
     * @param v5  第五个值
     * @param k6  第六个键
     * @param v6  第六个值
     * @param k7  第七个键
     * @param v7  第七个值
     * @param k8  第八个键
     * @param v8  第八个值
     * @param k9  第九个键
     * @param v9  第九个值
     * @param <K> Map 键的类型
     * @param <V> Map 值的类型
     * @return 包含指定键值对的不可修改的 Map
     * @throws NullPointerException     如果任何键或值为 {@code null}
     * @throws IllegalArgumentException 如果有重复的键
     */
    public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        requireNonNull(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
        java.util.Map<K, V> map = new HashMap<>(9);
        putAndCheckDuplicate(map, k1, v1);
        putAndCheckDuplicate(map, k2, v2);
        putAndCheckDuplicate(map, k3, v3);
        putAndCheckDuplicate(map, k4, v4);
        putAndCheckDuplicate(map, k5, v5);
        putAndCheckDuplicate(map, k6, v6);
        putAndCheckDuplicate(map, k7, v7);
        putAndCheckDuplicate(map, k8, v8);
        putAndCheckDuplicate(map, k9, v9);
        return Collections.unmodifiableMap(map);
    }

    /**
     * 返回包含十个键值对的不可修改的 Map。
     *
     * @param k1  第一个键
     * @param v1  第一个值
     * @param k2  第二个键
     * @param v2  第二个值
     * @param k3  第三个键
     * @param v3  第三个值
     * @param k4  第四个键
     * @param v4  第四个值
     * @param k5  第五个键
     * @param v5  第五个值
     * @param k6  第六个键
     * @param v6  第六个值
     * @param k7  第七个键
     * @param v7  第七个值
     * @param k8  第八个键
     * @param v8  第八个值
     * @param k9  第九个键
     * @param v9  第九个值
     * @param k10 第十个键
     * @param v10 第十个值
     * @param <K> Map 键的类型
     * @param <V> Map 值的类型
     * @return 包含指定键值对的不可修改的 Map
     * @throws NullPointerException     如果任何键或值为 {@code null}
     * @throws IllegalArgumentException 如果有重复的键
     */
    public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        requireNonNull(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
        java.util.Map<K, V> map = new HashMap<>(10);
        putAndCheckDuplicate(map, k1, v1);
        putAndCheckDuplicate(map, k2, v2);
        putAndCheckDuplicate(map, k3, v3);
        putAndCheckDuplicate(map, k4, v4);
        putAndCheckDuplicate(map, k5, v5);
        putAndCheckDuplicate(map, k6, v6);
        putAndCheckDuplicate(map, k7, v7);
        putAndCheckDuplicate(map, k8, v8);
        putAndCheckDuplicate(map, k9, v9);
        putAndCheckDuplicate(map, k10, v10);
        return Collections.unmodifiableMap(map);
    }

    // 辅助方法: 检查键值是否为 null
    private static void requireNonNull(Object... elements) {
        for (Object obj : elements) Objects.requireNonNull(obj, "键或值不能为null");
    }

    // 辅助方法: 向 map 中添加键值对并检查重复键
    private static <K, V> void putAndCheckDuplicate(java.util.Map<K, V> map, K key, V value) {
        V oldValue = map.put(key, value);
        if (oldValue != null) throw new IllegalArgumentException("重复的键: " + key);
    }
}
