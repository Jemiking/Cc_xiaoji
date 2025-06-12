# Git Commit 建议

## 建议的提交信息

```bash
git add .
git commit -m "架构迁移: 完成Ledger模块交易记录功能迁移

- 扩展LedgerApi接口，添加完整的交易管理方法
- 迁移TransactionRepository到feature-ledger模块
- 创建新的LedgerViewModel和LedgerScreen
- 更新app模块使用LedgerApi替代TransactionRepository
- 更新DataExportViewModel使用LedgerApi导出交易数据

待完成：
- 账户管理功能迁移
- 预算管理功能迁移
- 存钱目标功能迁移
- 定期交易功能迁移
- 信用卡管理功能迁移

进度：Ledger模块迁移约60%完成"
```

## 或者使用更详细的提交信息

```bash
git add .
git commit -m "refactor: 迁移Ledger模块交易记录功能到feature-ledger

### 完成的工作
1. LedgerApi扩展
   - 添加交易CRUD方法
   - 添加交易搜索和统计功能
   - 定义TransactionItem和TransactionDetail数据模型

2. 核心组件迁移
   - TransactionRepository完整迁移
   - 创建feature-ledger的LedgerViewModel
   - 迁移LedgerScreen和相关UI组件

3. App模块更新
   - LedgerViewModel使用LedgerApi
   - DataExportViewModel使用LedgerApi
   - NavGraph使用feature-ledger的UI

### 技术细节
- 解决了TransactionItem到Transaction的类型转换
- 处理了Category.Type枚举映射
- 实现了按日期范围获取交易的逻辑

### 注意事项
- TransactionRepository暂时保留，因其他组件依赖
- StatisticsScreen等组件待后续迁移

Refs: #architecture-migration"
```

## 标签建议

创建一个标签来标记当前进度：

```bash
git tag -a "migration-ledger-transaction-v1" -m "架构迁移：Ledger模块交易记录功能完成"
```

## 分支建议

如果还没有创建专门的迁移分支：

```bash
git checkout -b feature/architecture-migration
```

## 推送到远程仓库

```bash
git push origin feature/architecture-migration
git push origin --tags
```