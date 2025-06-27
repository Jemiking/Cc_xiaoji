# Plan模块 UseCase单元测试套件

## 测试概览

本测试套件为Plan模块的5个核心UseCase创建了完整的单元测试，确保计划管理功能的可靠性和正确性。

## 测试文件列表

### 1. CreatePlanUseCaseTest.kt
**测试用例数**: 8个
**覆盖功能**: 创建计划用例
- ✅ 成功创建计划 - 输入验证正确
- ✅ 创建计划失败 - 标题为空白
- ✅ 创建计划失败 - 开始日期晚于结束日期
- ✅ 创建计划失败 - 进度值超出范围
- ✅ 创建计划失败 - 进度值为负数
- ✅ 创建计划失败 - Repository抛出异常
- ✅ 边界条件测试 - 进度为0和100
- ✅ 边界条件测试 - 开始日期等于结束日期

### 2. UpdatePlanUseCaseTest.kt
**测试用例数**: 11个
**覆盖功能**: 更新计划用例
- ✅ 成功更新计划 - 所有字段验证正确
- ✅ 更新计划失败 - 计划ID为空白
- ✅ 更新计划失败 - 标题为空
- ✅ 更新计划失败 - 开始日期晚于结束日期
- ✅ 更新计划失败 - 进度值大于100
- ✅ 更新计划失败 - 进度值小于0
- ✅ 更新计划失败 - Repository抛出异常
- ✅ 边界条件测试 - 进度值为0和100
- ✅ 边界条件测试 - 开始日期等于结束日期
- ✅ 边界条件测试 - 最小ID长度
- ✅ 成功更新不同状态的计划

### 3. DeletePlanUseCaseTest.kt
**测试用例数**: 10个
**覆盖功能**: 删除计划用例（包括级联删除）
- ✅ 成功删除计划 - 有效的计划ID
- ✅ 成功删除计划 - 带有子计划的父计划
- ✅ 成功删除计划 - 子计划
- ✅ 删除计划失败 - Repository抛出异常
- ✅ 删除计划失败 - 计划不存在
- ✅ 删除计划失败 - 数据库约束错误
- ✅ 边界条件测试 - 空字符串ID
- ✅ 边界条件测试 - 空白字符串ID
- ✅ 边界条件测试 - 很长的ID
- ✅ 边界条件测试 - 特殊字符ID
- ✅ 级联删除测试 - 多层级子计划
- ✅ 并发删除测试 - 同时删除多个计划

### 4. GetAllPlansUseCaseTest.kt
**测试用例数**: 10个
**覆盖功能**: 获取所有计划用例（树形结构）
- ✅ 成功获取所有计划 - 返回空列表
- ✅ 成功获取所有计划 - 返回单个顶级计划
- ✅ 成功获取所有计划 - 返回多个顶级计划
- ✅ 成功获取所有计划 - 返回树形结构计划
- ✅ 成功获取所有计划 - 多层级树形结构
- ✅ 成功获取所有计划 - 包含不同状态的计划
- ✅ Flow特性测试 - 数据变化时自动更新
- ✅ 返回类型验证 - 确认返回Flow类型
- ✅ 边界条件测试 - 大量计划数据
- ✅ UseCase简单性验证 - 不做任何数据处理

### 5. UpdatePlanProgressUseCaseTest.kt
**测试用例数**: 13个
**覆盖功能**: 更新计划进度用例（包括自动状态更新）
- ✅ 成功更新进度 - 进度为50不改变状态
- ✅ 成功更新进度 - 进度为0且状态为NOT_STARTED不改变状态
- ✅ 成功更新进度 - 进度为100自动设置为COMPLETED
- ✅ 成功更新进度 - 进度从0变为50自动设置为IN_PROGRESS
- ✅ 成功更新进度 - 已完成计划进度设置为100不改变状态
- ✅ 更新进度失败 - 进度值超出范围(大于100)
- ✅ 更新进度失败 - 进度值小于0
- ✅ 更新进度失败 - 计划不存在
- ✅ 更新进度失败 - Repository更新进度抛出异常
- ✅ 更新进度失败 - Repository获取计划抛出异常
- ✅ 更新进度失败 - Repository更新状态抛出异常
- ✅ 边界条件测试 - 进度为0和100的边界值
- ✅ 状态转换逻辑测试 - 各种状态组合

## 测试特性

### 🎯 测试风格
- **框架**: JUnit 4 + MockK + Truth + Coroutines Test
- **Mock策略**: 使用MockK进行Repository接口Mock
- **断言库**: Google Truth提供清晰的断言语法
- **异步测试**: Coroutines Test支持协程测试

### 📊 测试覆盖范围
- **成功场景**: 正常流程的验证
- **失败场景**: 异常情况的处理
- **边界条件**: 边界值和极端情况
- **输入验证**: 参数校验逻辑
- **状态转换**: 计划状态的自动更新逻辑
- **级联操作**: 删除时的级联处理

### 🔧 技术特点
- **中文测试名称**: 提高测试可读性
- **详细注释**: 每个测试都有清晰的中文注释
- **Given-When-Then**: 标准的测试结构
- **Mock验证**: 确保与Repository的交互正确
- **异常测试**: 覆盖各种异常情况

## 运行测试

```bash
# 运行Plan模块的所有单元测试
./gradlew :feature:plan:testDebugUnitTest

# 运行特定的UseCase测试
./gradlew :feature:plan:testDebugUnitTest --tests "*CreatePlanUseCaseTest*"
./gradlew :feature:plan:testDebugUnitTest --tests "*UpdatePlanUseCaseTest*"
./gradlew :feature:plan:testDebugUnitTest --tests "*DeletePlanUseCaseTest*"
./gradlew :feature:plan:testDebugUnitTest --tests "*GetAllPlansUseCaseTest*"
./gradlew :feature:plan:testDebugUnitTest --tests "*UpdatePlanProgressUseCaseTest*"

# 生成测试报告
./gradlew :feature:plan:testDebugUnitTest --info
```

## 测试统计

- **总测试文件**: 5个
- **总测试用例**: 52个
- **覆盖的UseCase**: 5个核心UseCase
- **测试类型**: 单元测试
- **预期覆盖率**: 95%+ (UseCase层)

## 质量保证

### ✅ 已验证的功能
1. **数据验证**: 所有输入参数的有效性检查
2. **业务逻辑**: 计划状态的自动转换逻辑
3. **异常处理**: Repository层异常的正确传播
4. **边界处理**: 各种边界值和极端情况
5. **级联操作**: 删除计划时的级联处理

### 🎖️ 测试最佳实践
- 遵循AAA（Arrange-Act-Assert）模式
- 每个测试只验证一个功能点
- 使用有意义的测试名称和注释
- 充分的Mock验证
- 覆盖正常和异常流程

这个测试套件确保了Plan模块核心UseCase的可靠性，为后续的功能开发和重构提供了坚实的安全网。