#!/bin/bash
# Woodlin 项目代码质量检查脚本
# Code quality check script for Woodlin project

set -e

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_message() {
    echo -e "${GREEN}==>${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}WARNING:${NC} $1"
}

print_error() {
    echo -e "${RED}ERROR:${NC} $1"
}

# 检查是否在项目根目录
if [ ! -f "pom.xml" ]; then
    print_error "请在项目根目录运行此脚本"
    exit 1
fi

print_message "开始代码质量检查..."
echo ""

# 1. 编译项目
print_message "步骤 1/6: 编译项目..."
if mvn clean compile -DskipTests --batch-mode -q; then
    echo "✓ 编译成功"
else
    print_error "编译失败，请修复编译错误后重试"
    exit 1
fi
echo ""

# 2. 运行 Checkstyle
print_message "步骤 2/6: 运行 Checkstyle 检查..."
if mvn checkstyle:checkstyle --batch-mode -q; then
    echo "✓ Checkstyle 检查完成"
    print_warning "查看详细报告: target/site/checkstyle.html"
else
    print_warning "Checkstyle 检查发现一些问题，继续执行..."
fi
echo ""

# 3. 运行 SpotBugs
print_message "步骤 3/6: 运行 SpotBugs 静态分析..."
if mvn compile spotbugs:check --batch-mode -q; then
    echo "✓ SpotBugs 检查通过"
else
    print_warning "SpotBugs 发现一些问题，继续执行..."
fi
echo ""

# 4. 验证 JavaDoc
print_message "步骤 4/6: 验证 JavaDoc..."
if mvn javadoc:javadoc --batch-mode -q; then
    echo "✓ JavaDoc 验证通过"
    print_warning "查看 JavaDoc: target/apidocs/index.html"
else
    print_warning "JavaDoc 验证发现一些问题，继续执行..."
fi
echo ""

# 5. 运行测试和代码覆盖率
print_message "步骤 5/6: 运行测试和代码覆盖率分析..."
if mvn test jacoco:report --batch-mode -q; then
    echo "✓ 测试执行完成"
    print_warning "查看覆盖率报告: target/site/jacoco/index.html"
else
    print_warning "测试执行有错误，继续执行..."
fi
echo ""

# 6. 前端代码检查
print_message "步骤 6/6: 检查前端代码..."
if [ -d "woodlin-web" ]; then
    cd woodlin-web
    
    # 检查 node_modules 是否存在
    if [ ! -d "node_modules" ]; then
        print_message "安装前端依赖..."
        npm ci
    fi
    
    print_message "运行 ESLint..."
    if npm run lint; then
        echo "✓ ESLint 检查通过"
    else
        print_warning "ESLint 发现一些问题"
    fi
    
    print_message "运行类型检查..."
    if npm run type-check; then
        echo "✓ TypeScript 类型检查通过"
    else
        print_warning "TypeScript 类型检查发现问题"
    fi
    
    cd ..
else
    print_warning "未找到 woodlin-web 目录，跳过前端检查"
fi
echo ""

# 总结
print_message "代码质量检查完成！"
echo ""
echo "详细报告位置:"
echo "  - Checkstyle: 各模块的 target/site/checkstyle.html"
echo "  - SpotBugs:   各模块的 target/spotbugsXml.xml"
echo "  - JavaDoc:    各模块的 target/apidocs/index.html"
echo "  - 覆盖率:     各模块的 target/site/jacoco/index.html"
echo ""
print_message "请查看报告以了解需要改进的地方"
