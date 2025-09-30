package com.mumu.woodlin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.system.entity.SysConfig;
import com.mumu.woodlin.system.mapper.SysConfigMapper;
import com.mumu.woodlin.system.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统配置服务实现
 * 
 * @author mumu
 * @description 系统配置服务实现类
 * @since 2025-01-01
 */
@Slf4j
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {
    
    @Override
    public SysConfig selectConfigByKey(String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        wrapper.eq(SysConfig::getDeleted, "0");
        return getOne(wrapper);
    }
    
    @Override
    public String selectConfigValueByKey(String configKey) {
        SysConfig config = selectConfigByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }
    
    @Override
    public List<SysConfig> selectConfigListByCategory(String category) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(SysConfig::getConfigKey, category + ".");
        wrapper.eq(SysConfig::getDeleted, "0");
        wrapper.orderByAsc(SysConfig::getConfigKey);
        return list(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertConfig(SysConfig config) {
        return save(config);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfig(SysConfig config) {
        return updateById(config);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfigByKey(String configKey, String configValue) {
        SysConfig config = selectConfigByKey(configKey);
        if (config != null) {
            config.setConfigValue(configValue);
            return updateById(config);
        }
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateConfig(List<SysConfig> configs) {
        return updateBatchById(configs);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfigByIds(List<Long> configIds) {
        // 逻辑删除
        return removeBatchByIds(configIds);
    }
}
