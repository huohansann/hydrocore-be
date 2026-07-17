-- Historical migration only: removes obsolete menus/roles from old installations.
-- This file is not part of the fresh HydroCore baseline capability set.
-- HydroCore phase-1 menu migration
-- Run against the existing MySQL database when ready (idempotent where noted).
-- DDL reference: db/schema.sql (sys_menu, sys_role_menu, sys_role)
--
-- menu_code MUST match frontend RouteName (vue-router name):
--   overview, trends, system, system.user, system.menu, system.org,
--   system.role, system.points, system.realtime, system.config, system.debug
--
-- Admin role: seed uses id = 1 (code ADMIN). Override if needed:
--   SELECT id, name, code FROM sys_role WHERE deleted = 0;

SET NAMES utf8mb4;
SET @admin_role_id := 1;

-- ---------------------------------------------------------------------------
-- 1) Detach roles from obsolete menus (kiln + removed FE pages)
-- ---------------------------------------------------------------------------
DELETE FROM sys_role_menu
WHERE menu_id IN (
  SELECT id FROM (
    SELECT id FROM sys_menu
    WHERE menu_code IN (
      'yaoluyuce', 'wenduyuce', 'yaolujiance', 'yaolukongzhi',
      'moxingshezhi', 'kongzhishezhi',
      'nenghaofenxi', 'shebeifenxi', 'lianglvfenxi',
      'xitongshezhi'
    )
    OR menu_url LIKE '%yaolu%'
    OR menu_name LIKE '%窑炉%'
  ) AS obsolete_menu_ids
);

-- ---------------------------------------------------------------------------
-- 2) Delete obsolete menus
-- ---------------------------------------------------------------------------
DELETE FROM sys_menu
WHERE menu_code IN (
  'yaoluyuce', 'wenduyuce', 'yaolujiance', 'yaolukongzhi',
  'moxingshezhi', 'kongzhishezhi',
  'nenghaofenxi', 'shebeifenxi', 'lianglvfenxi',
  'xitongshezhi'
)
OR menu_url LIKE '%yaolu%'
OR menu_name LIKE '%窑炉%';

-- ---------------------------------------------------------------------------
-- 3) Remove kiln-only roles (keep ADMIN)
-- ---------------------------------------------------------------------------
DELETE FROM sys_user_role
WHERE role_id IN (SELECT id FROM (
  SELECT id FROM sys_role WHERE code IN ('MXKZ', 'KZSZ', 'YLKZ') OR name LIKE '%窑炉%'
) AS kiln_roles);

DELETE FROM sys_role_menu
WHERE role_id IN (SELECT id FROM (
  SELECT id FROM sys_role WHERE code IN ('MXKZ', 'KZSZ', 'YLKZ') OR name LIKE '%窑炉%'
) AS kiln_roles);

DELETE FROM sys_role
WHERE code IN ('MXKZ', 'KZSZ', 'YLKZ') OR name LIKE '%窑炉%';

-- ---------------------------------------------------------------------------
-- 4) Insert HydroCore menus (skip if menu_code already exists)
-- ---------------------------------------------------------------------------
INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_url, type, menu_icon, model_show, status, deleted)
SELECT 0, 'overview', '总览', '/overview', 1, 'tabler:layout-dashboard', 1, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_code = 'overview' AND deleted = 0);

INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_url, type, menu_icon, model_show, status, deleted)
SELECT 0, 'trends', '趋势', '/trends', 1, 'tabler:chart-line', 1, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_code = 'trends' AND deleted = 0);

INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_url, type, menu_icon, model_show, status, deleted)
SELECT 0, 'system', '系统管理', '/system', 0, 'tabler:settings', 1, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_code = 'system' AND deleted = 0);

SET @system_menu_id := (SELECT id FROM sys_menu WHERE menu_code = 'system' AND deleted = 0 LIMIT 1);

INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_url, type, menu_icon, model_show, status, deleted)
SELECT @system_menu_id, 'system.user', '用户管理', '/system/user', 1, 'tabler:user', 1, 1, 0
FROM DUAL WHERE @system_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_code = 'system.user' AND deleted = 0);

INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_url, type, menu_icon, model_show, status, deleted)
SELECT @system_menu_id, 'system.role', '角色管理', '/system/role', 1, 'tabler:shield', 1, 1, 0
FROM DUAL WHERE @system_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_code = 'system.role' AND deleted = 0);

INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_url, type, menu_icon, model_show, status, deleted)
SELECT @system_menu_id, 'system.menu', '菜单管理', '/system/menu', 1, 'tabler:menu-2', 1, 1, 0
FROM DUAL WHERE @system_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_code = 'system.menu' AND deleted = 0);

INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_url, type, menu_icon, model_show, status, deleted)
SELECT @system_menu_id, 'system.org', '组织管理', '/system/org', 1, 'tabler:building', 1, 1, 0
FROM DUAL WHERE @system_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_code = 'system.org' AND deleted = 0);

INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_url, type, menu_icon, model_show, status, deleted)
SELECT @system_menu_id, 'system.points', '点位映射', '/system/points', 1, 'tabler:topology-star', 1, 1, 0
FROM DUAL WHERE @system_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_code = 'system.points' AND deleted = 0);

INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_url, type, menu_icon, model_show, status, deleted)
SELECT @system_menu_id, 'system.realtime', '实时查询', '/system/realtime', 1, 'tabler:activity', 1, 1, 0
FROM DUAL WHERE @system_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_code = 'system.realtime' AND deleted = 0);

INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_url, type, menu_icon, model_show, status, deleted)
SELECT @system_menu_id, 'system.config', '系统配置', '/system/config', 1, 'tabler:adjustments', 1, 1, 0
FROM DUAL WHERE @system_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_code = 'system.config' AND deleted = 0);

INSERT INTO sys_menu (parent_id, menu_code, menu_name, menu_url, type, menu_icon, model_show, status, deleted)
SELECT @system_menu_id, 'system.debug', '调试', '/system/debug', 1, 'tabler:bug', 1, 1, 0
FROM DUAL WHERE @system_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_code = 'system.debug' AND deleted = 0);

-- ---------------------------------------------------------------------------
-- 5) Grant all HydroCore menus to admin
-- ---------------------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, m.id
FROM sys_menu m
WHERE m.deleted = 0
  AND m.menu_code IN (
    'overview', 'trends', 'system',
    'system.user', 'system.role', 'system.menu', 'system.org',
    'system.points', 'system.realtime', 'system.config', 'system.debug'
  )
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = @admin_role_id AND rm.menu_id = m.id
  );

-- ---------------------------------------------------------------------------
-- 6) Sanity check (optional — run manually)
-- SELECT menu_code, menu_name, menu_url, parent_id FROM sys_menu WHERE deleted = 0 ORDER BY id;
-- SELECT r.code, m.menu_code FROM sys_role_menu rm
--   JOIN sys_role r ON r.id = rm.role_id
--   JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE r.id = @admin_role_id;
