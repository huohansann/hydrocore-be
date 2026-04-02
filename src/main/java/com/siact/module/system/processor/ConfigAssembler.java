package com.siact.module.system.processor;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.siact.module.system.entity.SysConfigEntity;
import com.siact.module.system.enums.SysConfigTypeEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;

/**
 * 配置组装处理器
 * 将多行 SysConfigEntity 组装为 JSON 对象或数组
 *
 * @author siact
 */
@Component
public class ConfigAssembler {

    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 将多行配置数据组装为 JSON 对象或数组
     *
     * @param entities 配置实体列表
     * @return 组装后的 JSON 对象或数组
     */
    public Object assemble(List<SysConfigEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new JSONObject();
        }

        // 按路径排序，保证数组顺序正确
        entities.sort(Comparator.comparing(SysConfigEntity::getScPath));

        // 判断根是对象还是数组：检查第一个路径是否以 "[" 开头
        String firstPath = entities.get(0).getScPath();
        boolean isRootArray = firstPath.startsWith("[");

        Object result;
        if (isRootArray) {
            result = assembleArray(entities);
        } else {
            result = assembleObject(entities);
        }

        // 清除 Fastjson 序列化时添加的 @type 字段
        return stripTypeInfo(result);
    }

    /**
     * 递归清除 @type 字段，返回干净的 JSON 对象
     */
    private Object stripTypeInfo(Object obj) {
        if (obj instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) obj;
            jsonObj.remove("@type");
            for (String key : jsonObj.keySet()) {
                Object value = jsonObj.get(key);
                if (value instanceof JSONObject || value instanceof JSONArray) {
                    jsonObj.put(key, stripTypeInfo(value));
                }
            }
            return jsonObj;
        } else if (obj instanceof JSONArray) {
            JSONArray jsonArr = (JSONArray) obj;
            for (int i = 0; i < jsonArr.size(); i++) {
                Object element = jsonArr.get(i);
                if (element instanceof JSONObject || element instanceof JSONArray) {
                    jsonArr.set(i, stripTypeInfo(element));
                }
            }
            return jsonArr;
        }
        return obj;
    }

    /**
     * 组装为 JSON 对象
     */
    private JSONObject assembleObject(List<SysConfigEntity> entities) {
        JSONObject result = new JSONObject();
        for (SysConfigEntity entity : entities) {
            String path = entity.getScPath();
            Object value = parseValue(entity.getScType(), entity.getScValue());
            setPathValue(result, path, value);
        }
        return result;
    }

    /**
     * 组装为 JSON 数组
     */
    private JSONArray assembleArray(List<SysConfigEntity> entities) {
        JSONArray result = new JSONArray();
        for (SysConfigEntity entity : entities) {
            String path = entity.getScPath();
            Object value = parseValue(entity.getScType(), entity.getScValue());
            setArrayPathValue(result, path, value);
        }
        return result;
    }

    private Object parseValue(SysConfigTypeEnum type, String value) {
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
     */
    private void setPathValue(JSONObject obj, String path, Object value) {
        if (path == null || path.isEmpty()) {
            return;
        }

        String[] parts = path.split("\\.");
        JSONObject current = obj;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (isArrayIndex(part)) {
                int index = parseArrayIndex(part);
                String arrayKey = parts[i - 1]; // 前一个部分是数组名
                JSONArray arr = getOrCreateArray(current, arrayKey);
                current = ensureArrayElement(arr, index);
            } else {
                current = getOrCreateObject(current, part);
            }
        }

        // 设置最终值
        String lastPart = parts[parts.length - 1];
        if (isArrayIndex(lastPart)) {
            int index = parseArrayIndex(lastPart);
            String arrayKey = parts.length > 1 ? parts[parts.length - 2] : null;
            if (arrayKey != null && isArrayIndex(arrayKey)) {
                // 嵌套数组情况
                JSONArray arr = current.getJSONArray("items");
                if (arr == null) {
                    arr = new JSONArray();
                    current.put("items", arr);
                }
                ensureArraySize(arr, index);
                arr.set(index, value);
            } else if (arrayKey != null) {
                JSONArray arr = getOrCreateArray(current, arrayKey);
                ensureArraySize(arr, index);
                arr.set(index, value);
            }
        } else {
            current.put(lastPart, value);
        }
    }

    /**
     * 设置数组路径值（根级数组情况）
     */
    private void setArrayPathValue(JSONArray arr, String path, Object value) {
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
        JSONObject obj;
        if (element instanceof JSONObject) {
            obj = (JSONObject) element;
        } else {
            obj = new JSONObject();
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

    private JSONArray getOrCreateArray(JSONObject obj, String key) {
        JSONArray arr = obj.getJSONArray(key);
        if (arr == null) {
            arr = new JSONArray();
            obj.put(key, arr);
        }
        return arr;
    }

    private JSONObject getOrCreateObject(JSONObject obj, String key) {
        JSONObject child = obj.getJSONObject(key);
        if (child == null) {
            child = new JSONObject();
            obj.put(key, child);
        }
        return child;
    }

    private JSONObject ensureArrayElement(JSONArray arr, int index) {
        ensureArraySize(arr, index);
        Object element = arr.get(index);
        if (element instanceof JSONObject) {
            return (JSONObject) element;
        } else {
            JSONObject obj = new JSONObject();
            arr.set(index, obj);
            return obj;
        }
    }

    private void ensureArraySize(JSONArray arr, int index) {
        while (arr.size() <= index) {
            arr.add(null);
        }
    }
}