package com.ccxiaoji.feature.ledger.data.importer.qianji

import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * 钱迹数据导入器
 * 负责协调整个导入流程
 */
class QianjiImporter @Inject constructor(
    private val parser: QianjiParser,
    private val mapper: QianjiMapper,
    private val transactionDao: TransactionDao
) {
    
    /**
     * 导入结果
     */
    sealed class ImportResult {
        data class Success(
            val imported: Int,
            val skipped: Int,
            val failed: Int,
            val total: Int
        ) : ImportResult()
        
        data class Error(val message: String) : ImportResult()
    }
    
    /**
     * 导入选项
     */
    data class ImportOptions(
        val skipDuplicates: Boolean = true,
        val createCategories: Boolean = true,
        val createAccounts: Boolean = true,
        val mergeSubCategories: Boolean = true,
        val handleRefunds: Boolean = true,
        val batchSize: Int = 500
    )
    
    /**
     * 导入进度
     */
    data class ImportProgress(
        val current: Int = 0,
        val total: Int = 0,
        val message: String = ""
    )
    
    // 进度状态流
    private val _progress = MutableStateFlow(ImportProgress())
    val progress: StateFlow<ImportProgress> = _progress
    
    /**
     * 导入钱迹数据
     */
    suspend fun import(
        file: File,
        userId: String,
        options: ImportOptions = ImportOptions()
    ): ImportResult = withContext(Dispatchers.IO) {
        android.util.Log.e("QIANJI_DEBUG", "========== 开始导入流程 ==========")
        android.util.Log.e("QIANJI_DEBUG", "文件: ${file.absolutePath}")
        android.util.Log.e("QIANJI_DEBUG", "用户ID: $userId")
        android.util.Log.e("QIANJI_DEBUG", "选项: $options")
        
        try {
            // 1. 解析文件
            updateProgress(0, 0, "正在解析文件...")
            android.util.Log.e("QIANJI_DEBUG", "开始解析文件...")
            val records = parser.parseFile(file)
            android.util.Log.e("QIANJI_DEBUG", "解析完成，共 ${records.size} 条记录")
            
            if (records.isEmpty()) {
                return@withContext ImportResult.Error("文件为空或格式错误")
            }
            
            val total = records.size
            var imported = 0
            var skipped = 0
            var failed = 0
            
            // 2. 批量处理
            val transactions = mutableListOf<TransactionEntity>()
            
            records.chunked(options.batchSize).forEachIndexed { batchIndex, batch ->
                val currentBatchStart = batchIndex * options.batchSize
                
                batch.forEachIndexed { index, record ->
                    val currentIndex = currentBatchStart + index + 1
                    updateProgress(currentIndex, total, "处理第 $currentIndex/$total 条记录...")
                    
                    try {
                        // 检查是否重复
                        if (options.skipDuplicates && mapper.isTransactionExists(record.id, userId)) {
                            skipped++
                        } else {
                            // 映射为交易实体
                            android.util.Log.e("QIANJI_DEBUG", "映射记录 ${record.id}: ${record.datetime}, ${record.type}, ${record.amount}")
                            val transaction = mapper.mapToTransaction(
                                record = record,
                                userId = userId,
                                createCategories = options.createCategories,
                                createAccounts = options.createAccounts,
                                mergeSubCategories = options.mergeSubCategories
                            )
                            
                            if (transaction != null) {
                                transactions.add(transaction)
                                imported++
                                android.util.Log.e("QIANJI_DEBUG", "成功映射交易: ${transaction.id}, 账户: ${transaction.accountId}, 分类: ${transaction.categoryId}")
                            } else {
                                failed++
                            }
                        }
                    } catch (e: Exception) {
                        println("处理记录失败: ${e.message}")
                        failed++
                    }
                }
                
                // 批量保存
                if (transactions.isNotEmpty()) {
                    updateProgress(currentBatchStart + batch.size, total, "正在保存数据...")
                    android.util.Log.e("QIANJI_DEBUG", "准备保存 ${transactions.size} 条交易到数据库")
                    android.util.Log.e("QIANJI_DEBUG", "第一条交易: ${transactions.first().let { "ID=${it.id}, UserID=${it.userId}, Amount=${it.amountCents}" }}")
                    transactionDao.insertAll(transactions)
                    android.util.Log.e("QIANJI_DEBUG", "保存成功！")
                    transactions.clear()
                }
            }
            
            // 3. 处理退款关联（如果需要）
            if (options.handleRefunds) {
                updateProgress(total, total, "处理退款关联...")
                // 这里可以添加退款关联的处理逻辑
            }
            
            updateProgress(total, total, "导入完成")
            
            // 验证导入结果
            android.util.Log.e("QIANJI_DEBUG", "========== 验证导入结果 ==========")
            val totalInDb = transactionDao.getAllTransactionsCount()
            val userTransactions = transactionDao.getUserTransactionsCount(userId)
            val recentTransactions = transactionDao.getRecentTransactions()
            
            android.util.Log.e("QIANJI_DEBUG", "数据库中总交易数: $totalInDb")
            android.util.Log.e("QIANJI_DEBUG", "当前用户交易数: $userTransactions")
            android.util.Log.e("QIANJI_DEBUG", "用户ID: $userId")
            
            if (recentTransactions.isNotEmpty()) {
                android.util.Log.e("QIANJI_DEBUG", "最近的交易记录:")
                recentTransactions.take(3).forEach { transaction ->
                    android.util.Log.e("QIANJI_DEBUG", "  - ID: ${transaction.id}")
                    android.util.Log.e("QIANJI_DEBUG", "    UserID: ${transaction.userId}")
                    android.util.Log.e("QIANJI_DEBUG", "    Amount: ${transaction.amountCents}")
                    android.util.Log.e("QIANJI_DEBUG", "    Time: ${transaction.createdAt}")
                }
            }
            
            android.util.Log.e("QIANJI_DEBUG", "========== 导入完成 ==========")
            android.util.Log.e("QIANJI_DEBUG", "导入: $imported 条")
            android.util.Log.e("QIANJI_DEBUG", "跳过: $skipped 条")
            android.util.Log.e("QIANJI_DEBUG", "失败: $failed 条")
            android.util.Log.e("QIANJI_DEBUG", "总计: $total 条")
            
            ImportResult.Success(
                imported = imported,
                skipped = skipped,
                failed = failed,
                total = total
            )
            
        } catch (e: Exception) {
            ImportResult.Error("导入失败: ${e.message}")
        }
    }
    
    /**
     * 预览导入数据
     */
    suspend fun preview(
        file: File,
        limit: Int = 100
    ): List<QianjiParser.QianjiRecord> = withContext(Dispatchers.IO) {
        android.util.Log.d("QianjiImporter", "preview: Starting preview for file: ${file.absolutePath}, limit: $limit")
        
        try {
            android.util.Log.d("QianjiImporter", "Parsing file for preview...")
            val records = parser.parseFile(file)
            android.util.Log.d("QianjiImporter", "Parsed ${records.size} total records")
            
            val previewRecords = records.take(limit)
            android.util.Log.d("QianjiImporter", "Returning ${previewRecords.size} preview records")
            
            if (previewRecords.isNotEmpty()) {
                android.util.Log.d("QianjiImporter", "First record: ${previewRecords[0]}")
            }
            
            return@withContext previewRecords
        } catch (e: Exception) {
            android.util.Log.e("QianjiImporter", "Error during preview: ${e.message}", e)
            return@withContext emptyList()
        }
    }
    
    /**
     * 验证文件格式
     */
    suspend fun validateFile(file: File): Boolean = withContext(Dispatchers.IO) {
        android.util.Log.d("QianjiImporter", "validateFile: Starting validation for file: ${file.absolutePath}")
        android.util.Log.d("QianjiImporter", "File exists: ${file.exists()}, canRead: ${file.canRead()}, size: ${file.length()}")
        
        try {
            android.util.Log.d("QianjiImporter", "Reading file lines...")
            val lines = file.bufferedReader().use { it.readLines() }
            android.util.Log.d("QianjiImporter", "Read ${lines.size} lines from file")
            
            if (lines.isEmpty()) {
                android.util.Log.w("QianjiImporter", "File is empty")
                return@withContext false
            }
            
            // 处理UTF-8 BOM
            var firstLine = lines[0]
            if (firstLine.startsWith("\uFEFF")) {
                firstLine = firstLine.substring(1)
                android.util.Log.d("QianjiImporter", "Removed UTF-8 BOM from first line")
            }
            android.util.Log.d("QianjiImporter", "First line (raw): $firstLine")
            
            // 使用parser的parseCSVLine方法来正确解析CSV
            val headers = parser.parseCSVLine(firstLine)
            android.util.Log.d("QianjiImporter", "Parsed headers (${headers.size}): ${headers.joinToString(", ")}")
            
            // 清理headers，去除空格
            val cleanHeaders = headers.map { it.trim() }
            val isValid = parser.isQianjiFormat(cleanHeaders)
            android.util.Log.d("QianjiImporter", "Validation result: $isValid")
            
            return@withContext isValid
        } catch (e: Exception) {
            android.util.Log.e("QianjiImporter", "Error validating file: ${e.message}", e)
            return@withContext false
        }
    }
    
    /**
     * 更新进度
     */
    private fun updateProgress(current: Int, total: Int, message: String) {
        _progress.value = ImportProgress(current, total, message)
    }
}