package com.ccxiaoji.app.data.excel

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import com.ccxiaoji.app.data.importer.ImportExportError
import com.ccxiaoji.app.domain.model.ModuleType
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.habit.data.local.entity.HabitEntity
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionEntity
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.todo.data.local.entity.TaskEntity
import com.ccxiaoji.feature.todo.api.TodoApi
import com.ccxiaoji.feature.habit.api.HabitApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
// Excel导入独立化：移除对JSON导入框架的依赖

// Excel 导入特定的数据模块
enum class ExcelDataModule {
    ACCOUNTS,
    CATEGORIES, 
    TRANSACTIONS,
    TASKS,
    HABITS
}

// Excel 导入结果
data class ExcelImportResult(
    val success: Boolean,
    val totalItems: Int,
    val importedItems: Int,
    val skippedItems: Int,
    val errors: List<ExcelImportError>,
    val moduleResults: Map<ExcelDataModule, ExcelModuleImportResult>
)

// Excel 模块导入结果
data class ExcelModuleImportResult(
    val module: ExcelDataModule,
    val success: Boolean,
    val totalItems: Int,
    val importedItems: Int,
    val skippedItems: Int,
    val errors: List<String>
) {
    companion object {
        fun success(
            module: ExcelDataModule,
            totalItems: Int,
            importedItems: Int,
            skippedItems: Int
        ) = ExcelModuleImportResult(
            module = module,
            success = true,
            totalItems = totalItems,
            importedItems = importedItems,
            skippedItems = skippedItems,
            errors = emptyList()
        )
        
        fun error(module: ExcelDataModule, message: String) = ExcelModuleImportResult(
            module = module,
            success = false,
            totalItems = 0,
            importedItems = 0,
            skippedItems = 0,
            errors = listOf(message)
        )
    }
}

// Excel 导入特定的进度状态
sealed class ExcelImportProgress {
    object Idle : ExcelImportProgress()
    data class Analyzing(val fileStructure: ExcelFileStructure) : ExcelImportProgress()
    data class Importing(val current: Int, val total: Int, val module: String) : ExcelImportProgress()
    data class ModuleCompleted(val module: ExcelDataModule, val result: ExcelModuleImportResult) : ExcelImportProgress()
    data class Completed(val result: ExcelImportResult) : ExcelImportProgress()
    data class Error(val error: ImportExportError) : ExcelImportProgress()
}

// Excel 导入特定的错误扩展
data class ExcelImportError(
    val message: String,
    val code: String,
    val row: Int? = null,
    val column: String? = null
) {
    // Excel专用错误，不需要转换为标准错误
}

// Excel 中的交易类型
enum class ExcelTransactionType {
    INCOME,    // 收入
    EXPENSE,   // 支出
    TRANSFER   // 转账
}

data class ExcelImportOptions(
    val selectedModules: Set<ExcelDataModule> = setOf(
        ExcelDataModule.ACCOUNTS,
        ExcelDataModule.CATEGORIES,
        ExcelDataModule.TRANSACTIONS,
        ExcelDataModule.TASKS,
        ExcelDataModule.HABITS
    ),
    val mergeStrategy: MergeStrategy = MergeStrategy.SKIP_DUPLICATES,
    val ignoreBalanceErrors: Boolean = false,
    val selectedSheets: Set<SheetInfo> = emptySet(),
    val columnMappings: Map<String, ColumnMapping> = emptyMap()
)

enum class MergeStrategy {
    SKIP_DUPLICATES,
    REPLACE_EXISTING,
    MERGE_DATA
}

// ColumnMapping已在ColumnMappingDetector.kt中定义

