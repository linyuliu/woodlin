package com.mumu.woodlin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.system.entity.SysConfig;

import java.util.List;

/**
 * 系统配置服务接口
 *
 * @author mumu
 * @description 系统配置服务接口，提供配置缓存优化的只读查询功能
 *              配置的增删改操作由前端直接管理，后端专注于缓存优化
 * @since 2025-01-01
 */
public interface ISysConfigService extends IService<SysConfig> {

    /**
     * 根据配置键名获取配置值（使用缓存）
     *
     * @param configKey 配置键名
     * @return 配置值
     */
    String getConfigValueByKey(String configKey);

    /**
     * 查询所有配置列表（使用缓存）
     *
     * @return 配置列表
     */
    List<SysConfig> listWithCache();

    /**
     * 根据配置键名获取配置对象（使用缓存）
     *
     * @param configKey 配置键名
     * @return 配置对象
     */
    SysConfig getByKeyWithCache(String configKey);

    /**
     * 清除配置缓存
     * 前端更新配置后应调用此方法刷新缓存
     */
    void evictCache();

    /**
     * 预热配置缓存
     * 系统启动时调用，提前加载配置到缓存
     */
    void warmupCache();
}
