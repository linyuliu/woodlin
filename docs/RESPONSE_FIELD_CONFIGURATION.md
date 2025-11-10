# 响应字段动态配置功能说明

## 概述

为了满足不同系统集成的需求，Woodlin 现在支持**两种级别**的响应字段动态配置：
1. **全局配置**：通过配置文件统一控制所有请求的响应字段
2. **请求级别配置**：通过请求头动态控制单个请求的响应字段

这样，在与其他系统对接时，可以根据需求灵活控制返回的字段，使得接口更加灵活和优雅。

## 功能特性

### 1. 全局响应字段过滤模式

支持三种过滤模式：

- **NONE（默认）**: 不过滤，包含所有字段（保持向后兼容）
- **MINIMAL**: 最小模式，仅包含 `code` 和 `data` 字段
- **CUSTOM**: 自定义模式，根据配置决定包含哪些字段

### 2. 请求级别动态控制（新增）

通过在请求头中添加 `X-Response-Fields`，可以动态控制每个请求的响应字段，**优先级高于全局配置**。

支持两种语法：
- **包含语法**：`code,data,message` - 只包含指定的字段
- **排除语法**：`-timestamp,-requestId` - 排除指定的字段

### 3. 可配置字段

- `code`: 响应状态码
- `message`: 响应消息
- `data`: 响应数据（始终包含，除非明确排除）
- `timestamp`: 响应时间戳
- `requestId`: 请求ID（用于链路追踪）

## 配置方法

### 全局配置

在 `application.yml` 中添加以下配置：

```yaml
woodlin:
  response:
    # 响应字段过滤模式（全局默认配置）
    # - NONE: 不过滤，包含所有字段（默认）
    # - MINIMAL: 最小模式，仅包含 code 和 data
    # - CUSTOM: 自定义模式，根据下面的 include* 配置决定
    filter-mode: CUSTOM
    
    # 以下配置仅在 filter-mode=CUSTOM 时生效（全局默认配置）
    include-timestamp: false    # 是否包含时间戳字段
    include-request-id: false   # 是否包含请求ID字段
    include-message: true       # 是否包含消息字段
    include-code: true          # 是否包含状态码字段
    
    # 是否启用请求级别的动态控制
    enable-request-control: true
    # 请求头名称，用于控制响应字段
    request-header-name: X-Response-Fields
```

### 环境变量配置

也可以通过环境变量来配置：

```bash
# 设置过滤模式
export RESPONSE_FILTER_MODE=MINIMAL

# 自定义模式下的字段控制
export RESPONSE_INCLUDE_TIMESTAMP=false
export RESPONSE_INCLUDE_REQUEST_ID=false
export RESPONSE_INCLUDE_MESSAGE=true
export RESPONSE_INCLUDE_CODE=true

# 启用/禁用请求级别控制
export RESPONSE_ENABLE_REQUEST_CONTROL=true
```

### 请求级别动态控制（推荐）

在每个请求中通过请求头动态控制响应字段：

```bash
# 示例1: 只包含 code 和 data 字段
curl -H "X-Response-Fields: code,data" \
     http://localhost:8080/api/users

# 示例2: 排除 timestamp 和 requestId 字段
curl -H "X-Response-Fields: -timestamp,-requestId" \
     http://localhost:8080/api/users

# 示例3: 只包含 code, data 和 message
curl -H "X-Response-Fields: code,data,message" \
     http://localhost:8080/api/users
```

**响应示例**：

使用 `X-Response-Fields: code,data`：
```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "admin"
  }
}
```

使用 `X-Response-Fields: -timestamp`：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "admin"
  }
}
```

## 使用场景

### 场景1: 与第三方系统集成（全局配置）

当与只需要状态码和数据的第三方系统集成时：

```yaml
woodlin:
  response:
    filter-mode: MINIMAL
```

返回示例：
```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "admin"
  }
}
```

### 场景2: 请求级别的动态控制（推荐）

**最灵活的方式**：不同的客户端或请求可以使用不同的响应格式。

移动端请求（简化响应）：
```bash
curl -H "X-Response-Fields: code,data" \
     http://localhost:8080/api/users
