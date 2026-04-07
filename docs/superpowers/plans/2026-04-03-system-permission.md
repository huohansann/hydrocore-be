# System 权限管理体系实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 system 模块中实现完整的 RBAC 权限管理体系，包含菜单、角色、用户、组织管理功能。

**Architecture:** 采用经典 RBAC 模型，用户通过角色关联菜单权限。所有数据访问通过 Repository 层封装，Service 层处理业务逻辑，Controller 层暴露 REST API。表名使用 `_new` 后缀过渡，验证后迁移替换旧表。

**Tech Stack:** Spring Boot 2.6.13, MyBatis-Plus 3.4.3.1, MapStruct 1.5.3, MySQL 8.0, Lombok, Hutool

**关键约定：**
- Entity 不继承 BaseEntity（BaseEntity 用 AUTO 主键，新模块用 ASSIGN_ID 雪花 ID）
- 字段 `createBy`/`createTime`/`updateBy`/`updateTime`/`deleted` 在每个 Entity 中直接定义，使用 MyBatis-Plus FieldFill 自动填充
- 使用 `@RequiredArgsConstructor` 构造器注入
- 接口直接返回对象，不使用 `R<T>` 包装
- MapStruct 做对象转换，componentModel = "spring"
- Repository 接口 + Impl 实现类
- Service 继承 `IService<Entity>` / `ServiceImpl<Mapper, Entity>`
- 分页使用 `Page.of(page, pageSize)`，返回 `PageVO<T>`
- 所有包路径基于 `com.siact.module.system`

---

### Task 1: 数据库迁移脚本

**Files:**
- Create: `src/main/resources/db/system_permission_tables.sql`

- [ ] **Step 1: 编写建表 SQL**

```sql
-- =============================================
-- System 模块权限管理表（_new 后缀，过渡用）
-- =============================================

-- 菜单表
CREATE TABLE IF NOT EXISTS `sys_menu_new` (
    `id`          BIGINT       NOT NULL COMMENT '雪花ID',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父级菜单ID，顶级为0',
    `menu_name`   VARCHAR(50)  NOT NULL COMMENT '菜单名称',
    `menu_code`   VARCHAR(100)          DEFAULT NULL COMMENT '菜单编码，权限标识',
    `path`        VARCHAR(200)          DEFAULT NULL COMMENT '前端路由地址',
    `icon`        VARCHAR(100)          DEFAULT NULL COMMENT '菜单图标',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
    `type`        TINYINT      NOT NULL DEFAULT 1 COMMENT '类型：1=目录，2=菜单',
    `visible`     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否显示',
    `status`      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=停用',
    `create_by`   VARCHAR(50)           DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME              DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(50)           DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME              DEFAULT NULL COMMENT '更新时间',
    `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_menu_code` (`menu_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role_new` (
    `id`          BIGINT       NOT NULL COMMENT '雪花ID',
    `role_name`   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    `role_code`   VARCHAR(100) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200)          DEFAULT NULL COMMENT '角色描述',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
    `status`      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=停用',
    `create_by`   VARCHAR(50)           DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME              DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(50)           DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME              DEFAULT NULL COMMENT '更新时间',
    `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user_new` (
    `id`          BIGINT       NOT NULL COMMENT '雪花ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名，登录账号',
    `password`    VARCHAR(200) NOT NULL COMMENT '密码，BCrypt加密',
    `nickname`    VARCHAR(50)           DEFAULT NULL COMMENT '用户昵称',
    `email`       VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    `phone`       VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    `avatar`      VARCHAR(200)          DEFAULT NULL COMMENT '头像URL',
    `org_id`      BIGINT                DEFAULT NULL COMMENT '所属组织ID',
    `status`      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=停用',
    `create_by`   VARCHAR(50)           DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME              DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(50)           DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME              DEFAULT NULL COMMENT '更新时间',
    `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_username` (`username`),
    INDEX `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 组织表
