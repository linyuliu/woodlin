#!/bin/bash

# ====================================================================================
# Docker 环境检查脚本
# 
# 用途: 检查 Docker 环境配置是否满足 Woodlin 项目运行要求
# 作者: Woodlin Team
# ====================================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印函数
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# 检查计数器
WARNINGS=0
ERRORS=0

# ====================================================================================
# 检查 Docker 是否安装
# ====================================================================================
print_header "检查 Docker 环境"

if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | awk '{print $3}' | tr -d ',')
    print_success "Docker 已安装: $DOCKER_VERSION"
    
    # 检查 Docker 版本
    MAJOR_VERSION=$(echo $DOCKER_VERSION | cut -d'.' -f1)
    if [ "$MAJOR_VERSION" -ge 20 ]; then
        print_success "Docker 版本满足要求 (>= 20.10)"
    else
        print_warning "Docker 版本过低，建议升级到 20.10 或更高版本"
        ((WARNINGS++))
    fi
else
    print_error "Docker 未安装，请先安装 Docker"
    print_info "安装命令: curl -fsSL https://get.docker.com | sh"
    ((ERRORS++))
fi

# 检查 Docker 服务是否运行
if systemctl is-active --quiet docker || service docker status &> /dev/null; then
    print_success "Docker 服务正在运行"
else
    print_error "Docker 服务未运行"
    print_info "启动命令: sudo systemctl start docker"
    ((ERRORS++))
fi

# 检查当前用户是否在 docker 组
if groups | grep -q docker; then
    print_success "当前用户在 docker 组中"
else
    print_warning "当前用户不在 docker 组中，可能需要 sudo 权限"
    print_info "添加用户到 docker 组: sudo usermod -aG docker $USER"
    ((WARNINGS++))
fi

# ====================================================================================
# 检查 Docker Compose 是否安装
# ====================================================================================
print_header "检查 Docker Compose"

if command -v docker-compose &> /dev/null; then
    COMPOSE_VERSION=$(docker-compose --version | awk '{print $4}' | tr -d ',')
    print_success "Docker Compose 已安装: $COMPOSE_VERSION"
elif docker compose version &> /dev/null; then
    COMPOSE_VERSION=$(docker compose version --short)
    print_success "Docker Compose 已安装 (插件版本): $COMPOSE_VERSION"
else
    print_error "Docker Compose 未安装"
    print_info "安装命令: sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-\$(uname -s)-\$(uname -m) -o /usr/local/bin/docker-compose"
    print_info "          sudo chmod +x /usr/local/bin/docker-compose"
    ((ERRORS++))
fi

# ====================================================================================
# 检查系统资源
# ====================================================================================
print_header "检查系统资源"

# 检查 CPU
CPU_CORES=$(nproc)
print_info "CPU 核心数: $CPU_CORES"
if [ "$CPU_CORES" -ge 4 ]; then
    print_success "CPU 核心数充足"
elif [ "$CPU_CORES" -ge 2 ]; then
    print_warning "CPU 核心数偏少，建议至少 4 核"
    ((WARNINGS++))
else
    print_error "CPU 核心数不足，建议至少 2 核"
    ((ERRORS++))
fi

# 检查内存
TOTAL_MEM=$(free -g | awk '/^Mem:/{print $2}')
print_info "总内存: ${TOTAL_MEM}GB"
if [ "$TOTAL_MEM" -ge 8 ]; then
    print_success "内存充足"
elif [ "$TOTAL_MEM" -ge 4 ]; then
    print_warning "内存偏少，建议至少 8GB"
    ((WARNINGS++))
else
    print_error "内存不足，建议至少 4GB"
    ((ERRORS++))
fi

# 检查磁盘空间
DISK_SPACE=$(df -BG / | awk 'NR==2 {print $4}' | tr -d 'G')
print_info "可用磁盘空间: ${DISK_SPACE}GB"
if [ "$DISK_SPACE" -ge 50 ]; then
    print_success "磁盘空间充足"
