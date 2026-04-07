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
    `account`     VARCHAR(50)  NOT NULL COMMENT '账号，登录账号',
    `username`    VARCHAR(50)           DEFAULT NULL COMMENT '用户名',
    `password`    VARCHAR(200) NOT NULL COMMENT '密码，BCrypt加密',
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
    UNIQUE INDEX `uk_account` (`account`),
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
