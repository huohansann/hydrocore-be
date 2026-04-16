# 设备点位维护功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 module/device 模块中实现设备点位的增删改查和多格式导入导出功能。

**Architecture:** 采用 DDD 分层风格（参照 system 模块 SysRole），Controller → Service → Repository → Mapper。不继承 BaseEntity，时间字段由数据库管理，主键使用雪花算法。

**Tech Stack:** Spring Boot 2.6.13, MyBatis-Plus 3.4.3.1, MapStruct 1.5.3, EasyPOI 4.4.0, JacksonUtils, Knife4j 4.3.0, MySQL 8.0

**关键约定：**
- Entity 不继承 BaseEntity，`createTime`/`updateTime` 由数据库自动管理
- 主键使用 `@TableId(type = IdType.ASSIGN_ID)` 雪花算法
- 不使用逻辑删除（物理删除）
- 使用 `@RequiredArgsConstructor` 构造器注入
- 接口直接返回对象，由 ResponseBodyAdvice 自动包装
- MapStruct 做对象转换，componentModel = "spring"
- 分页使用 `Page.of(page, pageSize)`，返回 `PageVO<T>`
- Entity 和 Controller 添加 Knife4j `@Schema` / `@Tag` / `@Operation` 注解
- 导出接口使用 `@NoResponseAdvice` 跳过响应包装（直接写文件流）
- 所有包路径基于 `com.siact.module.device`

---

### Task 1: DDL + Entity + Mapper

**Files:**
- Modify: `db/schema.sql`
- Create: `src/main/java/com/siact/module/device/entity/DeviceMappingEntity.java`
- Create: `src/main/java/com/siact/module/device/mapper/DeviceMappingMapper.java`

- [ ] **Step 1: 在 db/schema.sql 末尾追加建表语句**

在文件末尾追加：

```sql
-- =============================================
-- 设备点位映射表
-- =============================================
CREATE TABLE IF NOT EXISTS `device_mapping` (
    `id`          BIGINT        NOT NULL COMMENT '主键',
    `point_name`  VARCHAR(255)  NOT NULL COMMENT '现场点位名称',
    `item_id`     VARCHAR(100)  NOT NULL COMMENT '点位ID(TAOS_DB编码)',
    `prop_code`   VARCHAR(255)  NOT NULL COMMENT '属性编码(孪生长码)',
    `prop_name`   VARCHAR(255)  NOT NULL COMMENT '属性名称',
    `device_code` VARCHAR(255)  NOT NULL COMMENT '设备编码',
    `device_name` VARCHAR(255)  NOT NULL COMMENT '设备名称',
    `create_time` TIMESTAMP     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`      TEXT          COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_point_name` (`point_name`),
    UNIQUE KEY `uk_item_id` (`item_id`),
    UNIQUE KEY `uk_prop_code` (`prop_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备点位映射表';
```

- [ ] **Step 2: 创建 DeviceMappingEntity**

```java
package com.siact.module.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("device_mapping")
@ApiModel(description = "设备点位映射")
public class DeviceMappingEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("现场点位名称")
    private String pointName;

    @ApiModelProperty("点位ID(TAOS_DB编码)")
    private String itemId;

    @ApiModelProperty("属性编码(孪生长码)")
    private String propCode;

    @ApiModelProperty("属性名称")
    private String propName;

    @ApiModelProperty("设备编码")
    private String deviceCode;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("备注")
    private String remark;
}
```

- [ ] **Step 3: 创建 DeviceMappingMapper**

```java
package com.siact.module.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.device.entity.DeviceMappingEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceMappingMapper extends BaseMapper<DeviceMappingEntity> {
}
```

- [ ] **Step 4: 提交**

```bash
git add db/schema.sql src/main/java/com/siact/module/device/entity/DeviceMappingEntity.java src/main/java/com/siact/module/device/mapper/DeviceMappingMapper.java
git commit -m "feat(device): 添加 device_mapping 表结构、Entity 和 Mapper"
```

---

### Task 2: DTO 层（Query、QueryDTO、Command、VO）

**Files:**
- Create: `src/main/java/com/siact/module/device/query/DeviceMappingQuery.java`
- Create: `src/main/java/com/siact/module/device/dto/DeviceMappingQueryDTO.java`
- Create: `src/main/java/com/siact/module/device/command/DeviceMappingCommand.java`
- Create: `src/main/java/com/siact/module/device/vo/DeviceMappingVO.java`
- Create: `src/main/java/com/siact/module/device/vo/DeviceImportResult.java`

- [ ] **Step 1: 创建 DeviceMappingQuery**

```java
package com.siact.module.device.query;

