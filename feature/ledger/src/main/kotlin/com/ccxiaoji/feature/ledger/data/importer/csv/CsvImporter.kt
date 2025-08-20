package com.ccxiaoji.feature.ledger.data.importer.csv

import android.util.Log
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.dao.BudgetDao
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionEntity
import com.ccxiaoji.feature.ledger.data.local.entity.BudgetEntity
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * CSV格式的记账数据导入器
 * 支持CC小记导出的CSV格式（v2.1）
 * 支持二级分类结构导入
 */
class CsvImporter @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    
    companion object {
        private const val TAG = "CsvImporter"
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }
    
    data class ImportResult(
        val accountsImported: Int,
        val categoriesImported: Int,
        val transactionsImported: Int,
        val budgetsImported: Int,
        val errors: List<String>
    )
    
    /**
     * 从CSV文件导入数据
     */
    suspend fun importFromCsv(
        inputStream: InputStream,
        userId: String,
        replaceExisting: Boolean = false
    ): ImportResult {
        val errors = mutableListOf<String>()
        var accountsImported = 0
        var categoriesImported = 0
        var transactionsImported = 0
        var budgetsImported = 0
        
        // 用于缓存已创建的分类和账户
        val categoryCache = mutableMapOf<String, String>() // name -> id
        val accountCache = mutableMapOf<String, String>() // name -> id
        val parentCategoryCache = mutableMapOf<String, CategoryEntity>() // 父分类缓存
        
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var line: String?
            var lineNumber = 0
            
            while (reader.readLine().also { line = it } != null) {
                lineNumber++
                val currentLine = line ?: continue
                
                // 跳过注释行和空行
                if (currentLine.startsWith("#") || currentLine.isBlank()) {
                    continue
                }
                
                // 解析CSV行
                val parts = parseCsvLine(currentLine)
                if (parts.isEmpty()) continue
                
                val dataType = parts[0]
                
                try {
                    when (dataType) {
                        "HEADER" -> {
                            // 处理文件头，可以用来验证版本等
                            Log.d(TAG, "导入文件版本: ${parts.getOrNull(2)}")
                        }
                        
                        "ACCOUNT" -> {
                            val account = parseAccount(parts, userId)
                            if (account != null) {
                                val existingAccount = accountDao.findByName(account.name, userId)
                                if (existingAccount == null || replaceExisting) {
                                    accountDao.insert(account)
                                    accountCache[account.name] = account.id
                                    accountsImported++
                                } else {
                                    accountCache[account.name] = existingAccount.id
                                }
                            }
                        }
                        
                        "CATEGORY" -> {
                            val result = parseCategory(parts, userId, parentCategoryCache)
                            if (result != null) {
                                val (category, parentName) = result
                                
                                // 如果有父分类名，需要先确保父分类存在
                                if (!parentName.isNullOrEmpty()) {
                                    var parentCategory = parentCategoryCache[parentName]
                                    if (parentCategory == null) {
                                        // 查找或创建父分类
                                        parentCategory = categoryDao.findByNameAndType(parentName, category.type, userId)
                                        if (parentCategory == null) {
                                            // 创建父分类
                                            parentCategory = CategoryEntity(
                                                id = UUID.randomUUID().toString(),
                                                userId = userId,
                                                name = parentName,
                                                type = category.type,
                                                icon = "📁",
                                                color = "#6200EE",
                                                parentId = null,
                                                displayOrder = 0,
                                                isSystem = false,
                                                usageCount = 0,
                                                createdAt = System.currentTimeMillis(),
                                                updatedAt = System.currentTimeMillis(),
                                                isDeleted = false,
                                                syncStatus = SyncStatus.SYNCED
                                            )
                                            categoryDao.insert(parentCategory)
                                            parentCategoryCache[parentName] = parentCategory
                                        } else {
                                            parentCategoryCache[parentName] = parentCategory
                                        }
                                    }
                                    
                                    // 设置子分类的parentId
                                    val childCategory = category.copy(parentId = parentCategory.id)
                                    
                                    // 检查子分类是否已存在
                                    val existingChild = categoryDao.findByNameAndParent(
                                        childCategory.name,
                                        parentCategory.id,
                                        userId
                                    )
                                    
                                    if (existingChild == null || replaceExisting) {
                                        categoryDao.insert(childCategory)
                                        categoryCache["${parentName}/${childCategory.name}"] = childCategory.id
                                        categoriesImported++
                                    } else {
                                        categoryCache["${parentName}/${existingChild.name}"] = existingChild.id
                                    }
                                } else {
                                    // 处理没有父分类的情况（一级分类）
                                    val existingCategory = categoryDao.findByNameAndType(
                                        category.name,
                                        category.type,
                                        userId
                                    )
                                    
                                    if (existingCategory == null || replaceExisting) {
                                        categoryDao.insert(category)
                                        categoryCache[category.name] = category.id
                                        parentCategoryCache[category.name] = category
                                        categoriesImported++
                                    } else {
                                        categoryCache[category.name] = existingCategory.id
                                        parentCategoryCache[category.name] = existingCategory
                                    }
                                }
                            }
                        }
                        
                        "TRANSACTION" -> {
                            val transaction = parseTransaction(parts, userId, accountCache, categoryCache)
                            if (transaction != null) {
                                // 检查是否已存在（通过时间和金额判断）
                                val existing = transactionDao.findByUserAndTimeAndAmount(
                                    userId,
                                    transaction.createdAt,
                                    transaction.amountCents
                                )
                                
                                if (existing.isEmpty() || replaceExisting) {
                                    transactionDao.insert(transaction)
                                    transactionsImported++
                                }
                            }
                        }
                        
                        "BUDGET" -> {
                            val budget = parseBudget(parts, userId, categoryCache)
                            if (budget != null) {
                                val existing = budgetDao.findByYearMonth(
                                    userId,
                                    budget.year,
                                    budget.month,
                                    budget.categoryId
                                )
                                
                                if (existing == null || replaceExisting) {
                                    budgetDao.insert(budget)
                                    budgetsImported++
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    errors.add("第${lineNumber}行解析失败: ${e.message}")
                    Log.e(TAG, "解析第${lineNumber}行失败", e)
                }
            }
        }
        
        return ImportResult(
            accountsImported = accountsImported,
            categoriesImported = categoriesImported,
            transactionsImported = transactionsImported,
            budgetsImported = budgetsImported,
            errors = errors
        )
    }
    
    /**
     * 解析CSV行
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            val c = line[i]
            
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        // 双引号转义
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> {
                    current.append(c)
                }
            }
            i++
        }
        
        result.add(current.toString())
        return result
    }
    
    /**
     * 解析账户数据
     */
    private fun parseAccount(parts: List<String>, userId: String): AccountEntity? {
        if (parts.size < 9) return null
        
        return try {
            AccountEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = parts[2],
                type = parts[3],
                balanceCents = (parts[4].toDoubleOrNull() ?: 0.0 * 100).toLong(),
                currency = "CNY",
                icon = parts[8].ifEmpty { "💰" },
                color = null,
                isDefault = parts[7] == "是",
                creditLimitCents = parts[5].toDoubleOrNull()?.let { (it * 100).toLong() },
                billingDay = parts[6].toIntOrNull(),
                paymentDueDay = null,
                gracePeriodDays = null,
                annualFeeAmountCents = null,
                annualFeeWaiverThresholdCents = null,
                cashAdvanceLimitCents = null,
                interestRate = null,
                createdAt = parseDate(parts[1]) ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            )
        } catch (e: Exception) {
            Log.e(TAG, "解析账户失败: ${e.message}")
            null
        }
    }
    
    /**
     * 解析分类数据
     * @return Pair(分类实体, 父分类名称)
     */
    @Suppress("UNUSED_PARAMETER")
    private fun parseCategory(
        parts: List<String>,
        userId: String,
        parentCache: Map<String, CategoryEntity>
    ): Pair<CategoryEntity, String?>? {
        if (parts.size < 8) return null
        
        return try {
            val parentName = parts[6].ifEmpty { null }
            
            val category = CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = parts[2],
                type = parts[3],
                icon = parts[4].ifEmpty { "📝" },
                color = parts[5].ifEmpty { "#6200EE" },
                parentId = null, // 稍后设置
                displayOrder = parts[7].toIntOrNull() ?: 0,
                isSystem = false,
                usageCount = 0,
                createdAt = parseDate(parts[1]) ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            )
            
            Pair(category, parentName)
        } catch (e: Exception) {
            Log.e(TAG, "解析分类失败: ${e.message}")
            null
        }
    }
    
    /**
     * 解析交易数据
     */
    private suspend fun parseTransaction(
        parts: List<String>,
        userId: String,
        accountCache: Map<String, String>,
        categoryCache: Map<String, String>
    ): TransactionEntity? {
        if (parts.size < 6) return null
        
        return try {
            val accountName = parts[2]
            val categoryName = parts[3]
            
            // 查找账户ID
            val accountId = accountCache[accountName] ?: run {
                // 尝试从数据库查找
                accountDao.findByName(accountName, userId)?.id
            } ?: return null
            
            // 查找分类ID（尝试多种格式）
            val categoryId = categoryCache[categoryName] 
                ?: categoryCache.entries.find { it.key.endsWith("/$categoryName") }?.value
                ?: run {
                    // 从数据库查找
                    categoryDao.findByNameAndType(categoryName, "EXPENSE", userId)?.id
                        ?: categoryDao.findByNameAndType(categoryName, "INCOME", userId)?.id
                } ?: return null
            
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = accountId,
                amountCents = (parts[4].toDoubleOrNull() ?: 0.0 * 100).toInt(),
                categoryId = categoryId,
                note = parts[5].ifEmpty { null },
                ledgerId = "default", // 默认记账簿
                createdAt = parseDate(parts[1]) ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                transactionDate = parseDate(parts[1]),
                locationLatitude = null,
                locationLongitude = null,
                locationAddress = null,
                locationPrecision = null,
                locationProvider = null,
                syncStatus = SyncStatus.SYNCED
            )
        } catch (e: Exception) {
            Log.e(TAG, "解析交易失败: ${e.message}")
            null
        }
    }
    
    /**
     * 解析预算数据
     */
    private fun parseBudget(
        parts: List<String>,
        userId: String,
        categoryCache: Map<String, String>
    ): BudgetEntity? {
        if (parts.size < 10) return null
        
        return try {
            val yearMonth = parts[1].split("-")
            if (yearMonth.size != 2) return null
            
            val categoryName = parts[2]
            val categoryId = if (categoryName == "总预算") {
                null
            } else {
                categoryCache[categoryName]
                    ?: categoryCache.entries.find { it.key.endsWith("/$categoryName") }?.value
            }
            
            BudgetEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                year = yearMonth[0].toInt(),
                month = yearMonth[1].toInt(),
                categoryId = categoryId,
                budgetAmountCents = (parts[3].toDoubleOrNull() ?: 0.0 * 100).toInt(),
                alertThreshold = parts[4].removeSuffix("%").toIntOrNull()?.div(100f) ?: 0.8f,
                note = parts[9].ifEmpty { null },
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            )
        } catch (e: Exception) {
            Log.e(TAG, "解析预算失败: ${e.message}")
            null
        }
    }
    
    /**
     * 解析日期
     */
    private fun parseDate(dateStr: String): Long? {
        return try {
            dateFormat.parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }
}