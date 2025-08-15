package com.ccxiaoji.feature.ledger.data.importer.converter

import com.ccxiaoji.feature.ledger.data.importer.DataLine
import com.ccxiaoji.feature.ledger.data.local.entity.SavingsGoalEntity
import com.ccxiaoji.feature.ledger.domain.importer.ImportError
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

/**
 * 储蓄目标转换器
 * SAVINGS格式: 目标名称,目标金额,当前金额,截止日期,进度百分比,颜色
 */
class SavingsGoalConverter @Inject constructor() : DataConverter<SavingsGoalEntity>() {
    
    override fun convert(dataLine: DataLine, userId: String): ConvertResult<SavingsGoalEntity> {
        val data = dataLine.data
        
        if (data.size < 4) {
            return ConvertResult.Error(
                ImportError.FormatError(dataLine.line, "储蓄目标数据格式错误，至少需要4个字段")
            )
        }
        
        try {
            val name = safeGetString(data, 0)
            val targetAmount = safeGetDouble(data, 1)
            val currentAmount = safeGetDouble(data, 2)
            val deadlineStr = safeGetString(data, 3)
            val progressPercentage = safeGetDouble(data, 4)
            val color = safeGetString(data, 5)
            
            // 验证必填字段
            if (name.isEmpty()) {
                return ConvertResult.Error(
                    ImportError.ValidationError(dataLine.line, "目标名称", "目标名称不能为空")
                )
            }
            
            // 解析截止日期
            val targetDate = try {
                if (deadlineStr.isNotEmpty()) {
                    LocalDate.parse(deadlineStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } else null
            } catch (e: Exception) {
                null
            }
            
            // 创建储蓄目标实体
            val savingsGoal = SavingsGoalEntity(
                id = 0, // 自动生成
                userId = userId,
                name = name,
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                targetDate = targetDate,
                color = color.ifEmpty { "#4CAF50" },
                iconName = "savings",
                description = null,
                isActive = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                syncStatus = com.ccxiaoji.common.model.SyncStatus.PENDING
            )
            
            return ConvertResult.Success(savingsGoal)
            
        } catch (e: Exception) {
            return ConvertResult.Error(
                ImportError.FormatError(dataLine.line, "储蓄目标数据转换失败: ${e.message}")
            )
        }
    }
}