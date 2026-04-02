# System 模块权限管理设计方案

## 背景

项目现有 `permission` 模块包含菜单、角色、用户、组织管理功能，使用 `sys_menu` 表。新开发的 `system` 模块创建了 `SysMenuEntity` 使用 `sys_menu_new` 表，功能不完整。

本设计目标：在 `system` 模块中重新实现完整的权限管理体系，采用经典 RBAC 模型，测试验证后再移除旧的 `permission` 模块。

---

## 1. 数据模型设计

### 1.1 菜单表 `sys_menu`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 雪花ID，主键 |
| parent_id | BIGINT | 父级菜单ID，顶级菜单为 0 |
| menu_name | VARCHAR(50) | 菜单名称 |
| menu_code | VARCHAR(100) | 菜单编码，用于权限标识 |
| path | VARCHAR(200) | 前端路由地址 |
| icon | VARCHAR(100) | 菜单图标 |
| sort | INT | 排序序号，默认 0 |
| type | TINYINT | 类型：1=目录，2=菜单 |
| visible | BOOLEAN | 是否显示，默认 true |
| status | BOOLEAN | 状态：true=启用，false=停用，默认 true |
| create_by | VARCHAR(50) | 创建者 |
| create_time | DATETIME | 创建时间 |
| update_by | VARCHAR(50) | 更新者 |
| update_time | DATETIME | 更新时间 |
| deleted | BOOLEAN | 逻辑删除，默认 false |

索引：
- `idx_parent_id` (parent_id)
- `idx_menu_code` (menu_code)

### 1.2 角色表 `sys_role`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 雪花ID，主键 |
| role_name | VARCHAR(50) | 角色名称 |
| role_code | VARCHAR(100) | 角色编码 |
| description | VARCHAR(200) | 角色描述 |
| sort | INT | 排序序号，默认 0 |
| status | BOOLEAN | 状态：true=启用，false=停用，默认 true |
| create_by | VARCHAR(50) | 创建者 |
| create_time | DATETIME | 创建时间 |
| update_by | VARCHAR(50) | 更新者 |
| update_time | DATETIME | 更新时间 |
| deleted | BOOLEAN | 逻辑删除，默认 false |

索引：
- `idx_role_code` (role_code)

### 1.3 用户表 `sys_user`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 雪花ID，主键 |
| username | VARCHAR(50) | 用户名，登录账号，唯一 |
| password | VARCHAR(200) | 密码，加密存储 |
| nickname | VARCHAR(50) | 用户昵称 |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| avatar | VARCHAR(200) | 头像URL |
| org_id | BIGINT | 所属组织ID |
| status | BOOLEAN | 状态：true=启用，false=停用，默认 true |
| create_by | VARCHAR(50) | 创建者 |
| create_time | DATETIME | 创建时间 |
| update_by | VARCHAR(50) | 更新者 |
| update_time | DATETIME | 更新时间 |
| deleted | BOOLEAN | 逻辑删除，默认 false |

索引：
- `uk_username` (username) UNIQUE
- `idx_org_id` (org_id)

### 1.4 组织表 `sys_organization`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 雪花ID，主键 |
| parent_id | BIGINT | 父组织ID，顶级组织为 0 |
| org_name | VARCHAR(50) | 组织名称 |
| org_code | VARCHAR(100) | 组织编码 |
| sort | INT | 排序序号，默认 0 |
| status | BOOLEAN | 状态：true=启用，false=停用，默认 true |
| create_by | VARCHAR(50) | 创建者 |
| create_time | DATETIME | 创建时间 |
| update_by | VARCHAR(50) | 更新者 |
| update_time | DATETIME | 更新时间 |
| deleted | BOOLEAN | 逻辑删除，默认 false |

索引：
- `idx_parent_id` (parent_id)
- `idx_org_code` (org_code)

### 1.5 关联表

**角色-菜单关联表 `sys_role_menu`**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| role_id | BIGINT | 角色ID |
| menu_id | BIGINT | 菜单ID |
| create_time | DATETIME | 创建时间 |

索引：
- `idx_role_id` (role_id)
- `idx_menu_id` (menu_id)
- `uk_role_menu` (role_id, menu_id) UNIQUE

**用户-角色关联表 `sys_user_role`**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID |
| role_id | BIGINT | 角色ID |
| create_time | DATETIME | 创建时间 |

索引：
- `idx_user_id` (user_id)
- `idx_role_id` (role_id)
- `uk_user_role` (user_id, role_id) UNIQUE

---

