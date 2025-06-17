package com.ccxiaoji.app.data.local.dao

import androidx.room.*
import com.ccxiaoji.app.data.local.entity.CreditCardPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardPaymentDao {
    @Insert
    suspend fun insert(payment: CreditCardPaymentEntity)

    @Update
    suspend fun update(payment: CreditCardPaymentEntity)

    @Query("UPDATE credit_card_payments SET isDeleted = 1, updatedAt = :now WHERE id = :paymentId")
    suspend fun softDelete(paymentId: String, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM credit_card_payments WHERE userId = :userId AND accountId = :accountId AND isDeleted = 0 ORDER BY paymentDate DESC")
    fun getPaymentsByAccount(userId: String, accountId: String): Flow<List<CreditCardPaymentEntity>>

    @Query("SELECT * FROM credit_card_payments WHERE userId = :userId AND isDeleted = 0 ORDER BY paymentDate DESC")
    fun getAllPayments(userId: String): Flow<List<CreditCardPaymentEntity>>

    @Query("SELECT * FROM credit_card_payments WHERE userId = :userId AND paymentDate >= :startTime AND paymentDate < :endTime AND isDeleted = 0 ORDER BY paymentDate DESC")
    fun getPaymentsByDateRange(userId: String, startTime: Long, endTime: Long): Flow<List<CreditCardPaymentEntity>>

    @Query("SELECT * FROM credit_card_payments WHERE id = :paymentId AND isDeleted = 0")
    suspend fun getPaymentById(paymentId: String): CreditCardPaymentEntity?

    @Query("SELECT COUNT(*) FROM credit_card_payments WHERE userId = :userId AND accountId = :accountId AND isOnTime = 1 AND isDeleted = 0")
    suspend fun getOnTimePaymentCount(userId: String, accountId: String): Int

    @Query("SELECT COUNT(*) FROM credit_card_payments WHERE userId = :userId AND accountId = :accountId AND isDeleted = 0")
    suspend fun getTotalPaymentCount(userId: String, accountId: String): Int

    @Query("SELECT SUM(paymentAmountCents) FROM credit_card_payments WHERE userId = :userId AND accountId = :accountId AND isDeleted = 0")
    suspend fun getTotalPaymentAmount(userId: String, accountId: String): Long?

    @Query("DELETE FROM credit_card_payments WHERE id = :paymentId")
    suspend fun deletePayment(paymentId: String)
}