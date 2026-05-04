package com.mumu.woodlin.tenant.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.common.response.PageResult;
import com.mumu.woodlin.tenant.entity.SysTenantPackage;

/**
 * 租户套餐服务接口
 *
 * @author yulin
 * @since 2026-06
 */
public interface ISysTenantPackageService extends IService<SysTenantPackage> {

    /**
     * 分页查询租户套餐列表
     *
     * @param query    查询条件
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<SysTenantPackage> queryPage(SysTenantPackage query, int pageNum, int pageSize);

    /**
     * 查询启用状态的全部套餐
     *
     * @return 套餐列表
     */
    List<SysTenantPackage> listEnabled();
}
