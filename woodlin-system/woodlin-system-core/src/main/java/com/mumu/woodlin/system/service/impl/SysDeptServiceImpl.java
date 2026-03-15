package com.mumu.woodlin.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.system.entity.SysDept;
import com.mumu.woodlin.system.entity.SysUser;
import com.mumu.woodlin.system.mapper.SysDeptMapper;
import com.mumu.woodlin.system.mapper.SysUserMapper;
import com.mumu.woodlin.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门信息服务实现类
 *
 * @author mumu
 * @description 部门管理服务实现
 * @since 2026-03-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

    @Override
    public List<SysDept> selectDeptTree(SysDept dept) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(dept.getDeptName()), SysDept::getDeptName, dept.getDeptName())
            .eq(StrUtil.isNotBlank(dept.getStatus()), SysDept::getStatus, dept.getStatus())
            .orderByAsc(SysDept::getSortOrder)
            .orderByAsc(SysDept::getDeptId);

        List<SysDept> deptList = list(wrapper);
        return buildDeptTree(deptList);
    }

    @Override
    public List<SysDept> buildDeptTree(List<SysDept> depts) {
        Map<Long, SysDept> nodeMap = new HashMap<>(depts.size());
        List<SysDept> roots = new ArrayList<>();

        for (SysDept dept : depts) {
            dept.setChildren(new ArrayList<>());
            dept.setHasChildren(false);
            nodeMap.put(dept.getDeptId(), dept);
        }

        for (SysDept dept : nodeMap.values()) {
            Long parentId = ObjectUtil.defaultIfNull(dept.getParentId(), 0L);
            if (parentId == 0L || !nodeMap.containsKey(parentId)) {
                roots.add(dept);
                continue;
            }

            SysDept parent = nodeMap.get(parentId);
            parent.getChildren().add(dept);
            parent.setHasChildren(true);
        }

        sortDeptTree(roots);
        return roots;
    }

    @Override
    public List<Long> selectDeptListByRoleId(Long roleId) {
        if (ObjectUtil.isNull(roleId)) {
            return List.of();
        }
        return deptMapper.selectDeptListByRoleId(roleId);
    }

    @Override
    public SysDept selectDeptById(Long deptId) {
        if (ObjectUtil.isNull(deptId)) {
            return null;
        }
        return getById(deptId);
    }

    @Override
    public boolean hasChildByDeptId(Long deptId) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, deptId);
        return count(wrapper) > 0;
    }

    @Override
    public boolean checkDeptExistUser(Long deptId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeptId, deptId);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean checkDeptNameUnique(SysDept dept) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, ObjectUtil.defaultIfNull(dept.getParentId(), 0L))
            .eq(SysDept::getDeptName, dept.getDeptName())
            .ne(ObjectUtil.isNotNull(dept.getDeptId()), SysDept::getDeptId, dept.getDeptId())
            .last("LIMIT 1");
        return ObjectUtil.isNull(getOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertDept(SysDept dept) {
        normalizeDeptParent(dept);

        if (!checkDeptNameUnique(dept)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "新增部门失败，部门名称已存在");
        }

        dept.setAncestors(buildAncestors(dept.getParentId()));
        return save(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDept(SysDept dept) {
        if (ObjectUtil.isNull(dept.getDeptId())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "部门ID不能为空");
        }
        if (ObjectUtil.equals(dept.getDeptId(), dept.getParentId())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "上级部门不能为自己");
        }

        normalizeDeptParent(dept);

        SysDept oldDept = selectDeptById(dept.getDeptId());
        if (ObjectUtil.isNull(oldDept)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "部门不存在");
        }

        if (!checkDeptNameUnique(dept)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "修改部门失败，部门名称已存在");
        }

        String oldAncestors = oldDept.getAncestors();
        String newAncestors = buildAncestors(dept.getParentId());
        dept.setAncestors(newAncestors);

        boolean updated = updateById(dept);
        if (updated && !StrUtil.equals(oldAncestors, newAncestors)) {
            updateChildrenAncestors(dept.getDeptId(), oldAncestors, newAncestors);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDeptById(Long deptId) {
        if (hasChildByDeptId(deptId)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "存在子部门，不允许删除");
        }
        if (checkDeptExistUser(deptId)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "部门下存在用户，不允许删除");
        }
        return removeById(deptId);
    }

    /**
     * 规范化父部门
     */
    private void normalizeDeptParent(SysDept dept) {
        if (ObjectUtil.isNull(dept.getParentId())) {
            dept.setParentId(0L);
        }
    }

    /**
     * 构建祖级链
     */
    private String buildAncestors(Long parentId) {
        Long normalizedParentId = ObjectUtil.defaultIfNull(parentId, 0L);
        if (normalizedParentId == 0L) {
            return "0";
        }

        SysDept parent = selectDeptById(normalizedParentId);
        if (ObjectUtil.isNull(parent)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "上级部门不存在");
        }

        String parentAncestors = StrUtil.blankToDefault(parent.getAncestors(), "0");
        return parentAncestors + "," + normalizedParentId;
    }

    /**
     * 更新子孙节点祖级链
     */
    private void updateChildrenAncestors(Long deptId, String oldAncestors, String newAncestors) {
        String oldPrefix = StrUtil.blankToDefault(oldAncestors, "0") + "," + deptId;
        String newPrefix = StrUtil.blankToDefault(newAncestors, "0") + "," + deptId;

        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(SysDept::getAncestors, oldPrefix);
        List<SysDept> descendants = list(wrapper);
        for (SysDept child : descendants) {
            if (StrUtil.isBlank(child.getAncestors())) {
                continue;
            }
            child.setAncestors(child.getAncestors().replaceFirst(oldPrefix, newPrefix));
            updateById(child);
        }
    }

    /**
     * 递归排序
     */
    private void sortDeptTree(List<SysDept> roots) {
        roots.sort(Comparator
            .comparing((SysDept node) -> ObjectUtil.defaultIfNull(node.getSortOrder(), 0))
            .thenComparing(node -> ObjectUtil.defaultIfNull(node.getDeptId(), 0L)));

        for (SysDept root : roots) {
            if (ObjectUtil.isNotEmpty(root.getChildren())) {
                sortDeptTree(root.getChildren());
            }
        }
    }
}
