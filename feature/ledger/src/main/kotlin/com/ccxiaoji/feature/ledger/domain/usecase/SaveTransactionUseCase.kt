package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.LocationData
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject

/**
 * 保存交易参数
 */
data class SaveTransactionParams(
    val isEditMode: Boolean,
    val transactionId: String?,
    val transactionType: String, // "INCOME", "EXPENSE", "TRANSFER"
    val selectedLedgerId: String?,
    val selectedAccountId: String?,
    val fromAccountId: String?,
    val toAccountId: String?,
    val selectedCategoryInfo: SelectedCategoryInfo?,
    val amountCents: Int,
    val note: String?,
    val transactionInstant: Instant,
    val location: LocationData?,
    val selectedSyncTargets: Set<String>
)

/**
 * 保存错误焦点，用于UI层定位错误
 */
enum class SaveErrorFocus {
    LEDGER,          // 账本未选择
    CATEGORY,        // 分类未选择
    ACCOUNT,         // 账户未选择
    FROM_ACCOUNT,    // 转出账户未选择
    TO_ACCOUNT,      // 转入账户未选择
    AMOUNT          // 金额错误
}

/**
 * 保存交易结果
 */
sealed class SaveTransactionResult {
    object Success : SaveTransactionResult()

    data class ValidationError(
        val message: String,
        val focus: SaveErrorFocus
    ) : SaveTransactionResult()

    data class Error(
        val exception: Throwable
    ) : SaveTransactionResult()
}

/**
 * 保存交易UseCase
 *
 * 负责处理交易的创建、编辑、转账等保存逻辑。
 * 包含完整的验证、业务规则检查和数据持久化。
 */
class SaveTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val createTransferUseCase: CreateTransferUseCase,
    private val createLinkedTransactionUseCase: CreateLinkedTransactionUseCase
) {

    suspend operator fun invoke(params: SaveTransactionParams): SaveTransactionResult {
        // 基础验证
        if (params.amountCents <= 0) {
            return SaveTransactionResult.ValidationError(
                message = "金额必须大于0",
                focus = SaveErrorFocus.AMOUNT
            )
        }

        if (params.selectedLedgerId == null) {
            return SaveTransactionResult.ValidationError(
                message = "请选择账本",
                focus = SaveErrorFocus.LEDGER
            )
        }

        return try {
            when (params.transactionType) {
                "TRANSFER" -> handleTransfer(params)
                else -> handleNormalTransaction(params)
            }
        } catch (e: Exception) {
            SaveTransactionResult.Error(e)
        }
    }

    /**
     * 处理普通交易（收入/支出）
     */
    private suspend fun handleNormalTransaction(
        params: SaveTransactionParams
    ): SaveTransactionResult {
        // 验证必填字段
        if (params.selectedAccountId == null) {
            return SaveTransactionResult.ValidationError(
                message = "请选择账户",
                focus = SaveErrorFocus.ACCOUNT
            )
        }

        if (params.selectedCategoryInfo == null) {
            return SaveTransactionResult.ValidationError(
                message = "请选择分类",
                focus = SaveErrorFocus.CATEGORY
            )
        }

        val now = Clock.System.now()

        // 编辑模式：更新现有交易
        if (params.isEditMode && params.transactionId != null) {
            val updated = Transaction(
                id = params.transactionId,
                accountId = params.selectedAccountId,
                amountCents = params.amountCents,
                categoryId = params.selectedCategoryInfo.categoryId,
                categoryDetails = null, // 会在repository中重新加载
                note = params.note,
                ledgerId = params.selectedLedgerId!!,
                createdAt = params.transactionInstant,
                updatedAt = now,
                transactionDate = params.transactionInstant,
                location = params.location
            )

            transactionRepository.updateTransaction(updated)
            return SaveTransactionResult.Success
        }

        // 新建模式：创建新交易
        // 检查是否需要联动同步
        val result = if (params.selectedSyncTargets.isNotEmpty()) {
            // 指定了同步目标，创建联动交易
            createLinkedTransactionUseCase.createLinkedTransaction(
                primaryLedgerId = params.selectedLedgerId!!,
                accountId = params.selectedAccountId,
                amountCents = params.amountCents,
                categoryId = params.selectedCategoryInfo.categoryId,
                note = params.note,
                transactionDate = params.transactionInstant,
                location = params.location,
                autoSync = false,
                specificTargetLedgers = params.selectedSyncTargets.toList()
            )
        } else {
            // 没有指定同步目标，检查是否自动同步
            createLinkedTransactionUseCase.createLinkedTransaction(
                primaryLedgerId = params.selectedLedgerId!!,
                accountId = params.selectedAccountId,
                amountCents = params.amountCents,
                categoryId = params.selectedCategoryInfo.categoryId,
                note = params.note,
                transactionDate = params.transactionInstant,
                location = params.location,
                autoSync = true,
                specificTargetLedgers = emptyList()
            )
        }

        return when (result) {
            is BaseResult.Success -> SaveTransactionResult.Success
            is BaseResult.Error -> SaveTransactionResult.Error(result.exception)
        }
    }

    /**
     * 处理转账交易
     */
    private suspend fun handleTransfer(
        params: SaveTransactionParams
    ): SaveTransactionResult {
        // 验证转账必填字段
        if (params.fromAccountId == null) {
            return SaveTransactionResult.ValidationError(
                message = "请选择转出账户",
                focus = SaveErrorFocus.FROM_ACCOUNT
            )
        }

        if (params.toAccountId == null) {
            return SaveTransactionResult.ValidationError(
                message = "请选择转入账户",
                focus = SaveErrorFocus.TO_ACCOUNT
            )
        }

        if (params.fromAccountId == params.toAccountId) {
            return SaveTransactionResult.ValidationError(
                message = "转出和转入账户不能相同",
                focus = SaveErrorFocus.FROM_ACCOUNT
            )
        }

        // 创建转账
        val result = createTransferUseCase.createTransfer(
            fromAccountId = params.fromAccountId,
            toAccountId = params.toAccountId,
            amountCents = params.amountCents,
            note = params.note,
            ledgerId = params.selectedLedgerId!!,
            transactionDate = params.transactionInstant,
            location = params.location,
            checkBalance = false // 不检查余额
        )

        return when (result) {
            is BaseResult.Success -> SaveTransactionResult.Success
            is BaseResult.Error -> SaveTransactionResult.Error(result.exception)
        }
    }
}