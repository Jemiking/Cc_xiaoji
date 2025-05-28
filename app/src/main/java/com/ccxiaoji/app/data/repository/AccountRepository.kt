package com.ccxiaoji.app.data.repository

import com.ccxiaoji.app.data.local.dao.AccountDao
import com.ccxiaoji.app.data.local.dao.ChangeLogDao
import com.ccxiaoji.app.data.local.entity.AccountEntity
import com.ccxiaoji.app.data.local.entity.ChangeLogEntity
import com.ccxiaoji.app.data.sync.SyncStatus
import com.ccxiaoji.app.domain.model.Account
import com.ccxiaoji.app.domain.model.AccountType
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val changeLogDao: ChangeLogDao,
    private val gson: Gson
) {
    fun getAccounts(): Flow<List<Account>> {
        return accountDao.getAccountsByUser(getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    suspend fun getAccountById(accountId: String): Account? {
        return accountDao.getAccountById(accountId)?.toDomainModel()
    }
    
    suspend fun getDefaultAccount(): Account? {
        return accountDao.getDefaultAccount(getCurrentUserId())?.toDomainModel()
    }
    
    suspend fun getTotalBalance(): Double {
        val totalCents = accountDao.getTotalBalance(getCurrentUserId()) ?: 0L
        return totalCents / 100.0
    }
    
    suspend fun createAccount(
        name: String,
        type: AccountType,
        initialBalanceCents: Long = 0L,
        currency: String = "CNY",
        icon: String? = null,
        color: String? = null
    ): Account {
        val accountId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        
        val entity = AccountEntity(
            id = accountId,
            userId = getCurrentUserId(),
            name = name,
            type = type.name,
            balanceCents = initialBalanceCents,
            currency = currency,
            icon = icon,
            color = color,
            isDefault = false,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        accountDao.insertAccount(entity)
        
        // Log the change for sync
        logChange("accounts", accountId, "INSERT", entity)
        
        return entity.toDomainModel()
    }
    
    suspend fun updateAccount(account: Account) {
        val now = System.currentTimeMillis()
        val entity = account.toEntity(getCurrentUserId(), now)
        
        accountDao.updateAccount(entity)
        
        // Log the change for sync
        logChange("accounts", account.id, "UPDATE", entity)
    }
    
    suspend fun setDefaultAccount(accountId: String) {
        val now = System.currentTimeMillis()
        
        // Clear other defaults
        accountDao.clearDefaultStatus(getCurrentUserId())
        
        // Set new default
        accountDao.setDefaultAccount(accountId, getCurrentUserId(), now)
        
        // Log the change for sync
        logChange("accounts", accountId, "UPDATE", mapOf("isDefault" to true))
    }
    
    suspend fun updateBalance(accountId: String, amountCents: Long) {
        val now = System.currentTimeMillis()
        
        accountDao.updateBalance(accountId, amountCents, now)
        
        // Log the change for sync
        logChange("accounts", accountId, "UPDATE", mapOf("balanceChange" to amountCents))
    }
    
    suspend fun transferBetweenAccounts(
        fromAccountId: String,
        toAccountId: String,
        amountCents: Long
    ) {
        val now = System.currentTimeMillis()
        
        // Deduct from source account
        accountDao.updateBalance(fromAccountId, -amountCents, now)
        
        // Add to destination account
        accountDao.updateBalance(toAccountId, amountCents, now)
        
        // Log the changes for sync
        logChange("accounts", fromAccountId, "TRANSFER", mapOf(
            "from" to fromAccountId,
            "to" to toAccountId,
            "amount" to amountCents
        ))
    }
    
    suspend fun deleteAccount(accountId: String) {
        val now = System.currentTimeMillis()
        
        accountDao.softDeleteAccount(accountId, now)
        
        // Log the change for sync
        logChange("accounts", accountId, "DELETE", mapOf("id" to accountId))
    }
    
    private suspend fun logChange(table: String, rowId: String, operation: String, payload: Any) {
        val changeLog = ChangeLogEntity(
            tableName = table,
            rowId = rowId,
            operation = operation,
            payload = gson.toJson(payload),
            timestamp = System.currentTimeMillis()
        )
        changeLogDao.insertChange(changeLog)
    }
    
    private fun getCurrentUserId(): String {
        // In a real app, this would get the actual current user ID
        return "current_user_id"
    }
}

private fun AccountEntity.toDomainModel(): Account {
    return Account(
        id = id,
        name = name,
        type = AccountType.valueOf(type),
        balanceCents = balanceCents,
        currency = currency,
        icon = icon,
        color = color,
        isDefault = isDefault,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt)
    )
}

private fun Account.toEntity(userId: String, updatedAt: Long): AccountEntity {
    return AccountEntity(
        id = id,
        userId = userId,
        name = name,
        type = type.name,
        balanceCents = balanceCents,
        currency = currency,
        icon = icon,
        color = color,
        isDefault = isDefault,
        createdAt = createdAt.toEpochMilliseconds(),
        updatedAt = updatedAt,
        syncStatus = SyncStatus.PENDING_SYNC
    )
}