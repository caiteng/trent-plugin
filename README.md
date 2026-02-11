# Trent Plugin

一款功能强大的IntelliJ IDEA插件，专注于提升开发者编码效率和体验。

## 📋 环境版本声明

### 开发环境
- **JDK 版本**: Java 17
- **Gradle 版本**: 9.3.1
- **IntelliJ IDEA 版本**: 2024.2.4 (Ultimate Edition)
- **操作系统**: Windows 11 25H2

### 构建配置
- **构建工具**: Gradle Kotlin DSL (build.gradle.kts)
- **IntelliJ插件版本**: org.jetbrains.intellij 1.17.3
- **目标IDE版本**: IntelliJ IDEA 2024.1.4 (IU)
- **Java编译版本**: Java 17
- **依赖库版本**:
  - JSoup: 1.16.2
  - Lombok: 1.18.32
- **兼容范围**: IntelliJ IDEA 2021.1 - 2024.x

## 🎯 核心功能

### 📖 智能阅读提示 (Read Tip)
- **网络内容抓取**：自动从指定URL抓取文章内容
- **分段展示**：智能分割长文本，每35个字符为一段
- **章节导航**：支持上一章/下一章切换
- **快捷键操作**：
  - `Ctrl + Alt + Q`：下一段内容
  - `Ctrl + Alt + W`：上一段内容

### 🎨 标签页高亮 (Click Light)
- **活动标签识别**：自动高亮当前活动的编辑器标签页
- **自定义颜色**：可配置的标签页背景色
- **视觉优化**：改善多标签页环境下的视觉体验
- **智能监听**：实时监听文件编辑器切换事件

### ⚙️ 统一配置中心
- **集成设置面板**：统一管理所有插件功能
- **模块化配置**：分别配置阅读提示、标签高亮等功能
- **即时生效**：配置修改后无需重启IDE

## 🛠️ 技术架构

### 核心组件

```
src/main/java/org/trent/helper/
├── readtip/                 # 阅读提示模块
│   ├── DataSource.java      # 数据源实体
│   ├── HandleUtils.java     # 内容处理工具
│   ├── ReadTipState.java    # 状态管理
│   ├── Render.java          # 渲染引擎
│   ├── TipActionPlus.java   # 增加提示动作
│   └── TipActionSub.java    # 减少提示动作
├── clicklight/              # 标签高亮模块
│   ├── ActiveTabHighlighterStartupActivity.java  # 启动活动
│   ├── CustomEditorTabColorProvider.java         # 颜色提供者
│   └── TabHighlighterFileEditorListener.java     # 文件监听器
├── settings/                # 设置模块
│   ├── TrentSettings.java   # 主设置面板
│   ├── ReadTipPanel.java    # 阅读提示设置
│   └── ClickLightPanel.java # 标签高亮设置
└── component/               # 插件组件
    └── TrentPluginComponent.java
```

### 依赖技术
- **JSoup**：HTML内容解析
- **IntelliJ Platform SDK**：IDE插件开发框架
- **Swing**：UI界面构建

## 🔧 安装与使用

### 系统要求
- **IntelliJ IDEA**: 2024.1.4 或更高版本
- **Java**: JDK 17
- **Gradle**: 8.0 或更高版本
- **操作系统**: Windows/macOS/Linux

### 安装步骤
1. 下载插件ZIP文件
2. 打开 IntelliJ IDEA
3. 进入 `File` → `Settings` → `Plugins`
4. 点击 ⚙️ 图标选择 `Install Plugin from Disk...`
5. 选择下载的ZIP文件
6. 重启 IDE 完成安装

### 快速开始

#### 1. 配置阅读提示
- 打开 `File` → `Settings` → `Tools` → `Trent Settings`
- 在 Read Tip 选项卡中配置数据源URL
- 使用快捷键 `Ctrl + Alt + Q/W` 控制内容显示

#### 2. 自定义标签高亮
- 在 Click Light 选项卡中启用功能
- 设置活动标签的背景色和透明度
- 即时生效，无需重启IDE

## 🏗️ 开发指南

### 项目构建
```bash
# 使用Gradle构建
./gradlew build

# 运行调试实例（使用配置的2024.1.4版本）
./gradlew runIde

# 使用当前IDEA运行
./gradlew runInCurrentIDEA

# 构建发行版
./gradlew buildPlugin

# 验证插件
./gradlew verifyPlugin
```

### 本地开发
1. 克隆项目到本地
2. **解压IDE运行时**：`Expand-Archive -Path "idea-runtime\ideaIC-2024.2.4.zip" -DestinationPath "idea-runtime" -Force`
3. 使用IntelliJ IDEA打开项目
4. 配置IntelliJ Platform Plugin SDK
5. 运行 `Run Plugin` 配置进行调试

> 💡 **注意**: `idea-runtime` 目录中的ZIP文件已提交到Git，但解压后的文件被忽略。首次使用时需要手动解压ZIP文件。

### 项目配置
- **构建工具**: Gradle Kotlin DSL (build.gradle.kts)
- **语言版本**: Java 17
- **目标平台**: IntelliJ IDEA 2024.1.4 (IU)
- **插件SDK版本**: org.jetbrains.intellij 1.17.3
- **核心依赖**: com.intellij.java

## 📊 插件统计

- **功能模块**: 2个核心功能模块
- **配置选项**: 15+ 可自定义参数
- **快捷键**: 2个主要操作快捷键 (`Ctrl+Alt+Q/W`)
- **兼容性**: IntelliJ IDEA 2021.1 - 2024.x
- **构建配置**:
  - Gradle版本: 9.3.1
  - Java版本: 17
  - 插件版本: 1.17.3
- **依赖库**:
  - JSoup: 1.16.2 (HTML解析)
  - Lombok: 1.18.32 (代码简化)

## 🤝 贡献与反馈

### 开发流程
1. Fork 项目仓库
2. 创建功能分支
3. 实现功能并添加测试
4. 提交 Pull Request

### 问题反馈
如遇到任何问题，请在GitHub Issues中提交详细描述。

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 👨‍💻 作者信息

- **维护者**: Trent
- **联系方式**: panda_old@qq.com
- **个人网站**: https://www.trent.com

---
*让编程变得更简单高效 💻✨*