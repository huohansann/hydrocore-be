# 选项式配置功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现统一的选项式配置管理系统，支持 JSON 对象扁平化存储与路径索引，提供完整的 CRUD API。

**Architecture:** 采用分层设计：Controller → Service → Processor(Flattener/Assembler) → Mapper。使用 Redis 缓存提高读取性能，Cache-Aside 策略保证一致性。

**Tech Stack:** Spring Boot 2.6.13, MyBatis Plus 3.4.3.1, Redis, Fastjson2 2.0.45

---

## 文件结构

```
src/main/java/com/siact/module/system/
├── controller/
│   └── SysConfigController.java          # REST API 控制器（10个接口）
├── service/
│   ├── SysConfigService.java             # 业务服务接口
│   └── impl/
│       └── SysConfigServiceImpl.java     # 业务服务实现（缓存+事务）
├── entity/
│   └── SysConfigEntity.java              # 数据实体（对应 sys_config 表）
├── mapper/
│   └── SysConfigMapper.java              # MyBatis Plus Mapper
├── enums/
│   ├── SysConfigModuleEnum.java          # 模块枚举
│   └── SysConfigTypeEnum.java            # 类型枚举
├── processor/
│   ├── ConfigFlattener.java              # JSON → 多行扁平化
│   └── ConfigAssembler.java              # 多行 → JSON 组装
├── dto/
│   ├── SysConfigDTO.java                 # 配置对象传输（组装后的 JSON）
│   └── SysConfigItemDTO.java             # 单项配置传输（单行数据）
└── command/
│   ├── SysConfigCreateCommand.java       # 创建命令（完整 JSON）
│   └── SysConfigUpdateCommand.java       # 更新命令（完整 JSON + version）
```

---

### Task 1: 创建数据库表

**Files:**
- Create: SQL 脚本（手动执行或记录）

- [ ] **Step 1: 执行建表 SQL**

在 MySQL 数据库中执行：

```sql
create table if not exists sys_config
(
    id          bigint              not null primary key comment '主键',
    module      varchar(255)        not null comment '模块: SYSTEM, CONTROL, FORECAST 等',
    sc_code     varchar(255)        not null comment '配置编码，全局唯一',
    sc_path     varchar(255)        not null comment '配置路径: 如 devices.[0].name',
    sc_name     varchar(500)        not null comment '配置名称',
    sc_type     varchar(50)         not null comment '配置类型: STRING, INTEGER, FLOAT, DOUBLE, DECIMAL, BOOLEAN, TIMESTAMP',
    sc_value    text                not null comment '配置值',
    description varchar(255)        not null comment '配置说明',
    version     int       default 1 not null comment '乐观锁版本号',
    create_time timestamp default current_timestamp,
    update_time timestamp default current_timestamp on update current_timestamp,
    unique key uk_sc_code_path (sc_code, sc_path)
) comment '系统选项配置表';
```

- [ ] **Step 2: 验证表创建成功**

执行 SQL 确认：
```sql
DESC sys_config;
```

---

### Task 2: 创建枚举类

**Files:**
- Create: `src/main/java/com/siact/module/system/enums/SysConfigModuleEnum.java`
- Create: `src/main/java/com/siact/module/system/enums/SysConfigTypeEnum.java`

- [ ] **Step 1: 创建模块枚举 SysConfigModuleEnum.java**

```java
package com.siact.module.system.enums;

/**
 * 系统配置模块枚举类
 *
 * @author siact
 */
public enum SysConfigModuleEnum {
    SYSTEM,    // 系统配置
    CONTROL,   // 控制模块配置
    FORECAST   // 预测模块配置
}
```

- [ ] **Step 2: 创建类型枚举 SysConfigTypeEnum.java**

```java
package com.siact.module.system.enums;

/**
 * 系统配置值类型枚举类
 *
 * @author siact
 */
public enum SysConfigTypeEnum {
    STRING,    // 字符串
    INTEGER,   // 整数
    FLOAT,     // 单精度浮点
    DOUBLE,    // 双精度浮点
    DECIMAL,   // 高精度数值
    BOOLEAN,   // 布尔值
    TIMESTAMP  // 时间戳
}
```

