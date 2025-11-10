# 响应字段动态配置功能说明

## 概述

为了满足不同系统集成的需求，Woodlin 现在支持动态配置全局响应结果中包含的字段。这样，在与其他系统对接时，可以根据需求灵活控制返回的字段，使得接口更加灵活和优雅。

## 功能特性

### 1. 响应字段过滤模式

支持三种过滤模式：

- **NONE（默认）**: 不过滤，包含所有字段（保持向后兼容）
- **MINIMAL**: 最小模式，仅包含 `code` 和 `data` 字段
- **CUSTOM**: 自定义模式，根据配置决定包含哪些字段

### 2. 可配置字段

- `code`: 响应状态码
- `message`: 响应消息
- `data`: 响应数据（始终包含）
- `timestamp`: 响应时间戳
- `requestId`: 请求ID（用于链路追踪）

## 配置方法

在 `application.yml` 中添加以下配置：

```yaml
woodlin:
  response:
    # 响应字段过滤模式
    # - NONE: 不过滤，包含所有字段（默认）
    # - MINIMAL: 最小模式，仅包含 code 和 data
    # - CUSTOM: 自定义模式，根据下面的 include* 配置决定
    filter-mode: CUSTOM
    
    # 以下配置仅在 filter-mode=CUSTOM 时生效
    include-timestamp: false    # 是否包含时间戳字段
    include-request-id: false   # 是否包含请求ID字段
    include-message: true       # 是否包含消息字段
    include-code: true          # 是否包含状态码字段
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
```

## 使用场景

### 场景1: 与第三方系统集成

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

### 场景2: 简化移动端接口

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

### 场景3: 保持默认行为（推荐用于生产环境）

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
2. **数据字段**: `data` 字段始终会被包含（如果不为 null）
3. **生产环境建议**: 建议在生产环境中使用 `NONE` 模式，保留所有字段用于调试和监控
4. **配置优先级**: 环境变量配置 > application.yml 配置 > 默认值

## 技术实现

### 核心组件

1. **ResponseProperties**: 配置属性类，管理响应字段的过滤配置
2. **ResultSerializer**: `Result` 类的自定义 Jackson 序列化器
3. **RSerializer**: `R` 类的自定义 Jackson 序列化器
4. **JacksonConfig**: 注册自定义序列化器到 ObjectMapper

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

## 相关文件

- `woodlin-common/src/main/java/com/mumu/woodlin/common/config/ResponseProperties.java`
- `woodlin-common/src/main/java/com/mumu/woodlin/common/response/ResultSerializer.java`
- `woodlin-common/src/main/java/com/mumu/woodlin/common/response/RSerializer.java`
- `woodlin-common/src/main/java/com/mumu/woodlin/common/config/JacksonConfig.java`
- `woodlin-admin/src/main/resources/application.yml`