CREATE TABLE IF NOT EXISTS `sys_organization_new` (
    `id`          BIGINT       NOT NULL COMMENT '雪花ID',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父组织ID，顶级为0',
    `org_name`    VARCHAR(50)  NOT NULL COMMENT '组织名称',
    `org_code`    VARCHAR(100) NOT NULL COMMENT '组织编码',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
    `status`      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=停用',
    `create_by`   VARCHAR(50)           DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME              DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(50)           DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME              DEFAULT NULL COMMENT '更新时间',
    `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_org_code` (`org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统组织表';

-- 角色-菜单关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu_new` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id`     BIGINT   NOT NULL COMMENT '角色ID',
    `menu_id`     BIGINT   NOT NULL COMMENT '菜单ID',
    `create_time` DATETIME          DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_menu_id` (`menu_id`),
    UNIQUE INDEX `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role_new` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `role_id`     BIGINT   NOT NULL COMMENT '角色ID',
    `create_time` DATETIME          DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_role_id` (`role_id`),
    UNIQUE INDEX `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/db/system_permission_tables.sql
git commit -m "feat(system): 添加权限管理表 SQL 迁移脚本"
```

---

### Task 2: MenuTypeEnum 枚举

**Files:**
- Create: `src/main/java/com/siact/module/system/enums/MenuTypeEnum.java`

- [ ] **Step 1: 创建 MenuTypeEnum**

```java
package com.siact.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MenuTypeEnum {

    DIRECTORY(1, "目录"),
    MENU(2, "菜单");

    private final int code;
    private final String description;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/siact/module/system/enums/MenuTypeEnum.java
git commit -m "feat(system): 添加 MenuTypeEnum 菜单类型枚举"
```

---

### Task 3: 重构 SysMenuEntity

**Files:**
- Modify: `src/main/java/com/siact/module/system/entity/SysMenuEntity.java`

- [ ] **Step 1: 重写 SysMenuEntity**

按设计文档重构字段：添加 `menuName`、`menuCode`、`path`、`icon`、`type`、`visible`、`status`、`deleted`；移除 `label`、`code`、`isShow`、`disabled`、`target`、`remark`。

```java
package com.siact.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_menu_new")
public class SysMenuEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long parentId;

    private String menuName;

    private String menuCode;

    private String path;

    private String icon;

    private Integer sort;

    private Integer type;

    private Boolean visible;

    private Boolean status;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    private Boolean deleted;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/siact/module/system/entity/SysMenuEntity.java
git commit -m "refactor(system): 重构 SysMenuEntity 字段匹配新设计"
```

---

### Task 4: 菜单模块 DTO/VO/Command/Query 重构

**Files:**
- Modify: `src/main/java/com/siact/module/system/dto/SysMenuQueryDTO.java`
- Modify: `src/main/java/com/siact/module/system/vo/SysMenuVO.java`
- Modify: `src/main/java/com/siact/module/system/vo/SysMenuTreeVO.java`
- Modify: `src/main/java/com/siact/module/system/command/SysMenuCreateCommand.java`
- Create: `src/main/java/com/siact/module/system/command/SysMenuUpdateCommand.java`
- Modify: `src/main/java/com/siact/module/system/query/SysMenuQuery.java`
- Delete: `src/main/java/com/siact/module/system/dto/SysMenuDTO.java`（不再需要）
- Delete: `src/main/java/com/siact/module/system/enums/MenuDeleteType.java`（不再使用策略删除）
- Delete: `src/main/java/com/siact/module/system/command/SysMenuDeleteCommand.java`（改为按 ID 删除）

- [ ] **Step 1: 重写 SysMenuQueryDTO**

```java
package com.siact.module.system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysMenuQueryDTO {
    private String menuName;
    private Integer status;
}
```

- [ ] **Step 2: 重写 SysMenuVO**

```java
package com.siact.module.system.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysMenuVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String menuName;
    private String menuCode;
    private String path;
    private String icon;
    private Integer sort;
    private Integer type;
    private Boolean visible;
    private Boolean status;
}
```

- [ ] **Step 3: 重写 SysMenuTreeVO**

```java
package com.siact.module.system.vo;

import lombok.Data;

import java.util.List;

@Data
public class SysMenuTreeVO {
    private Long id;
    private Long parentId;
    private String menuName;
    private String menuCode;
    private String path;
    private String icon;
    private Integer sort;
    private Integer type;
    private Boolean visible;
    private Boolean status;
    private List<SysMenuTreeVO> children;
}
```

- [ ] **Step 4: 重写 SysMenuCreateCommand**

```java
package com.siact.module.system.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SysMenuCreateCommand {

    private Long parentId = 0L;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    private String menuCode;

    @NotBlank(message = "路由地址不能为空")
    private String path;

    private String icon;

    private Integer sort = 0;

    @NotNull(message = "菜单类型不能为空")
    private Integer type;

    private Boolean visible = true;
}
```

- [ ] **Step 5: 创建 SysMenuUpdateCommand**

```java
package com.siact.module.system.command;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SysMenuUpdateCommand {

    @NotNull(message = "菜单ID不能为空")
    private Long id;

    private Long parentId;

    private String menuName;

    private String menuCode;

    private String path;

    private String icon;

    private Integer sort;

    private Integer type;

    private Boolean visible;

    private Boolean status;
}
```

- [ ] **Step 6: 重写 SysMenuQuery**

```java
package com.siact.module.system.query;

import com.siact.common.query.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysMenuQuery extends PageQuery {
    private String menuName;
    private Integer status;
}
```

- [ ] **Step 7: 删除不再使用的文件**

```bash
rm src/main/java/com/siact/module/system/dto/SysMenuDTO.java
rm src/main/java/com/siact/module/system/enums/MenuDeleteType.java
rm src/main/java/com/siact/module/system/command/SysMenuDeleteCommand.java
```

- [ ] **Step 8: Commit**

```bash
git add -A src/main/java/com/siact/module/system/dto/ src/main/java/com/siact/module/system/vo/ src/main/java/com/siact/module/system/command/ src/main/java/com/siact/module/system/query/ src/main/java/com/siact/module/system/enums/
git commit -m "refactor(system): 重构菜单模块 DTO/VO/Command/Query"
```

---

### Task 5: 菜单 Mapper + Repository 重构

**Files:**
- Modify: `src/main/java/com/siact/module/system/mapper/SysMenuMapper.java`
- Modify: `src/main/java/com/siact/module/system/repository/SysMenuRepository.java`
- Modify: `src/main/java/com/siact/module/system/repository/impl/SysMenuRepositoryImpl.java`

- [ ] **Step 1: 重写 SysMenuMapper**

```java
package com.siact.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.system.entity.SysMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuEntity> {

    @Select("SELECT id, parent_id, menu_name, menu_code, path, icon, sort, type, visible, status FROM sys_menu_new WHERE deleted = 0 ORDER BY sort")
    List<SysMenuEntity> queryAllForTree();
}
```

- [ ] **Step 2: 重写 SysMenuRepository**

```java
package com.siact.module.system.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysMenuQueryDTO;
import com.siact.module.system.entity.SysMenuEntity;

import java.util.List;

public interface SysMenuRepository {
    Page<SysMenuEntity> queryList(SysMenuQueryDTO queryDTO, Page<SysMenuEntity> page);

    List<SysMenuEntity> queryAllForTree();

    List<SysMenuEntity> queryByParentId(Long parentId);

    List<SysMenuEntity> queryByIds(List<Long> ids);
}
```

- [ ] **Step 3: 重写 SysMenuRepositoryImpl**

```java
package com.siact.module.system.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysMenuQueryDTO;
import com.siact.module.system.entity.SysMenuEntity;
import com.siact.module.system.mapper.SysMenuMapper;
import com.siact.module.system.repository.SysMenuRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class SysMenuRepositoryImpl implements SysMenuRepository {
    private final SysMenuMapper mapper;

    @Override
    public Page<SysMenuEntity> queryList(SysMenuQueryDTO queryDTO, Page<SysMenuEntity> page) {
        return mapper.selectPage(page, buildWrapper(queryDTO));
    }

    @Override
    public List<SysMenuEntity> queryAllForTree() {
        return mapper.queryAllForTree();
    }

    @Override
    public List<SysMenuEntity> queryByParentId(Long parentId) {
        return mapper.selectList(Wrappers.<SysMenuEntity>lambdaQuery()
                .eq(SysMenuEntity::getParentId, parentId));
    }

    @Override
    public List<SysMenuEntity> queryByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return mapper.selectList(Wrappers.<SysMenuEntity>lambdaQuery()
                .in(SysMenuEntity::getId, ids));
    }

    private LambdaQueryWrapper<SysMenuEntity> buildWrapper(SysMenuQueryDTO queryDTO) {
        return Wrappers.<SysMenuEntity>lambdaQuery()
                .like(StringUtils.isNotBlank(queryDTO.getMenuName()), SysMenuEntity::getMenuName, queryDTO.getMenuName())
                .eq(queryDTO.getStatus() != null, SysMenuEntity::getStatus, queryDTO.getStatus())
                .orderByAsc(SysMenuEntity::getSort);
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/siact/module/system/mapper/SysMenuMapper.java src/main/java/com/siact/module/system/repository/SysMenuRepository.java src/main/java/com/siact/module/system/repository/impl/SysMenuRepositoryImpl.java
git commit -m "refactor(system): 重构菜单 Mapper 和 Repository"
```

---

### Task 6: 菜单 Convert 重构

**Files:**
- Modify: `src/main/java/com/siact/module/system/convert/SysMenuConvert.java`

- [ ] **Step 1: 重写 SysMenuConvert**

```java
package com.siact.module.system.convert;

import com.siact.module.system.command.SysMenuCreateCommand;
import com.siact.module.system.command.SysMenuUpdateCommand;
import com.siact.module.system.dto.SysMenuQueryDTO;
import com.siact.module.system.entity.SysMenuEntity;
import com.siact.module.system.query.SysMenuQuery;
import com.siact.module.system.vo.SysMenuTreeVO;
import com.siact.module.system.vo.SysMenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SysMenuConvert {

    SysMenuQueryDTO toQueryDTO(SysMenuQuery query);

    SysMenuVO toVO(SysMenuEntity entity);

    List<SysMenuVO> toVOList(List<SysMenuEntity> entities);

    SysMenuTreeVO toTreeVO(SysMenuEntity entity);

    SysMenuEntity toEntity(SysMenuCreateCommand command);

    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SysMenuEntity toEntity(SysMenuUpdateCommand command);
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/siact/module/system/convert/SysMenuConvert.java
git commit -m "refactor(system): 重构 SysMenuConvert 适配新字段"
```

---

### Task 7: 菜单 Service + Controller 重构

**Files:**
- Modify: `src/main/java/com/siact/module/system/service/SysMenuService.java`
- Modify: `src/main/java/com/siact/module/system/service/impl/SysMenuServiceImpl.java`
- Modify: `src/main/java/com/siact/module/system/controller/SysMenuController.java`

- [ ] **Step 1: 重写 SysMenuService 接口**

```java
package com.siact.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysMenuCreateCommand;
import com.siact.module.system.command.SysMenuUpdateCommand;
import com.siact.module.system.entity.SysMenuEntity;
import com.siact.module.system.query.SysMenuQuery;
import com.siact.module.system.vo.SysMenuTreeVO;
import com.siact.module.system.vo.SysMenuVO;

import java.util.List;

public interface SysMenuService extends IService<SysMenuEntity> {
    PageVO<SysMenuVO> list(SysMenuQuery query);

    List<SysMenuTreeVO> tree();

    Boolean create(SysMenuCreateCommand command);

    Boolean update(SysMenuUpdateCommand command);

    Boolean delete(Long id);
}
```

- [ ] **Step 2: 重写 SysMenuServiceImpl**

```java
package com.siact.module.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysMenuCreateCommand;
import com.siact.module.system.command.SysMenuUpdateCommand;
import com.siact.module.system.convert.SysMenuConvert;
import com.siact.module.system.dto.SysMenuQueryDTO;
import com.siact.module.system.entity.SysMenuEntity;
import com.siact.module.system.mapper.SysMenuMapper;
import com.siact.module.system.query.SysMenuQuery;
import com.siact.module.system.repository.SysMenuRepository;
import com.siact.module.system.service.SysMenuService;
import com.siact.module.system.vo.SysMenuTreeVO;
import com.siact.module.system.vo.SysMenuVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuService {
    private final SysMenuConvert convert;
    private final SysMenuRepository repository;

    @Override
    public PageVO<SysMenuVO> list(SysMenuQuery query) {
        SysMenuQueryDTO queryDTO = convert.toQueryDTO(query);
        Page<SysMenuEntity> page = repository.queryList(queryDTO, Page.of(query.getPage(), query.getPageSize()));
        List<SysMenuVO> voList = convert.toVOList(page.getRecords());

        return PageVO.<SysMenuVO>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(voList)
                .build();
    }

    @Override
    public List<SysMenuTreeVO> tree() {
        List<SysMenuEntity> allMenus = repository.queryAllForTree();

        Map<Long, SysMenuTreeVO> treeMap = allMenus.stream()
                .map(convert::toTreeVO)
                .collect(Collectors.toMap(SysMenuTreeVO::getId, Function.identity()));

        List<SysMenuTreeVO> roots = new ArrayList<>();
        List<SysMenuTreeVO> sorted = treeMap.values().stream()
                .sorted(Comparator.comparingLong(SysMenuTreeVO::getParentId)
                        .thenComparingInt(SysMenuTreeVO::getSort))
                .collect(Collectors.toList());

        for (SysMenuTreeVO node : sorted) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0L || !treeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                SysMenuTreeVO parent = treeMap.get(parentId);
                if (CollectionUtils.isEmpty(parent.getChildren())) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    public Boolean create(SysMenuCreateCommand command) {
        SysMenuEntity entity = convert.toEntity(command);
        return this.save(entity);
    }

    @Override
    public Boolean update(SysMenuUpdateCommand command) {
        SysMenuEntity entity = convert.toEntity(command);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        // 有子菜单则禁止删除
        List<SysMenuEntity> children = repository.queryByParentId(id);
        if (CollectionUtils.isNotEmpty(children)) {
            throw new RuntimeException("存在子菜单，无法删除");
        }
        return this.removeById(id);
    }
}
```

- [ ] **Step 3: 重写 SysMenuController**

```java
package com.siact.module.system.controller;

import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysMenuCreateCommand;
import com.siact.module.system.command.SysMenuUpdateCommand;
import com.siact.module.system.query.SysMenuQuery;
import com.siact.module.system.service.SysMenuService;
import com.siact.module.system.vo.SysMenuTreeVO;
import com.siact.module.system.vo.SysMenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sysmenu")
public class SysMenuController {
    private final SysMenuService service;

    @PostMapping("/list")
    public PageVO<SysMenuVO> list(@RequestBody SysMenuQuery query) {
        return service.list(query);
    }

    @GetMapping("/tree")
    public List<SysMenuTreeVO> tree() {
        return service.tree();
    }

    @PostMapping
    public Boolean create(@RequestBody SysMenuCreateCommand command) {
        return service.create(command);
    }

    @PutMapping
    public Boolean update(@RequestBody SysMenuUpdateCommand command) {
        return service.update(command);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/siact/module/system/service/SysMenuService.java src/main/java/com/siact/module/system/service/impl/SysMenuServiceImpl.java src/main/java/com/siact/module/system/controller/SysMenuController.java
git commit -m "refactor(system): 重构菜单 Service 和 Controller"
```

---

### Task 8: 角色模块 Entity + Mapper

**Files:**
- Create: `src/main/java/com/siact/module/system/entity/SysRoleEntity.java`
- Create: `src/main/java/com/siact/module/system/mapper/SysRoleMapper.java`

- [ ] **Step 1: 创建 SysRoleEntity**

```java
package com.siact.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_role_new")
public class SysRoleEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String roleName;

    private String roleCode;

    private String description;

    private Integer sort;

    private Boolean status;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    private Boolean deleted;
}
```

- [ ] **Step 2: 创建 SysRoleMapper**

```java
package com.siact.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.system.entity.SysRoleEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleEntity> {
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/module/system/entity/SysRoleEntity.java src/main/java/com/siact/module/system/mapper/SysRoleMapper.java
git commit -m "feat(system): 添加角色 Entity 和 Mapper"
```

---

### Task 9: 角色模块 DTO/VO/Command/Query/Convert

**Files:**
- Create: `src/main/java/com/siact/module/system/dto/SysRoleQueryDTO.java`
- Create: `src/main/java/com/siact/module/system/vo/SysRoleVO.java`
- Create: `src/main/java/com/siact/module/system/command/SysRoleCreateCommand.java`
- Create: `src/main/java/com/siact/module/system/command/SysRoleUpdateCommand.java`
- Create: `src/main/java/com/siact/module/system/query/SysRoleQuery.java`
- Create: `src/main/java/com/siact/module/system/convert/SysRoleConvert.java`

- [ ] **Step 1: 创建 SysRoleQueryDTO**

```java
package com.siact.module.system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysRoleQueryDTO {
    private String roleName;
    private Integer status;
}
```

- [ ] **Step 2: 创建 SysRoleVO**

```java
package com.siact.module.system.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysRoleVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer sort;
    private Boolean status;
}
```

- [ ] **Step 3: 创建 SysRoleCreateCommand**

```java
package com.siact.module.system.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SysRoleCreateCommand {

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    private String description;

    private Integer sort = 0;
}
```

- [ ] **Step 4: 创建 SysRoleUpdateCommand**

```java
package com.siact.module.system.command;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SysRoleUpdateCommand {

    @NotNull(message = "角色ID不能为空")
    private Long id;

    private String roleName;

    private String roleCode;

    private String description;

    private Integer sort;

    private Boolean status;
}
```

- [ ] **Step 5: 创建 SysRoleQuery**

```java
package com.siact.module.system.query;

import com.siact.common.query.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysRoleQuery extends PageQuery {
    private String roleName;
    private Integer status;
}
```

- [ ] **Step 6: 创建 SysRoleConvert**

```java
package com.siact.module.system.convert;

import com.siact.module.system.command.SysRoleCreateCommand;
import com.siact.module.system.command.SysRoleUpdateCommand;
import com.siact.module.system.dto.SysRoleQueryDTO;
import com.siact.module.system.entity.SysRoleEntity;
import com.siact.module.system.query.SysRoleQuery;
import com.siact.module.system.vo.SysRoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SysRoleConvert {

    SysRoleQueryDTO toQueryDTO(SysRoleQuery query);

    SysRoleVO toVO(SysRoleEntity entity);

    List<SysRoleVO> toVOList(List<SysRoleEntity> entities);

    SysRoleEntity toEntity(SysRoleCreateCommand command);

    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SysRoleEntity toEntity(SysRoleUpdateCommand command);
}
```

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/siact/module/system/dto/SysRoleQueryDTO.java src/main/java/com/siact/module/system/vo/SysRoleVO.java src/main/java/com/siact/module/system/command/SysRoleCreateCommand.java src/main/java/com/siact/module/system/command/SysRoleUpdateCommand.java src/main/java/com/siact/module/system/query/SysRoleQuery.java src/main/java/com/siact/module/system/convert/SysRoleConvert.java
git commit -m "feat(system): 添加角色模块 DTO/VO/Command/Query/Convert"
```

---

### Task 10: 角色模块 Repository + Service + Controller

**Files:**
- Create: `src/main/java/com/siact/module/system/repository/SysRoleRepository.java`
- Create: `src/main/java/com/siact/module/system/repository/impl/SysRoleRepositoryImpl.java`
- Create: `src/main/java/com/siact/module/system/service/SysRoleService.java`
- Create: `src/main/java/com/siact/module/system/service/impl/SysRoleServiceImpl.java`
- Create: `src/main/java/com/siact/module/system/controller/SysRoleController.java`

- [ ] **Step 1: 创建 SysRoleRepository**

```java
package com.siact.module.system.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysRoleQueryDTO;
import com.siact.module.system.entity.SysRoleEntity;

public interface SysRoleRepository {
    Page<SysRoleEntity> queryList(SysRoleQueryDTO queryDTO, Page<SysRoleEntity> page);

    boolean existsByRoleCode(String roleCode);

    boolean existsByRoleCodeExcludeId(String roleCode, Long excludeId);
}
```

- [ ] **Step 2: 创建 SysRoleRepositoryImpl**

```java
package com.siact.module.system.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysRoleQueryDTO;
import com.siact.module.system.entity.SysRoleEntity;
import com.siact.module.system.mapper.SysRoleMapper;
import com.siact.module.system.repository.SysRoleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class SysRoleRepositoryImpl implements SysRoleRepository {
    private final SysRoleMapper mapper;

    @Override
    public Page<SysRoleEntity> queryList(SysRoleQueryDTO queryDTO, Page<SysRoleEntity> page) {
        return mapper.selectPage(page, Wrappers.<SysRoleEntity>lambdaQuery()
                .like(StringUtils.isNotBlank(queryDTO.getRoleName()), SysRoleEntity::getRoleName, queryDTO.getRoleName())
                .eq(queryDTO.getStatus() != null, SysRoleEntity::getStatus, queryDTO.getStatus())
                .orderByAsc(SysRoleEntity::getSort));
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        return mapper.selectCount(Wrappers.<SysRoleEntity>lambdaQuery()
                .eq(SysRoleEntity::getRoleCode, roleCode)) > 0;
    }

    @Override
    public boolean existsByRoleCodeExcludeId(String roleCode, Long excludeId) {
        return mapper.selectCount(Wrappers.<SysRoleEntity>lambdaQuery()
                .eq(SysRoleEntity::getRoleCode, roleCode)
                .ne(SysRoleEntity::getId, excludeId)) > 0;
    }
}
```

- [ ] **Step 3: 创建 SysRoleService**

```java
package com.siact.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysRoleCreateCommand;
import com.siact.module.system.command.SysRoleUpdateCommand;
import com.siact.module.system.entity.SysRoleEntity;
import com.siact.module.system.query.SysRoleQuery;
import com.siact.module.system.vo.SysRoleVO;

import java.util.List;

public interface SysRoleService extends IService<SysRoleEntity> {
    PageVO<SysRoleVO> list(SysRoleQuery query);

    Boolean create(SysRoleCreateCommand command);

    Boolean update(SysRoleUpdateCommand command);

    Boolean delete(Long id);

    void assignMenus(Long roleId, List<Long> menuIds);

    List<Long> getMenuIds(Long roleId);
}
```

- [ ] **Step 4: 创建 SysRoleServiceImpl**

```java
package com.siact.module.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysRoleCreateCommand;
import com.siact.module.system.command.SysRoleUpdateCommand;
import com.siact.module.system.convert.SysRoleConvert;
import com.siact.module.system.dto.SysRoleQueryDTO;
import com.siact.module.system.entity.SysRoleEntity;
import com.siact.module.system.entity.SysRoleMenuEntity;
import com.siact.module.system.mapper.SysRoleMapper;
import com.siact.module.system.mapper.SysRoleMenuMapper;
import com.siact.module.system.query.SysRoleQuery;
import com.siact.module.system.repository.SysRoleRepository;
import com.siact.module.system.service.SysRoleService;
import com.siact.module.system.vo.SysRoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {
    private final SysRoleConvert convert;
    private final SysRoleRepository repository;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public PageVO<SysRoleVO> list(SysRoleQuery query) {
        SysRoleQueryDTO queryDTO = convert.toQueryDTO(query);
        Page<SysRoleEntity> page = repository.queryList(queryDTO, Page.of(query.getPage(), query.getPageSize()));
        List<SysRoleVO> voList = convert.toVOList(page.getRecords());

        return PageVO.<SysRoleVO>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(voList)
                .build();
    }

    @Override
    public Boolean create(SysRoleCreateCommand command) {
        if (repository.existsByRoleCode(command.getRoleCode())) {
            throw new RuntimeException("角色编码已存在");
        }
        SysRoleEntity entity = convert.toEntity(command);
        return this.save(entity);
    }

    @Override
    public Boolean update(SysRoleUpdateCommand command) {
        if (repository.existsByRoleCodeExcludeId(command.getRoleCode(), command.getId())) {
            throw new RuntimeException("角色编码已存在");
        }
        SysRoleEntity entity = convert.toEntity(command);
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        // 删除角色-菜单关联
        roleMenuMapper.deleteByRoleId(id);
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            roleMenuMapper.batchInsert(roleId, menuIds);
        }
    }

    @Override
    public List<Long> getMenuIds(Long roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }
}
```

注意：此步骤依赖 `SysRoleMenuMapper`（Task 12），如果执行顺序问题，可先创建 Mapper 占位。

- [ ] **Step 5: 创建 SysRoleController**

```java
package com.siact.module.system.controller;

import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysRoleCreateCommand;
import com.siact.module.system.command.SysRoleUpdateCommand;
import com.siact.module.system.query.SysRoleQuery;
import com.siact.module.system.service.SysRoleService;
import com.siact.module.system.vo.SysRoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sysrole")
public class SysRoleController {
    private final SysRoleService service;

    @PostMapping("/list")
    public PageVO<SysRoleVO> list(@RequestBody SysRoleQuery query) {
        return service.list(query);
    }

    @PostMapping
    public Boolean create(@RequestBody SysRoleCreateCommand command) {
        return service.create(command);
    }

    @PutMapping
    public Boolean update(@RequestBody SysRoleUpdateCommand command) {
        return service.update(command);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return service.delete(id);
    }

    @PutMapping("/{id}/menus")
    public Boolean assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        service.assignMenus(id, menuIds);
        return true;
    }

    @GetMapping("/{id}/menus")
    public List<Long> getMenuIds(@PathVariable Long id) {
        return service.getMenuIds(id);
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/siact/module/system/repository/SysRoleRepository.java src/main/java/com/siact/module/system/repository/impl/SysRoleRepositoryImpl.java src/main/java/com/siact/module/system/service/SysRoleService.java src/main/java/com/siact/module/system/service/impl/SysRoleServiceImpl.java src/main/java/com/siact/module/system/controller/SysRoleController.java
git commit -m "feat(system): 添加角色 Repository/Service/Controller"
```

---

### Task 11: 用户模块 Entity + Mapper

**Files:**
- Create: `src/main/java/com/siact/module/system/entity/SysUserEntity.java`
- Create: `src/main/java/com/siact/module/system/mapper/SysUserMapper.java`

- [ ] **Step 1: 创建 SysUserEntity**

```java
package com.siact.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_user_new")
public class SysUserEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Long orgId;

    private Boolean status;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    private Boolean deleted;
}
```

- [ ] **Step 2: 创建 SysUserMapper**

```java
package com.siact.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.system.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/module/system/entity/SysUserEntity.java src/main/java/com/siact/module/system/mapper/SysUserMapper.java
git commit -m "feat(system): 添加用户 Entity 和 Mapper"
```

---

### Task 12: 用户模块 DTO/VO/Command/Query/Convert

**Files:**
- Create: `src/main/java/com/siact/module/system/dto/SysUserQueryDTO.java`
- Create: `src/main/java/com/siact/module/system/vo/SysUserVO.java`
- Create: `src/main/java/com/siact/module/system/command/SysUserCreateCommand.java`
- Create: `src/main/java/com/siact/module/system/command/SysUserUpdateCommand.java`
- Create: `src/main/java/com/siact/module/system/command/AssignUserRoleCommand.java`
- Create: `src/main/java/com/siact/module/system/command/ResetPasswordCommand.java`
- Create: `src/main/java/com/siact/module/system/query/SysUserQuery.java`
- Create: `src/main/java/com/siact/module/system/convert/SysUserConvert.java`

- [ ] **Step 1: 创建 SysUserQueryDTO**

```java
package com.siact.module.system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysUserQueryDTO {
    private String username;
    private String nickname;
    private Long orgId;
    private Integer status;
}
```

- [ ] **Step 2: 创建 SysUserVO**

```java
package com.siact.module.system.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Long orgId;
    private String orgName;
    private Boolean status;
}
```

- [ ] **Step 3: 创建 SysUserCreateCommand**

```java
package com.siact.module.system.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SysUserCreateCommand {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Long orgId;
}
```

- [ ] **Step 4: 创建 SysUserUpdateCommand**

```java
package com.siact.module.system.command;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SysUserUpdateCommand {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Long orgId;

    private Boolean status;
}
```

- [ ] **Step 5: 创建 AssignUserRoleCommand**

```java
package com.siact.module.system.command;

import lombok.Data;

import java.util.List;

@Data
public class AssignUserRoleCommand {
    private List<Long> roleIds;
}
```

- [ ] **Step 6: 创建 ResetPasswordCommand**

```java
package com.siact.module.system.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ResetPasswordCommand {

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
```

- [ ] **Step 7: 创建 SysUserQuery**

```java
package com.siact.module.system.query;

import com.siact.common.query.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysUserQuery extends PageQuery {
    private String username;
    private String nickname;
    private Long orgId;
    private Integer status;
}
```

- [ ] **Step 8: 创建 SysUserConvert**

```java
package com.siact.module.system.convert;

import com.siact.module.system.command.SysUserCreateCommand;
import com.siact.module.system.command.SysUserUpdateCommand;
import com.siact.module.system.dto.SysUserQueryDTO;
import com.siact.module.system.entity.SysUserEntity;
import com.siact.module.system.query.SysUserQuery;
import com.siact.module.system.vo.SysUserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SysUserConvert {

    SysUserQueryDTO toQueryDTO(SysUserQuery query);

    @Mapping(target = "password", ignore = true)
    SysUserVO toVO(SysUserEntity entity);

    List<SysUserVO> toVOList(List<SysUserEntity> entities);

    SysUserEntity toEntity(SysUserCreateCommand command);

    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SysUserEntity toEntity(SysUserUpdateCommand command);
}
```

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/siact/module/system/dto/SysUserQueryDTO.java src/main/java/com/siact/module/system/vo/SysUserVO.java src/main/java/com/siact/module/system/command/SysUserCreateCommand.java src/main/java/com/siact/module/system/command/SysUserUpdateCommand.java src/main/java/com/siact/module/system/command/AssignUserRoleCommand.java src/main/java/com/siact/module/system/command/ResetPasswordCommand.java src/main/java/com/siact/module/system/query/SysUserQuery.java src/main/java/com/siact/module/system/convert/SysUserConvert.java
git commit -m "feat(system): 添加用户模块 DTO/VO/Command/Query/Convert"
```

---

### Task 13: 用户模块 Repository + Service + Controller

**Files:**
- Create: `src/main/java/com/siact/module/system/repository/SysUserRepository.java`
- Create: `src/main/java/com/siact/module/system/repository/impl/SysUserRepositoryImpl.java`
- Create: `src/main/java/com/siact/module/system/service/SysUserService.java`
- Create: `src/main/java/com/siact/module/system/service/impl/SysUserServiceImpl.java`
- Create: `src/main/java/com/siact/module/system/controller/SysUserController.java`

- [ ] **Step 1: 创建 SysUserRepository**

```java
package com.siact.module.system.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysUserQueryDTO;
import com.siact.module.system.entity.SysUserEntity;

public interface SysUserRepository {
    Page<SysUserEntity> queryList(SysUserQueryDTO queryDTO, Page<SysUserEntity> page);

    boolean existsByUsername(String username);
}
```

- [ ] **Step 2: 创建 SysUserRepositoryImpl**

```java
package com.siact.module.system.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysUserQueryDTO;
import com.siact.module.system.entity.SysUserEntity;
import com.siact.module.system.mapper.SysUserMapper;
import com.siact.module.system.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class SysUserRepositoryImpl implements SysUserRepository {
    private final SysUserMapper mapper;

    @Override
    public Page<SysUserEntity> queryList(SysUserQueryDTO queryDTO, Page<SysUserEntity> page) {
        return mapper.selectPage(page, Wrappers.<SysUserEntity>lambdaQuery()
                .like(StringUtils.isNotBlank(queryDTO.getUsername()), SysUserEntity::getUsername, queryDTO.getUsername())
                .like(StringUtils.isNotBlank(queryDTO.getNickname()), SysUserEntity::getNickname, queryDTO.getNickname())
                .eq(queryDTO.getOrgId() != null, SysUserEntity::getOrgId, queryDTO.getOrgId())
                .eq(queryDTO.getStatus() != null, SysUserEntity::getStatus, queryDTO.getStatus())
                .orderByDesc(SysUserEntity::getCreateTime));
    }

    @Override
    public boolean existsByUsername(String username) {
        return mapper.selectCount(Wrappers.<SysUserEntity>lambdaQuery()
                .eq(SysUserEntity::getUsername, username)) > 0;
    }
}
```

- [ ] **Step 3: 创建 SysUserService**

```java
package com.siact.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.AssignUserRoleCommand;
import com.siact.module.system.command.ResetPasswordCommand;
import com.siact.module.system.command.SysUserCreateCommand;
import com.siact.module.system.command.SysUserUpdateCommand;
import com.siact.module.system.entity.SysUserEntity;
import com.siact.module.system.query.SysUserQuery;
import com.siact.module.system.vo.SysUserVO;

import java.util.List;

public interface SysUserService extends IService<SysUserEntity> {
    PageVO<SysUserVO> list(SysUserQuery query);

    Boolean create(SysUserCreateCommand command);

    Boolean update(SysUserUpdateCommand command);

    Boolean delete(Long id);

    Boolean resetPassword(Long id, ResetPasswordCommand command);

    void assignRoles(Long userId, List<Long> roleIds);

    List<Long> getRoleIds(Long userId);
}
```

- [ ] **Step 4: 创建 SysUserServiceImpl**

```java
package com.siact.module.system.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.AssignUserRoleCommand;
import com.siact.module.system.command.ResetPasswordCommand;
import com.siact.module.system.command.SysUserCreateCommand;
import com.siact.module.system.command.SysUserUpdateCommand;
import com.siact.module.system.convert.SysUserConvert;
import com.siact.module.system.dto.SysUserQueryDTO;
import com.siact.module.system.entity.SysUserEntity;
import com.siact.module.system.mapper.SysUserMapper;
import com.siact.module.system.mapper.SysUserRoleMapper;
import com.siact.module.system.query.SysUserQuery;
import com.siact.module.system.repository.SysUserRepository;
import com.siact.module.system.service.SysUserService;
import com.siact.module.system.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements SysUserService {
    private final SysUserConvert convert;
    private final SysUserRepository repository;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public PageVO<SysUserVO> list(SysUserQuery query) {
        SysUserQueryDTO queryDTO = convert.toQueryDTO(query);
        Page<SysUserEntity> page = repository.queryList(queryDTO, Page.of(query.getPage(), query.getPageSize()));
        List<SysUserVO> voList = convert.toVOList(page.getRecords());

        return PageVO.<SysUserVO>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(voList)
                .build();
    }

    @Override
    public Boolean create(SysUserCreateCommand command) {
        if (repository.existsByUsername(command.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        SysUserEntity entity = convert.toEntity(command);
        entity.setPassword(BCrypt.hashpw(command.getPassword()));
        return this.save(entity);
    }

    @Override
    public Boolean update(SysUserUpdateCommand command) {
        SysUserEntity entity = convert.toEntity(command);
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        userRoleMapper.deleteByUserId(id);
        return this.removeById(id);
    }

    @Override
    public Boolean resetPassword(Long id, ResetPasswordCommand command) {
        SysUserEntity entity = new SysUserEntity();
        entity.setId(id);
        entity.setPassword(BCrypt.hashpw(command.getNewPassword()));
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            userRoleMapper.batchInsert(userId, roleIds);
        }
    }

    @Override
    public List<Long> getRoleIds(Long userId) {
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }
}
```

注意：此步骤依赖 `SysUserRoleMapper`（Task 15），如果执行顺序问题，可先创建 Mapper 占位。

- [ ] **Step 5: 创建 SysUserController**

```java
package com.siact.module.system.controller;

import com.siact.common.vo.PageVO;
import com.siact.module.system.command.AssignUserRoleCommand;
import com.siact.module.system.command.ResetPasswordCommand;
import com.siact.module.system.command.SysUserCreateCommand;
import com.siact.module.system.command.SysUserUpdateCommand;
import com.siact.module.system.query.SysUserQuery;
import com.siact.module.system.service.SysUserService;
import com.siact.module.system.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sysuser")
public class SysUserController {
    private final SysUserService service;

    @PostMapping("/list")
    public PageVO<SysUserVO> list(@RequestBody SysUserQuery query) {
        return service.list(query);
    }

    @PostMapping
    public Boolean create(@RequestBody SysUserCreateCommand command) {
        return service.create(command);
    }

    @PutMapping
    public Boolean update(@RequestBody SysUserUpdateCommand command) {
        return service.update(command);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return service.delete(id);
    }

    @PutMapping("/{id}/reset-password")
    public Boolean resetPassword(@PathVariable Long id, @RequestBody ResetPasswordCommand command) {
        return service.resetPassword(id, command);
    }

    @PutMapping("/{id}/roles")
    public Boolean assignRoles(@PathVariable Long id, @RequestBody AssignUserRoleCommand command) {
        service.assignRoles(id, command.getRoleIds());
        return true;
    }

    @GetMapping("/{id}/roles")
    public List<Long> getRoleIds(@PathVariable Long id) {
        return service.getRoleIds(id);
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/siact/module/system/repository/SysUserRepository.java src/main/java/com/siact/module/system/repository/impl/SysUserRepositoryImpl.java src/main/java/com/siact/module/system/service/SysUserService.java src/main/java/com/siact/module/system/service/impl/SysUserServiceImpl.java src/main/java/com/siact/module/system/controller/SysUserController.java
git commit -m "feat(system): 添加用户 Repository/Service/Controller"
```

---

### Task 14: 组织模块 Entity + Mapper

**Files:**
- Create: `src/main/java/com/siact/module/system/entity/SysOrganizationEntity.java`
- Create: `src/main/java/com/siact/module/system/mapper/SysOrganizationMapper.java`

- [ ] **Step 1: 创建 SysOrganizationEntity**

```java
package com.siact.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_organization_new")
public class SysOrganizationEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long parentId;

    private String orgName;

    private String orgCode;

    private Integer sort;

    private Boolean status;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    private Boolean deleted;
}
```

- [ ] **Step 2: 创建 SysOrganizationMapper**

```java
package com.siact.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.system.entity.SysOrganizationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysOrganizationMapper extends BaseMapper<SysOrganizationEntity> {

    @Select("SELECT id, parent_id, org_name, org_code, sort, status FROM sys_organization_new WHERE deleted = 0 ORDER BY sort")
    List<SysOrganizationEntity> queryAllForTree();
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/siact/module/system/entity/SysOrganizationEntity.java src/main/java/com/siact/module/system/mapper/SysOrganizationMapper.java
git commit -m "feat(system): 添加组织 Entity 和 Mapper"
```

---

### Task 15: 组织模块 DTO/VO/Command/Query/Convert

**Files:**
- Create: `src/main/java/com/siact/module/system/dto/SysOrganizationQueryDTO.java`
- Create: `src/main/java/com/siact/module/system/vo/SysOrganizationVO.java`
- Create: `src/main/java/com/siact/module/system/vo/SysOrganizationTreeVO.java`
- Create: `src/main/java/com/siact/module/system/command/SysOrganizationCreateCommand.java`
- Create: `src/main/java/com/siact/module/system/command/SysOrganizationUpdateCommand.java`
- Create: `src/main/java/com/siact/module/system/query/SysOrganizationQuery.java`
- Create: `src/main/java/com/siact/module/system/convert/SysOrganizationConvert.java`

- [ ] **Step 1: 创建 SysOrganizationQueryDTO**

```java
package com.siact.module.system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysOrganizationQueryDTO {
    private String orgName;
    private Integer status;
}
```

- [ ] **Step 2: 创建 SysOrganizationVO**

```java
package com.siact.module.system.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysOrganizationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String orgName;
    private String orgCode;
    private Integer sort;
    private Boolean status;
}
```

- [ ] **Step 3: 创建 SysOrganizationTreeVO**

```java
package com.siact.module.system.vo;

import lombok.Data;

import java.util.List;

@Data
public class SysOrganizationTreeVO {
    private Long id;
    private Long parentId;
    private String orgName;
    private String orgCode;
    private Integer sort;
    private Boolean status;
    private List<SysOrganizationTreeVO> children;
}
```

- [ ] **Step 4: 创建 SysOrganizationCreateCommand**

```java
package com.siact.module.system.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SysOrganizationCreateCommand {

    private Long parentId = 0L;

    @NotBlank(message = "组织名称不能为空")
    private String orgName;

    @NotBlank(message = "组织编码不能为空")
    private String orgCode;

    private Integer sort = 0;
}
```

- [ ] **Step 5: 创建 SysOrganizationUpdateCommand**

```java
package com.siact.module.system.command;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SysOrganizationUpdateCommand {

    @NotNull(message = "组织ID不能为空")
    private Long id;

    private Long parentId;

    private String orgName;

    private String orgCode;

    private Integer sort;

    private Boolean status;
}
```

- [ ] **Step 6: 创建 SysOrganizationQuery**

```java
package com.siact.module.system.query;

import com.siact.common.query.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysOrganizationQuery extends PageQuery {
    private String orgName;
    private Integer status;
}
```

- [ ] **Step 7: 创建 SysOrganizationConvert**

```java
package com.siact.module.system.convert;

import com.siact.module.system.command.SysOrganizationCreateCommand;
import com.siact.module.system.command.SysOrganizationUpdateCommand;
import com.siact.module.system.dto.SysOrganizationQueryDTO;
import com.siact.module.system.entity.SysOrganizationEntity;
import com.siact.module.system.query.SysOrganizationQuery;
import com.siact.module.system.vo.SysOrganizationTreeVO;
import com.siact.module.system.vo.SysOrganizationVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SysOrganizationConvert {

    SysOrganizationQueryDTO toQueryDTO(SysOrganizationQuery query);

    SysOrganizationVO toVO(SysOrganizationEntity entity);

    List<SysOrganizationVO> toVOList(List<SysOrganizationEntity> entities);

    SysOrganizationTreeVO toTreeVO(SysOrganizationEntity entity);

    SysOrganizationEntity toEntity(SysOrganizationCreateCommand command);

    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SysOrganizationEntity toEntity(SysOrganizationUpdateCommand command);
}
```

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/siact/module/system/dto/SysOrganizationQueryDTO.java src/main/java/com/siact/module/system/vo/SysOrganizationVO.java src/main/java/com/siact/module/system/vo/SysOrganizationTreeVO.java src/main/java/com/siact/module/system/command/SysOrganizationCreateCommand.java src/main/java/com/siact/module/system/command/SysOrganizationUpdateCommand.java src/main/java/com/siact/module/system/query/SysOrganizationQuery.java src/main/java/com/siact/module/system/convert/SysOrganizationConvert.java
git commit -m "feat(system): 添加组织模块 DTO/VO/Command/Query/Convert"
```

---

### Task 16: 组织模块 Repository + Service + Controller

**Files:**
- Create: `src/main/java/com/siact/module/system/repository/SysOrganizationRepository.java`
- Create: `src/main/java/com/siact/module/system/repository/impl/SysOrganizationRepositoryImpl.java`
- Create: `src/main/java/com/siact/module/system/service/SysOrganizationService.java`
- Create: `src/main/java/com/siact/module/system/service/impl/SysOrganizationServiceImpl.java`
- Create: `src/main/java/com/siact/module/system/controller/SysOrganizationController.java`

- [ ] **Step 1: 创建 SysOrganizationRepository**

```java
package com.siact.module.system.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysOrganizationQueryDTO;
import com.siact.module.system.entity.SysOrganizationEntity;

import java.util.List;

public interface SysOrganizationRepository {
    Page<SysOrganizationEntity> queryList(SysOrganizationQueryDTO queryDTO, Page<SysOrganizationEntity> page);

    List<SysOrganizationEntity> queryAllForTree();

    List<SysOrganizationEntity> queryByParentId(Long parentId);

    boolean existsByOrgCode(String orgCode);

    boolean existsByOrgCodeExcludeId(String orgCode, Long excludeId);
}
```

- [ ] **Step 2: 创建 SysOrganizationRepositoryImpl**

```java
package com.siact.module.system.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysOrganizationQueryDTO;
import com.siact.module.system.entity.SysOrganizationEntity;
import com.siact.module.system.mapper.SysOrganizationMapper;
import com.siact.module.system.repository.SysOrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class SysOrganizationRepositoryImpl implements SysOrganizationRepository {
    private final SysOrganizationMapper mapper;

    @Override
    public Page<SysOrganizationEntity> queryList(SysOrganizationQueryDTO queryDTO, Page<SysOrganizationEntity> page) {
        return mapper.selectPage(page, Wrappers.<SysOrganizationEntity>lambdaQuery()
                .like(StringUtils.isNotBlank(queryDTO.getOrgName()), SysOrganizationEntity::getOrgName, queryDTO.getOrgName())
                .eq(queryDTO.getStatus() != null, SysOrganizationEntity::getStatus, queryDTO.getStatus())
                .orderByAsc(SysOrganizationEntity::getSort));
    }

    @Override
    public List<SysOrganizationEntity> queryAllForTree() {
        return mapper.queryAllForTree();
    }

    @Override
    public List<SysOrganizationEntity> queryByParentId(Long parentId) {
        return mapper.selectList(Wrappers.<SysOrganizationEntity>lambdaQuery()
                .eq(SysOrganizationEntity::getParentId, parentId));
    }

    @Override
    public boolean existsByOrgCode(String orgCode) {
        return mapper.selectCount(Wrappers.<SysOrganizationEntity>lambdaQuery()
                .eq(SysOrganizationEntity::getOrgCode, orgCode)) > 0;
    }

    @Override
    public boolean existsByOrgCodeExcludeId(String orgCode, Long excludeId) {
        return mapper.selectCount(Wrappers.<SysOrganizationEntity>lambdaQuery()
                .eq(SysOrganizationEntity::getOrgCode, orgCode)
                .ne(SysOrganizationEntity::getId, excludeId)) > 0;
    }
}
```

- [ ] **Step 3: 创建 SysOrganizationService**

```java
package com.siact.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysOrganizationCreateCommand;
import com.siact.module.system.command.SysOrganizationUpdateCommand;
import com.siact.module.system.entity.SysOrganizationEntity;
import com.siact.module.system.query.SysOrganizationQuery;
import com.siact.module.system.vo.SysOrganizationTreeVO;
import com.siact.module.system.vo.SysOrganizationVO;

import java.util.List;

public interface SysOrganizationService extends IService<SysOrganizationEntity> {
    PageVO<SysOrganizationVO> list(SysOrganizationQuery query);

    List<SysOrganizationTreeVO> tree();

    Boolean create(SysOrganizationCreateCommand command);

    Boolean update(SysOrganizationUpdateCommand command);

    Boolean delete(Long id);
}
```

- [ ] **Step 4: 创建 SysOrganizationServiceImpl**

```java
package com.siact.module.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysOrganizationCreateCommand;
import com.siact.module.system.command.SysOrganizationUpdateCommand;
import com.siact.module.system.convert.SysOrganizationConvert;
import com.siact.module.system.dto.SysOrganizationQueryDTO;
import com.siact.module.system.entity.SysOrganizationEntity;
import com.siact.module.system.mapper.SysOrganizationMapper;
import com.siact.module.system.mapper.SysUserMapper;
import com.siact.module.system.query.SysOrganizationQuery;
import com.siact.module.system.repository.SysOrganizationRepository;
import com.siact.module.system.service.SysOrganizationService;
import com.siact.module.system.vo.SysOrganizationTreeVO;
import com.siact.module.system.vo.SysOrganizationVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SysOrganizationServiceImpl extends ServiceImpl<SysOrganizationMapper, SysOrganizationEntity> implements SysOrganizationService {
    private final SysOrganizationConvert convert;
    private final SysOrganizationRepository repository;
    private final SysUserMapper userMapper;

    @Override
    public PageVO<SysOrganizationVO> list(SysOrganizationQuery query) {
        SysOrganizationQueryDTO queryDTO = convert.toQueryDTO(query);
        Page<SysOrganizationEntity> page = repository.queryList(queryDTO, Page.of(query.getPage(), query.getPageSize()));
        List<SysOrganizationVO> voList = convert.toVOList(page.getRecords());

        return PageVO.<SysOrganizationVO>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(voList)
                .build();
    }

    @Override
    public List<SysOrganizationTreeVO> tree() {
        List<SysOrganizationEntity> allOrgs = repository.queryAllForTree();

        Map<Long, SysOrganizationTreeVO> treeMap = allOrgs.stream()
                .map(convert::toTreeVO)
                .collect(Collectors.toMap(SysOrganizationTreeVO::getId, Function.identity()));

        List<SysOrganizationTreeVO> roots = new ArrayList<>();
        List<SysOrganizationTreeVO> sorted = treeMap.values().stream()
                .sorted(Comparator.comparingLong(SysOrganizationTreeVO::getParentId)
                        .thenComparingInt(SysOrganizationTreeVO::getSort))
                .collect(Collectors.toList());

        for (SysOrganizationTreeVO node : sorted) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0L || !treeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                SysOrganizationTreeVO parent = treeMap.get(parentId);
                if (CollectionUtils.isEmpty(parent.getChildren())) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    public Boolean create(SysOrganizationCreateCommand command) {
        if (repository.existsByOrgCode(command.getOrgCode())) {
            throw new RuntimeException("组织编码已存在");
        }
        SysOrganizationEntity entity = convert.toEntity(command);
        return this.save(entity);
    }

    @Override
    public Boolean update(SysOrganizationUpdateCommand command) {
        if (repository.existsByOrgCodeExcludeId(command.getOrgCode(), command.getId())) {
            throw new RuntimeException("组织编码已存在");
        }
        SysOrganizationEntity entity = convert.toEntity(command);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        // 有子组织则禁止删除
        List<SysOrganizationEntity> children = repository.queryByParentId(id);
        if (CollectionUtils.isNotEmpty(children)) {
            throw new RuntimeException("存在子组织，无法删除");
        }
        // 有关联用户则禁止删除
        long userCount = userMapper.selectCount(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<com.siact.module.system.entity.SysUserEntity>lambdaQuery()
                        .eq(com.siact.module.system.entity.SysUserEntity::getOrgId, id));
        if (userCount > 0) {
            throw new RuntimeException("存在关联用户，无法删除");
        }
        return this.removeById(id);
    }
}
```

- [ ] **Step 5: 创建 SysOrganizationController**

```java
package com.siact.module.system.controller;

import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysOrganizationCreateCommand;
import com.siact.module.system.command.SysOrganizationUpdateCommand;
import com.siact.module.system.query.SysOrganizationQuery;
import com.siact.module.system.service.SysOrganizationService;
import com.siact.module.system.vo.SysOrganizationTreeVO;
import com.siact.module.system.vo.SysOrganizationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sysorg")
public class SysOrganizationController {
    private final SysOrganizationService service;

    @PostMapping("/list")
    public PageVO<SysOrganizationVO> list(@RequestBody SysOrganizationQuery query) {
        return service.list(query);
    }

    @GetMapping("/tree")
    public List<SysOrganizationTreeVO> tree() {
        return service.tree();
    }

    @PostMapping
    public Boolean create(@RequestBody SysOrganizationCreateCommand command) {
        return service.create(command);
    }

    @PutMapping
    public Boolean update(@RequestBody SysOrganizationUpdateCommand command) {
        return service.update(command);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/siact/module/system/repository/SysOrganizationRepository.java src/main/java/com/siact/module/system/repository/impl/SysOrganizationRepositoryImpl.java src/main/java/com/siact/module/system/service/SysOrganizationService.java src/main/java/com/siact/module/system/service/impl/SysOrganizationServiceImpl.java src/main/java/com/siact/module/system/controller/SysOrganizationController.java
git commit -m "feat(system): 添加组织 Repository/Service/Controller"
```

---

### Task 17: 关联表 Entity + Mapper

**Files:**
- Create: `src/main/java/com/siact/module/system/entity/SysRoleMenuEntity.java`
- Create: `src/main/java/com/siact/module/system/entity/SysUserRoleEntity.java`
- Create: `src/main/java/com/siact/module/system/mapper/SysRoleMenuMapper.java`
- Create: `src/main/java/com/siact/module/system/mapper/SysUserRoleMapper.java`
- Create: `src/main/resources/mapper/SysRoleMenuMapper.xml`
- Create: `src/main/resources/mapper/SysUserRoleMapper.xml`

- [ ] **Step 1: 创建 SysRoleMenuEntity**

```java
package com.siact.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_role_menu_new")
public class SysRoleMenuEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long roleId;

    private Long menuId;

    private Date createTime;
}
```

- [ ] **Step 2: 创建 SysUserRoleEntity**

```java
package com.siact.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_user_role_new")
public class SysUserRoleEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long roleId;

    private Date createTime;
}
```

- [ ] **Step 3: 创建 SysRoleMenuMapper 接口**

```java
package com.siact.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.system.entity.SysRoleMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenuEntity> {

    void batchInsert(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    void deleteByRoleId(@Param("roleId") Long roleId);

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    List<Long> selectMenuIdsByRoleIds(@Param("roleIds") List<Long> roleIds);
}
```

- [ ] **Step 4: 创建 SysRoleMenuMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.siact.module.system.mapper.SysRoleMenuMapper">

    <insert id="batchInsert">
        INSERT INTO sys_role_menu_new (role_id, menu_id, create_time)
        VALUES
        <foreach collection="menuIds" item="menuId" separator=",">
            (#{roleId}, #{menuId}, NOW())
        </foreach>
    </insert>

    <delete id="deleteByRoleId">
        DELETE FROM sys_role_menu_new WHERE role_id = #{roleId}
    </delete>

    <select id="selectMenuIdsByRoleId" resultType="java.lang.Long">
        SELECT menu_id FROM sys_role_menu_new WHERE role_id = #{roleId}
    </select>

    <select id="selectMenuIdsByRoleIds" resultType="java.lang.Long">
        SELECT DISTINCT menu_id FROM sys_role_menu_new WHERE role_id IN
        <foreach collection="roleIds" item="roleId" open="(" separator="," close=")">
            #{roleId}
        </foreach>
    </select>

</mapper>
```

- [ ] **Step 5: 创建 SysUserRoleMapper 接口**

```java
package com.siact.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.system.entity.SysUserRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRoleEntity> {

    void batchInsert(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    void deleteByUserId(@Param("userId") Long userId);

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
```

- [ ] **Step 6: 创建 SysUserRoleMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.siact.module.system.mapper.SysUserRoleMapper">

    <insert id="batchInsert">
        INSERT INTO sys_user_role_new (user_id, role_id, create_time)
        VALUES
        <foreach collection="roleIds" item="roleId" separator=",">
            (#{userId}, #{roleId}, NOW())
        </foreach>
    </insert>

    <delete id="deleteByUserId">
        DELETE FROM sys_user_role_new WHERE user_id = #{userId}
    </delete>

    <select id="selectRoleIdsByUserId" resultType="java.lang.Long">
        SELECT role_id FROM sys_user_role_new WHERE user_id = #{userId}
    </select>

</mapper>
```

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/siact/module/system/entity/SysRoleMenuEntity.java src/main/java/com/siact/module/system/entity/SysUserRoleEntity.java src/main/java/com/siact/module/system/mapper/SysRoleMenuMapper.java src/main/java/com/siact/module/system/mapper/SysUserRoleMapper.java src/main/resources/mapper/SysRoleMenuMapper.xml src/main/resources/mapper/SysUserRoleMapper.xml
git commit -m "feat(system): 添加角色菜单和用户角色关联 Entity/Mapper"
```

---

### Task 18: 用户菜单查询接口

**Files:**
- Modify: `src/main/java/com/siact/module/system/service/SysUserService.java`
- Modify: `src/main/java/com/siact/module/system/service/impl/SysUserServiceImpl.java`
- Modify: `src/main/java/com/siact/module/system/controller/SysUserController.java`

- [ ] **Step 1: 在 SysUserService 接口添加方法**

在 `SysUserService.java` 中添加：

```java
List<SysMenuTreeVO> getUserMenus(Long userId);
```

- [ ] **Step 2: 在 SysUserServiceImpl 添加实现**

在 `SysUserServiceImpl.java` 中注入 `SysUserRoleMapper`（已有）和 `SysRoleMenuMapper`（已有），添加：

```java
private final SysMenuMapper sysMenuMapper;
private final SysRoleMenuMapper sysRoleMenuMapper;

@Override
public List<SysMenuTreeVO> getUserMenus(Long userId) {
    // 1. 查询用户角色ID列表
    List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
    if (CollectionUtils.isEmpty(roleIds)) {
        return Collections.emptyList();
    }

    // 2. 查询角色关联的菜单ID列表
    List<Long> menuIds = sysRoleMenuMapper.selectMenuIdsByRoleIds(roleIds);
    if (CollectionUtils.isEmpty(menuIds)) {
        return Collections.emptyList();
    }

    // 3. 查询菜单实体
    List<SysMenuEntity> menus = sysMenuMapper.selectBatchIds(menuIds);
    if (CollectionUtils.isEmpty(menus)) {
        return Collections.emptyList();
    }

    // 4. 构建树形结构
    SysMenuConvert menuConvert = SpringUtil.getBean(SysMenuConvert.class);
    Map<Long, SysMenuTreeVO> treeMap = menus.stream()
            .map(menuConvert::toTreeVO)
            .collect(Collectors.toMap(SysMenuTreeVO::getId, Function.identity()));

    List<SysMenuTreeVO> roots = new ArrayList<>();
    List<SysMenuTreeVO> sorted = treeMap.values().stream()
            .sorted(Comparator.comparingLong(SysMenuTreeVO::getParentId)
                    .thenComparingInt(SysMenuTreeVO::getSort))
            .collect(Collectors.toList());

    for (SysMenuTreeVO node : sorted) {
        Long parentId = node.getParentId();
        if (parentId == null || parentId == 0L || !treeMap.containsKey(parentId)) {
            roots.add(node);
        } else {
            SysMenuTreeVO parent = treeMap.get(parentId);
            if (CollectionUtils.isEmpty(parent.getChildren())) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(node);
        }
    }
    return roots;
}
```

注意：需要导入 `cn.hutool.core.util.SpringUtil`（项目已引入 Hutool），或者改为通过构造器注入 `SysMenuConvert`。

- [ ] **Step 3: 在 SysUserController 添加接口**

```java
@GetMapping("/{id}/menus")
public List<SysMenuTreeVO> getUserMenus(@PathVariable Long id) {
    return service.getUserMenus(id);
}
```

需要导入 `com.siact.module.system.vo.SysMenuTreeVO`。

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/siact/module/system/service/SysUserService.java src/main/java/com/siact/module/system/service/impl/SysUserServiceImpl.java src/main/java/com/siact/module/system/controller/SysUserController.java
git commit -m "feat(system): 添加用户菜单树查询接口"
```

---

### Task 19: 编译验证

- [ ] **Step 1: 运行 Maven 编译**

Run: `mvn compile -pl . -am`
Expected: BUILD SUCCESS

- [ ] **Step 2: 修复编译错误（如有）**

根据编译输出修复类型不匹配、缺少导入等问题。

- [ ] **Step 3: Commit（如有修复）**

```bash
git add -A
git commit -m "fix(system): 修复编译错误"
```

---

## 任务依赖关系

```
Task 1 (SQL) ─────────────────────────────────────────────────┐
Task 2 (MenuTypeEnum) ────────────────────────────────────────┤
                                                              ↓
Task 3 (SysMenuEntity) → Task 4 (DTO/VO/Command) → Task 5 (Mapper/Repo) → Task 6 (Convert) → Task 7 (Service/Controller)
                                                              │
Task 8 (SysRoleEntity) ──→ Task 9 (DTO/VO/Command) ──────────→│
                                                              │
Task 11 (SysUserEntity) ─→ Task 12 (DTO/VO/Command) ─────────→│
                                                              │
Task 14 (SysOrgEntity) ──→ Task 15 (DTO/VO/Command) ─────────→│
                                                              │
Task 17 (关联表 Entity/Mapper) ←── Task 10/13/16 依赖此任务 ──→│
                                                              │
Task 18 (用户菜单查询) ←── 依赖所有前置任务 ───────────────────→│
                                                              │
Task 19 (编译验证) ←── 最后执行 ──────────────────────────────→┘
```

**注意：** Task 10（角色 Service/Controller）和 Task 13（用户 Service/Controller）引用了 `SysRoleMenuMapper` 和 `SysUserRoleMapper`，因此 Task 17 应在 Task 10 和 Task 13 之前执行，或者先执行 Task 17 创建 Mapper，再执行 Task 10/13。建议实际执行顺序为：Task 1-9 → Task 17 → Task 10-16 → Task 18 → Task 19。
