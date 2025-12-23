# Dynamic Routing Optimization

## 问题描述 (Problem Description)

原有系统存在以下问题：
1. 每次用户访问都会查询数据库获取路由信息，导致数据库压力过大
2. 没有实现路由缓存机制
3. 用户登录后可能出现页面不显示的问题

## 解决方案 (Solution)

### 1. 后端路由缓存 (Backend Route Caching)

#### 1.1 添加路由缓存服务
在 `PermissionCacheService` 中添加了以下方法：

- `getUserRoutes(Long userId)` - 从缓存获取用户路由
- `cacheUserRoutes(Long userId, List<T> routes)` - 缓存用户路由
- `evictUserRouteCache(Long userId)` - 清除指定用户的路由缓存
- `evictAllUserRoutes()` - 清除所有用户的路由缓存

#### 1.2 缓存策略
- **缓存键格式**: `auth:user:routes:{userId}`
- **过期时间**: 与权限缓存相同（默认从配置文件读取）
- **缓存更新**: 
  - 用户角色变更时自动清除
  - 权限数据变更时自动清除
  - 支持延迟双删策略防止缓存不一致

#### 1.3 实现位置
```
woodlin-system/woodlin-system-security/src/main/java/com/mumu/woodlin/security/service/PermissionCacheService.java
woodlin-system/woodlin-system-core/src/main/java/com/mumu/woodlin/system/service/impl/SysPermissionServiceImpl.java
woodlin-system/woodlin-system-core/src/main/java/com/mumu/woodlin/system/service/impl/SysRoleServiceImpl.java
```

### 2. 路由数据流程 (Route Data Flow)

```
用户登录
  ↓
前端调用 /auth/routes API
  ↓
后端 SysPermissionServiceImpl.selectRoutesByUserId()
  ↓
1. 检查 Redis 缓存
   - 有缓存 → 直接返回 (快速响应)
   - 无缓存 → 查询数据库
  ↓
2. 查询数据库 (仅当缓存未命中时)
   - 获取用户所有权限（包括继承的权限）
   - 过滤菜单和目录类型
   - 构建树形结构
  ↓
3. 缓存结果到 Redis
  ↓
4. 返回路由数据给前端
  ↓
前端 permission store 转换路由
  ↓
动态添加到 Vue Router
  ↓
用户可以访问页面
```

### 3. 前端路由转换 (Frontend Route Conversion)

#### 3.1 后端路由格式
```json
[
  {
    "id": 1,
    "parentId": 0,
    "name": "dashboardView",
    "path": "dashboard",
    "component": "DashboardView",
    "meta": {
      "title": "仪表板",
      "icon": "dashboard-outline",
      "hideInMenu": false,
      "affix": true,
      "keepAlive": true,
      "permissions": ["dashboard:view"],
      "order": 0
    }
  },
  {
    "id": 10,
    "parentId": 0,
    "name": "system",
    "path": "system",
    "component": null,
    "redirect": "/system/user",
    "meta": {
      "title": "系统管理",
      "icon": "settings-outline"
    },
    "children": [
      {
        "id": 11,
        "parentId": 10,
        "name": "systemUser",
        "path": "user",
        "component": "system/UserView",
        "meta": {
          "title": "用户管理",
          "icon": "people-outline",
          "permissions": ["system:user:view"]
        }
      }
    ]
  }
]
```

#### 3.2 前端转换逻辑
1. 创建根路由 `/` 并使用 `AdminLayout` 作为布局组件
2. 将后端返回的路由树作为根路由的 children
3. 递归转换每个路由节点：
   - 保持路径不变（相对路径）
   - 动态加载组件：`/src/views/${component}.vue`
   - 转换 meta 信息
   - 递归处理 children

#### 3.3 组件路径映射
- 后端 `DashboardView` → 前端 `/src/views/DashboardView.vue`
- 后端 `system/UserView` → 前端 `/src/views/system/UserView.vue`

### 4. 数据库权限配置 (Database Permission Configuration)

使用 `sql/mysql/enhanced_permissions.sql` 初始化权限数据，该文件包含：

1. **完整的菜单结构**：
   - 仪表板 (Dashboard)
   - 系统管理 (System Management)
     - 用户管理、角色管理、部门管理、权限管理、字典管理、配置管理、系统设置
   - 数据源管理 (Datasource Management)
   - 租户管理 (Tenant Management)
   - 文件管理 (File Management)
   - 任务管理 (Task Management)
   - 开发工具 (Development Tools)

2. **与前端路由完全匹配**：
   - 路径 (path) 与前端路由定义一致
   - 组件 (component) 指向正确的 Vue 组件
   - 图标 (icon) 使用 ionicons5

3. **权限分配**：
   - 管理员角色 (role_id=1) 拥有所有权限
   - 普通用户角色 (role_id=2) 只有查看权限

## 性能优化效果 (Performance Improvement)

### 优化前 (Before)
- 每次页面访问都查询数据库
- 数据库查询复杂（包含权限继承计算）
- 响应时间：100-300ms（取决于数据库负载）

