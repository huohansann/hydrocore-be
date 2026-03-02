package com.siact.module.base.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-27 13:38
 * @className : AppConfigJsonNode
 * @description : 系统配置 JSON 对象转换器
 * <br>
 * <p>
 * 轻量级的 JSON 读取/包装工具, 用于读取存储在数据库中的 JSON 配置字符串并以「路径+类型安全」的方式访问.
 * 主要能力:
 * <ul>
 *   <li>支持点号分隔的路径访问(例如 "a.b.c")</li>
 *   <li>支持数组下标访问(例如 "items[0].name", "a.b[2].c[1].d")</li>
 *   <li>支持缓存解析后的路径 token(PathToken)以降低频繁 split/regex 的开销</li>
 *   <li>提供常见的类型安全读取方法(getString/getInteger/getLong/getBoolean/getBigDecimal/getBigInteger/getList/getObject/getNodeMap 等)</li>
 *   <li>提供常见的类型安全转换方法(asText/asInteger/asLong/asBoolean/asBigDecimal/asBigInteger/asList/asbject/asNMap 等)</li>
 *   <li>可以把任意 JsonNode 转成目标类型(toObject)</li>
 * </ul>
 * <p>
 * 线程安全性:
 * <ul>
 *   <li>本类为不可变包装器(内部持有一个 final JsonNode), 对该 JsonNode 的读取操作是线程安全的</li>
 *   <li>PATH_CACHE 使用 ConcurrentHashMap, 适合多线程环境下缓存路径解析结果</li>
 * </ul>
 *
 * <p>使用示例:
 * <pre>
 * {@code
 * AppConfigJsonNode cfg = AppConfigJsonNode.parse(jsonString);
 * String name = cfg.getString("items[0].name");
 * List<MyDto> list = cfg.getList("items", MyDto.class);
 * Map<String, AppConfigJsonNode> nodeMap = cfg.getNodeMap("feature.buttons");
 * boolean exists = cfg.contains("feature.buttons.save");
 * }
 * </pre>
 *
 * <p>注意:
 * <ul>
 *   <li>parse 方法在解析失败时会抛出 RuntimeException(可根据需要 catch/转换)</li>
 *   <li>getXXX 系列方法在节点不存在或类型不匹配时返回默认值(方法签名中有 defaultValue 的会返回该默认值, 否则返回类型的常见默认, 比如 getInteger -> 0)</li>
 * </ul>
 */
@Slf4j
public class AppConfigJsonNode {
    /**
     * Jackson ObjectMapper 单例(线程安全可重用)
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();
    /**
     * 匹配数组形式的 token, 例如 "items[0]", 第 1 组是字段名, 第 2 组是索引
     */
    private static final Pattern ARRAY_PATTERN = Pattern.compile("([a-zA-Z0-9_]+)\\[(\\d+)]");
    /**
     * 缓存已解析的路径 token 列表: path -> List<PathToken>
     * 使用 ConcurrentHashMap 以支持并发访问.
     */
    private static final Map<String, List<PathToken>> PATH_CACHE = new ConcurrentHashMap<>();
    /**
     * 包装的 JsonNode 根节点(可为 null, 表示空或不存在)
     */
    private final JsonNode node;

    /**
     * 私有构造器: 用 JsonNode 创建包装器实例
     *
     * @param node 要包装的 JsonNode(可以为 null)
     */
    private AppConfigJsonNode(JsonNode node) {
        this.node = node;
    }

    /**
     * 用已存在的 JsonNode 创建包装器(不会做额外拷贝)
     *
     * @param node JsonNode(可能是对象/数组/值或 null)
     * @return 包装器实例
     */
    public static AppConfigJsonNode of(JsonNode node) {
        return new AppConfigJsonNode(node);
    }

    /**
     * 解析 JSON 字符串并创建包装器
     *
     * @param json JSON 字符串
     * @return 包装器实例
     * @throws RuntimeException 当 JSON 解析失败时抛出
     */
    public static AppConfigJsonNode parse(String json) {
        try {
            return new AppConfigJsonNode(MAPPER.readTree(json));
        } catch (Exception e) {
            throw new RuntimeException("JSON string parsing failed", e);
        }
    }