import com.siact.common.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "设备点位查询条件")
public class DeviceMappingQuery extends PageQuery {

    @ApiModelProperty("现场点位名称(模糊)")
    private String pointName;

    @ApiModelProperty("点位ID(精确)")
    private String itemId;

    @ApiModelProperty("属性编码(精确)")
    private String propCode;

    @ApiModelProperty("属性名称(模糊)")
    private String propName;

    @ApiModelProperty("设备编码(精确)")
    private String deviceCode;

    @ApiModelProperty("设备名称(模糊)")
    private String deviceName;
}
```

- [ ] **Step 2: 创建 DeviceMappingQueryDTO**

```java
package com.siact.module.device.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceMappingQueryDTO {
    private String pointName;
    private String itemId;
    private String propCode;
    private String propName;
    private String deviceCode;
    private String deviceName;
}
```

- [ ] **Step 3: 创建 DeviceMappingCommand**

```java
package com.siact.module.device.command;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "设备点位新增/修改")
public class DeviceMappingCommand {

    @ApiModelProperty("主键(修改时必传)")
    private Long id;

    @NotBlank(message = "现场点位名称不能为空")
    @ApiModelProperty("现场点位名称")
    private String pointName;

    @NotBlank(message = "点位ID不能为空")
    @ApiModelProperty("点位ID(TAOS_DB编码)")
    private String itemId;

    @NotBlank(message = "属性编码不能为空")
    @ApiModelProperty("属性编码(孪生长码)")
    private String propCode;

    @NotBlank(message = "属性名称不能为空")
    @ApiModelProperty("属性名称")
    private String propName;

    @NotBlank(message = "设备编码不能为空")
    @ApiModelProperty("设备编码")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("备注")
    private String remark;
}
```

- [ ] **Step 4: 创建 DeviceMappingVO**

```java
package com.siact.module.device.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "设备点位视图对象")
public class DeviceMappingVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("现场点位名称")
    private String pointName;

    @ApiModelProperty("点位ID(TAOS_DB编码)")
    private String itemId;

    @ApiModelProperty("属性编码(孪生长码)")
    private String propCode;

    @ApiModelProperty("属性名称")
    private String propName;

    @ApiModelProperty("设备编码")
    private String deviceCode;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("备注")
    private String remark;
}
```

- [ ] **Step 5: 创建 DeviceImportResult**

```java
package com.siact.module.device.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel(description = "设备点位导入结果")
public class DeviceImportResult {

    @ApiModelProperty("新增成功条数")
    private int successCount;

    @ApiModelProperty("覆盖更新条数")
    private int updateCount;

    @ApiModelProperty("失败条数")
    private int failCount;

    @ApiModelProperty("失败详情")
    private List<ImportError> errors = new ArrayList<>();

    @Data
    @ApiModel(description = "导入错误详情")
    public static class ImportError {

        @ApiModelProperty("行号")
        private int row;

        @ApiModelProperty("点位名称")
        private String pointName;

        @ApiModelProperty("点位ID")
        private String itemId;

        @ApiModelProperty("失败原因")
        private String reason;
    }
}
```

- [ ] **Step 6: 提交**

```bash
git add src/main/java/com/siact/module/device/query/DeviceMappingQuery.java src/main/java/com/siact/module/device/dto/DeviceMappingQueryDTO.java src/main/java/com/siact/module/device/command/DeviceMappingCommand.java src/main/java/com/siact/module/device/vo/DeviceMappingVO.java src/main/java/com/siact/module/device/vo/DeviceImportResult.java
git commit -m "feat(device): 添加设备点位 Query、Command、VO 等 DTO 类"
```

---

### Task 3: Convert（MapStruct）

**Files:**
- Create: `src/main/java/com/siact/module/device/convert/DeviceMappingConvert.java`

- [ ] **Step 1: 创建 DeviceMappingConvert**

```java
package com.siact.module.device.convert;

