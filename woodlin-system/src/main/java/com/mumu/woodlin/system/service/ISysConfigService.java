package com.mumu.woodlin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mumu.woodlin.system.entity.SysConfig;

import java.util.List;

/**
 * 系统配置服务接口
 * 
 * @author mumu
 * @description 系统配置服务接口，提供配置管理的业务功能
 * @since 2025-01-01
 */
public interface ISysConfigService extends IService<SysConfig> {
    
    /**
     * 根据配置键名获取配置
     * 
     * @param configKey 配置键名
     * @return 配置信息
     */
    SysConfig selectConfigByKey(String configKey);
    
    /**
     * 根据配置键名获取配置值
     * 
     * @param configKey 配置键名
     * @return 配置值
     */
    String selectConfigValueByKey(String configKey);
    
    /**
     * 根据配置分类获取配置列表
     * 
     * @param category 配置分类（如：api.encryption, password.policy等）
     * @return 配置列表
     */
    List<SysConfig> selectConfigListByCategory(String category);
    
    /**
     * 新增配置
     * 
     * @param config 配置信息
     * @return 结果
     */
    boolean insertConfig(SysConfig config);
    
    /**
     * 修改配置
     * 
     * @param config 配置信息
     * @return 结果
     */
    boolean updateConfig(SysConfig config);
    
    /**
     * 根据配置键名更新配置值
     * 
     * @param configKey 配置键名
     * @param configValue 配置值
     * @return 结果
     */
    boolean updateConfigByKey(String configKey, String configValue);
    
    /**
     * 批量更新配置
     * 
     * @param configs 配置列表
     * @return 结果
     */
    boolean batchUpdateConfig(List<SysConfig> configs);
    
    /**
     * 删除配置
     * 
     * @param configIds 配置ID数组
     * @return 结果
     */
    boolean deleteConfigByIds(List<Long> configIds);
}
