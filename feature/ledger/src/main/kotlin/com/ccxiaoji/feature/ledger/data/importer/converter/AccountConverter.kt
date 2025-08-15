package com.ccxiaoji.feature.ledger.data.importer.converter

import com.ccxiaoji.feature.ledger.data.importer.DataLine
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import com.ccxiaoji.feature.ledger.domain.importer.ImportError
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import java.util.UUID
import javax.inject.Inject

/**
 * 账户数据转换器
 * ACCOUNT格式: 创建日期,账户名称,账户类型,余额,信用额度,账单日,还款日,默认账户,图标
 */
class AccountConverter @Inject constructor() : DataConverter<AccountEntity>() {
    
    override fun convert(dataLine: DataLine, userId: String): ConvertResult<AccountEntity> {
        val data = dataLine.data
        
        if (data.size < 4) {
            return ConvertResult.Error(
                ImportError.FormatError(dataLine.line, "账户数据格式错误，至少需要4个字段")
            )
        }
        
        try {
            val createDate = safeGetString(data, 0)
            val name = safeGetString(data, 1)
            val typeStr = safeGetString(data, 2)
            val balance = safeGetDouble(data, 3)
            val creditLimit = if (data.size > 4) safeGetDouble(data, 4) else null
            val billingDay = if (data.size > 5) safeGetInt(data, 5, -1).takeIf { it > 0 } else null
            val paymentDueDay = if (data.size > 6) safeGetInt(data, 6, -1).takeIf { it > 0 } else null
            val isDefault = if (data.size > 7) safeGetBoolean(data, 7) else false
            val icon = if (data.size > 8) safeGetString(data, 8) else null
            
            // 验证必填字段
            if (name.isEmpty()) {
                return ConvertResult.Error(
                    ImportError.ValidationError(dataLine.line, "账户名称", "账户名称不能为空")
                )
            }
            
            // 解析账户类型
            val accountType = try {
                AccountType.valueOf(typeStr).name
            } catch (e: Exception) {
                "CASH" // 默认现金账户
            }
            
            // 创建账户实体
            val account = AccountEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                type = accountType,
                balanceCents = amountToCents(balance),
                creditLimitCents = creditLimit?.let { amountToCents(it) },
                billingDay = billingDay,
                paymentDueDay = paymentDueDay,
                isDefault = isDefault,
                icon = icon,
                color = null,
                createdAt = parseDate(createDate) ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = com.ccxiaoji.common.model.SyncStatus.PENDING
            )
            
            return ConvertResult.Success(account)
            
        } catch (e: Exception) {
            return ConvertResult.Error(
                ImportError.FormatError(dataLine.line, "账户数据转换失败: ${e.message}")
            )
        }
    }
}