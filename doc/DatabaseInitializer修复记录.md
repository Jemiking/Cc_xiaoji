# DatabaseInitializer 修复记录

## 编译错误修复

### 错误原因
`DatabaseInitializer.kt` 中调用的 DAO 方法名与实际定义不符：

| 错误的方法调用 | 正确的方法名 | 所在行数 |
|--------------|------------|---------|
| `userDao.insert()` | `userDao.insertUser()` | 71 |
| `accountDao.insert()` | `accountDao.insertAccount()` | 95 |
| `categoryDao.insertAll()` | `categoryDao.insertCategories()` | 147, 169 |

### 修复内容
已将 `DatabaseInitializer.kt` 中的所有方法调用修正为正确的 DAO 方法名：

1. **第71行**：
   ```kotlin
   userDao.insertUser(defaultUser)  // 修正前：userDao.insert(defaultUser)
   ```

2. **第95行**：
   ```kotlin
   accountDao.insertAccount(defaultAccount)  // 修正前：accountDao.insert(defaultAccount)
   ```

3. **第147行**：
   ```kotlin
   categoryDao.insertCategories(expenseEntities)  // 修正前：categoryDao.insertAll(expenseEntities)
   ```

4. **第169行**：
   ```kotlin
   categoryDao.insertCategories(incomeEntities)  // 修正前：categoryDao.insertAll(incomeEntities)
   ```

## 验证步骤

由于 gradlew 脚本存在换行符问题（Windows 格式），请在 Windows 环境下运行：

```bash
# Windows PowerShell 或 CMD
gradlew.bat :app:compileDebugKotlin
```

或在 Android Studio 中直接构建项目。

## 说明
这是数据库初始化系统性修复方案的一部分，`DatabaseInitializer` 类为备用方案，当前 `DatabaseModule` 中的 SQL 初始化也已修复。