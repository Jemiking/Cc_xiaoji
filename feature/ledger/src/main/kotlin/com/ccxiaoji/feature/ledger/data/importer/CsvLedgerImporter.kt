package com.ccxiaoji.feature.ledger.data.importer

import android.content.Context
import android.util.Log
import com.ccxiaoji.feature.ledger.data.importer.converter.*
import com.ccxiaoji.feature.ledger.data.importer.validator.DataValidator
import com.ccxiaoji.feature.ledger.data.importer.validator.ValidationResult
import com.ccxiaoji.feature.ledger.data.importer.resolver.ConflictResolver
import com.ccxiaoji.feature.ledger.data.importer.resolver.ResolveResult
import com.ccxiaoji.feature.ledger.data.local.dao.*
import com.ccxiaoji.feature.ledger.data.local.entity.*
import com.ccxiaoji.feature.ledger.domain.importer.*
import com.ccxiaoji.shared.user.data.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * CSV格式的记账数据导入器
 * 支持v2.0和v2.1格式
 */
class CsvLedgerImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val creditCardBillDao: CreditCardBillDao,
    private val creditCardPaymentDao: CreditCardPaymentDao,
    private val userRepository: UserRepository,
    private val csvParser: CsvParser,
    private val accountConverter: AccountConverter,
    private val categoryConverter: CategoryConverter,
    private val transactionConverter: TransactionConverter,
    private val budgetConverter: BudgetConverter,
    private val savingsGoalConverter: SavingsGoalConverter,
    private val dataValidator: DataValidator,
    private val conflictResolver: ConflictResolver
) : LedgerImporter {
    
    companion object {
        private const val TAG = "CsvLedgerImporter"
    }
    
    override suspend fun importData(
        file: File,
        config: ImportConfig
    ): ImportResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. 验证文件
            if (!validateFile(file)) {
                return@withContext ImportResult(
                    success = false,
                    totalRows = 0,
                    successCount = 0,
                    failedCount = 0,
                    skippedCount = 0,
                    errors = listOf(ImportError.FormatError(0, "不支持的文件格式")),
                    summary = ImportSummary()
                )
            }
            
            // 2. 解析CSV文件
            val parsedData = csvParser.parseFile(file)
            
            // 3. 获取当前用户
            val currentUser = userRepository.getCurrentUser()
                ?: throw IllegalStateException("当前用户不存在")
            
            Log.d(TAG, "开始导入数据，用户ID: ${currentUser.id}")
            
            // 4. 按依赖顺序导入数据
            val importContext = ImportContext(
                userId = currentUser.id,
                config = config
            )
            
            // 5. 执行导入
            val result = importDataInOrder(parsedData, importContext)
            
            // 6. 计算耗时
            val duration = System.currentTimeMillis() - startTime
            
            return@withContext result.copy(duration = duration)
            
        } catch (e: Exception) {
            Log.e(TAG, "导入失败", e)
            return@withContext ImportResult(
                success = false,
                totalRows = 0,
                successCount = 0,
                failedCount = 0,
                skippedCount = 0,
                errors = listOf(ImportError.DatabaseError(0, "导入失败: ${e.message}", e)),
                summary = ImportSummary()
            )
        }
    }
    
    override suspend fun previewImport(file: File): ImportPreview = withContext(Dispatchers.IO) {
        try {
            val parsedData = csvParser.parseFile(file)
            
            ImportPreview(
                fileName = file.name,
                fileSize = file.length(),
                format = "CSV",
                version = parsedData.version,
                totalRows = parsedData.totalRows,
                dataTypes = parsedData.dataTypeCounts,
                dateRange = parsedData.dateRange,
                hasErrors = parsedData.errors.isNotEmpty(),
                errors = parsedData.errors
            )
        } catch (e: Exception) {
            ImportPreview(
                fileName = file.name,
                fileSize = file.length(),
                format = "CSV",
                version = null,
                totalRows = 0,
                dataTypes = emptyMap(),
                dateRange = null,
                hasErrors = true,
                errors = listOf(ImportError.FormatError(0, "文件解析失败: ${e.message}"))
            )
        }
    }
    
    override suspend fun validateFile(file: File): Boolean {
        // 基本检查：文件存在且可读
        if (!file.exists() || !file.isFile || !file.canRead()) {
            return false
        }
        
        // 检查文件扩展名（可选）或者内容格式
        // 如果不是.csv扩展名，尝试读取第一行检查是否为CSV格式
        if (!file.extension.equals("csv", ignoreCase = true)) {
            try {
                val firstLine = file.bufferedReader().use { it.readLine() }
                // 检查第一行是否包含CSV格式的特征（逗号分隔）
                return firstLine != null && firstLine.contains(",")
            } catch (e: Exception) {
                Log.w(TAG, "无法读取文件内容进行验证", e)
                return false
            }
        }
        
        return true
    }
    
    private suspend fun importDataInOrder(
        parsedData: ParsedData,
        context: ImportContext
    ): ImportResult {
        val errors = mutableListOf<ImportError>()
        var successCount = 0
        var failedCount = 0
        var skippedCount = 0
        
        var summary = ImportSummary()
        
        try {
            // 1. 导入账户（无依赖）
            val accountResult = importAccounts(parsedData.accounts, context)
            summary = summary.copy(accountsImported = accountResult.imported)
            successCount += accountResult.imported
            failedCount += accountResult.failed
            skippedCount += accountResult.skipped
            errors.addAll(accountResult.errors)
            
            // 2. 导入分类（可能依赖父分类）
            val categoryResult = importCategories(parsedData.categories, context)
            summary = summary.copy(categoriesImported = categoryResult.imported)
            successCount += categoryResult.imported
            failedCount += categoryResult.failed
            skippedCount += categoryResult.skipped
            errors.addAll(categoryResult.errors)
            
            // 3. 导入交易（依赖账户和分类）
            val transactionResult = importTransactions(parsedData.transactions, context)
            summary = summary.copy(transactionsImported = transactionResult.imported)
            successCount += transactionResult.imported
            failedCount += transactionResult.failed
            skippedCount += transactionResult.skipped
            errors.addAll(transactionResult.errors)
            
            // 4. 导入预算（依赖分类）
            val budgetResult = importBudgets(parsedData.budgets, context)
            summary = summary.copy(budgetsImported = budgetResult.imported)
            successCount += budgetResult.imported
            failedCount += budgetResult.failed
            skippedCount += budgetResult.skipped
            errors.addAll(budgetResult.errors)
            
            // 5. 导入定期交易（依赖账户和分类）
            val recurringResult = importRecurringTransactions(parsedData.recurringTransactions, context)
            summary = summary.copy(recurringImported = recurringResult.imported)
            successCount += recurringResult.imported
            failedCount += recurringResult.failed
            skippedCount += recurringResult.skipped
            errors.addAll(recurringResult.errors)
            
            // 6. 导入储蓄目标
            val savingsResult = importSavingsGoals(parsedData.savingsGoals, context)
            summary = summary.copy(savingsImported = savingsResult.imported)
            successCount += savingsResult.imported
            failedCount += savingsResult.failed
            skippedCount += savingsResult.skipped
            errors.addAll(savingsResult.errors)
            
            // 7. 导入信用卡账单（依赖账户）
            val creditBillResult = importCreditBills(parsedData.creditBills, context)
            summary = summary.copy(creditBillsImported = creditBillResult.imported)
            successCount += creditBillResult.imported
            failedCount += creditBillResult.failed
            skippedCount += creditBillResult.skipped
            errors.addAll(creditBillResult.errors)
            
        } catch (e: Exception) {
            Log.e(TAG, "导入过程发生错误", e)
            errors.add(ImportError.DatabaseError(0, "导入失败: ${e.message}", e))
        }
        
        return ImportResult(
            success = failedCount == 0 || (context.config.allowPartialImport && successCount > 0),
            totalRows = parsedData.totalRows,
            successCount = successCount,
            failedCount = failedCount,
            skippedCount = skippedCount,
            errors = errors,
            summary = summary
        )
    }
    
    // 导入账户的具体实现
    private suspend fun importAccounts(
        accounts: List<DataLine>,
        context: ImportContext
    ): ImportBatchResult {
        var imported = 0
        var failed = 0
        var skipped = 0
        val errors = mutableListOf<ImportError>()
        val batch = mutableListOf<AccountEntity>()
        
        accounts.forEach { dataLine ->
            when (val convertResult = accountConverter.convert(dataLine, context.userId)) {
                is ConvertResult.Success -> {
                    val account = convertResult.data
                    
                    // 验证数据
                    when (val validationResult = dataValidator.validateAccount(account)) {
                        is ValidationResult.Success -> {
                            // 处理冲突
                            when (val resolveResult = conflictResolver.resolveAccountConflict(
                                account, 
                                context.config.conflictStrategy
                            )) {
                                is ResolveResult.NoConflict -> {
                                    batch.add(resolveResult.data)
                                    context.accountMap[account.name] = account.id
                                }
                                is ResolveResult.Modified -> {
                                    batch.add(resolveResult.data)
                                    context.accountMap[account.name] = resolveResult.data.id
                                }
                                is ResolveResult.Merge -> {
                                    accountDao.updateAccount(resolveResult.data)
                                    context.accountMap[account.name] = resolveResult.data.id
                                    imported++
                                }
                                is ResolveResult.Skip -> {
                                    skipped++
                                    Log.d(TAG, resolveResult.reason)
                                    // 将已存在账户的ID添加到映射中
                                    resolveResult.existingData?.let { existingAccount ->
                                        context.accountMap[existingAccount.name] = existingAccount.id
                                    }
                                }
                            }
                        }
                        is ValidationResult.Failed -> {
                            failed++
                            errors.add(ImportError.ValidationError(
                                dataLine.line,
                                "账户",
                                validationResult.errors.joinToString("; ")
                            ))
                        }
                    }
                }
                is ConvertResult.Error -> {
                    failed++
                    errors.add(convertResult.error)
                }
            }
            
            // 批量插入
            if (batch.size >= context.config.batchSize) {
                batch.forEach { accountDao.insertAccount(it) }
                imported += batch.size
                batch.clear()
            }
        }
        
        // 插入剩余数据
        if (batch.isNotEmpty()) {
            batch.forEach { accountDao.insertAccount(it) }
            imported += batch.size
        }
        
        return ImportBatchResult(imported, failed, skipped, errors)
    }
    
    // 导入分类的具体实现
    private suspend fun importCategories(
        categories: List<DataLine>,
        context: ImportContext
    ): ImportBatchResult {
        var imported = 0
        var failed = 0
        var skipped = 0
        val errors = mutableListOf<ImportError>()
        val batch = mutableListOf<CategoryEntity>()
        
        categories.forEach { dataLine ->
            when (val convertResult = categoryConverter.convert(dataLine, context.userId)) {
                is ConvertResult.Success -> {
                    val category = convertResult.data
                    
                    // 验证数据
                    when (val validationResult = dataValidator.validateCategory(category)) {
                        is ValidationResult.Success -> {
                            // 处理冲突
                            when (val resolveResult = conflictResolver.resolveCategoryConflict(
                                category,
                                context.config.conflictStrategy
                            )) {
                                is ResolveResult.NoConflict -> {
                                    batch.add(resolveResult.data)
                                    context.categoryMap[category.name] = category.id
                                }
                                is ResolveResult.Modified -> {
                                    batch.add(resolveResult.data)
                                    context.categoryMap[category.name] = resolveResult.data.id
                                }
                                is ResolveResult.Merge -> {
                                    // 合并策略：使用已存在的分类
                                    context.categoryMap[category.name] = resolveResult.data.id
                                    imported++
                                }
                                is ResolveResult.Skip -> {
                                    skipped++
                                    Log.d(TAG, resolveResult.reason)
                                    // 将已存在分类的ID添加到映射中
                                    resolveResult.existingData?.let { existingCategory ->
                                        context.categoryMap[existingCategory.name] = existingCategory.id
                                    }
                                }
                            }
                        }
                        is ValidationResult.Failed -> {
                            failed++
                            errors.add(ImportError.ValidationError(
                                dataLine.line,
                                "分类",
                                validationResult.errors.joinToString("; ")
                            ))
                        }
                    }
                }
                is ConvertResult.Error -> {
                    failed++
                    errors.add(convertResult.error)
                }
            }
            
            // 批量插入
            if (batch.size >= context.config.batchSize) {
                categoryDao.insertCategories(batch)
                imported += batch.size
                batch.clear()
            }
        }
        
        // 插入剩余数据
        if (batch.isNotEmpty()) {
            categoryDao.insertCategories(batch)
            imported += batch.size
        }
        
        return ImportBatchResult(imported, failed, skipped, errors)
    }
    
    // 导入交易的具体实现
    private suspend fun importTransactions(
        transactions: List<DataLine>,
        context: ImportContext
    ): ImportBatchResult {
        var imported = 0
        var failed = 0
        var skipped = 0
        val errors = mutableListOf<ImportError>()
        val batch = mutableListOf<TransactionEntity>()
        
        transactions.forEach { dataLine ->
            when (val convertResult = transactionConverter.convert(dataLine, context.userId)) {
                is ConvertResult.Success -> {
                    var transaction = convertResult.data
                    
                    // 映射账户和分类ID
                    val accountId = context.accountMap[transaction.accountId]
                    val categoryId = context.categoryMap[transaction.categoryId]
                    
                    if (accountId == null) {
                        failed++
                        errors.add(ImportError.DependencyError(
                            dataLine.line,
                            "账户: ${transaction.accountId}",
                            "找不到对应的账户"
                        ))
                        return@forEach
                    }
                    
                    if (categoryId == null) {
                        failed++
                        errors.add(ImportError.DependencyError(
                            dataLine.line,
                            "分类: ${transaction.categoryId}",
                            "找不到对应的分类"
                        ))
                        return@forEach
                    }
                    
                    // 更新实际的ID
                    transaction = transaction.copy(
                        accountId = accountId,
                        categoryId = categoryId
                    )
                    
                    // 检测重复
                    if (context.config.conflictStrategy == ConflictStrategy.SKIP) {
                        if (conflictResolver.isTransactionDuplicate(transaction)) {
                            skipped++
                            return@forEach
                        }
                    }
                    
                    batch.add(transaction)
                }
                is ConvertResult.Error -> {
                    failed++
                    errors.add(convertResult.error)
                }
            }
            
            // 批量插入
            if (batch.size >= context.config.batchSize) {
                transactionDao.insertTransactions(batch)
                imported += batch.size
                batch.clear()
            }
        }
        
        // 插入剩余数据
        if (batch.isNotEmpty()) {
            transactionDao.insertTransactions(batch)
            imported += batch.size
        }
        
        return ImportBatchResult(imported, failed, skipped, errors)
    }
    
    // 导入预算的具体实现
    private suspend fun importBudgets(budgets: List<DataLine>, context: ImportContext): ImportBatchResult {
        var imported = 0
        var failed = 0
        var skipped = 0
        val errors = mutableListOf<ImportError>()
        val batch = mutableListOf<BudgetEntity>()
        
        budgets.forEach { dataLine ->
            when (val convertResult = budgetConverter.convert(dataLine, context.userId)) {
                is ConvertResult.Success -> {
                    var budget = convertResult.data
                    
                    // 映射分类ID
                    val categoryId = context.categoryMap[budget.categoryId]
                    
                    if (categoryId == null) {
                        failed++
                        errors.add(ImportError.DependencyError(
                            dataLine.line,
                            "分类: ${budget.categoryId}",
                            "找不到对应的分类"
                        ))
                        return@forEach
                    }
                    
                    // 更新实际的ID
                    budget = budget.copy(categoryId = categoryId)
                    
                    // 检查是否已存在相同年月的预算
                    val existingBudgets = budgetDao.getBudgetsByMonthSync(
                        context.userId, 
                        budget.year, 
                        budget.month
                    )
                    
                    val existing = existingBudgets.find { it.categoryId == categoryId }
                    
                    if (existing != null) {
                        when (context.config.conflictStrategy) {
                            ConflictStrategy.SKIP -> {
                                skipped++
                                return@forEach
                            }
                            ConflictStrategy.OVERWRITE -> {
                                budget = budget.copy(id = existing.id)
                                budgetDao.updateBudget(budget)
                                imported++
                            }
                            ConflictStrategy.MERGE -> {
                                val merged = existing.copy(
                                    budgetAmountCents = budget.budgetAmountCents,
                                    updatedAt = System.currentTimeMillis()
                                )
                                budgetDao.updateBudget(merged)
                                imported++
                            }
                            ConflictStrategy.RENAME -> {
                                // 预算不适用重命名策略，跳过
                                skipped++
                                return@forEach
                            }
                        }
                    } else {
                        batch.add(budget)
                    }
                }
                is ConvertResult.Error -> {
                    failed++
                    errors.add(convertResult.error)
                }
            }
            
            // 批量插入
            if (batch.size >= context.config.batchSize) {
                budgetDao.insertBudgets(batch)
                imported += batch.size
                batch.clear()
            }
        }
        
        // 插入剩余数据
        if (batch.isNotEmpty()) {
            budgetDao.insertBudgets(batch)
            imported += batch.size
        }
        
        return ImportBatchResult(imported, failed, skipped, errors)
    }
    
    private suspend fun importRecurringTransactions(
        recurringTransactions: List<DataLine>, 
        context: ImportContext
    ): ImportBatchResult {
        // TODO: 实现定期交易导入
        return ImportBatchResult(0, 0, 0, emptyList())
    }
    
    private suspend fun importSavingsGoals(
        savingsGoals: List<DataLine>, 
        context: ImportContext
    ): ImportBatchResult {
        var imported = 0
        var failed = 0
        var skipped = 0
        val errors = mutableListOf<ImportError>()
        val batch = mutableListOf<SavingsGoalEntity>()
        
        savingsGoals.forEach { dataLine ->
            when (val convertResult = savingsGoalConverter.convert(dataLine, context.userId)) {
                is ConvertResult.Success -> {
                    val savingsGoal = convertResult.data
                    
                    // 检查是否已存在同名储蓄目标
                    val existingSavingsGoals = savingsGoalDao.getAllSavingsGoalsSync()
                    val existing = existingSavingsGoals.find { it.name == savingsGoal.name }
                    
                    if (existing != null) {
                        when (context.config.conflictStrategy) {
                            ConflictStrategy.SKIP -> {
                                skipped++
                                return@forEach
                            }
                            ConflictStrategy.OVERWRITE -> {
                                val updated = savingsGoal.copy(id = existing.id)
                                savingsGoalDao.updateSavingsGoal(updated)
                                imported++
                            }
                            ConflictStrategy.MERGE -> {
                                val merged = existing.copy(
                                    targetAmount = savingsGoal.targetAmount,
                                    currentAmount = existing.currentAmount + savingsGoal.currentAmount,
                                    targetDate = savingsGoal.targetDate,
                                    updatedAt = LocalDateTime.now()
                                )
                                savingsGoalDao.updateSavingsGoal(merged)
                                imported++
                            }
                            ConflictStrategy.RENAME -> {
                                val newName = generateUniqueSavingsGoalName(
                                    savingsGoal.name, 
                                    existingSavingsGoals.map { it.name }
                                )
                                val renamed = savingsGoal.copy(name = newName)
                                batch.add(renamed)
                            }
                        }
                    } else {
                        batch.add(savingsGoal)
                    }
                }
                is ConvertResult.Error -> {
                    failed++
                    errors.add(convertResult.error)
                }
            }
            
            // 批量插入
            if (batch.size >= context.config.batchSize) {
                savingsGoalDao.insertAll(batch)
                imported += batch.size
                batch.clear()
            }
        }
        
        // 插入剩余数据
        if (batch.isNotEmpty()) {
            savingsGoalDao.insertAll(batch)
            imported += batch.size
        }
        
        return ImportBatchResult(imported, failed, skipped, errors)
    }
    
    private fun generateUniqueSavingsGoalName(baseName: String, existingNames: List<String>): String {
        var counter = 1
        var newName = "$baseName (导入)"
        
        while (existingNames.contains(newName)) {
            counter++
            newName = "$baseName (导入$counter)"
        }
        
        return newName
    }
    
    private suspend fun importCreditBills(
        creditBills: List<DataLine>, 
        context: ImportContext
    ): ImportBatchResult {
        // TODO: 实现信用卡账单导入
        return ImportBatchResult(0, 0, 0, emptyList())
    }
}

/**
 * 批量导入结果
 */
data class ImportBatchResult(
    val imported: Int,
    val failed: Int,
    val skipped: Int,
    val errors: List<ImportError>
)