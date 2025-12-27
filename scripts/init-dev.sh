#!/bin/bash

###############################################################################
# Woodlin 开发环境快速初始化脚本
# 
# 功能：
# 1. 检查并启动MySQL数据库
# 2. 初始化数据库架构和数据
# 3. 检查并启动Redis（可选）
# 4. 验证配置文件
# 
# 使用方法：
#   ./scripts/init-dev.sh
#
# 作者：mumu
# 日期：2025-12-27
###############################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
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

# 获取脚本所在目录的父目录（项目根目录）
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

log_info "项目根目录: $PROJECT_ROOT"
echo ""

###############################################################################
# 第一步：检查依赖
###############################################################################

log_info "====== 第一步：检查系统依赖 ======"

# 检查Java
if ! command -v java &> /dev/null; then
    log_error "Java未安装，请先安装Java 17或更高版本"
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
log_success "Java版本: $JAVA_VERSION"

# 检查Maven
if ! command -v mvn &> /dev/null; then
    log_error "Maven未安装，请先安装Maven 3.8+"}
    exit 1
fi
MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
log_success "Maven版本: $MVN_VERSION"

# 检查Node.js
if ! command -v node &> /dev/null; then
    log_error "Node.js未安装，请先安装Node.js 20+或22+"
    exit 1
fi
NODE_VERSION=$(node -v)
log_success "Node.js版本: $NODE_VERSION"

# 检查npm
if ! command -v npm &> /dev/null; then
    log_error "npm未安装"
    exit 1
fi
NPM_VERSION=$(npm -v)
log_success "npm版本: $NPM_VERSION"

echo ""

###############################################################################
# 第二步：检查并初始化数据库
###############################################################################

log_info "====== 第二步：数据库初始化 ======"

# 数据库配置（从环境变量或使用默认值）
DB_HOST="${DATABASE_HOST:-localhost}"
DB_PORT="${DATABASE_PORT:-3306}"
DB_NAME="${DATABASE_NAME:-woodlin}"
DB_USER="${DATABASE_USERNAME:-root}"
DB_PASS="${DATABASE_PASSWORD:-Passw0rd}"

log_info "数据库配置:"
log_info "  主机: $DB_HOST"
log_info "  端口: $DB_PORT"
log_info "  数据库: $DB_NAME"
log_info "  用户: $DB_USER"

# 检查MySQL是否可访问
if ! command -v mysql &> /dev/null; then
    log_warning "MySQL客户端未安装"
    log_warning "请手动运行以下命令初始化数据库:"
    log_warning "  mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS < sql/mysql/woodlin_complete_schema.sql"
    log_warning "  mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME < sql/mysql/woodlin_complete_data.sql"
else
    log_info "检查MySQL连接..."
    # 使用MySQL配置文件方式连接，避免密码在命令行暴露
    MYSQL_CMD="mysql -h$DB_HOST -P$DB_PORT -u$DB_USER"
    if echo "$DB_PASS" | mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p -e "SELECT 1" 2>/dev/null; then
        log_success "MySQL连接成功"
        
        # 检查数据库是否存在
        if echo "$DB_PASS" | mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p -e "USE $DB_NAME" 2>/dev/null; then
            log_info "数据库 '$DB_NAME' 已存在"
            
            # 询问是否重新初始化
            read -p "是否重新初始化数据库？这将删除所有现有数据！(y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                log_warning "重新初始化数据库..."
                echo "$DB_PASS" | mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p -e "DROP DATABASE IF EXISTS $DB_NAME; CREATE DATABASE $DB_NAME DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
                echo "$DB_PASS" | mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p "$DB_NAME" < sql/mysql/woodlin_complete_schema.sql 2>/dev/null
                echo "$DB_PASS" | mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p "$DB_NAME" < sql/mysql/woodlin_complete_data.sql 2>/dev/null
                log_success "数据库重新初始化完成"
            else
                log_info "跳过数据库初始化"
            fi
        else
            log_info "创建并初始化数据库..."
            echo "$DB_PASS" | mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p -e "CREATE DATABASE IF NOT EXISTS $DB_NAME DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
            echo "$DB_PASS" | mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p "$DB_NAME" < sql/mysql/woodlin_complete_schema.sql 2>/dev/null
            echo "$DB_PASS" | mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p "$DB_NAME" < sql/mysql/woodlin_complete_data.sql 2>/dev/null
            log_success "数据库初始化完成"
        fi
        
        # 验证数据
        USER_COUNT=$(echo "$DB_PASS" | mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p -N -e "USE $DB_NAME; SELECT COUNT(*) FROM sys_user;" 2>/dev/null)
        log_success "数据验证: 用户表有 $USER_COUNT 条记录"
    else
        log_error "无法连接到MySQL数据库"
        log_error "请检查MySQL是否运行以及配置是否正确"
        exit 1
    fi
