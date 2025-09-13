package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 转账专用分类ID常量
 */
private const val TRANSFER_CATEGORY_ID = "TRANSFER_CATEGORY"

/**
 * 创建转账交易用例
 * 
 * 转账功能实现核心逻辑：
 * 1. 验证转出和转入账户的有效性
 * 2. 检查转出账户余额是否充足（可选）
 * 3. 创建两笔关联的转账记录
 * 4. 更新账户余额
 */
@Singleton
class CreateTransferUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository,
    private val userApi: com.ccxiaoji.shared.user.api.UserApi
) {
    
    /**
     * 创建转账交易
     * 
     * @param fromAccountId 转出账户ID
     * @param toAccountId 转入账户ID
     * @param amountCents 转账金额（分）
     * @param note 转账备注
     * @param ledgerId 记账簿ID
     * @param transactionDate 交易发生时间
     * @param location 交易发生地点（可选）
     * @param checkBalance 是否检查余额，默认为false
     * @return 转账结果
     */
    suspend fun createTransfer(
        fromAccountId: String,
        toAccountId: String,
        amountCents: Int,
        note: String?,
        ledgerId: String,
        transactionDate: Instant,
        location: LocationData? = null,
        checkBalance: Boolean = false
    ): BaseResult<TransferResult> {
        return withContext(Dispatchers.IO) {
            try {
                println("🔄 [CreateTransfer] 开始创建转账")
                println("  - 转出账户: $fromAccountId")
                println("  - 转入账户: $toAccountId")
                println("  - 金额: ${amountCents}分")
                
                // 1. 验证输入参数
                if (fromAccountId == toAccountId) {
                    return@withContext BaseResult.Error(Exception("转出和转入账户不能相同"))
                }
                
                if (amountCents <= 0) {
                    return@withContext BaseResult.Error(Exception("转账金额必须大于0"))
                }
                
                // 2. 验证账户存在性
                println("🔍 [CreateTransfer] 验证账户")
                val fromAccount = accountRepository.getAccountById(fromAccountId)
                    ?: return@withContext BaseResult.Error(Exception("转出账户不存在"))
                    
                val toAccount = accountRepository.getAccountById(toAccountId)
                    ?: return@withContext BaseResult.Error(Exception("转入账户不存在"))
                
                println("  - 转出账户: ${fromAccount.name}")
                println("  - 转入账户: ${toAccount.name}")
                
                // 3. 可选的余额检查
                if (checkBalance && fromAccount.balanceCents < amountCents) {
                    return@withContext BaseResult.Error(
                        Exception("账户余额不足，当前余额: ${fromAccount.balanceYuan}元，需要: ${amountCents / 100.0}元")
                    )
                }
                
                // 4. 选择分类（暂用兜底：优先“转账*”，否则选择各自类型的“其他”或第一个父分类）
                suspend fun fallbackCategoryId(type: String): String {
                    val userId = userApi.getCurrentUserId()
                    // 优先找包含“转账”字样的父分类
                    val parents = categoryRepository.getParentCategories(userId, type)
                    val transferLike = parents.firstOrNull { it.name.contains("转账") }
                    if (transferLike != null) return transferLike.id
                    // 其次“其他/其它/Other”
                    val other = parents.firstOrNull { 
                        val n = it.name.trim()
                        n.contains("其他") || n.contains("其它") || n.equals("Other", ignoreCase = true)
                    }
                    if (other != null) return other.id
                    // 最后回退第一个父分类
                    return parents.firstOrNull()?.id ?: throw IllegalStateException("找不到可用的$type 分类")
                }

                val outCategoryId = fallbackCategoryId("EXPENSE")
                val inCategoryId = fallbackCategoryId("INCOME")

                // 5. 生成转账批次ID和交易ID
                val transferId = UUID.randomUUID().toString()
                val transferOutId = UUID.randomUUID().toString()
                val transferInId = UUID.randomUUID().toString()
                val currentTime = Clock.System.now()
                
                println("🔑 [CreateTransfer] 生成ID")
                println("  - 转账批次ID: $transferId")
                println("  - 转出记录ID: $transferOutId")
                println("  - 转入记录ID: $transferInId")
                
                // 6. 创建转出交易记录（使用正数金额 + 支出类分类）
                val transferOutNote = note?.let { "转账给${toAccount.name}: $it" } 
                    ?: "转账给${toAccount.name}"
                
                println("💸 [CreateTransfer] 创建转出记录")
                val transferOutResult = transactionRepository.addTransaction(
                    amountCents = amountCents, // 使用正数金额，方向由分类类型控制
                    categoryId = outCategoryId, // 暂用支出类兜底分类
                    note = transferOutNote,
                    accountId = fromAccountId,
                    ledgerId = ledgerId,
                    transactionDate = transactionDate,
                    location = location,
                    transactionId = transferOutId
                )
                
                when (transferOutResult) {
                    is BaseResult.Error -> {
                        println("❌ [CreateTransfer] 转出记录创建失败: ${transferOutResult.exception.message}")
                        return@withContext BaseResult.Error(
                            Exception("创建转出记录失败: ${transferOutResult.exception.message}")
                        )
                    }
                    is BaseResult.Success -> {
                        println("✅ [CreateTransfer] 转出记录创建成功")
                    }
                }
                
                // 7. 创建转入交易记录（使用正数金额 + 收入类分类）
                val transferInNote = note?.let { "从${fromAccount.name}转入: $it" } 
                    ?: "从${fromAccount.name}转入"
                
                println("💰 [CreateTransfer] 创建转入记录")
                val transferInResult = transactionRepository.addTransaction(
                    amountCents = amountCents, // 正数金额
                    categoryId = inCategoryId, // 暂用收入类兜底分类
                    note = transferInNote,
                    accountId = toAccountId,
                    ledgerId = ledgerId,
                    transactionDate = transactionDate,
                    location = location,
                    transactionId = transferInId
                )
                
                when (transferInResult) {
                    is BaseResult.Error -> {
                        println("❌ [CreateTransfer] 转入记录创建失败: ${transferInResult.exception.message}")
                        // 回滚已创建的转出记录
                        try {
                            transactionRepository.deleteTransaction(transferOutId)
                        } catch (_: Exception) { }
                        return@withContext BaseResult.Error(
                            Exception("创建转入记录失败: ${transferInResult.exception.message}")
                        )
                    }
                    is BaseResult.Success -> {
                        println("✅ [CreateTransfer] 转入记录创建成功")
                    }
                }
                
                // 8. 更新转账记录的关联信息（通过 updateTransaction 回填元信息）
                println("🔗 [CreateTransfer] 更新转账关联信息")
                val out = transactionRepository.getTransactionById(transferOutId)
                val `in` = transactionRepository.getTransactionById(transferInId)
                if (out == null || `in` == null) {
                    println("❌ [CreateTransfer] 无法读取刚创建的交易用于回填转账信息")
                    return@withContext BaseResult.Error(Exception("转账关联失败：读取交易失败"))
                }
                val outUpdated = out.copy(
                    transferId = transferId,
                    transferType = TransferType.TRANSFER_OUT,
                    relatedTransactionId = transferInId
                )
                val inUpdated = `in`.copy(
                    transferId = transferId,
                    transferType = TransferType.TRANSFER_IN,
                    relatedTransactionId = transferOutId
                )
                when (val u1 = transactionRepository.updateTransaction(outUpdated)) {
                    is BaseResult.Error -> return@withContext BaseResult.Error(u1.exception)
                    else -> {}
                }
                when (val u2 = transactionRepository.updateTransaction(inUpdated)) {
                    is BaseResult.Error -> return@withContext BaseResult.Error(u2.exception)
                    else -> {}
                }
                
                // 9. 构造转账结果
                val result = TransferResult(
                    transferId = transferId,
                    transferOutTransactionId = transferOutId,
                    transferInTransactionId = transferInId,
                    fromAccount = fromAccount,
                    toAccount = toAccount,
                    amountCents = amountCents,
                    transferDate = transactionDate
                )
                
                println("✅ [CreateTransfer] 转账创建完成")
                BaseResult.Success(result)
                
            } catch (e: Exception) {
                println("💥 [CreateTransfer] 转账创建失败: ${e.message}")
                BaseResult.Error(e)
            }
        }
    }
}

/**
 * 转账结果数据类
 */
data class TransferResult(
    val transferId: String,              // 转账批次ID
    val transferOutTransactionId: String, // 转出记录ID
    val transferInTransactionId: String,  // 转入记录ID
    val fromAccount: Account,            // 转出账户信息
    val toAccount: Account,              // 转入账户信息
    val amountCents: Int,                // 转账金额（分）
    val transferDate: Instant            // 转账时间
) {
    val amountYuan: Double
        get() = amountCents / 100.0
}
