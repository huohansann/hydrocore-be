package com.siact.module.system.processor;

import com.siact.module.system.entity.SysConfigEntity;
import com.siact.module.system.enums.SysConfigModuleEnum;
import com.siact.module.system.enums.SysConfigTypeEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 配置扁平化处理器
 * 将 JSON 对象转换为多行 SysConfigEntity
 *
 * @author siact
 */
@Component
public class ConfigFlattener {

    /**
     * 将 JSON 对象扁平化为多行配置数据
     *
     * @param module      模块
     * @param scCode      配置编码
     * @param scName      配置名称
     * @param description 配置说明
     * @param json        JSON 对象
     * @return 扁平化后的实体列表
     */
    public List<SysConfigEntity> flatten(SysConfigModuleEnum module, String scCode,
                                          String scName, String description, Object json) {
        List<SysConfigEntity> result = new ArrayList<>();
        doFlatten(module, scCode, scName, description, "", json, result);
        return result;
    }

    private void doFlatten(SysConfigModuleEnum module, String scCode, String scName,
                           String description, String path, Object value, List<SysConfigEntity> result) {
        if (value == null) {
            // null 值存储为空字符串
            SysConfigEntity entity = createEntity(module, scCode, path, scName, SysConfigTypeEnum.STRING, "", description);
            result.add(entity);
            return;
        }

        // 处理 Map（JSON 对象，来自 Jackson 反序列化）
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            if (map.isEmpty()) {
                // 空对象作为叶子节点存储
                SysConfigEntity entity = createEntity(module, scCode, path, scName, SysConfigTypeEnum.OBJECT, "{}", description);
                result.add(entity);
                return;
            }
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String childPath = path.isEmpty() ? key : path + "." + key;
                doFlatten(module, scCode, scName, description, childPath, entry.getValue(), result);
            }
        }
        // 处理 List（JSON 数组，来自 Jackson 反序列化）
        else if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                // 空数组作为叶子节点存储
                SysConfigEntity entity = createEntity(module, scCode, path, scName, SysConfigTypeEnum.ARRAY, "[]", description);
                result.add(entity);
                return;
            }
            for (int i = 0; i < list.size(); i++) {
                String childPath = path.isEmpty() ? "[" + i + "]" : path + ".[" + i + "]";
                doFlatten(module, scCode, scName, description, childPath, list.get(i), result);
            }
        }
        // 叶子节点：直接存储
        else {
            SysConfigTypeEnum type = determineType(value);
            SysConfigEntity entity = createEntity(module, scCode, path, scName, type, String.valueOf(value), description);
            result.add(entity);
        }
    }

    private SysConfigTypeEnum determineType(Object value) {
        if (value instanceof String) {
            return SysConfigTypeEnum.STRING;
        } else if (value instanceof Integer) {
            return SysConfigTypeEnum.INTEGER;
        } else if (value instanceof Float) {
            return SysConfigTypeEnum.FLOAT;
        } else if (value instanceof Double) {
            return SysConfigTypeEnum.DOUBLE;
        } else if (value instanceof BigDecimal) {
            return SysConfigTypeEnum.DECIMAL;
        } else if (value instanceof Boolean) {
            return SysConfigTypeEnum.BOOLEAN;
        } else {
            return SysConfigTypeEnum.STRING;
        }
    }

    private SysConfigEntity createEntity(SysConfigModuleEnum module, String scCode, String scPath,
                                          String scName, SysConfigTypeEnum scType, String scValue, String description) {
        SysConfigEntity entity = new SysConfigEntity();
        entity.setModule(module);
        entity.setScCode(scCode);
        entity.setScPath(scPath);
        entity.setScName(scName);
        entity.setScType(scType);
        entity.setScValue(scValue);
        entity.setDescription(description);
        entity.setVersion(1);
        return entity;
    }
}