# 在线用户跟踪系统使用说明

## 概述

基于Redis的高效在线用户跟踪和统计系统，支持实时监控在线用户、统计在线时长等功能。

## 系统架构

### Redis数据结构设计

```
1. Hash: online:users:info
   - 存储用户详细信息（JSON格式）
   - 支持批量获取多个用户信息

2. ZSet: online:users:login_time  
   - Key: userId
   - Score: 登录时间戳
   - 支持按时间排序和范围查询

3. String: online:session:{userId}
   - 记录本次会话开始时间
   - 用于计算会话时长

4. AtomicLong: stats:duration:{userId}
   - 用户累计在线时长（秒）
   - 支持原子增量操作

5. AtomicLong: stats:daily:{userId}:{date}
   - 用户每日在线时长（秒）
   - 自动过期（90天）
```

## API接口

### 1. 获取在线用户数量
```http
GET /security/online-users/count
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": 42
}
```

### 2. 获取在线用户列表（分页）
```http
GET /security/online-users/list?page=1&pageSize=20
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "userId": "1001",
        "username": "张三",
        "ip": "192.168.1.100",
        "loginTime": 1703577600000,
        "lastActivityTime": 1703581200000,
        "browser": "Chrome 120",
        "os": "Windows 10"
      }
    ],
    "total": 42,
    "page": 1,
    "pageSize": 20,
    "pages": 3
  }
}
```

### 3. 获取用户累计在线时长
```http
GET /security/online-users/duration/total/{userId}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": 86400  // 秒，表示24小时
}
```

### 4. 获取用户当日在线时长
```http
GET /security/online-users/duration/daily/{userId}
```

### 5. 获取用户指定日期在线时长
```http
GET /security/online-users/duration/{userId}/{date}
```
示例：`GET /security/online-users/duration/1001/2025-12-26`

### 6. 强制用户下线
```http
POST /security/online-users/offline/{userId}
```

### 7. 清理超时用户
```http
POST /security/online-users/cleanup?timeoutSeconds=1800
```

## 后端使用方法

### 1. 用户登录时记录上线
```java
@Autowired
private OnlineUserService onlineUserService;

public void handleUserLogin(User user, HttpServletRequest request) {
    String userId = user.getUserId().toString();
    String username = user.getUsername();
    String ip = getClientIp(request);
    String browser = getBrowserInfo(request);
    String os = getOSInfo(request);
    
    onlineUserService.userOnline(userId, username, ip, browser, os);
}
```

### 2. 用户活动时更新时间（前端上报）
```java
@PostMapping("/activity/update")
public R<Void> updateUserActivity() {
    String userId = StpUtil.getLoginIdAsString();
    onlineUserService.updateUserActivity(userId);
    return R.ok();
}
```

### 3. 用户登出时记录下线
```java
public void handleUserLogout(String userId) {
    onlineUserService.userOffline(userId);
}
```

### 4. 定时清理超时用户
```java
@Scheduled(fixedRate = 60000) // 每分钟执行一次
public void cleanupInactiveUsers() {
    int cleaned = onlineUserService.cleanupInactiveUsers(1800); // 30分钟超时
    if (cleaned > 0) {
        log.info("清理了 {} 个超时用户", cleaned);
    }
}
```

## 前端集成

### 1. 定期上报用户活动
```typescript
// 前端监听用户交互事件（鼠标、键盘等）
let lastActivityTime = Date.now();

// 监听用户交互
document.addEventListener('mousemove', () => {
  lastActivityTime = Date.now();
});

document.addEventListener('keydown', () => {
  lastActivityTime = Date.now();
});

// 每30秒上报一次活动
setInterval(() => {
  const now = Date.now();
  if (now - lastActivityTime < 30000) { // 最近30秒有活动
    fetch('/security/activity/update', { method: 'POST' });
  }
}, 30000);
```

### 2. 显示在线用户列表
```typescript
async function getOnlineUsers(page = 1, pageSize = 20) {
  const response = await fetch(
    `/security/online-users/list?page=${page}&pageSize=${pageSize}`
  );
  const result = await response.json();
  return result.data;
}
```

### 3. 显示在线用户数量
```typescript
async function getOnlineUserCount() {
  const response = await fetch('/security/online-users/count');
  const result = await response.json();
  return result.data;
}
```

## 性能优化说明

### 1. 批量操作
- 使用Redis Hash存储用户信息，支持批量读取（`getAll()`）
- 避免逐个获取造成的网络开销

### 2. 时间复杂度优化
- 在线人数统计：O(1) - 直接获取ZSet大小
- 分页查询：O(log n + m) - ZSet范围查询
- 更新活动时间：O(1) - 字符串替换避免JSON解析

### 3. 内存优化
- 会话数据设置24小时过期
- 每日统计数据保留90天自动清理
- 用户下线时立即清理相关缓存

### 4. 原子操作
- 使用Redis AtomicLong保证并发安全
- 在线时长累加使用原子增量操作

## 国标支持

性别字段已更新为符合 **GB/T 2261.1-2003** 标准：

| 代码 | 含义 | 英文 (ISO 5218) |
|-----|------|----------------|
| 0 | 未知的性别 | Unknown |
| 1 | 男性 | Male |
| 2 | 女性 | Female |
| 9 | 未说明的性别 | Not Applicable |

## 监控和维护

### 查看Redis中的数据
```bash
# 查看在线用户数量
redis-cli ZCARD online:users:login_time

# 查看所有在线用户ID
redis-cli ZRANGE online:users:login_time 0 -1

# 查看用户详细信息
redis-cli HGET online:users:info {userId}

# 查看用户累计在线时长
redis-cli GET stats:duration:{userId}
```

### 清理所有在线用户数据
```bash
# 谨慎使用！会清理所有在线用户数据
redis-cli DEL online:users:info
redis-cli DEL online:users:login_time
redis-cli KEYS "online:session:*" | xargs redis-cli DEL
```

## 注意事项

1. **前端上报频率**：建议30秒上报一次，避免频繁请求
2. **超时设置**：根据业务需求设置合理的超时时间（建议30分钟）
3. **定时清理**：建议每分钟执行一次超时清理
4. **数据保留**：每日统计默认保留90天，可根据需要调整
5. **并发控制**：Redis操作已优化为原子操作，支持高并发

## 扩展功能

可基于现有架构扩展的功能：

1. **在线用户消息推送**：基于在线用户列表推送系统消息
2. **用户行为分析**：记录用户访问的页面和操作
3. **用户活跃度统计**：分析用户活跃时间段
4. **并发用户峰值统计**：记录系统同时在线最高人数
5. **部门/角色在线统计**：统计不同部门或角色的在线情况
