# ✅ 最终检查清单

## 重启前必须完成
- [x] 创建会话恢复文档 (CLAUDE_SESSION_RESTORE.md)
- [x] 更新快速恢复指南 (README_RESTART_CLAUDE.md) 
- [x] 创建自动恢复脚本 (scripts/restore-session.sh)
- [x] 验证版本迁移完成情况
- [x] 保存所有重要文件路径

## 重启后立即执行
1. [ ] 运行恢复脚本: `./scripts/restore-session.sh`
2. [ ] 测试MCP: `使用mcp__kotlin-compiler__compile_project工具`
3. [ ] 如果MCP仍有问题，检查日志: `tail -n 20 combined.log`

## 待提交的文件
```bash
# 查看所有修改
git status

# 需要提交的核心文件
git add shared/*/build.gradle.kts
git add /mnt/d/kotlin/mcp-kotlin-compiler/src/server.ts

# 可选：提交文档和脚本
git add doc/20250628-*.md
git add scripts/restore-session.sh
git add CLAUDE_SESSION_RESTORE.md
```

## MCP修复验证
1. 检查日志是否显示 "Executing in WSL" 而不是 "Executing in Windows"
2. 确认没有 ".\\gradlew.bat: not found" 错误
3. 编译应该成功完成

## 关键命令备忘
```bash
# MCP相关
claude mcp list
pkill -f mcp-kotlin-compiler

# 编译测试
./gradlew :shared-user:compileKotlin
./gradlew clean build

# Git操作
git checkout feature/plan-module-migration
git log --oneline -5
```