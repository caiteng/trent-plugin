# Trent-Helper IntelliJ Plugin

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![IntelliJ Platform](https://img.shields.io/badge/IntelliJ%20Platform-2021.1+-brightgreen.svg)](https://plugins.jetbrains.com/)
[![Java](https://img.shields.io/badge/java-8+-blue.svg)](https://www.oracle.com/java/)

## 📝 项目简介

Trent-Helper 是一个旨在提升开发者工作效率的 IntelliJ IDEA 插件。该插件提供了多种实用功能来改善日常开发体验。

## ✨ 核心功能

### 1. 阅读提示功能 (Read Tip)
- 读取tip文件，提供智能阅读提示
- 帮助开发者快速获取关键信息
- 支持快捷键操作：
  - `Ctrl + Alt + Q`: 增加提示
  - `Ctrl + Alt + W`: 减少提示

### 2. 文件点击轨迹高亮 (Click Light)
- 可视化显示文件点击历史轨迹
- 便于在多个文件间快速切换和修改代码
- 提供标签页颜色标识功能

### 3. 智能配置界面
- 集成的设置面板，方便自定义各项功能
- 支持个性化配置保存

## 🔧 安装要求

- **IntelliJ IDEA**: 2024.1 或更高版本
- **Java**: JDK 11 或更高版本
- **操作系统**: Windows/macOS/Linux

## 🚀 安装方式

### 方式一：从插件市场安装
1. 打开 IntelliJ IDEA
2. 进入 `File` → `Settings` → `Plugins`
3. 搜索 "Trent-Helper"
4. 点击安装并重启 IDE

### 方式二：手动安装
1. 下载插件jar文件
2. 在 IDEA 中进入 `File` → `Settings` → `Plugins`
3. 点击齿轮图标选择 `Install Plugin from Disk`
4. 选择下载的jar文件并安装

## ⚙️ 使用说明

### 启用功能
安装后插件会自动启用，您可以在以下位置找到相关功能：

- **设置界面**: `File` → `Settings` → `Tools` → `Trent Settings`
- **快捷键**: 
  - 增加提示: `Ctrl + Alt + Q`
  - 减少提示: `Ctrl + Alt + W`

### 配置选项
在设置面板中可以配置：
- 阅读提示的显示样式
- 文件轨迹高亮的颜色和效果
- 各项功能的启用状态

## 🛠️ 开发环境搭建

### 克隆项目
```bash
git clone https://github.com/yourusername/trent-plugin.git
cd trent-plugin
```

### 构建项目
```bash
# Windows
.\gradlew.bat build

# macOS/Linux
./gradlew build
```

### 调试运行
1. 在 IntelliJ IDEA 中打开项目
2. 创建 Plugin 运行配置
3. 点击 Run 或 Debug 按钮

### 项目结构
```
src/
├── main/
│   ├── java/org/trent/helper/
│   │   ├── actions/          # 动作处理器
│   │   ├── clicklight/       # 点击轨迹功能
│   │   ├── component/        # 插件组件
│   │   ├── readtip/          # 阅读提示功能
│   │   └── settings/         # 设置界面
│   └── resources/META-INF/
│       └── plugin.xml        # 插件配置文件
```

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 开发规范
- 遵循 IntelliJ Platform SDK 最佳实践
- 保持代码风格一致性
- 添加适当的单元测试
- 更新相关文档

### 提交流程
1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📧 联系方式

- **作者**: Trent
- **邮箱**: panda_old@qq.com
- **网站**: https://www.trent.com

## 🙏 致谢

感谢 JetBrains 提供的优秀的开发平台和插件开发工具。

---
*Made with ❤️ by Trent*