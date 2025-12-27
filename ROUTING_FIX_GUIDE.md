# Woodlin 路由问题修复指南 / Routing Issues Fix Guide

## 问题描述 / Problem Description

路由全部无法访问，前后端都进不去。
All routes are inaccessible, both frontend and backend are unreachable.

## 问题根因分析 / Root Cause Analysis

根据代码分析，可能的问题包括：

1. **数据库未初始化** - MySQL数据库不存在或未初始化数据
2. **后端服务未启动** - Spring Boot应用未运行
3. **前端代理配置错误** - Vite开发服务器代理配置问题
4. **CORS配置问题** - 跨域请求被阻止
5. **路由守卫死循环** - 前端路由守卫可能导致无限重定向

## 完整修复步骤 / Complete Fix Steps

### 第一步：数据库初始化 / Step 1: Database Initialization

```bash
# 1. 启动MySQL服务（如果使用Docker）
docker run -d \
  --name woodlin-mysql \
  -e MYSQL_ROOT_PASSWORD=Passw0rd \
  -e MYSQL_DATABASE=woodlin \
  -p 3306:3306 \
  mysql:8.0

# 2. 等待MySQL启动完成（约30秒）
sleep 30

# 3. 初始化数据库架构
docker exec -i woodlin-mysql mysql -uroot -pPassw0rd woodlin < sql/mysql/woodlin_complete_schema.sql

# 4. 初始化数据库数据
docker exec -i woodlin-mysql mysql -uroot -pPassw0rd woodlin < sql/mysql/woodlin_complete_data.sql

# 5. 验证数据库初始化
docker exec -it woodlin-mysql mysql -uroot -pPassw0rd -e "USE woodlin; SELECT COUNT(*) FROM sys_user;"
```

**或者使用本地MySQL：**

```bash
# 1. 创建数据库
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS woodlin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 初始化架构
mysql -uroot -p woodlin < sql/mysql/woodlin_complete_schema.sql

# 3. 初始化数据
mysql -uroot -p woodlin < sql/mysql/woodlin_complete_data.sql

# 4. 验证
mysql -uroot -p -e "USE woodlin; SELECT username, nick_name FROM sys_user;"
```

### 第二步：启动Redis / Step 2: Start Redis

```bash
# 使用Docker启动Redis（可选，如果需要完整功能）
docker run -d \
  --name woodlin-redis \
  -p 6379:6379 \
  redis:7-alpine

# 或者使用本地Redis
redis-server &
```

### 第三步：配置后端 / Step 3: Configure Backend

1. 检查 `woodlin-admin/src/main/resources/application-dev.yml` 配置：

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/woodlin?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
          username: root
          password: Passw0rd  # 修改为你的MySQL密码
          
  data:
    redis:
      host: localhost
      port: 6379
      password:  # 如果Redis有密码则填写
```

2. 确认CORS配置正确（在 `application.yml` 中）：

```yaml
woodlin:
  cors:
    enabled: true
    allowed-origin-patterns:
      - http://localhost:*
      - http://127.0.0.1:*
```

### 第四步：启动后端服务 / Step 4: Start Backend Service

```bash
# 方式1：使用Maven直接运行
cd /home/runner/work/woodlin/woodlin
mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.profiles=dev

# 方式2：先构建再运行
mvn clean package -DskipTests
java -jar woodlin-admin/target/woodlin-admin-1.0.0.jar --spring.profiles.active=dev

# 方式3：使用开发脚本
./scripts/dev.sh backend
```

**验证后端启动成功：**

```bash
# 检查健康状态
curl http://localhost:8080/api/actuator/health

# 测试登录接口
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Passw0rd","loginType":"password"}'
```

预期响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "...",
    "userInfo": {...}
  }
}
```

### 第五步：启动前端服务 / Step 5: Start Frontend Service

```bash
# 进入前端目录
cd woodlin-web

# 安装依赖（如果未安装）
npm install

# 启动开发服务器
npm run dev
```

**验证前端启动成功：**

1. 浏览器访问：http://localhost:5173/
2. 应该看到登录页面
3. 使用默认账号登录：
   - 用户名：`admin`
   - 密码：`Passw0rd`

### 第六步：测试路由 / Step 6: Test Routes

登录成功后，测试以下路由：

