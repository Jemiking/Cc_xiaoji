package com.ccxiaoji.app.data.mapper

import com.ccxiaoji.app.domain.usecase.excel.*
import com.ccxiaoji.core.common.util.DateConverter
import com.ccxiaoji.feature.ledger.api.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import javax.inject.Inject

/**
 * 记账数据映射器
 * 负责处理记账模块相关的数据转换
 * 
 * 主要职责：
 * 1. API数据类型 → 业务数据类型
 * 2. 金额单位转换（分 ↔ 元）
 * 3. 时间戳和日期转换
 * 4. 字段映射和默认值处理
 */
class LedgerDataMapper @Inject constructor() {
    
    /**
     * 将交易记录转换为Excel导出数据
     * @param transaction API交易记录
     * @return Excel导出用的交易数据
     */
    fun mapTransactionToExportData(transaction: TransactionItem): TransactionData {
        return TransactionData(
            createdAt = transaction.date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            type = if (transaction.amount < 0) "支出" else "收入",
            categoryName = transaction.categoryName ?: "未分类",
            amount = Math.abs(transaction.amount),
            accountName = transaction.accountName,
            note = transaction.note
        )
    }
    
    /**
     * 将账户信息转换为Excel导出数据
     * @param account API账户信息
     * @return Excel导出用的账户数据
     */
    fun mapAccountToExportData(account: AccountItem): AccountData {
        return AccountData(
            name = account.name,
            type = getAccountTypeText(account.type),
            balance = account.balanceCents / 100.0,
            currency = account.currency,
            createdAt = account.createdAt.toEpochMilliseconds()
        )
    }
    
    /**
     * 将分类信息转换为Excel导出数据
     * @param category API分类信息
     * @param parentName 父分类名称（需要单独查询）
     * @return Excel导出用的分类数据
     */
    fun mapCategoryToExportData(
        category: CategoryItem,
        parentName: String? = null
    ): CategoryData {
        return CategoryData(
            name = category.name,
            type = getCategoryTypeText(category.type),
            parentName = parentName,
            icon = category.icon,
            color = category.color
        )
    }
    
    /**
     * 将预算信息转换为Excel导出数据
     * @param budget API预算信息
     * @return Excel导出用的预算数据
     */
    fun mapBudgetToExportData(budget: BudgetItem): BudgetData {
        return BudgetData(
            year = budget.year,
            month = budget.month,
            categoryName = budget.categoryName,
            budgetAmount = budget.budgetAmountCents / 100.0,
            spentAmount = budget.spentAmountCents / 100.0,
            note = budget.note
        )
    }
    
    /**
     * 将存钱目标转换为Excel导出数据
     * @param goal API存钱目标
     * @return Excel导出用的存钱目标数据
     */
    fun mapSavingsGoalToExportData(goal: SavingsGoalItem): SavingsGoalData {
        return SavingsGoalData(
            name = goal.name,
            description = goal.description,
            targetAmount = goal.targetAmountCents / 100.0,
            currentAmount = goal.currentAmountCents / 100.0,
            targetDate = goal.targetDate?.atStartOfDayIn(TimeZone.currentSystemDefault())?.toEpochMilliseconds(),
            isActive = goal.isActive
        )
    }
    
    /**
     * 转换账户类型为中文显示
     */
    private fun getAccountTypeText(type: String): String {
        return when (type) {
            "cash" -> "现金"
            "debit_card" -> "借记卡"
            "credit_card" -> "信用卡"
            "alipay" -> "支付宝"
            "wechat" -> "微信"
            "other" -> "其他"
            else -> type
        }
    }
    
    /**
     * 转换分类类型为中文显示
     */
    private fun getCategoryTypeText(type: String): String {
        return when (type) {
            "expense" -> "支出"
            "income" -> "收入"
            else -> type
        }
    }
    
    /**
     * 将分（cents）转换为元
     */
    fun centsToYuan(cents: Long): Double {
        return cents / 100.0
    }
    
    /**
     * 将元转换为分（cents）
     */
    fun yuanToCents(yuan: Double): Long {
        return (yuan * 100).toLong()
    }
    
    /**
     * 将kotlinx.datetime.Instant转换为时间戳
     */
    fun instantToTimestamp(instant: Instant): Long {
        return instant.toEpochMilliseconds()
    }
    
    /**
     * 将kotlinx.datetime.LocalDate转换为时间戳（当天开始时间）
     */
    fun localDateToTimestamp(date: LocalDate): Long {
        return date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }
}