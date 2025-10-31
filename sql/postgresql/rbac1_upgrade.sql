-- =============================================
-- PostgreSQL version - Auto-converted from MySQL
-- Source: rbac1_upgrade.sql
-- Database: PostgreSQL 12+
-- =============================================

-- =============================================
-- RBAC1 Upgrade - 角色层次结构升级脚本
-- 作者: Copilot
-- 描述: 将 RBAC0 升级到 RBAC1，添加角色继承功能
-- 版本: 1.0.0
-- 时间: 2025-10-31
-- =============================================

-- \c woodlin;

-- =============================================
-- 1. 为 sys_role 表添加父角色支持
-- =============================================
ALTER TABLE sys_role 
ADD COLUMN parent_role_id bigint(20) DEFAULT NULL COMMENT '父角色ID（用于角色继承）' AFTER role_id,
ADD COLUMN role_level int(11) DEFAULT 0 COMMENT '角色层级（0为顶级角色）' AFTER parent_role_id,
ADD COLUMN role_path varchar(500) DEFAULT '' COMMENT '角色路径（用于快速查找祖先角色）' AFTER role_level,
ADD COLUMN is_inheritable char(1) DEFAULT '1' COMMENT '是否可继承（1-是，0-否）' AFTER data_scope;

-- 添加索引以优化角色继承查询
ALTER TABLE sys_role
ADD KEY idx_parent_role_id (parent_role_id),
ADD KEY idx_role_level (role_level);

