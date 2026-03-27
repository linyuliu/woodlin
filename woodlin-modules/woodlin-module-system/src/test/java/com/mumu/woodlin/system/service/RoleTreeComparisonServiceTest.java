package com.mumu.woodlin.system.service;

import com.mumu.woodlin.system.dto.RoleTreeDTO;
import com.mumu.woodlin.system.entity.SysRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RoleTreeComparisonService 单元测试
 *
 * @author mumu
 * @since 2025-01-04
 */
class RoleTreeComparisonServiceTest {

    private RoleTreeComparisonService service;
    private List<SysRole> allRoles;

    @BeforeEach
    void setUp() {
        service = new RoleTreeComparisonService();
        allRoles = createTestRoles();
    }

    /**
     * 创建测试角色树结构:
     * 1 (admin) - role_path: /1/
     *   ├─ 2 (manager) - role_path: /1/2/
     *   │   └─ 4 (team_lead) - role_path: /1/2/4/
     *   └─ 3 (developer) - role_path: /1/3/
     */
    private List<SysRole> createTestRoles() {
        List<SysRole> roles = new ArrayList<>();

        // Root role: admin
        SysRole admin = new SysRole();
        admin.setRoleId(1L);
        admin.setRoleName("管理员");
        admin.setRoleCode("admin");
        admin.setParentRoleId(null);
        admin.setRoleLevel(0);
        admin.setRolePath("/1/");
        roles.add(admin);

        // Second level: manager
        SysRole manager = new SysRole();
        manager.setRoleId(2L);
        manager.setRoleName("经理");
        manager.setRoleCode("manager");
        manager.setParentRoleId(1L);
        manager.setRoleLevel(1);
        manager.setRolePath("/1/2/");
        roles.add(manager);

        // Second level: developer
        SysRole developer = new SysRole();
        developer.setRoleId(3L);
        developer.setRoleName("开发人员");
        developer.setRoleCode("developer");
        developer.setParentRoleId(1L);
        developer.setRoleLevel(1);
        developer.setRolePath("/1/3/");
        roles.add(developer);

        // Third level: team_lead
        SysRole teamLead = new SysRole();
        teamLead.setRoleId(4L);
        teamLead.setRoleName("组长");
        teamLead.setRoleCode("team_lead");
        teamLead.setParentRoleId(2L);
        teamLead.setRoleLevel(2);
        teamLead.setRolePath("/1/2/4/");
        roles.add(teamLead);

        return roles;
    }

    @Test
    void testCompareByPath_AddNewRole() {
        // 用户当前没有角色
        List<SysRole> currentRoles = new ArrayList<>();
        
        // 分配新角色: team_lead (4)
        // 应该自动包含其祖先角色: admin (1), manager (2)
        List<Long> newRoleIds = Arrays.asList(4L);
        
        RoleTreeComparisonService.ComparisonResult result = 
            service.compareByPath(currentRoles, newRoleIds, allRoles);
        
        assertTrue(result.hasChanges());
        assertEquals(3, result.getAddedRoleIds().size());
        assertTrue(result.getAddedRoleIds().contains(1L)); // admin
        assertTrue(result.getAddedRoleIds().contains(2L)); // manager
        assertTrue(result.getAddedRoleIds().contains(4L)); // team_lead
        assertEquals(0, result.getRemovedRoleIds().size());
        assertEquals(0, result.getUnchangedRoleIds().size());
    }

    @Test
    void testCompareByPath_RemoveRole() {
        // 用户当前有角色: admin, manager
        List<SysRole> currentRoles = Arrays.asList(
            getRoleById(1L),
            getRoleById(2L)
        );
        
        // 新分配空角色列表 (移除所有角色)
        List<Long> newRoleIds = new ArrayList<>();
        
        RoleTreeComparisonService.ComparisonResult result = 
            service.compareByPath(currentRoles, newRoleIds, allRoles);
        
        assertTrue(result.hasChanges());
        assertEquals(0, result.getAddedRoleIds().size());
        assertEquals(2, result.getRemovedRoleIds().size());
        assertTrue(result.getRemovedRoleIds().contains(1L));
        assertTrue(result.getRemovedRoleIds().contains(2L));
        assertEquals(0, result.getUnchangedRoleIds().size());
    }

    @Test
    void testCompareByPath_NoChanges() {
        // 用户当前有角色: admin, developer
        List<SysRole> currentRoles = Arrays.asList(
            getRoleById(1L),
            getRoleById(3L)
        );
        
        // 新分配相同角色: developer (会自动包含 admin)
        List<Long> newRoleIds = Arrays.asList(3L);
        
        RoleTreeComparisonService.ComparisonResult result = 
            service.compareByPath(currentRoles, newRoleIds, allRoles);
        
        assertFalse(result.hasChanges());
        assertEquals(0, result.getAddedRoleIds().size());
        assertEquals(0, result.getRemovedRoleIds().size());
        assertEquals(2, result.getUnchangedRoleIds().size());
        assertTrue(result.getUnchangedRoleIds().contains(1L));
        assertTrue(result.getUnchangedRoleIds().contains(3L));
    }

