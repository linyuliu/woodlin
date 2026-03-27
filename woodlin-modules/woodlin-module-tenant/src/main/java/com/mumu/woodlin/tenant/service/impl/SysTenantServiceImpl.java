package com.mumu.woodlin.tenant.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.common.constant.SystemConstant;
import com.mumu.woodlin.common.enums.ResultCode;
import com.mumu.woodlin.common.exception.BusinessException;
import com.mumu.woodlin.common.mp.support.MyBatisPlusPageResults;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.tenant.entity.SysTenant;
import com.mumu.woodlin.tenant.mapper.SysTenantMapper;
import com.mumu.woodlin.tenant.service.ISysTenantService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;

/**
 * 租户信息服务实现
 *
 * @author mumu
 * @since 2026-03-18
 */
@Service
@RequiredArgsConstructor
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements ISysTenantService {

    private static final int DEFAULT_USER_LIMIT = 100;

    @Override
    public PageResult<SysTenant> selectTenantPage(SysTenant tenant, Integer pageNum, Integer pageSize) {
        SysTenant query = tenant == null ? new SysTenant() : tenant;
        Page<SysTenant> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenant::getDeleted, CommonConstant.DELETED_NO)
            .like(StrUtil.isNotBlank(query.getTenantName()), SysTenant::getTenantName, query.getTenantName())
            .like(StrUtil.isNotBlank(query.getTenantCode()), SysTenant::getTenantCode, query.getTenantCode())
            .eq(StrUtil.isNotBlank(query.getStatus()), SysTenant::getStatus, query.getStatus())
            .orderByDesc(SysTenant::getCreateTime)
            .orderByDesc(SysTenant::getTenantId);
        return MyBatisPlusPageResults.of(this.page(page, wrapper));
    }

    @Override
    public boolean checkTenantCodeUnique(SysTenant tenant) {
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenant::getTenantCode, tenant.getTenantCode())
            .eq(SysTenant::getDeleted, CommonConstant.DELETED_NO)
            .ne(StrUtil.isNotBlank(tenant.getTenantId()), SysTenant::getTenantId, tenant.getTenantId());
        return count(wrapper) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTenant(SysTenant tenant) {
        if (StrUtil.isBlank(tenant.getTenantCode())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户编码不能为空");
        }
        if (!checkTenantCodeUnique(tenant)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户编码已存在");
        }
        validateTenantData(tenant.getStatus(), tenant.getUserLimit());
        fillDefaultValues(tenant);
        tenant.setTenantId(StrUtil.blankToDefault(tenant.getTenantId(), null));
        return save(tenant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTenant(SysTenant tenant) {
        if (StrUtil.isBlank(tenant.getTenantId())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户ID不能为空");
        }

        SysTenant existingTenant = getById(tenant.getTenantId());
        if (existingTenant == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户不存在");
        }
        if (!checkTenantCodeUnique(tenant)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户编码已存在");
        }
        validateDefaultTenantProtection(existingTenant.getTenantId(), tenant.getStatus());
        validateTenantData(tenant.getStatus(), tenant.getUserLimit());
        fillDefaultValues(tenant);
        return updateById(tenant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTenantByIds(List<String> tenantIds) {
        if (CollUtil.isEmpty(tenantIds)) {
            return false;
        }
        for (String tenantId : tenantIds) {
            if (SystemConstant.DEFAULT_TENANT_ID.equals(tenantId)) {
                throw BusinessException.of(ResultCode.BAD_REQUEST, "默认租户不允许删除");
            }
        }
        return removeByIds(tenantIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTenantStatus(String tenantId, String status) {
        if (StrUtil.isBlank(tenantId)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户ID不能为空");
        }
        if (StrUtil.isBlank(status)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户状态不能为空");
        }

        SysTenant tenant = getById(tenantId);
        if (tenant == null) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户不存在");
        }
        validateDefaultTenantProtection(tenantId, status);
        validateTenantData(status, tenant.getUserLimit());
        tenant.setStatus(status);
        return updateById(tenant);
    }

    private void fillDefaultValues(SysTenant tenant) {
        if (StrUtil.isBlank(tenant.getStatus())) {
            tenant.setStatus(CommonConstant.STATUS_ENABLE);
        }
        if (tenant.getUserLimit() == null) {
            tenant.setUserLimit(DEFAULT_USER_LIMIT);
        }
    }

    private void validateDefaultTenantProtection(String tenantId, String status) {
        if (SystemConstant.DEFAULT_TENANT_ID.equals(tenantId) && CommonConstant.STATUS_DISABLE.equals(status)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "默认租户不允许禁用");
        }
    }

    private void validateTenantData(String status, Integer userLimit) {
        if (StrUtil.isNotBlank(status)
            && !CommonConstant.STATUS_ENABLE.equals(status)
            && !CommonConstant.STATUS_DISABLE.equals(status)) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "租户状态仅支持 1 或 0");
        }
        if (userLimit != null && userLimit <= 0) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "用户配额必须大于 0");
        }
    }
}
