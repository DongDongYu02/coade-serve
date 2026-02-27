/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : localhost:3306
 Source Schema         : nexus

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 07/02/2026 10:44:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `value` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置值',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `group` int(10) NULL DEFAULT NULL COMMENT '配置分类',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES ('systemName', '', '系统名称', 1, '2026-02-07 08:43:16', '2018951326683222017');
INSERT INTO `sys_config` VALUES ('systemLogo', '', '系统LOGO', 1, '2026-02-07 08:43:16', '2018951326683222017');

-- ----------------------------
-- Table structure for sys_data_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_data_dict`;
CREATE TABLE `sys_data_dict`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典名称',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典编码',
  `sort` int(10) NULL DEFAULT 999 COMMENT '排序',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_data_dict
-- ----------------------------

-- ----------------------------
-- Table structure for sys_data_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_data_dict_item`;
CREATE TABLE `sys_data_dict_item`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典项',
  `data_dict_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典id',
  `sort` int(10) NULL DEFAULT 999 COMMENT '排序',
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '值',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_data_dict_item
-- ----------------------------

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限名称',
  `pid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上级ID',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由地址',
  `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '重定向地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '页面组件地址',
  `auth_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  `type` int(1) NULL DEFAULT NULL COMMENT '权限类型 1目录 2菜单 3按钮',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `status` int(1) NULL DEFAULT NULL COMMENT '状态 0禁用 1启用',
  `affix` int(1) NULL DEFAULT NULL COMMENT '是否固定标签页 0否 1是',
  `keep_alive` int(1) NULL DEFAULT NULL COMMENT '是否缓存页面 0否 1是',
  `hidden` int(1) NULL DEFAULT NULL COMMENT '是否隐藏 0否 1是',
  `sort` int(10) NULL DEFAULT NULL COMMENT '排序',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标识',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES ('2016760834855903233', '系统管理', '0', 'sys', '/sys/permission', '', '', 1, 'SettingOutlined', 1, 0, 0, 0, 999, NULL, NULL, '2026-02-06 10:47:51', '2018951326683222017', 0);
INSERT INTO `sys_permission` VALUES ('2016761672542298113', '权限管理', '2016760834855903233', 'permission', '', '/system/permission/Index', '', 2, 'MenuOutlined', 1, 0, 1, 0, 999, NULL, NULL, '2026-02-06 11:04:23', '2018951326683222017', 0);
INSERT INTO `sys_permission` VALUES ('2018219316041859074', '角色管理', '2016760834855903233', 'role', '', '/system/role/Index', '', 2, 'UsergroupAddOutlined', 1, 0, 1, 0, 999, '2026-02-02 15:05:56', '2', '2026-02-02 15:05:56', '2', 0);
INSERT INTO `sys_permission` VALUES ('2018576659606765570', '用户管理', '2016760834855903233', 'user', '', '/system/user/Index', '', 2, 'UserOutlined', 1, 0, 1, 0, 998, '2026-02-03 14:45:54', '2', '2026-02-07 10:43:04', '2018951326683222017', 0);
INSERT INTO `sys_permission` VALUES ('2019206550819823617', '新增用户', '2018576659606765570', '', '', '', 'sys:user:add', 3, '', 1, 0, 1, 0, 999, '2026-02-05 08:28:51', '2018951326683222017', '2026-02-05 08:28:51', '2018951326683222017', 0);
INSERT INTO `sys_permission` VALUES ('2019206643719462914', '编辑用户', '2018576659606765570', '', '', '', 'sys:user:edit', 3, '', 1, 0, 1, 0, 999, '2026-02-05 08:29:14', '2018951326683222017', '2026-02-05 08:29:14', '2018951326683222017', 0);
INSERT INTO `sys_permission` VALUES ('2019206785314971650', '删除用户', '2018576659606765570', '', '', '', 'sys:user:delete', 3, '', 1, 0, 1, 0, 999, '2026-02-05 08:29:47', '2018951326683222017', '2026-02-05 08:29:47', '2018951326683222017', 0);
INSERT INTO `sys_permission` VALUES ('2019206947751976961', '重置密码', '2018576659606765570', '', '', '', 'sys:user:resetpwd', 3, '', 1, 0, 1, 0, 999, '2026-02-05 08:30:26', '2018951326683222017', '2026-02-05 08:31:52', '2018951326683222017', 0);
INSERT INTO `sys_permission` VALUES ('2019237244690255874', '数据字典', '2016760834855903233', 'data-dict', '', '/system/data-dict/Index', '', 2, 'BookOutlined', 1, 0, 1, 0, 999, '2026-02-05 10:30:49', '2018951326683222017', '2026-02-05 10:30:49', '2018951326683222017', 0);
INSERT INTO `sys_permission` VALUES ('2019594820342784001', '首页', '0', 'home', '', '/Home', '', 2, 'HomeOutlined', 1, 0, 0, 0, 1, '2026-02-06 10:11:42', '2018951326683222017', '2026-02-06 10:11:52', '2018951326683222017', 0);
INSERT INTO `sys_permission` VALUES ('2019634140013416450', '系统设置', '2016760834855903233', 'setting', '', '/system/setting/Index', '', 2, 'SettingOutlined', 1, 0, 0, 0, 999, '2026-02-06 12:47:57', '2018951326683222017', '2026-02-06 12:53:17', '2018951326683222017', 0);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '名称',
  `code` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色编码',
  `description` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `role_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色ID',
  `permission_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限ID',
  UNIQUE INDEX `unique`(`role_id`, `permission_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `username` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `nickname` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `password` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码MD5',
  `phone` varchar(13) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `sex` tinyint(1) NULL DEFAULT 1 COMMENT '性别 1男 2女',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 1正常 2禁用',
  `identity` tinyint(1) NULL DEFAULT 2 COMMENT '身份 1超管 2普通用户',
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES ('2018951326683222017', 'admin', '管理员', '$2a$12$rcUyivY/DvjYVC5KAz.HIu25fXSc7D4hQy88yPMtXCRkBXsp99hFq', '15558905171', 1, 1, 1, '20260204/nexus-logo_1770190479042.png', '2026-02-04 15:34:41', '2', '2026-02-04 15:34:52', '2', 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `role_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
  UNIQUE INDEX `unique`(`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
