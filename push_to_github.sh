#!/bin/bash

echo "=== GitHub推送助手 ==="
echo ""
echo "当前Git状态："
git status --short
echo ""
echo "本地提交记录："
git log --oneline -n 5
echo ""
echo "远程仓库配置："
git remote -v
echo ""
echo "准备推送到GitHub..."
echo "请选择认证方式："
echo "1. Personal Access Token (推荐)"
echo "2. GitHub用户名密码"
echo ""
echo "推送命令："
echo "git push -u origin main"
echo ""
echo "如果使用Token："
echo "- 用户名: Jemiking"
echo "- 密码: [您的Personal Access Token]"
echo ""
echo "获取Token: https://github.com/settings/tokens"