@Singleton
class ExcelImportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val excelReader: ExcelReader,
    private val ledgerApi: LedgerApi,
    private val todoApi: TodoApi,
    private val habitApi: HabitApi,
    private val balanceValidator: BalanceValidator,
    private val batchImportProcessor: BatchImportProcessor,
    private val simpleXlsxReader: SimpleXlsxReader // 添加降级方案
) {
    
    private val _importProgress = MutableStateFlow<ExcelImportProgress>(ExcelImportProgress.Idle)
    val importProgress: StateFlow<ExcelImportProgress> = _importProgress
    
    suspend fun analyzeExcelFile(uri: Uri): Result<ExcelFileStructure> {
        // 首先尝试使用POI
        val poiResult = excelReader.analyzeExcelFile(uri)
        
        return if (poiResult.isSuccess) {
            poiResult.also { result ->
                result.onSuccess { structure ->
                    _importProgress.value = ExcelImportProgress.Analyzing(structure)
                }
            }
        } else {
            // POI失败，尝试使用简化读取器
            val error = poiResult.exceptionOrNull()
            if (simpleXlsxReader.canHandle(uri)) {
                try {
                    val simpleResult = simpleXlsxReader.analyzeSimpleStructure(uri)
                    simpleResult.also { result ->
                        result.onSuccess { structure ->
                            _importProgress.value = ExcelImportProgress.Analyzing(structure)
                        }
                    }
                } catch (e: Exception) {
                    // 两种方式都失败了，返回原始POI错误
                    Result.failure(error ?: e)
                }
            } else {
                poiResult
            }
        }
    }
    
    suspend fun importFromExcel(
        uri: Uri,
        options: ExcelImportOptions,
        onProgress: (ExcelImportProgress) -> Unit = {}
    ): ExcelImportResult = withContext(Dispatchers.IO) {
        try {
            val moduleResults = mutableMapOf<ExcelDataModule, ExcelModuleImportResult>()
            var totalItems = 0
            var importedItems = 0
            var skippedItems = 0
            val allErrors = mutableListOf<String>()
            
            // 导入记账数据
            if (ExcelDataModule.TRANSACTIONS in options.selectedModules || 
                ExcelDataModule.ACCOUNTS in options.selectedModules || 
                ExcelDataModule.CATEGORIES in options.selectedModules) {
                val result = importLedgerData(uri, options)
                // 记账模块包含多个子模块
                if (ExcelDataModule.TRANSACTIONS in options.selectedModules) {
                    moduleResults[ExcelDataModule.TRANSACTIONS] = result
                }
                if (ExcelDataModule.ACCOUNTS in options.selectedModules) {
                    moduleResults[ExcelDataModule.ACCOUNTS] = result
                }
                if (ExcelDataModule.CATEGORIES in options.selectedModules) {
                    moduleResults[ExcelDataModule.CATEGORIES] = result
                }
                totalItems += result.totalItems
                importedItems += result.importedItems
                skippedItems += result.skippedItems
                allErrors.addAll(result.errors)
                
                onProgress(ExcelImportProgress.ModuleCompleted(ExcelDataModule.TRANSACTIONS, result))
            }
            
            // 导入待办数据
            if (ExcelDataModule.TASKS in options.selectedModules) {
                val result = importTodoData(uri, options)
                moduleResults[ExcelDataModule.TASKS] = result
                totalItems += result.totalItems
                importedItems += result.importedItems
                skippedItems += result.skippedItems
                allErrors.addAll(result.errors)
                
                onProgress(ExcelImportProgress.ModuleCompleted(ExcelDataModule.TASKS, result))
            }
            
            // 导入习惯数据
            if (ExcelDataModule.HABITS in options.selectedModules) {
                val result = importHabitData(uri, options)
                moduleResults[ExcelDataModule.HABITS] = result
                totalItems += result.totalItems
                importedItems += result.importedItems
                skippedItems += result.skippedItems
                allErrors.addAll(result.errors)
                
                onProgress(ExcelImportProgress.ModuleCompleted(ExcelDataModule.HABITS, result))
            }
            
            val excelResult = ExcelImportResult(
                success = moduleResults.all { it.value.success },
                totalItems = totalItems,
                importedItems = importedItems,
                skippedItems = skippedItems,
                errors = allErrors.map { ExcelImportError(it, "IMPORT_ERROR") },
                moduleResults = moduleResults
            )
            
            _importProgress.value = ExcelImportProgress.Completed(excelResult)
            onProgress(ExcelImportProgress.Completed(excelResult))
            
            // 直接返回Excel专用结果
            excelResult
        } catch (e: Exception) {
            val error = ImportExportError.Unknown
            _importProgress.value = ExcelImportProgress.Error(error)
            onProgress(ExcelImportProgress.Error(error))
            
            // 直接返回Excel专用错误结果
            ExcelImportResult(
                success = false,
                totalItems = 0,
                importedItems = 0,
                skippedItems = 0,
                errors = listOf(ExcelImportError(e.message ?: "导入失败", "GENERAL_ERROR")),
                moduleResults = emptyMap()
            )
        }
    }
    
    private suspend fun importLedgerData(
        uri: Uri,
        options: ExcelImportOptions
    ): ExcelModuleImportResult = withContext(Dispatchers.IO) {
        try {
            // 读取交易记录表
            val transactionSheet = excelReader.readSheet(uri, "交易记录")
                .getOrNull() ?: return@withContext ExcelModuleImportResult.error(ExcelDataModule.TRANSACTIONS, "找不到交易记录表")
            
            // 读取账户信息表
            val accountSheet = excelReader.readSheet(uri, "账户信息").getOrNull()
            
            // 读取分类设置表
            val categorySheet = excelReader.readSheet(uri, "分类设置").getOrNull()
            
            var importedCount = 0
            var skippedCount = 0
            val errors = mutableListOf<ExcelImportError>()
            
            // 导入账户
            accountSheet?.let { sheet ->
                val accountResult = importAccounts(sheet, options)
                importedCount += accountResult.importedItems
                skippedCount += accountResult.skippedItems
                errors.addAll(accountResult.errors.map { ExcelImportError(it, "ACCOUNT_ERROR") })
            }
            
            // 导入分类
            categorySheet?.let { sheet ->
                val categoryResult = importCategories(sheet, options)
                importedCount += categoryResult.importedItems
                skippedCount += categoryResult.skippedItems
                errors.addAll(categoryResult.errors.map { ExcelImportError(it, "CATEGORY_ERROR") })
            }
            
            // 准备余额验证
            if (!options.ignoreBalanceErrors) {
                // 查找余额相关列
                val headers = transactionSheet.headers
                val balanceColumnIndex = headers.indexOfFirst { 
                    it.contains("账户余额") || it.contains("余额") || it.contains("Balance")
                }
                val totalAssetsColumnIndex = headers.indexOfFirst {
                    it.contains("总资产") || it.contains("Total Assets")
                }
                
                // 如果存在余额列，进行验证
                if (balanceColumnIndex >= 0 || totalAssetsColumnIndex >= 0) {
                    val validationResult = validateTransactionBalances(transactionSheet, options)
                    
                    if (!validationResult.isValid && !options.ignoreBalanceErrors) {
                        // 将余额错误添加到错误列表
                        validationResult.errors.forEach { balanceError ->
                            errors.add(ExcelImportError(
                                message = balanceError.message,
                                code = "BALANCE_ERROR",
                                row = balanceError.row,
                                column = "余额"
                            ))
                        }
                        
                        // 如果有严重余额错误，可以选择中止导入
                        if (validationResult.errors.any { it.type == BalanceErrorType.ACCOUNT_BALANCE_MISMATCH }) {
                            return@withContext ExcelModuleImportResult(
                                module = ExcelDataModule.TRANSACTIONS,
                                success = false,
                                totalItems = transactionSheet.rows.size,
                                importedItems = 0,
                                skippedItems = transactionSheet.rows.size,
                                errors = errors.map { it.message } + listOf("余额验证失败，请检查数据或选择忽略余额错误")
                            )
                        }
                    }
                }
            }
            
            // 导入交易记录
            val transactionResult = importTransactions(transactionSheet, options)
            importedCount += transactionResult.importedItems
            skippedCount += transactionResult.skippedItems
            errors.addAll(transactionResult.errors.map { ExcelImportError(it, "TRANSACTION_ERROR") })
            
            ExcelModuleImportResult(
                module = ExcelDataModule.TRANSACTIONS,
                success = errors.isEmpty(),
                totalItems = transactionSheet.rows.size + (accountSheet?.rows?.size ?: 0) + (categorySheet?.rows?.size ?: 0),
                importedItems = importedCount,
                skippedItems = skippedCount,
                errors = errors.map { it.message }
            )
        } catch (e: Exception) {
            ExcelModuleImportResult.error(ExcelDataModule.TRANSACTIONS, "导入记账数据失败: ${e.message}")
        }
    }
    
    private suspend fun importTransactions(
        sheetData: SheetData,
        options: ExcelImportOptions
    ): ExcelModuleImportResult {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        val errors = mutableListOf<ExcelImportError>()
        val transactionItems = mutableListOf<TransactionBatchItem>()
        var skippedCount = 0
        
        // 获取列索引（支持列映射）
        val headers = sheetData.headers
        val columnMappings = options.columnMappings
        
        // 通过列映射查找列索引，如果没有映射则使用默认列名
        fun findColumnIndex(fieldName: String, defaultColumns: List<String>): Int {
            // 先尝试通过映射查找
            columnMappings.entries.find { it.value.targetField == fieldName }?.let { entry ->
                return headers.indexOf(entry.key)
            }
            // 如果没有映射，尝试默认列名
            for (defaultName in defaultColumns) {
                val index = headers.indexOf(defaultName)
                if (index >= 0) return index
            }
            return -1
        }
        
        val dateIndex = findColumnIndex("date", listOf("日期", "Date", "交易日期"))
        val timeIndex = findColumnIndex("time", listOf("时间", "Time", "交易时间"))
        val typeIndex = findColumnIndex("type", listOf("类型", "Type", "交易类型"))
        val categoryIndex = findColumnIndex("category", listOf("分类", "Category", "类别"))
        val incomeIndex = findColumnIndex("income", listOf("收入(元)", "收入", "Income"))
        val expenseIndex = findColumnIndex("expense", listOf("支出(元)", "支出", "Expense"))
        val accountIndex = findColumnIndex("account", listOf("账户", "Account", "支付方式"))
        val noteIndex = findColumnIndex("note", listOf("备注", "Note", "说明"))
        val tagsIndex = findColumnIndex("tags", listOf("标签", "Tags", "Tag"))
        
        // 预先加载所有账户和分类，建立名称映射
        val accounts = ledgerApi.getAccounts().first()
        val accountNameToId = accounts.associateBy { it.name }
        
        val categories = ledgerApi.getCategories().first()
        val categoryNameToId = categories.associateBy { it.name }
        
        // 先收集所有有效的交易数据
        sheetData.rows.forEachIndexed { rowIndex, row ->
            try {
                // 跳过期初余额行
                if (row.getOrNull(typeIndex)?.formattedValue == "-" && 
                    row.getOrNull(categoryIndex)?.formattedValue == "期初余额") {
                    skippedCount++
                    return@forEachIndexed
                }
                
                // 获取值的辅助函数，支持数据转换
                fun getCellValue(index: Int, fieldName: String): String {
                    val rawValue = row.getOrNull(index)?.formattedValue ?: ""
                    // 查找是否有对应的转换函数
                    val mapping = columnMappings.values.find { it.targetField == fieldName }
                    return mapping?.transform?.invoke(rawValue) ?: rawValue
                }
                
                // 解析日期时间
                val dateStr = if (dateIndex >= 0) getCellValue(dateIndex, "date") else ""
                val timeStr = if (timeIndex >= 0) getCellValue(timeIndex, "time") else "00:00:00"
                val createdAt = try {
                    dateTimeFormat.parse("$dateStr $timeStr")?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
                
                // 解析交易类型和金额
                val typeStr = if (typeIndex >= 0) getCellValue(typeIndex, "type") else ""
                val incomeStr = if (incomeIndex >= 0) getCellValue(incomeIndex, "income") else ""
                val expenseStr = if (expenseIndex >= 0) getCellValue(expenseIndex, "expense") else ""
                
                val (type, amount) = when {
                    typeStr == "收入" || incomeStr.isNotEmpty() && incomeStr != "-" -> {
                        ExcelTransactionType.INCOME to parseAmount(incomeStr)
                    }
                    typeStr == "支出" || expenseStr.isNotEmpty() && expenseStr != "-" -> {
                        ExcelTransactionType.EXPENSE to parseAmount(expenseStr)
                    }
                    typeStr == "转账" -> {
                        ExcelTransactionType.TRANSFER to parseAmount(
                            if (incomeStr != "-") incomeStr else expenseStr
                        )
                    }
                    else -> {
                        errors.add(ExcelImportError(
                            "无法识别交易类型",
                            "INVALID_TYPE",
                            rowIndex + 2,
                            "类型"
                        ))
                        skippedCount++
                        return@forEachIndexed
                    }
                }
                
                // 查找账户ID
                val accountName = if (accountIndex >= 0) getCellValue(accountIndex, "account") else ""
                val account = accountNameToId[accountName]
                if (account == null) {
                    errors.add(ExcelImportError(
                        "找不到账户: $accountName",
                        "ACCOUNT_NOT_FOUND",
                        rowIndex + 2,
                        "账户"
                    ))
                    skippedCount++
                    return@forEachIndexed
                }
                
                // 查找分类ID
                val categoryName = if (categoryIndex >= 0) getCellValue(categoryIndex, "category") else ""
                val category = categoryNameToId[categoryName]
                if (category == null) {
                    errors.add(ExcelImportError(
                        "找不到分类: $categoryName",
                        "CATEGORY_NOT_FOUND",
                        rowIndex + 2,
                        "分类"
                    ))
                    skippedCount++
                    return@forEachIndexed
                }
                
                // 收集交易数据
                val note = if (noteIndex >= 0) getCellValue(noteIndex, "note").takeIf { it.isNotEmpty() } else null
                transactionItems.add(TransactionBatchItem(
                    amountCents = (amount * 100).toInt(),
                    categoryId = category.id,
                    accountId = account.id,
                    note = note,
                    createdAt = createdAt
                ))
            } catch (e: Exception) {
                errors.add(ExcelImportError(
                    "导入第${rowIndex + 2}行失败: ${e.message}",
                    "ROW_ERROR",
                    rowIndex + 2
                ))
                skippedCount++
            }
        }
        
        // 使用批量导入处理器批量插入交易
        val batchConfig = BatchImportProcessor.BatchImportConfig(
            batchSize = 100,
            enableTransaction = true,
            failOnError = false
        )
        
        val batchResult = batchImportProcessor.batchInsertTransactions(
            transactions = transactionItems,
            ledgerApi = ledgerApi,
            config = batchConfig
        )
        
        // 添加批量导入的错误信息
        batchResult.errors.forEach { batchError ->
            errors.add(ExcelImportError(
                message = batchError.message,
                code = "BATCH_ERROR",
                row = batchError.itemIndex + 2
            ))
        }
        
        return ExcelModuleImportResult(
            module = ExcelDataModule.TRANSACTIONS,
            success = errors.isEmpty(),
            totalItems = sheetData.rows.size,
            importedItems = batchResult.successCount,
            skippedItems = skippedCount + batchResult.failedCount,
            errors = errors.map { it.message }
        )
    }
    
    private suspend fun importAccounts(
        sheetData: SheetData,
        options: ExcelImportOptions
    ): ExcelModuleImportResult {
        val errors = mutableListOf<ExcelImportError>()
        val accountItems = mutableListOf<AccountBatchItem>()
        val updateAccountItems = mutableListOf<Pair<String, Long>>() // accountId to new balance
        var skippedCount = 0
        
        // 获取列索引
        val headers = sheetData.headers
        val nameIndex = headers.indexOf("账户名称")
        val typeIndex = headers.indexOf("账户类型")
        val balanceIndex = headers.indexOf("当前余额(元)")
        val currencyIndex = headers.indexOf("币种")
        val isDefaultIndex = headers.indexOf("是否默认")
        val noteIndex = headers.indexOf("备注")
        
        // 获取现有账户用于去重
        val existingAccounts = ledgerApi.getAccounts().first()
        val existingAccountNames = existingAccounts.map { it.name }.toSet()
        
        // 收集所有账户数据
        sheetData.rows.forEachIndexed { rowIndex, row ->
            try {
                val accountName = row.getOrNull(nameIndex)?.formattedValue ?: ""
                if (accountName.isEmpty()) {
                    errors.add(ExcelImportError(
                        "账户名称不能为空",
                        "EMPTY_NAME",
                        rowIndex + 2,
                        "账户名称"
                    ))
                    skippedCount++
                    return@forEachIndexed
                }
                
                // 检查是否已存在
                if (existingAccountNames.contains(accountName)) {
                    when (options.mergeStrategy) {
                        MergeStrategy.SKIP_DUPLICATES -> {
                            skippedCount++
                            return@forEachIndexed
                        }
                        MergeStrategy.REPLACE_EXISTING -> {
                            // 找到并更新现有账户
                            val existingAccount = existingAccounts.find { it.name == accountName }
                            if (existingAccount != null) {
                                val balanceStr = row.getOrNull(balanceIndex)?.formattedValue ?: "0"
                                val balance = parseAmount(balanceStr)
                                updateAccountItems.add(existingAccount.id to (balance * 100).toLong())
                                return@forEachIndexed
                            }
                        }
                        MergeStrategy.MERGE_DATA -> {
                            // 合并模式下跳过已存在的账户
                            skippedCount++
                            return@forEachIndexed
                        }
                    }
                }
                
                // 解析账户信息
                val accountType = row.getOrNull(typeIndex)?.formattedValue ?: "现金"
                val balanceStr = row.getOrNull(balanceIndex)?.formattedValue ?: "0"
                val balance = parseAmount(balanceStr)
                val currency = row.getOrNull(currencyIndex)?.formattedValue ?: "CNY"
                val isDefaultStr = row.getOrNull(isDefaultIndex)?.formattedValue ?: "否"
                val isDefault = isDefaultStr == "是" || isDefaultStr.toLowerCase() == "true"
                
                // 收集账户数据
                accountItems.add(AccountBatchItem(
                    name = accountName,
                    type = accountType,
                    balanceCents = (balance * 100).toLong(),
                    currency = currency,
                    isDefault = isDefault
                ))
            } catch (e: Exception) {
                errors.add(ExcelImportError(
                    "导入第${rowIndex + 2}行失败: ${e.message}",
                    "ROW_ERROR",
                    rowIndex + 2
                ))
                skippedCount++
            }
        }
        
        // 使用批量导入处理器批量插入账户
        val batchConfig = BatchImportProcessor.BatchImportConfig(
            batchSize = 50,
            enableTransaction = true,
            failOnError = false
        )
        
        var importedCount = 0
        
        // 批量创建新账户
        if (accountItems.isNotEmpty()) {
            val batchResult = batchImportProcessor.batchInsertAccounts(
                accounts = accountItems,
                ledgerApi = ledgerApi,
                config = batchConfig
            )
            
            importedCount += batchResult.successCount
            
            // 添加批量导入的错误信息
            batchResult.errors.forEach { batchError ->
                errors.add(ExcelImportError(
                    message = batchError.message,
                    code = "BATCH_ERROR",
                    row = batchError.itemIndex + 2
                ))
            }
        }
        
        // 批量更新现有账户余额
        updateAccountItems.forEach { (accountId, newBalance) ->
            try {
                ledgerApi.updateAccountBalance(accountId, newBalance)
                importedCount++
            } catch (e: Exception) {
                errors.add(ExcelImportError(
                    message = "更新账户余额失败: ${e.message}",
                    code = "UPDATE_ERROR"
                ))
            }
        }
        
        return ExcelModuleImportResult(
            module = ExcelDataModule.ACCOUNTS,
            success = errors.isEmpty(),
            totalItems = sheetData.rows.size,
            importedItems = importedCount,
            skippedItems = skippedCount,
            errors = errors.map { it.message }
        )
    }
    
    private suspend fun importCategories(
        sheetData: SheetData,
        options: ExcelImportOptions
    ): ExcelModuleImportResult {
        val errors = mutableListOf<ExcelImportError>()
        val categoryItems = mutableListOf<CategoryBatchItem>()
        val updateCategoryItems = mutableListOf<Category>()
        var skippedCount = 0
        
        // 获取列索引
        val headers = sheetData.headers
        val nameIndex = headers.indexOf("分类名称")
        val typeIndex = headers.indexOf("分类类型")
        val iconIndex = headers.indexOf("图标")
        val colorIndex = headers.indexOf("颜色")
        val sortIndex = headers.indexOf("排序")
        val parentIndex = headers.indexOf("父分类")
        
        // 获取现有分类用于去重和父分类查找
        val existingCategories = ledgerApi.getCategories().first()
        val existingCategoryNames = existingCategories.map { it.name }.toSet()
        val categoryNameToId = existingCategories.associateBy { it.name }.toMutableMap()
        
        // 收集所有分类数据
        sheetData.rows.forEachIndexed { rowIndex, row ->
            try {
                val categoryName = row.getOrNull(nameIndex)?.formattedValue ?: ""
                if (categoryName.isEmpty()) {
                    errors.add(ExcelImportError(
                        "分类名称不能为空",
                        "EMPTY_NAME",
                        rowIndex + 2,
                        "分类名称"
                    ))
                    skippedCount++
                    return@forEachIndexed
                }
                
                // 检查是否已存在
                if (existingCategoryNames.contains(categoryName)) {
                    when (options.mergeStrategy) {
                        MergeStrategy.SKIP_DUPLICATES -> {
                            skippedCount++
                            return@forEachIndexed
                        }
                        MergeStrategy.REPLACE_EXISTING -> {
                            // 找到并更新现有分类
                            val existingCategory = existingCategories.find { it.name == categoryName }
                            if (existingCategory != null) {
                                val typeStr = row.getOrNull(typeIndex)?.formattedValue ?: "支出"
                                val icon = row.getOrNull(iconIndex)?.formattedValue
                                val color = row.getOrNull(colorIndex)?.formattedValue ?: "#3A7AFE"
                                
                                updateCategoryItems.add(existingCategory.copy(
                                    type = if (typeStr == "收入") Category.Type.INCOME else Category.Type.EXPENSE,
                                    icon = icon ?: existingCategory.icon,
                                    color = color
                                ))
                                return@forEachIndexed
                            }
                        }
                        MergeStrategy.MERGE_DATA -> {
                            // 合并模式下跳过已存在的分类
                            skippedCount++
                            return@forEachIndexed
                        }
                    }
                }
                
                // 解析分类信息
                val typeStr = row.getOrNull(typeIndex)?.formattedValue ?: "支出"
                val typeString = if (typeStr == "收入") "INCOME" else "EXPENSE"
                val icon = row.getOrNull(iconIndex)?.formattedValue
                val color = row.getOrNull(colorIndex)?.formattedValue ?: "#3A7AFE"
                val parentName = row.getOrNull(parentIndex)?.formattedValue
                
                // 查找父分类ID
                val parentId = if (!parentName.isNullOrEmpty()) {
                    categoryNameToId[parentName]?.id
                } else null
                
                // 收集分类数据
                categoryItems.add(CategoryBatchItem(
                    name = categoryName,
                    type = typeString,
                    icon = icon ?: "📝",
                    color = color,
                    parentId = parentId
                ))
            } catch (e: Exception) {
                errors.add(ExcelImportError(
                    "导入第${rowIndex + 2}行失败: ${e.message}",
                    "ROW_ERROR",
                    rowIndex + 2
                ))
                skippedCount++
            }
        }
        
        // 使用批量导入处理器批量插入分类
        val batchConfig = BatchImportProcessor.BatchImportConfig(
            batchSize = 50,
            enableTransaction = true,
            failOnError = false
        )
        
        var importedCount = 0
        
        // 批量创建新分类
        if (categoryItems.isNotEmpty()) {
            val batchResult = batchImportProcessor.batchInsertCategories(
                categories = categoryItems,
                ledgerApi = ledgerApi,
                config = batchConfig
            )
            
            importedCount += batchResult.successCount
            
            // 添加批量导入的错误信息
            batchResult.errors.forEach { batchError ->
                errors.add(ExcelImportError(
                    message = batchError.message,
                    code = "BATCH_ERROR",
                    row = batchError.itemIndex + 2
                ))
            }
            
            // 更新映射表，以便处理第二批有父分类依赖的数据
            // 注意：这里需要重新加载分类以获取新创建的分类
            val updatedCategories = ledgerApi.getCategories().first()
            updatedCategories.forEach { category ->
                categoryNameToId[category.name] = category
            }
        }
        
        // 批量更新现有分类
        updateCategoryItems.forEach { category ->
            try {
                ledgerApi.updateCategory(category)
                importedCount++
            } catch (e: Exception) {
                errors.add(ExcelImportError(
                    message = "更新分类失败: ${e.message}",
                    code = "UPDATE_ERROR"
                ))
            }
        }
        
        return ExcelModuleImportResult(
            module = ExcelDataModule.CATEGORIES,
            success = errors.isEmpty(),
            totalItems = sheetData.rows.size,
            importedItems = importedCount,
            skippedItems = skippedCount,
            errors = errors.map { it.message }
        )
    }
    
    private suspend fun importTodoData(
        uri: Uri,
        options: ExcelImportOptions
    ): ExcelModuleImportResult = withContext(Dispatchers.IO) {
        try {
            // 读取待办任务表
            val todoSheet = excelReader.readSheet(uri, "待办任务")
                .getOrNull() ?: return@withContext ExcelModuleImportResult.error(ExcelDataModule.TASKS, "找不到待办任务表")
            
            val errors = mutableListOf<ExcelImportError>()
            val taskItems = mutableListOf<TaskBatchItem>()
            val completedTaskIds = mutableListOf<String>()
            var skippedCount = 0
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            
            // 获取列索引
            val headers = todoSheet.headers
            val titleIndex = headers.indexOf("任务标题")
            val descIndex = headers.indexOf("任务描述")
            val priorityIndex = headers.indexOf("优先级")
            val dueDateIndex = headers.indexOf("截止日期")
            val statusIndex = headers.indexOf("完成状态")
            val completedTimeIndex = headers.indexOf("完成时间")
            val tagsIndex = headers.indexOf("标签")
            val createdTimeIndex = headers.indexOf("创建时间")
            
            // 收集所有任务数据
            todoSheet.rows.forEachIndexed { rowIndex, row ->
                try {
                    val title = row.getOrNull(titleIndex)?.formattedValue ?: ""
                    if (title.isEmpty()) {
                        errors.add(ExcelImportError(
                            "任务标题不能为空",
                            "EMPTY_TITLE",
                            rowIndex + 2,
                            "任务标题"
                        ))
                        skippedCount++
                        return@forEachIndexed
                    }
                    
                    // 解析任务信息
                    val description = row.getOrNull(descIndex)?.formattedValue
                    val priorityStr = row.getOrNull(priorityIndex)?.formattedValue ?: "中"
                    val priority = when (priorityStr) {
                        "高" -> 3
                        "中" -> 2
                        "低" -> 1
                        else -> 2
                    }
                    
                    // 解析截止日期
                    val dueDateStr = row.getOrNull(dueDateIndex)?.formattedValue
                    val dueAt = if (!dueDateStr.isNullOrEmpty()) {
                        try {
                            val date = dateFormat.parse(dueDateStr)
                            date?.let { Instant.fromEpochMilliseconds(it.time) }
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                    
                    // 解析标签
                    val tagsStr = row.getOrNull(tagsIndex)?.formattedValue
                    val tags = tagsStr?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
                    
                    // 检查完成状态
                    val statusStr = row.getOrNull(statusIndex)?.formattedValue ?: "未完成"
                    val isCompleted = statusStr == "已完成" || statusStr.toLowerCase() == "true"
                    
                    // 收集任务数据
                    taskItems.add(TaskBatchItem(
                        title = title,
                        description = description,
                        priority = priority,
                        dueAt = dueAt,
                        tags = tags
                    ))
                    
                    // 记录需要标记为完成的任务索引
                    if (isCompleted) {
                        // 注意：这里暂时使用索引，后面需要获取真实的任务ID
                        completedTaskIds.add(rowIndex.toString())
                    }
                } catch (e: Exception) {
                    errors.add(ExcelImportError(
                        "导入第${rowIndex + 2}行失败: ${e.message}",
                        "ROW_ERROR",
                        rowIndex + 2
                    ))
                    skippedCount++
                }
            }
            
            // 使用批量导入处理器批量插入任务
            val batchConfig = BatchImportProcessor.BatchImportConfig(
                batchSize = 50,
                enableTransaction = true,
                failOnError = false
            )
            
            val batchResult = batchImportProcessor.batchInsertTasks(
                tasks = taskItems,
                todoApi = todoApi,
                config = batchConfig
            )
            
            // 添加批量导入的错误信息
            batchResult.errors.forEach { batchError ->
                errors.add(ExcelImportError(
                    message = batchError.message,
                    code = "BATCH_ERROR",
                    row = batchError.itemIndex + 2
                ))
            }
            
            // 注意：批量创建后无法直接获取任务ID来更新完成状态
            // 这是批量导入的一个限制，可能需要通过其他方式处理
            
            ExcelModuleImportResult(
                module = ExcelDataModule.TASKS,
                success = errors.isEmpty(),
                totalItems = todoSheet.rows.size,
                importedItems = batchResult.successCount,
                skippedItems = skippedCount + batchResult.failedCount,
                errors = errors.map { it.message }
            )
        } catch (e: Exception) {
            ExcelModuleImportResult.error(ExcelDataModule.TASKS, "导入待办任务失败: ${e.message}")
        }
    }
    
    private suspend fun importHabitData(
        uri: Uri,
        options: ExcelImportOptions
    ): ExcelModuleImportResult = withContext(Dispatchers.IO) {
        try {
            // 读取习惯记录表
            val habitSheet = excelReader.readSheet(uri, "习惯记录")
                .getOrNull() ?: return@withContext ExcelModuleImportResult.error(ExcelDataModule.HABITS, "找不到习惯记录表")
            
            val errors = mutableListOf<ExcelImportError>()
            val habitItems = mutableListOf<HabitBatchItem>()
            var skippedCount = 0
            
            // 获取列索引
            val headers = habitSheet.headers
            val nameIndex = headers.indexOf("习惯名称")
            val descIndex = headers.indexOf("描述")
            val periodIndex = headers.indexOf("周期")
            val targetIndex = headers.indexOf("目标频率")
            val colorIndex = headers.indexOf("颜色")
            val iconIndex = headers.indexOf("图标")
            val currentStreakIndex = headers.indexOf("当前连续天数")
            val totalCountIndex = headers.indexOf("总完成次数")
            val createdDateIndex = headers.indexOf("创建日期")
            
            // 收集所有习惯数据
            habitSheet.rows.forEachIndexed { rowIndex, row ->
                try {
                    val habitName = row.getOrNull(nameIndex)?.formattedValue ?: ""
                    if (habitName.isEmpty()) {
                        errors.add(ExcelImportError(
                            "习惯名称不能为空",
                            "EMPTY_NAME",
                            rowIndex + 2,
                            "习惯名称"
                        ))
                        skippedCount++
                        return@forEachIndexed
                    }
                    
                    // 解析习惯信息
                    val description = row.getOrNull(descIndex)?.formattedValue
                    val periodStr = row.getOrNull(periodIndex)?.formattedValue ?: "每日"
                    val period = when (periodStr) {
                        "每日" -> "daily"
                        "每周" -> "weekly"
                        "每月" -> "monthly"
                        else -> "daily"
                    }
                    
                    val targetStr = row.getOrNull(targetIndex)?.formattedValue ?: "1"
                    val target = try {
                        targetStr.toInt()
                    } catch (e: Exception) {
                        1
                    }
                    
                    val color = row.getOrNull(colorIndex)?.formattedValue ?: "#3A7AFE"
                    val icon = row.getOrNull(iconIndex)?.formattedValue
                    
                    // 收集习惯数据
                    habitItems.add(HabitBatchItem(
                        title = habitName,
                        description = description,
                        period = period,
                        target = target,
                        color = color,
                        icon = icon
                    ))
                    
                    // 注意：当前API不支持直接设置连续天数和总完成次数
                    // 这些数据需要通过历史打卡记录来重建
                } catch (e: Exception) {
                    errors.add(ExcelImportError(
                        "导入第${rowIndex + 2}行失败: ${e.message}",
                        "ROW_ERROR",
                        rowIndex + 2
                    ))
                    skippedCount++
                }
            }
            
            // 使用批量导入处理器批量插入习惯
            val batchConfig = BatchImportProcessor.BatchImportConfig(
                batchSize = 50,
                enableTransaction = true,
                failOnError = false
            )
            
            val batchResult = batchImportProcessor.batchInsertHabits(
                habits = habitItems,
                habitApi = habitApi,
                config = batchConfig
            )
            
            // 添加批量导入的错误信息
            batchResult.errors.forEach { batchError ->
                errors.add(ExcelImportError(
                    message = batchError.message,
                    code = "BATCH_ERROR",
                    row = batchError.itemIndex + 2
                ))
            }
            
            ExcelModuleImportResult(
                module = ExcelDataModule.HABITS,
                success = errors.isEmpty(),
                totalItems = habitSheet.rows.size,
                importedItems = batchResult.successCount,
                skippedItems = skippedCount + batchResult.failedCount,
                errors = errors.map { it.message }
            )
        } catch (e: Exception) {
            ExcelModuleImportResult.error(ExcelDataModule.HABITS, "导入习惯记录失败: ${e.message}")
        }
    }
    
    private fun parseAmount(amountStr: String): Double {
        return try {
            amountStr
                .replace(",", "")
                .replace("，", "")
                .replace("¥", "")
                .replace("￥", "")
                .replace(" ", "")
                .toDouble()
        } catch (e: Exception) {
            0.0
        }
    }
    
    fun resetProgress() {
        _importProgress.value = ExcelImportProgress.Idle
    }
    
    private suspend fun validateTransactionBalances(
        sheetData: SheetData,
        options: ExcelImportOptions
    ): BalanceValidationResult {
        val headers = sheetData.headers
        val columnMappings = options.columnMappings
        
        // 查找列索引
        fun findColumnIndex(fieldName: String, defaultColumns: List<String>): Int {
            columnMappings.entries.find { it.value.targetField == fieldName }?.let { entry ->
                return headers.indexOf(entry.key)
            }
            for (defaultName in defaultColumns) {
                val index = headers.indexOf(defaultName)
                if (index >= 0) return index
            }
            return -1
        }
        
        val dateIndex = findColumnIndex("date", listOf("日期", "Date", "交易日期"))
        val typeIndex = findColumnIndex("type", listOf("类型", "Type", "交易类型"))
        val incomeIndex = findColumnIndex("income", listOf("收入(元)", "收入", "Income"))
        val expenseIndex = findColumnIndex("expense", listOf("支出(元)", "支出", "Expense"))
        val accountIndex = findColumnIndex("account", listOf("账户", "Account", "支付方式"))
        val balanceIndex = headers.indexOfFirst { 
            it.contains("账户余额") || it.contains("Balance") 
        }
        val totalAssetsIndex = headers.indexOfFirst {
            it.contains("总资产") || it.contains("Total Assets")
        }
        
        // 准备验证数据
        val transactions = mutableListOf<TransactionWithBalance>()
        val accountBalances = mutableMapOf<String, AccountBalance>()
        
        // 获取现有账户信息
        val accounts = ledgerApi.getAccounts().first()
        accounts.forEach { account ->
            accountBalances[account.name] = AccountBalance(
                accountId = account.id,
                accountName = account.name,
                initialBalance = account.balanceCents / 100.0,
                currentBalance = account.balanceCents / 100.0
            )
        }
        
        // 解析交易数据
        sheetData.rows.forEachIndexed { rowIndex, row ->
            try {
                // 跳过期初余额行
                if (row.getOrNull(typeIndex)?.formattedValue == "-") {
                    return@forEachIndexed
                }
                
                // 解析交易信息
                val typeStr = row.getOrNull(typeIndex)?.formattedValue ?: ""
                val incomeStr = row.getOrNull(incomeIndex)?.formattedValue ?: ""
                val expenseStr = row.getOrNull(expenseIndex)?.formattedValue ?: ""
                val accountName = row.getOrNull(accountIndex)?.formattedValue ?: ""
                
                val type = when {
                    typeStr == "收入" || incomeStr.isNotEmpty() && incomeStr != "-" -> ExcelTransactionType.INCOME
                    typeStr == "支出" || expenseStr.isNotEmpty() && expenseStr != "-" -> ExcelTransactionType.EXPENSE
                    typeStr == "转账" -> ExcelTransactionType.TRANSFER
                    else -> null
                }
                
                if (type != null) {
                    val amount = when (type) {
                        ExcelTransactionType.INCOME -> parseAmount(incomeStr)
                        ExcelTransactionType.EXPENSE -> parseAmount(expenseStr)
                        ExcelTransactionType.TRANSFER -> parseAmount(
                            if (incomeStr != "-") incomeStr else expenseStr
                        )
                    }
                    
                    val accountBalance = if (balanceIndex >= 0) {
                        row.getOrNull(balanceIndex)?.let { cell ->
                            when (val value = cell.value) {
                                is Double -> value
                                is String -> value.toDoubleOrNull()
                                else -> null
                            }
                        }
                    } else null
                    
                    val totalAssets = if (totalAssetsIndex >= 0) {
                        row.getOrNull(totalAssetsIndex)?.let { cell ->
                            when (val value = cell.value) {
                                is Double -> value
                                is String -> value.toDoubleOrNull()
                                else -> null
                            }
                        }
                    } else null
                    
                    transactions.add(TransactionWithBalance(
                        transaction = ExcelTransaction(
                            id = "row_${rowIndex}",
                            type = type,
                            amount = amount,
                            accountId = accountName,
                            categoryId = "",
                            createdAt = System.currentTimeMillis()
                        ),
                        accountBalance = accountBalance,
                        totalAssets = totalAssets
                    ))
                }
            } catch (e: Exception) {
                // 忽略解析错误的行
            }
        }
        
        // 执行余额验证
        return balanceValidator.validateTransactionBalances(
            transactions = transactions,
            accounts = accountBalances,
            options = BalanceValidationOptions(
                strictMode = false,
                validateTotalAssets = true,
                checkNegativeBalance = true
            )
        )
    }
}