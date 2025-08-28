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
    private val accountRepository: AccountRepository
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
                
                // 4. 生成转账批次ID和交易ID
                val transferId = UUID.randomUUID().toString()
                val transferOutId = UUID.randomUUID().toString()
                val transferInId = UUID.randomUUID().toString()
                val currentTime = Clock.System.now()
                
                println("🔑 [CreateTransfer] 生成ID")
                println("  - 转账批次ID: $transferId")
                println("  - 转出记录ID: $transferOutId")
                println("  - 转入记录ID: $transferInId")
                
                // 5. 创建转出交易记录
                val transferOutNote = note?.let { "转账给${toAccount.name}: $it" } 
                    ?: "转账给${toAccount.name}"
                
                println("💸 [CreateTransfer] 创建转出记录")
                val transferOutResult = transactionRepository.addTransaction(
                    amountCents = -amountCents, // 负数表示支出
                    categoryId = TRANSFER_CATEGORY_ID, // 使用转账专用分类
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
                
                // 6. 创建转入交易记录
                val transferInNote = note?.let { "从${fromAccount.name}转入: $it" } 
                    ?: "从${fromAccount.name}转入"
                
                println("💰 [CreateTransfer] 创建转入记录")
                val transferInResult = transactionRepository.addTransaction(
                    amountCents = amountCents, // 正数表示收入
                    categoryId = TRANSFER_CATEGORY_ID, // 使用转账专用分类
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
                        // TODO: 这里应该回滚转出记录，但当前Repository没有事务支持
                        return@withContext BaseResult.Error(
                            Exception("创建转入记录失败: ${transferInResult.exception.message}")
                        )
                    }
                    is BaseResult.Success -> {
                        println("✅ [CreateTransfer] 转入记录创建成功")
                    }
                }
                
                // 7. 更新转账记录的关联信息（需要Repository层支持）
                // 注意：当前Repository的addTransaction不支持转账字段
                // 这里先创建成功，后续需要通过updateTransaction来添加转账字段
                
                println("🔗 [CreateTransfer] 更新转账关联信息")
                // 这里需要获取刚创建的Transaction并更新转账字段
                // 由于当前架构限制，暂时跳过，在后续版本中完善
                
                // 8. 构造转账结果
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