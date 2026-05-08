# 液位控制模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在后端新建 `levelcontrol` 模块，实现液位控制配置管理、实时数据查询、算法结果存储和预测曲线查询。

**Architecture:** 新建 `com.siact.module.levelcontrol` 独立模块，分层结构（Controller → Service → Repository → Mapper/Entity）。复用 `TaosDataService`（TDengine 查询）、`BaseEntity`（审计字段）、`SysConfigService`（点位编码配置）。Controller 直接返回数据对象，由 `ResponseBodyAdvice` 自动包装响应。

**Tech Stack:** Java 8, Spring Boot 2.6.13, MyBatis-Plus 3.4.3.1, TDengine 2.x (JDBC REST), MySQL 8.0, Knife4j 4.3.0

**Spec:** `docs/superpowers/specs/2026-05-07-level-control-design.md`

---

## File Map

| 操作 | 文件路径 | 职责 |
|------|----------|------|
| Modify | `db/schema.sql` | 添加3张表的DDL |
| Modify | `src/.../module/system/constants/SysConfigCodeConstants.java` | 添加液位配置常量 |
| Create | `src/.../module/levelcontrol/enums/LevelControlModeEnum.java` | 控制模式枚举 |
| Create | `src/.../module/levelcontrol/entity/LevelControlConfigEntity.java` | 配置表实体 |
| Create | `src/.../module/levelcontrol/entity/LevelAlgorithmResultEntity.java` | 算法结果表实体 |
| Create | `src/.../module/levelcontrol/entity/LevelPredictedDataEntity.java` | 预测数据表实体 |
| Create | `src/.../module/levelcontrol/mapper/LevelControlConfigMapper.java` | 配置表Mapper |
| Create | `src/.../module/levelcontrol/mapper/LevelAlgorithmResultMapper.java` | 算法结果Mapper |
| Create | `src/.../module/levelcontrol/mapper/LevelPredictedDataMapper.java` | 预测数据Mapper |
| Create | `src/.../module/levelcontrol/dto/LevelControlConfigDTO.java` | 保存配置请求 |
| Create | `src/.../module/levelcontrol/dto/LevelModeSwitchDTO.java` | 模式切换请求 |
| Create | `src/.../module/levelcontrol/query/LevelPredictCurveQuery.java` | 预测曲线查询参数 |
| Create | `src/.../module/levelcontrol/vo/LevelControlConfigVO.java` | 配置返回 |
| Create | `src/.../module/levelcontrol/vo/LevelRealtimeVO.java` | 实时数据返回 |
| Create | `src/.../module/levelcontrol/vo/LevelAlgorithmResultVO.java` | 算法结果返回 |
| Create | `src/.../module/levelcontrol/vo/LevelPredictCurveVO.java` | 预测曲线返回 |
| Create | `src/.../module/levelcontrol/vo/LevelPredictCurveSeriesVO.java` | 曲线系列数据 |
| Create | `src/.../module/levelcontrol/vo/LevelCurveDataVO.java` | 曲线数据点 |
| Create | `src/.../module/levelcontrol/repository/LevelControlConfigRepository.java` | 配置Repository接口 |
| Create | `src/.../module/levelcontrol/repository/impl/LevelControlConfigRepositoryImpl.java` | 配置Repository实现 |
| Create | `src/.../module/levelcontrol/repository/LevelAlgorithmResultRepository.java` | 算法结果Repository接口 |
| Create | `src/.../module/levelcontrol/repository/impl/LevelAlgorithmResultRepositoryImpl.java` | 算法结果Repository实现 |
| Create | `src/.../module/levelcontrol/service/LevelControlConfigService.java` | 配置Service接口 |
| Create | `src/.../module/levelcontrol/service/impl/LevelControlConfigServiceImpl.java` | 配置Service实现 |
| Create | `src/.../module/levelcontrol/service/LevelAlgorithmResultService.java` | 算法结果Service接口 |
| Create | `src/.../module/levelcontrol/service/impl/LevelAlgorithmResultServiceImpl.java` | 算法结果Service实现 |
| Create | `src/.../module/levelcontrol/service/LevelPredictService.java` | 预测+实时Service接口 |
| Create | `src/.../module/levelcontrol/service/impl/LevelPredictServiceImpl.java` | 预测+实时Service实现 |
| Create | `src/.../module/levelcontrol/controller/LevelControlController.java` | 配置写操作Controller |
| Create | `src/.../module/levelcontrol/controller/LevelDataController.java` | 数据读操作Controller |

Base path: `/home/Tso/devroot/code/projects/kic-be/src/main/java/com/siact/module/levelcontrol/`

---

### Task 1: 添加数据库 DDL

**Files:**
- Modify: `db/schema.sql`

- [ ] **Step 1: 追加建表语句**

在 `db/schema.sql` 文件末尾追加以下 DDL：