- [ ] **Step 3: 提交枚举类**

```bash
git add src/main/java/com/siact/module/system/enums/
git commit -m "feat(sys-config): 添加模块和类型枚举类"
```

---

### Task 3: 创建实体类

**Files:**
- Create: `src/main/java/com/siact/module/system/entity/SysConfigEntity.java`

- [ ] **Step 1: 创建实体 SysConfigEntity.java**

```java
package com.siact.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.siact.module.system.enums.SysConfigModuleEnum;
import com.siact.module.system.enums.SysConfigTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 系统选项配置实体
 *
 * @author siact
 */
@Data
@TableName("sys_config")
public class SysConfigEntity {

    @ApiModelProperty("主键")
    @NotNull(message = "[主键]不能为空")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("模块")
    @NotBlank(message = "[模块]不能为空")
    @Size(max = 255, message = "模块长度不能超过255")
    private SysConfigModuleEnum module;

    @ApiModelProperty("配置编码")
    @NotBlank(message = "[配置编码]不能为空")
    @Size(max = 255, message = "配置编码长度不能超过255")
    private String scCode;

    @ApiModelProperty("配置路径")
    @NotBlank(message = "[配置路径]不能为空")
    @Size(max = 255, message = "配置路径长度不能超过255")
    private String scPath;

    @ApiModelProperty("配置名称")
    @NotBlank(message = "[配置名称]不能为空")
    @Size(max = 500, message = "配置名称长度不能超过500")
    private String scName;

    @ApiModelProperty("配置类型")
    @NotBlank(message = "[配置类型]不能为空")
    private SysConfigTypeEnum scType;

    @ApiModelProperty("配置值")
    @NotBlank(message = "[配置值]不能为空")
    private String scValue;

    @ApiModelProperty("配置说明")
    @NotBlank(message = "[配置说明]不能为空")
    @Size(max = 255, message = "配置说明长度不能超过255")
    private String description;

    @ApiModelProperty("乐观锁版本号")
    @Version
    private Integer version;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;
}
```

- [ ] **Step 2: 提交实体类**

```bash
git add src/main/java/com/siact/module/system/entity/SysConfigEntity.java
git commit -m "feat(sys-config): 添加 SysConfigEntity 实体类"
```

---

### Task 4: 创建 Mapper

**Files:**
- Create: `src/main/java/com/siact/module/system/mapper/SysConfigMapper.java`

- [ ] **Step 1: 创建 Mapper SysConfigMapper.java**

```java
package com.siact.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.system.entity.SysConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统选项配置 Mapper
 *
 * @author siact
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfigEntity> {
}
```

- [ ] **Step 2: 提交 Mapper**

```bash
git add src/main/java/com/siact/module/system/mapper/SysConfigMapper.java
git commit -m "feat(sys-config): 添加 SysConfigMapper"
```

---

### Task 5: 创建扁平化处理器 ConfigFlattener

**Files:**
- Create: `src/main/java/com/siact/module/system/processor/ConfigFlattener.java`

- [ ] **Step 1: 创建扁平化处理器 ConfigFlattener.java**

```java
package com.siact.module.system.processor;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.siact.module.system.entity.SysConfigEntity;
import com.siact.module.system.enums.SysConfigModuleEnum;
import com.siact.module.system.enums.SysConfigTypeEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

        if (value instanceof JSONObject) {
            // 对象：递归处理每个属性
            JSONObject obj = (JSONObject) value;
            for (String key : obj.keySet()) {
                String childPath = path.isEmpty() ? key : path + "." + key;
                doFlatten(module, scCode, scName, description, childPath, obj.get(key), result);
            }
        } else if (value instanceof JSONArray) {
            // 数组：递归处理每个元素
            JSONArray arr = (JSONArray) value;
            for (int i = 0; i < arr.size(); i++) {
                String childPath = path + ".[" + i + "]";
                doFlatten(module, scCode, scName, description, childPath, arr.get(i), result);
            }
        } else {
            // 叶子节点：直接存储
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
```

