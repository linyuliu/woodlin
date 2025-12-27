# 🚀 Woodlin 快速参考卡

> 一页纸解决所有问题！

## ⚡ 最快启动方式

```bash
./scripts/quick-start.sh
```

访问: http://localhost:8080/api
登录: admin / Passw0rd

## 📦 完整命令列表

### Docker方式（推荐新手）

```bash
# 启动所有服务
./scripts/quick-start.sh

# 查看日志
./scripts/quick-start.sh logs

# 停止服务（保留数据）
./scripts/quick-start.sh stop

# 重启服务
./scripts/quick-start.sh restart

# 查看状态
./scripts/quick-start.sh status

# 完全清理（危险！删除所有数据）
./scripts/quick-start.sh clean
```

### 本地开发方式（推荐开发者）

```bash
# 一次性初始化
./scripts/init-dev.sh

# 仅启动数据库
docker compose up -d mysql redis

# 启动后端
./scripts/dev.sh backend
# 或
mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.profiles=dev

# 启动前端（新终端）
./scripts/dev.sh frontend
# 或
cd woodlin-web && npm run dev
```

## 🔍 快速检查

### 检查服务状态
```bash
# Docker方式
docker compose ps

# 预期输出: 所有服务都应该是 "Up (healthy)"
```

### 测试后端
```bash
# 健康检查
curl http://localhost:8080/api/actuator/health
# 预期: {"status":"UP"}

# 测试登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Passw0rd","loginType":"password"}'
# 预期: 包含token的JSON响应
```

### 测试前端
1. 浏览器打开: http://localhost:5173 (本地) 或 http://localhost:8080/api (Docker)
2. 输入: admin / Passw0rd
3. 应该能看到Dashboard

## 🐛 快速故障排查

### 问题: 无法连接数据库
```bash
# 检查MySQL
docker ps | grep mysql

# 查看日志
docker compose logs -f mysql

# 重启MySQL
docker compose restart mysql
```

### 问题: 前端无法访问后端（CORS错误）
```bash
# 1. 确认已更新application-dev.yml中的CORS配置
# 2. 清除浏览器缓存和Local Storage
# 3. 重启后端服务
```

### 问题: 登录后立即退出
```bash
# 1. 打开浏览器开发者工具（F12）
# 2. 清除Application > Local Storage
# 3. 查看Console标签的错误信息
# 4. 重新登录
```

### 问题: 端口被占用
```bash
# 修改.env文件中的端口
nano .env

# 修改这些行:
DATABASE_PORT=3307  # 原3306
REDIS_PORT=6380     # 原6379
SERVER_PORT=8081    # 原8080

# 重启服务
./scripts/quick-start.sh restart
```

### 问题: 需要重新初始化数据库
```bash
# 方式1: Docker完全清理
./scripts/quick-start.sh clean
./scripts/quick-start.sh

# 方式2: 仅删除MySQL数据卷
docker compose down -v mysql
docker compose up -d mysql
```

## 📝 重要配置文件

| 文件 | 说明 | 何时修改 |
|------|------|----------|
| `.env` | 环境变量 | 首次启动前（可选） |
| `application-dev.yml` | 后端开发配置 | 已修复，无需修改 |
| `woodlin-web/.env.development` | 前端开发配置 | 通常无需修改 |
| `docker-compose.yml` | Docker配置 | 已配置好，无需修改 |

## 🔗 访问地址

### Docker方式
- 后端API: http://localhost:8080/api
- API文档: http://localhost:8080/api/doc.html
- Druid监控: http://localhost:8080/api/druid
- 前端: http://localhost:3000 (需要 `--profile frontend`)

### 本地开发方式
- 前端: http://localhost:5173
- 后端API: http://localhost:8080/api
- API文档: http://localhost:8080/api/doc.html
- MySQL: localhost:3306
- Redis: localhost:6379

## 🔑 默认账号

| 账号 | 密码 | 权限 |
|------|------|------|
| admin | Passw0rd | 超级管理员（所有权限） |
| demo | Passw0rd | 普通用户（部分权限） |

## 📚 详细文档

| 文档 | 用途 |
|------|------|
| [ROUTING_FIX_SUMMARY_CN.md](ROUTING_FIX_SUMMARY_CN.md) | 中文详细说明，包含根本原因分析 |
| [ROUTING_FIX_GUIDE.md](ROUTING_FIX_GUIDE.md) | 英文完整故障排查指南 |
| [QUICKSTART.md](QUICKSTART.md) | 英文5分钟快速开始 |
| [README.md](README.md) | 项目总览 |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 系统架构 |

## 💡 实用技巧

### 查看实时日志
```bash
# 所有服务
./scripts/quick-start.sh logs

# 仅后端
docker compose logs -f woodlin-app

# 最后100行
docker compose logs --tail=100 woodlin-app
```

### 进入容器调试
```bash
# 进入应用容器
docker compose exec woodlin-app sh

# 进入MySQL
docker compose exec mysql bash
mysql -uroot -p123456 woodlin
```

### 备份和恢复数据
```bash
# 备份
docker compose exec mysql mysqldump -uroot -p123456 woodlin > backup_$(date +%Y%m%d).sql

# 恢复
docker compose exec -T mysql mysql -uroot -p123456 woodlin < backup_20250101.sql
```

### 清理Docker资源
```bash
# 清理未使用的镜像
docker image prune -a

# 清理未使用的卷
docker volume prune

# 清理未使用的网络
docker network prune
```

## ⚠️ 注意事项

1. **首次启动较慢**: MySQL初始化约需要30-60秒
2. **密码修改**: 生产环境务必修改`.env`中的所有密码
3. **端口冲突**: 如果端口被占用，修改`.env`中的端口配置
4. **数据持久化**: Docker volumes保存数据，`down -v`会删除数据
5. **资源使用**: 建议至少4GB RAM，10GB磁盘空间

## 🆘 获取帮助

1. 📖 先查看 [ROUTING_FIX_SUMMARY_CN.md](ROUTING_FIX_SUMMARY_CN.md)
2. 🔍 搜索 [GitHub Issues](https://github.com/linyuliu/woodlin/issues)
3. 💬 提交新Issue（附上错误日志和环境信息）

## ✅ 验证清单

启动后检查以下项目：

- [ ] MySQL容器运行中且健康
- [ ] Redis容器运行中且健康
- [ ] 后端应用运行中且健康
- [ ] 可以访问API文档页面
- [ ] 可以用admin/Passw0rd登录
- [ ] 登录后能看到Dashboard
- [ ] 左侧菜单显示完整
- [ ] 可以访问用户管理等页面
- [ ] 浏览器控制台无错误

---

**🎉 一切正常！开始享受Woodlin吧！**

如有问题，请查看详细文档或提Issue。