elif [ "$DISK_SPACE" -ge 20 ]; then
    print_warning "磁盘空间偏少，建议至少 50GB"
    ((WARNINGS++))
else
    print_error "磁盘空间不足，建议至少 20GB"
    ((ERRORS++))
fi

# ====================================================================================
# 检查系统限制
# ====================================================================================
print_header "检查系统限制 (ulimit)"

# 检查文件描述符限制
NOFILE_SOFT=$(ulimit -Sn)
NOFILE_HARD=$(ulimit -Hn)
print_info "文件描述符限制: soft=$NOFILE_SOFT, hard=$NOFILE_HARD"
if [ "$NOFILE_SOFT" -ge 65536 ] && [ "$NOFILE_HARD" -ge 65536 ]; then
    print_success "文件描述符限制已正确配置"
elif [ "$NOFILE_SOFT" -ge 10000 ] && [ "$NOFILE_HARD" -ge 10000 ]; then
    print_warning "文件描述符限制偏低，建议设置为 65536"
    print_info "配置方法: 编辑 /etc/security/limits.conf"
    ((WARNINGS++))
else
    print_error "文件描述符限制过低，建议设置为 65536"
    print_info "配置方法: 编辑 /etc/security/limits.conf"
    ((ERRORS++))
fi

# 检查进程数限制
NPROC_SOFT=$(ulimit -Su)
NPROC_HARD=$(ulimit -Hu)
print_info "进程数限制: soft=$NPROC_SOFT, hard=$NPROC_HARD"
if [ "$NPROC_SOFT" -ge 65536 ] && [ "$NPROC_HARD" -ge 65536 ]; then
    print_success "进程数限制已正确配置"
elif [ "$NPROC_SOFT" -ge 10000 ] && [ "$NPROC_HARD" -ge 10000 ]; then
    print_warning "进程数限制偏低，建议设置为 65536"
    print_info "配置方法: 编辑 /etc/security/limits.conf"
    ((WARNINGS++))
else
    print_error "进程数限制过低，建议设置为 65536"
    print_info "配置方法: 编辑 /etc/security/limits.conf"
    ((ERRORS++))
fi

# ====================================================================================
# 检查内核参数
# ====================================================================================
print_header "检查内核参数"

# 检查 fs.file-max
if command -v sysctl &> /dev/null; then
    FILE_MAX=$(sysctl -n fs.file-max 2>/dev/null || echo "0")
    print_info "fs.file-max: $FILE_MAX"
    if [ "$FILE_MAX" -ge 2097152 ]; then
        print_success "fs.file-max 已正确配置"
    elif [ "$FILE_MAX" -ge 100000 ]; then
        print_warning "fs.file-max 偏低，建议设置为 2097152"
        print_info "配置方法: echo 'fs.file-max = 2097152' >> /etc/sysctl.conf && sysctl -p"
        ((WARNINGS++))
    else
        print_error "fs.file-max 过低，建议设置为 2097152"
        print_info "配置方法: echo 'fs.file-max = 2097152' >> /etc/sysctl.conf && sysctl -p"
        ((ERRORS++))
    fi
    
    # 检查 net.core.somaxconn
    SOMAXCONN=$(sysctl -n net.core.somaxconn 2>/dev/null || echo "0")
    print_info "net.core.somaxconn: $SOMAXCONN"
    if [ "$SOMAXCONN" -ge 32768 ]; then
        print_success "net.core.somaxconn 已正确配置"
    elif [ "$SOMAXCONN" -ge 1024 ]; then
        print_warning "net.core.somaxconn 偏低，建议设置为 32768"
        print_info "配置方法: echo 'net.core.somaxconn = 32768' >> /etc/sysctl.conf && sysctl -p"
        ((WARNINGS++))
    else
        print_error "net.core.somaxconn 过低，建议设置为 32768"
        print_info "配置方法: echo 'net.core.somaxconn = 32768' >> /etc/sysctl.conf && sysctl -p"
        ((ERRORS++))
    fi
    
    # 检查 vm.max_map_count
    MAX_MAP_COUNT=$(sysctl -n vm.max_map_count 2>/dev/null || echo "0")
    print_info "vm.max_map_count: $MAX_MAP_COUNT"
    if [ "$MAX_MAP_COUNT" -ge 262144 ]; then
        print_success "vm.max_map_count 已正确配置"
    else
        print_warning "vm.max_map_count 偏低，建议设置为 262144"
        print_info "配置方法: echo 'vm.max_map_count = 262144' >> /etc/sysctl.conf && sysctl -p"
        ((WARNINGS++))
    fi
