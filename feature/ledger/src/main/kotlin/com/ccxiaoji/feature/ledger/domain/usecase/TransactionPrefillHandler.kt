package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionType
import kotlinx.datetime.*
import javax.inject.Inject

/**
 * 交易预填充来源
 *
 * 封装两种预填充场景：DeepLink参数预填充和编辑模式数据加载
 */
sealed class PrefillSource {
    /**
     * DeepLink参数预填充（自动记账场景）
     *
     * @param amountCents 金额（分，Int.MIN_VALUE表示未提供）
     * @param direction 交易方向（INCOME/EXPENSE）
     * @param merchant 商户名称
     * @param categoryId 分类ID
     * @param note 备注
     */
    data class DeepLink(
        val amountCents: Int?,
        val direction: String?,
        val merchant: String?,
        val categoryId: String?,
        val note: String?
    ) : PrefillSource()

    /**
     * 编辑模式数据加载
     *
     * @param transactionId 要编辑的交易记录ID
     */
    data class EditMode(
        val transactionId: String
    ) : PrefillSource()
}

/**
 * 交易预填充结果
 *
 * 包含所有预填充后的表单字段
 */
data class PrefillResult(
    /** 交易类型 */
    val transactionType: TransactionType? = null,
    /** 金额文本（用户输入格式） */
    val amountText: String? = null,
    /** 计算后的金额 */
    val evaluatedAmount: Double? = null,
    /** 备注 */
    val note: String? = null,
    /** 选中的分类信息 */
    val selectedCategoryInfo: SelectedCategoryInfo? = null,
    /** 选中的日期（仅编辑模式） */
    val selectedDate: LocalDate? = null,
    /** 选中的时间（仅编辑模式） */
    val selectedTime: LocalTime? = null,
    /** 正在编辑的交易ID（仅编辑模式） */
    val editingTransactionId: String? = null,
    /** 账户ID（仅编辑模式） */
    val accountId: String? = null
)

/**
 * 交易预填充处理器
 *
 * 职责：
 * - 处理DeepLink参数预填充（自动记账场景）
 * - 处理编辑模式数据加载
 * - 统一的错误处理和数据转换
 *
 * 使用场景：
 * ```kotlin
 * // DeepLink预填充
 * val result = transactionPrefillHandler(
 *     PrefillSource.DeepLink(
 *         amountCents = 100,
 *         direction = "EXPENSE",
 *         merchant = "星巴克",
 *         categoryId = "category_id",
 *         note = "咖啡"
 *     )
 * )
 *
 * // 编辑模式加载
 * val result = transactionPrefillHandler(
 *     PrefillSource.EditMode(transactionId = "transaction_id")
 * )
 * ```
 */
class TransactionPrefillHandler @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val evaluateAmountExpression: EvaluateAmountExpressionUseCase
) {

    /**
     * 执行预填充处理
     *
     * @param source 预填充来源
     * @return 成功时返回预填充结果，失败时返回异常
     */
    suspend operator fun invoke(source: PrefillSource): Result<PrefillResult> = runCatching {
        when (source) {
            is PrefillSource.DeepLink -> handleDeepLinkPrefill(source)
            is PrefillSource.EditMode -> handleEditModePrefill(source)
        }
    }

    /**
     * 处理DeepLink参数预填充
     *
     * 逻辑来源：AddTransactionViewModel.applyAutoLedgerPrefill()
     */
    private suspend fun handleDeepLinkPrefill(source: PrefillSource.DeepLink): PrefillResult {
        // 解析交易类型
        val transactionType = source.direction?.let { dir ->
            if (dir.equals("INCOME", ignoreCase = true)) {
                TransactionType.INCOME
            } else {
                TransactionType.EXPENSE
            }
        }

        // 转换金额：分 -> 元（格式化为字符串）
        val amountText = source.amountCents
            ?.takeIf { it != Int.MIN_VALUE && it > 0 }
            ?.let { cents ->
                val yuan = cents.toDouble() / 100.0
                // 整数显示为整数，小数保留两位
                if (yuan % 1.0 == 0.0) {
                    yuan.toInt().toString()
                } else {
                    String.format(java.util.Locale.getDefault(), "%.2f", yuan)
                }
            }

        // 计算金额表达式
        val evaluatedAmount = amountText?.let { text ->
            evaluateAmountExpression(text).evaluatedAmount
        }

        // 获取分类信息
        val selectedCategoryInfo = source.categoryId?.let { cid ->
            try {
                categoryRepository.getCategoryFullInfo(cid)
            } catch (e: Exception) {
                null // 分类不存在时忽略
            }
        }

        // 备注：仅在明确提供时设置
        val note = source.note?.takeIf { it.isNotBlank() }

        return PrefillResult(
            transactionType = transactionType,
            amountText = amountText,
            evaluatedAmount = evaluatedAmount,
            note = note,
            selectedCategoryInfo = selectedCategoryInfo
        )
    }

    /**
     * 处理编辑模式数据加载
     *
     * 逻辑来源：AddTransactionViewModel.loadTransactionForEdit()
     */
    private suspend fun handleEditModePrefill(source: PrefillSource.EditMode): PrefillResult {
        // 获取交易记录
        val transaction = transactionRepository.getTransactionById(source.transactionId)
            ?: throw IllegalArgumentException("交易记录不存在: ${source.transactionId}")

        // 获取分类信息
        val category = categoryRepository.getCategoryById(transaction.categoryId)
        val selectedCategoryInfo = category?.let { cat ->
            SelectedCategoryInfo(
                categoryId = cat.id,
                categoryName = cat.name,
                parentId = cat.parentId,
                parentName = cat.parentId?.let { parentId ->
                    categoryRepository.getCategoryById(parentId)?.name
                },
                fullPath = if (cat.parentId != null) {
                    val parentName = categoryRepository.getCategoryById(cat.parentId!!)?.name ?: ""
                    "$parentName/${cat.name}"
                } else {
                    cat.name
                },
                icon = cat.icon,
                color = cat.color
            )
        }

        // 解析交易类型
        val transactionType = when (transaction.categoryDetails?.type) {
            "INCOME" -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

        // 金额文本和计算值
        val amountText = transaction.amountYuan.toString()
        val amountResult = evaluateAmountExpression(amountText)

        // 提取日期时间
        val transactionDateTime = transaction.transactionDate
            ?.toLocalDateTime(TimeZone.currentSystemDefault())
            ?: transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())

        return PrefillResult(
            transactionType = transactionType,
            amountText = amountText,
            evaluatedAmount = amountResult.evaluatedAmount,
            note = transaction.note,
            selectedCategoryInfo = selectedCategoryInfo,
            selectedDate = transactionDateTime.date,
            selectedTime = transactionDateTime.time,
            editingTransactionId = source.transactionId,
            accountId = transaction.accountId
        )
    }
}
