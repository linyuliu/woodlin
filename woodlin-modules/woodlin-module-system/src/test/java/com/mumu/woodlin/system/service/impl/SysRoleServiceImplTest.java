package com.mumu.woodlin.system.service.impl;

import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.system.entity.SysRole;
import com.mumu.woodlin.system.entity.SysRolePermission;
import com.mumu.woodlin.system.mapper.SysPermissionMapper;
import com.mumu.woodlin.system.mapper.SysRoleHierarchyMapper;
import com.mumu.woodlin.system.mapper.SysRoleInheritedPermissionMapper;
import com.mumu.woodlin.system.mapper.SysRoleMapper;
import com.mumu.woodlin.system.mapper.SysRolePermissionMapper;
import com.mumu.woodlin.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SysRoleServiceImpl 关键行为测试
 *
 * @author mumu
 * @since 2026-03-15
 */
class SysRoleServiceImplTest {

    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysPermissionMapper permissionMapper;
    @Mock
    private SysRolePermissionMapper rolePermissionMapper;
    @Mock
    private SysRoleHierarchyMapper hierarchyMapper;
    @Mock
    private SysRoleInheritedPermissionMapper inheritedPermissionMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;

    private SysRoleServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = Mockito.spy(new SysRoleServiceImpl(
            roleMapper,
            permissionMapper,
            rolePermissionMapper,
            hierarchyMapper,
            inheritedPermissionMapper,
            userRoleMapper
        ));
    }

    @Test
    void assignRolePermissions_shouldThrow_whenSuperAdminRole() {
        SysRole superAdminRole = new SysRole();
        superAdminRole.setRoleId(1L);
        superAdminRole.setRoleCode("admin");
        doReturn(superAdminRole).when(service).getById(1L);

        assertThrows(BusinessException.class, () -> service.assignRolePermissions(1L, List.of(100L)));
        verify(rolePermissionMapper, never()).deleteByRoleId(anyLong());
        verify(rolePermissionMapper, never()).batchInsert(any());
    }

    @Test
    void assignRolePermissions_shouldThrow_whenLegacySuperAdminRoleCode() {
        SysRole superAdminRole = new SysRole();
        superAdminRole.setRoleId(9L);
        superAdminRole.setRoleCode("super_admin");
        doReturn(superAdminRole).when(service).getById(9L);

        assertThrows(BusinessException.class, () -> service.assignRolePermissions(9L, List.of(100L)));
        verify(rolePermissionMapper, never()).deleteByRoleId(anyLong());
        verify(rolePermissionMapper, never()).batchInsert(any());
    }

    @Test
    void deleteRoleByIds_shouldThrow_whenContainsSuperAdminRole() {
        SysRole superAdminRole = new SysRole();
        superAdminRole.setRoleId(1L);
        superAdminRole.setRoleCode("admin");
        doReturn(superAdminRole).when(service).getById(1L);

        assertThrows(BusinessException.class, () -> service.deleteRoleByIds(List.of(1L, 2L)));
        verify(roleMapper, never()).selectDescendantRoles(anyLong());
    }

    @Test
    void updateRole_shouldThrow_whenSuperAdminRoleCodeChanged() {
        SysRole existingRole = new SysRole()
            .setRoleId(1L)
            .setRoleCode("admin")
            .setStatus("1")
            .setParentRoleId(0L);
        doReturn(existingRole).when(service).getById(1L);

        SysRole input = new SysRole()
            .setRoleId(1L)
            .setRoleCode("admin_new")
            .setStatus("1")
            .setParentRoleId(0L);

        assertThrows(BusinessException.class, () -> service.updateRole(input));
    }

    @Test
    void updateRole_shouldThrow_whenSuperAdminRoleDisabled() {
        SysRole existingRole = new SysRole()
            .setRoleId(1L)
            .setRoleCode("admin")
            .setStatus("1")
            .setParentRoleId(0L);
        doReturn(existingRole).when(service).getById(1L);

        SysRole input = new SysRole()
            .setRoleId(1L)
            .setRoleCode("admin")
            .setStatus("0")
            .setParentRoleId(0L);

        assertThrows(BusinessException.class, () -> service.updateRole(input));
    }

    @Test
    void updateRole_shouldThrow_whenSuperAdminRoleParentChanged() {
        SysRole existingRole = new SysRole()
            .setRoleId(1L)
            .setRoleCode("admin")
            .setStatus("1")
            .setParentRoleId(0L);
        doReturn(existingRole).when(service).getById(1L);

        SysRole input = new SysRole()
            .setRoleId(1L)
            .setRoleCode("admin")
            .setStatus("1")
            .setParentRoleId(2L);

        assertThrows(BusinessException.class, () -> service.updateRole(input));
    }

    @SuppressWarnings("unchecked")
    @Test
    void assignRolePermissions_shouldReplaceAndRefresh_whenNormalRole() {
        SysRole normalRole = new SysRole();
        normalRole.setRoleId(2L);
        normalRole.setRoleCode("common");
        doReturn(normalRole).when(service).getById(2L);

        SysRole descendantA = new SysRole().setRoleId(21L);
        SysRole descendantB = new SysRole().setRoleId(22L);
        when(roleMapper.selectDescendantRoles(2L)).thenReturn(List.of(descendantA, descendantB));

        boolean result = service.assignRolePermissions(2L, Arrays.asList(100L, 101L, 100L, null));

        assertTrue(result);
        verify(rolePermissionMapper, times(1)).deleteByRoleId(2L);
        verify(rolePermissionMapper, times(1)).batchInsert(any());

        ArgumentCaptor<List<SysRolePermission>> captor = ArgumentCaptor.forClass(List.class);
        verify(rolePermissionMapper).batchInsert(captor.capture());
        Set<Long> permissionIds = captor.getValue().stream()
            .map(SysRolePermission::getPermissionId)
            .collect(Collectors.toSet());
        assertEquals(Set.of(100L, 101L), permissionIds);

        // 当前角色 + 两个后代角色，共刷新3次
        verify(inheritedPermissionMapper, times(3)).refreshInheritedPermissions(anyLong());
    }
}
