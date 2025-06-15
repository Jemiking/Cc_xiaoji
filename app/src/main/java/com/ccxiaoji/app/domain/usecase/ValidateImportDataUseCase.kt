package com.ccxiaoji.app.domain.usecase

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javax.inject.Inject

// TODO: 编译验证 - 需要执行 ./gradlew :app:compileDebugKotlin
class ValidateImportDataUseCase @Inject constructor(
    private val gson: Gson
) {
    
    sealed class ValidationResult {
        data class Success(
            val dataType: DataType,
            val hasLedgerData: Boolean,
            val hasTodoData: Boolean,
            val hasHabitData: Boolean,
            val totalItemCount: Int,
            val ledgerItemDetails: LedgerDataDetails? = null,
            val todoItemCount: Int = 0,
            val habitItemCount: Int = 0
        ) : ValidationResult()
        
        data class Error(val message: String) : ValidationResult()
    }
    
    data class LedgerDataDetails(
        val transactionCount: Int = 0,
        val accountCount: Int = 0,
        val categoryCount: Int = 0,
        val budgetCount: Int = 0,
        val savingsGoalCount: Int = 0
    )
    
    enum class DataType {
        SINGLE_MODULE,  // 单个模块的数据
        MULTI_MODULE,   // 多个模块的数据
        FULL_BACKUP     // 完整备份
    }
    
    operator fun invoke(jsonContent: String): ValidationResult {
        return try {
            val jsonElement = JsonParser.parseString(jsonContent)
            
            if (!jsonElement.isJsonObject) {
                return ValidationResult.Error("导入的数据格式不正确，必须是有效的JSON对象")
            }
            
            val jsonObject = jsonElement.asJsonObject
            validateStructure(jsonObject)
            
        } catch (e: Exception) {
            ValidationResult.Error("数据解析失败: ${e.message}")
        }
    }
    
    private fun validateStructure(jsonObject: JsonObject): ValidationResult {
        // 检查版本信息
        if (!jsonObject.has("version")) {
            return ValidationResult.Error("缺少版本信息")
        }
        
        val version = jsonObject.get("version")?.asString ?: ""
        if (!isValidVersion(version)) {
            return ValidationResult.Error("不支持的数据版本: $version")
        }
        
        // 检查数据结构
        if (!jsonObject.has("data")) {
            return ValidationResult.Error("缺少数据内容")
        }
        
        val dataElement = jsonObject.get("data")
        if (!dataElement.isJsonObject) {
            return ValidationResult.Error("数据格式不正确")
        }
        
        val data = dataElement.asJsonObject
        
        // 统计各模块数据
        val hasLedgerData = data.has("ledger") && data.get("ledger").isJsonObject
        val hasTodoData = data.has("todo") && data.get("todo").isJsonObject  
        val hasHabitData = data.has("habit") && data.get("habit").isJsonObject
        
        if (!hasLedgerData && !hasTodoData && !hasHabitData) {
            return ValidationResult.Error("没有找到任何可导入的数据")
        }
        
        // 验证各模块数据
        var totalItemCount = 0
        var ledgerDetails: LedgerDataDetails? = null
        var todoItemCount = 0
        var habitItemCount = 0
        
        // 验证记账数据
        if (hasLedgerData) {
            val ledgerValidation = validateLedgerData(data.getAsJsonObject("ledger"))
            if (ledgerValidation is LedgerValidationResult.Error) {
                return ValidationResult.Error("记账数据验证失败: ${ledgerValidation.message}")
            }
            if (ledgerValidation is LedgerValidationResult.Success) {
                ledgerDetails = ledgerValidation.details
                totalItemCount += ledgerValidation.totalCount
            }
        }
        
        // 验证待办数据
        if (hasTodoData) {
            val todoValidation = validateTodoData(data.getAsJsonObject("todo"))
            if (todoValidation is ModuleValidationResult.Error) {
                return ValidationResult.Error("待办数据验证失败: ${todoValidation.message}")
            }
            if (todoValidation is ModuleValidationResult.Success) {
                todoItemCount = todoValidation.itemCount
                totalItemCount += todoValidation.itemCount
            }
        }
        
        // 验证习惯数据
        if (hasHabitData) {
            val habitValidation = validateHabitData(data.getAsJsonObject("habit"))
            if (habitValidation is ModuleValidationResult.Error) {
                return ValidationResult.Error("习惯数据验证失败: ${habitValidation.message}")
            }
            if (habitValidation is ModuleValidationResult.Success) {
                habitItemCount = habitValidation.itemCount
                totalItemCount += habitValidation.itemCount
            }
        }
        
        // 确定数据类型
        val moduleCount = listOf(hasLedgerData, hasTodoData, hasHabitData).count { it }
        val dataType = when {
            moduleCount == 3 -> DataType.FULL_BACKUP
            moduleCount > 1 -> DataType.MULTI_MODULE
            else -> DataType.SINGLE_MODULE
        }
        
        return ValidationResult.Success(
            dataType = dataType,
            hasLedgerData = hasLedgerData,
            hasTodoData = hasTodoData,
            hasHabitData = hasHabitData,
            totalItemCount = totalItemCount,
            ledgerItemDetails = ledgerDetails,
            todoItemCount = todoItemCount,
            habitItemCount = habitItemCount
        )
    }
    
    private fun isValidVersion(version: String): Boolean {
        // 支持的版本格式: 1.0, 1.0.0, 2.0等
        val versionPattern = Regex("^\\d+(\\.\\d+){0,2}$")
        if (!versionPattern.matches(version)) {
            return false
        }
        
        // 检查主版本号
        val majorVersion = version.split(".").firstOrNull()?.toIntOrNull() ?: return false
        
        // 当前只支持版本1.x
        return majorVersion == 1
    }
    
    // 记账数据验证
    private sealed class LedgerValidationResult {
        data class Success(val details: LedgerDataDetails, val totalCount: Int) : LedgerValidationResult()
        data class Error(val message: String) : LedgerValidationResult()
    }
    
    private fun validateLedgerData(ledgerData: JsonObject): LedgerValidationResult {
        val details = LedgerDataDetails()
        var totalCount = 0
        
        // 验证交易记录
        if (ledgerData.has("transactions")) {
            val transactionsElement = ledgerData.get("transactions")
            if (!transactionsElement.isJsonArray) {
                return LedgerValidationResult.Error("交易记录格式不正确")
            }
            val transactions = transactionsElement.asJsonArray
            
            transactions.forEach { element ->
                if (!element.isJsonObject) {
                    return LedgerValidationResult.Error("交易记录项格式不正确")
                }
                val transaction = element.asJsonObject
                
                // 验证必要字段
                if (!transaction.has("amountCents") || !transaction.has("categoryId")) {
                    return LedgerValidationResult.Error("交易记录缺少必要字段")
                }
            }
            
            totalCount += transactions.size()
        }
        
        // 验证账户
        if (ledgerData.has("accounts")) {
            val accountsElement = ledgerData.get("accounts")
            if (!accountsElement.isJsonArray) {
                return LedgerValidationResult.Error("账户列表格式不正确")
            }
            val accounts = accountsElement.asJsonArray
            
            accounts.forEach { element ->
                if (!element.isJsonObject) {
                    return LedgerValidationResult.Error("账户项格式不正确")
                }
                val account = element.asJsonObject
                
                // 验证必要字段
                if (!account.has("name") || !account.has("type")) {
                    return LedgerValidationResult.Error("账户缺少必要字段")
                }
            }
            
            totalCount += accounts.size()
        }
        
        // 验证分类
        if (ledgerData.has("categories")) {
            val categoriesElement = ledgerData.get("categories")
            if (!categoriesElement.isJsonArray) {
                return LedgerValidationResult.Error("分类列表格式不正确")
            }
            val categories = categoriesElement.asJsonArray
            
            categories.forEach { element ->
                if (!element.isJsonObject) {
                    return LedgerValidationResult.Error("分类项格式不正确")
                }
                val category = element.asJsonObject
                
                // 验证必要字段
                if (!category.has("name") || !category.has("type")) {
                    return LedgerValidationResult.Error("分类缺少必要字段")
                }
            }
            
            totalCount += categories.size()
        }
        
        // 验证预算
        if (ledgerData.has("budgets")) {
            val budgetsElement = ledgerData.get("budgets")
            if (!budgetsElement.isJsonArray) {
                return LedgerValidationResult.Error("预算列表格式不正确")
            }
            totalCount += budgetsElement.asJsonArray.size()
        }
        
        // 验证存钱目标
        if (ledgerData.has("savingsGoals")) {
            val goalsElement = ledgerData.get("savingsGoals")
            if (!goalsElement.isJsonArray) {
                return LedgerValidationResult.Error("存钱目标列表格式不正确")
            }
            totalCount += goalsElement.asJsonArray.size()
        }
        
        return LedgerValidationResult.Success(
            details = LedgerDataDetails(
                transactionCount = ledgerData.get("transactions")?.asJsonArray?.size() ?: 0,
                accountCount = ledgerData.get("accounts")?.asJsonArray?.size() ?: 0,
                categoryCount = ledgerData.get("categories")?.asJsonArray?.size() ?: 0,
                budgetCount = ledgerData.get("budgets")?.asJsonArray?.size() ?: 0,
                savingsGoalCount = ledgerData.get("savingsGoals")?.asJsonArray?.size() ?: 0
            ),
            totalCount = totalCount
        )
    }
    
    // 通用模块验证结果
    private sealed class ModuleValidationResult {
        data class Success(val itemCount: Int) : ModuleValidationResult()
        data class Error(val message: String) : ModuleValidationResult()
    }
    
    // 待办数据验证
    private fun validateTodoData(todoData: JsonObject): ModuleValidationResult {
        if (!todoData.has("tasks")) {
            return ModuleValidationResult.Error("缺少任务列表")
        }
        
        val tasksElement = todoData.get("tasks")
        if (!tasksElement.isJsonArray) {
            return ModuleValidationResult.Error("任务列表格式不正确")
        }
        
        val tasks = tasksElement.asJsonArray
        
        tasks.forEach { element ->
            if (!element.isJsonObject) {
                return ModuleValidationResult.Error("任务项格式不正确")
            }
            val task = element.asJsonObject
            
            // 验证必要字段
            if (!task.has("title")) {
                return ModuleValidationResult.Error("任务缺少标题")
            }
        }
        
        return ModuleValidationResult.Success(tasks.size())
    }
    
    // 习惯数据验证
    private fun validateHabitData(habitData: JsonObject): ModuleValidationResult {
        if (!habitData.has("habits")) {
            return ModuleValidationResult.Error("缺少习惯列表")
        }
        
        val habitsElement = habitData.get("habits")
        if (!habitsElement.isJsonArray) {
            return ModuleValidationResult.Error("习惯列表格式不正确")
        }
        
        val habits = habitsElement.asJsonArray
        
        habits.forEach { element ->
            if (!element.isJsonObject) {
                return ModuleValidationResult.Error("习惯项格式不正确")
            }
            val habit = element.asJsonObject
            
            // 验证必要字段
            if (!habit.has("title")) {
                return ModuleValidationResult.Error("习惯缺少标题")
            }
            
            // 验证period字段
            if (habit.has("period")) {
                val period = habit.get("period").asString
                if (!isValidPeriod(period)) {
                    return ModuleValidationResult.Error("习惯周期类型不正确: $period")
                }
            }
        }
        
        return ModuleValidationResult.Success(habits.size())
    }
    
    private fun isValidPeriod(period: String): Boolean {
        return period in listOf("daily", "weekly", "monthly")
    }
}