    /**
     * 根据路径获取目标 JsonNode(内部方法, 返回原生 JsonNode)
     * 支持路径语法:
     * <ul>
     *   <li>点分字段: a.b.c</li>
     *   <li>数组索引: items[0]</li>
     *   <li>混合: a.b[2].c[1].d</li>
     * </ul>
     *
     * <p>如果路径为空或空白, 返回 null(调用方通常使用 getNode 来得到包装器实例)
     *
     * @param path 路径字符串
     * @return 找到的 JsonNode(未找到或路径非法时返回 null)
     */
    private JsonNode get(String path) {
        if (StringUtils.isBlank(path)) return null;

        List<PathToken> tokens = parsePath(path);
        JsonNode current = this.node;

        for (PathToken token : tokens) {
            if (!isNotEmpty(current)) {
                log.warn("[{}] The JsonNode obtained is null.", path);
                return null;
            }
            current = current.get(token.field);

            if (ObjectUtils.isNotEmpty(token.index)) {
                current = (ObjectUtils.isNotEmpty(current) && current.isArray() && token.index < current.size()) ? current.get(token.index) : null;
            }
        }
        return current;
    }


    /**
     * 获取某个路径对应的包装节点(返回 {@link AppConfigJsonNode}, 以支持链式调用)。
     *
     * @param fields 路径(a.b[0].c 形式)
     * @return 对应的包装节点(如果路径为空则返回 this; 如果找不到则返回包装了 null 的实例)
     */
    public AppConfigJsonNode getNode(String fields) {
        return StringUtils.isBlank(fields) ? this : new AppConfigJsonNode(get(fields));
    }

    /**
     * 直接从当前节点(期望为数组)取指定下标位置并返回包装器
     *
     * @param index 下标
     * @return 包装器(下标越界或当前节点不是数组则返回 null)
     */
    public AppConfigJsonNode getNode(int index) {
        return (isNotEmpty(node) && node.isArray() && index < node.size()) ? new AppConfigJsonNode(node.get(index)) : null;
    }

    /**
     * 获取字符串值(若不存在返回 null)
     *
     * @param fields 路径
     * @return 字符串值或 null
     */
    public String getString(String fields) {
        return getString(fields, null);
    }

    /**
     * 获取字符串值(若不存在返回提供的默认值)
     *
     * @param fields       路径
     * @param defaultValue 默认值
     * @return 字符串值或 defaultValue
     */
    public String getString(String fields, String defaultValue) {
        JsonNode node = get(fields);
        return isNotEmpty(node) ? node.asText() : defaultValue;
    }

    /**
     * 获取布尔值(不存在返回 false)
     *
     * @param fields 路径
     * @return 布尔值(默认 false)
     */
    public Boolean getBoolean(String fields) {
        return getBoolean(fields, false);
    }


    /**
     * 获取布尔值(不存在返回默认值)
     *
     * @param fields       路径
     * @param defaultValue 默认布尔值
     * @return 布尔值或 defaultValue
     */
    public Boolean getBoolean(String fields, boolean defaultValue) {
        JsonNode node = get(fields);
        return isNotEmpty(node) && node.isBoolean() ? node.asBoolean() : defaultValue;
    }

    /**
     * 获取 Integer(不存在返回 0)
     *
     * @param fields 路径
     * @return Integer 值或 0
     */
    public Integer getInteger(String fields) {
        return getInteger(fields, 0);
    }

    /**
     * 获取 Integer(不存在返回 defaultValue)
     *
     * <p>注意: 当 JSON 节点为任意数字类型(int/long/decimal)时都会以 asInt() 返回(若可能有精度问题请使用 BigDecimal)
     *
     * @param fields       路径
     * @param defaultValue 默认 Integer
     * @return Integer 值或 defaultValue
     */
    public Integer getInteger(String fields, Integer defaultValue) {
        JsonNode node = get(fields);
        return isNotEmpty(node) && node.isNumber() ? node.asInt() : defaultValue;
    }


    /**
     * 获取 Long(不存在返回 0L)
     *
     * @param fields 路径
     * @return Long 值或 0L
     */
    public Long getLong(String fields) {
        return getLong(fields, 0L);
    }

    /**
     * 获取 Long(不存在返回 defaultValue)
     *
     * @param fields       路径
     * @param defaultValue 默认 Long
     * @return Long 值或 defaultValue
     */
    public Long getLong(String fields, Long defaultValue) {
        JsonNode node = get(fields);
        return isNotEmpty(node) && node.isNumber() ? node.asLong() : defaultValue;
    }

    /**
     * 获取 Double(不存在返回 0.0)
     *
     * @param fields 路径
     * @return Double 值或 0.0
     */
    public Double getDouble(String fields) {
        return getDouble(fields, 0.0D);
    }

    /**
     * 获取 Double(不存在返回 defaultValue)
     *
     * @param fields       路径
     * @param defaultValue 默认 Double
     * @return Double 值或 defaultValue
     */
    public Double getDouble(String fields, Double defaultValue) {
        JsonNode node = get(fields);
        return isNotEmpty(node) && node.isNumber() ? node.asDouble() : defaultValue;
    }

