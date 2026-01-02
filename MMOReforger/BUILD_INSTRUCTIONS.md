# MMOReforger 插件构建指南

## 当前状况
- 项目代码已完成开发，包含所有必要的 Java 文件
- 缺少 Maven 构建环境
- 插件配置正确（pom.xml, plugin.yml, config.yml）

## 构建解决方案

### 方案 1: 使用外部 Maven 构建环境
1. 在有 Maven 的系统上运行以下命令：
   ```bash
   cd /path/to/MMOReforger
   mvn clean package
   ```

2. 生成的插件 JAR 文件将在 `target/MMOReforger.jar`

### 方案 2: 在线 Maven 构建服务
1. 上传项目到 GitHub
2. 使用 GitHub Actions 自动构建
3. 下载生成的 artifact

### 方案 3: 本地手动构建（推荐）
1. 下载并安装 Maven：
   - 访问 https://maven.apache.org/download.cgi
   - 下载 apache-maven-3.9.4-bin.zip
   - 解压到本地目录
   - 设置环境变量 MAVEN_HOME 和 PATH

2. 在项目目录中运行：
   ```bash
   mvn clean package
   ```

## 项目特性
- ✅ 支持 MMOItems 物品强化
- ✅ 图形化用户界面 (GUI)
- ✅ 可配置的成功率和材料需求
- ✅ 支持失败惩罚机制
- ✅ 命令 `/reforge` 打开强化界面

## 部署说明
1. 将生成的 `MMOReforger.jar` 放入服务器的 `plugins` 目录
2. 重启服务器或使用 `/reload` 重新加载插件
3. 确保服务器已安装 MMOItems 插件

## 配置文件
- `config.yml`: 强化配置（成功率、材料、消息等）
- `plugin.yml`: 插件信息和权限

## 使用方法
1. 玩家手持 MMOItems 物品
2. 输入命令 `/reforge`
3. 在 GUI 中查看材料需求和成功率
4. 点击强化按钮进行强化