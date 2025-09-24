#!/bin/bash

# Woodlin 开发环境启动脚本
# 作者: mumu
# 描述: 快速启动开发环境

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 获取项目根目录
PROJECT_ROOT=$(dirname $(readlink -f $0))/..
cd $PROJECT_ROOT

print_message "🚀 启动 Woodlin 开发环境..."

# 检查参数
case "$1" in
    "backend"|"be")
        print_message "启动后端开发服务..."
        mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.profiles=dev
        ;;
    "frontend"|"fe")
        print_message "启动前端开发服务..."
        if [ ! -d "woodlin-web/node_modules" ]; then
            print_message "安装前端依赖..."
            cd woodlin-web && npm install && cd ..
        fi
        cd woodlin-web && npm run dev
        ;;
    "full"|"")
        print_message "启动完整开发环境..."
        
        # 检查是否安装了 tmux 或 screen
        if command -v tmux >/dev/null 2>&1; then
            SESSION_NAME="woodlin-dev"
            
            # 创建 tmux 会话
            tmux new-session -d -s $SESSION_NAME
            
            # 启动后端
            tmux send-keys -t $SESSION_NAME "cd $PROJECT_ROOT && mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.profiles=dev" Enter
            
            # 创建新窗口启动前端
            tmux new-window -t $SESSION_NAME
            if [ ! -d "woodlin-web/node_modules" ]; then
                tmux send-keys -t $SESSION_NAME "cd $PROJECT_ROOT/woodlin-web && npm install" Enter
                sleep 10
            fi
            tmux send-keys -t $SESSION_NAME "cd $PROJECT_ROOT/woodlin-web && npm run dev" Enter
            
            # 附加到会话
            print_message "启动完成! 使用以下命令管理开发环境:"
            echo "  查看: tmux attach-session -t $SESSION_NAME"
            echo "  退出: tmux kill-session -t $SESSION_NAME"
            echo "  切换窗口: Ctrl+B 然后按数字键 0/1"
            
            tmux attach-session -t $SESSION_NAME
            
        elif command -v screen >/dev/null 2>&1; then
            # 使用 screen
            screen -dmS woodlin-backend bash -c "cd $PROJECT_ROOT && mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.profiles=dev"
            
            if [ ! -d "woodlin-web/node_modules" ]; then
                print_message "安装前端依赖..."
                cd woodlin-web && npm install && cd ..
            fi
            screen -dmS woodlin-frontend bash -c "cd $PROJECT_ROOT/woodlin-web && npm run dev"
            
            print_message "启动完成! 使用以下命令管理开发环境:"
            echo "  后端: screen -r woodlin-backend"
            echo "  前端: screen -r woodlin-frontend"
            echo "  查看会话: screen -ls"
            
        else
            print_warning "未安装 tmux 或 screen，请手动启动:"
            echo "终端1: mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.profiles=dev"
            echo "终端2: cd woodlin-web && npm run dev"
        fi
        ;;
    "build")
        print_message "构建项目..."
        mvn clean package -DskipTests
        if [ -d "woodlin-web" ]; then
            cd woodlin-web
            if [ ! -d "node_modules" ]; then
                npm install
            fi
            npm run build
            cd ..
        fi
        print_message "构建完成!"
        ;;
    "clean")
        print_message "清理项目..."
        mvn clean
        if [ -d "woodlin-web/node_modules" ]; then
            rm -rf woodlin-web/node_modules
        fi
        if [ -d "woodlin-web/dist" ]; then
            rm -rf woodlin-web/dist
        fi
        print_message "清理完成!"
        ;;
    *)
        echo "Woodlin 开发工具"
        echo ""
        echo "用法:"
        echo "  $0 [选项]"
        echo ""
        echo "选项:"
        echo "  backend, be     仅启动后端服务"
        echo "  frontend, fe    仅启动前端服务"  
        echo "  full, (空)      启动完整开发环境"
        echo "  build           构建项目"
        echo "  clean           清理项目"
        echo ""
        echo "示例:"
        echo "  $0 backend      # 启动后端"
        echo "  $0 frontend     # 启动前端"
        echo "  $0              # 启动完整环境"
        ;;
esac