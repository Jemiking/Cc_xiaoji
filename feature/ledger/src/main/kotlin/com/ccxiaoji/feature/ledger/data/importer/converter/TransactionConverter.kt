package com.ccxiaoji.feature.ledger.data.importer.converter

import com.ccxiaoji.feature.ledger.data.importer.DataLine
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionEntity
import com.ccxiaoji.feature.ledger.domain.importer.ImportError
import java.util.UUID
import javax.inject.Inject

/**
 * 交易记录转换器
 * TRANSACTION格式: 交易时间,账户,分类,金额,备注,定期生成
 */
class TransactionConverter @Inject constructor() : DataConverter<TransactionEntity>() {
    
    override fun convert(dataLine: DataLine, userId: String): ConvertResult<TransactionEntity> {
        val data = dataLine.data
        
        if (data.size < 4) {
            return ConvertResult.Error(
                ImportError.FormatError(dataLine.line, "交易数据格式错误，至少需要4个字段")
            )
        }
        
        try {
            val transactionTime = safeGetString(data, 0)
            val accountName = safeGetString(data, 1) // 需要映射为accountId
            val categoryName = safeGetString(data, 2) // 需要映射为categoryId
            val amount = safeGetDouble(data, 3)
            val note = safeGetString(data, 4)
            val isRecurring = safeGetBoolean(data, 5)
            
            // 验证必填字段
            if (accountName.isEmpty()) {
                return ConvertResult.Error(
                    ImportError.ValidationError(dataLine.line, "账户", "账户不能为空")
                )
            }
            if (categoryName.isEmpty()) {
                return ConvertResult.Error(
                    ImportError.ValidationError(dataLine.line, "分类", "分类不能为空")
                )
            }
            
            // 解析交易时间
            val transactionDate = parseDateTime(transactionTime) ?: parseDate(transactionTime)
            if (transactionDate == null) {
                return ConvertResult.Error(
                    ImportError.ValidationError(dataLine.line, "交易时间", "交易时间格式错误")
                )
            }
            
            // 创建交易实体（accountId和categoryId暂时使用名称，后续需要映射）
            val transaction = TransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = accountName, // 临时使用名称，后续映射
                categoryId = categoryName, // 临时使用名称，后续映射
                amountCents = amountToCents(amount).toInt(),
                note = note.ifEmpty { null },
                createdAt = transactionDate,
                updatedAt = System.currentTimeMillis(),
                syncStatus = com.ccxiaoji.common.model.SyncStatus.PENDING
            )
            
            return ConvertResult.Success(transaction)
            
        } catch (e: Exception) {
            return ConvertResult.Error(
                ImportError.FormatError(dataLine.line, "交易数据转换失败: ${e.message}")
            )
        }
    }
}