```sql
-- ----------------------------
-- 液位控制配置表
-- ----------------------------
DROP TABLE IF EXISTS `level_control_config`;
CREATE TABLE `level_control_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `data_code` VARCHAR(64) NOT NULL COMMENT '点位编码',
  `mode` VARCHAR(16) NOT NULL DEFAULT 'ai' COMMENT '控制模式：ai/pid/manual',
  `ai_predict_window` DECIMAL(10,2) DEFAULT NULL COMMENT 'AI预测窗口',
  `ai_predict_duration` DECIMAL(10,2) DEFAULT NULL COMMENT 'AI预测时长',
  `pid_pb` DECIMAL(10,2) DEFAULT NULL COMMENT 'PID比例带PB',
  `pid_ti` DECIMAL(10,2) DEFAULT NULL COMMENT 'PID积分时间TI',
  `pid_td` DECIMAL(10,2) DEFAULT NULL COMMENT 'PID微分时间TD',
  `manual_control_value` DECIMAL(10,2) DEFAULT NULL COMMENT '人工控制值',
  `safe_limit` DECIMAL(10,2) DEFAULT NULL COMMENT '安全限制',
  `opening_upper_limit` DECIMAL(10,2) DEFAULT NULL COMMENT '开度上限',
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `create_by` VARCHAR(64) DEFAULT NULL,
  `update_by` VARCHAR(64) DEFAULT NULL,
  `deleted` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_data_code` (`data_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='液位控制配置表';