## 2. 业务功能设计

### 2.1 菜单管理

**功能列表：**
- 分页列表查询：支持按菜单名称、状态筛选
- 菜单树查询：返回树形结构，用于角色分配权限展示
- 新增菜单：校验父菜单存在性，menu_code 自动生成（拼音首字母）或手动输入
- 编辑菜单：校验父菜单有效性（不能自设为父级、不能设为子级的父级）
- 删除菜单：有子菜单则禁止删除

**业务规则：**
- 目录类型(type=1)必须有 path，菜单类型(type=2)必须有 path
- parent_id 为 0 表示顶级菜单
- 删除前检查是否存在子菜单
- 编辑时校验循环引用

### 2.2 角色管理

**功能列表：**
- 分页列表查询：支持按角色名称、状态筛选
- 新增角色：role_code 自动生成或手动输入
- 编辑角色：同新增校验规则
- 删除角色：删除时清除 sys_role_menu 关联数据
- 分配菜单权限：保存角色-菜单关联（先删后增）
- 获取角色菜单：查询角色已分配的菜单ID列表

**业务规则：**
- 删除角色时同步删除 sys_role_menu 关联
- 分配菜单权限时先删除旧关联再保存新关联
- 角色编码(role_code)唯一

### 2.3 用户管理

**功能列表：**
- 分页列表查询：支持按用户名、昵称、组织、状态筛选
- 新增用户：密码加密存储，校验用户名唯一
- 编辑用户：不能修改 username，其他字段可修改
- 删除用户：删除时清除 sys_user_role 关联数据
- 重置密码：将密码重置为默认值
- 分配角色：保存用户-角色关联
- 获取用户角色：查询用户已分配的角色ID列表

**业务规则：**
- 用户名(username)唯一
- 新增时密码默认加密存储
- 删除用户时同步删除 sys_user_role 关联
- 密码使用 BCrypt 加密

### 2.4 组织管理

**功能列表：**
- 分页列表查询：支持按组织名称、状态筛选
- 组织树查询：返回树形结构
- 新增组织：校验父组织存在性
- 编辑组织：校验父组织有效性
- 删除组织：有子组织或有用户则禁止删除

**业务规则：**
- 删除前检查是否存在子组织
- 删除前检查是否存在关联用户
- 组织编码(org_code)唯一

---

## 3. 权限校验流程

### 3.1 登录后获取用户菜单

流程：
1. 用户登录成功
2. 根据 user_id 查询 sys_user_role 获取角色ID列表
3. 根据角色ID列表查询 sys_role_menu 获取菜单ID列表
4. 根据菜单ID列表查询 sys_menu 获取菜单实体（去重）
5. 将菜单列表构建为树形结构返回前端

### 3.2 接口权限校验（可选扩展）

设计预留，后续可实现：
- 登录时将用户的 menu_code 列表存入 Redis 缓存
- 接口通过注解标注需要的权限编码
- 拦截器校验用户缓存中是否包含该权限编码

---

## 4. 目录结构设计

