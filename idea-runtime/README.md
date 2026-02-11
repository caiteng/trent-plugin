# IntelliJ IDEA 运行时环境

## 目录说明
此目录包含用于插件开发的IntelliJ IDEA Community Edition运行时环境。

## 文件列表
- `ideaIC-2024.2.4.zip` - IntelliJ IDEA Community Edition 2024.2.4 (已打包，已提交Git)
- `README.md` - 本说明文件 (已提交Git)
- `USAGE.md` - 详细使用说明 (已提交Git)

## 使用方式
1. **首次使用需要解压**: `Expand-Archive -Path "ideaIC-2024.2.4.zip" -DestinationPath "." -Force`
2. 构建脚本会自动检测此目录中的IDE文件，优先使用项目内置的IDE进行插件调试

## Git 管理策略
- ✅ **提交到Git**: ZIP文件和说明文档
- ❌ **忽略提交**: 解压后的所有文件和目录
- 📝 **原理**: 通过ZIP文件可以随时恢复完整运行时环境

## 注意事项
- 文件较大，请确保有足够的磁盘空间
- 解压后大小约为原始ZIP的2倍
- 团队成员需要手动解压ZIP文件才能正常使用
- 如需更新IDE版本，请替换此目录中的zip文件并重新解压

## 详细使用说明
请查看 [USAGE.md](USAGE.md) 文件获取完整的使用指南、故障排除和最佳实践。