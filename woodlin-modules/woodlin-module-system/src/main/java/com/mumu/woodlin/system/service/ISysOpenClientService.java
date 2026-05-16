package com.mumu.woodlin.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.mumu.woodlin.system.entity.SysOpenClient;

/**
 * 开放平台客户服务。
 *
 * @author mumu
 * @since 2026-06-03
 */
public interface ISysOpenClientService extends IService<SysOpenClient> {

    /**
     * 查询客户列表。
     *
     * @param keyword 关键字
     * @return 客户列表
     */
    List<SysOpenClient> listClients(String keyword);

    /**
     * 新增客户。
     *
     * @param client 客户
     * @return 是否成功
     */
    boolean createClient(SysOpenClient client);

    /**
     * 更新客户。
     *
     * @param client 客户
     * @return 是否成功
     */
    boolean updateClient(SysOpenClient client);
}
