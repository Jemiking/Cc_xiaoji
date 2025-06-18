package com.ccxiaoji.feature.ledger.data.local.dao

import androidx.room.*
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE userId = :userId AND isDeleted = 0 ORDER BY isDefault DESC, createdAt DESC")
    fun getAccountsByUser(userId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :accountId AND isDeleted = 0")
    suspend fun getAccountById(accountId: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE userId = :userId AND isDefault = 1 AND isDeleted = 0 LIMIT 1")
    suspend fun getDefaultAccount(userId: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET isDeleted = 1, updatedAt = :timestamp WHERE id = :accountId")
    suspend fun softDeleteAccount(accountId: String, timestamp: Long)

    @Query("UPDATE accounts SET balanceCents = balanceCents + :amountCents, updatedAt = :timestamp WHERE id = :accountId")
    suspend fun updateBalance(accountId: String, amountCents: Long, timestamp: Long)

    @Query("UPDATE accounts SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultAccounts(userId: String)
    
    @Query("UPDATE accounts SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultStatus(userId: String)
    
    @Query("SELECT SUM(balanceCents) FROM accounts WHERE userId = :userId AND isDeleted = 0")
    suspend fun getTotalBalance(userId: String): Long?

    @Transaction
    suspend fun setDefaultAccount(accountId: String, userId: String, timestamp: Long) {
        clearDefaultAccounts(userId)
        val account = getAccountById(accountId)
        account?.let {
            updateAccount(it.copy(isDefault = true, updatedAt = timestamp))
        }
    }
    
    @Query("SELECT * FROM accounts WHERE id = :accountId AND isDeleted = 0")
    fun getAccountByIdSync(accountId: String): AccountEntity?
    
    // Credit card specific queries
    @Query("SELECT * FROM accounts WHERE userId = :userId AND type = 'CREDIT_CARD' AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getCreditCardAccounts(userId: String): Flow<List<AccountEntity>>
    
    @Query("SELECT * FROM accounts WHERE userId = :userId AND type = 'CREDIT_CARD' AND billingDay = :dayOfMonth AND isDeleted = 0")
    suspend fun getCreditCardsWithBillingDay(userId: String, dayOfMonth: Int): List<AccountEntity>
    
    @Query("SELECT * FROM accounts WHERE userId = :userId AND type = 'CREDIT_CARD' AND paymentDueDay = :dayOfMonth AND isDeleted = 0")
    suspend fun getCreditCardsWithPaymentDueDay(userId: String, dayOfMonth: Int): List<AccountEntity>
    
    @Query("SELECT * FROM accounts WHERE userId = :userId AND type = 'CREDIT_CARD' AND balanceCents < 0 AND isDeleted = 0")
    suspend fun getCreditCardsWithDebt(userId: String): List<AccountEntity>
    
    @Query("UPDATE accounts SET creditLimitCents = :creditLimitCents, billingDay = :billingDay, paymentDueDay = :paymentDueDay, gracePeriodDays = :gracePeriodDays, updatedAt = :timestamp WHERE id = :accountId")
    suspend fun updateCreditCardInfo(
        accountId: String,
        creditLimitCents: Long,
        billingDay: Int,
        paymentDueDay: Int,
        gracePeriodDays: Int,
        timestamp: Long
    )
}