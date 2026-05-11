# Temperature Predict 实现计划

> **对于代理工作者：** 所需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐项执行此计划。步骤使用复选框（`- [ ]`）语法进行跟踪。

**目标：** 解析算法响应中的 `result.temps` 温度预测数据，保存到新的 `temperature_predict` 表。

**架构：** 新建 Entity → Mapper → Repository(接口+实现)，在 `IntelligentDataServiceImpl.callIntelligentInterface()` 中遍历 cptData 匹配 result.temps 批量保存。

**技术栈：** Java 8, Spring Boot, MyBatis-Plus, fastjson2

**编译命令：** `export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`

---

### Task 1: 新建 TemperaturePredictEntity

**Files:**
- Create: `src/main/java/com/siact/module/algorithm/entity/TemperaturePredictEntity.java`

- [ ] **Step 1: 创建 Entity 文件**

```java
package com.siact.module.algorithm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("temperature_predict")
public class TemperaturePredictEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String pointName;

    private String propName;

    private String propCode;

    private String time;

    private BigDecimal itemValue;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/module/algorithm/entity/TemperaturePredictEntity.java
git commit -m "feat(algorithm): 新增 TemperaturePredictEntity"
```

---

### Task 2: 新建 TemperaturePredictMapper

**Files:**
- Create: `src/main/java/com/siact/module/algorithm/mapper/TemperaturePredictMapper.java`

- [ ] **Step 1: 创建 Mapper 文件**

```java
package com.siact.module.algorithm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.algorithm.entity.TemperaturePredictEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TemperaturePredictMapper extends BaseMapper<TemperaturePredictEntity> {
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/module/algorithm/mapper/TemperaturePredictMapper.java
git commit -m "feat(algorithm): 新增 TemperaturePredictMapper"
```

---

### Task 3: 新建 TemperaturePredictRepository 接口和实现

**Files:**
- Create: `src/main/java/com/siact/module/algorithm/repository/TemperaturePredictRepository.java`
- Create: `src/main/java/com/siact/module/algorithm/repository/impl/TemperaturePredictRepositoryImpl.java`

- [ ] **Step 1: 创建 Repository 接口**

```java
package com.siact.module.algorithm.repository;

import com.siact.common.repository.BaseRepository;
import com.siact.module.algorithm.entity.TemperaturePredictEntity;

public interface TemperaturePredictRepository extends BaseRepository<TemperaturePredictEntity> {
}
```

- [ ] **Step 2: 创建 Repository 实现**

```java
package com.siact.module.algorithm.repository.impl;

import com.siact.common.repository.BaseRepositoryImpl;
import com.siact.module.algorithm.mapper.TemperaturePredictMapper;
import com.siact.module.algorithm.entity.TemperaturePredictEntity;
import com.siact.module.algorithm.repository.TemperaturePredictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class TemperaturePredictRepositoryImpl extends BaseRepositoryImpl<TemperaturePredictMapper, TemperaturePredictEntity> implements TemperaturePredictRepository {
    private final TemperaturePredictMapper mapper;
}
```

- [ ] **Step 3: 验证编译通过**

Run: `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/siact/module/algorithm/repository/TemperaturePredictRepository.java src/main/java/com/siact/module/algorithm/repository/impl/TemperaturePredictRepositoryImpl.java
git commit -m "feat(algorithm): 新增 TemperaturePredictRepository"
```

---

### Task 4: 添加建表 SQL

**Files:**
- Modify: `db/schema.sql` (文件末尾追加)

- [ ] **Step 1: 在 `db/schema.sql` 文件末尾追加建表语句**

```sql

-- 温度预测数据表
create table if not exists temperature_predict
(
    id          bigint primary key not null comment '主键',
    point_name  varchar(100)       not null comment '点位名称',
    prop_name   varchar(255)       not null comment '属性名称',
    prop_code   varchar(100)       not null comment '属性编码',
    time        varchar(50)    default null comment '预测时间点',
    item_value  decimal(10, 4) default null comment '预测值',
    create_time datetime       default current_timestamp comment '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='温度预测数据表';
```

> 注意：`time` 字段类型使用 `varchar(50)` 与 `IntelligentDataEntity.time` 的 String 类型一致（该项目的 time 字段存储格式化的时间字符串，非 timestamp）。如果需要改为 `timestamp` 类型，请调整此处及 Entity 中 `time` 字段类型。

- [ ] **Step 2: 提交**

```bash
git add db/schema.sql
git commit -m "feat(algorithm): 新增 temperature_predict 建表 SQL"
```

---

### Task 5: 在 callIntelligentInterface 中解析 result.temps 并保存

**Files:**
- Modify: `src/main/java/com/siact/module/algorithm/services/impl/IntelligentDataServiceImpl.java`

- [ ] **Step 1: 注入 TemperaturePredictRepository**

在类的字段区域（约第 70 行 `deviceMappingRepository` 之后）添加：

```java
    private final TemperaturePredictRepository temperaturePredictRepository;
```

并在文件头部添加 import：

```java
import com.siact.module.algorithm.entity.TemperaturePredictEntity;
import com.siact.module.algorithm.repository.TemperaturePredictRepository;
```

- [ ] **Step 2: 替换 TODO 注释块为实际解析和保存逻辑**

找到第 143-190 行的 TODO 注释块（从 `// TODO: 数据解析调整:` 开始到 `// }` 结束的整块），替换为：

```java
        // 解析温度预测数据
        JSONObject temps = result.getJSONObject("temps");
        if (temps != null) {
            ArrayList<TemperaturePredictEntity> predictList = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> entry : cptData.entrySet()) {
                String pointName = entry.getKey();
                Map<String, Object> pointConfig = entry.getValue();
                String propName = MapUtils.getString(pointConfig, "name");
                String propCode = MapUtils.getString(pointConfig, "code");

                JSONObject tempData = temps.getJSONObject(propName);
                if (tempData == null) {
                    continue;
                }
                BigDecimal predValue = tempData.getBigDecimal("pred_value");
                if (predValue == null) {
                    continue;
                }

                predictList.add(TemperaturePredictEntity.builder()
                        .pointName(pointName)
                        .propName(propName)
                        .propCode(propCode)
                        .time(time)
                        .itemValue(predValue)
                        .build());
            }
            if (!predictList.isEmpty()) {
                temperaturePredictRepository.saveBatch(predictList);
            }
        }
```

替换后该区域完整代码应为：

```java
        collect.add(builder.intelliType(IntelliTypeEnum.GAS_DELTAC_EXPERT).val(expertDeltaC).build());
        collect.add(builder.intelliType(IntelliTypeEnum.GAS_LAST_SUM).val(lastGasSum).build());

        // 解析温度预测数据
        JSONObject temps = result.getJSONObject("temps");
        if (temps != null) {
            ... (如上代码块)
        }

        // 保存数据
        saveBatch(collect);
```

- [ ] **Step 3: 验证编译通过**

Run: `cd /home/Tso/devroot/code/projects/kic-be && export JAVA_HOME=/etc/jdk/zulu8 && mvn compile -s ~/.m2/settings-siact.xml -q 2>&1 | tail -10`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/siact/module/algorithm/services/impl/IntelligentDataServiceImpl.java
git commit -m "feat(algorithm): 解析温度预测数据并保存到 temperature_predict 表"
```