- [ ] **Step 2: 提交扁平化处理器**

```bash
git add src/main/java/com/siact/module/system/processor/ConfigFlattener.java
git commit -m "feat(sys-config): 添加 ConfigFlattener 扁平化处理器"
```

---

### Task 6: 创建组装处理器 ConfigAssembler

**Files:**
- Create: `src/main/java/com/siact/module/system/processor/ConfigAssembler.java`

- [ ] **Step 1: 创建组装处理器 ConfigAssembler.java**

```java
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
import java.util.Date;
import java.util.List;

/**
 * 配置组装处理器
 * 将多行 SysConfigEntity 组装为 JSON 对象
 *
 * @author siact
 */
@Component
public class ConfigAssembler {

    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 将多行配置数据组装为 JSON 对象
     *
     * @param entities 配置实体列表
     * @return 组装后的 JSON 对象
     */
    public JSONObject assemble(List<SysConfigEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new JSONObject();
        }

        // 按路径排序，保证数组顺序正确
        entities.sort(Comparator.comparing(SysConfigEntity::getScPath));

        JSONObject result = new JSONObject();
        for (SysConfigEntity entity : entities) {
            String path = entity.getScPath();
            Object value = parseValue(entity.getScType(), entity.getScValue());
            setPathValue(result, path, value);
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

    private void setPathValue(JSONObject obj, String path, Object value) {
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
                // 嵌套数组情况，已在上面处理
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
```

- [ ] **Step 2: 提交组装处理器**

```bash
git add src/main/java/com/siact/module/system/processor/ConfigAssembler.java
git commit -m "feat(sys-config): 添加 ConfigAssembler 组装处理器"
```

---

### Task 7: 创建 DTO 和 Command 类

**Files:**
- Create: `src/main/java/com/siact/module/system/dto/SysConfigDTO.java`
- Create: `src/main/java/com/siact/module/system/dto/SysConfigItemDTO.java`
- Create: `src/main/java/com/siact/module/system/command/SysConfigCreateCommand.java`
- Create: `src/main/java/com/siact/module/system/command/SysConfigUpdateCommand.java`

- [ ] **Step 1: 创建 SysConfigDTO.java**

```java
package com.siact.module.system.dto;

import com.alibaba.fastjson2.JSONObject;
import com.siact.module.system.enums.SysConfigModuleEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 系统配置传输对象（组装后的完整配置）
 *
 * @author siact
 */
@Data
public class SysConfigDTO {

    @ApiModelProperty("配置编码")
    private String scCode;

    @ApiModelProperty("模块")
    private SysConfigModuleEnum module;

    @ApiModelProperty("配置名称")
    private String scName;

    @ApiModelProperty("配置说明")
    private String description;

    @ApiModelProperty("乐观锁版本号")
    private Integer version;

    @ApiModelProperty("配置数据（JSON 对象）")
    private JSONObject data;
}
```

- [ ] **Step 2: 创建 SysConfigItemDTO.java**

```java
package com.siact.module.system.dto;

import com.siact.module.system.enums.SysConfigTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 系统配置单项传输对象（单个路径的配置）
 *
 * @author siact
 */
@Data
public class SysConfigItemDTO {

    @ApiModelProperty("配置编码")
    private String scCode;

    @ApiModelProperty("配置路径")
    private String scPath;

    @ApiModelProperty("配置名称")
    private String scName;

    @ApiModelProperty("配置类型")
    private SysConfigTypeEnum scType;

    @ApiModelProperty("配置值")
    private String scValue;

    @ApiModelProperty("配置说明")
    private String description;

    @ApiModelProperty("乐观锁版本号")
    private Integer version;
}
```

- [ ] **Step 3: 创建 SysConfigCreateCommand.java**

