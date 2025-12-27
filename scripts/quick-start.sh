#!/bin/bash

###############################################################################
# Woodlin 快速启动脚本 - 使用Docker Compose
# 
# 功能：一键启动完整的开发环境（MySQL + Redis + 应用）
# 
# 使用方法：
#   ./scripts/quick-start.sh         # 启动所有服务
#   ./scripts/quick-start.sh stop    # 停止所有服务
#   ./scripts/quick-start.sh restart # 重启所有服务
#   ./scripts/quick-start.sh logs    # 查看日志
#   ./scripts/quick-start.sh clean   # 清理所有数据（危险操作）
#
# 作者：mumu
# 日期：2025-12-27
###############################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 获取项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# 命令
COMMAND="${1:-start}"

case "$COMMAND" in
  start)
    log_info "====== 启动Woodlin开发环境 ======"
    echo ""
    
    # 检查.env文件
    if [ ! -f ".env" ]; then
      log_warning ".env 文件不存在，从 .env.example 复制..."
      cp .env.example .env
      log_info "请根据需要修改 .env 文件中的配置"
      log_info "默认配置："
      log_info "  - 数据库密码：123456"
      log_info "  - Redis密码：123456"
      echo ""
    fi
    
    # 检查Docker
    if ! command -v docker &> /dev/null; then
      log_error "Docker未安装，请先安装Docker"
      exit 1
    fi
    
    # 检查Docker Compose
    if ! docker compose version &> /dev/null; then
      log_error "Docker Compose未安装或版本过低，需要Docker Compose v2+"
      exit 1
    fi
    
    log_info "启动Docker Compose服务..."
    echo ""
    
    # 启动服务
    docker compose up -d
    
    echo ""
    log_success "服务启动成功！"
    echo ""
    log_info "等待服务完全启动（约60秒）..."
    
    # 等待MySQL启动
    log_info "等待MySQL启动..."
    for i in {1..30}; do
      if docker compose exec -T mysql mysqladmin ping -h localhost --silent &> /dev/null; then
        log_success "MySQL已启动"
        break
      fi
      sleep 2
      echo -n "."
    done
    echo ""
    
    # 检查数据库是否已初始化
    log_info "检查数据库..."
    DB_PASSWORD="${DATABASE_PASSWORD:-123456}"
    if docker compose exec -T mysql mysql -uroot -p"$DB_PASSWORD" -e "USE woodlin; SELECT COUNT(*) FROM sys_user;" &> /dev/null; then
      log_success "数据库已初始化"
    else
      log_warning "数据库未初始化，正在初始化..."
      
      # 初始化数据库
      docker compose exec -T mysql mysql -uroot -p"$DB_PASSWORD" woodlin < sql/mysql/woodlin_complete_schema.sql
      docker compose exec -T mysql mysql -uroot -p"$DB_PASSWORD" woodlin < sql/mysql/woodlin_complete_data.sql
      
      log_success "数据库初始化完成"
    fi
    
    echo ""
    log_success "====== Woodlin开发环境启动完成 ======"
    echo ""
    log_info "访问地址："
    echo "  - 前端: ${GREEN}http://localhost:3000${NC}"
    echo "  - 后端API: ${GREEN}http://localhost:8080/api${NC}"
    echo "  - API文档: ${GREEN}http://localhost:8080/api/doc.html${NC}"
    echo "  - MySQL: ${GREEN}localhost:3306${NC}"
    echo "  - Redis: ${GREEN}localhost:6379${NC}"
    echo ""
    log_info "默认登录账号："
    echo "  - 用户名: ${GREEN}admin${NC}"
    echo "  - 密码: ${GREEN}Passw0rd${NC}"
    echo ""
    log_info "常用命令："
    echo "  - 查看日志: ${YELLOW}./scripts/quick-start.sh logs${NC}"
    echo "  - 停止服务: ${YELLOW}./scripts/quick-start.sh stop${NC}"
    echo "  - 重启服务: ${YELLOW}./scripts/quick-start.sh restart${NC}"
    echo ""
    ;;
    
  stop)
    log_info "====== 停止Woodlin开发环境 ======"
    docker compose stop
    log_success "服务已停止"
    ;;
    
  down)
    log_warning "====== 停止并删除Woodlin容器 ======"
    log_warning "注意：这不会删除数据卷，数据仍然保留"
    docker compose down
    log_success "容器已删除"
    ;;
    
  restart)
    log_info "====== 重启Woodlin开发环境 ======"
    docker compose restart
    log_success "服务已重启"
    ;;
    
  logs)
    log_info "====== 查看服务日志 ======"
    log_info "按 Ctrl+C 退出日志查看"
    echo ""
    docker compose logs -f
    ;;
    
  clean)
    log_error "====== 清理所有数据（危险操作！） ======"
    log_error "这将删除所有容器、镜像和数据卷"
    log_error "所有数据将永久丢失！"
    echo ""
    read -p "确定要继续吗？请输入 'YES' 确认: " -r
    echo ""
    if [ "$REPLY" = "YES" ]; then
      log_warning "清理中..."
      docker compose down -v --rmi local
      log_success "清理完成"
    else
      log_info "取消清理"
    fi
    ;;
    
  status)
    log_info "====== 服务状态 ======"
    docker compose ps
    ;;
    
  *)
    echo "用法: $0 {start|stop|down|restart|logs|clean|status}"
    echo ""
    echo "命令说明："
    echo "  start   - 启动所有服务（默认）"
    echo "  stop    - 停止所有服务（保留容器）"
    echo "  down    - 停止并删除所有容器（保留数据）"
    echo "  restart - 重启所有服务"
    echo "  logs    - 查看服务日志"
    echo "  clean   - 清理所有数据（危险操作）"
    echo "  status  - 查看服务状态"
    echo ""
    exit 1
    ;;
esac