-- =============================================
-- 2. 创建角色继承关系表（用于快速查询完整的继承链）
-- =============================================
DROP TABLE IF EXISTS sys_role_hierarchy CASCADE;
CREATE TABLE sys_role_hierarchy (
    ancestor_role_id bigint(20) NOT NULL COMMENT '祖先角色ID',
    descendant_role_id bigint(20) NOT NULL COMMENT '后代角色ID',
    distance int(11) NOT NULL DEFAULT 0 COMMENT '层级距离（0表示自己）',
    tenant_id varchar(64) DEFAULT NULL COMMENT '租户ID',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (ancestor_role_id, descendant_role_id),
    KEY idx_descendant (descendant_role_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_distance (distance)
) -- Comment: 角色继承层次关系表（闭包表）;

-- =============================================
-- 3. 初始化现有角色的层次关系（每个角色指向自己，距离为0）
-- =============================================
INSERT INTO sys_role_hierarchy (ancestor_role_id, descendant_role_id, distance, tenant_id)
SELECT role_id, role_id, 0, tenant_id
FROM sys_role
WHERE deleted = '0';

-- =============================================
-- 4. 创建角色继承权限缓存表（可选，用于性能优化）
-- =============================================
DROP TABLE IF EXISTS sys_role_inherited_permission CASCADE;
CREATE TABLE sys_role_inherited_permission (
    role_id bigint(20) NOT NULL COMMENT '角色ID',
    permission_id bigint(20) NOT NULL COMMENT '权限ID（包括继承的）',
    is_inherited char(1) DEFAULT '0' COMMENT '是否继承而来（0-直接拥有，1-继承获得）',
    inherited_from bigint(20) DEFAULT NULL COMMENT '继承自哪个角色',
    tenant_id varchar(64) DEFAULT NULL COMMENT '租户ID',
    update_time datetime DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
    PRIMARY KEY (role_id, permission_id),
    KEY idx_permission_id (permission_id),
    KEY idx_inherited_from (inherited_from),
    KEY idx_tenant_id (tenant_id)
) -- Comment: 角色继承权限缓存表;

-- =============================================
-- 5. 初始化继承权限缓存（仅包含直接权限）
-- =============================================
INSERT INTO sys_role_inherited_permission (role_id, permission_id, is_inherited, tenant_id)
SELECT rp.role_id, rp.permission_id, '0', r.tenant_id
FROM sys_role_permission rp
INNER JOIN sys_role r ON rp.role_id = r.role_id
WHERE r.deleted = '0';

-- =============================================
-- 6. 添加角色层次约束的触发器（MySQL 8.0+）
-- =============================================

-- 防止循环依赖的触发器（在实际插入/更新前检查）
-- 注意：MySQL触发器不能修改同一个表，所以循环检查需要在应用层实现
-- 这里提供一个存储过程供应用层调用

DROP PROCEDURE IF EXISTS check_role_hierarchy_cycle;

DELIMITER $$
CREATE PROCEDURE check_role_hierarchy_cycle(
    IN p_role_id BIGINT,
    IN p_parent_role_id BIGINT,
    OUT p_has_cycle INT
)
BEGIN
    DECLARE v_count INT DEFAULT 0;
    
    -- 检查父角色是否是当前角色的后代
    SELECT COUNT(*) INTO v_count
    FROM sys_role_hierarchy
    WHERE ancestor_role_id = p_role_id 
      AND descendant_role_id = p_parent_role_id;
    
    IF v_count > 0 THEN
        SET p_has_cycle = 1;
    ELSE
        SET p_has_cycle = 0;
    END IF;
END$$

DELIMITER ;

-- =============================================
-- 7. 创建刷新角色继承关系的存储过程
-- =============================================

DROP PROCEDURE IF EXISTS refresh_role_hierarchy;

DELIMITER $$
CREATE PROCEDURE refresh_role_hierarchy(
    IN p_role_id BIGINT
)
BEGIN
    DECLARE v_tenant_id VARCHAR(64);
    
    -- 获取角色的租户ID
    SELECT tenant_id INTO v_tenant_id FROM sys_role WHERE role_id = p_role_id;
    
    -- 删除该角色相关的继承关系（除了自己）
    DELETE FROM sys_role_hierarchy
    WHERE descendant_role_id = p_role_id AND distance > 0;
    
    -- 重新构建继承关系
    INSERT INTO sys_role_hierarchy (ancestor_role_id, descendant_role_id, distance, tenant_id)
    WITH RECURSIVE role_ancestors AS (
        -- 基础情况：直接父角色
        SELECT 
            r1.parent_role_id AS ancestor_role_id,
            r1.role_id AS descendant_role_id,
            1 AS distance,
            r1.tenant_id
        FROM sys_role r1
        WHERE r1.role_id = p_role_id
          AND r1.parent_role_id IS NOT NULL
          AND r1.deleted = '0'
        
        UNION ALL
        
        -- 递归情况：祖先角色
        SELECT 
            r2.parent_role_id AS ancestor_role_id,
            ra.descendant_role_id,
            ra.distance + 1 AS distance,
            r2.tenant_id
        FROM role_ancestors ra
        INNER JOIN sys_role r2 ON ra.ancestor_role_id = r2.role_id
        WHERE r2.parent_role_id IS NOT NULL
          AND r2.deleted = '0'
          AND ra.distance < 10  -- 防止无限递归，最多10层
    )
    SELECT * FROM role_ancestors;
    
    -- 刷新继承权限缓存
    CALL refresh_role_inherited_permissions(p_role_id);
END$$

DELIMITER ;

-- =============================================
-- 8. 创建刷新角色继承权限的存储过程
-- =============================================

DROP PROCEDURE IF EXISTS refresh_role_inherited_permissions;

DELIMITER $$
CREATE PROCEDURE refresh_role_inherited_permissions(
    IN p_role_id BIGINT
)
BEGIN
    DECLARE v_tenant_id VARCHAR(64);
    
    -- 获取角色的租户ID
    SELECT tenant_id INTO v_tenant_id FROM sys_role WHERE role_id = p_role_id;
    
    -- 删除该角色的所有继承权限缓存
    DELETE FROM sys_role_inherited_permission
    WHERE role_id = p_role_id;
    
    -- 重新构建继承权限（包括直接权限和继承权限）
    INSERT INTO sys_role_inherited_permission (role_id, permission_id, is_inherited, inherited_from, tenant_id)
    -- 直接权限
    SELECT 
        p_role_id,
        rp.permission_id,
        '0',
        NULL,
        v_tenant_id
    FROM sys_role_permission rp
    WHERE rp.role_id = p_role_id
    
    UNION
    
    -- 继承权限
    SELECT DISTINCT
        p_role_id,
        rp.permission_id,
        '1',
        rp.role_id,
        v_tenant_id
    FROM sys_role_hierarchy rh
    INNER JOIN sys_role_permission rp ON rh.ancestor_role_id = rp.role_id
    INNER JOIN sys_role r ON rh.ancestor_role_id = r.role_id
    WHERE rh.descendant_role_id = p_role_id
      AND rh.distance > 0
      AND r.is_inheritable = '1'
      AND r.deleted = '0';
END$$

DELIMITER ;

-- =============================================
-- 9. 更新已有角色的路径信息
-- =============================================
-- 初始化所有角色的路径为自己的ID（顶级角色）
UPDATE sys_role 
SET role_path = CONCAT('/', role_id, '/')
WHERE parent_role_id IS NULL OR parent_role_id = 0;

-- =============================================
-- 备注说明
-- =============================================
-- 1. parent_role_id: 指向父角色，NULL表示顶级角色
-- 2. role_level: 角色在层次结构中的级别，0为顶级
-- 3. role_path: 形如 "/1/2/3/" 的路径，便于快速查找所有祖先
-- 4. is_inheritable: 控制该角色的权限是否可以被子角色继承
-- 5. sys_role_hierarchy: 使用闭包表模式存储角色继承的完整层次关系
-- 6. sys_role_inherited_permission: 缓存表，提升查询性能

-- =============================================
-- 兼容性说明
-- =============================================
-- 1. 现有角色自动初始化为顶级角色（parent_role_id = NULL）
-- 2. 所有已有权限关系保持不变
-- 3. 向后兼容RBAC0，不设置父角色时行为与原来完全一致
-- 4. 多租户隔离通过tenant_id保证，每个租户的角色层次独立

-- =============================================
-- 使用建议
-- =============================================
-- 1. 在Java应用层实现循环依赖检查
-- 2. 角色关系变更时，调用 refresh_role_hierarchy 存储过程
-- 3. 定期刷新 sys_role_inherited_permission 缓存表
-- 4. 考虑在应用层使用Redis缓存权限信息以提升性能