### 优化后 (After)
- 首次访问查询数据库并缓存
- 后续访问直接从 Redis 获取
- 响应时间：<10ms（Redis 缓存命中）
- 数据库负载显著降低

### 缓存失效场景
- 用户角色变更 → 清除该用户缓存
- 权限数据变更 → 清除所有用户缓存
- 角色权限变更 → 清除所有用户缓存

## 使用说明 (Usage Guide)

### 1. 数据库初始化
```bash
# 在 MySQL 中执行
mysql -u root -p woodlin < sql/mysql/enhanced_permissions.sql
```

### 2. 配置缓存参数
在 `application.yml` 中配置：
```yaml
woodlin:
  cache:
    permission:
      enabled: true  # 启用权限缓存
      expire-seconds: 3600  # 缓存过期时间（秒）
      role-expire-seconds: 7200  # 角色权限缓存过期时间
    delayed-double-delete:
      enabled: true  # 启用延迟双删
      delay-millis: 500  # 延迟时间（毫秒）
```

### 3. 测试步骤

#### 3.1 启动应用
```bash
# 确保 MySQL 和 Redis 已启动
docker compose up -d mysql redis

# 启动后端
mvn spring-boot:run -pl woodlin-admin

# 启动前端
cd woodlin-web
npm run dev
```

#### 3.2 测试路由加载
1. 打开浏览器开发者工具（F12）
2. 访问 http://localhost:5173/
3. 使用 admin / Passw0rd 登录
4. 查看 Console 日志：
   - 应该看到 "🌐 从后端获取用户路由..." 
   - 应该看到 "✅ 成功获取后端路由: X 个"
   - 应该看到 "✅ 路由转换完成: X 个"
   - 应该看到组件加载日志

#### 3.3 验证缓存效果
```bash
# 连接 Redis 查看缓存
redis-cli

# 查看用户路由缓存键
KEYS auth:user:routes:*

# 查看具体缓存内容（替换 1 为实际用户ID）
GET auth:user:routes:1

# 查看缓存过期时间
TTL auth:user:routes:1
```

#### 3.4 测试缓存清除
1. 修改用户角色
2. 重新登录
3. 应该看到路由重新加载

### 4. 调试技巧

#### 4.1 查看后端日志
```bash
# 搜索路由相关日志
tail -f logs/woodlin.log | grep -E "路由|Route|Permission"
```

#### 4.2 查看前端日志
打开浏览器 Console，搜索：
- "📥 加载用户信息"
- "🔄 生成动态路由"
- "✅ 路由已生成"
- "📦 加载组件"
- "⚠️ 找不到组件"

#### 4.3 常见问题

**问题1：登录后没有菜单显示**
- 检查数据库是否有权限数据
- 检查用户是否有分配角色
- 检查角色是否有分配权限
- 查看浏览器 Console 日志

**问题2：页面显示 404**
- 检查组件路径是否正确
- 检查 Vue 组件文件是否存在
- 查看 "找不到组件" 的警告日志

**问题3：缓存未生效**
- 检查 Redis 是否运行
- 检查配置文件中缓存是否启用
- 查看 Redis 中是否有对应的 key

## 技术细节 (Technical Details)

### 1. 缓存键设计
```
auth:user:permissions:{userId}  - 用户权限缓存
auth:user:roles:{userId}        - 用户角色缓存
auth:user:routes:{userId}       - 用户路由缓存（新增）
auth:role:permissions:{roleId}  - 角色权限缓存
```

### 2. 路由树构建算法
使用 Map 分组算法，时间复杂度 O(n)：
1. 将所有路由按 parentId 分组
2. 从根节点（parentId=0）开始构建树
3. 递归为每个节点添加 children

### 3. 前端组件动态加载
使用 Vite 的 `import.meta.glob` 预加载所有组件：
```typescript
const componentModules = import.meta.glob('@/views/**/*.vue')
```

### 4. 安全性考虑
- 路由缓存基于用户ID，不会泄露其他用户的数据
- 权限验证在后端完成，前端路由只是展示
- 缓存键使用 Redis 命名空间隔离

## 维护指南 (Maintenance Guide)

### 1. 添加新菜单
1. 在数据库中添加权限记录
2. 创建对应的 Vue 组件
3. 更新角色权限关联
4. 清除缓存测试

### 2. 修改菜单结构
1. 更新数据库权限记录
2. 调用 `evictAllUserRoutes()` 清除所有路由缓存
3. 用户重新登录后会获取新的路由

### 3. 监控缓存性能
```bash
# Redis 命令
INFO stats  # 查看统计信息
INFO keyspace  # 查看键空间信息
MONITOR  # 实时监控命令执行
```

## 参考文档 (References)

- [Vue Router 动态路由](https://router.vuejs.org/zh/guide/advanced/dynamic-routing.html)
- [Redis 缓存最佳实践](https://redis.io/docs/manual/patterns/)
- [RBAC 权限模型](https://en.wikipedia.org/wiki/Role-based_access_control)
