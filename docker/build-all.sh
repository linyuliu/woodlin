#!/bin/bash

# ====================================================================================
# Woodlin Docker 镜像构建脚本
# 
# 功能:
# - 构建所有 Docker 镜像
# - 支持多架构构建
# - 支持单架构构建
# - 支持构建指定服务
# 
# 使用方法:
# ./build-all.sh                    # 构建所有镜像 (单架构)
# ./build-all.sh --multi-arch       # 构建所有镜像 (多架构)
# ./build-all.sh app mysql redis    # 只构建指定服务
# ./build-all.sh --multi-arch app   # 多架构构建应用服务
# 
# 参数:
# --multi-arch    使用 Docker Buildx 进行多架构构建
# --push          推送镜像到 registry (需要先登录)
# --no-cache      不使用缓存构建
# --registry      指定镜像 registry (默认: 无)
# --tag           指定镜像标签 (默认: latest)
# 
# 示例:
# ./build-all.sh --multi-arch --push --registry myregistry.com/woodlin
# ./build-all.sh --tag v1.0.0 app mysql
# ====================================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 默认配置
MULTI_ARCH=false
PUSH=false
NO_CACHE=false
REGISTRY=""
TAG="latest"
PLATFORMS="linux/amd64,linux/arm64,linux/arm/v7"

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 服务列表
ALL_SERVICES=("app" "mysql" "redis" "minio" "postgresql")
SELECTED_SERVICES=()

# ====================================================================================
# 函数: 打印帮助信息
# ====================================================================================
print_help() {
    cat << EOF
Woodlin Docker 镜像构建脚本

使用方法:
  $0 [选项] [服务...]

选项:
  --multi-arch        使用 Docker Buildx 进行多架构构建
  --push             推送镜像到 registry
  --no-cache         不使用缓存构建
  --registry <url>   指定镜像 registry
  --tag <tag>        指定镜像标签 (默认: latest)
  --platforms <list> 指定构建平台 (默认: linux/amd64,linux/arm64,linux/arm/v7)
  -h, --help         显示帮助信息

服务列表:
  app         Woodlin 应用服务
  mysql       MySQL 8.0 数据库
  redis       Redis 7.x 缓存
  minio       MinIO 对象存储
  postgresql  PostgreSQL 16 数据库

示例:
  # 构建所有服务 (单架构)
  $0

  # 多架构构建所有服务
  $0 --multi-arch

  # 只构建应用和 MySQL
  $0 app mysql

  # 多架构构建并推送到 registry
  $0 --multi-arch --push --registry myregistry.com/woodlin

  # 使用自定义标签构建
  $0 --tag v1.0.0 app

EOF
}

# ====================================================================================
# 函数: 打印信息
# ====================================================================================
info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# ====================================================================================
# 函数: 检查 Docker 是否安装
# ====================================================================================
check_docker() {
    if ! command -v docker &> /dev/null; then
        error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        error "Docker 服务未运行，请启动 Docker"
        exit 1
    fi
    
    info "Docker 版本: $(docker version --format '{{.Server.Version}}')"
}

# ====================================================================================
# 函数: 检查 Docker Buildx 是否可用
# ====================================================================================
check_buildx() {
    if [ "$MULTI_ARCH" = true ]; then
        if ! docker buildx version &> /dev/null; then
            error "Docker Buildx 不可用，请升级 Docker 或安装 Buildx"
            exit 1
        fi
        
        info "Docker Buildx 版本: $(docker buildx version | head -1)"
        
        # 检查是否存在 multiarch builder
        if ! docker buildx ls | grep -q multiarch; then
            info "创建 multiarch builder..."
            if ! docker buildx create --name multiarch --use --driver docker-container --driver-opt network=host; then
                error "创建 multiarch builder 失败"
                exit 1
            fi
            docker buildx inspect --bootstrap multiarch
        else
            info "使用现有的 multiarch builder"
            docker buildx use multiarch
        fi
    fi
}