```java
package com.siact.module.system.command;

import com.alibaba.fastjson2.JSONObject;
import com.siact.module.system.enums.SysConfigModuleEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 系统配置创建命令
 *
 * @author siact
 */
@Data
public class SysConfigCreateCommand {

    @ApiModelProperty("模块")
    @NotNull(message = "[模块]不能为空")
    private SysConfigModuleEnum module;

    @ApiModelProperty("配置编码")
    @NotBlank(message = "[配置编码]不能为空")
    private String scCode;

    @ApiModelProperty("配置名称")
    @NotBlank(message = "[配置名称]不能为空")
    private String scName;

    @ApiModelProperty("配置说明")
    @NotBlank(message = "[配置说明]不能为空")
    private String description;

    @ApiModelProperty("配置数据（JSON 对象）")
    @NotNull(message = "[配置数据]不能为空")
    private JSONObject data;
}
```

- [ ] **Step 4: 创建 SysConfigUpdateCommand.java**

```java
package com.siact.module.system.command;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 系统配置更新命令
 *
 * @author siact
 */
@Data
public class SysConfigUpdateCommand {

    @ApiModelProperty("配置名称")
    private String scName;

    @ApiModelProperty("配置说明")
    private String description;

    @ApiModelProperty("配置数据（JSON 对象）")
    @NotNull(message = "[配置数据]不能为空")
    private JSONObject data;

    @ApiModelProperty("乐观锁版本号")
    @NotNull(message = "[版本号]不能为空")
    private Integer version;
}
```

- [ ] **Step 5: 提交 DTO 和 Command**

```bash
git add src/main/java/com/siact/module/system/dto/ src/main/java/com/siact/module/system/command/
git commit -m "feat(sys-config): 添加 DTO 和 Command 类"
```

---

### Task 8: 创建服务接口

**Files:**
- Create: `src/main/java/com/siact/module/system/service/SysConfigService.java`

- [ ] **Step 1: 创建服务接口 SysConfigService.java**

```java
package com.siact.module.system.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.system.command.SysConfigCreateCommand;
import com.siact.module.system.command.SysConfigUpdateCommand;
import com.siact.module.system.dto.SysConfigDTO;
import com.siact.module.system.dto.SysConfigItemDTO;
import com.siact.module.system.entity.SysConfigEntity;
import com.siact.module.system.enums.SysConfigModuleEnum;

import java.util.List;
import java.util.Map;

/**
 * 系统选项配置服务接口
 *
 * @author siact
 */
public interface SysConfigService extends IService<SysConfigEntity> {

    /**
     * 获取配置（按 scCode）
     *
     * @param scCode 配置编码
     * @return 组装后的配置对象
     */
    SysConfigDTO getByCode(String scCode);

    /**
     * 创建配置
     *
     * @param command 创建命令
     * @return 是否成功
     */
    Boolean create(SysConfigCreateCommand command);

    /**
     * 更新配置（完整覆盖）
     *
     * @param scCode  配置编码
     * @param command 更新命令
     * @return 是否成功
     */
    Boolean update(String scCode, SysConfigUpdateCommand command);

    /**
     * 删除配置（按 scCode）
     *
     * @param scCode 配置编码
     * @return 是否成功
     */
    Boolean deleteByCode(String scCode);

    /**
     * 按模块查询所有配置
     *
     * @param module 模块
     * @return 配置列表
     */
    List<SysConfigDTO> listByModule(SysConfigModuleEnum module);

    /**
     * 按编码列表批量查询
     *
     * @param scCodes 配置编码列表
     * @return 配置 Map（key: scCode）
     */
    Map<String, SysConfigDTO> batchGet(List<String> scCodes);

    /**
     * 获取单个配置项
     *
     * @param scCode 配置编码
     * @param scPath 配置路径
     * @return 单项配置数据
     */
    SysConfigItemDTO getItem(String scCode, String scPath);

    /**
     * 更新单个配置项
     *
     * @param scCode  配置编码
     * @param scPath  配置路径
     * @param value   新值
     * @param version 版本号
     * @return 是否成功
     */
    Boolean updateItem(String scCode, String scPath, String value, Integer version);

    /**
     * 删除单个配置项
     *
     * @param scCode 配置编码
     * @param scPath 配置路径
     * @return 是否成功
     */
    Boolean deleteItem(String scCode, String scPath);

    /**
     * 全量刷新配置
     *
     * @param scCode  配置编码
     * @param command 更新命令
     * @return 是否成功
     */
    Boolean refresh(String scCode, SysConfigUpdateCommand command);
}
```

