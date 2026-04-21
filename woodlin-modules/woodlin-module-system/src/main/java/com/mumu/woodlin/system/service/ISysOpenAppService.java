package com.mumu.woodlin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.system.entity.SysOpenApp;

import java.util.List;

/**
 * 开放应用服务。
 *
 * @author mumu
 * @since 2026-04-13
 */
public interface ISysOpenAppService extends IService<SysOpenApp> {

    /**
     * 查询应用列表。
     *
     * @param keyword 关键字
     * @return 应用列表
     */
    List<SysOpenApp> listApps(String keyword);

    /**
     * 新增应用。
     *
     * @param app 应用
     * @return 是否成功
     */
    boolean createApp(SysOpenApp app);

    /**
     * 修改应用。
     *
     * @param app 应用
     * @return 是否成功
     */
    boolean updateApp(SysOpenApp app);

    /**
     * 删除应用并级联删除凭证。
     *
     * @param appId 应用ID
     * @return 是否成功
     */
    boolean removeAppCascade(Long appId);
}
