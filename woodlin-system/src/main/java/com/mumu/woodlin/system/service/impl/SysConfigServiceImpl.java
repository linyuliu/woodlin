package com.mumu.woodlin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mumu.woodlin.common.service.RedisCacheService;
import com.mumu.woodlin.system.entity.SysConfig;
import com.mumu.woodlin.system.mapper.SysConfigMapper;
import com.mumu.woodlin.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统配置服务实现
 *
 * @author mumu
 * @description 系统配置业务逻辑实现，集成Redis二级缓存优化
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    private final RedisCacheService redisCacheService;

    private static final String CONFIG_CACHE_TYPE = "sys_config";
    private static final String CONFIG_KEY_PREFIX = "config_key:";

    @Override
    public String getConfigValueByKey(String configKey) {
        SysConfig config = getByKeyWithCache(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public List<SysConfig> listWithCache() {
        return redisCacheService.getConfigCache(CONFIG_CACHE_TYPE, () -> {
            LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysConfig::getDeleted, "0");
            wrapper.orderByAsc(SysConfig::getConfigId);
            return list(wrapper);
        });
    }

    @Override
    public SysConfig getByKeyWithCache(String configKey) {
        // 使用带key的缓存类型
        String cacheType = CONFIG_CACHE_TYPE + ":" + CONFIG_KEY_PREFIX + configKey;

        List<SysConfig> configs = redisCacheService.getConfigCache(cacheType, () -> {
            LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysConfig::getConfigKey, configKey);
            wrapper.eq(SysConfig::getDeleted, "0");
            wrapper.last("LIMIT 1");
            List<SysConfig> list = list(wrapper);
            return list;
        });

        return configs != null && !configs.isEmpty() ? configs.get(0) : null;
    }

    @Override
    public void evictCache() {
        redisCacheService.evictConfigCache(CONFIG_CACHE_TYPE);
        log.info("已清除所有配置缓存");
    }

    @Override
    public void warmupCache() {
        redisCacheService.warmupConfigCache(CONFIG_CACHE_TYPE, () -> {
            LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysConfig::getDeleted, "0");
            wrapper.orderByAsc(SysConfig::getConfigId);
            return list(wrapper);
        });
        log.info("配置缓存预热完成");
    }
}
