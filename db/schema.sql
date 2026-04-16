/*
 Navicat Premium Dump SQL

 Source Server         : localhost8.0
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3307
 Source Schema         : kiln_intelligent_control

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 22/05/2025 16:19:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for control_interval_config
-- ----------------------------
DROP TABLE IF EXISTS `control_interval_config`;
CREATE TABLE `control_interval_config`  (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                            `measure_point` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '测点，温度中有MC1-10',
                                            `point_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '测点code，暂时预留一下',
                                            `up_control` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上控制值',
                                            `low_control` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '下控制值',
                                            `up_alarm` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上告警值',
                                            `low_alarm` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '下告警值',
                                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测点控制区间配置表，表现为页面窑炉控制下控制区间设置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of control_interval_config
-- ----------------------------
INSERT INTO `control_interval_config` VALUES (1, 'MC1', 'temperature', '150.0', '20.0', '160.0', '10.0');
INSERT INTO `control_interval_config` VALUES (2, 'MC2', 'temperature', '150.0', '20.0', '', '');
INSERT INTO `control_interval_config` VALUES (3, 'MC3', 'temperature', '150.0', '20.0', '', '');
INSERT INTO `control_interval_config` VALUES (4, 'MC4', 'temperature', '150.0', '20.0', '', '');
INSERT INTO `control_interval_config` VALUES (5, 'MC5', 'temperature', '150.0', '20.0', '160.0', '10.0');
INSERT INTO `control_interval_config` VALUES (6, 'MC6', 'temperature', '150.0', '20.0', '', '');
INSERT INTO `control_interval_config` VALUES (7, 'MC7', 'temperature', '150.0', '20.0', '', '');
INSERT INTO `control_interval_config` VALUES (8, 'MC8', 'temperature', '150.0', '20.0', '', '');
INSERT INTO `control_interval_config` VALUES (9, 'MC9', 'temperature', '150.0', '20.0', '', '');
INSERT INTO `control_interval_config` VALUES (10, 'MC10', 'temperature', '150.0', '20.0', '160.0', '10.0');
INSERT INTO `control_interval_config` VALUES (11, 'YWMC', 'liquidLevel', '150.0', '20.0', '160.0', '10.0');
INSERT INTO `control_interval_config` VALUES (12, 'YLMC', 'pressure', '150.0', '20.0', '160.0', '10.0');

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
INSERT INTO `dic` VALUES (1, 'reportCategory', '人事报表', 'personnelReport', '1', '', 'reportSetting', 0, '0', 0, '', '', '', '2025-05-22 15:49:19');
INSERT INTO `dic` VALUES (2, 'reportCategory', '能耗报表', 'energyReport', '2', '', 'reportSetting', 0, '0', 1, '', '', '', '2025-05-22 15:49:19');
INSERT INTO `dic` VALUES (3, 'reportCategory', '生产报表', 'productionReport', '3', '', 'reportSetting', 0, '0', 2, '', '', '', '2025-05-22 15:49:19');
INSERT INTO `dic` VALUES (4, 'importantParam', '参数1', 'importp1', '参数1', NULL, 'modelParamSetting', 0, '0', 0, NULL, NULL, NULL, '2025-05-22 16:06:17');
INSERT INTO `dic` VALUES (5, 'importantParam', '参数2', 'importp2', '参数2', NULL, 'modelParamSetting', 0, '0', 1, NULL, NULL, NULL, '2025-05-22 16:06:17');
INSERT INTO `dic` VALUES (6, 'importantParam', '参数3', 'importp3', '参数3', NULL, 'modelParamSetting', 0, '0', 2, NULL, NULL, NULL, '2025-05-22 16:06:17');
INSERT INTO `dic` VALUES (7, 'secondParam', '参数1', 'secondp1', '参数1', NULL, 'modelParamSetting', 0, '0', 0, NULL, NULL, NULL, '2025-05-22 16:06:17');
INSERT INTO `dic` VALUES (8, 'secondParam', '参数2', 'secondp2', '参数2', NULL, 'modelParamSetting', 0, '0', 1, NULL, NULL, NULL, '2025-05-22 16:06:17');
INSERT INTO `dic` VALUES (9, 'secondParam', '参数3', 'secondp3', '参数3', NULL, 'modelParamSetting', 0, '0', 2, NULL, NULL, NULL, '2025-05-22 16:06:17');
INSERT INTO `dic` VALUES (10, 'generalParam', '参数1', 'generalp1', '参数1', NULL, 'modelParamSetting', 0, '0', 0, NULL, NULL, NULL, '2025-05-22 16:06:17');
INSERT INTO `dic` VALUES (11, 'generalParam', '参数2', 'generalp2', '参数2', NULL, 'modelParamSetting', 0, '0', 1, NULL, NULL, NULL, '2025-05-22 16:06:17');
INSERT INTO `dic` VALUES (12, 'generalParam', '参数3', 'generalp3', '参数3', NULL, 'modelParamSetting', 0, '0', 2, NULL, NULL, NULL, '2025-05-22 16:06:17');

-- ----------------------------
-- Table structure for kiln_info
-- ----------------------------
DROP TABLE IF EXISTS `kiln_info`;
CREATE TABLE `kiln_info`  (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '炉号',
                              `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '炉子code',
                              `state` tinyint(1) NULL DEFAULT NULL COMMENT '是否自动状态（1是 0否）',
                              `gas_calc` decimal(10, 2) NULL DEFAULT NULL COMMENT '天然气计算值',
                              `gas_val` decimal(10, 2) NULL DEFAULT NULL COMMENT '天然气设定值',
                              `wind_val` decimal(10, 2) NULL DEFAULT NULL COMMENT '助燃风设定值',
                              `gas_val_up` decimal(10, 2) NULL DEFAULT NULL COMMENT '天然气流量设定值上限',
                              `gas_val_low` decimal(10, 2) NULL DEFAULT NULL COMMENT '天然气流量设定值下限',
                              `wind_dis_up` decimal(10, 2) NULL DEFAULT NULL COMMENT '气量分布上限',
                              `wind_dis_low` decimal(10, 2) NULL DEFAULT NULL COMMENT '气量分布下限',
                              `total_wind_val` decimal(10, 2) NULL DEFAULT NULL COMMENT '总气量',
                              PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of kiln_info
-- ----------------------------
INSERT INTO `kiln_info` VALUES (1, '1#', '', 0, 660.00, NULL, 25.00, NULL, 80.00, 1.00, NULL, NULL, NULL);
INSERT INTO `kiln_info` VALUES (2, '2#', '', 1, 660.00, NULL, 25.00, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `kiln_info` VALUES (3, '3#', '', 1, 660.00, NULL, 25.00, NULL, 40.00, 20.00, NULL, NULL, NULL);
INSERT INTO `kiln_info` VALUES (4, '4#', '', 1, 660.00, NULL, 25.00, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `kiln_info` VALUES (5, '5#', '', 1, 660.00, NULL, 25.00, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `kiln_info` VALUES (6, '6#', '', 1, 660.00, NULL, 25.00, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `kiln_info` VALUES (7, '7#', '', 1, 660.00, NULL, 25.00, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `kiln_info` VALUES (8, '8#', '', 1, 660.00, NULL, 25.00, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `kiln_info` VALUES (9, '9#', '', 1, 660.00, NULL, 25.00, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `kiln_info` VALUES (10, '10#', '', 1, 660.00, NULL, 25.00, NULL, NULL, NULL, 200.00, 20.00, NULL);

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report`  (
                           `number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报表编号',
                           `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '报表名称',
                           PRIMARY KEY (`number`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of report
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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, 'yaoluyuce', '窑炉预测', '/yaoluyuce', 0, 'yaoluyuce', 1, 1, '2025-05-20 02:44:16', '2025-05-20 15:45:36', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (2, 1, 'wenduyuce', '温度预测', '/wenduyuce', 0, 'wenduyuce', 1, 1, '2025-05-20 02:44:16', '2025-05-20 15:45:36', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (3, 0, 'yaolujiance', '窑炉监测', '/yaolujiance', 1, 'yaolujiance', 1, 1, '2025-05-20 09:39:43', '2025-05-20 17:40:55', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (4, 0, 'yaolukongzhi', '窑炉控制', '/yaolukongzhi', 1, 'yaolukongzhi', 1, 1, '2025-05-20 09:40:35', '2025-05-20 17:40:55', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (5, 0, 'moxingshezhi', '模型设置', '/moxingshezhi', 1, 'moxingshezhi', 1, 1, '2025-05-20 09:41:22', '2025-05-20 09:41:22', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (6, 0, 'kongzhishezhi', '控制设置', '/kongzhishezhi', 1, 'kongzhishezhi', 1, 1, '2025-05-20 09:41:43', '2025-05-20 09:41:43', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (7, 0, 'xitongshezhi', '系统设置', '/xitongshezhi', 1, 'xitongshezhi', 1, 1, '2025-05-20 09:41:56', '2025-05-20 09:41:56', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (8, 0, 'nenghaofenxi', '能耗分析', '/nenghaofenxi', 1, 'nenghaofenxi', 1, 1, '2025-05-20 09:42:46', '2025-05-20 09:42:46', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (9, 0, 'shebeifenxi', '设备分析', '/shebeifenxi', 1, 'shebeifenxi', 1, 1, '2025-05-20 09:43:00', '2025-05-20 09:43:00', 'admin', 'admin', 0);
INSERT INTO `sys_menu` VALUES (10, 0, 'lianglvfenxi', '良率分析', '/lianglvfenxi', 1, 'lianglvfenxi', 1, 1, '2025-05-20 09:43:18', '2025-05-20 09:43:18', 'admin', 'admin', 0);

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
INSERT INTO `sys_role` VALUES (4, '模型设置员', 'MXKZ', '控制模型设置', 1, 1, 0, '2025-05-20 02:47:11', '2025-05-20 02:47:11', 'admin', 'admin');
INSERT INTO `sys_role` VALUES (5, '控制设置员', 'KZSZ', '控制设置', 1, 1, 0, '2025-05-20 02:47:11', '2025-05-20 14:55:04', 'admin', 'admin');
INSERT INTO `sys_role` VALUES (6, '窑炉控制员', 'YLKZ', '窑炉控制', 1, 1, 0, '2025-05-20 02:47:11', '2025-05-20 14:55:04', 'admin', 'admin');

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
INSERT INTO `sys_role_menu` VALUES (27, 1, 1, '2025-05-20 17:44:29');
INSERT INTO `sys_role_menu` VALUES (28, 1, 3, '2025-05-20 17:44:29');
INSERT INTO `sys_role_menu` VALUES (29, 1, 4, '2025-05-20 17:44:29');
INSERT INTO `sys_role_menu` VALUES (30, 1, 5, '2025-05-20 17:44:29');
INSERT INTO `sys_role_menu` VALUES (31, 1, 6, '2025-05-20 17:44:29');
INSERT INTO `sys_role_menu` VALUES (32, 1, 7, '2025-05-20 17:44:29');
INSERT INTO `sys_role_menu` VALUES (33, 1, 8, '2025-05-20 17:44:29');
INSERT INTO `sys_role_menu` VALUES (34, 1, 9, '2025-05-20 17:44:29');
INSERT INTO `sys_role_menu` VALUES (35, 1, 10, '2025-05-20 17:44:29');

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
INSERT INTO `sys_user` VALUES (23, 'admin', '13800138000', 1, '123456', 'true', NULL, NULL, 1, 0, '2025-05-19 17:48:50', '2025-05-19 17:48:50', 'admin', 'admin', 1);
INSERT INTO `sys_user` VALUES (24, 'test01', '13800138000', 1, '123456', 'true', NULL, NULL, 1, 1, '2025-05-20 09:30:42', '2025-05-20 17:25:39', 'admin', 'admin', 1);
INSERT INTO `sys_user` VALUES (38, 'admin1', '13800138000', 1, '123456', 'true', NULL, NULL, 1, 0, '2025-05-20 02:30:25', '2025-05-20 02:30:25', 'admin', 'admin', 1);
INSERT INTO `sys_user` VALUES (39, '张三', '13800138000', 1, '$2a$10$Zf31T7qutt.4oKp5.LdHcujz5ch7S4JZWKyJ7VLea9n.vcE8z54p2', 'true', NULL, NULL, 1, 0, '2025-05-20 06:57:58', '2025-05-20 06:57:58', 'admin', 'admin', 1);

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
INSERT INTO `sys_user_organization` VALUES (32, 1, 1, '2025-05-19 17:26:57');
INSERT INTO `sys_user_organization` VALUES (33, 1, 2, '2025-05-19 17:26:57');
INSERT INTO `sys_user_organization` VALUES (34, 1, 3, '2025-05-19 17:26:57');
INSERT INTO `sys_user_organization` VALUES (35, 23, 1, '2025-05-19 17:50:07');
INSERT INTO `sys_user_organization` VALUES (36, 23, 2, '2025-05-19 17:50:07');
INSERT INTO `sys_user_organization` VALUES (37, 23, 3, '2025-05-19 17:50:07');
INSERT INTO `sys_user_organization` VALUES (41, 38, 1, '2025-05-20 10:30:25');
INSERT INTO `sys_user_organization` VALUES (42, 38, 2, '2025-05-20 10:30:25');
INSERT INTO `sys_user_organization` VALUES (43, 38, 3, '2025-05-20 10:30:25');
INSERT INTO `sys_user_organization` VALUES (55, 39, 1, '2025-05-21 10:03:59');

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
INSERT INTO `sys_user_role` VALUES (32, 1, 1, '2025-05-19 17:26:57');
INSERT INTO `sys_user_role` VALUES (35, 23, 1, '2025-05-19 17:48:54');
INSERT INTO `sys_user_role` VALUES (41, 38, 1, '2025-05-20 10:30:25');
INSERT INTO `sys_user_role` VALUES (57, 39, 1, '2025-05-21 10:03:59');

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
INSERT INTO `tpl` VALUES (3, '窑炉预测菜单', 'kilnPrediction', '{\r\n	\"temperaturePrediction\": [\r\n		{\r\n			\"tabName\": \"温度趋势预测\",\r\n			\"tabCode\": \"temperatureTrendPrediction\",\r\n			\"curve\": [\r\n				{\r\n					\"dataCode\": \"数字孪生属性code\",\r\n					\"forecastCode\": \"MC1\"\r\n				},\r\n				{\r\n					\"dataCode\": \"数字孪生属性code\",\r\n					\"forecastCode\": \"MC5\"\r\n				},\r\n				{\r\n					\"dataCode\": \"数字孪生属性code\",\r\n					\"forecastCode\": \"MC10\"\r\n				}\r\n			]\r\n		},\r\n		{\r\n			\"tabName\": \"MC2-MC4历史曲线\",\r\n			\"tabCode\": \"mc2ToMc4HistoryCurve\",\r\n			\"curve\": [\r\n				{\r\n					\"dataCode\": \"数字孪生属性code\",\r\n					\"forecastCode\": \"MC2\"\r\n				},\r\n				{\r\n					\"dataCode\": \"数字孪生属性code\",\r\n					\"forecastCode\": \"MC3\"\r\n				},\r\n				{\r\n					\"dataCode\": \"数字孪生属性code\",\r\n					\"forecastCode\": \"MC4\"\r\n				}\r\n			]\r\n		},\r\n		{\r\n			\"tabName\": \"MC6-MC9历史曲线\",\r\n			\"tabCode\": \"mc6ToMc9HistoryCurve\",\r\n			\"curve\": [\r\n				{\r\n					\"dataCode\": \"数字孪生属性code\",\r\n					\"forecastCode\": \"MC6\"\r\n				},\r\n				{\r\n					\"dataCode\": \"数字孪生属性code\",\r\n					\"forecastCode\": \"MC7\"\r\n				},\r\n				{\r\n					\"dataCode\": \"数字孪生属性code\",\r\n					\"forecastCode\": \"MC8\"\r\n				},\r\n				{\r\n					\"dataCode\": \"数字孪生属性code\",\r\n					\"forecastCode\": \"MC9\"\r\n				}\r\n			]\r\n		}\r\n	],\r\n	\"liquidLevelPrediction\": [\r\n		{\r\n			\"tabName\": \"液位\",\r\n			\"tabCode\": \"waterLeve\",\r\n			\"curve\": [\r\n				{\r\n					\"dataCode\": \"数字孪生属性code\",\r\n					\"forecastCode\": \"wl\"\r\n				}\r\n			]\r\n		}\r\n	],\r\n	\"pressurePrediction\": [\r\n		{\r\n			\"tabName\": \"压力\",\r\n			\"tabCode\": \"yali\",\r\n			\"curve\": [\r\n				{\r\n					\"dataCode\": \"数字孪生属性code\",\r\n					\"forecastCode\": \"wl\"\r\n				}\r\n			]\r\n		}\r\n	]\r\n}', '', '窑炉预测模块菜单tab页配置', '2025-05-21 16:23:13', '2025-05-21 16:21:45', NULL, NULL, 1);

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for process_log
-- ----------------------------
DROP TABLE IF EXISTS `process_log`;
CREATE TABLE `process_log`  (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `start_time` DATETIME NOT NULL COMMENT '开始日期',
    `end_time` DATETIME NOT NULL COMMENT '结束日期',
    `product_line_num` varchar(50) NOT NULL COMMENT '产线数量(Ⅲ\Ⅳ)',
    `fire_cycle` INT NOT NULL COMMENT '换火周期(单位min)',
    `defoam_system` char(2) NOT NULL COMMENT '除泡系统 Y:有 X:无',
    `replace_machine` TINYINT NOT NULL COMMENT '更换设备 1:正常 2:换机',
    `operating_code` varchar(50) NOT NULL COMMENT '工况编码',
    `binary_code` varchar(50) NOT NULL COMMENT '二进制编码',
    `operator` VARCHAR(64) NOT NULL COMMENT '操作员',
    `operation_date` DATETIME NOT NULL COMMENT '操作日期',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '工艺日志表';

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