-- ----------------------------
-- 液位算法结果表
-- ----------------------------
DROP TABLE IF EXISTS `level_algorithm_result`;
CREATE TABLE `level_algorithm_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `data_code` VARCHAR(64) NOT NULL COMMENT '点位编码',
  `level_trend` DECIMAL(10,4) DEFAULT NULL COMMENT '液位趋势值',
  `recommended_opening` DECIMAL(10,4) DEFAULT NULL COMMENT '推荐开度',
  `level_status` VARCHAR(32) DEFAULT NULL COMMENT '液位状态：normal/warning/alarm',
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_data_code` (`data_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='液位算法结果表';

-- ----------------------------
-- 液位预测数据表（预留）
-- ----------------------------
DROP TABLE IF EXISTS `level_predicted_data`;
CREATE TABLE `level_predicted_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `data_code` VARCHAR(64) NOT NULL COMMENT '点位编码',
  `predicted_time` VARCHAR(32) DEFAULT NULL COMMENT '预测时间点',
  `predicted_value` DECIMAL(10,4) DEFAULT NULL COMMENT '预测值',
  `predicted_type` INT DEFAULT NULL COMMENT '预测类型（预留）',
  `unit` VARCHAR(16) DEFAULT NULL COMMENT '单位',
  `create_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_data_code_time` (`data_code`, `predicted_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='液位预测数据表';
```

- [ ] **Step 2: Commit**

```bash
git add db/schema.sql
git commit -m "feat(level-control): 添加液位控制模块数据库DDL"
```

---

### Task 2: 创建枚举类和更新配置常量

**Files:**
- Create: `src/.../module/levelcontrol/enums/LevelControlModeEnum.java`
- Modify: `src/.../module/system/constants/SysConfigCodeConstants.java`

- [ ] **Step 1: 创建控制模式枚举**

```java
// 文件: com/siact/module/levelcontrol/enums/LevelControlModeEnum.java
package com.siact.module.levelcontrol.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LevelControlModeEnum {
    AI("ai", "AI智控"),
    PID("pid", "PID控制"),
    MANUAL("manual", "人工控制");

    private final String code;
    private final String name;

    public static LevelControlModeEnum fromCode(String code) {
        for (LevelControlModeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知的控制模式: " + code);
    }
}
```

- [ ] **Step 2: 更新 SysConfigCodeConstants 添加液位配置常量**

在 `SysConfigCodeConstants` 类中追加常量字段：

```java
public static final String LEVEL_CONTROL_DATACODES = "level_control_datacodes";
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/module/levelcontrol/enums/LevelControlModeEnum.java \
        src/main/java/com/siact/module/system/constants/SysConfigCodeConstants.java
git commit -m "feat(level-control): 新增控制模式枚举和配置常量"
```

---

### Task 3: 创建 Entity 类

**Files:**
- Create: `src/.../module/levelcontrol/entity/LevelControlConfigEntity.java`
- Create: `src/.../module/levelcontrol/entity/LevelAlgorithmResultEntity.java`
- Create: `src/.../module/levelcontrol/entity/LevelPredictedDataEntity.java`

- [ ] **Step 1: 创建 LevelControlConfigEntity**

继承 `BaseEntity`，包含完整审计字段和逻辑删除。

```java
// 文件: com/siact/module/levelcontrol/entity/LevelControlConfigEntity.java
package com.siact.module.levelcontrol.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.siact.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("level_control_config")
public class LevelControlConfigEntity extends BaseEntity {
    private String dataCode;
    private String mode;
    private BigDecimal aiPredictWindow;
    private BigDecimal aiPredictDuration;
    private BigDecimal pidPb;
    private BigDecimal pidTi;
    private BigDecimal pidTd;
    private BigDecimal manualControlValue;
    private BigDecimal safeLimit;
    private BigDecimal openingUpperLimit;
}
```

- [ ] **Step 2: 创建 LevelAlgorithmResultEntity**

不继承 `BaseEntity`，仅有 `createTime` 和 `updateTime`。

```java
// 文件: com/siact/module/levelcontrol/entity/LevelAlgorithmResultEntity.java
package com.siact.module.levelcontrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("level_algorithm_result")
public class LevelAlgorithmResultEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String dataCode;
    private BigDecimal levelTrend;
    private BigDecimal recommendedOpening;
    private String levelStatus;
    private Date createTime;
    private Date updateTime;
}
```

- [ ] **Step 3: 创建 LevelPredictedDataEntity**

不继承 `BaseEntity`，仅有 `createTime`。

```java
// 文件: com/siact/module/levelcontrol/entity/LevelPredictedDataEntity.java
package com.siact.module.levelcontrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("level_predicted_data")
public class LevelPredictedDataEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String dataCode;
    private String predictedTime;
    private BigDecimal predictedValue;
    private Integer predictedType;
    private String unit;
    private Date createTime;
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/siact/module/levelcontrol/entity/
git commit -m "feat(level-control): 新增液位控制模块Entity类"
```

---

### Task 4: 创建 Mapper 接口

**Files:**
- Create: `src/.../module/levelcontrol/mapper/LevelControlConfigMapper.java`
- Create: `src/.../module/levelcontrol/mapper/LevelAlgorithmResultMapper.java`
- Create: `src/.../module/levelcontrol/mapper/LevelPredictedDataMapper.java`

- [ ] **Step 1: 创建三个 Mapper 接口**

基础 CRUD 均由 `BaseMapper` 提供，无需自定义 XML。

```java
// 文件: com/siact/module/levelcontrol/mapper/LevelControlConfigMapper.java
package com.siact.module.levelcontrol.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.levelcontrol.entity.LevelControlConfigEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LevelControlConfigMapper extends BaseMapper<LevelControlConfigEntity> {
}
```

```java
// 文件: com/siact/module/levelcontrol/mapper/LevelAlgorithmResultMapper.java
package com.siact.module.levelcontrol.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.levelcontrol.entity.LevelAlgorithmResultEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LevelAlgorithmResultMapper extends BaseMapper<LevelAlgorithmResultEntity> {
}
```

```java
// 文件: com/siact/module/levelcontrol/mapper/LevelPredictedDataMapper.java
package com.siact.module.levelcontrol.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.levelcontrol.entity.LevelPredictedDataEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LevelPredictedDataMapper extends BaseMapper<LevelPredictedDataEntity> {
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/siact/module/levelcontrol/mapper/
git commit -m "feat(level-control): 新增液位控制模块Mapper接口"
```

---

### Task 5: 创建 DTO、Query 和 VO 类

**Files:**
- Create: `src/.../module/levelcontrol/dto/LevelControlConfigDTO.java`
- Create: `src/.../module/levelcontrol/dto/LevelModeSwitchDTO.java`
- Create: `src/.../module/levelcontrol/query/LevelPredictCurveQuery.java`
- Create: `src/.../module/levelcontrol/vo/LevelControlConfigVO.java`
- Create: `src/.../module/levelcontrol/vo/LevelRealtimeVO.java`
- Create: `src/.../module/levelcontrol/vo/LevelAlgorithmResultVO.java`
- Create: `src/.../module/levelcontrol/vo/LevelPredictCurveVO.java`
- Create: `src/.../module/levelcontrol/vo/LevelPredictCurveSeriesVO.java`
- Create: `src/.../module/levelcontrol/vo/LevelCurveDataVO.java`

- [ ] **Step 1: 创建 DTO 类**

```java
// 文件: com/siact/module/levelcontrol/dto/LevelControlConfigDTO.java
package com.siact.module.levelcontrol.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class LevelControlConfigDTO {

    @NotBlank(message = "点位编码不能为空")
    @ApiModelProperty(value = "点位编码", required = true)
    private String dataCode;

    @NotBlank(message = "控制模式不能为空")
    @ApiModelProperty(value = "控制模式：ai/pid/manual", required = true)
    private String mode;

    @ApiModelProperty(value = "AI预测窗口")
    private BigDecimal aiPredictWindow;

    @ApiModelProperty(value = "AI预测时长")
    private BigDecimal aiPredictDuration;

    @ApiModelProperty(value = "PID比例带PB")
    private BigDecimal pidPb;

    @ApiModelProperty(value = "PID积分时间TI")
    private BigDecimal pidTi;

    @ApiModelProperty(value = "PID微分时间TD")
    private BigDecimal pidTd;

    @ApiModelProperty(value = "人工控制值")
    private BigDecimal manualControlValue;

    @ApiModelProperty(value = "安全限制")
    private BigDecimal safeLimit;

    @ApiModelProperty(value = "开度上限")
    private BigDecimal openingUpperLimit;
}
```

```java
// 文件: com/siact/module/levelcontrol/dto/LevelModeSwitchDTO.java
package com.siact.module.levelcontrol.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LevelModeSwitchDTO {

    @NotBlank(message = "点位编码不能为空")
    @ApiModelProperty(value = "点位编码", required = true)
    private String dataCode;

    @NotBlank(message = "控制模式不能为空")
    @ApiModelProperty(value = "控制模式：ai/pid/manual", required = true)
    private String mode;
}
```

- [ ] **Step 2: 创建 Query 类**

```java
// 文件: com/siact/module/levelcontrol/query/LevelPredictCurveQuery.java
package com.siact.module.levelcontrol.query;

import com.siact.common.validated.StringContains;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class LevelPredictCurveQuery {

    @NotBlank(message = "点位编码不能为空")
    @ApiModelProperty(value = "点位编码", required = true)
    private String dataCode;

    @NotBlank(message = "开始时间不能为空")
    @ApiModelProperty(value = "开始时间 yyyy-MM-dd HH:mm:ss", required = true)
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    @ApiModelProperty(value = "结束时间 yyyy-MM-dd HH:mm:ss", required = true)
    private String endTime;

    @NotNull(message = "步长不能为空")
    @ApiModelProperty(value = "步长", required = true)
    private Integer ts;

    @StringContains(limitValues = {"Y", "M", "D", "H", "MIN"}, message = "步长单位不正确")
    @ApiModelProperty(value = "步长单位：Y/M/D/H/MIN", required = true)
    private String tsUnit;

    @StringContains(limitValues = {"AVG", "MAX", "MIN", "LAST", "FIRST", "SUM", "COUNT"}, message = "计算类型不正确")
    @ApiModelProperty(value = "计算类型：AVG/MAX/MIN/LAST/FIRST/SUM/COUNT")
    private String calcType;
}
```

- [ ] **Step 3: 创建 VO 类**

```java
// 文件: com/siact/module/levelcontrol/vo/LevelControlConfigVO.java
package com.siact.module.levelcontrol.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LevelControlConfigVO {
    private String dataCode;
    private String mode;
    private BigDecimal aiPredictWindow;
    private BigDecimal aiPredictDuration;
    private BigDecimal pidPb;
    private BigDecimal pidTi;
    private BigDecimal pidTd;
    private BigDecimal manualControlValue;
    private BigDecimal safeLimit;
    private BigDecimal openingUpperLimit;
}
```

```java
// 文件: com/siact/module/levelcontrol/vo/LevelRealtimeVO.java
package com.siact.module.levelcontrol.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
public class LevelRealtimeVO {
    private DataPoint level;
    private DataPoint opening;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataPoint {
        private BigDecimal value;
        private String unit;
    }
}
```

```java
// 文件: com/siact/module/levelcontrol/vo/LevelAlgorithmResultVO.java
package com.siact.module.levelcontrol.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LevelAlgorithmResultVO {
    private BigDecimal levelTrend;
    private BigDecimal recommendedOpening;
    private String levelStatus;
}
```

```java
// 文件: com/siact/module/levelcontrol/vo/LevelPredictCurveVO.java
package com.siact.module.levelcontrol.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class LevelPredictCurveVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<String> xdata;
    private List<LevelPredictCurveSeriesVO> series;
}
```

```java
// 文件: com/siact/module/levelcontrol/vo/LevelPredictCurveSeriesVO.java
package com.siact.module.levelcontrol.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
public class LevelPredictCurveSeriesVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String dataCode;
    private String name;
    private Map<String, LevelCurveDataVO> data;
}
```

```java
// 文件: com/siact/module/levelcontrol/vo/LevelCurveDataVO.java
package com.siact.module.levelcontrol.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class LevelCurveDataVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private List<Object[]> value;
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/siact/module/levelcontrol/dto/ \
        src/main/java/com/siact/module/levelcontrol/query/ \
        src/main/java/com/siact/module/levelcontrol/vo/
git commit -m "feat(level-control): 新增DTO/Query/VO类"
```

---

### Task 6: 创建 Repository 层

**Files:**
- Create: `src/.../module/levelcontrol/repository/LevelControlConfigRepository.java`
- Create: `src/.../module/levelcontrol/repository/impl/LevelControlConfigRepositoryImpl.java`
- Create: `src/.../module/levelcontrol/repository/LevelAlgorithmResultRepository.java`
- Create: `src/.../module/levelcontrol/repository/impl/LevelAlgorithmResultRepositoryImpl.java`

- [ ] **Step 1: 创建 LevelControlConfigRepository 接口**

```java
// 文件: com/siact/module/levelcontrol/repository/LevelControlConfigRepository.java
package com.siact.module.levelcontrol.repository;

import com.siact.common.repository.BaseRepository;
import com.siact.module.levelcontrol.entity.LevelControlConfigEntity;

public interface LevelControlConfigRepository extends BaseRepository<LevelControlConfigEntity> {
    LevelControlConfigEntity getByDataCode(String dataCode);

    void saveOrUpdate(LevelControlConfigEntity entity);
}
```

- [ ] **Step 2: 创建 LevelControlConfigRepositoryImpl**

```java
// 文件: com/siact/module/levelcontrol/repository/impl/LevelControlConfigRepositoryImpl.java
package com.siact.module.levelcontrol.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.siact.common.repository.BaseRepositoryImpl;
import com.siact.module.levelcontrol.entity.LevelControlConfigEntity;
import com.siact.module.levelcontrol.mapper.LevelControlConfigMapper;
import com.siact.module.levelcontrol.repository.LevelControlConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class LevelControlConfigRepositoryImpl
        extends BaseRepositoryImpl<LevelControlConfigMapper, LevelControlConfigEntity>
        implements LevelControlConfigRepository {

    private final LevelControlConfigMapper mapper;

    @Override
    public LevelControlConfigEntity getByDataCode(String dataCode) {
        return mapper.selectOne(
                Wrappers.<LevelControlConfigEntity>lambdaQuery()
                        .eq(LevelControlConfigEntity::getDataCode, dataCode));
    }

    @Override
    public void saveOrUpdate(LevelControlConfigEntity entity) {
        LevelControlConfigEntity existing = getByDataCode(entity.getDataCode());
        if (existing != null) {
            entity.setId(existing.getId());
            mapper.updateById(entity);
        } else {
            mapper.insert(entity);
        }
    }
}
```

- [ ] **Step 3: 创建 LevelAlgorithmResultRepository 接口**

```java
// 文件: com/siact/module/levelcontrol/repository/LevelAlgorithmResultRepository.java
package com.siact.module.levelcontrol.repository;

import com.siact.common.repository.BaseRepository;
import com.siact.module.levelcontrol.entity.LevelAlgorithmResultEntity;

public interface LevelAlgorithmResultRepository extends BaseRepository<LevelAlgorithmResultEntity> {
    LevelAlgorithmResultEntity getByDataCode(String dataCode);

    void saveOrUpdate(LevelAlgorithmResultEntity entity);
}
```

- [ ] **Step 4: 创建 LevelAlgorithmResultRepositoryImpl**

```java
// 文件: com/siact/module/levelcontrol/repository/impl/LevelAlgorithmResultRepositoryImpl.java
package com.siact.module.levelcontrol.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.siact.common.repository.BaseRepositoryImpl;
import com.siact.module.levelcontrol.entity.LevelAlgorithmResultEntity;
import com.siact.module.levelcontrol.mapper.LevelAlgorithmResultMapper;
import com.siact.module.levelcontrol.repository.LevelAlgorithmResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class LevelAlgorithmResultRepositoryImpl
        extends BaseRepositoryImpl<LevelAlgorithmResultMapper, LevelAlgorithmResultEntity>
        implements LevelAlgorithmResultRepository {

    private final LevelAlgorithmResultMapper mapper;

    @Override
    public LevelAlgorithmResultEntity getByDataCode(String dataCode) {
        return mapper.selectOne(
                Wrappers.<LevelAlgorithmResultEntity>lambdaQuery()
                        .eq(LevelAlgorithmResultEntity::getDataCode, dataCode));
    }

    @Override
    public void saveOrUpdate(LevelAlgorithmResultEntity entity) {
        LevelAlgorithmResultEntity existing = getByDataCode(entity.getDataCode());
        if (existing != null) {
            entity.setId(existing.getId());
            mapper.updateById(entity);
        } else {
            mapper.insert(entity);
        }
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/siact/module/levelcontrol/repository/
git commit -m "feat(level-control): 新增Repository层"
```

---

### Task 7: 创建 Service 层

**Files:**
- Create: `src/.../module/levelcontrol/service/LevelControlConfigService.java`
- Create: `src/.../module/levelcontrol/service/impl/LevelControlConfigServiceImpl.java`
- Create: `src/.../module/levelcontrol/service/LevelAlgorithmResultService.java`
- Create: `src/.../module/levelcontrol/service/impl/LevelAlgorithmResultServiceImpl.java`
- Create: `src/.../module/levelcontrol/service/LevelPredictService.java`
- Create: `src/.../module/levelcontrol/service/impl/LevelPredictServiceImpl.java`

- [ ] **Step 1: 创建 LevelControlConfigService 接口和实现**

```java
// 文件: com/siact/module/levelcontrol/service/LevelControlConfigService.java
package com.siact.module.levelcontrol.service;

import com.siact.module.levelcontrol.dto.LevelControlConfigDTO;
import com.siact.module.levelcontrol.dto.LevelModeSwitchDTO;
import com.siact.module.levelcontrol.vo.LevelControlConfigVO;

public interface LevelControlConfigService {
    LevelControlConfigVO getConfig(String dataCode);

    void saveConfig(LevelControlConfigDTO dto);

    void switchMode(LevelModeSwitchDTO dto);
}
```

```java
// 文件: com/siact/module/levelcontrol/service/impl/LevelControlConfigServiceImpl.java
package com.siact.module.levelcontrol.service.impl;

import com.siact.module.levelcontrol.dto.LevelControlConfigDTO;
import com.siact.module.levelcontrol.dto.LevelModeSwitchDTO;
import com.siact.module.levelcontrol.entity.LevelControlConfigEntity;
import com.siact.module.levelcontrol.enums.LevelControlModeEnum;
import com.siact.module.levelcontrol.repository.LevelControlConfigRepository;
import com.siact.module.levelcontrol.service.LevelControlConfigService;
import com.siact.module.levelcontrol.vo.LevelControlConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@RequiredArgsConstructor
@Service
public class LevelControlConfigServiceImpl implements LevelControlConfigService {

    private final LevelControlConfigRepository repository;

    @Override
    public LevelControlConfigVO getConfig(String dataCode) {
        LevelControlConfigEntity entity = repository.getByDataCode(dataCode);
        if (entity == null) {
            return null;
        }
        return toVO(entity);
    }

    @Override
    public void saveConfig(LevelControlConfigDTO dto) {
        LevelControlModeEnum.fromCode(dto.getMode());
        LevelControlConfigEntity entity = repository.getByDataCode(dto.getDataCode());
        if (entity == null) {
            entity = new LevelControlConfigEntity();
            entity.setDataCode(dto.getDataCode());
            entity.setCreateTime(new Date());
            entity.setCreateBy("system");
        }
        entity.setMode(dto.getMode());
        entity.setAiPredictWindow(dto.getAiPredictWindow());
        entity.setAiPredictDuration(dto.getAiPredictDuration());
        entity.setPidPb(dto.getPidPb());
        entity.setPidTi(dto.getPidTi());
        entity.setPidTd(dto.getPidTd());
        entity.setManualControlValue(dto.getManualControlValue());
        entity.setSafeLimit(dto.getSafeLimit());
        entity.setOpeningUpperLimit(dto.getOpeningUpperLimit());
        entity.setUpdateTime(new Date());
        entity.setUpdateBy("system");
        repository.saveOrUpdate(entity);
    }

    @Override
    public void switchMode(LevelModeSwitchDTO dto) {
        LevelControlModeEnum.fromCode(dto.getMode());
        LevelControlConfigEntity entity = repository.getByDataCode(dto.getDataCode());
        if (entity == null) {
            entity = new LevelControlConfigEntity();
            entity.setDataCode(dto.getDataCode());
            entity.setCreateTime(new Date());
            entity.setCreateBy("system");
        }
        entity.setMode(dto.getMode());
        entity.setUpdateTime(new Date());
        entity.setUpdateBy("system");
        repository.saveOrUpdate(entity);
    }

    private LevelControlConfigVO toVO(LevelControlConfigEntity entity) {
        LevelControlConfigVO vo = new LevelControlConfigVO();
        vo.setDataCode(entity.getDataCode());
        vo.setMode(entity.getMode());
        vo.setAiPredictWindow(entity.getAiPredictWindow());
        vo.setAiPredictDuration(entity.getAiPredictDuration());
        vo.setPidPb(entity.getPidPb());
        vo.setPidTi(entity.getPidTi());
        vo.setPidTd(entity.getPidTd());
        vo.setManualControlValue(entity.getManualControlValue());
        vo.setSafeLimit(entity.getSafeLimit());
        vo.setOpeningUpperLimit(entity.getOpeningUpperLimit());
        return vo;
    }
}
```

- [ ] **Step 2: 创建 LevelAlgorithmResultService 接口和实现**

```java
// 文件: com/siact/module/levelcontrol/service/LevelAlgorithmResultService.java
package com.siact.module.levelcontrol.service;

import com.siact.module.levelcontrol.vo.LevelAlgorithmResultVO;

public interface LevelAlgorithmResultService {
    LevelAlgorithmResultVO getResult(String dataCode);
}
```

```java
// 文件: com/siact/module/levelcontrol/service/impl/LevelAlgorithmResultServiceImpl.java
package com.siact.module.levelcontrol.service.impl;

import com.siact.module.levelcontrol.entity.LevelAlgorithmResultEntity;
import com.siact.module.levelcontrol.repository.LevelAlgorithmResultRepository;
import com.siact.module.levelcontrol.service.LevelAlgorithmResultService;
import com.siact.module.levelcontrol.vo.LevelAlgorithmResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LevelAlgorithmResultServiceImpl implements LevelAlgorithmResultService {

    private final LevelAlgorithmResultRepository repository;

    @Override
    public LevelAlgorithmResultVO getResult(String dataCode) {
        LevelAlgorithmResultEntity entity = repository.getByDataCode(dataCode);
        if (entity == null) {
            return null;
        }
        LevelAlgorithmResultVO vo = new LevelAlgorithmResultVO();
        vo.setLevelTrend(entity.getLevelTrend());
        vo.setRecommendedOpening(entity.getRecommendedOpening());
        vo.setLevelStatus(entity.getLevelStatus());
        return vo;
    }
}
```

- [ ] **Step 3: 创建 LevelPredictService 接口和实现**

注入 `TaosDataService` 用于 TDengine 查询，注入 `SysConfigService` 用于读取液位和开度的点位编码配置。

```java
// 文件: com/siact/module/levelcontrol/service/LevelPredictService.java
package com.siact.module.levelcontrol.service;

import com.siact.module.levelcontrol.query.LevelPredictCurveQuery;
import com.siact.module.levelcontrol.vo.LevelPredictCurveVO;
import com.siact.module.levelcontrol.vo.LevelRealtimeVO;

public interface LevelPredictService {
    LevelRealtimeVO getRealtimeData(String dataCode);

    LevelPredictCurveVO queryPredictCurve(LevelPredictCurveQuery query);
}
```

```java
// 文件: com/siact/module/levelcontrol/service/impl/LevelPredictServiceImpl.java
package com.siact.module.levelcontrol.service.impl;

import com.siact.module.levelcontrol.query.LevelPredictCurveQuery;
import com.siact.module.levelcontrol.service.LevelPredictService;
import com.siact.module.levelcontrol.vo.LevelCurveDataVO;
import com.siact.module.levelcontrol.vo.LevelPredictCurveSeriesVO;
import com.siact.module.levelcontrol.vo.LevelPredictCurveVO;
import com.siact.module.levelcontrol.vo.LevelRealtimeVO;
import com.siact.module.system.constants.SysConfigCodeConstants;
import com.siact.module.system.dto.SysConfigDTO;
import com.siact.module.system.service.SysConfigService;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.tdengine.service.TaosDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class LevelPredictServiceImpl implements LevelPredictService {

    private final TaosDataService taosDataService;
    private final SysConfigService sysConfigService;

    @Override
    @SuppressWarnings("unchecked")
    public LevelRealtimeVO getRealtimeData(String dataCode) {
        // 从 sysconfig 读取液位和开度的点位编码映射
        SysConfigDTO config = sysConfigService.getByCode(SysConfigCodeConstants.LEVEL_CONTROL_DATACODES);
        String levelCode = dataCode;
        String openingCode = null;
        if (config != null && config.getData() instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) config.getData();
            if (dataMap.containsKey("openingCode")) {
                openingCode = String.valueOf(dataMap.get("openingCode"));
            }
        }

        LevelRealtimeVO vo = new LevelRealtimeVO();

        // 查询液位实时值
        List<IntervalDataDto> levelResult = queryLastRealValue(levelCode);
        if (!levelResult.isEmpty()) {
            IntervalDataDto levelDto = levelResult.get(0);
            vo.setLevel(new LevelRealtimeVO.DataPoint(levelDto.getItemVal(), levelDto.getUnit()));
        }

        // 查询开度实时值
        if (openingCode != null) {
            List<IntervalDataDto> openingResult = queryLastRealValue(openingCode);
            if (!openingResult.isEmpty()) {
                IntervalDataDto openingDto = openingResult.get(0);
                vo.setOpening(new LevelRealtimeVO.DataPoint(openingDto.getItemVal(), openingDto.getUnit()));
            }
        }

        return vo;
    }

    private List<IntervalDataDto> queryLastRealValue(String dataCode) {
        try {
            return taosDataService.queryIntervalVal(buildLastQueryParams(dataCode));
        } catch (Exception e) {
            log.error("查询实时值失败, dataCode={}", dataCode, e);
            return Collections.emptyList();
        }
    }

    private IntervalValParamsDto buildLastQueryParams(String dataCode) {
        IntervalValParamsDto params = new IntervalValParamsDto();
        params.setDataCodes(Collections.singletonList(dataCode));
        // 查最近1小时的数据
        Calendar cal = Calendar.getInstance();
        String endTime = String.format("%tF %<tT", cal);
        cal.add(Calendar.HOUR, -1);
        String startTime = String.format("%tF %<tT", cal);
        params.setStartTime(startTime);
        params.setEndTime(endTime);
        params.setTs(1);
        params.setTsUnit("H");
        params.setCalcType("LAST");
        params.setFormatVal("HH:mm");
        return params;
    }

    @Override
    public LevelPredictCurveVO queryPredictCurve(LevelPredictCurveQuery query) {
        IntervalValParamsDto paramsDto = new IntervalValParamsDto();
        paramsDto.setDataCodes(Collections.singletonList(query.getDataCode()));
        paramsDto.setStartTime(query.getStartTime());
        paramsDto.setEndTime(query.getEndTime());
        paramsDto.setTs(query.getTs());
        paramsDto.setTsUnit(query.getTsUnit());
        paramsDto.setCalcType(query.getCalcType() != null ? query.getCalcType() : "AVG");
        paramsDto.setFormatVal("HH:mm");

        List<IntervalDataDto> dataList = taosDataService.queryIntervalVal(paramsDto);

        List<String> xdata = dataList.stream()
                .map(IntervalDataDto::getTime)
                .distinct()
                .collect(Collectors.toList());

        List<Object[]> actualValues = dataList.stream()
                .map(d -> new Object[]{d.getTime(), d.getItemVal()})
                .collect(Collectors.toList());

        Map<String, LevelCurveDataVO> dataMap = new LinkedHashMap<>();
        dataMap.put("actual", new LevelCurveDataVO("液位实际值", actualValues));

        LevelPredictCurveSeriesVO seriesVO = LevelPredictCurveSeriesVO.builder()
                .dataCode(query.getDataCode())
                .name("液位")
                .data(dataMap)
                .build();

        return LevelPredictCurveVO.builder()
                .xdata(xdata)
                .series(Collections.singletonList(seriesVO))
                .build();
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/siact/module/levelcontrol/service/
git commit -m "feat(level-control): 新增Service层"
```

---

### Task 8: 创建 Controller 层

**Files:**
- Create: `src/.../module/levelcontrol/controller/LevelControlController.java`
- Create: `src/.../module/levelcontrol/controller/LevelDataController.java`

- [ ] **Step 1: 创建 LevelControlController（配置写操作）**

```java
// 文件: com/siact/module/levelcontrol/controller/LevelControlController.java
package com.siact.module.levelcontrol.controller;

import com.siact.module.levelcontrol.dto.LevelControlConfigDTO;
import com.siact.module.levelcontrol.dto.LevelModeSwitchDTO;
import com.siact.module.levelcontrol.service.LevelControlConfigService;
import com.siact.module.levelcontrol.vo.LevelControlConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "液位控制配置")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/level-control")
public class LevelControlController {

    private final LevelControlConfigService configService;

    @ApiOperation("获取控制配置")
    @ApiImplicitParam(name = "dataCode", value = "点位编码", required = true, dataType = "String", paramType = "path")
    @GetMapping("/config/{dataCode}")
    public LevelControlConfigVO getConfig(@PathVariable String dataCode) {
        return configService.getConfig(dataCode);
    }

    @ApiOperation("保存/更新控制配置")
    @PutMapping("/config")
    public void saveConfig(@RequestBody @Validated LevelControlConfigDTO dto) {
        configService.saveConfig(dto);
    }

    @ApiOperation("切换控制模式")
    @PutMapping("/mode")
    public void switchMode(@RequestBody @Validated LevelModeSwitchDTO dto) {
        configService.switchMode(dto);
    }
}
```

- [ ] **Step 2: 创建 LevelDataController（数据读操作）**

```java
// 文件: com/siact/module/levelcontrol/controller/LevelDataController.java
package com.siact.module.levelcontrol.controller;

import com.siact.module.levelcontrol.query.LevelPredictCurveQuery;
import com.siact.module.levelcontrol.service.LevelAlgorithmResultService;
import com.siact.module.levelcontrol.service.LevelPredictService;
import com.siact.module.levelcontrol.vo.LevelAlgorithmResultVO;
import com.siact.module.levelcontrol.vo.LevelPredictCurveVO;
import com.siact.module.levelcontrol.vo.LevelRealtimeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "液位数据查询")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/level-control")
public class LevelDataController {

    private final LevelPredictService predictService;
    private final LevelAlgorithmResultService algorithmResultService;

    @ApiOperation("获取实时数据（液位+开度）")
    @ApiImplicitParam(name = "dataCode", value = "点位编码", required = true, dataType = "String", paramType = "path")
    @GetMapping("/realtime/{dataCode}")
    public LevelRealtimeVO getRealtimeData(@PathVariable String dataCode) {
        return predictService.getRealtimeData(dataCode);
    }

    @ApiOperation("获取算法结果")
    @ApiImplicitParam(name = "dataCode", value = "点位编码", required = true, dataType = "String", paramType = "path")
    @GetMapping("/algorithm-result/{dataCode}")
    public LevelAlgorithmResultVO getAlgorithmResult(@PathVariable String dataCode) {
        return algorithmResultService.getResult(dataCode);
    }

    @ApiOperation("液位预测曲线查询")
    @PostMapping("/predict-curve")
    public LevelPredictCurveVO queryPredictCurve(@RequestBody @Validated LevelPredictCurveQuery query) {
        return predictService.queryPredictCurve(query);
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/module/levelcontrol/controller/
git commit -m "feat(level-control): 新增Controller层，实现液位控制接口"
```

---

### Task 9: 编译验证

- [ ] **Step 1: 执行 Maven 编译**

```bash
cd /home/Tso/devroot/code/projects/kic-be && mvn compile -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 2: 修复编译问题（如有）**

根据编译错误输出调整代码。

- [ ] **Step 3: 最终 Commit（如有修复）**

```bash
git add -A && git commit -m "fix(level-control): 修复编译问题"
```