# ====================================================================================
# 函数: 构建镜像
# ====================================================================================
build_image() {
    local service=$1
    local dockerfile=$2
    local image_name=$3
    local context=${4:-$PROJECT_ROOT}
    
    info "开始构建 $service 服务..."
    
    # 构建镜像名称
    local full_image_name="$image_name:$TAG"
    if [ -n "$REGISTRY" ]; then
        full_image_name="$REGISTRY/$full_image_name"
    fi
    
    # 构建参数
    local build_args=""
    if [ "$NO_CACHE" = true ]; then
        build_args="$build_args --no-cache"
    fi
    
    # 执行构建
    if [ "$MULTI_ARCH" = true ]; then
        # 多架构构建
        info "多架构构建: $PLATFORMS"
        
        local push_arg=""
        if [ "$PUSH" = true ]; then
            push_arg="--push"
        else
            # --load 不支持多架构构建，必须使用 --push
            # 如果不推送，则只构建单个平台的镜像
            error "多架构构建必须使用 --push 参数推送到 registry"
            error "或者不使用 --multi-arch 标志进行单架构构建"
            exit 1
        fi
        
        docker buildx build \
            --platform "$PLATFORMS" \
            -t "$full_image_name" \
            -f "$dockerfile" \
            $build_args \
            $push_arg \
            "$context"
    else
        # 单架构构建
        docker build \
            -t "$full_image_name" \
            -f "$dockerfile" \
            $build_args \
            "$context"
        
        if [ "$PUSH" = true ]; then
            info "推送镜像: $full_image_name"
            docker push "$full_image_name"
        fi
    fi
    
    success "$service 服务构建完成: $full_image_name"
}

# ====================================================================================
# 函数: 构建应用服务
# ====================================================================================
build_app() {
    info "========================================"
    info "构建 Woodlin 应用服务"
    info "========================================"
    
    # 构建标准镜像
    build_image "app" \
                "$SCRIPT_DIR/app/Dockerfile" \
                "woodlin-app" \
                "$PROJECT_ROOT"
    
    # 构建 distroless 镜像
    if [ "$MULTI_ARCH" = true ]; then
        # Distroless 仅支持 amd64 和 arm64
        local original_platforms="$PLATFORMS"
        PLATFORMS="linux/amd64,linux/arm64"
    fi
    
    build_image "app-distroless" \
                "$SCRIPT_DIR/app/Dockerfile.distroless" \
                "woodlin-app" \
                "$PROJECT_ROOT"
    
    if [ "$MULTI_ARCH" = true ]; then
        PLATFORMS="$original_platforms"
    fi
}

# ====================================================================================
# 函数: 构建 MySQL
# ====================================================================================
build_mysql() {
    info "========================================"
    info "构建 MySQL 8.4 LTS 数据库"
    info "========================================"
    
    # MySQL 仅支持 amd64 和 arm64
    if [ "$MULTI_ARCH" = true ]; then
        local original_platforms="$PLATFORMS"
        PLATFORMS="linux/amd64,linux/arm64"
    fi
    
    # 构建标准镜像
    build_image "mysql" \
                "$SCRIPT_DIR/mysql/Dockerfile" \
                "woodlin-mysql" \
                "$PROJECT_ROOT"
    
    # 构建 distroless 镜像
    build_image "mysql-distroless" \
                "$SCRIPT_DIR/mysql/Dockerfile.distroless" \
                "woodlin-mysql" \
                "$PROJECT_ROOT"
    
    if [ "$MULTI_ARCH" = true ]; then
        PLATFORMS="$original_platforms"
    fi
}

# ====================================================================================
# 函数: 构建 Redis
# ====================================================================================
build_redis() {
    info "========================================"
    info "构建 Redis 7.x 缓存"
    info "========================================"
    
    build_image "redis" \
                "$SCRIPT_DIR/redis/Dockerfile" \
                "woodlin-redis" \
                "$PROJECT_ROOT"
}

