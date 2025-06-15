# 编译验证清单

## 修复的问题
1. **SavingsGoalDetailScreen.kt**
   - 移除了不存在的 `toJavaLocalDate()` 扩展函数调用（第422行）
   - 移除了不存在的 `toJavaInstant()` 扩展函数调用（第567行）

## 原因分析
- `targetDate` 在 `SavingsGoalItem` 中已经是 `java.time.LocalDate` 类型，无需转换
- `createdAt` 在 `SavingsContributionItem` 中已经是 `java.time.Instant` 类型，无需转换

## 编译命令
```bash
# 编译 presentation 模块
./gradlew :feature:ledger:presentation:compileDebugKotlin

# 如果需要编译整个 ledger 功能模块
./gradlew :feature:ledger:compileDebugKotlin
```

## TODO: 编译验证
- [ ] 验证移除的扩展函数调用不会导致编译错误
- [ ] 确认日期格式化功能正常工作
- [ ] 测试运行时日期显示是否正确