- [ ] **Step 2: 提交服务接口**

```bash
git add src/main/java/com/siact/module/system/service/SysConfigService.java
git commit -m "feat(sys-config): 添加 SysConfigService 服务接口"
```

---

### Task 9: 创建服务实现类

**Files:**
- Create: `src/main/java/com/siact/module/system/service/impl/SysConfigServiceImpl.java`

- [ ] **Step 1: 创建服务实现 SysConfigServiceImpl.java**

```java
package com.siact.module.system.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.system.command.SysConfigCreateCommand;
import com.siact.module.system.command.SysConfigUpdateCommand;
import com.siact.module.system.dto.SysConfigDTO;
import com.siact.module.system.dto.SysConfigItemDTO;
import com.siact.module.system.entity.SysConfigEntity;
import com.siact.module.system.enums.SysConfigModuleEnum;
import com.siact.module.system.mapper.SysConfigMapper;
import com.siact.module.system.processor.ConfigAssembler;
import com.siact.module.system.processor.ConfigFlattener;
import com.siact.module.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 系统选项配置服务实现
 *
 * @author siact
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfigEntity> implements SysConfigService {

    private final ConfigFlattener flattener;
    private final ConfigAssembler assembler;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "sys:config:";
    private static final String CACHE_KEY_MODULE = "sys:config:module:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    @Override
    public SysConfigDTO getByCode(String scCode) {
        String cacheKey = CACHE_KEY_PREFIX + scCode;

        // 1. 查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof SysConfigDTO) {
            return (SysConfigDTO) cached;
        }

        // 2. 查数据库
        List<SysConfigEntity> entities = listByScCode(scCode);
        if (entities.isEmpty()) {
            return null;
        }

        // 3. 组装并缓存
        SysConfigDTO dto = assembleDTO(entities);
        redisTemplate.opsForValue().set(cacheKey, dto, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(SysConfigCreateCommand command) {
        // 1. 检查 scCode 是否已存在
        if (existsByScCode(command.getScCode())) {
            throw new RuntimeException("配置编码已存在: " + command.getScCode());
        }

        // 2. 扁平化并保存
        List<SysConfigEntity> entities = flattener.flatten(
                command.getModule(),
                command.getScCode(),
                command.getScName(),
                command.getDescription(),
                command.getData()
        );

        if (entities.isEmpty()) {
            // 空 JSON 对象，创建一个占位记录
            SysConfigEntity placeholder = new SysConfigEntity();
            placeholder.setModule(command.getModule());
            placeholder.setScCode(command.getScCode());
            placeholder.setScPath("");
            placeholder.setScName(command.getScName());
            placeholder.setScType(null);
            placeholder.setScValue("");
            placeholder.setDescription(command.getDescription());
            placeholder.setVersion(1);
            entities.add(placeholder);
        }

        boolean success = saveBatch(entities);

        // 3. 写缓存
        if (success) {
            SysConfigDTO dto = assembleDTO(entities);
            String cacheKey = CACHE_KEY_PREFIX + command.getScCode();
            redisTemplate.opsForValue().set(cacheKey, dto, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(String scCode, SysConfigUpdateCommand command) {
        // 1. 查询现有数据获取 module 和版本校验
        List<SysConfigEntity> existing = listByScCode(scCode);
        if (existing.isEmpty()) {
            throw new RuntimeException("配置不存在: " + scCode);
        }

        // 2. 版本校验（取第一条记录的版本）
        Integer currentVersion = existing.get(0).getVersion();
        if (!currentVersion.equals(command.getVersion())) {
            throw new RuntimeException("配置已被修改，请刷新后重试");
        }

        SysConfigModuleEnum module = existing.get(0).getModule();
        String scName = command.getScName() != null ? command.getScName() : existing.get(0).getScName();
        String description = command.getDescription() != null ? command.getDescription() : existing.get(0).getDescription();

        // 3. 扁平化新数据
        List<SysConfigEntity> newEntities = flattener.flatten(module, scCode, scName, description, command.getData());

        // 4. 设置新版本号
        int newVersion = currentVersion + 1;
        for (SysConfigEntity entity : newEntities) {
            entity.setVersion(newVersion);
        }

        // 5. 删除旧数据，插入新数据
        deleteByScCodeInternal(scCode);
        boolean success = saveBatch(newEntities);

        // 6. 删除缓存
        if (success) {
            evictCache(scCode, module);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByCode(String scCode) {
        List<SysConfigEntity> entities = listByScCode(scCode);
        if (entities.isEmpty()) {
            return false;
        }

        SysConfigModuleEnum module = entities.get(0).getModule();
        boolean success = deleteByScCodeInternal(scCode);

        if (success) {
            evictCache(scCode, module);
        }

        return success;
    }

    @Override
    public List<SysConfigDTO> listByModule(SysConfigModuleEnum module) {
        String cacheKey = CACHE_KEY_MODULE + module.name();

        // 1. 查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Map) {
            return new ArrayList<>(((Map<String, SysConfigDTO>) cached).values());
        }

        // 2. 查数据库
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getModule, module);
        List<SysConfigEntity> allEntities = baseMapper.selectList(wrapper);

        // 3. 按 scCode 分组并组装
        Map<String, List<SysConfigEntity>> grouped = allEntities.stream()
                .collect(Collectors.groupingBy(SysConfigEntity::getScCode));

        Map<String, SysConfigDTO> resultMap = new HashMap<>();
        for (Map.Entry<String, List<SysConfigEntity>> entry : grouped.entrySet()) {
            SysConfigDTO dto = assembleDTO(entry.getValue());
            resultMap.put(entry.getKey(), dto);
        }

        // 4. 缓存
        redisTemplate.opsForValue().set(cacheKey, resultMap, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return new ArrayList<>(resultMap.values());
    }

    @Override
    public Map<String, SysConfigDTO> batchGet(List<String> scCodes) {
        Map<String, SysConfigDTO> result = new HashMap<>();

        for (String scCode : scCodes) {
            SysConfigDTO dto = getByCode(scCode);
            if (dto != null) {
                result.put(scCode, dto);
            }
        }

        return result;
    }

    @Override
    public SysConfigItemDTO getItem(String scCode, String scPath) {
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getScCode, scCode)
                .eq(SysConfigEntity::getScPath, scPath);

        SysConfigEntity entity = baseMapper.selectOne(wrapper);
        if (entity == null) {
            return null;
        }

        return toItemDTO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateItem(String scCode, String scPath, String value, Integer version) {
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getScCode, scCode)
                .eq(SysConfigEntity::getScPath, scPath);

        SysConfigEntity entity = baseMapper.selectOne(wrapper);
        if (entity == null) {
            throw new RuntimeException("配置项不存在: " + scCode + "/" + scPath);
        }

        if (!entity.getVersion().equals(version)) {
            throw new RuntimeException("配置已被修改，请刷新后重试");
        }

        entity.setScValue(value);
        entity.setVersion(version + 1);

        int rows = baseMapper.updateById(entity);

        if (rows > 0) {
            evictCache(scCode, entity.getModule());
        }

        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteItem(String scCode, String scPath) {
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getScCode, scCode)
                .eq(SysConfigEntity::getScPath, scPath);

        SysConfigEntity entity = baseMapper.selectOne(wrapper);
        if (entity == null) {
            return false;
        }

        int rows = baseMapper.delete(wrapper);

        if (rows > 0) {
            evictCache(scCode, entity.getModule());
        }

        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean refresh(String scCode, SysConfigUpdateCommand command) {
        return update(scCode, command);
    }

    // ========== 私有方法 ==========

    private List<SysConfigEntity> listByScCode(String scCode) {
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getScCode, scCode)
                .orderByAsc(SysConfigEntity::getScPath);
        return baseMapper.selectList(wrapper);
    }

    private boolean existsByScCode(String scCode) {
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getScCode, scCode);
        return baseMapper.selectCount(wrapper) > 0;
    }

    private boolean deleteByScCodeInternal(String scCode) {
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getScCode, scCode);
        return baseMapper.delete(wrapper) > 0;
    }

    private SysConfigDTO assembleDTO(List<SysConfigEntity> entities) {
        if (entities.isEmpty()) {
            return null;
        }

        SysConfigDTO dto = new SysConfigDTO();
        SysConfigEntity first = entities.get(0);
        dto.setScCode(first.getScCode());
        dto.setModule(first.getModule());
        dto.setScName(first.getScName());
        dto.setDescription(first.getDescription());
        dto.setVersion(first.getVersion());
        dto.setData(assembler.assemble(entities));

        return dto;
    }

    private SysConfigItemDTO toItemDTO(SysConfigEntity entity) {
        SysConfigItemDTO dto = new SysConfigItemDTO();
        dto.setScCode(entity.getScCode());
        dto.setScPath(entity.getScPath());
        dto.setScName(entity.getScName());
        dto.setScType(entity.getScType());
        dto.setScValue(entity.getScValue());
        dto.setDescription(entity.getDescription());
        dto.setVersion(entity.getVersion());
        return dto;
    }

    private void evictCache(String scCode, SysConfigModuleEnum module) {
        redisTemplate.delete(CACHE_KEY_PREFIX + scCode);
        redisTemplate.delete(CACHE_KEY_MODULE + module.name());
    }
}
```

