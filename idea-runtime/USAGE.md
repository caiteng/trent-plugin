# IntelliJ IDEA 运行时使用说明

## 📁 目录结构说明

```
idea-runtime/
├── ideaIC-2024.2.4.zip     # IntelliJ IDEA 运行时ZIP包（已提交到Git）
├── README.md               # 基本说明文件（已提交到Git）
├── USAGE.md                # 本使用说明文件（已提交到Git）
├── LICENSE.txt             # 许可证文件
├── build.txt               # 构建信息
├── product-info.json       # 产品信息
├── bin/                    # 解压后的二进制文件（Git忽略）
├── lib/                    # 解压后的库文件（Git忽略）
├── plugins/                # 解压后的插件文件（Git忽略）
├── license/                # 解压后的许可证文件（Git忽略）
└── ...                     # 其他解压后的文件（Git忽略）
```

## 🚀 使用步骤

### 1. 首次使用（需要解压）

如果你是首次克隆项目或 `idea-runtime` 目录为空，请按以下步骤操作：

```bash
# 进入项目根目录
cd trent-plugin

# 解压IDE运行时（Windows PowerShell）
Expand-Archive -Path "idea-runtime\ideaIC-2024.2.4.zip" -DestinationPath "idea-runtime" -Force

# 或使用命令行工具
# tar -xf idea-runtime/ideaIC-2024.2.4.zip -C idea-runtime
```

### 2. 验证解压结果

解压完成后，应该看到以下目录结构：
```
idea-runtime/
├── ideaIC-2024.2.4.zip     # ZIP源文件
├── bin/                    # 包含 idea64.exe 等启动文件
├── lib/                    # 核心库文件
├── plugins/                # 内置插件
├── license/                # 许可证文件
└── ...                     # 其他必要文件
```

### 3. 构建和运行项目

解压完成后，可以直接使用Gradle命令：

```bash
# 构建项目
./gradlew build

# 运行IDE调试实例
./gradlew runIde

# 使用当前IDEA运行
./gradlew runInCurrentIDEA

# 构建插件
./gradlew buildPlugin
```

## 🔄 更新IDE版本

当需要更新IntelliJ IDEA运行时版本时：

1. 下载新的IDE版本ZIP包
2. 替换 `idea-runtime/ideaIC-2024.2.4.zip` 文件
3. 删除原有的解压文件（`bin/`, `lib/`, `plugins/` 等目录）
4. 重新解压新的ZIP文件
5. 提交更新后的ZIP文件到Git

```bash
# 示例：更新到新版本
rm -rf idea-runtime/bin idea-runtime/lib idea-runtime/plugins idea-runtime/license
# 将新的ZIP文件放入 idea-runtime/ 目录
Expand-Archive -Path "idea-runtime\新的版本.zip" -DestinationPath "idea-runtime" -Force
git add idea-runtime/新的版本.zip
git commit -m "Update IntelliJ IDEA runtime to 新版本"
```

## ⚠️ 注意事项

### Git提交策略
- ✅ **提交**: `ideaIC-2024.2.4.zip` 和说明文档
- ❌ **忽略**: 所有解压后的文件和目录
- 📝 **原因**: 解压后的文件体积巨大（~680MB），且可以通过ZIP文件随时恢复

### 性能优化
- 解压后的文件会被 `.gitignore` 忽略，不会影响Git操作性能
- 团队成员克隆项目后需要手动解压ZIP文件
- 建议在CI/CD环境中也添加自动解压步骤

### 磁盘空间
- ZIP文件大小：约 680MB
- 解压后大小：约 1.2GB
- 请确保有足够的磁盘空间

## 🤝 团队协作建议

### 对于新团队成员
```bash
# 克隆项目后执行
git clone <repository-url>
cd trent-plugin

# 下载IDE运行时ZIP文件到idea-runtime目录
# 下载地址: https://d2cico3c979uwg.cloudfront.net/com/jetbrains/intellij/idea/ideaIC/2024.2.4/ideaIC-2024.2.4.zip

# 解压IDE运行时
Expand-Archive -Path "idea-runtime\ideaIC-2024.2.4.zip" -DestinationPath "idea-runtime" -Force

# 构建项目
./gradlew build
```

### CI/CD 配置示例
```yaml
# GitHub Actions 示例
- name: Setup IntelliJ IDEA Runtime
  run: |
    Expand-Archive -Path "idea-runtime\ideaIC-2024.2.4.zip" -DestinationPath "idea-runtime" -Force
    
- name: Build Plugin
  run: ./gradlew buildPlugin
```

## 📋 故障排除

### 问题1：找不到IDE运行时
**症状**: `Could not find IntelliJ IDEA at ...`
**解决方案**: 确保已正确解压ZIP文件，检查 `idea-runtime/bin` 目录是否存在

### 问题2：权限不足
**症状**: 解压失败或无法访问文件
**解决方案**: 以管理员权限运行命令行工具

### 问题3：版本不匹配
**症状**: 插件API不兼容
**解决方案**: 检查 `build.gradle.kts` 中的IDE版本配置是否与运行时匹配

## 📞 支持信息

如有疑问，请联系项目维护者或查看项目文档。