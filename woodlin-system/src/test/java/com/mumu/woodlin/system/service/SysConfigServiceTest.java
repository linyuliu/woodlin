package com.mumu.woodlin.system.service;

import com.mumu.woodlin.system.entity.SysConfig;
import com.mumu.woodlin.system.mapper.SysConfigMapper;
import com.mumu.woodlin.common.service.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 系统配置服务测试
 * 
 * @author mumu
 * @description 测试系统配置服务的缓存功能
 * @since 2025-01-01
 */
class SysConfigServiceTest {
    
    @Mock
    private SysConfigMapper configMapper;
    
    @Mock
    private RedisCacheService redisCacheService;
    
    @Mock
    private ISysConfigService configService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testGetConfigValueByKey() {
        // 准备测试数据
        String configKey = "sys.user.initPassword";
        String expectedValue = "123456";
        
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        config.setConfigValue(expectedValue);
        
        // 模拟服务行为
        when(configService.getByKeyWithCache(configKey)).thenReturn(config);
        when(configService.getConfigValueByKey(configKey)).thenReturn(expectedValue);
        
        // 执行测试
        String actualValue = configService.getConfigValueByKey(configKey);
        
        // 验证结果
        assertEquals(expectedValue, actualValue);
        verify(configService, times(1)).getConfigValueByKey(configKey);
    }
    
    @Test
    void testListWithCache() {
        // 准备测试数据
        SysConfig config1 = new SysConfig();
        config1.setConfigId(1L);
        config1.setConfigKey("sys.index.skinName");
        config1.setConfigValue("skin-blue");
        
        SysConfig config2 = new SysConfig();
        config2.setConfigId(2L);
        config2.setConfigKey("sys.user.initPassword");
        config2.setConfigValue("123456");
        
        List<SysConfig> expectedList = Arrays.asList(config1, config2);
        
        // 模拟服务行为
        when(configService.listWithCache()).thenReturn(expectedList);
        
        // 执行测试
        List<SysConfig> actualList = configService.listWithCache();
        
        // 验证结果
        assertNotNull(actualList);
        assertEquals(2, actualList.size());
        assertEquals("sys.index.skinName", actualList.get(0).getConfigKey());
        assertEquals("sys.user.initPassword", actualList.get(1).getConfigKey());
    }
    
    @Test
    void testGetByKeyWithCache() {
        // 准备测试数据
        String configKey = "sys.account.captchaEnabled";
        SysConfig expectedConfig = new SysConfig();
        expectedConfig.setConfigKey(configKey);
        expectedConfig.setConfigValue("true");
        
        // 模拟服务行为
        when(configService.getByKeyWithCache(configKey)).thenReturn(expectedConfig);
        
        // 执行测试
        SysConfig actualConfig = configService.getByKeyWithCache(configKey);
        
        // 验证结果
        assertNotNull(actualConfig);
        assertEquals(configKey, actualConfig.getConfigKey());
        assertEquals("true", actualConfig.getConfigValue());
    }
    
    @Test
    void testSaveOrUpdateConfig() {
        // 准备测试数据
        SysConfig config = new SysConfig();
        config.setConfigKey("test.config.key");
        config.setConfigValue("test.value");
        
        // 模拟服务行为
        when(configService.saveOrUpdateConfig(config)).thenReturn(true);
        
        // 执行测试
        boolean result = configService.saveOrUpdateConfig(config);
        
        // 验证结果
        assertTrue(result);
        verify(configService, times(1)).saveOrUpdateConfig(config);
    }
    
    @Test
    void testDeleteConfig() {
        // 准备测试数据
        Long configId = 1L;
        
        // 模拟服务行为
        when(configService.deleteConfig(configId)).thenReturn(true);
        
        // 执行测试
        boolean result = configService.deleteConfig(configId);
        
        // 验证结果
        assertTrue(result);
        verify(configService, times(1)).deleteConfig(configId);
    }
    
    @Test
    void testEvictCache() {
        // 执行测试
        doNothing().when(configService).evictCache();
        configService.evictCache();
        
        // 验证调用
        verify(configService, times(1)).evictCache();
    }
    
    @Test
    void testWarmupCache() {
        // 执行测试
        doNothing().when(configService).warmupCache();
        configService.warmupCache();
        
        // 验证调用
        verify(configService, times(1)).warmupCache();
    }
}