- [ ] **Step 2: 提交服务实现**

```bash
git add src/main/java/com/siact/module/system/service/impl/SysConfigServiceImpl.java
git commit -m "feat(sys-config): 添加 SysConfigServiceImpl 服务实现"
```

---

### Task 10: 创建控制器

**Files:**
- Create: `src/main/java/com/siact/module/system/controller/SysConfigController.java`

- [ ] **Step 1: 创建控制器 SysConfigController.java**

```java
package com.siact.module.system.controller;

import com.siact.common.R;
import com.siact.module.system.command.SysConfigCreateCommand;
import com.siact.module.system.command.SysConfigUpdateCommand;
import com.siact.module.system.dto.SysConfigDTO;
import com.siact.module.system.dto.SysConfigItemDTO;
import com.siact.module.system.enums.SysConfigModuleEnum;
import com.siact.module.system.service.SysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统选项配置控制器
 *
 * @author siact
 */
@Api(tags = "系统选项配置")
@RequiredArgsConstructor
@RestController
@RequestMapping("/sys-config")
public class SysConfigController {

    private final SysConfigService service;

    // ========== 单配置 CRUD ==========

    @ApiOperation("获取配置")
    @GetMapping("/{scCode}")
    public R<SysConfigDTO> getByCode(@PathVariable String scCode) {
        SysConfigDTO dto = service.getByCode(scCode);
        if (dto == null) {
            return R.fail(404, "配置不存在");
        }
        return R.data(dto);
    }

    @ApiOperation("创建配置")
    @PostMapping
    public R<Boolean> create(@Validated @RequestBody SysConfigCreateCommand command) {
        try {
            return R.data(service.create(command));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("已存在")) {
                return R.fail(409, e.getMessage());
            }
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("更新配置")
    @PutMapping("/{scCode}")
    public R<Boolean> update(@PathVariable String scCode, @Validated @RequestBody SysConfigUpdateCommand command) {
        try {
            return R.data(service.update(scCode, command));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return R.fail(404, e.getMessage());
            }
            if (e.getMessage().contains("已被修改")) {
                return R.fail(409, e.getMessage());
            }
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("删除配置")
    @DeleteMapping("/{scCode}")
    public R<Boolean> delete(@PathVariable String scCode) {
        return R.data(service.deleteByCode(scCode));
    }

    // ========== 批量查询 ==========

    @ApiOperation("按模块查询配置列表")
    @GetMapping("/module/{module}")
    public R<List<SysConfigDTO>> listByModule(@PathVariable SysConfigModuleEnum module) {
        return R.data(service.listByModule(module));
    }

    @ApiOperation("按编码列表批量查询")
    @PostMapping("/batch")
    public R<Map<String, SysConfigDTO>> batchGet(@RequestBody List<String> scCodes) {
        return R.data(service.batchGet(scCodes));
    }

    // ========== 配置项管理 ==========

    @ApiOperation("获取单个配置项")
    @GetMapping("/{scCode}/path/{scPath}")
    public R<SysConfigItemDTO> getItem(@PathVariable String scCode, @PathVariable String scPath) {
        SysConfigItemDTO dto = service.getItem(scCode, scPath);
        if (dto == null) {
            return R.fail(404, "配置项不存在");
        }
        return R.data(dto);
    }

    @ApiOperation("更新单个配置项")
    @PatchMapping("/{scCode}/path/{scPath}")
    public R<Boolean> updateItem(@PathVariable String scCode, @PathVariable String scPath,
                                  @RequestParam String value, @RequestParam Integer version) {
        try {
            return R.data(service.updateItem(scCode, scPath, value, version));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return R.fail(404, e.getMessage());
            }
            if (e.getMessage().contains("已被修改")) {
                return R.fail(409, e.getMessage());
            }
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("删除单个配置项")
    @DeleteMapping("/{scCode}/path/{scPath}")
    public R<Boolean> deleteItem(@PathVariable String scCode, @PathVariable String scPath) {
        return R.data(service.deleteItem(scCode, scPath));
    }

    // ========== 全量刷新 ==========

    @ApiOperation("全量刷新配置")
    @PostMapping("/{scCode}/refresh")
    public R<Boolean> refresh(@PathVariable String scCode, @Validated @RequestBody SysConfigUpdateCommand command) {
        try {
            return R.data(service.refresh(scCode, command));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return R.fail(404, e.getMessage());
            }
            if (e.getMessage().contains("已被修改")) {
                return R.fail(409, e.getMessage());
            }
            return R.fail(e.getMessage());
        }
    }
}
```

