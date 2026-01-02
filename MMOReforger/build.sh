#!/bin/bash

echo "=== MMOReforger 插件构建脚本 ==="

# 检查 Maven 是否安装
if ! command -v mvn &> /dev/null; then
    echo "错误: Maven 未安装！"
    echo "请先安装 Maven: https://maven.apache.org/install.html"
    echo ""
    echo "或者使用以下命令安装:"
    echo "  Windows: choco install maven"
    echo "  macOS: brew install maven"
    echo "  Ubuntu: sudo apt install maven"
    exit 1
fi

echo "Maven 版本信息:"
mvn --version
echo ""

echo "开始构建插件..."
mvn clean package

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 构建成功！"
    echo "插件 JAR 文件位置: target/MMOReforger.jar"
    echo ""
    echo "部署说明:"
    echo "1. 将 target/MMOReforger.jar 复制到您的 Minecraft 服务器 plugins 目录"
    echo "2. 确保服务器已安装 MMOItems 插件"
    echo "3. 重启服务器或使用 /reload 命令"
else
    echo ""
    echo "❌ 构建失败，请检查错误信息"
    exit 1
fi