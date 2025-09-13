## 变更说明

- 目的：
- 关联 Issue：
- 影响范围：

## 自检清单（提交前请确认）

- [ ] 已本地执行 `./gradlew check` 并通过（包含：重复枚举名校验）
- [ ] 如涉及数据库：已更新 Room Schema（`app/schemas`）与 Migration；必要时补充/更新相关测试
- [ ] 如涉及构建/脚本/签名：已在下方“特别说明”中注明并给出验证方法
- [ ] 如涉及 UI 变更：已附截图/GIF
- [ ] 单元测试 `./gradlew test` 通过（如适用）
- [ ] 仪器/UI 测试 `./gradlew connectedAndroidTest` 通过（如适用）
- [ ] Lint `./gradlew lint` 通过（如适用）

## 测试计划与命令

示例：
```
./gradlew check
./gradlew test
./gradlew :app:assembleDebug
```

## 特别说明

- 数据库/安全/构建相关的额外说明：
- 回滚方案：