```
com.siact.module.system
├── controller
│   ├── SysMenuController.java
│   ├── SysRoleController.java
│   ├── SysUserController.java
│   └── SysOrganizationController.java
├── service
│   ├── SysMenuService.java
│   ├── SysRoleService.java
│   ├── SysUserService.java
│   ├── SysOrganizationService.java
│   └── impl/
│       ├── SysMenuServiceImpl.java
│       ├── SysRoleServiceImpl.java
│       ├── SysUserServiceImpl.java
│       ├── SysOrganizationServiceImpl.java
├── entity
│   ├── SysMenuEntity.java          (已有，需重构字段)
│   ├── SysRoleEntity.java
│   ├── SysUserEntity.java
│   ├── SysOrganizationEntity.java
│   ├── SysRoleMenuEntity.java
│   ├── SysUserRoleEntity.java
├── mapper
│   ├── SysMenuMapper.java          (已有，需补充方法)
│   ├── SysRoleMapper.java
│   ├── SysUserMapper.java
│   ├── SysOrganizationMapper.java
│   ├── SysRoleMenuMapper.java
│   ├── SysUserRoleMapper.java
├── dto
│   ├── SysMenuDTO.java
│   ├── SysRoleDTO.java
│   ├── SysUserDTO.java
│   ├── SysOrganizationDTO.java
├── vo
│   ├── SysMenuVO.java
│   ├── SysMenuTreeVO.java          (已有)
│   ├── SysRoleVO.java
│   ├── SysUserVO.java
│   ├── SysOrganizationVO.java
│   ├── SysOrganizationTreeVO.java
├── query
│   ├── SysMenuQuery.java           (已有)
│   ├── SysRoleQuery.java
│   ├── SysUserQuery.java
│   ├── SysOrganizationQuery.java
├── command
│   ├── SysMenuCreateCommand.java   (已有，需重构)
│   ├── SysMenuUpdateCommand.java   (新增)
│   ├── SysMenuDeleteCommand.java   (已有)
│   ├── SysRoleCommand.java
│   ├── SysUserCommand.java
│   ├── SysOrganizationCommand.java
│   ├── AssignRoleMenuCommand.java
│   ├── AssignUserRoleCommand.java
│   ├── ResetPasswordCommand.java
├── convert
│   ├── SysMenuConvert.java         (已有，需扩展)
│   ├── SysRoleConvert.java
│   ├── SysUserConvert.java
│   ├── SysOrganizationConvert.java
├── repository
│   ├── SysMenuRepository.java      (已有)
│   ├── SysRoleRepository.java
│   ├── SysUserRepository.java
│   ├── SysOrganizationRepository.java
│   └── impl/
│       ├── SysMenuRepositoryImpl.java  (已有)
│       ├── SysRoleRepositoryImpl.java
│       ├── SysUserRepositoryImpl.java
│       ├── SysOrganizationRepositoryImpl.java
├── enums
│   ├── MenuTypeEnum.java           (新增：目录/菜单)
│   ├── SysConfigModuleEnum.java    (已有)
│   ├── SysConfigTypeEnum.java      (已有)
```

---

## 5. 接口设计

### 5.1 菜单管理接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | POST | `/sysmenu/list` | RequestBody: SysMenuQuery（沿用现有） |
| 菜单树 | GET | `/sysmenu/tree` | 返回树形结构（沿用现有） |
| 新增菜单 | POST | `/sysmenu` | RequestBody: SysMenuCommand |
| 编辑菜单 | PUT | `/sysmenu` | RequestBody: SysMenuCommand |
| 删除菜单 | DELETE | `/sysmenu/{id}` | PathVariable: id |

### 5.2 角色管理接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | POST | `/sysrole/list` | RequestBody: SysRoleQuery |
| 新增角色 | POST | `/sysrole` | RequestBody: SysRoleCommand |
| 编辑角色 | PUT | `/sysrole` | RequestBody: SysRoleCommand |
| 删除角色 | DELETE | `/sysrole/{id}` | PathVariable: id |
| 分配菜单 | PUT | `/sysrole/{id}/menus` | PathVariable: id, RequestBody: AssignRoleMenuCommand |
| 获取角色菜单 | GET | `/sysrole/{id}/menus` | PathVariable: id |

### 5.3 用户管理接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | POST | `/sysuser/list` | RequestBody: SysUserQuery |
| 新增用户 | POST | `/sysuser` | RequestBody: SysUserCommand |
| 编辑用户 | PUT | `/sysuser` | RequestBody: SysUserCommand |
| 删除用户 | DELETE | `/sysuser/{id}` | PathVariable: id |
| 重置密码 | PUT | `/sysuser/{id}/reset-password` | PathVariable: id |
| 分配角色 | PUT | `/sysuser/{id}/roles` | PathVariable: id, RequestBody: AssignUserRoleCommand |
| 获取用户角色 | GET | `/sysuser/{id}/roles` | PathVariable: id |

### 5.4 组织管理接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | POST | `/sysorg/list` | RequestBody: SysOrganizationQuery |
| 组织树 | GET | `/sysorg/tree` | 返回树形结构 |
| 新增组织 | POST | `/sysorg` | RequestBody: SysOrganizationCommand |
| 编辑组织 | PUT | `/sysorg` | RequestBody: SysOrganizationCommand |
| 删除组织 | DELETE | `/sysorg/{id}` | PathVariable: id |

---

## 6. 数据库脚本

见实现计划中的 SQL 迁移脚本。

---

## 7. 实现优先级

1. 数据库表结构创建
2. Entity + Mapper 层
3. 菜单管理 Service + Controller
4. 角色管理 Service + Controller
5. 用户管理 Service + Controller
6. 组织管理 Service + Controller
7. 权限校验流程集成

---

## 8. 备注

- 旧的 `permission` 模块代码保留不动，待新模块测试验证后移除
- 权限控制仅菜单级别，按钮权限由前端指令控制
- 密码加密使用 BCrypt 算法
- 使用 MyBatis-Plus 的逻辑删除功能