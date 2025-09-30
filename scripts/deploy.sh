#!/bin/bash

# Woodlin 项目部署脚本
# 作者: mumu
# 描述: 一键部署 Woodlin 多租户管理系统

set -e

echo "🚀 开始部署 Woodlin 多租户管理系统..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印彩色消息
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        print_error "$1 命令未找到，请先安装"
        exit 1
    fi
}

# 检查必要的命令
print_message "检查系统依赖..."
check_command "java"
check_command "mvn"
check_command "docker"
check_command "docker-compose"

# 检查Java版本
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt "17" ]; then
    print_error "需要 Java 17 或更高版本，当前版本: $JAVA_VERSION"
    exit 1
fi
print_message "Java 版本检查通过: $(java -version 2>&1 | head -n 1)"

# 获取项目根目录
PROJECT_ROOT=$(dirname $(readlink -f $0))/..
cd $PROJECT_ROOT

print_message "项目根目录: $PROJECT_ROOT"

# 编译后端项目
print_message "编译后端项目..."
mvn clean package -DskipTests -q

if [ ! -f "woodlin-admin/target/woodlin-admin-1.0.0.jar" ]; then
    print_error "后端项目编译失败"
    exit 1
fi
print_message "后端项目编译成功"

# 编译前端项目
if [ -d "woodlin-web" ]; then
    print_message "编译前端项目..."
    cd woodlin-web
    
    if [ ! -d "node_modules" ]; then
        print_message "安装前端依赖..."
        npm install
    fi
    
    npm run build
    
    if [ ! -d "dist" ]; then
        print_error "前端项目编译失败"
        exit 1
    fi
    
    print_message "前端项目编译成功"
    cd $PROJECT_ROOT
fi

# 创建部署目录
DEPLOY_DIR="$PROJECT_ROOT/deploy"
mkdir -p $DEPLOY_DIR

# 复制文件
print_message "准备部署文件..."
cp woodlin-admin/target/woodlin-admin-1.0.0.jar $DEPLOY_DIR/
cp docker-compose.yml $DEPLOY_DIR/
cp scripts/Dockerfile $DEPLOY_DIR/

if [ -d "woodlin-web/dist" ]; then
    cp -r woodlin-web/dist $DEPLOY_DIR/web
fi

# 使用Docker Compose启动服务
print_message "启动服务..."
cd $DEPLOY_DIR

# 检查端口是否被占用
check_port() {
    if command -v lsof >/dev/null 2>&1 && lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
        print_warning "端口 $1 已被占用"
        read -p "是否停止现有服务？(y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            docker-compose down
        else
            print_error "端口冲突，部署中断"
            exit 1
        fi
    fi
}

check_port 8080
check_port 3306
check_port 6379

# 启动服务
docker-compose up -d

# 等待服务启动
print_message "等待服务启动..."
sleep 10

# 检查服务状态
print_message "检查服务状态..."
docker-compose ps

# 检查应用是否正常启动
print_message "检查应用健康状态..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/actuator/health > /dev/null; then
        print_message "应用启动成功!"
        break
    fi
    
    if [ $i -eq 30 ]; then
        print_error "应用启动超时"
        docker-compose logs woodlin-app
        exit 1
    fi
    
    sleep 2
    echo -n "."
done

echo
print_message "🎉 部署完成!"
echo
echo "=========================================="
echo "🌍 访问地址:"
echo "  后台管理: http://localhost:8080/api"
echo "  API文档:  http://localhost:8080/api/doc.html"
echo "  数据库监控: http://localhost:8080/api/druid"
if [ -d "$DEPLOY_DIR/web" ]; then
    echo "  前端页面: http://localhost:3000"
fi
echo
echo "🔑 默认账号:"
echo "  用户名: admin"
echo "  密码:   Passw0rd"
echo
echo "📊 管理命令:"
echo "  查看日志: docker-compose logs -f woodlin-app"
echo "  停止服务: docker-compose down"
echo "  重启服务: docker-compose restart"
echo "=========================================="