import com.siact.module.device.command.DeviceMappingCommand;
import com.siact.module.device.dto.DeviceMappingQueryDTO;
import com.siact.module.device.entity.DeviceMappingEntity;
import com.siact.module.device.query.DeviceMappingQuery;
import com.siact.module.device.vo.DeviceMappingVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeviceMappingConvert {

    DeviceMappingQueryDTO toQueryDTO(DeviceMappingQuery query);

    DeviceMappingVO toVO(DeviceMappingEntity entity);

    List<DeviceMappingVO> toVOList(List<DeviceMappingEntity> entities);

    DeviceMappingEntity toEntity(DeviceMappingCommand command);

    DeviceMappingEntity toUpdateEntity(DeviceMappingCommand command);
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/siact/module/device/convert/DeviceMappingConvert.java
git commit -m "feat(device): 添加 DeviceMappingConvert MapStruct 转换器"
```

---

### Task 4: Repository

**Files:**
- Create: `src/main/java/com/siact/module/device/repository/DeviceMappingRepository.java`
- Create: `src/main/java/com/siact/module/device/repository/impl/DeviceMappingRepositoryImpl.java`

- [ ] **Step 1: 创建 DeviceMappingRepository 接口**

```java
package com.siact.module.device.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.device.dto.DeviceMappingQueryDTO;
import com.siact.module.device.entity.DeviceMappingEntity;

import java.util.List;

public interface DeviceMappingRepository {

    Page<DeviceMappingEntity> queryList(DeviceMappingQueryDTO queryDTO, Page<DeviceMappingEntity> page);

    List<DeviceMappingEntity> queryList(DeviceMappingQueryDTO queryDTO);

    boolean existsByPointName(String pointName);

    boolean existsByPointNameExcludeId(String pointName, Long excludeId);

    boolean existsByItemId(String itemId);

    boolean existsByItemIdExcludeId(String itemId, Long excludeId);

    boolean existsByPropCode(String propCode);

    boolean existsByPropCodeExcludeId(String propCode, Long excludeId);

    DeviceMappingEntity findByItemId(String itemId);

    DeviceMappingEntity findByPropCode(String propCode);
}
```

- [ ] **Step 2: 创建 DeviceMappingRepositoryImpl**

```java
package com.siact.module.device.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.device.dto.DeviceMappingQueryDTO;
import com.siact.module.device.entity.DeviceMappingEntity;
import com.siact.module.device.mapper.DeviceMappingMapper;
import com.siact.module.device.repository.DeviceMappingRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class DeviceMappingRepositoryImpl implements DeviceMappingRepository {
    private final DeviceMappingMapper mapper;

    @Override
    public Page<DeviceMappingEntity> queryList(DeviceMappingQueryDTO queryDTO, Page<DeviceMappingEntity> page) {
        return mapper.selectPage(page, buildQueryWrapper(queryDTO));
    }

    @Override
    public List<DeviceMappingEntity> queryList(DeviceMappingQueryDTO queryDTO) {
        return mapper.selectList(buildQueryWrapper(queryDTO));
    }

    @Override
    public boolean existsByPointName(String pointName) {
        return mapper.selectCount(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getPointName, pointName)) > 0;
    }

    @Override
    public boolean existsByPointNameExcludeId(String pointName, Long excludeId) {
        return mapper.selectCount(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getPointName, pointName)
                .ne(DeviceMappingEntity::getId, excludeId)) > 0;
    }

    @Override
    public boolean existsByItemId(String itemId) {
        return mapper.selectCount(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getItemId, itemId)) > 0;
    }

    @Override
    public boolean existsByItemIdExcludeId(String itemId, Long excludeId) {
        return mapper.selectCount(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getItemId, itemId)
                .ne(DeviceMappingEntity::getId, excludeId)) > 0;
    }

    @Override
    public boolean existsByPropCode(String propCode) {
        return mapper.selectCount(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getPropCode, propCode)) > 0;
    }

    @Override
    public boolean existsByPropCodeExcludeId(String propCode, Long excludeId) {
        return mapper.selectCount(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getPropCode, propCode)
                .ne(DeviceMappingEntity::getId, excludeId)) > 0;
    }

    @Override
    public DeviceMappingEntity findByItemId(String itemId) {
        return mapper.selectOne(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getItemId, itemId));
    }

    @Override
    public DeviceMappingEntity findByPropCode(String propCode) {
        return mapper.selectOne(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getPropCode, propCode));
    }

    private LambdaQueryWrapper<DeviceMappingEntity> buildQueryWrapper(DeviceMappingQueryDTO queryDTO) {
        return Wrappers.<DeviceMappingEntity>lambdaQuery()
                .like(StringUtils.isNotBlank(queryDTO.getPointName()), DeviceMappingEntity::getPointName, queryDTO.getPointName())
                .eq(StringUtils.isNotBlank(queryDTO.getItemId()), DeviceMappingEntity::getItemId, queryDTO.getItemId())
                .eq(StringUtils.isNotBlank(queryDTO.getPropCode()), DeviceMappingEntity::getPropCode, queryDTO.getPropCode())
                .like(StringUtils.isNotBlank(queryDTO.getPropName()), DeviceMappingEntity::getPropName, queryDTO.getPropName())
                .eq(StringUtils.isNotBlank(queryDTO.getDeviceCode()), DeviceMappingEntity::getDeviceCode, queryDTO.getDeviceCode())
                .like(StringUtils.isNotBlank(queryDTO.getDeviceName()), DeviceMappingEntity::getDeviceName, queryDTO.getDeviceName())
                .orderByDesc(DeviceMappingEntity::getUpdateTime);
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/module/device/repository/DeviceMappingRepository.java src/main/java/com/siact/module/device/repository/impl/DeviceMappingRepositoryImpl.java
git commit -m "feat(device): 添加 DeviceMappingRepository 查询与唯一性校验"
```

---

### Task 5: Service（CRUD）

**Files:**
- Create: `src/main/java/com/siact/module/device/service/DeviceMappingService.java`
- Create: `src/main/java/com/siact/module/device/service/impl/DeviceMappingServiceImpl.java`

- [ ] **Step 1: 创建 DeviceMappingService 接口**

```java
package com.siact.module.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.common.vo.PageVO;
import com.siact.module.device.command.DeviceMappingCommand;
import com.siact.module.device.entity.DeviceMappingEntity;
import com.siact.module.device.query.DeviceMappingQuery;
import com.siact.module.device.vo.DeviceMappingVO;

import java.util.List;

public interface DeviceMappingService extends IService<DeviceMappingEntity> {

    PageVO<DeviceMappingVO> list(DeviceMappingQuery query);

    DeviceMappingVO getById(Long id);

    Boolean add(DeviceMappingCommand command);

    Boolean update(DeviceMappingCommand command);

    Boolean delete(Long id);

    Boolean deleteBatch(List<Long> ids);

    Boolean clear();
}
```

- [ ] **Step 2: 创建 DeviceMappingServiceImpl**

```java
package com.siact.module.device.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.vo.PageVO;
import com.siact.common.exception.BizException;
import com.siact.module.device.command.DeviceMappingCommand;
import com.siact.module.device.convert.DeviceMappingConvert;
import com.siact.module.device.dto.DeviceMappingQueryDTO;
import com.siact.module.device.entity.DeviceMappingEntity;
import com.siact.module.device.mapper.DeviceMappingMapper;
import com.siact.module.device.query.DeviceMappingQuery;
import com.siact.module.device.repository.DeviceMappingRepository;
import com.siact.module.device.service.DeviceMappingService;
import com.siact.module.device.vo.DeviceMappingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DeviceMappingServiceImpl extends ServiceImpl<DeviceMappingMapper, DeviceMappingEntity> implements DeviceMappingService {
    private final DeviceMappingConvert convert;
    private final DeviceMappingRepository repository;

    @Override
    public PageVO<DeviceMappingVO> list(DeviceMappingQuery query) {
        DeviceMappingQueryDTO queryDTO = convert.toQueryDTO(query);
        Page<DeviceMappingEntity> page = repository.queryList(queryDTO, Page.of(query.getPage(), query.getPageSize()));
        List<DeviceMappingVO> voList = convert.toVOList(page.getRecords());
        return PageVO.<DeviceMappingVO>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(voList)
                .build();
    }

    @Override
    public DeviceMappingVO getById(Long id) {
        DeviceMappingEntity entity = this.getById(id);
        return convert.toVO(entity);
    }

    @Override
    public Boolean add(DeviceMappingCommand command) {
        checkUniqueForAdd(command);
        DeviceMappingEntity entity = convert.toEntity(command);
        return this.save(entity);
    }

    @Override
    public Boolean update(DeviceMappingCommand command) {
        checkUniqueForUpdate(command);
        DeviceMappingEntity entity = convert.toUpdateEntity(command);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteBatch(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean clear() {
        return this.remove(Wrappers.<DeviceMappingEntity>lambdaQuery());
    }

    private void checkUniqueForAdd(DeviceMappingCommand command) {
        if (repository.existsByPointName(command.getPointName())) {
            throw new BizException("现场点位名称已存在: " + command.getPointName());
        }
        if (repository.existsByItemId(command.getItemId())) {
            throw new BizException("点位ID已存在: " + command.getItemId());
        }
        if (repository.existsByPropCode(command.getPropCode())) {
            throw new BizException("属性编码已存在: " + command.getPropCode());
        }
    }

    private void checkUniqueForUpdate(DeviceMappingCommand command) {
        if (repository.existsByPointNameExcludeId(command.getPointName(), command.getId())) {
            throw new BizException("现场点位名称已存在: " + command.getPointName());
        }
        if (repository.existsByItemIdExcludeId(command.getItemId(), command.getId())) {
            throw new BizException("点位ID已存在: " + command.getItemId());
        }
        if (repository.existsByPropCodeExcludeId(command.getPropCode(), command.getId())) {
            throw new BizException("属性编码已存在: " + command.getPropCode());
        }
    }
}
```

**注意：** `getById(Long id)` 方法中调用 `this.getById(id)` 会产生递归调用。需要在实现中使用 `this.baseMapper.selectById(id)` 或通过 `super.getById(id)` 调用。修正如下：

```java
    @Override
    public DeviceMappingVO getById(Long id) {
        DeviceMappingEntity entity = super.getById(id);
        return convert.toVO(entity);
    }
```

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/module/device/service/DeviceMappingService.java src/main/java/com/siact/module/device/service/impl/DeviceMappingServiceImpl.java
git commit -m "feat(device): 添加 DeviceMappingService CRUD 业务逻辑"
```

---

### Task 6: Controller（CRUD）

**Files:**
- Create: `src/main/java/com/siact/module/device/controller/DeviceMappingController.java`

- [ ] **Step 1: 创建 DeviceMappingController**

```java
package com.siact.module.device.controller;

import com.siact.common.vo.PageVO;
import com.siact.module.device.command.DeviceMappingCommand;
import com.siact.module.device.query.DeviceMappingQuery;
import com.siact.module.device.service.DeviceMappingService;
import com.siact.module.device.vo.DeviceMappingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "设备点位管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("/device/mapping")
public class DeviceMappingController {
    private final DeviceMappingService service;

    @ApiOperation("分页查询")
    @GetMapping("/page")
    public PageVO<DeviceMappingVO> page(DeviceMappingQuery query) {
        return service.list(query);
    }

    @ApiOperation("查询单条")
    @GetMapping("/{id}")
    public DeviceMappingVO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @ApiOperation("新增")
    @PostMapping("/add")
    public Boolean add(@Valid @RequestBody DeviceMappingCommand command) {
        return service.add(command);
    }

    @ApiOperation("修改")
    @PutMapping("/update")
    public Boolean update(@Valid @RequestBody DeviceMappingCommand command) {
        return service.update(command);
    }

    @ApiOperation("单条删除")
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return service.delete(id);
    }

    @ApiOperation("批量删除")
    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody List<Long> ids) {
        return service.deleteBatch(ids);
    }

    @ApiOperation("全部清空")
    @DeleteMapping("/clear")
    public Boolean clear() {
        return service.clear();
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/siact/module/device/controller/DeviceMappingController.java
git commit -m "feat(device): 添加 DeviceMappingController CRUD 接口"
```

---

### Task 7: Service 导入功能

**Files:**
- Modify: `src/main/java/com/siact/module/device/service/DeviceMappingService.java`
- Modify: `src/main/java/com/siact/module/device/service/impl/DeviceMappingServiceImpl.java`

- [ ] **Step 1: 在 Service 接口中添加导入方法**

在 `DeviceMappingService` 接口末尾添加：

```java
    DeviceImportResult importData(MultipartFile file);
```

- [ ] **Step 2: 在 ServiceImpl 中实现导入逻辑**

在 `DeviceMappingServiceImpl` 中添加以下依赖和实现：

添加导入：

```java
import com.siact.module.device.vo.DeviceImportResult;
import com.siact.common.utils.JacksonUtils;
import com.siact.common.utils.ExcelUtils;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
```

添加实现方法：

```java
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceImportResult importData(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BizException("文件名不能为空");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        List<DeviceMappingCommand> commands;
        if (".xlsx".equals(extension) || ".xls".equals(extension)) {
            commands = parseExcel(file);
        } else if (".csv".equals(extension)) {
            commands = parseCsv(file);
        } else if (".json".equals(extension)) {
            commands = parseJson(file);
        } else {
            throw new BizException("不支持的文件格式: " + extension + "，仅支持 xlsx/xls/csv/json");
        }

        DeviceImportResult result = new DeviceImportResult();
        for (int i = 0; i < commands.size(); i++) {
            DeviceMappingCommand command = commands.get(i);
            int rowNum = i + 2; // Excel/CSV 第1行为表头，数据从第2行开始；JSON 从第1条开始但行号也从2显示
            try {
                validateCommand(command);
                DeviceMappingEntity existing = repository.findByItemId(command.getItemId());
                if (existing != null) {
                    command.setId(existing.getId());
                    DeviceMappingEntity entity = convert.toUpdateEntity(command);
                    this.updateById(entity);
                    result.setUpdateCount(result.getUpdateCount() + 1);
                } else {
                    DeviceMappingEntity entity = convert.toEntity(command);
                    this.save(entity);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                }
            } catch (Exception e) {
                DeviceImportResult.ImportError error = new DeviceImportResult.ImportError();
                error.setRow(rowNum);
                error.setPointName(command.getPointName());
                error.setItemId(command.getItemId());
                error.setReason(e.getMessage());
                result.getErrors().add(error);
                result.setFailCount(result.getFailCount() + 1);
            }
        }
        return result;
    }

    private void validateCommand(DeviceMappingCommand command) {
        if (command.getItemId() == null || command.getItemId().trim().isEmpty()) {
            throw new BizException("点位ID不能为空");
        }
        if (command.getPointName() == null || command.getPointName().trim().isEmpty()) {
            throw new BizException("现场点位名称不能为空");
        }
        if (command.getPropCode() == null || command.getPropCode().trim().isEmpty()) {
            throw new BizException("属性编码不能为空");
        }
        if (command.getPropName() == null || command.getPropName().trim().isEmpty()) {
            throw new BizException("属性名称不能为空");
        }
        if (command.getDeviceCode() == null || command.getDeviceCode().trim().isEmpty()) {
            throw new BizException("设备编码不能为空");
        }
        if (command.getDeviceName() == null || command.getDeviceName().trim().isEmpty()) {
            throw new BizException("设备名称不能为空");
        }
    }

    @SuppressWarnings("unchecked")
    private List<DeviceMappingCommand> parseExcel(MultipartFile file) {
        try {
            List<DeviceMappingCommand> result = new ArrayList<>();
            List<Object> rows = ExcelUtils.importExcel(file.getInputStream(), 0, 1, 0, Map.class);
            for (Object row : rows) {
                Map<String, Object> map = (Map<String, Object>) row;
                DeviceMappingCommand command = new DeviceMappingCommand();
                command.setPointName(getStringValue(map, "现场点位名称"));
                command.setItemId(getStringValue(map, "点位ID"));
                command.setPropCode(getStringValue(map, "属性编码"));
                command.setPropName(getStringValue(map, "属性名称"));
                command.setDeviceCode(getStringValue(map, "设备编码"));
                command.setDeviceName(getStringValue(map, "设备名称"));
                command.setRemark(getStringValue(map, "备注"));
                result.add(command);
            }
            return result;
        } catch (IOException e) {
            throw new BizException("Excel文件解析失败: " + e.getMessage());
        }
    }

    private List<DeviceMappingCommand> parseCsv(MultipartFile file) {
        try {
            List<DeviceMappingCommand> result = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String[] headers = reader.readLine().split(",");
            int pointNameIdx = findIndex(headers, "现场点位名称");
            int itemIdIdx = findIndex(headers, "点位ID");
            int propCodeIdx = findIndex(headers, "属性编码");
            int propNameIdx = findIndex(headers, "属性名称");
            int deviceCodeIdx = findIndex(headers, "设备编码");
            int deviceNameIdx = findIndex(headers, "设备名称");
            int remarkIdx = findIndex(headers, "备注");

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",", -1);
                DeviceMappingCommand command = new DeviceMappingCommand();
                command.setPointName(getIndexValue(values, pointNameIdx));
                command.setItemId(getIndexValue(values, itemIdIdx));
                command.setPropCode(getIndexValue(values, propCodeIdx));
                command.setPropName(getIndexValue(values, propNameIdx));
                command.setDeviceCode(getIndexValue(values, deviceCodeIdx));
                command.setDeviceName(getIndexValue(values, deviceNameIdx));
                command.setRemark(getIndexValue(values, remarkIdx));
                result.add(command);
            }
            return result;
        } catch (IOException e) {
            throw new BizException("CSV文件解析失败: " + e.getMessage());
        }
    }

    private List<DeviceMappingCommand> parseJson(MultipartFile file) {
        try {
            String json = new String(file.getBytes(), StandardCharsets.UTF_8);
            return JacksonUtils.fromJson(json, new TypeReference<List<DeviceMappingCommand>>() {});
        } catch (Exception e) {
            throw new BizException("JSON文件解析失败: " + e.getMessage());
        }
    }

    private int findIndex(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (name.equals(headers[i].trim())) return i;
        }
        return -1;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString().trim() : null;
    }

    private String getIndexValue(String[] values, int index) {
        if (index < 0 || index >= values.length) return null;
        return values[index].trim();
    }
```

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/module/device/service/DeviceMappingService.java src/main/java/com/siact/module/device/service/impl/DeviceMappingServiceImpl.java
git commit -m "feat(device): 添加设备点位导入功能(Excel/CSV/JSON)"
```

---

### Task 8: Service 导出功能

**Files:**
- Modify: `src/main/java/com/siact/module/device/service/DeviceMappingService.java`
- Modify: `src/main/java/com/siact/module/device/service/impl/DeviceMappingServiceImpl.java`

- [ ] **Step 1: 在 Service 接口中添加导出方法**

在 `DeviceMappingService` 接口末尾添加：

```java
    void exportData(DeviceMappingQuery query, String format, HttpServletResponse response);
```

- [ ] **Step 2: 在 ServiceImpl 中实现导出逻辑**

添加导入：

```java
import javax.servlet.http.HttpServletResponse;
import cn.afterturn.easypoi.excel.entity.ExcelExportEntity;
import java.util.LinkedHashMap;
import java.util.Map;
```

添加实现方法：

```java
    @Override
    public void exportData(DeviceMappingQuery query, String format, HttpServletResponse response) {
        DeviceMappingQueryDTO queryDTO = convert.toQueryDTO(query);
        List<DeviceMappingEntity> list = repository.queryList(queryDTO);
        List<DeviceMappingVO> voList = convert.toVOList(list);

        switch (format.toLowerCase()) {
            case "excel":
                exportExcel(voList, response);
                break;
            case "csv":
                exportCsv(voList, response);
                break;
            case "json":
                exportJson(voList, response);
                break;
            default:
                throw new BizException("不支持的导出格式: " + format + "，仅支持 excel/csv/json");
        }
    }

    private void exportExcel(List<DeviceMappingVO> voList, HttpServletResponse response) {
        List<ExcelExportEntity> headList = new ArrayList<>();
        headList.add(new ExcelExportEntity("现场点位名称", "pointName", 25));
        headList.add(new ExcelExportEntity("点位ID", "itemId", 20));
        headList.add(new ExcelExportEntity("属性编码", "propCode", 25));
        headList.add(new ExcelExportEntity("属性名称", "propName", 20));
        headList.add(new ExcelExportEntity("设备编码", "deviceCode", 20));
        headList.add(new ExcelExportEntity("设备名称", "deviceName", 20));
        headList.add(new ExcelExportEntity("备注", "remark", 30));

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (DeviceMappingVO vo : voList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("pointName", vo.getPointName());
            item.put("itemId", vo.getItemId());
            item.put("propCode", vo.getPropCode());
            item.put("propName", vo.getPropName());
            item.put("deviceCode", vo.getDeviceCode());
            item.put("deviceName", vo.getDeviceName());
            item.put("remark", vo.getRemark());
            dataList.add(item);
        }

        ExcelUtils.exportExcel(headList, "设备点位", dataList, response);
    }

    private void exportCsv(List<DeviceMappingVO> voList, HttpServletResponse response) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    java.net.URLEncoder.encode("设备点位.csv", "UTF-8"));

            // 写入 BOM 头，确保 Excel 打开 CSV 时中文不乱码
            response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            String[] headers = {"现场点位名称", "点位ID", "属性编码", "属性名称", "设备编码", "设备名称", "备注"};
            response.getOutputStream().write(String.join(",", headers).getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().write("\n".getBytes(StandardCharsets.UTF_8));

            for (DeviceMappingVO vo : voList) {
                String[] row = {
                        escapeCsv(vo.getPointName()),
                        escapeCsv(vo.getItemId()),
                        escapeCsv(vo.getPropCode()),
                        escapeCsv(vo.getPropName()),
                        escapeCsv(vo.getDeviceCode()),
                        escapeCsv(vo.getDeviceName()),
                        escapeCsv(vo.getRemark())
                };
                response.getOutputStream().write(String.join(",", row).getBytes(StandardCharsets.UTF_8));
                response.getOutputStream().write("\n".getBytes(StandardCharsets.UTF_8));
            }
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new BizException("CSV导出失败: " + e.getMessage());
        }
    }

    private void exportJson(List<DeviceMappingVO> voList, HttpServletResponse response) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    java.net.URLEncoder.encode("设备点位.json", "UTF-8"));
            String json = JacksonUtils.toPrettyJson(voList);
            response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new BizException("JSON导出失败: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
```

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/siact/module/device/service/DeviceMappingService.java src/main/java/com/siact/module/device/service/impl/DeviceMappingServiceImpl.java
git commit -m "feat(device): 添加设备点位导出功能(Excel/CSV/JSON)"
```

---

### Task 9: Controller 导入导出接口

**Files:**
- Modify: `src/main/java/com/siact/module/device/controller/DeviceMappingController.java`

- [ ] **Step 1: 在 Controller 中添加导入导出接口**

在 `DeviceMappingController` 中添加：

添加导入：

```java
import com.siact.module.device.vo.DeviceImportResult;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import com.siact.common.annotation.NoResponseAdvice;
```

添加方法：

```java
    @ApiOperation("导入设备点位")
    @PostMapping("/import")
    public DeviceImportResult importData(@RequestParam("file") MultipartFile file) {
        return service.importData(file);
    }

    @NoResponseAdvice
    @ApiOperation("导出设备点位")
    @GetMapping("/export")
    public void exportData(DeviceMappingQuery query, @RequestParam(defaultValue = "excel") String format,
                           HttpServletResponse response) {
        service.exportData(query, format, response);
    }
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/siact/module/device/controller/DeviceMappingController.java
git commit -m "feat(device): 添加设备点位导入导出接口"
```

---

### Task 10: 编译验证

- [ ] **Step 1: 执行 Maven 编译**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: 修复编译错误（如有）**

根据编译输出修复所有错误，确保代码可以正常编译。

- [ ] **Step 3: 提交修复（如有）**

```bash
git add -A
git commit -m "fix(device): 修复编译问题"
```