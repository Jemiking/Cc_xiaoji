package com.ccxiaoji.app.data.importer

import com.ccxiaoji.common.data.import.*
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.repository.BudgetRepository
import com.ccxiaoji.shared.user.data.repository.UserRepository
import com.ccxiaoji.common.base.BaseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 导入管理器 - App模块统一导入功能
 * 在app模块中直接注入各模块Repository，避免跨模块依赖问题
 */
@Singleton
class ImportManager @Inject constructor(
    // Todo模块
    private val todoRepository: TodoRepository,
    
    // Habit模块  
    private val habitRepository: HabitRepository,
    
    // Ledger模块
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    
    // User模块
    private val userRepository: UserRepository
) {
    
    /**
     * 执行完整的数据导入
     * @param importData 导入数据
     * @param skipExisting 是否跳过已存在的数据
     * @return 导入结果
     */
    suspend fun importData(
        importData: ImportData,
        skipExisting: Boolean = true
    ): ImportResult = withContext(Dispatchers.IO) {
        
        val results = mutableMapOf<DataModule, ModuleImportResult>()
        var totalImported = 0
        var totalSkipped = 0
        var totalErrors = 0
        
        try {
            // 导入账户数据
            importData.ledger?.accounts?.let { accounts ->
                val result = importAccounts(accounts, skipExisting)
                results[DataModule.ACCOUNTS] = result
                totalImported += result.importedItems
                totalSkipped += result.skippedItems
                totalErrors += result.errors.size
            }
            
            // 导入分类数据
            importData.ledger?.categories?.let { categories ->
                val result = importCategories(categories, skipExisting)
                results[DataModule.CATEGORIES] = result
                totalImported += result.importedItems
                totalSkipped += result.skippedItems
                totalErrors += result.errors.size
            }
            
            // 导入交易数据
            importData.ledger?.transactions?.let { transactions ->
                val result = importTransactions(transactions, skipExisting)
                results[DataModule.TRANSACTIONS] = result
                totalImported += result.importedItems
                totalSkipped += result.skippedItems
                totalErrors += result.errors.size
            }
            
            // 导入任务数据
            importData.tasks?.let { tasks ->
                val result = importTasks(tasks, skipExisting)
                results[DataModule.TASKS] = result
                totalImported += result.importedItems
                totalSkipped += result.skippedItems
                totalErrors += result.errors.size
            }
            
            // 导入习惯数据
            importData.habits?.habits?.let { habits ->
                val result = importHabits(habits, skipExisting)
                results[DataModule.HABITS] = result
                totalImported += result.importedItems
                totalSkipped += result.skippedItems
                totalErrors += result.errors.size
            }
            
            // 导入预算数据
            importData.ledger?.budgets?.let { budgets ->
                val result = importBudgets(budgets, skipExisting)
                results[DataModule.BUDGETS] = result
                totalImported += result.importedItems
                totalSkipped += result.skippedItems
                totalErrors += result.errors.size
            }
            
            return@withContext ImportResult(
                success = totalErrors == 0,
                totalItems = totalImported + totalSkipped,
                importedItems = totalImported,
                skippedItems = totalSkipped,
                errors = results.values.flatMap { it.errors.map { error -> ImportError(error, "") } },
                moduleResults = results
            )
            
        } catch (e: Exception) {
            return@withContext ImportResult(
                success = false,
                totalItems = 0,
                importedItems = 0,
                skippedItems = 0,
                errors = listOf(ImportError("导入失败: ${e.message}", "")),
                moduleResults = results
            )
        }
    }
    
    /**
     * 导入账户数据
     */
    private suspend fun importAccounts(
        accounts: List<AccountData>,
        skipExisting: Boolean
    ): ModuleImportResult {
        var importedCount = 0
        var skippedCount = 0
        val errors = mutableListOf<String>()
        
        accounts.forEach { accountData ->
            try {
                // 转换账户类型
                val accountType = when (accountData.accountType.lowercase()) {
                    "cash" -> com.ccxiaoji.feature.ledger.domain.model.AccountType.CASH
                    "credit_card" -> com.ccxiaoji.feature.ledger.domain.model.AccountType.CREDIT_CARD
                    "bank_card", "bank" -> com.ccxiaoji.feature.ledger.domain.model.AccountType.BANK
                    "alipay" -> com.ccxiaoji.feature.ledger.domain.model.AccountType.ALIPAY
                    "wechat" -> com.ccxiaoji.feature.ledger.domain.model.AccountType.WECHAT
                    "investment", "other" -> com.ccxiaoji.feature.ledger.domain.model.AccountType.OTHER
                    else -> com.ccxiaoji.feature.ledger.domain.model.AccountType.CASH
                }
                
                // 检查是否已存在
                if (skipExisting) {
                    val existingAccount = accountRepository.getAccountById(accountData.id)
                    if (existingAccount != null) {
                        skippedCount++
                        return@forEach
                    }
                }
                
                // 创建账户
                val accountId = accountRepository.createAccount(
                    name = accountData.name,
                    type = accountType,
                    initialBalanceCents = (accountData.balance * 100).toLong(),
                    creditLimitCents = null, // 导入数据中没有信用卡信息
                    billingDay = null,
                    paymentDueDay = null,
                    gracePeriodDays = null
                )
                
                if (accountId > 0) {
                    importedCount++
                    
                    // 如果是默认账户
                    if (accountData.isDefault) {
                        accountRepository.setDefaultAccount(accountData.id)
                    }
                } else {
                    errors.add("账户创建失败: ${accountData.name}")
                }
            } catch (e: Exception) {
                errors.add("账户导入失败: ${accountData.name} - ${e.message}")
            }
        }
        
        return ModuleImportResult(
            module = DataModule.ACCOUNTS,
            totalItems = accounts.size,
            importedItems = importedCount,
            skippedItems = skippedCount,
            errors = errors
        )
    }
    
    /**
     * 导入分类数据
     */
    private suspend fun importCategories(
        categories: List<CategoryData>,
        skipExisting: Boolean
    ): ModuleImportResult {
        var importedCount = 0
        var skippedCount = 0
        val errors = mutableListOf<String>()
        
        categories.forEach { categoryData ->
            try {
                // 检查是否已存在
                if (skipExisting) {
                    val existingCategory = categoryRepository.getCategoryById(categoryData.id)
                    if (existingCategory != null) {
                        skippedCount++
                        return@forEach
                    }
                }
                
                // 创建分类
                val categoryId = categoryRepository.createCategory(
                    name = categoryData.name,
                    type = categoryData.categoryType,
                    icon = categoryData.icon ?: "category",
                    color = categoryData.color ?: "#4CAF50",
                    parentId = categoryData.parentId
                )
                
                if (categoryId > 0) {
                    importedCount++
                } else {
                    errors.add("分类创建失败: ${categoryData.name}")
                }
            } catch (e: Exception) {
                errors.add("分类导入失败: ${categoryData.name} - ${e.message}")
            }
        }
        
        return ModuleImportResult(
            module = DataModule.CATEGORIES,
            totalItems = categories.size,
            importedItems = importedCount,
            skippedItems = skippedCount,
            errors = errors
        )
    }
    
    /**
     * 导入交易数据
     */
    private suspend fun importTransactions(
        transactions: List<TransactionData>,
        skipExisting: Boolean
    ): ModuleImportResult {
        var importedCount = 0
        var skippedCount = 0
        val errors = mutableListOf<String>()
        
        transactions.forEach { transactionData ->
            try {
                // 检查是否已存在
                if (skipExisting) {
                    val existingTransaction = transactionRepository.getTransactionById(transactionData.id)
                    if (existingTransaction != null) {
                        skippedCount++
                        return@forEach
                    }
                }
                
                // 添加交易
                val result = transactionRepository.addTransaction(
                    amountCents = (transactionData.amount * 100).toInt(),
                    categoryId = transactionData.categoryId,
                    note = transactionData.note,
                    accountId = transactionData.accountId
                )
                
                when (result) {
                    is BaseResult.Success -> {
                        importedCount++
                    }
                    is BaseResult.Error -> {
                        errors.add("交易创建失败: ${result.exception.message ?: "未知错误"}")
                    }
                }
            } catch (e: Exception) {
                errors.add("交易导入失败: ${transactionData.description ?: "未知交易"} - ${e.message}")
            }
        }
        
        return ModuleImportResult(
            module = DataModule.TRANSACTIONS,
            totalItems = transactions.size,
            importedItems = importedCount,
            skippedItems = skippedCount,
            errors = errors
        )
    }
    
    /**
     * 导入任务数据
     */
    private suspend fun importTasks(
        tasks: List<TaskData>,
        skipExisting: Boolean
    ): ModuleImportResult {
        var importedCount = 0
        var skippedCount = 0
        val errors = mutableListOf<String>()
        
        tasks.forEach { taskData ->
            try {
                // 检查是否已存在
                if (skipExisting) {
                    val existingTask = todoRepository.getTodoById(taskData.id)
                    when (existingTask) {
                        is BaseResult.Success -> {
                            if (existingTask.data != null) {
                                skippedCount++
                                return@forEach
                            }
                        }
                        is BaseResult.Error -> {
                            // 任务不存在，继续导入
                        }
                    }
                }
                
                // 转换时间戳为Instant
                val dueAt = taskData.dueDate?.let { 
                    Instant.fromEpochMilliseconds(it)
                }
                
                // 添加任务
                val result = todoRepository.addTodo(
                    title = taskData.title,
                    description = taskData.description,
                    dueAt = dueAt,
                    priority = taskData.priority
                )
                
                when (result) {
                    is BaseResult.Success -> {
                        importedCount++
                        
                        // 如果任务已完成，更新完成状态
                        if (taskData.isCompleted) {
                            result.data?.let { task ->
                                todoRepository.updateTodoCompletion(task.id, true)
                            }
                        }
                    }
                    is BaseResult.Error -> {
                        errors.add("任务创建失败: ${result.exception.message ?: "未知错误"}")
                    }
                }
            } catch (e: Exception) {
                errors.add("任务导入失败: ${taskData.title} - ${e.message}")
            }
        }
        
        return ModuleImportResult(
            module = DataModule.TASKS,
            totalItems = tasks.size,
            importedItems = importedCount,
            skippedItems = skippedCount,
            errors = errors
        )
    }
    
    /**
     * 导入习惯数据
     */
    private suspend fun importHabits(
        habits: List<HabitData>,
        skipExisting: Boolean
    ): ModuleImportResult {
        var importedCount = 0
        var skippedCount = 0
        val errors = mutableListOf<String>()
        
        habits.forEach { habitData ->
            try {
                // 简化检查：根据名称判断是否已存在
                if (skipExisting) {
                    // 由于没有根据ID查询习惯的方法，这里暂时跳过重复检查
                    // 实际使用时可以通过搜索功能来检查重复
                }
                
                // 创建习惯
                val result = habitRepository.createHabit(
                    title = habitData.name,
                    description = habitData.description,
                    period = habitData.frequency,
                    target = habitData.targetDays,
                    color = habitData.color ?: "#4CAF50",
                    icon = habitData.icon
                )
                
                when (result) {
                    is BaseResult.Success -> {
                        importedCount++
                    }
                    is BaseResult.Error -> {
                        errors.add("习惯创建失败: ${result.exception.message ?: "未知错误"}")
                    }
                }
            } catch (e: Exception) {
                errors.add("习惯导入失败: ${habitData.name} - ${e.message}")
            }
        }
        
        return ModuleImportResult(
            module = DataModule.HABITS,
            totalItems = habits.size,
            importedItems = importedCount,
            skippedItems = skippedCount,
            errors = errors
        )
    }
    
    /**
     * 导入预算数据
     */
    private suspend fun importBudgets(
        budgets: List<BudgetData>,
        skipExisting: Boolean
    ): ModuleImportResult {
        var importedCount = 0
        var skippedCount = 0
        val errors = mutableListOf<String>()
        
        budgets.forEach { budgetData ->
            try {
                // 检查是否已存在
                if (skipExisting) {
                    val existingBudget = budgetRepository.getBudgetById(budgetData.id)
                    if (existingBudget != null) {
                        skippedCount++
                        return@forEach
                    }
                }
                
                // 从时间戳提取年份和月份
                val startDate = Instant.fromEpochMilliseconds(budgetData.startDate)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                val year = startDate.year
                val month = startDate.monthNumber
                
                // 创建预算（简化处理，只取第一个分类ID）
                val categoryId = budgetData.categoryIds.firstOrNull()
                val budgetId = budgetRepository.createBudget(
                    year = year,
                    month = month,
                    categoryId = categoryId,
                    amountCents = (budgetData.amount * 100).toInt()
                )
                
                if (budgetId > 0) {
                    importedCount++
                } else {
                    errors.add("预算创建失败: ${budgetData.name}")
                }
            } catch (e: Exception) {
                errors.add("预算导入失败: ${budgetData.name} - ${e.message}")
            }
        }
        
        return ModuleImportResult(
            module = DataModule.BUDGETS,
            totalItems = budgets.size,
            importedItems = importedCount,
            skippedItems = skippedCount,
            errors = errors
        )
    }
}