- [ ] **Step 2: 提交控制器**

```bash
git add src/main/java/com/siact/module/system/controller/SysConfigController.java
git commit -m "feat(sys-config): 添加 SysConfigController 控制器"
```

---

### Task 11: 集成测试与验证

**Files:**
- 无新增文件

- [ ] **Step 1: 启动应用验证编译**

```bash
mvn compile
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 启动应用**

```bash
mvn spring-boot:run
```

Expected: 应用正常启动，无异常

- [ ] **Step 3: 测试 API 接口**

使用 Knife4j 文档页面（`/doc.html`）或 curl 测试：

```bash
# 创建配置
curl -X POST http://localhost:8080/sys-config \
  -H "Content-Type: application/json" \
  -d '{"module":"SYSTEM","scCode":"test_config","scName":"测试配置","description":"测试描述","data":{"name":"测试","range":[100,500]}}'

# 获取配置
curl http://localhost:8080/sys-config/test_config

# 更新配置
curl -X PUT http://localhost:8080/sys-config/test_config \
  -H "Content-Type: application/json" \
  -d '{"data":{"name":"更新测试","range":[200,800]},,"version":1}'

# 删除配置
curl -X DELETE http://localhost:8080/sys-config/test_config
```

- [ ] **Step 4: 最终提交（如有修复）**

如有编译错误或逻辑修复，提交修复：
```bash
git add -A
git commit -m "fix(sys-config): 修复编译和逻辑问题"
```

---

## 自检清单

**Spec 覆盖：**
- [x] 表结构创建（Task 1）
- [x] 枚举定义（Task 2）
- [x] 实体类（Task 3）
- [x] Mapper（Task 4）
- [x] 扁平化处理器（Task 5）
- [x] 组装处理器（Task 6）
- [x] DTO/Command（Task 7）
- [x] 服务接口（Task 8）
- [x] 服务实现 + 缓存（Task 9）
- [x] 控制器所有接口（Task 10）
- [x] 验证测试（Task 11）

**Placeholder 扫描：** 无 TBD/TODO/实现占位符

**类型一致性：**
- SysConfigDTO.data 为 JSONObject（Task 7 → Task 9 → Task 10）
- SysConfigUpdateCommand.version 为 Integer（Task 7 → Task 9）
- scCode/scPath 命名一致（所有 Task）