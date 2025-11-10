# 请求级别响应字段控制 - 功能增强

## 用户反馈

用户 @linyuliu 提出：不希望将响应字段配置固定化，希望能让**请求动态决定**响应字段。

## 解决方案

实现了请求级别的动态响应字段控制，通过请求头 `X-Response-Fields` 来控制每个请求返回的字段。

## 核心特性

### 1. 请求头控制

**语法**：
- **包含模式**: `code,data,message` - 只返回指定的字段
- **排除模式**: `-timestamp,-requestId` - 排除指定的字段

**示例**：
```bash
# 只返回 code 和 data（最简洁）
curl -H "X-Response-Fields: code,data" \
     http://localhost:8080/api/users

# 排除时间戳和请求ID
curl -H "X-Response-Fields: -timestamp,-requestId" \
     http://localhost:8080/api/users

# 自定义返回字段
curl -H "X-Response-Fields: code,message,data" \
     http://localhost:8080/api/users
```

### 2. 优先级

**请求头配置 > 全局配置**

```
请求头 (X-Response-Fields)
    ↓ (如果有)
使用请求头配置
    ↓ (如果没有)
使用全局配置 (application.yml)
    ↓
使用默认配置 (NONE 模式)
```

### 3. 配置项

```yaml
woodlin:
  response:
    # 是否启用请求级别控制（默认: true）
    enable-request-control: true
    # 请求头名称（默认: X-Response-Fields）
    request-header-name: X-Response-Fields
    # 全局默认配置（作为后备）
    filter-mode: NONE
```

## 实现细节

### 新增组件

1. **ResponseFieldContext**
   - 使用 ThreadLocal 存储请求级别的字段配置
   - 线程安全，不会影响其他请求

2. **ResponseFieldInterceptor**
   - 拦截请求，解析 `X-Response-Fields` 头
   - 将配置设置到 ThreadLocal
   - 请求完成后自动清理，防止内存泄漏

3. **WebMvcConfig**
   - 注册拦截器到 Spring MVC

### 修改组件

1. **ResultSerializer / RSerializer**
   - 优先检查 ThreadLocal 中的请求配置
   - 如果没有请求配置，则使用全局配置

2. **ResponseProperties**
   - 新增 `enableRequestControl` 配置
   - 新增 `requestHeaderName` 配置

## 使用场景

### 场景1: 移动端和Web端使用不同格式

```bash
# 移动端请求 - 简化响应
curl -H "X-Response-Fields: code,data" \
     -H "User-Agent: MobileApp/1.0" \
     http://localhost:8080/api/users

# Web端请求 - 完整响应（使用全局默认）
curl http://localhost:8080/api/users
```

### 场景2: 第三方系统集成

```bash
# 第三方系统只需要状态码和数据
curl -H "X-Response-Fields: code,data" \
     http://localhost:8080/api/users
```

### 场景3: 调试时包含追踪信息

```bash
# 开发环境调试，需要完整的追踪信息
curl -H "X-Response-Fields: code,message,data,timestamp,requestId" \
     http://localhost:8080/api/users
```

## 技术优势

1. **灵活性**: 每个请求可以有不同的响应格式
2. **无需重启**: 不需要修改配置文件和重启服务
3. **向后兼容**: 默认行为不变，不影响现有代码
4. **线程安全**: 使用 ThreadLocal，多线程环境安全
5. **自动清理**: 拦截器自动清理上下文，无内存泄漏风险

## 对比表

| 特性 | 全局配置 | 请求级别控制 |
|------|---------|------------|
| 灵活性 | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| 配置方式 | 配置文件 | 请求头 |
| 生效方式 | 需要重启 | 立即生效 |
| 适用场景 | 统一默认 | 个性化需求 |
| 优先级 | 低 | 高 |

## 测试

```bash
# 编译测试
mvn clean compile -DskipTests

# 运行单元测试
mvn test -Dtest=ResponsePropertiesTest -pl woodlin-common

# 结果: ✅ 所有测试通过 (7/7)
```

## 文档

详细使用说明见：`docs/RESPONSE_FIELD_CONFIGURATION.md`

---

**实现时间**: 2025-11-10  
**提交**: df9231d  
**反馈**: @linyuliu
