# Trent Plugin

这是一个IntelliJ IDEA插件项目，提供了多种实用功能。

## 功能特性

- **MyBatis跳转功能** - 快速在Mapper接口和XML文件间跳转
- **点击高亮** - 活动标签页高亮显示
- **阅读提示** - 小说内容读取功能（基于jsoup）
- **自定义设置** - 可配置的各项功能参数

## 环境要求

- Java 11 或更高版本
- IntelliJ IDEA 2024.1.4 或更高版本
- Gradle 7.x

## 快速开始

### 克隆项目

```bash
git clone <repository-url>
cd trent-plugin
```

### 构建项目

#### Windows:
```bash
./gradlew.bat build
```

#### Linux/Mac:
```bash
./gradlew build
```

### 运行插件

#### 在IntelliJ IDEA中运行:
```bash
./gradlew.bat runIde
```

#### 构建插件分发包:
```bash
./gradlew.bat buildPlugin
```

生成的插件包将在 `build/distributions/` 目录下。

## 开发指南

### 项目结构

```
src/
├── main/
│   ├── java/org/trent/helper/
│   │   ├── actions/          # 动作类
│   │   ├── clicklight/       # 点击高亮功能
│   │   ├── component/        # 组件类
│   │   ├── mybatis/          # MyBatis相关功能
│   │   ├── readtip/          # 阅读提示功能
│   │   └── settings/         # 设置界面
│   └── resources/
│       └── META-INF/
│           └── plugin.xml    # 插件配置文件
```

### 主要组件

1. **MyBatisJumpAction** - MyBatis跳转核心功能
2. **ActiveTabHighlighter** - 标签页高亮功能
3. **ReadTip** - 小说阅读功能（使用jsoup解析网页）
4. **Setting panels** - 各项功能的设置界面

## 配置说明

插件支持以下配置：

- 点击高亮颜色和样式
- MyBatis文件路径映射
- 阅读提示的数据源设置
- 各项功能的启用/禁用

## 贡献指南

1. Fork 本项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

如有问题或建议，请提交 Issue 或联系项目维护者。