else
    print_warning "sysctl 命令不可用，跳过内核参数检查"
    ((WARNINGS++))
fi

# ====================================================================================
# 检查端口占用
# ====================================================================================
print_header "检查端口占用"

check_port() {
    local port=$1
    local service=$2
    
    if command -v lsof &> /dev/null && lsof -Pi :$port -sTCP:LISTEN -t &> /dev/null; then
        print_warning "端口 $port ($service) 已被占用"
        lsof -Pi :$port -sTCP:LISTEN
        ((WARNINGS++))
    elif command -v netstat &> /dev/null && netstat -tunl | grep -q ":$port "; then
        print_warning "端口 $port ($service) 已被占用"
        netstat -tunlp | grep ":$port "
        ((WARNINGS++))
    else
        print_success "端口 $port ($service) 可用"
    fi
}

check_port 3306 "MySQL"
check_port 6379 "Redis"
check_port 8080 "Woodlin App"
check_port 3000 "Nginx (可选)"

# ====================================================================================
# 检查配置文件
# ====================================================================================
print_header "检查配置文件"

# 检查 docker-compose.yml
if [ -f "docker-compose.yml" ]; then
    print_success "docker-compose.yml 存在"
    
    # 验证 docker-compose 配置
    if docker compose config &> /dev/null || docker-compose config &> /dev/null; then
        print_success "docker-compose.yml 配置语法正确"
    else
        print_error "docker-compose.yml 配置语法错误"
        ((ERRORS++))
    fi
else
    print_error "docker-compose.yml 不存在"
    ((ERRORS++))
fi

# 检查 .env 文件
if [ -f ".env" ]; then
    print_success ".env 文件存在"
    
    # 检查关键配置
    if grep -q "DATABASE_PASSWORD=123456" .env; then
        print_warning ".env 使用默认密码，生产环境请修改"
        ((WARNINGS++))
    fi
else
    print_warning ".env 文件不存在，将使用默认配置"
    print_info "建议: cp .env.example .env"
    ((WARNINGS++))
fi

# 检查 Dockerfile
if [ -f "scripts/Dockerfile" ]; then
    print_success "Dockerfile 存在"
else
    print_error "Dockerfile 不存在"
    ((ERRORS++))
fi

# ====================================================================================
# 总结
# ====================================================================================
print_header "检查总结"

echo -e "错误: ${RED}$ERRORS${NC}"
echo -e "警告: ${YELLOW}$WARNINGS${NC}"
echo ""

if [ "$ERRORS" -gt 0 ]; then
    print_error "存在 $ERRORS 个错误，请先解决后再部署"
    exit 1
elif [ "$WARNINGS" -gt 0 ]; then
    print_warning "存在 $WARNINGS 个警告，建议优化后再部署"
    echo ""
    read -p "是否继续部署？(y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "已取消部署"
        exit 0
    fi
else
    print_success "所有检查通过，可以开始部署！"
fi

echo ""
print_info "开始部署: docker compose up -d"
print_info "查看日志: docker compose logs -f"
print_info "查看状态: docker compose ps"
echo ""
