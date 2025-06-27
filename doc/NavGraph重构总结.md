# NavGraph 重构总结

## 重构背景
为了适应模块化架构，需要将 `NavGraph.kt` 文件中对 feature-ledger 模块中 Screen 的直接引用改为通过 `LedgerApi` 接口访问。

## 主要改动

### 1. 扩展 LedgerApi 接口
在 `LedgerApi` 接口中添加了获取屏幕的方法：
```kotlin
// Screen Providers
@Composable
fun getLedgerScreen(navController: NavHostController, accountId: String?)

@Composable
fun getTransactionDetailScreen(transactionId: String, navController: NavHostController)

@Composable
fun getAccountScreen(navController: NavHostController)

// ... 其他屏幕方法
```

### 2. 实现 LedgerApiImpl
创建了 `LedgerApiImpl` 类来实现 `LedgerApi` 接口，包括：
- 所有业务方法的占位实现（TODO）
- 屏幕提供方法的实际实现，返回对应的 Composable 屏幕

### 3. 更新 LedgerModule
将 `LedgerModule` 从 object 改为 abstract class，添加了 `LedgerApi` 的绑定：
```kotlin
@Binds
abstract fun bindLedgerApi(ledgerApiImpl: LedgerApiImpl): LedgerApi
```

### 4. 修改 MainActivity
- 注入 `LedgerApi` 实例
- 将 `LedgerApi` 传递给 `NavGraph`

### 5. 重构 NavGraph
- 删除了所有对 feature-ledger 模块中 Screen 的直接导入
- 添加了 `LedgerApi` 参数
- 将所有记账相关的屏幕改为通过 `LedgerApi` 获取

## 重构后的架构优势

1. **解耦合**：app 模块不再直接依赖 feature-ledger 的具体实现
2. **灵活性**：可以轻松替换 LedgerApi 的实现
3. **模块化**：符合模块化架构的依赖原则
4. **可测试性**：可以通过 mock LedgerApi 来测试导航逻辑

## 注意事项

1. 抽象的 `@Composable` 函数不能有默认参数值
2. 所有屏幕获取方法都需要在 `LedgerApiImpl` 中实现
3. 业务方法的实际实现需要后续完成（当前都是 TODO）

## 下一步工作

1. 实现 `LedgerApiImpl` 中的所有业务方法
2. 考虑将导航方法也通过依赖注入的方式实现
3. 为其他 feature 模块创建类似的 API 接口