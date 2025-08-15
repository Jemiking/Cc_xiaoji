package com.ccxiaoji.feature.ledger.data.importer.converter

import com.ccxiaoji.feature.ledger.data.importer.DataLine
import com.ccxiaoji.feature.ledger.data.local.entity.BudgetEntity
import com.ccxiaoji.feature.ledger.domain.importer.ImportError
import java.util.UUID
import javax.inject.Inject

/**
 * 预算数据转换器
 * BUDGET格式: 年月,分类,预算金额,已用百分比,已用金额,剩余金额
 */
class BudgetConverter @Inject constructor() : DataConverter<BudgetEntity>() {
    
    override fun convert(dataLine: DataLine, userId: String): ConvertResult<BudgetEntity> {
        val data = dataLine.data
        
        if (data.size < 3) {
            return ConvertResult.Error(
                ImportError.FormatError(dataLine.line, "预算数据格式错误，至少需要3个字段")
            )
        }
        
        try {
            val yearMonth = safeGetString(data, 0) // 格式: 2025-08
            val categoryName = safeGetString(data, 1) // 需要映射为categoryId
            val budgetAmount = safeGetDouble(data, 2)
            val usedPercentage = safeGetDouble(data, 3)
            val usedAmount = safeGetDouble(data, 4)
            val remainingAmount = safeGetDouble(data, 5)
            
            // 验证必填字段
            if (yearMonth.isEmpty()) {
                return ConvertResult.Error(
                    ImportError.ValidationError(dataLine.line, "年月", "年月不能为空")
                )
            }
            if (categoryName.isEmpty()) {
                return ConvertResult.Error(
                    ImportError.ValidationError(dataLine.line, "分类", "分类不能为空")
                )
            }
            
            // 解析年月
            val (year, month) = try {
                val parts = yearMonth.split("-")
                Pair(parts[0].toInt(), parts[1].toInt())
            } catch (e: Exception) {
                return ConvertResult.Error(
                    ImportError.ValidationError(dataLine.line, "年月", "年月格式错误，应为YYYY-MM")
                )
            }
            
            // 创建预算实体
            val budget = BudgetEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                categoryId = categoryName, // 临时使用名称，后续映射
                year = year,
                month = month,
                budgetAmountCents = amountToCents(budgetAmount).toInt(),
                alertThreshold = 0.8f,
                note = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = com.ccxiaoji.common.model.SyncStatus.PENDING
            )
            
            return ConvertResult.Success(budget)
            
        } catch (e: Exception) {
            return ConvertResult.Error(
                ImportError.FormatError(dataLine.line, "预算数据转换失败: ${e.message}")
            )
        }
    }
}