    @Test
    void testCompareByPath_MixedChanges() {
        // 用户当前有角色: admin, developer
        List<SysRole> currentRoles = Arrays.asList(
            getRoleById(1L),
            getRoleById(3L)
        );
        
        // 新分配角色: manager (会自动包含 admin)
        // admin: 不变
        // developer: 删除
        // manager: 新增
        List<Long> newRoleIds = Arrays.asList(2L);
        
        RoleTreeComparisonService.ComparisonResult result = 
            service.compareByPath(currentRoles, newRoleIds, allRoles);
        
        assertTrue(result.hasChanges());
        assertEquals(1, result.getAddedRoleIds().size());
        assertTrue(result.getAddedRoleIds().contains(2L)); // manager
        assertEquals(1, result.getRemovedRoleIds().size());
        assertTrue(result.getRemovedRoleIds().contains(3L)); // developer
        assertEquals(1, result.getUnchangedRoleIds().size());
        assertTrue(result.getUnchangedRoleIds().contains(1L)); // admin
    }

    @Test
    void testCompareBySet_AddNewRole() {
        // 用户当前没有角色
        List<SysRole> currentRoles = new ArrayList<>();
        
        // 分配新角色: team_lead (4)
        List<Long> newRoleIds = Arrays.asList(4L);
        
        RoleTreeComparisonService.ComparisonResult result = 
            service.compareBySet(currentRoles, newRoleIds, allRoles);
        
        assertTrue(result.hasChanges());
        assertEquals(3, result.getAddedRoleIds().size());
        assertTrue(result.getAddedRoleIds().contains(1L)); // admin
        assertTrue(result.getAddedRoleIds().contains(2L)); // manager
        assertTrue(result.getAddedRoleIds().contains(4L)); // team_lead
    }

    @Test
    void testBuildRoleTreeWithSelection() {
        // 用户拥有的角色
        List<Long> userRoleIds = Arrays.asList(2L, 4L);
        
        List<RoleTreeDTO> tree = service.buildRoleTreeWithSelection(allRoles, userRoleIds);
        
        assertNotNull(tree);
        assertEquals(1, tree.size()); // 只有一个根节点 (admin)
        
        RoleTreeDTO admin = tree.get(0);
        assertEquals(1L, admin.getRoleId());
        assertTrue(admin.getHasChildren());
        assertEquals(2, admin.getChildren().size());
        
        // 检查 manager 节点
        RoleTreeDTO manager = admin.getChildren().stream()
            .filter(r -> r.getRoleId().equals(2L))
            .findFirst()
            .orElse(null);
        assertNotNull(manager);
        assertTrue(manager.getHasChildren());
        assertEquals(1, manager.getChildren().size());
        
        // 检查 team_lead 节点
        RoleTreeDTO teamLead = manager.getChildren().get(0);
        assertEquals(4L, teamLead.getRoleId());
        assertFalse(teamLead.getHasChildren());
    }

    @Test
    void testBuildRoleTreeWithSelection_EmptyRoles() {
        List<RoleTreeDTO> tree = service.buildRoleTreeWithSelection(new ArrayList<>(), new ArrayList<>());
        assertNotNull(tree);
        assertTrue(tree.isEmpty());
    }

    @Test
    void testCompareByPath_WithMultipleNewRoles() {
        // 用户当前没有角色
        List<SysRole> currentRoles = new ArrayList<>();
        
        // 分配多个新角色: manager (2) 和 developer (3)
        // 应该自动包含 admin (1)
        List<Long> newRoleIds = Arrays.asList(2L, 3L);
        
        RoleTreeComparisonService.ComparisonResult result = 
            service.compareByPath(currentRoles, newRoleIds, allRoles);
        
        assertTrue(result.hasChanges());
        assertEquals(3, result.getAddedRoleIds().size());
        assertTrue(result.getAddedRoleIds().contains(1L)); // admin
        assertTrue(result.getAddedRoleIds().contains(2L)); // manager
        assertTrue(result.getAddedRoleIds().contains(3L)); // developer
    }

    @Test
    void testCompareBySet_WithMultipleNewRoles() {
        // 用户当前有角色: admin
        List<SysRole> currentRoles = Arrays.asList(getRoleById(1L));
        
        // 分配多个新角色: manager, developer, team_lead
        List<Long> newRoleIds = Arrays.asList(2L, 3L, 4L);
        
        RoleTreeComparisonService.ComparisonResult result = 
            service.compareBySet(currentRoles, newRoleIds, allRoles);
        
        assertTrue(result.hasChanges());
        assertEquals(3, result.getAddedRoleIds().size());
        assertTrue(result.getAddedRoleIds().contains(2L));
        assertTrue(result.getAddedRoleIds().contains(3L));
        assertTrue(result.getAddedRoleIds().contains(4L));
        assertEquals(0, result.getRemovedRoleIds().size());
        assertEquals(1, result.getUnchangedRoleIds().size());
        assertTrue(result.getUnchangedRoleIds().contains(1L));
    }

    private SysRole getRoleById(Long roleId) {
        return allRoles.stream()
            .filter(r -> r.getRoleId().equals(roleId))
            .findFirst()
            .orElse(null);
    }
}
