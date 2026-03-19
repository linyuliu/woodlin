package com.mumu.woodlin.tenant.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.tenant.entity.SysTenant;

/**
 * 租户信息服务接口
 *
 * @author mumu
 * @since 2026-03-18
 */
public interface ISysTenantService extends IService<SysTenant> {

    /**
     * 分页查询租户列表
     *
     * @param tenant 查询条件
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<SysTenant> selectTenantPage(SysTenant tenant, Integer pageNum, Integer pageSize);

    /**
     * 校验租户编码是否唯一
     *
     * @param tenant 租户信息
     * @return 是否唯一
     */
    boolean checkTenantCodeUnique(SysTenant tenant);

    /**
     * 创建租户
     *
     * @param tenant 租户信息
     * @return 是否成功
     */
    boolean createTenant(SysTenant tenant);

    /**
     * 更新租户
     *
     * @param tenant 租户信息
     * @return 是否成功
     */
    boolean updateTenant(SysTenant tenant);

    /**
     * 删除租户
     *
     * @param tenantIds 租户ID列表
     * @return 是否成功
     */
    boolean deleteTenantByIds(List<String> tenantIds);

    /**
     * 更新租户状态
     *
     * @param tenantId 租户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateTenantStatus(String tenantId, String status);
}
