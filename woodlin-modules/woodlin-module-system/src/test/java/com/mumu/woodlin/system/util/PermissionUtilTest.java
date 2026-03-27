package com.mumu.woodlin.system.util;

import com.mumu.woodlin.system.entity.SysPermission;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PermissionUtil 单元测试
 *
 * @author mumu
 * @since 2025-01-04
 */
class PermissionUtilTest {

    @Test
    void testFilterMenuPermissions() {
        // 准备测试数据
        List<SysPermission> permissions = new ArrayList<>();
        
        SysPermission menu = new SysPermission();
        menu.setPermissionId(1L);
        menu.setPermissionCode("system:user");
        menu.setPermissionType("M");
        permissions.add(menu);
        
        SysPermission component = new SysPermission();
        component.setPermissionId(2L);
        component.setPermissionCode("system:user:list");
        component.setPermissionType("C");
        permissions.add(component);
        
        SysPermission button = new SysPermission();
        button.setPermissionId(3L);
        button.setPermissionCode("system:user:add");
        button.setPermissionType("F");
        permissions.add(button);
        
        // 执行测试
        List<String> menuPermissions = PermissionUtil.filterMenuPermissions(permissions);
        
        // 验证结果
        assertEquals(2, menuPermissions.size());
        assertTrue(menuPermissions.contains("system:user"));
        assertTrue(menuPermissions.contains("system:user:list"));
        assertFalse(menuPermissions.contains("system:user:add"));
    }

    @Test
    void testFilterButtonPermissions() {
        // 准备测试数据
        List<SysPermission> permissions = new ArrayList<>();
        
        SysPermission menu = new SysPermission();
        menu.setPermissionId(1L);
        menu.setPermissionCode("system:user");
        menu.setPermissionType("M");
        permissions.add(menu);
        
        SysPermission button1 = new SysPermission();
        button1.setPermissionId(2L);
        button1.setPermissionCode("system:user:add");
        button1.setPermissionType("F");
        permissions.add(button1);
        
        SysPermission button2 = new SysPermission();
        button2.setPermissionId(3L);
        button2.setPermissionCode("system:user:delete");
        button2.setPermissionType("F");
        permissions.add(button2);
        
        // 执行测试
        List<String> buttonPermissions = PermissionUtil.filterButtonPermissions(permissions);
        
        // 验证结果
        assertEquals(2, buttonPermissions.size());
        assertTrue(buttonPermissions.contains("system:user:add"));
        assertTrue(buttonPermissions.contains("system:user:delete"));
        assertFalse(buttonPermissions.contains("system:user"));
    }

    @Test
    void testGetAllPermissionCodes() {
        // 准备测试数据
        List<SysPermission> permissions = new ArrayList<>();
        
        SysPermission p1 = new SysPermission();
        p1.setPermissionId(1L);
        p1.setPermissionCode("system:user");
        p1.setPermissionType("M");
        permissions.add(p1);
        
        SysPermission p2 = new SysPermission();
        p2.setPermissionId(2L);
        p2.setPermissionCode("system:user:list");
        p2.setPermissionType("C");
        permissions.add(p2);
        
        SysPermission p3 = new SysPermission();
        p3.setPermissionId(3L);
        p3.setPermissionCode("system:user:add");
        p3.setPermissionType("F");
        permissions.add(p3);
        
        // 执行测试
        List<String> allPermissions = PermissionUtil.getAllPermissionCodes(permissions);
        
        // 验证结果
        assertEquals(3, allPermissions.size());
        assertTrue(allPermissions.contains("system:user"));
        assertTrue(allPermissions.contains("system:user:list"));
        assertTrue(allPermissions.contains("system:user:add"));
    }

    @Test
    void testCategorizePermissions() {
        // 准备测试数据
        List<SysPermission> permissions = new ArrayList<>();
        
        SysPermission menu = new SysPermission();
        menu.setPermissionId(1L);
        menu.setPermissionCode("system:user");
        menu.setPermissionType("M");
        permissions.add(menu);
        
        SysPermission component = new SysPermission();
        component.setPermissionId(2L);
        component.setPermissionCode("system:user:list");
        component.setPermissionType("C");
        permissions.add(component);
        
        SysPermission button = new SysPermission();
        button.setPermissionId(3L);
        button.setPermissionCode("system:user:add");
        button.setPermissionType("F");
        permissions.add(button);
        
        // 执行测试
        Map<String, List<String>> categorized = PermissionUtil.categorizePermissions(permissions);
        
        // 验证结果
        assertNotNull(categorized);
        assertEquals(3, categorized.size());
        
        List<String> buttonList = categorized.get(PermissionUtil.TYPE_BUTTON);
        assertEquals(1, buttonList.size());
        assertTrue(buttonList.contains("system:user:add"));
        
        List<String> menuList = categorized.get(PermissionUtil.TYPE_MENU);
        assertEquals(2, menuList.size());
        assertTrue(menuList.contains("system:user"));
        assertTrue(menuList.contains("system:user:list"));
    }

    @Test
    void testIsMenuPermission() {
        assertTrue(PermissionUtil.isMenuPermission("M"));
        assertTrue(PermissionUtil.isMenuPermission("C"));
        assertFalse(PermissionUtil.isMenuPermission("F"));
        assertFalse(PermissionUtil.isMenuPermission("X"));
    }

    @Test
    void testIsButtonPermission() {
        assertTrue(PermissionUtil.isButtonPermission("F"));
        assertFalse(PermissionUtil.isButtonPermission("M"));
        assertFalse(PermissionUtil.isButtonPermission("C"));
        assertFalse(PermissionUtil.isButtonPermission("X"));
    }

    @Test
    void testFilterMenuPermissionsWithEmptyList() {
        List<String> result = PermissionUtil.filterMenuPermissions(new ArrayList<>());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterMenuPermissionsWithNull() {
        List<String> result = PermissionUtil.filterMenuPermissions(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterMenuPermissionsWithBlankCode() {
        List<SysPermission> permissions = new ArrayList<>();
        
        SysPermission p1 = new SysPermission();
        p1.setPermissionId(1L);
        p1.setPermissionCode("");
        p1.setPermissionType("M");
        permissions.add(p1);
        
        SysPermission p2 = new SysPermission();
        p2.setPermissionId(2L);
        p2.setPermissionCode(null);
        p2.setPermissionType("C");
        permissions.add(p2);
        
        SysPermission p3 = new SysPermission();
        p3.setPermissionId(3L);
        p3.setPermissionCode("system:user");
        p3.setPermissionType("M");
        permissions.add(p3);
        
        List<String> result = PermissionUtil.filterMenuPermissions(permissions);
        
        assertEquals(1, result.size());
        assertTrue(result.contains("system:user"));
    }

    @Test
    void testFilterButtonPermissionsWithDuplicates() {
        List<SysPermission> permissions = new ArrayList<>();
        
        SysPermission button1 = new SysPermission();
        button1.setPermissionId(1L);
        button1.setPermissionCode("system:user:add");
        button1.setPermissionType("F");
        permissions.add(button1);
        
        SysPermission button2 = new SysPermission();
        button2.setPermissionId(2L);
        button2.setPermissionCode("system:user:add");
        button2.setPermissionType("F");
        permissions.add(button2);
        
        List<String> result = PermissionUtil.filterButtonPermissions(permissions);
        
        // distinct() should remove duplicates
        assertEquals(1, result.size());
        assertTrue(result.contains("system:user:add"));
    }
}
