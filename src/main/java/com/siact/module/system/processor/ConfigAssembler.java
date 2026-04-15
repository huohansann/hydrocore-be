package com.siact.module.system.processor;

import com.siact.module.system.entity.SysConfigEntity;
import com.siact.module.system.enums.SysConfigTypeEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 配置组装处理器
 * 将多行 SysConfigEntity 组装为嵌套的 Map/List 结构
 *
 * @author siact
 */
@Component
public class ConfigAssembler {

    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 将多行配置数据组装为 Map 或 List 结构
     *
     * @param entities 配置实体列表
     * @return 组装后的 Map 或 List
     */
    public Object assemble(List<SysConfigEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new LinkedHashMap<>();
        }

        // 按路径排序，保证数组顺序正确
        entities.sort(Comparator.comparing(SysConfigEntity::getScPath));

        // 判断根是对象还是数组：检查第一个路径是否以 "[" 开头
        String firstPath = entities.get(0).getScPath();
        boolean isRootArray = firstPath.startsWith("[");

        if (isRootArray) {
            return assembleArray(entities);
        } else {
            return assembleObject(entities);
        }
    }

    /**
     * 组装为 Map 对象
     */
    private Map<String, Object> assembleObject(List<SysConfigEntity> entities) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (SysConfigEntity entity : entities) {
            String path = entity.getScPath();
            Object value = parseValue(entity.getScType(), entity.getScValue());
            setPathValue(result, path, value);
        }
        return result;
    }

    /**
     * 组装为 List 数组
     */
    private List<Object> assembleArray(List<SysConfigEntity> entities) {
        List<Object> result = new ArrayList<>();
        for (SysConfigEntity entity : entities) {
            String path = entity.getScPath();
            Object value = parseValue(entity.getScType(), entity.getScValue());
            setArrayPathValue(result, path, value);
        }
        return result;
    }

    private Object parseValue(SysConfigTypeEnum type, String value) {
        if (type == SysConfigTypeEnum.OBJECT) {
            return new LinkedHashMap<>();
        }
        if (type == SysConfigTypeEnum.ARRAY) {
            return new ArrayList<>();
        }
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            switch (type) {
                case STRING:
                    return value;
                case INTEGER:
                    return Integer.parseInt(value);
                case FLOAT:
                    return Float.parseFloat(value);
                case DOUBLE:
                    return Double.parseDouble(value);
                case DECIMAL:
                    return new BigDecimal(value);
                case BOOLEAN:
                    return Boolean.parseBoolean(value);
                case TIMESTAMP:
                    return TIMESTAMP_FORMAT.parse(value);
                default:
                    return value;
            }
        } catch (NumberFormatException | ParseException e) {
            return value;
        }
    }

    /**
     * 设置对象路径值
     * 使用 look-ahead 判断当前 part 是数组容器还是对象容器
     */
    private void setPathValue(Map<String, Object> obj, String path, Object value) {
        if (path == null || path.isEmpty()) {
            return;
        }

        String[] parts = path.split("\\.");
        Map<String, Object> current = obj;

        int i = 0;
        while (i < parts.length - 1) {
            String part = parts[i];
            String nextPart = (i + 1 < parts.length) ? parts[i + 1] : null;

            if (!isArrayIndex(part) && nextPart != null && isArrayIndex(nextPart)) {
                // part 是数组容器名，nextPart 是 [N]：创建数组并导航到元素
                List<Object> arr = getOrCreateArray(current, part);
                int index = parseArrayIndex(nextPart);
                current = ensureArrayElement(arr, index);
                i += 2; // 跳过 part 和 [N]
            } else if (!isArrayIndex(part)) {
                // 普通对象字段
                current = getOrCreateObject(current, part);
                i++;
            } else {
                // 不应到达这里：[N] 应该总是跟在数组容器名后面被一起处理
                i++;
            }
        }

        // 设置最终值
        String lastPart = parts[parts.length - 1];
        if (isArrayIndex(lastPart)) {
            // lastPart 是 [N]，说明 parts[-2] 是数组容器名
            String arrayKey = parts.length > 1 ? parts[parts.length - 2] : null;
            if (arrayKey != null && !isArrayIndex(arrayKey)) {
                List<Object> arr = getOrCreateArray(current, arrayKey);
                ensureArraySize(arr, parseArrayIndex(lastPart));
                arr.set(parseArrayIndex(lastPart), value);
            }
        } else {
            current.put(lastPart, value);
        }
    }

    /**
     * 设置数组路径值（根级数组情况）
     */
    private void setArrayPathValue(List<Object> arr, String path, Object value) {
        if (path == null || path.isEmpty()) {
            return;
        }

        // 路径格式: [0], [0].name, [0].items.[0].code 等
        String[] parts = path.split("\\.");

        // 第一部分一定是数组索引
        int rootIndex = parseArrayIndex(parts[0]);
        ensureArraySize(arr, rootIndex);

        if (parts.length == 1) {
            // 只有索引，直接设置值
            arr.set(rootIndex, value);
            return;
        }

        // 需要确保该位置有对象
        Object element = arr.get(rootIndex);
        Map<String, Object> obj;
        if (element instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> existing = (Map<String, Object>) element;
            obj = existing;
        } else {
            obj = new LinkedHashMap<>();
            arr.set(rootIndex, obj);
        }

        // 处理剩余路径
        StringBuilder remainingPath = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (remainingPath.length() > 0) {
                remainingPath.append(".");
            }
            remainingPath.append(parts[i]);
        }

        setPathValue(obj, remainingPath.toString(), value);
    }

    private boolean isArrayIndex(String part) {
        return part.startsWith("[") && part.endsWith("]");
    }

    private int parseArrayIndex(String part) {
        return Integer.parseInt(part.substring(1, part.length() - 1));
    }

    private List<Object> getOrCreateArray(Map<String, Object> obj, String key) {
        Object existing = obj.get(key);
        if (existing instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> arr = (List<Object>) existing;
            return arr;
        }
        List<Object> arr = new ArrayList<>();
        obj.put(key, arr);
        return arr;
    }

    private Map<String, Object> getOrCreateObject(Map<String, Object> obj, String key) {
        Object existing = obj.get(key);
        if (existing instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> child = (Map<String, Object>) existing;
            return child;
        }
        Map<String, Object> child = new LinkedHashMap<>();
        obj.put(key, child);
        return child;
    }

    private Map<String, Object> ensureArrayElement(List<Object> arr, int index) {
        ensureArraySize(arr, index);
        Object element = arr.get(index);
        if (element instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) element;
            return map;
        } else {
            Map<String, Object> obj = new LinkedHashMap<>();
            arr.set(index, obj);
            return obj;
        }
    }

    private void ensureArraySize(List<Object> arr, int index) {
        while (arr.size() <= index) {
            arr.add(null);
        }
    }
}
