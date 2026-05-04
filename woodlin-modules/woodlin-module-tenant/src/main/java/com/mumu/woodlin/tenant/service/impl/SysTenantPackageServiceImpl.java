package com.mumu.woodlin.tenant.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.common.constant.CommonConstant;
import com.mumu.woodlin.common.mp.support.MyBatisPlusPageResults;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.tenant.entity.SysTenantPackage;
import com.mumu.woodlin.tenant.mapper.SysTenantPackageMapper;
import com.mumu.woodlin.tenant.service.ISysTenantPackageService;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;

/**
 * 租户套餐服务实现
 *
 * @author yulin
 * @since 2026-06
 */
@Service
@RequiredArgsConstructor
public class SysTenantPackageServiceImpl extends ServiceImpl<SysTenantPackageMapper, SysTenantPackage>
        implements ISysTenantPackageService {

    @Override
    public PageResult<SysTenantPackage> queryPage(SysTenantPackage query, int pageNum, int pageSize) {
        SysTenantPackage condition = query == null ? new SysTenantPackage() : query;
        Page<SysTenantPackage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysTenantPackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenantPackage::getDeleted, CommonConstant.DELETED_NO)
            .like(StrUtil.isNotBlank(condition.getPackageName()),
                SysTenantPackage::getPackageName, condition.getPackageName())
            .eq(StrUtil.isNotBlank(condition.getStatus()),
                SysTenantPackage::getStatus, condition.getStatus())
            .orderByDesc(SysTenantPackage::getCreateTime)
            .orderByDesc(SysTenantPackage::getPackageId);
        return MyBatisPlusPageResults.of(this.page(page, wrapper));
    }

    @Override
    public List<SysTenantPackage> listEnabled() {
        LambdaQueryWrapper<SysTenantPackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenantPackage::getDeleted, CommonConstant.DELETED_NO)
            .eq(SysTenantPackage::getStatus, CommonConstant.STATUS_ENABLE)
            .orderByDesc(SysTenantPackage::getCreateTime)
            .orderByDesc(SysTenantPackage::getPackageId);
        return list(wrapper);
    }
}