    /**
     * 获取 BigInteger(不存在返回 BigInteger.ZERO)
     *
     * @param fields 路径
     * @return BigInteger 值或 BigInteger.ZERO
     */
    public BigInteger getBigInteger(String fields) {
        return getBigInteger(fields, BigInteger.ZERO);
    }

    /**
     * 获取 BigInteger(不存在返回 defaultValue)
     *
     * @param fields       路径
     * @param defaultValue 默认 BigInteger
     * @return BigInteger 值或 defaultValue
     */
    public BigInteger getBigInteger(String fields, BigInteger defaultValue) {
        JsonNode node = get(fields);
        return isNotEmpty(node) && node.isNumber() ? node.bigIntegerValue() : defaultValue;
    }

    /**
     * 获取 BigDecimal(不存在返回 BigDecimal.ZERO)
     *
     * @param fields 路径
     * @return BigDecimal 值或 BigDecimal.ZERO
     */
    public BigDecimal getBigDecimal(String fields) {
        return getBigDecimal(fields, BigDecimal.ZERO);
    }

    /**
     * 获取 BigDecimal(不存在返回 defaultValue)
     *
     * @param fields       路径
     * @param defaultValue 默认 BigDecimal
     * @return BigDecimal 值或 defaultValue
     */
    public BigDecimal getBigDecimal(String fields, BigDecimal defaultValue) {
        JsonNode node = get(fields);
        return isNotEmpty(node) && node.isNumber() ? node.decimalValue() : defaultValue;
    }

    /**
     * 将路径处的数组元素转换为目标类型的 List
     * <p>示例: getList("items", MyDto.class)
     *
     * @param <T>    列表元素类型
     * @param fields 路径
     * @param clazz  目标类型 Class
     * @return 不为 null 的 List(如果节点不存在或不是数组, 返回空列表)
     */
    public <T> List<T> getList(String fields, Class<T> clazz) {
        JsonNode node = get(fields);
        if (isNotEmpty(node) && node.isArray()) {
            List<T> nodes = new ArrayList<>();
            node.forEach(n -> nodes.add(toObject(n, clazz)));
            return nodes;
        }
        return Collections.emptyList();
    }

    /**
     * 返回路径对应对象节点的 Map(值为 {@link  AppConfigJsonNode}, 方便链式读取)
     * <p>示例: Map&lt;String, AppConfigJsonNode&gt; btn = getNodeMap("feature.buttons");
     *
     * @param fields 路径
     * @return 有序 Map(LinkedHashMap), 若节点不存在或不是对象则返回空 map
     */
    public Map<String, AppConfigJsonNode> getNodeMap(String fields) {
        JsonNode node = get(fields);
        if (isNotEmpty(node) && node.isObject()) {
            Map<String, AppConfigJsonNode> map = new LinkedHashMap<>();
            node.fields().forEachRemaining(entry -> map.put(entry.getKey(), new AppConfigJsonNode(entry.getValue())));
            return map;
        }
        return Collections.emptyMap();
    }

    /**
     * 将路径处的节点转换为目标类型对象(若节点不存在返回 null)
     * <p>示例: MyDto dto = getObject("feature", MyDto.class);
     *
     * @param <T>    目标类型
     * @param fields 路径
     * @param clazz  目标类型
     * @return 转换后的对象或 null
     */
    public <T> T getObject(String fields, Class<T> clazz) {
        JsonNode node = get(fields);
        return isNotEmpty(node) ? toObject(node, clazz) : null;
    }

    /**
     * 将当前包装节点转换为字符串(不返回默认值, 节点不存在则返回 null)
     * <p>等价于 {@code node == null ? null : node.asText()}</p>
     *
     * @return 文本内容或 null
     */
    public String asText() {
        return isNotEmpty(node) ? node.asText() : null;
    }

    /**
     * 将当前包装节点转换为 Boolean
     * <p>
     * 行为:
     * <ul>
     *   <li>若节点为 boolean 类型, 直接返回其值</li>
     *   <li>若节点为文本, 则使用 {@link Boolean#parseBoolean(String)} 解析("true"/"false" 不区分大小写)</li>
     *   <li>否则返回 null(表示不存在或无法转换)</li>
     * </ul>
     *
     * @return Boolean 值或 null
     */
    public Boolean asBoolean() {
        if (isNotEmpty(node)) {
            if (node.isBoolean()) return node.asBoolean();
            if (node.isTextual()) return Boolean.parseBoolean(node.asText());
        }
        return null;
    }

