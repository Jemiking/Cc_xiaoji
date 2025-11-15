package com.ccxiaoji.feature.ledger.presentation.screen.transaction

import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.model.LocationData
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionType
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

/**
 * 交易表单状态
 *
 * 仅包含业务数据字段，UI元状态由EntityEditorState管理
 * 遵循单一数据源原则，避免状态重复
 */
data class TransactionFormState(
    // 基础数据列表
    val accounts: List<Account> = emptyList(),
    val ledgers: List<Ledger> = emptyList(),
    val categoryGroups: List<CategoryGroup> = emptyList(),
    val frequentCategories: List<Category> = emptyList(),

    // 交易类型和选择项
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val selectedLedger: Ledger? = null,
    val selectedAccount: Account? = null,      // 普通交易的账户
    val fromAccount: Account? = null,          // 转账：转出账户
    val toAccount: Account? = null,            // 转账：转入账户
    val selectedCategoryInfo: SelectedCategoryInfo? = null,

    // 金额相关
    val amountText: String = "",
    val evaluatedAmount: Double? = null,       // 表达式求值结果
    val amountError: String? = null,

    // 其他字段
    val note: String = "",
    val selectedDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val selectedTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,
    val selectedLocation: LocationData? = null,

    // 对话框显示状态
    val showCategoryPicker: Boolean = false,
    val showAccountPicker: Boolean = false,    // 普通交易的账户选择器
    val showLedgerSelector: Boolean = false,
    val showDateTimePicker: Boolean = false,
    val showFromAccountPicker: Boolean = false,
    val showToAccountPicker: Boolean = false,
    val showLinkTargetSelector: Boolean = false,

    // 联动功能相关
    val availableLinkTargets: List<Ledger> = emptyList(),
    val selectedSyncTargets: Set<String> = emptySet(),
    val hasLinkOptions: Boolean = false,

    // 编辑模式标识
    val editingTransactionId: String? = null,

    // 时间记录设置
    val enableTimeRecording: Boolean = false,

    // 状态标识
    val saveSuccess: Boolean = false
) {
    /**
     * 便捷属性：是否为收入类型
     */
    val isIncome: Boolean
        get() = transactionType == TransactionType.INCOME

    /**
     * 便捷属性：是否为转账类型
     */
    val isTransfer: Boolean
        get() = transactionType == TransactionType.TRANSFER

    /**
     * 检查表单是否有效
     */
    val isValid: Boolean
        get() {
            // 基础验证
            if (amountText.isBlank() || evaluatedAmount == null || evaluatedAmount <= 0) {
                return false
            }

            if (selectedLedger == null) {
                return false
            }

            // 根据交易类型验证
            return when (transactionType) {
                TransactionType.TRANSFER -> {
                    // 转账：需要转出和转入账户
                    fromAccount != null && toAccount != null
                }
                else -> {
                    // 普通交易：需要账户和分类
                    selectedAccount != null && selectedCategoryInfo != null
                }
            }
        }

    /**
     * 创建快照用于脏值检测
     */
    fun createSnapshot(): TransactionSnapshot {
        return TransactionSnapshot(
            type = transactionType,
            ledgerId = selectedLedger?.id,
            accountId = selectedAccount?.id,
            fromAccountId = fromAccount?.id,
            toAccountId = toAccount?.id,
            categoryId = selectedCategoryInfo?.categoryId,
            amountText = amountText,
            date = selectedDate,
            time = selectedTime,
            note = note,
            selectedTargets = selectedSyncTargets
        )
    }
}

/**
 * 交易表单快照，用于脏值检测
 */
data class TransactionSnapshot(
    val type: TransactionType,
    val ledgerId: String?,
    val accountId: String?,
    val fromAccountId: String?,
    val toAccountId: String?,
    val categoryId: String?,
    val amountText: String,
    val date: LocalDate,
    val time: LocalTime,
    val note: String,
    val selectedTargets: Set<String>
)