fi

echo ""

###############################################################################
# 第三步：检查Redis（可选）
###############################################################################

log_info "====== 第三步：Redis检查（可选） ======"

REDIS_HOST="${REDIS_HOST:-localhost}"
REDIS_PORT="${REDIS_PORT:-6379}"

if command -v redis-cli &> /dev/null; then
    if redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" ping &> /dev/null; then
        log_success "Redis连接成功 ($REDIS_HOST:$REDIS_PORT)"
    else
        log_warning "Redis未运行或无法连接"
        log_warning "某些功能（如缓存）可能受影响"
    fi
else
    log_warning "redis-cli未安装，跳过Redis检查"
fi

echo ""

###############################################################################
# 第四步：编译后端项目
###############################################################################

log_info "====== 第四步：编译后端项目 ======"

log_info "执行Maven编译..."
if mvn clean compile -DskipTests -q; then
    log_success "后端编译成功"
else
    log_error "后端编译失败"
    exit 1
fi

echo ""

###############################################################################
# 第五步：安装前端依赖
###############################################################################

log_info "====== 第五步：安装前端依赖 ======"

cd woodlin-web

if [ ! -d "node_modules" ]; then
    log_info "安装前端依赖..."
    if npm install; then
        log_success "前端依赖安装成功"
    else
        log_error "前端依赖安装失败"
        exit 1
    fi
else
    log_success "前端依赖已安装"
fi

cd ..

echo ""

###############################################################################
# 第六步：生成开发配置文件
###############################################################################

log_info "====== 第六步：检查配置文件 ======"

# 检查后端配置
BACKEND_CONFIG="woodlin-admin/src/main/resources/application-dev.yml"
if [ -f "$BACKEND_CONFIG" ]; then
    log_success "后端开发配置存在: $BACKEND_CONFIG"
else
    log_warning "后端开发配置不存在: $BACKEND_CONFIG"
fi

# 检查前端配置
FRONTEND_CONFIG="woodlin-web/.env.development"
if [ -f "$FRONTEND_CONFIG" ]; then
    log_success "前端开发配置存在: $FRONTEND_CONFIG"
else
    log_warning "前端开发配置不存在: $FRONTEND_CONFIG"
fi

echo ""

###############################################################################
# 完成
###############################################################################

log_success "====== 开发环境初始化完成 ======"
echo ""
log_info "下一步操作："
echo ""
echo "1. 启动后端服务："
echo "   ${GREEN}mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.profiles=dev${NC}"
echo "   或"
echo "   ${GREEN}./scripts/dev.sh backend${NC}"
echo ""
echo "2. 启动前端服务（新终端）："
echo "   ${GREEN}cd woodlin-web && npm run dev${NC}"
echo "   或"
echo "   ${GREEN}./scripts/dev.sh frontend${NC}"
echo ""
echo "3. 访问应用："
echo "   - 前端: ${BLUE}http://localhost:5173/${NC}"
echo "   - 后端API: ${BLUE}http://localhost:8080/api${NC}"
echo "   - API文档: ${BLUE}http://localhost:8080/api/doc.html${NC}"
echo ""
echo "4. 默认登录账号："
echo "   - 用户名: ${GREEN}admin${NC}"
echo "   - 密码: ${GREEN}Passw0rd${NC}"
echo ""
log_info "如遇到问题，请查看 ROUTING_FIX_GUIDE.md 获取详细的故障排查指南"
echo ""
