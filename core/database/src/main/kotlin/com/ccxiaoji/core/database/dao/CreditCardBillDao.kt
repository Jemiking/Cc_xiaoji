package com.ccxiaoji.core.database.dao

import androidx.room.*
import com.ccxiaoji.core.database.entity.CreditCardBillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardBillDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: CreditCardBillEntity)
    
    @Update
    suspend fun update(bill: CreditCardBillEntity)
    
    @Query("UPDATE credit_card_bills SET isDeleted = 1, updatedAt = :timestamp WHERE id = :billId")
    suspend fun softDelete(billId: String, timestamp: Long)
    
    @Query("DELETE FROM credit_card_bills WHERE id = :billId")
    suspend fun deleteBill(billId: String)
    
    // 查询单个账单
    @Query("SELECT * FROM credit_card_bills WHERE id = :billId AND isDeleted = 0")
    suspend fun getBillById(billId: String): CreditCardBillEntity?
    
    // 查询某个信用卡的所有账单
    @Query("""
        SELECT * FROM credit_card_bills 
        WHERE accountId = :accountId AND isDeleted = 0
        ORDER BY billEndDate DESC
    """)
    fun getBillsByAccount(accountId: String): Flow<List<CreditCardBillEntity>>
    
    // 查询某个信用卡的当前账单（最新的已生成账单）
    @Query("""
        SELECT * FROM credit_card_bills 
        WHERE accountId = :accountId AND isGenerated = 1 AND isDeleted = 0
        ORDER BY billEndDate DESC
        LIMIT 1
    """)
    suspend fun getCurrentBill(accountId: String): CreditCardBillEntity?
    
    // 查询某个信用卡的未还清账单
    @Query("""
        SELECT * FROM credit_card_bills 
        WHERE accountId = :accountId AND isPaid = 0 AND isDeleted = 0
        ORDER BY paymentDueDate ASC
    """)
    fun getUnpaidBills(accountId: String): Flow<List<CreditCardBillEntity>>
    
    // 查询日期范围内的账单
    @Query("""
        SELECT * FROM credit_card_bills 
        WHERE accountId = :accountId 
        AND billStartDate >= :startDate 
        AND billEndDate <= :endDate 
        AND isDeleted = 0
        ORDER BY billEndDate DESC
    """)
    suspend fun getBillsByDateRange(
        accountId: String,
        startDate: Long,
        endDate: Long
    ): List<CreditCardBillEntity>
    
    // 更新账单支付状态
    @Query("""
        UPDATE credit_card_bills 
        SET paidAmountCents = paidAmountCents + :paymentAmountCents,
            isPaid = CASE WHEN paidAmountCents + :paymentAmountCents >= totalAmountCents THEN 1 ELSE 0 END,
            updatedAt = :timestamp
        WHERE id = :billId
    """)
    suspend fun updatePaymentStatus(
        billId: String, 
        paymentAmountCents: Long,
        timestamp: Long
    )
    
    // 标记账单为逾期
    @Query("""
        UPDATE credit_card_bills 
        SET isOverdue = 1, updatedAt = :timestamp
        WHERE accountId = :accountId 
        AND isPaid = 0 
        AND paymentDueDate < :currentDate
        AND isDeleted = 0
    """)
    suspend fun markOverdueBills(accountId: String, currentDate: Long, timestamp: Long)
    
    // 获取账单统计信息
    @Query("""
        SELECT COUNT(*) as totalBills,
               SUM(CASE WHEN isPaid = 1 THEN 1 ELSE 0 END) as paidBills,
               SUM(CASE WHEN isOverdue = 1 THEN 1 ELSE 0 END) as overdueBills,
               SUM(totalAmountCents) as totalAmountCents,
               SUM(paidAmountCents) as totalPaidCents
        FROM credit_card_bills
        WHERE accountId = :accountId AND isDeleted = 0
    """)
    suspend fun getBillStatistics(accountId: String): BillStatistics?
    
    // 检查是否存在某个周期的账单
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM credit_card_bills 
            WHERE accountId = :accountId 
            AND billStartDate = :startDate 
            AND billEndDate = :endDate
            AND isDeleted = 0
        )
    """)
    suspend fun hasBillForPeriod(accountId: String, startDate: Long, endDate: Long): Boolean
}

// 账单统计数据类
data class BillStatistics(
    val totalBills: Int,
    val paidBills: Int,
    val overdueBills: Int,
    val totalAmountCents: Long,
    val totalPaidCents: Long
)