    /**
     * 将当前包装节点转换为 Integer(整型)
     * <p>
     * 行为:
     * <ul>
     *   <li>若节点为数字类型, 返回其 int 值(可能存在截断/精度损失, 基于 Jackson 的 asInt)</li>
     *   <li>若节点为文本且内容为合法整数字符串, 则尝试 {@link Integer#parseInt(String)}</li>
     *   <li>否则返回 null</li>
     * </ul>
     *
     * @return Integer 值或 null
     */
    public Integer asInteger() {
        if (!isNotEmpty(node)) return null;
        if (node.isNumber()) return node.asInt();
        if (node.isTextual()) {
            try {
                return Integer.parseInt(node.asText().trim());
            } catch (NumberFormatException e) {
                log.warn("JSON json data conversion failed, int type number cannot be formatted: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 将当前包装节点转换为 Long(长整型)
     * <p>规则同 {@link #asInteger()}, 但返回 Long</p>
     *
     * @return Long 值或 null
     */
    public Long asLong() {
        if (!isNotEmpty(node)) return null;
        if (node.isNumber()) return node.asLong();
        if (node.isTextual()) {
            try {
                return Long.parseLong(node.asText().trim());
            } catch (NumberFormatException e) {
                log.warn("JSON json data conversion failed, long type number cannot be formatted: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 将当前包装节点转换为 Double(双精度浮点)
     * <p>规则同 {@link #asInteger()}, 但使用 {@link Double#parseDouble(String)}</p>
     *
     * @return Double 值或 null
     */
    public Double asDouble() {
        if (!isNotEmpty(node)) return null;
        if (node.isNumber()) return node.asDouble();
        if (node.isTextual()) {
            try {
                return Double.parseDouble(node.asText().trim());
            } catch (NumberFormatException e) {
                log.warn("JSON json data conversion failed, double type number cannot be formatted: {}", e.getMessage());
            }
        }
        return null;
    }


    /**
     * 将当前包装节点转换为 {@link BigInteger}。
     * <p>
     * 行为:
     * <ul>
     *   <li>若为数字类型, 使用 {@link JsonNode#bigIntegerValue()}</li>
     *   <li>若为文本且是合法整数表示, 尝试 new {@link BigInteger}(text)</li>
     *   <li>失败或节点为空时返回 null</li>
     * </ul>
     *
     * @return BigInteger 或 null
     */
    public BigInteger asBigInteger() {
        if (!isNotEmpty(node)) return null;
        if (node.isNumber()) return node.bigIntegerValue();
        if (node.isTextual()) {
            try {
                return new BigInteger(node.asText().trim());
            } catch (NumberFormatException e) {
                log.warn("JSON json data conversion failed, big integer type number cannot be formatted: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 将当前包装节点转换为 {@link BigDecimal}
     * <p>
     * 行为:
     * <ul>
     *   <li>若为数字类型, 使用 {@link JsonNode#decimalValue()}</li>
     *   <li>若为文本且是合法十进制表示, 尝试 new {@link BigDecimal}(text)</li>
     *   <li>失败或节点为空时返回 null</li>
     * </ul>
     *
     * @return BigDecimal 或 null
     */
    public BigDecimal asBigDecimal() {
        if (!isNotEmpty(node)) return null;
        if (node.isNumber()) return node.decimalValue();
        if (node.isTextual()) {
            try {
                return new BigDecimal(node.asText().trim());
            } catch (NumberFormatException e) {
                log.warn("JSON json data conversion failed, big decimal type number cannot be formatted: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 将当前包装节点(期望为 JSON 数组)转换为目标元素类型的 List
     * <p>
     * 行为:
     * <ul>
     *   <li>若节点不存在或不是数组, 则返回空列表(与 getList 行为一致, 避免返回 null 导致 NPE)</li>
     *   <li>对数组中每个元素, 使用类内私有方法 {@code toObject} 将元素转换为目标类型(若单元素转换失败会抛出 {@link ClassCastException})</li>
     * </ul>
     *
     * @param <T>   列表元素类型
     * @param clazz 元素的 Class
     * @return 不为 null 的 List(节点不存在或不是数组时返回 {@link Collections#emptyList()})
     * @throws ClassCastException 当数组中某个元素无法转换为目标类型时抛出
     */
    public <T> List<T> asList(Class<T> clazz) {
        if (!isNotEmpty(node) || !node.isArray()) return Collections.emptyList();
        List<T> list = new ArrayList<>(node.size());
        for (JsonNode element : node) {
            list.add(toObject(element, clazz));
        }
        return list;
    }

    /**
     * 将当前包装节点(期望为 JSON 对象)转换为键为 String、值为指定类型的 Map
     * <p>
     * 行为:
     * <ul>
     *   <li>若节点不存在或不是对象, 则返回空映射（{@link Collections#emptyMap()})</li>
     *   <li>保留原始字段顺序(使用 {@link LinkedHashMap})</li>
     *   <li>对每个字段值使用类内私有方法 {@code toObject} 转换为目标类型(若某个字段值转换失败将抛出 {@link ClassCastException})</li>
     * </ul>
     *
     * @param <V>   Map 的值类型
     * @param clazz 值类型的 Class
     * @return 不为 null 的 Map(节点不存在或不是对象时返回 {@link Collections#emptyMap()})
     * @throws ClassCastException 当某个字段的值无法转换为目标类型时抛出
     */
    public <V> Map<String, V> asMap(Class<V> clazz) {
        if (!isNotEmpty(node) || !node.isObject()) return Collections.emptyMap();
        Map<String, V> map = new LinkedHashMap<>();
        node.fields().forEachRemaining(entry -> map.put(entry.getKey(), toObject(entry.getValue(), clazz)));
        return map;
    }

    /**
     * 将当前包装节点转换为目标类型对象(等同于对 node 调用 {@link ObjectMapper#convertValue(Object, Class)})
     * <p>
     * 行为:
     * <ul>
     *   <li>若包装节点为 null 或 JSON null, 则返回 null</li>
     *   <li>若转换失败, 则抛出 {@link ClassCastException}, 异常消息包含底层异常信息(与类内私有方法 {@code toObject} 风格一致)</li>
     * </ul>
     *
     * @param <T>   目标类型
     * @param clazz 目标类型 Class
     * @return 转换后的对象, 或 null(当节点为空时)
     * @throws ClassCastException 当无法把 JSON 转为目标类型时抛出(消息中包含底层异常信息)
     */
    public <T> T asObject(Class<T> clazz) {
        return isNotEmpty(this.node) ? toObject(this.node, clazz) : null;
    }

    /**
     * 判断某个路径是否存在(节点非 null 且非 JSON null)
     *
     * @param fields 路径
     * @return 存在则 true, 否则 false
     */
    public boolean contains(String fields) {
        return isNotEmpty(get(fields));
    }

    /**
     * 将 JsonNode 转换为目标类型对象(内部方法, 含异常包装)
     *
     * @param <T>   目标类型
     * @param node  要转换的 JsonNode(非 null)
     * @param clazz 目标类型
     * @return 转换结果
     * @throws ClassCastException 转换失败时抛出(消息中包含底层异常信息)
     */
    private <T> T toObject(JsonNode node, Class<T> clazz) {
        try {
            return MAPPER.convertValue(node, clazz);
        } catch (Exception e) {
            throw new ClassCastException(String.format("Type conversion failed, json string does not match the target type: %s", e.getMessage()));
        }
    }

    /**
     * 判断 JsonNode 是否非空(既不是 null 引用, 也不是 Json 的 null 节点)
     *
     * @param node JsonNode(可以为 null)
     * @return 非空返回 true
     */
    private @Contract("null -> false") boolean isNotEmpty(JsonNode node) {
        return !ObjectUtils.isEmpty(node) && !node.isNull();
    }

    /**
     * PathToken: 代表路径中的一个 token(字段名 + 可选数组下标)
     *
     * <p>例如:
     * <ul>
     *   <li>"items" -> field = "items", index = null</li>
     *   <li>"items[0]" -> field = "items", index = 0</li>
     * </ul>
     */
    private static class PathToken {
        String field;
        Integer index; // 如果为 null 表示不是数组

        PathToken(String field, Integer index) {
            this.field = field;
            this.index = index;
        }
    }

    /**
     * 解析路径字符串为 PathToken 列表, 并把结果缓存到 PATH_CACHE
     * <p>解析行为:
     * <ul>
     *   <li>按 '.' 切分</li>
     *   <li>对每个片段用正则匹配是否为 field[index] 形式</li>
     *   <li>缓存 key 为原始 path 字符串</li>
     * </ul>
     *
     * @param path 路径字符串
     * @return PathToken 列表(不为 null)
     */
    private static List<PathToken> parsePath(String path) {
        return PATH_CACHE.computeIfAbsent(path, p -> {
            List<PathToken> tokens = new ArrayList<>();
            String[] parts = p.split("\\.");

            for (String part : parts) {
                Matcher matcher = ARRAY_PATTERN.matcher(part);
                if (matcher.matches()) tokens.add(new PathToken(matcher.group(1), Integer.parseInt(matcher.group(2))));
                else tokens.add(new PathToken(part, null));
            }
            return tokens;
        });
    }
}
