package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.BillStatus
import com.ccxiaoji.feature.ledger.domain.model.CreditCardBill
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * 信用卡账单仓库接口
 */
interface CreditCardBillRepository {
    /**
     * 生成账单
     * @param accountId 信用卡账户ID
     * @param periodStart 账单周期开始日期
     * @param periodEnd 账单周期结束日期
     * @return 生成的账单
     */
    suspend fun generateBill(
        accountId: String,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): BaseResult<CreditCardBill>
    
    /**
     * 按账户查询账单列表
     * @param accountId 信用卡账户ID
     * @return 账单列表流
     */
    fun getBillsByAccount(accountId: String): Flow<List<CreditCardBill>>
    
    /**
     * 查询账单详情
     * @param billId 账单ID
     * @return 账单详情
     */
    suspend fun getBillById(billId: String): BaseResult<CreditCardBill>
    
    /**
     * 更新账单状态
     * @param billId 账单ID
     * @param status 账单状态
     * @return 操作结果
     */
    suspend fun updateBillStatus(
        billId: String,
        status: BillStatus
    ): BaseResult<Unit>
    
    /**
     * 记录还款
     * @param billId 账单ID
     * @param amount 还款金额（分）
     * @return 操作结果
     */
    suspend fun recordPayment(
        billId: String,
        amount: Int
    ): BaseResult<Unit>
    
    /**
     * 获取待还款账单
     * @return 待还款账单列表流
     */
    fun getPendingBills(): Flow<List<CreditCardBill>>
    
    /**
     * 标记逾期账单
     * @return 标记的账单数量
     */
    suspend fun markOverdueBills(): BaseResult<Int>
    
    /**
     * 查询某个信用卡的当前账单（最新的已生成账单）
     * @param accountId 信用卡账户ID
     * @return 当前账单
     */
    suspend fun getCurrentBill(accountId: String): BaseResult<CreditCardBill?>
    
    /**
     * 检查是否存在某个周期的账单
     * @param accountId 信用卡账户ID
     * @param periodStart 账单周期开始日期
     * @param periodEnd 账单周期结束日期
     * @return 是否存在
     */
    suspend fun hasBillForPeriod(
        accountId: String,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): BaseResult<Boolean>
}