package com.mumu.woodlin.system.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mumu.woodlin.system.entity.SysConfig;

import java.util.Arrays;
import java.util.List;

/**
 * 系统配置缓存使用示例
 * 
 * @author mumu
 * @description 演示系统配置的缓存使用方法和序列化效果
 * @since 2025-01-01
 */
public class SysConfigCacheExample {
    
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        System.out.println("=== Woodlin SysConfig Cache Demo ===\n");
        
        // 1. 单个配置对象序列化
        System.out.println("1. Single SysConfig Serialization:");
        SysConfig config1 = new SysConfig();
        config1.setConfigId(1L);
        config1.setConfigKey("sys.user.initPassword");
        config1.setConfigValue("123456");
        config1.setConfigName("用户管理-账号初始密码");
        config1.setConfigType("Y");
        config1.setDeleted("0");
        
        System.out.println(mapper.writeValueAsString(config1));
        
        // 2. 配置列表
        System.out.println("\n2. SysConfig List:");
        SysConfig config2 = new SysConfig();
        config2.setConfigId(2L);
        config2.setConfigKey("sys.account.captchaEnabled");
        config2.setConfigValue("true");
        config2.setConfigName("账号自助-验证码开关");
        config2.setConfigType("Y");
        config2.setDeleted("0");
        
        List<SysConfig> configList = Arrays.asList(config1, config2);
        System.out.println(mapper.writeValueAsString(configList));
        
        // 3. 缓存键示例
        System.out.println("\n3. Cache Key Examples:");
        System.out.println("全局配置列表缓存键: config:sys_config");
        System.out.println("特定配置缓存键: config:sys_config:config_key:sys.user.initPassword");
        
        // 4. 缓存操作示例（伪代码）
        System.out.println("\n4. Cache Operation Examples (Pseudo Code):");
        System.out.println("// 查询所有配置（使用缓存）");
        System.out.println("List<SysConfig> configs = configService.listWithCache();");
        System.out.println();
        System.out.println("// 根据键名查询配置（使用缓存）");
        System.out.println("SysConfig config = configService.getByKeyWithCache(\"sys.user.initPassword\");");
        System.out.println();
        System.out.println("// 更新配置（自动清除缓存）");
        System.out.println("configService.saveOrUpdateConfig(config);");
        System.out.println();
        System.out.println("// 手动清除缓存");
        System.out.println("configService.evictCache();");
        System.out.println();
        System.out.println("// 预热缓存");
        System.out.println("configService.warmupCache();");
        
        // 5. 与字典缓存的对比
        System.out.println("\n5. Comparison with Dictionary Cache:");
        System.out.println("配置缓存 (Config Cache):");
        System.out.println("  - 缓存前缀: config:");
        System.out.println("  - 过期时间: 7200秒 (2小时)");
        System.out.println("  - 刷新间隔: 3600秒 (1小时)");
        System.out.println("  - 适用场景: 系统配置参数，变更较少");
        System.out.println();
        System.out.println("字典缓存 (Dictionary Cache):");
        System.out.println("  - 缓存前缀: dict:");
        System.out.println("  - 过期时间: 3600秒 (1小时)");
        System.out.println("  - 刷新间隔: 1800秒 (30分钟)");
        System.out.println("  - 适用场景: 枚举字典数据，相对固定");
        
        // 6. 缓存一致性说明
        System.out.println("\n6. Cache Consistency:");
        System.out.println("✓ 使用相同的 RedisCacheService");
        System.out.println("✓ 采用相同的分布式锁机制");
        System.out.println("✓ 支持双重检查防止缓存击穿");
        System.out.println("✓ 使用 FastJSON2 序列化");
        System.out.println("✓ 统一的缓存配置管理");
        System.out.println("✓ 一致的异常降级策略");
    }
}
