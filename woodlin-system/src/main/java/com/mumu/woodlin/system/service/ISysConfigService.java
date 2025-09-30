package com.mumu.woodlin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.system.entity.SysConfig;

import java.util.List;

/**
 * 系统配置服务接口
 * 
 * @author mumu
 * @description 系统配置业务逻辑接口，支持Redis二级缓存（只读访问）
 *              配置的增删改操作由前端直接管理，本服务仅提供缓存优化的查询功能
 * @since 2025-01-01
 */
public interface ISysConfigService extends IService<SysConfig> {
    
    /**
     * 根据配置键名查询配置值（使用缓存）
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
     * 根据配置键名查询配置（使用缓存）
     * 
     * @param configKey 配置键名
     * @return 配置信息
     */
    SysConfig getByKeyWithCache(String configKey);
    
    /**
     * 清除配置缓存
     * 注意：此方法用于在前端更新配置后刷新缓存
     */
    void evictCache();
    
    /**
     * 预热配置缓存
     */
    void warmupCache();
}