# ====================================================================================
# 函数: 构建 MinIO
# ====================================================================================
build_minio() {
    info "========================================"
    info "构建 MinIO 对象存储"
    info "========================================"
    
    build_image "minio" \
                "$SCRIPT_DIR/minio/Dockerfile" \
                "woodlin-minio" \
                "$PROJECT_ROOT"
    
    # 构建 distroless 镜像
    if [ "$MULTI_ARCH" = true ]; then
        # Distroless 仅支持 amd64 和 arm64
        local original_platforms="$PLATFORMS"
        PLATFORMS="linux/amd64,linux/arm64"
    fi
    
    build_image "minio-distroless" \
                "$SCRIPT_DIR/minio/Dockerfile.distroless" \
                "woodlin-minio" \
                "$PROJECT_ROOT"
    
    if [ "$MULTI_ARCH" = true ]; then
        PLATFORMS="$original_platforms"
    fi
}

# ====================================================================================
# 函数: 构建 PostgreSQL
# ====================================================================================
build_postgresql() {
    info "========================================"
    info "构建 PostgreSQL 16 数据库"
    info "========================================"
    
    # PostgreSQL 仅支持 amd64 和 arm64
    if [ "$MULTI_ARCH" = true ]; then
        local original_platforms="$PLATFORMS"
        PLATFORMS="linux/amd64,linux/arm64"
    fi
    
    build_image "postgresql" \
                "$SCRIPT_DIR/postgresql/Dockerfile" \
                "woodlin-postgresql" \
                "$PROJECT_ROOT"
    
    if [ "$MULTI_ARCH" = true ]; then
        PLATFORMS="$original_platforms"
    fi
}

# ====================================================================================
# 主程序
# ====================================================================================
main() {
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            --multi-arch)
                MULTI_ARCH=true
                shift
                ;;
            --push)
                PUSH=true
                shift
                ;;
            --no-cache)
                NO_CACHE=true
                shift
                ;;
            --registry)
                REGISTRY="$2"
                shift 2
                ;;
            --tag)
                TAG="$2"
                shift 2
                ;;
            --platforms)
                PLATFORMS="$2"
                shift 2
                ;;
            -h|--help)
                print_help
                exit 0
                ;;
            -*)
                error "未知选项: $1"
                print_help
                exit 1
                ;;
            *)
                # 服务名称
                SELECTED_SERVICES+=("$1")
                shift
                ;;
        esac
    done
    
    # 如果没有指定服务，则构建所有服务
    if [ ${#SELECTED_SERVICES[@]} -eq 0 ]; then
        SELECTED_SERVICES=("${ALL_SERVICES[@]}")
    fi
    
    # 验证服务名称
    for service in "${SELECTED_SERVICES[@]}"; do
        if [[ ! " ${ALL_SERVICES[@]} " =~ " ${service} " ]]; then
            error "未知服务: $service"
            echo "可用服务: ${ALL_SERVICES[*]}"
            exit 1
        fi
    done
    
    # 打印构建配置
    info "========================================"
    info "Woodlin Docker 镜像构建"
    info "========================================"
    info "多架构构建: $MULTI_ARCH"
    if [ "$MULTI_ARCH" = true ]; then
        info "构建平台: $PLATFORMS"
    fi
    info "推送镜像: $PUSH"
    info "使用缓存: $([ "$NO_CACHE" = true ] && echo "否" || echo "是")"
    info "镜像标签: $TAG"
    if [ -n "$REGISTRY" ]; then
        info "镜像仓库: $REGISTRY"
    fi
    info "构建服务: ${SELECTED_SERVICES[*]}"
    info "========================================"
    echo
    
    # 检查环境
    check_docker
    check_buildx
    
    # 记录开始时间
    local start_time=$(date +%s)
    
    # 构建服务
    for service in "${SELECTED_SERVICES[@]}"; do
        case $service in
            app)
                build_app
                ;;
            mysql)
                build_mysql
                ;;
            redis)
                build_redis
                ;;
            minio)
                build_minio
                ;;
            postgresql)
                build_postgresql
                ;;
        esac
        echo
    done
    
    # 计算耗时
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    info "========================================"
    success "所有镜像构建完成!"
    info "总耗时: ${duration}s"
    info "========================================"
    
    # 列出构建的镜像
    if [ "$PUSH" != true ]; then
        echo
        info "已构建的镜像:"
        docker images | grep -E "woodlin-(app|mysql|redis|minio|postgresql)" || true
    fi
}

# 执行主程序
main "$@"
