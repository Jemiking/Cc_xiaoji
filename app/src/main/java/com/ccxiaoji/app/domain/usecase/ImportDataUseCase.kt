package com.ccxiaoji.app.domain.usecase

import com.ccxiaoji.feature.habit.api.HabitApi
import com.ccxiaoji.feature.habit.api.ImportHabitsResult
import com.ccxiaoji.feature.ledger.api.ImportLedgerResult
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.todo.api.ImportTasksResult
import com.ccxiaoji.feature.todo.api.TodoApi
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

// TODO: 编译验证 - 需要执行 ./gradlew :app:compileDebugKotlin
class ImportDataUseCase @Inject constructor(
    private val ledgerApi: LedgerApi,
    private val todoApi: TodoApi,
    private val habitApi: HabitApi,
    private val validateImportDataUseCase: ValidateImportDataUseCase
) {
    
    sealed class ImportResult {
        data class Success(
            val totalImported: Int,
            val totalSkipped: Int,
            val totalFailed: Int,
            val ledgerResult: ImportLedgerResult? = null,
            val todoResult: ImportTasksResult? = null,
            val habitResult: ImportHabitsResult? = null,
            val messages: List<String> = emptyList()
        ) : ImportResult()
        
        data class Error(val message: String) : ImportResult()
        
        data class Progress(
            val currentModule: String,
            val progress: Float,
            val message: String
        ) : ImportResult()
    }
    
    suspend operator fun invoke(
        jsonContent: String,
        conflictResolution: String,
        onProgress: (ImportResult.Progress) -> Unit = {}
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            // 1. 验证数据
            onProgress(ImportResult.Progress("验证", 0.1f, "正在验证数据格式..."))
            
            val validationResult = validateImportDataUseCase(jsonContent)
            if (validationResult is ValidateImportDataUseCase.ValidationResult.Error) {
                return@withContext ImportResult.Error(validationResult.message)
            }
            
            val validation = validationResult as ValidateImportDataUseCase.ValidationResult.Success
            
            // 2. 解析数据
            onProgress(ImportResult.Progress("解析", 0.2f, "正在解析数据..."))
            
            val jsonElement = JsonParser.parseString(jsonContent)
            val jsonObject = jsonElement.asJsonObject
            val data = jsonObject.getAsJsonObject("data")
            
            // 3. 并行导入各模块数据
            var ledgerResult: ImportLedgerResult? = null
            var todoResult: ImportTasksResult? = null
            var habitResult: ImportHabitsResult? = null
            val messages = mutableListOf<String>()
            
            coroutineScope {
                // 导入记账数据
                val ledgerDeferred = if (validation.hasLedgerData) {
                    async {
                        onProgress(ImportResult.Progress("记账", 0.4f, "正在导入记账数据..."))
                        try {
                            val ledgerData = data.getAsJsonObject("ledger")
                            val ledgerDataMap = parseLedgerData(ledgerData)
                            ledgerApi.importLedgerData(ledgerDataMap, conflictResolution)
                        } catch (e: Exception) {
                            messages.add("记账数据导入失败: ${e.message}")
                            null
                        }
                    }
                } else null
                
                // 导入待办数据
                val todoDeferred = if (validation.hasTodoData) {
                    async {
                        onProgress(ImportResult.Progress("待办", 0.6f, "正在导入待办数据..."))
                        try {
                            val todoData = data.getAsJsonObject("todo")
                            val tasks = parseTodoData(todoData)
                            todoApi.importTasks(tasks, conflictResolution)
                        } catch (e: Exception) {
                            messages.add("待办数据导入失败: ${e.message}")
                            null
                        }
                    }
                } else null
                
                // 导入习惯数据
                val habitDeferred = if (validation.hasHabitData) {
                    async {
                        onProgress(ImportResult.Progress("习惯", 0.8f, "正在导入习惯数据..."))
                        try {
                            val habitData = data.getAsJsonObject("habit")
                            val habits = parseHabitData(habitData)
                            habitApi.importHabits(habits, conflictResolution)
                        } catch (e: Exception) {
                            messages.add("习惯数据导入失败: ${e.message}")
                            null
                        }
                    }
                } else null
                
                // 等待所有导入完成
                ledgerResult = ledgerDeferred?.await()
                todoResult = todoDeferred?.await()
                habitResult = habitDeferred?.await()
            }
            
            onProgress(ImportResult.Progress("完成", 1.0f, "数据导入完成"))
            
            // 4. 汇总结果
            var totalImported = 0
            var totalSkipped = 0
            var totalFailed = 0
            
            // 汇总记账结果
            ledgerResult?.let { result ->
                totalImported += result.transactionSuccess + result.accountSuccess + result.categorySuccess
                totalSkipped += result.transactionSkipped + result.accountSkipped + result.categorySkipped
                totalFailed += result.transactionFailed + result.accountFailed + result.categoryFailed
                
                if (result.errors.isNotEmpty()) {
                    messages.add("记账模块错误: ${result.errors.size}个")
                }
            }
            
            // 汇总待办结果
            todoResult?.let { result ->
                totalImported += result.successCount
                totalSkipped += result.skippedCount
                totalFailed += result.failedCount
                
                if (result.errors.isNotEmpty()) {
                    messages.add("待办模块错误: ${result.errors.size}个")
                }
            }
            
            // 汇总习惯结果
            habitResult?.let { result ->
                totalImported += result.successCount
                totalSkipped += result.skippedCount
                totalFailed += result.failedCount
                
                if (result.errors.isNotEmpty()) {
                    messages.add("习惯模块错误: ${result.errors.size}个")
                }
            }
            
            // 添加成功消息
            if (totalImported > 0) {
                messages.add(0, "成功导入 $totalImported 条数据")
            }
            if (totalSkipped > 0) {
                messages.add("跳过 $totalSkipped 条重复数据")
            }
            if (totalFailed > 0) {
                messages.add("失败 $totalFailed 条数据")
            }
            
            ImportResult.Success(
                totalImported = totalImported,
                totalSkipped = totalSkipped,
                totalFailed = totalFailed,
                ledgerResult = ledgerResult,
                todoResult = todoResult,
                habitResult = habitResult,
                messages = messages
            )
            
        } catch (e: Exception) {
            ImportResult.Error("导入过程发生错误: ${e.message}")
        }
    }
    
    private fun parseLedgerData(ledgerData: JsonObject): Map<String, Any> {
        val dataMap = mutableMapOf<String, Any>()
        
        // 解析交易记录
        if (ledgerData.has("transactions")) {
            val transactions = ledgerData.getAsJsonArray("transactions")
            val transactionList = mutableListOf<Map<String, Any>>()
            
            transactions.forEach { element ->
                val transaction = element.asJsonObject
                val transactionMap = mutableMapOf<String, Any>()
                
                transaction.entrySet().forEach { entry ->
                    when (entry.value) {
                        is com.google.gson.JsonPrimitive -> {
                            val primitive = entry.value.asJsonPrimitive
                            transactionMap[entry.key] = when {
                                primitive.isBoolean -> primitive.asBoolean
                                primitive.isNumber -> primitive.asNumber
                                else -> primitive.asString
                            }
                        }
                        is com.google.gson.JsonNull -> {
                            // Skip null values
                        }
                        else -> {
                            transactionMap[entry.key] = entry.value.toString()
                        }
                    }
                }
                
                transactionList.add(transactionMap)
            }
            
            dataMap["transactions"] = transactionList
        }
        
        // 解析账户
        if (ledgerData.has("accounts")) {
            val accounts = ledgerData.getAsJsonArray("accounts")
            val accountList = mutableListOf<Map<String, Any>>()
            
            accounts.forEach { element ->
                val account = element.asJsonObject
                val accountMap = mutableMapOf<String, Any>()
                
                account.entrySet().forEach { entry ->
                    when (entry.value) {
                        is com.google.gson.JsonPrimitive -> {
                            val primitive = entry.value.asJsonPrimitive
                            accountMap[entry.key] = when {
                                primitive.isBoolean -> primitive.asBoolean
                                primitive.isNumber -> primitive.asNumber
                                else -> primitive.asString
                            }
                        }
                        is com.google.gson.JsonNull -> {
                            // Skip null values
                        }
                        else -> {
                            accountMap[entry.key] = entry.value.toString()
                        }
                    }
                }
                
                accountList.add(accountMap)
            }
            
            dataMap["accounts"] = accountList
        }
        
        // 解析分类
        if (ledgerData.has("categories")) {
            val categories = ledgerData.getAsJsonArray("categories")
            val categoryList = mutableListOf<Map<String, Any>>()
            
            categories.forEach { element ->
                val category = element.asJsonObject
                val categoryMap = mutableMapOf<String, Any>()
                
                category.entrySet().forEach { entry ->
                    when (entry.value) {
                        is com.google.gson.JsonPrimitive -> {
                            val primitive = entry.value.asJsonPrimitive
                            categoryMap[entry.key] = when {
                                primitive.isBoolean -> primitive.asBoolean
                                primitive.isNumber -> primitive.asNumber
                                else -> primitive.asString
                            }
                        }
                        is com.google.gson.JsonNull -> {
                            // Skip null values
                        }
                        else -> {
                            categoryMap[entry.key] = entry.value.toString()
                        }
                    }
                }
                
                categoryList.add(categoryMap)
            }
            
            dataMap["categories"] = categoryList
        }
        
        // 解析预算
        if (ledgerData.has("budgets")) {
            val budgets = ledgerData.getAsJsonArray("budgets")
            val budgetList = mutableListOf<Map<String, Any>>()
            
            budgets.forEach { element ->
                val budget = element.asJsonObject
                val budgetMap = mutableMapOf<String, Any>()
                
                budget.entrySet().forEach { entry ->
                    when (entry.value) {
                        is com.google.gson.JsonPrimitive -> {
                            val primitive = entry.value.asJsonPrimitive
                            budgetMap[entry.key] = when {
                                primitive.isBoolean -> primitive.asBoolean
                                primitive.isNumber -> primitive.asNumber
                                else -> primitive.asString
                            }
                        }
                        is com.google.gson.JsonNull -> {
                            // Skip null values
                        }
                        else -> {
                            budgetMap[entry.key] = entry.value.toString()
                        }
                    }
                }
                
                budgetList.add(budgetMap)
            }
            
            dataMap["budgets"] = budgetList
        }
        
        // 解析存钱目标
        if (ledgerData.has("savingsGoals")) {
            val goals = ledgerData.getAsJsonArray("savingsGoals")
            val goalList = mutableListOf<Map<String, Any>>()
            
            goals.forEach { element ->
                val goal = element.asJsonObject
                val goalMap = mutableMapOf<String, Any>()
                
                goal.entrySet().forEach { entry ->
                    when (entry.value) {
                        is com.google.gson.JsonPrimitive -> {
                            val primitive = entry.value.asJsonPrimitive
                            goalMap[entry.key] = when {
                                primitive.isBoolean -> primitive.asBoolean
                                primitive.isNumber -> primitive.asNumber
                                else -> primitive.asString
                            }
                        }
                        is com.google.gson.JsonNull -> {
                            // Skip null values
                        }
                        else -> {
                            goalMap[entry.key] = entry.value.toString()
                        }
                    }
                }
                
                goalList.add(goalMap)
            }
            
            dataMap["savingsGoals"] = goalList
        }
        
        return dataMap
    }
    
    private fun parseTodoData(todoData: JsonObject): List<Map<String, Any>> {
        val tasks = todoData.getAsJsonArray("tasks")
        val taskList = mutableListOf<Map<String, Any>>()
        
        tasks.forEach { element ->
            val task = element.asJsonObject
            val taskMap = mutableMapOf<String, Any>()
            
            task.entrySet().forEach { entry ->
                when (entry.value) {
                    is com.google.gson.JsonPrimitive -> {
                        val primitive = entry.value.asJsonPrimitive
                        taskMap[entry.key] = when {
                            primitive.isBoolean -> primitive.asBoolean
                            primitive.isNumber -> primitive.asNumber
                            else -> primitive.asString
                        }
                    }
                    is com.google.gson.JsonNull -> {
                        // Skip null values
                    }
                    else -> {
                        taskMap[entry.key] = entry.value.toString()
                    }
                }
            }
            
            taskList.add(taskMap)
        }
        
        return taskList
    }
    
    private fun parseHabitData(habitData: JsonObject): List<Map<String, Any>> {
        val habits = habitData.getAsJsonArray("habits")
        val habitList = mutableListOf<Map<String, Any>>()
        
        habits.forEach { element ->
            val habit = element.asJsonObject
            val habitMap = mutableMapOf<String, Any>()
            
            habit.entrySet().forEach { entry ->
                when (entry.value) {
                    is com.google.gson.JsonPrimitive -> {
                        val primitive = entry.value.asJsonPrimitive
                        habitMap[entry.key] = when {
                            primitive.isBoolean -> primitive.asBoolean
                            primitive.isNumber -> primitive.asNumber
                            else -> primitive.asString
                        }
                    }
                    is com.google.gson.JsonNull -> {
                        // Skip null values
                    }
                    else -> {
                        habitMap[entry.key] = entry.value.toString()
                    }
                }
            }
            
            habitList.add(habitMap)
        }
        
        return habitList
    }
}