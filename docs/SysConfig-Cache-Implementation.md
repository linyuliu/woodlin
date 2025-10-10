# SysConfig Redis二级缓存实现文档

## 概述

本次实现为系统配置（SysConfig）添加了完整的Redis二级缓存支持，与现有的字典（Dictionary）缓存机制保持一致，实现了配置数据的高效缓存管理。

## 实现特性

### 1. 缓存架构设计

- **二级缓存**：使用Redis作为二级缓存，一级为内存，二级为Redis
- **分布式锁**：使用Redis分布式锁防止缓存击穿
- **双重检查**：在获取锁后再次检查缓存，确保并发安全
- **降级策略**：缓存异常时自动降级，直接从数据库加载

### 2. 核心功能

#### 2.1 缓存配置（CacheProperties）

新增配置缓存配置项：

```yaml
woodlin:
  cache:
    redis-enabled: true
    config:
      enabled: true
      expire-seconds: 7200    # 2小时
      refresh-interval-seconds: 3600  # 1小时
```

- 配置缓存过期时间为7200秒（2小时）
- 与字典缓存类似，但过期时间更长（因为配置变化相对较少）

#### 2.2 RedisCacheService扩展

新增配置缓存相关方法：

- `getConfigCache()` - 获取配置缓存
- `evictConfigCache()` - 清除指定配置缓存
- `evictAllConfigCache()` - 清除所有配置缓存
- `warmupConfigCache()` - 预热配置缓存

#### 2.3 SysConfig服务层

实现完整的CRUD操作和缓存管理：

**ISysConfigService接口**：

- `listWithCache()` - 查询所有配置（带缓存）
- `getByKeyWithCache()` - 根据键名查询配置（带缓存）
- `getConfigValueByKey()` - 获取配置值
- `saveOrUpdateConfig()` - 新增或更新配置（自动清除缓存）
- `deleteConfig()` - 删除配置（软删除，自动清除缓存）
- `evictCache()` - 清除配置缓存
- `warmupCache()` - 预热配置缓存

**SysConfigServiceImpl实现**：

- 使用`RedisCacheService`管理缓存
- 增删改操作自动清除相关缓存
- 支持按配置key的精确缓存

#### 2.4 SysConfig 控制器

提供完整的 RESTful API：

```http
GET    /api/system/config/list                # 查询配置列表（带缓存）
GET    /api/system/config/key/{configKey}     # 根据键名查询配置（带缓存）
GET    /api/system/config/value/{configKey}   # 根据键名查询配置值（带缓存）
POST   /api/system/config                     # 新增或更新配置（自动清除缓存）
DELETE /api/system/config/{configId}          # 删除配置（软删除，自动清除缓存）
DELETE /api/system/config/cache               # 手动清除配置缓存
POST   /api/system/config/cache/warmup        # 预热配置缓存
```

### 3. 缓存策略

#### 3.1 缓存键设计

::: tip 缓存键命名规范
- **全局配置列表**：`config:sys_config`
- **特定键配置**：`config:sys_config:config_key:{configKey}`

示例：
- `config:sys_config` - 缓存所有配置列表
- `config:sys_config:config_key:sys.user.initPassword` - 缓存特定配置
:::

#### 3.2 缓存更新策略

- **增删改操作**：自动清除相关缓存
- **查询操作**：优先从缓存获取，缓存不存在时从数据库加载并缓存
- **预热机制**：支持手动预热，在系统启动或业务高峰前加载热点配置

### 4. 与字典缓存的一致性

本实现与现有的字典（DictEnum）缓存机制保持完全一致：

| 特性 | 字典缓存 | 配置缓存 | 说明 |
|------|---------|---------|------|
| **缓存服务** | ✅ RedisCacheService | ✅ RedisCacheService | 使用统一的缓存服务 |
| **二级缓存** | ✅ Redis | ✅ Redis | 使用 Redis 作为二级缓存 |
| **分布式锁** | ✅ 支持 | ✅ 支持 | 防止缓存击穿 |
| **双重检查** | ✅ 支持 | ✅ 支持 | 确保并发安全 |
| **降级策略** | ✅ 支持 | ✅ 支持 | 缓存异常时降级到数据库 |
| **序列化方式** | FastJSON2 | FastJSON2 | 统一的序列化方式 |

3. **统一的配置方式**：
   - 通过`CacheProperties`统一管理
   - 支持动态配置（环境变量）
   - 独立的启用/禁用开关

4. **一致的序列化**：
   - 使用FastJSON2进行序列化
   - 支持类型信息保留
   - 保证反序列化正确性

## 使用示例

### 1. 查询配置值

```java
@Autowired
private ISysConfigService configService;

// 获取配置值
String password = configService.getConfigValueByKey("sys.user.initPassword");
```

### 2. 查询配置对象

```java
// 获取配置对象（使用缓存）
SysConfig config = configService.getByKeyWithCache("sys.account.captchaEnabled");
```

### 3. 更新配置

```java
SysConfig config = new SysConfig();
config.setConfigKey("sys.custom.setting");
config.setConfigValue("value");
config.setConfigName("自定义设置");
config.setConfigType("N");

// 保存配置（自动清除缓存）
configService.saveOrUpdateConfig(config);
```

### 4. 缓存管理

```java
// 清除所有配置缓存
configService.evictCache();

// 预热配置缓存
configService.warmupCache();
```

## 性能优化

1. **减少数据库访问**：热点配置从缓存读取，减少数据库压力
2. **分布式锁优化**：使用Redis setIfAbsent实现高性能分布式锁
3. **缓存预热**：支持系统启动时预热配置，提升首次访问速度
4. **精确缓存失效**：更新特定配置时只清除相关缓存

## 测试覆盖

- 单元测试：`SysConfigServiceTest`
- 测试覆盖：配置CRUD、缓存操作、异常处理
- 所有测试通过：7/7

## 配置说明

### 应用配置（application.yml）

```yaml
woodlin:
  cache:
    # 是否启用Redis二级缓存
    redis-enabled: ${CACHE_REDIS_ENABLED:true}
    
    # 系统配置缓存配置
    config:
      # 是否启用配置缓存
      enabled: ${CACHE_CONFIG_ENABLED:true}
      # 缓存过期时间（秒），默认7200秒（2小时）
      expire-seconds: ${CACHE_CONFIG_EXPIRE:7200}
      # 缓存刷新间隔（秒），默认3600秒（1小时）
      refresh-interval-seconds: ${CACHE_CONFIG_REFRESH:3600}
```

### 环境变量

- `CACHE_REDIS_ENABLED` - 是否启用Redis缓存（默认：true）
- `CACHE_CONFIG_ENABLED` - 是否启用配置缓存（默认：true）
- `CACHE_CONFIG_EXPIRE` - 配置缓存过期时间（默认：7200秒）
- `CACHE_CONFIG_REFRESH` - 配置缓存刷新间隔（默认：3600秒）

## 注意事项

1. 配置缓存依赖Redis，确保Redis服务可用
2. 更新配置后需要确保缓存已清除
3. 分布式环境下，配置更新会自动清除所有节点的缓存
4. 建议定期预热热点配置，提升访问性能
5. 配置缓存时间较长，适合相对稳定的系统配置

## 后续优化建议

1. **缓存刷新**：添加定时任务定期刷新配置缓存
2. **配置监听**：实现配置变更监听，自动更新缓存
3. **缓存统计**：添加缓存命中率统计
4. **配置版本**：添加配置版本管理，支持配置回滚
5. **分组缓存**：支持按配置类型分组缓存
