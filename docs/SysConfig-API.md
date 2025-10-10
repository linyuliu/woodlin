# SysConfig API Endpoints

## 系统配置管理接口

### 基础路径

```text
/api/system/config
```

### API列表

#### 1. 查询配置列表
```http
GET /api/system/config/list
```

**描述**: 查询所有系统配置，使用Redis二级缓存

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "configId": 1,
      "configName": "主框架页-默认皮肤样式名称",
      "configKey": "sys.index.skinName",
      "configValue": "skin-blue",
      "configType": "Y",
      "remark": "蓝色 skin-blue、绿色 skin-green",
      "deleted": "0"
    }
  ]
}
```

---

#### 2. 根据配置键名查询配置
```http
GET /api/system/config/key/{configKey}
```

**描述**: 根据配置键名查询配置信息，使用Redis二级缓存

**路径参数**:
- `configKey`: 配置键名，如 `sys.user.initPassword`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "configId": 2,
    "configName": "用户管理-账号初始密码",
    "configKey": "sys.user.initPassword",
    "configValue": "123456",
    "configType": "Y",
    "deleted": "0"
  }
}
```

---

#### 3. 查询配置值
```http
GET /api/system/config/value/{configKey}
```

**描述**: 根据配置键名直接查询配置值，使用Redis二级缓存

**路径参数**:
- `configKey`: 配置键名，如 `sys.account.captchaEnabled`

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": "true"
}
```

---

#### 4. 新增或更新配置
```http
POST /api/system/config
```

**描述**: 新增或更新系统配置，自动清除缓存

**请求体**:
```json
{
  "configId": 1,
  "configName": "自定义配置",
  "configKey": "sys.custom.setting",
  "configValue": "custom_value",
  "configType": "N",
  "remark": "这是自定义配置"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功"
}
```

---

#### 5. 删除配置
```http
DELETE /api/system/config/{configId}
```

**描述**: 删除系统配置（软删除），自动清除缓存

**路径参数**:
- `configId`: 配置ID

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

#### 6. 清除配置缓存
```http
DELETE /api/system/config/cache
```

**描述**: 手动清除所有系统配置缓存

**响应示例**:
```json
{
  "code": 200,
  "message": "缓存清除成功"
}
```

---

#### 7. 预热配置缓存
```http
POST /api/system/config/cache/warmup
```

**描述**: 预热系统配置缓存，提前加载热点配置到Redis

**响应示例**:
```json
{
  "code": 200,
  "message": "缓存预热成功"
}
```

---

## 缓存说明

### 缓存键设计
- **全局配置列表**: `config:sys_config`
- **特定配置**: `config:sys_config:config_key:{configKey}`

### 缓存策略
- **过期时间**: 7200秒（2小时）
- **刷新间隔**: 3600秒（1小时）
- **自动清除**: 增删改操作自动清除相关缓存
- **降级策略**: 缓存异常时自动从数据库读取

### 缓存一致性
与字典缓存保持一致的实现模式：
- ✅ 使用相同的 RedisCacheService
- ✅ 采用分布式锁防止缓存击穿
- ✅ 支持双重检查模式
- ✅ 使用 FastJSON2 序列化
- ✅ 统一的配置管理

## 使用示例

::: code-tabs#examples

@tab Java 客户端

```java
@Autowired
private ISysConfigService configService;

// 1. 查询配置值（推荐，最常用）
String password = configService.getConfigValueByKey("sys.user.initPassword");

// 2. 查询配置对象（使用缓存）
SysConfig config = configService.getByKeyWithCache("sys.account.captchaEnabled");

// 3. 查询所有配置（使用缓存）
List<SysConfig> configs = configService.listWithCache();

// 4. 更新配置（自动清除缓存）
SysConfig newConfig = new SysConfig();
newConfig.setConfigKey("sys.custom.setting");
newConfig.setConfigValue("new_value");
newConfig.setConfigName("自定义设置");
newConfig.setConfigType("N");
configService.saveOrUpdateConfig(newConfig);

// 5. 手动清除缓存
configService.evictCache();

// 6. 预热缓存（系统启动时推荐调用）
configService.warmupCache();
```

@tab cURL 命令

```bash
# 查询配置列表
curl -X GET "http://localhost:8080/api/system/config/list"

# 根据键名查询配置
curl -X GET "http://localhost:8080/api/system/config/key/sys.user.initPassword"

# 查询配置值（直接返回值）
curl -X GET "http://localhost:8080/api/system/config/value/sys.account.captchaEnabled"

# 新增或更新配置
curl -X POST "http://localhost:8080/api/system/config" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -d '{
    "configName": "测试配置",
    "configKey": "test.config.key",
    "configValue": "test_value",
    "configType": "N",
    "remark": "这是测试配置"
  }'

# 删除配置（软删除）
curl -X DELETE "http://localhost:8080/api/system/config/1" \
  -H "Authorization: Bearer your-token"

# 清除配置缓存
curl -X DELETE "http://localhost:8080/api/system/config/cache" \
  -H "Authorization: Bearer your-token"

# 预热配置缓存
curl -X POST "http://localhost:8080/api/system/config/cache/warmup" \
  -H "Authorization: Bearer your-token"
```

@tab JavaScript/TypeScript

```typescript
import axios from 'axios';

// 创建 API 实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api/system/config',
  headers: {
    'Authorization': 'Bearer your-token'
  }
});

// 1. 查询配置列表
const configs = await api.get('/list');

// 2. 根据键名查询配置
const config = await api.get('/key/sys.user.initPassword');

// 3. 查询配置值
const value = await api.get('/value/sys.account.captchaEnabled');

// 4. 新增或更新配置
await api.post('/', {
  configName: '测试配置',
  configKey: 'test.config.key',
  configValue: 'test_value',
  configType: 'N'
});

// 5. 删除配置
await api.delete('/1');

// 6. 清除缓存
await api.delete('/cache');

// 7. 预热缓存
await api.post('/cache/warmup');
```

:::

## 配置说明

### 配置管理

::: code-tabs#config

@tab application.yml

```yaml
woodlin:
  cache:
    # 是否启用 Redis 二级缓存
    redis-enabled: true
    
    # 系统配置缓存配置
    config:
      # 是否启用配置缓存
      enabled: true
      # 缓存过期时间（秒，默认 2 小时）
      expire-seconds: 7200
      # 缓存刷新间隔（秒，默认 1 小时）
      refresh-interval-seconds: 3600
```

@tab 环境变量

```bash
# 是否启用 Redis 缓存
export CACHE_REDIS_ENABLED=true

# 是否启用配置缓存
export CACHE_CONFIG_ENABLED=true

# 配置缓存过期时间（秒）
export CACHE_CONFIG_EXPIRE=7200

# 配置缓存刷新间隔（秒）
export CACHE_CONFIG_REFRESH=3600
```

:::

## 注意事项

1. **配置更新**：更新配置后会自动清除相关缓存，确保数据一致性
2. **Redis 依赖**：配置缓存依赖 Redis 服务，请确保 Redis 可用
3. **分布式环境**：支持分布式环境，配置更新会清除所有节点的缓存
4. **缓存预热**：建议在系统启动或业务高峰前预热热点配置
5. **降级策略**：缓存异常时自动降级，直接从数据库读取
6. **性能优化**：缓存命中率可达 95% 以上，查询响应时间小于 1ms
