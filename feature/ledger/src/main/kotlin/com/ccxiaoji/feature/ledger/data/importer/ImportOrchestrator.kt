package com.ccxiaoji.feature.ledger.data.importer

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 导入协调器
 * 负责协调各种数据类型的批量导入操作
 */
class ImportOrchestrator @Inject constructor(
    private val userRepository: UserRepository,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val accountConverter: AccountConverter,
    private val categoryConverter: CategoryConverter,
    private val transactionConverter: TransactionConverter,
    private val budgetConverter: BudgetConverter,
    private val savingsGoalConverter: SavingsGoalConverter,
    private val dataValidator: DataValidator,
    private val conflictResolver: ConflictResolver
) {
    
    companion object {
        private const val TAG = "ImportOrchestrator"
    }
    
    /**
     * 处理批量数据
     */
    suspend fun processBatchData(
        groupedData: Map<String, List<DataLine>>,
        config: ImportConfig
    ): ImportResult = withContext(Dispatchers.IO) {
        val currentUser = userRepository.getCurrentUser()
            ?: throw IllegalStateException("当前用户不存在")
        
        val context = ImportContext(
            userId = currentUser.id,
            config = config
        )
        
        var successCount = 0
        var failedCount = 0
        var skippedCount = 0
        val errors = mutableListOf<ImportError>()
        val summary = ImportSummary()
        
        // 按依赖顺序处理
        // 1. 账户（无依赖）
        groupedData["ACCOUNT"]?.let { accounts ->
            val result = processAccounts(accounts, context)
            successCount += result.successCount
            failedCount += result.failedCount
            skippedCount += result.skippedCount
            errors.addAll(result.errors)
        }
        
        // 2. 分类（可能有父分类依赖）
        groupedData["CATEGORY"]?.let { categories ->
            val result = processCategories(categories, context)
            successCount += result.successCount
            failedCount += result.failedCount
            skippedCount += result.skippedCount
            errors.addAll(result.errors)
        }
        
        // 3. 交易（依赖账户和分类）
        groupedData["TRANSACTION"]?.let { transactions ->
            val result = processTransactions(transactions, context)
            successCount += result.successCount
            failedCount += result.failedCount
            skippedCount += result.skippedCount
            errors.addAll(result.errors)
        }
        
        // 4. 预算（依赖分类）
        groupedData["BUDGET"]?.let { budgets ->
            val result = processBudgets(budgets, context)
            successCount += result.successCount
            failedCount += result.failedCount
            skippedCount += result.skippedCount
            errors.addAll(result.errors)
        }
        
        // 5. 储蓄目标（无依赖）
        groupedData["SAVINGS"]?.let { savingsGoals ->
            val result = processSavingsGoals(savingsGoals, context)
            successCount += result.successCount
            failedCount += result.failedCount
            skippedCount += result.skippedCount
            errors.addAll(result.errors)
        }
        
        ImportResult(
            success = failedCount == 0 || (config.allowPartialImport && successCount > 0),
            totalRows = groupedData.values.sumOf { it.size },
            successCount = successCount,
            failedCount = failedCount,
            skippedCount = skippedCount,
            errors = errors,
            summary = summary
        )
    }
    
    private suspend fun processAccounts(
        accounts: List<DataLine>,
        context: ImportContext
    ): ImportResult {
        var successCount = 0
        var failedCount = 0
        var skippedCount = 0
        val errors = mutableListOf<ImportError>()
        
        accounts.forEach { dataLine ->
            try {
                when (val convertResult = accountConverter.convert(dataLine, context.userId)) {
                    is ConvertResult.Success -> {
                        val account = convertResult.data
                        
                        // 验证并处理冲突
                        when (val validationResult = dataValidator.validateAccount(account)) {
                            is ValidationResult.Success -> {
                                when (val resolveResult = conflictResolver.resolveAccountConflict(
                                    account,
                                    context.config.conflictStrategy
                                )) {
                                    is ResolveResult.NoConflict -> {
                                        accountDao.insertAccount(resolveResult.data)
                                        context.accountMap[account.name] = resolveResult.data.id
                                        successCount++
                                    }
                                    is ResolveResult.Modified -> {
                                        accountDao.insertAccount(resolveResult.data)
                                        context.accountMap[account.name] = resolveResult.data.id
                                        successCount++
                                    }
                                    is ResolveResult.Merge -> {
                                        accountDao.updateAccount(resolveResult.data)
                                        context.accountMap[account.name] = resolveResult.data.id
                                        successCount++
                                    }
                                    is ResolveResult.Skip -> {
                                        skippedCount++
                                        Log.d(TAG, resolveResult.reason)
                                    }
                                }
                            }
                            is ValidationResult.Failed -> {
                                failedCount++
                                errors.add(ImportError.ValidationError(
                                    dataLine.line,
                                    "账户",
                                    validationResult.errors.joinToString("; ")
                                ))
                            }
                        }
                    }
                    is ConvertResult.Error -> {
                        failedCount++
                        errors.add(convertResult.error)
                    }
                }
            } catch (e: Exception) {
                failedCount++
                errors.add(ImportError.DatabaseError(
                    dataLine.line,
                    "处理账户失败: ${e.message}",
                    e
                ))
            }
        }
        
        return ImportResult(
            success = failedCount == 0,
            totalRows = accounts.size,
            successCount = successCount,
            failedCount = failedCount,
            skippedCount = skippedCount,
            errors = errors,
            summary = ImportSummary(accountsImported = successCount)
        )
    }
    
    private suspend fun processCategories(
        categories: List<DataLine>,
        context: ImportContext
    ): ImportResult {
        var successCount = 0
        var failedCount = 0
        var skippedCount = 0
        val errors = mutableListOf<ImportError>()
        
        categories.forEach { dataLine ->
            try {
                when (val convertResult = categoryConverter.convert(dataLine, context.userId)) {
                    is ConvertResult.Success -> {
                        val category = convertResult.data
                        
                        when (val validationResult = dataValidator.validateCategory(category)) {
                            is ValidationResult.Success -> {
                                when (val resolveResult = conflictResolver.resolveCategoryConflict(
                                    category,
                                    context.config.conflictStrategy
                                )) {
                                    is ResolveResult.NoConflict -> {
                                        categoryDao.insertCategory(resolveResult.data)
                                        context.categoryMap[category.name] = resolveResult.data.id
                                        successCount++
                                    }
                                    is ResolveResult.Modified -> {
                                        categoryDao.insertCategory(resolveResult.data)
                                        context.categoryMap[category.name] = resolveResult.data.id
                                        successCount++
                                    }
                                    is ResolveResult.Merge -> {
                                        context.categoryMap[category.name] = resolveResult.data.id
                                        successCount++
                                    }
                                    is ResolveResult.Skip -> {
                                        skippedCount++
                                    }
                                }
                            }
                            is ValidationResult.Failed -> {
                                failedCount++
                                errors.add(ImportError.ValidationError(
                                    dataLine.line,
                                    "分类",
                                    validationResult.errors.joinToString("; ")
                                ))
                            }
                        }
                    }
                    is ConvertResult.Error -> {
                        failedCount++
                        errors.add(convertResult.error)
                    }
                }
            } catch (e: Exception) {
                failedCount++
                errors.add(ImportError.DatabaseError(
                    dataLine.line,
                    "处理分类失败: ${e.message}",
                    e
                ))
            }
        }
        
        return ImportResult(
            success = failedCount == 0,
            totalRows = categories.size,
            successCount = successCount,
            failedCount = failedCount,
            skippedCount = skippedCount,
            errors = errors,
            summary = ImportSummary(categoriesImported = successCount)
        )
    }
    
    private suspend fun processTransactions(
        transactions: List<DataLine>,
        context: ImportContext
    ): ImportResult {
        var successCount = 0
        var failedCount = 0
        var skippedCount = 0
        val errors = mutableListOf<ImportError>()
        val batch = mutableListOf<TransactionEntity>()
        
        transactions.forEach { dataLine ->
            try {
                when (val convertResult = transactionConverter.convert(dataLine, context.userId)) {
                    is ConvertResult.Success -> {
                        var transaction = convertResult.data
                        
                        // 映射账户和分类ID
                        val accountId = context.accountMap[transaction.accountId]
                        val categoryId = context.categoryMap[transaction.categoryId]
                        
                        if (accountId == null || categoryId == null) {
                            failedCount++
                            if (accountId == null) {
                                errors.add(ImportError.DependencyError(
                                    dataLine.line,
                                    "账户: ${transaction.accountId}",
                                    "找不到对应的账户"
                                ))
                            }
                            if (categoryId == null) {
                                errors.add(ImportError.DependencyError(
                                    dataLine.line,
                                    "分类: ${transaction.categoryId}",
                                    "找不到对应的分类"
                                ))
                            }
                            return@forEach
                        }
                        
                        transaction = transaction.copy(
                            accountId = accountId,
                            categoryId = categoryId
                        )
                        
                        // 检测重复
                        if (context.config.conflictStrategy == ConflictStrategy.SKIP) {
                            if (conflictResolver.isTransactionDuplicate(transaction)) {
                                skippedCount++
                                return@forEach
                            }
                        }
                        
                        batch.add(transaction)
                        
                        // 批量插入
                        if (batch.size >= context.config.batchSize) {
                            transactionDao.insertTransactions(batch)
                            successCount += batch.size
                            batch.clear()
                        }
                    }
                    is ConvertResult.Error -> {
                        failedCount++
                        errors.add(convertResult.error)
                    }
                }
            } catch (e: Exception) {
                failedCount++
                errors.add(ImportError.DatabaseError(
                    dataLine.line,
                    "处理交易失败: ${e.message}",
                    e
                ))
            }
        }
        
        // 插入剩余数据
        if (batch.isNotEmpty()) {
            transactionDao.insertTransactions(batch)
            successCount += batch.size
        }
        
        return ImportResult(
            success = failedCount == 0,
            totalRows = transactions.size,
            successCount = successCount,
            failedCount = failedCount,
            skippedCount = skippedCount,
            errors = errors,
            summary = ImportSummary(transactionsImported = successCount)
        )
    }
    
    private suspend fun processBudgets(
        budgets: List<DataLine>,
        context: ImportContext
    ): ImportResult {
        var successCount = 0
        var failedCount = 0
        var skippedCount = 0
        val errors = mutableListOf<ImportError>()
        
        budgets.forEach { dataLine ->
            try {
                when (val convertResult = budgetConverter.convert(dataLine, context.userId)) {
                    is ConvertResult.Success -> {
                        var budget = convertResult.data
                        
                        // 映射分类ID
                        val categoryId = context.categoryMap[budget.categoryId]
                        if (categoryId == null) {
                            failedCount++
                            errors.add(ImportError.DependencyError(
                                dataLine.line,
                                "分类: ${budget.categoryId}",
                                "找不到对应的分类"
                            ))
                            return@forEach
                        }
                        
                        budget = budget.copy(categoryId = categoryId)
                        
                        when (val validationResult = dataValidator.validateBudget(budget)) {
                            is ValidationResult.Success -> {
                                // 检查是否已存在
                                val existingBudgets = budgetDao.getBudgetsByMonthSync(
                                    context.userId,
                                    budget.year,
                                    budget.month
                                )
                                
                                val existing = existingBudgets.find { it.categoryId == categoryId }
                                
                                if (existing != null) {
                                    when (context.config.conflictStrategy) {
                                        ConflictStrategy.SKIP -> skippedCount++
                                        ConflictStrategy.OVERWRITE -> {
                                            budgetDao.updateBudget(budget.copy(id = existing.id))
                                            successCount++
                                        }
                                        ConflictStrategy.MERGE -> {
                                            val merged = existing.copy(
                                                budgetAmountCents = budget.budgetAmountCents,
                                                updatedAt = System.currentTimeMillis()
                                            )
                                            budgetDao.updateBudget(merged)
                                            successCount++
                                        }
                                        ConflictStrategy.RENAME -> skippedCount++
                                    }
                                } else {
                                    budgetDao.insertBudget(budget)
                                    successCount++
                                }
                            }
                            is ValidationResult.Failed -> {
                                failedCount++
                                errors.add(ImportError.ValidationError(
                                    dataLine.line,
                                    "预算",
                                    validationResult.errors.joinToString("; ")
                                ))
                            }
                        }
                    }
                    is ConvertResult.Error -> {
                        failedCount++
                        errors.add(convertResult.error)
                    }
                }
            } catch (e: Exception) {
                failedCount++
                errors.add(ImportError.DatabaseError(
                    dataLine.line,
                    "处理预算失败: ${e.message}",
                    e
                ))
            }
        }
        
        return ImportResult(
            success = failedCount == 0,
            totalRows = budgets.size,
            successCount = successCount,
            failedCount = failedCount,
            skippedCount = skippedCount,
            errors = errors,
            summary = ImportSummary(budgetsImported = successCount)
        )
    }
    
    private suspend fun processSavingsGoals(
        savingsGoals: List<DataLine>,
        context: ImportContext
    ): ImportResult {
        var successCount = 0
        var failedCount = 0
        var skippedCount = 0
        val errors = mutableListOf<ImportError>()
        
        savingsGoals.forEach { dataLine ->
            try {
                when (val convertResult = savingsGoalConverter.convert(dataLine, context.userId)) {
                    is ConvertResult.Success -> {
                        val savingsGoal = convertResult.data
                        
                        when (val validationResult = dataValidator.validateSavingsGoal(savingsGoal)) {
                            is ValidationResult.Success -> {
                                // 检查是否已存在
                                val existingGoals = savingsGoalDao.getAllSavingsGoalsSync()
                                val existing = existingGoals.find { it.name == savingsGoal.name }
                                
                                if (existing != null) {
                                    when (context.config.conflictStrategy) {
                                        ConflictStrategy.SKIP -> skippedCount++
                                        ConflictStrategy.OVERWRITE -> {
                                            savingsGoalDao.updateSavingsGoal(
                                                savingsGoal.copy(id = existing.id)
                                            )
                                            successCount++
                                        }
                                        ConflictStrategy.MERGE -> {
                                            val merged = existing.copy(
                                                targetAmount = savingsGoal.targetAmount,
                                                currentAmount = existing.currentAmount + savingsGoal.currentAmount,
                                                targetDate = savingsGoal.targetDate,
                                                updatedAt = java.time.LocalDateTime.now()
                                            )
                                            savingsGoalDao.updateSavingsGoal(merged)
                                            successCount++
                                        }
                                        ConflictStrategy.RENAME -> {
                                            val newName = generateUniqueName(
                                                savingsGoal.name,
                                                existingGoals.map { it.name }
                                            )
                                            savingsGoalDao.insertSavingsGoal(
                                                savingsGoal.copy(name = newName)
                                            )
                                            successCount++
                                        }
                                    }
                                } else {
                                    savingsGoalDao.insertSavingsGoal(savingsGoal)
                                    successCount++
                                }
                            }
                            is ValidationResult.Failed -> {
                                failedCount++
                                errors.add(ImportError.ValidationError(
                                    dataLine.line,
                                    "储蓄目标",
                                    validationResult.errors.joinToString("; ")
                                ))
                            }
                        }
                    }
                    is ConvertResult.Error -> {
                        failedCount++
                        errors.add(convertResult.error)
                    }
                }
            } catch (e: Exception) {
                failedCount++
                errors.add(ImportError.DatabaseError(
                    dataLine.line,
                    "处理储蓄目标失败: ${e.message}",
                    e
                ))
            }
        }
        
        return ImportResult(
            success = failedCount == 0,
            totalRows = savingsGoals.size,
            successCount = successCount,
            failedCount = failedCount,
            skippedCount = skippedCount,
            errors = errors,
            summary = ImportSummary(savingsImported = successCount)
        )
    }
    
    private fun generateUniqueName(baseName: String, existingNames: List<String>): String {
        var counter = 1
        var newName = "$baseName (导入)"
        
        while (existingNames.contains(newName)) {
            counter++
            newName = "$baseName (导入$counter)"
        }
        
        return newName
    }
}

/**
 * 导入上下文
 */
data class ImportContext(
    val userId: String,
    val config: ImportConfig,
    val accountMap: MutableMap<String, String> = mutableMapOf(),
    val categoryMap: MutableMap<String, String> = mutableMapOf()
)

