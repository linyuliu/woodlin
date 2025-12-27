# 🚀 Woodlin 快速开始指南

> 5分钟快速启动Woodlin开发环境

## 📋 前置要求

### 必需工具
- [Docker](https://www.docker.com/) 20+ 和 Docker Compose v2+
- [Git](https://git-scm.com/)

### 开发工具（如果需要本地开发）
- [Java](https://adoptium.net/) 17+
- [Maven](https://maven.apache.org/) 3.8+
- [Node.js](https://nodejs.org/) 20+

## 🎯 方式一：Docker快速启动（推荐）

### 1. 克隆项目
```bash
git clone https://github.com/linyuliu/woodlin.git
cd woodlin
```

### 2. 配置环境变量（可选）
```bash
# 复制环境变量模板
cp .env.example .env

# 使用默认配置即可启动，也可以根据需要修改
# nano .env
```

### 3. 一键启动
```bash
# 使用快速启动脚本
./scripts/quick-start.sh

# 或者直接使用docker compose
docker compose up -d
```

### 4. 访问系统
- **前端**: http://localhost:3000 (需要先构建前端: `docker compose --profile frontend up -d`)
- **后端API**: http://localhost:8080/api
- **API文档**: http://localhost:8080/api/doc.html
- **默认账号**: 
  - 用户名: `admin`
  - 密码: `Passw0rd`

### 5. 查看日志
```bash
# 查看所有服务日志
./scripts/quick-start.sh logs

# 或查看特定服务
docker compose logs -f woodlin-app
docker compose logs -f mysql
docker compose logs -f redis
```

### 6. 停止服务
```bash
# 停止服务（保留数据）
./scripts/quick-start.sh stop

# 停止并删除容器（保留数据）
docker compose down

# 停止并删除所有数据（危险操作！）
./scripts/quick-start.sh clean
```

## 🛠️ 方式二：本地开发环境

适合需要修改代码并实时看到效果的开发者。

### 1. 初始化开发环境
```bash
# 使用初始化脚本自动设置
./scripts/init-dev.sh
```

### 2. 启动MySQL和Redis
```bash
# 仅启动MySQL和Redis
docker compose up -d mysql redis
```

### 3. 启动后端
```bash
# 方式1: 使用Maven直接运行
mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.profiles=dev

# 方式2: 使用开发脚本
./scripts/dev.sh backend
```

### 4. 启动前端
```bash
# 在新终端窗口中
cd woodlin-web
npm run dev

# 或使用开发脚本
./scripts/dev.sh frontend
```

### 5. 访问系统
- **前端**: http://localhost:5173 (Vite开发服务器)
- **后端API**: http://localhost:8080/api
- **API文档**: http://localhost:8080/api/doc.html

## 🔧 常见问题

### 问题1: 端口被占用
**错误**: `Error: bind: address already in use`

**解决**:
```bash
# 检查端口占用
netstat -an | grep 3306  # MySQL
netstat -an | grep 6379  # Redis
netstat -an | grep 8080  # 后端

# 修改 .env 文件中的端口配置
DATABASE_PORT=3307
REDIS_PORT=6380
SERVER_PORT=8081
```

### 问题2: 无法连接数据库
**错误**: `Unable to connect to database`

**解决**:
```bash
# 检查MySQL容器状态
docker ps | grep mysql

# 查看MySQL日志
docker logs woodlin-mysql

# 重新初始化数据库
docker compose down -v  # 删除数据卷
docker compose up -d    # 重新启动
```

### 问题3: 前端无法访问后端
**错误**: 浏览器控制台显示CORS错误

**解决**:
1. 检查后端是否正常运行: `curl http://localhost:8080/api/actuator/health`
2. 检查application-dev.yml中的CORS配置
3. 清除浏览器缓存

### 问题4: 登录后立即退出
**可能原因**: Token存储失败或权限加载失败

**解决**:
1. 打开浏览器开发工具 > Application > Local Storage
2. 清除Local Storage
3. 查看控制台是否有错误
4. 重新登录

## 📚 更多文档

- [完整README](README.md) - 项目介绍和详细文档
- [路由问题修复指南](ROUTING_FIX_GUIDE.md) - 详细的故障排查
- [架构文档](ARCHITECTURE.md) - 系统架构设计
- [开发脚本](scripts/dev.sh) - 开发辅助工具

## 🎨 开发工作流

### 修改后端代码
```bash
# 1. 修改Java代码
# 2. Spring Boot DevTools会自动重新编译和重启

# 如果没有自动重启，手动重启
mvn spring-boot:run -pl woodlin-admin
```

### 修改前端代码
```bash
# 1. 修改Vue代码
# 2. Vite会自动热重载
# 3. 浏览器会自动刷新
```

### 修改数据库
```bash
# 1. 修改SQL文件
# 2. 重新初始化数据库
docker compose down -v mysql
docker compose up -d mysql

# 等待初始化完成（约30秒）
docker compose logs -f mysql
```

## 🐛 故障排查

遇到任何问题时，按以下顺序排查：

### 1. 检查服务状态
```bash
docker compose ps
```

### 2. 查看日志
```bash
# 所有服务
docker compose logs

# 特定服务
docker compose logs woodlin-app
docker compose logs mysql
```

### 3. 验证数据库
```bash
# 连接到MySQL
docker compose exec mysql mysql -uroot -p123456 woodlin

# 检查表和数据
SHOW TABLES;
SELECT * FROM sys_user LIMIT 5;
```

### 4. 测试后端API
```bash
# 健康检查
curl http://localhost:8080/api/actuator/health

# 登录测试
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Passw0rd","loginType":"password"}'
```

### 5. 检查前端
1. 打开浏览器开发工具
2. 查看Console标签的错误日志
3. 查看Network标签的网络请求
4. 检查Application > Local Storage的token

## 💡 提示和技巧

### 快速重启开发环境
```bash
# 重启所有服务
./scripts/quick-start.sh restart

# 仅重启应用（保留数据库）
docker compose restart woodlin-app
```

### 查看实时日志
```bash
# 跟踪所有日志
./scripts/quick-start.sh logs

# 只看后端日志
docker compose logs -f --tail=100 woodlin-app
```

### 进入容器调试
```bash
# 进入应用容器
docker compose exec woodlin-app sh

# 进入MySQL容器
docker compose exec mysql bash
```

### 备份数据
```bash
# 备份MySQL数据
docker compose exec mysql mysqldump -uroot -p123456 woodlin > backup.sql

# 恢复数据
docker compose exec -T mysql mysql -uroot -p123456 woodlin < backup.sql
```

## 🤝 获取帮助

如果遇到问题：

1. 查看 [ROUTING_FIX_GUIDE.md](ROUTING_FIX_GUIDE.md) 详细排查指南
2. 检查 [Issue](https://github.com/linyuliu/woodlin/issues) 是否有类似问题
3. 提交新的 Issue 并提供：
   - 错误日志
   - 操作步骤
   - 环境信息（OS、Docker版本等）

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件