```

Web端请求（完整响应）：
```bash
curl http://localhost:8080/api/users
# 返回所有字段（使用全局默认配置）
```

管理后台请求（需要追踪信息）：
```bash
curl -H "X-Response-Fields: code,message,data,requestId" \
     http://localhost:8080/api/users
```

### 场景3: 简化移动端接口（全局配置）

移动端可能不需要 `requestId` 和详细的 `timestamp`：

```yaml
woodlin:
  response:
    filter-mode: CUSTOM
    include-timestamp: false
    include-request-id: false
    include-message: true
    include-code: true
```

返回示例：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "admin"
  }
}
```

### 场景4: 保持默认行为（推荐用于生产环境）

不做任何配置，或明确设置为 NONE 模式：

```yaml
woodlin:
  response:
    filter-mode: NONE
```

返回示例（包含所有字段）：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "admin"
  },
  "timestamp": "2025-01-10T08:00:00"
}
```

## 注意事项

1. **向后兼容**: 默认情况下（`filter-mode: NONE`），行为与之前版本完全一致
2. **数据字段**: `data` 字段始终会被包含（如果不为 null），除非请求明确排除
3. **生产环境建议**: 建议在生产环境中使用 `NONE` 模式，保留所有字段用于调试和监控
4. **配置优先级**: **请求头配置 > 全局配置**
   - 如果请求头中设置了 `X-Response-Fields`，将优先使用请求头的配置
   - 否则使用全局配置（`application.yml` 或环境变量）
5. **请求级别控制**: 默认启用（`enable-request-control: true`），可以通过配置关闭
6. **线程安全**: 使用 `ThreadLocal` 实现，保证多线程环境下的安全性

## 技术实现

### 核心组件

1. **ResponseProperties**: 配置属性类，管理全局响应字段的过滤配置
2. **ResponseFieldContext**: 线程上下文持有者，存储请求级别的字段配置
3. **ResponseFieldInterceptor**: 请求拦截器，解析请求头并设置上下文
4. **ResultSerializer**: `Result` 类的自定义 Jackson 序列化器
5. **RSerializer**: `R` 类的自定义 Jackson 序列化器
6. **JacksonConfig**: 注册自定义序列化器到 ObjectMapper
7. **WebMvcConfig**: 注册响应字段控制拦截器

### 处理流程

1. **请求到达** → `ResponseFieldInterceptor` 拦截
2. **解析请求头** → 提取 `X-Response-Fields` 的值
3. **设置上下文** → 将字段配置存入 `ThreadLocal`
4. **序列化响应** → `ResultSerializer` 或 `RSerializer` 读取上下文
5. **优先级判断** → 有请求配置则使用请求配置，否则使用全局配置
6. **清除上下文** → 请求完成后清除 `ThreadLocal`，避免内存泄漏

### 扩展性

如果需要添加新的过滤模式或字段，只需：

1. 在 `ResponseProperties` 中添加新的配置项
2. 在序列化器中实现相应的逻辑
3. 更新配置文件和文档

## 测试

运行测试验证功能：

```bash
mvn test -Dtest=ResponsePropertiesTest
```

## 最佳实践

### 推荐做法

1. **使用请求级别控制**: 比全局配置更灵活，不需要重启服务
   ```bash
   curl -H "X-Response-Fields: code,data" http://localhost:8080/api/users
   ```

2. **全局配置作为默认**: 为大多数场景设置合理的默认值
   ```yaml
   woodlin:
     response:
       filter-mode: NONE  # 默认返回所有字段
       enable-request-control: true  # 允许请求级别覆盖
   ```

3. **文档化接口**: 在 API 文档中说明支持的请求头参数

### 不推荐做法

1. **频繁修改全局配置**: 需要重启服务，影响可用性
2. **禁用请求级别控制**: 失去灵活性，建议保持启用状态
3. **在生产环境使用 MINIMAL 模式**: 可能丢失重要的调试信息

## 相关文件

- `woodlin-common/src/main/java/com/mumu/woodlin/common/config/ResponseProperties.java`
- `woodlin-common/src/main/java/com/mumu/woodlin/common/response/ResultSerializer.java`
- `woodlin-common/src/main/java/com/mumu/woodlin/common/response/RSerializer.java`
- `woodlin-common/src/main/java/com/mumu/woodlin/common/config/JacksonConfig.java`
- `woodlin-admin/src/main/resources/application.yml`