1. ✅ Dashboard: http://localhost:5173/dashboard
2. ✅ 用户管理: http://localhost:5173/system/user
3. ✅ 角色管理: http://localhost:5173/system/role
4. ✅ 部门管理: http://localhost:5173/system/dept
5. ✅ 权限管理: http://localhost:5173/system/permission

## 常见问题排查 / Troubleshooting

### 问题1：无法连接数据库

**症状：** 后端启动失败，日志显示 "Unable to connect to database"

**解决方案：**
```bash
# 检查MySQL是否运行
docker ps | grep mysql
# 或
ps aux | grep mysql

# 检查端口是否被占用
netstat -an | grep 3306

# 检查MySQL日志
docker logs woodlin-mysql
# 或
tail -f /var/log/mysql/error.log
```

### 问题2：前端无法访问后端API

**症状：** 浏览器控制台显示CORS错误或网络错误

**解决方案：**

1. 检查后端是否正常运行：
```bash
curl http://localhost:8080/api/actuator/health
```

2. 检查前端代理配置（`woodlin-web/vite.config.ts`）：
```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

3. 检查浏览器控制台的Network标签，查看请求详情

### 问题3：登录后立即退出或无限重定向

**症状：** 输入用户名密码后，页面一直在加载或跳回登录页

**可能原因：**
1. Token存储失败
2. 用户权限获取失败
3. 路由守卫配置错误

**解决方案：**

1. 打开浏览器开发者工具 > Application > Local Storage
2. 检查是否有 `token` 键
3. 查看控制台是否有错误日志
4. 清除浏览器缓存和Local Storage后重试

### 问题4：部分路由404

**症状：** 登录成功但访问某些菜单显示404

**解决方案：**

1. 检查后端权限表数据：
```sql
SELECT permission_id, parent_id, permission_name, permission_code, permission_type 
FROM sys_permission 
WHERE deleted = '0' 
ORDER BY sort ASC;
```

2. 检查用户角色权限：
```sql
SELECT r.role_name, p.permission_name, p.permission_code
FROM sys_user_role ur
JOIN sys_role r ON ur.role_id = r.role_id
JOIN sys_role_permission rp ON r.role_id = rp.role_id
JOIN sys_permission p ON rp.permission_id = p.permission_id
WHERE ur.user_id = 1;
```

3. 前端清除缓存，重新登录

## 完全重置系统 / Complete System Reset

如果以上步骤都无法解决问题，可以完全重置系统：

```bash
# 1. 停止所有服务
pkill -f "woodlin-admin"
pkill -f "vite"

# 2. 清理Docker容器（如果使用）
docker stop woodlin-mysql woodlin-redis
docker rm woodlin-mysql woodlin-redis

# 3. 删除数据库（谨慎操作！）
mysql -uroot -p -e "DROP DATABASE IF EXISTS woodlin;"

# 4. 清理前端缓存
cd woodlin-web
rm -rf node_modules dist .vite
npm cache clean --force

# 5. 清理后端构建
cd ..
mvn clean

# 6. 重新执行第一步到第五步
```

## 验证清单 / Verification Checklist

完成修复后，确认以下所有项目：

- [ ] MySQL数据库已创建并初始化数据
- [ ] Redis服务正常运行（可选）
- [ ] 后端服务成功启动，健康检查通过
- [ ] 前端开发服务器成功启动
- [ ] 可以访问登录页面 (http://localhost:5173/)
- [ ] 可以使用 admin/Passw0rd 登录
- [ ] 登录后可以看到Dashboard
- [ ] 左侧菜单显示正常
- [ ] 可以访问各个系统管理页面
- [ ] 浏览器控制台无错误日志

## 默认账号信息 / Default Credentials

- **超级管理员**
  - 用户名：`admin`
  - 密码：`Passw0rd`
  - 权限：所有权限

- **演示用户**
  - 用户名：`demo`
  - 密码：`Passw0rd`
  - 权限：有限权限

## 技术支持 / Technical Support

如果问题仍然存在，请提供以下信息：

1. 后端启动日志（最后100行）
2. 前端浏览器控制台错误日志
3. 数据库连接配置（隐藏密码）
4. 操作系统和环境信息
5. 具体的错误现象和复现步骤

## 相关文档 / Related Documentation

- [README.md](README.md) - 项目总览
- [ARCHITECTURE.md](ARCHITECTURE.md) - 系统架构
- [scripts/dev.sh](scripts/dev.sh) - 开发脚本
- [docker-compose.yml](docker-compose.yml) - Docker部署
