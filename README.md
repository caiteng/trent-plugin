# Trent Plugin

一款简洁实用的IntelliJ IDEA阅读辅助插件，帮助开发者在编码时轻松阅读网络内容。

## 🚀 快速开始

### 安装方式
1. 下载插件ZIP文件
2. 打开 IntelliJ IDEA
3. 进入 `File` → `Settings` → `Plugins`
4. 点击 ⚙️ 图标选择 `Install Plugin from Disk...`
5. 选择下载的ZIP文件
6. 重启 IDE 完成安装

## 📖 使用指南

### 1. 基本配置
打开 `File` → `Settings` → `Tools` → `Trent Settings` 进行配置

### 2. 数据源管理
- **添加数据源**：
  - 在"新URL"输入框中输入网站地址
  - 在"标题"输入框中命名数据源
  - 点击"加载"按钮完成添加
- **切换数据源**：从下拉框选择已保存的数据源
- **删除数据源**：选择数据源后点击"删除"按钮

### 3. 导航测试
- **上一页测试**：验证上一页导航配置
- **下一页测试**：验证下一页导航配置

> 💡 **提示**：首次使用请先添加数据源，建议添加小说网站或技术博客

### 4. 快捷键操作
- `Ctrl + Alt + Q`：显示下一段内容
- `Ctrl + Alt + W`：显示上一段内容

## 🛠️ 开发相关

### 构建命令
```bash
./gradlew build          # 构建项目
./gradlew runIde         # 运行调试实例
./gradlew buildPlugin    # 构建发行版
```

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

**作者**: Trent  
**邮箱**: panda_old@qq.com