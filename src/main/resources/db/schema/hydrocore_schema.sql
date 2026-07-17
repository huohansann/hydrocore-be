/*
 Navicat Premium Dump SQL

 Source Server         : localhost8.0
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3307
 Source Schema         : hydrocore

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 22/05/2025 16:19:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dic
-- ----------------------------
DROP TABLE IF EXISTS `dic`;
CREATE TABLE `dic`  (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '表自增主键',
                        `type` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型',
                        `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
                        `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '编码',
                        `value` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '值',
                        `unit` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '单位',
                        `tag` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签',
                        `parent_id` bigint NULL DEFAULT NULL COMMENT '父id',
                        `accuracy` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '精度',
                        `sort` int NULL DEFAULT NULL COMMENT '排序',
                        `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态',
                        `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述信息',
                        `formula` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '计算公式',
                        `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
                        PRIMARY KEY (`id`) USING BTREE,
                        UNIQUE INDEX `uni`(`type` ASC, `code` ASC) USING BTREE COMMENT '唯一限制'
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dic
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `parent_id` bigint NULL DEFAULT 0 COMMENT '父级ID，顶级为0',
                             `menu_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单code',
                             `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
                             `menu_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由地址',
                             `type` tinyint(1) NULL DEFAULT 0 COMMENT '类型（0目录，1菜单，2实例/项目）',
                             `menu_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
                             `model_show` tinyint(1) NULL DEFAULT 1 COMMENT '是否显示（1是 0否）',
                             `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（1正常 0停用）',
                             `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
                             `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
                             `deleted` int NULL DEFAULT 0 COMMENT '逻辑删除状态',
                             PRIMARY KEY (`id`) USING BTREE,
                             INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE COMMENT '父级ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu (HydroCore slim shell — menu_code matches FE RouteName)
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, 'overview', '总览', '/overview', 1, 'tabler:layout-dashboard', 1, 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (2, 0, 'trends', '趋势', '/trends', 1, 'tabler:chart-line', 1, 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (3, 0, 'system', '系统管理', '/system', 0, 'tabler:settings', 1, 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (4, 3, 'system.user', '用户管理', '/system/user', 1, 'tabler:user', 1, 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (5, 3, 'system.role', '角色管理', '/system/role', 1, 'tabler:shield', 1, 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (6, 3, 'system.menu', '菜单管理', '/system/menu', 1, 'tabler:menu-2', 1, 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (7, 3, 'system.org', '组织管理', '/system/org', 1, 'tabler:building', 1, 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (8, 3, 'system.points', '点位映射', '/system/points', 1, 'tabler:topology-star', 1, 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (9, 3, 'system.realtime', '实时查询', '/system/realtime', 1, 'tabler:activity', 1, 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (10, 3, 'system.config', '系统配置', '/system/config', 1, 'tabler:adjustments', 1, 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00', 'admin', 'admin', 0);

-- ----------------------------
-- Table structure for sys_organization
-- ----------------------------
DROP TABLE IF EXISTS `sys_organization`;
CREATE TABLE `sys_organization`  (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                     `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组织名称',
                                     `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织编码',
                                     `parent_id` bigint NULL DEFAULT 0 COMMENT '父级ID，顶级为0',
                                     `ancestors` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '祖级列表，以逗号分隔',
                                     `sort` int NULL DEFAULT 0 COMMENT '显示顺序',
                                     `leader` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人',
                                     `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
                                     `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
                                     `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（1正常 0停用）',
                                     `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除（1是 0否）',
                                     `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
                                     `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE COMMENT '父级ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织结构表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_organization
-- ----------------------------
INSERT INTO `sys_organization` VALUES (1, '技术部', 'TECH', 0, '0', 1, '张三', '13800138000', 'example@example.com', 1, 0, '2025-05-20 02:51:22', '2025-05-20 02:51:22', 'admin', 'admin');
INSERT INTO `sys_organization` VALUES (7, '销售部', 'XIAOSHOU', 0, '0', 1, '李四', '13800138000', 'example@example.com', 1, 0, '2025-05-20 06:44:50', '2025-05-20 06:44:50', 'admin', 'admin');
INSERT INTO `sys_organization` VALUES (8, '研发技术部', 'TECH', 1, '0,1', 1, '张三小一', '13800138000', 'example@example.com', 1, 0, '2025-05-20 06:48:00', '2025-05-20 06:48:00', 'admin', 'admin');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
                             `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色编码',
                             `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
                             `sort` int NULL DEFAULT 0 COMMENT '显示顺序',
                             `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（1正常 0停用）',
                             `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除（1是 0否）',
                             `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
                             `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `idx_code`(`code` ASC) USING BTREE COMMENT '角色编码唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '管理员', 'ADMIN', '系统管理员，拥有所有权限', 1, 1, 0, '2025-05-20 02:47:11', '2025-05-20 02:47:11', 'admin', 'admin');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `role_id` bigint NOT NULL COMMENT '角色ID',
                                  `menu_id` bigint NOT NULL COMMENT '菜单ID',
                                  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  UNIQUE INDEX `idx_role_menu`(`role_id` ASC, `menu_id` ASC) USING BTREE COMMENT '角色菜单唯一索引',
                                  INDEX `idx_menu_id`(`menu_id` ASC) USING BTREE COMMENT '菜单ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (27, 1, 1, '2026-07-17 00:00:00');
INSERT INTO `sys_role_menu` VALUES (28, 1, 2, '2026-07-17 00:00:00');
INSERT INTO `sys_role_menu` VALUES (29, 1, 3, '2026-07-17 00:00:00');
INSERT INTO `sys_role_menu` VALUES (30, 1, 4, '2026-07-17 00:00:00');
INSERT INTO `sys_role_menu` VALUES (31, 1, 5, '2026-07-17 00:00:00');
INSERT INTO `sys_role_menu` VALUES (32, 1, 6, '2026-07-17 00:00:00');
INSERT INTO `sys_role_menu` VALUES (33, 1, 7, '2026-07-17 00:00:00');
INSERT INTO `sys_role_menu` VALUES (34, 1, 8, '2026-07-17 00:00:00');
INSERT INTO `sys_role_menu` VALUES (35, 1, 9, '2026-07-17 00:00:00');
INSERT INTO `sys_role_menu` VALUES (36, 1, 10, '2026-07-17 00:00:00');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
                             `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号码',
                             `org_id` bigint NULL DEFAULT NULL COMMENT '所属组织ID',
                             `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
                             `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1' COMMENT '账号\r\n',
                             `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像地址',
                             `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
                             `gender` tinyint(1) NULL DEFAULT 0 COMMENT '性别（0未知 1男 2女）',
                             `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除（1是 0否）',
                             `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
                             `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
                             `status` tinyint NULL DEFAULT NULL COMMENT '状态',
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE COMMENT '用户名唯一索引',
                             INDEX `idx_org_id`(`org_id` ASC) USING BTREE COMMENT '组织ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', NULL, 1, '$2a$10$S9LBvmhHtHk0EMBxqHhgwOc03ADjVhDnCefY9PKV6htJEEyoCUIeG', 'admin', NULL, 'admin@local.invalid', 0, 0, '2026-07-17 00:00:00', '2026-07-17 00:00:00', 'system', 'system', 1);

-- ----------------------------
-- Table structure for sys_user_organization
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_organization`;
CREATE TABLE `sys_user_organization`  (
                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                          `user_id` bigint NOT NULL COMMENT '用户ID',
                                          `org_id` bigint NOT NULL COMMENT '组织ID',
                                          `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          PRIMARY KEY (`id`) USING BTREE,
                                          UNIQUE INDEX `idx_user_org`(`user_id` ASC, `org_id` ASC) USING BTREE COMMENT '用户组织唯一索引',
                                          INDEX `idx_org_id`(`org_id` ASC) USING BTREE COMMENT '组织ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户组织关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_organization
-- ----------------------------
INSERT INTO `sys_user_organization` VALUES (1, 1, 1, '2026-07-17 00:00:00');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `user_id` bigint NOT NULL COMMENT '用户ID',
                                  `role_id` bigint NOT NULL COMMENT '角色ID',
                                  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  UNIQUE INDEX `idx_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE COMMENT '用户角色唯一索引',
                                  INDEX `idx_role_id`(`role_id` ASC) USING BTREE COMMENT '角色ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, '2026-07-17 00:00:00');

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
                              `id` bigint NOT NULL COMMENT '主键ID',
                              `module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置模块',
                              `sc_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置编码',
                              `sc_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置路径',
                              `sc_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置名称',
                              `sc_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置类型',
                              `sc_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置值',
                              `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置说明',
                              `version` int NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
                              `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`id`) USING BTREE,
                              UNIQUE INDEX `idx_sc_code_path`(`sc_code` ASC, `sc_path` ASC) USING BTREE,
                              INDEX `idx_module`(`module` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'SYSTEM', 'system_display_name', 'value', '系统显示名称', 'STRING', 'HydroCore', 'HydroCore 基线系统显示名称', 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00');
INSERT INTO `sys_config` VALUES (2, 'SYSTEM', 'system_default_locale', 'value', '默认语言', 'STRING', 'zh-CN', 'HydroCore 基线默认语言设置', 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00');
INSERT INTO `sys_config` VALUES (3, 'INTEGRATION', 'integration_endpoints', '[0].name', '集成端点占位', 'STRING', 'local-api', '本地二开集成端点占位，不包含生产地址', 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00');
INSERT INTO `sys_config` VALUES (4, 'INTEGRATION', 'integration_endpoints', '[0].url', '集成端点占位', 'STRING', 'http://localhost:8080', '本地二开集成端点占位，不包含生产地址', 1, '2026-07-17 00:00:00', '2026-07-17 00:00:00');

-- ----------------------------
-- Table structure for tpl
-- ----------------------------
DROP TABLE IF EXISTS `tpl`;
CREATE TABLE `tpl`  (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `tpl_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模板名称',
                        `tpl_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模板编码',
                        `tpl_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '模板内容',
                        `tpl_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模板类型',
                        `tpl_describe` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模板描述',
                        `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `create_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
                        `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
                        `update_by` bigint NULL DEFAULT NULL COMMENT '修改人',
                        `data_status` tinyint(1) NULL DEFAULT 1 COMMENT '1有效 0无效',
                        PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tpl
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;

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

