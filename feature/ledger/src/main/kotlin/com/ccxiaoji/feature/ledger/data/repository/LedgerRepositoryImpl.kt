package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.data.local.dao.LedgerDao
import com.ccxiaoji.feature.ledger.data.local.entity.LedgerEntity
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.model.LedgerWithStats
import com.ccxiaoji.feature.ledger.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 记账簿仓库实现
 */
@Singleton
class LedgerRepositoryImpl @Inject constructor(
    private val ledgerDao: LedgerDao
) : LedgerRepository {
    
    override fun getUserLedgers(userId: String): Flow<List<Ledger>> {
        return ledgerDao.getUserLedgers(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getUserLedgersWithStats(userId: String): Flow<List<LedgerWithStats>> {
        return ledgerDao.getUserLedgersWithStats(userId).map { entities ->
            entities.map { entity ->
                LedgerWithStats(
                    ledger = entity.ledger.toDomain(),
                    transactionCount = entity.transactionCount,
                    totalIncome = entity.totalIncome,
                    totalExpense = entity.totalExpense,
                    lastTransactionDate = entity.lastTransactionDate
                )
            }
        }
    }
    
    override suspend fun getDefaultLedger(userId: String): BaseResult<Ledger> {
        return try {
            val entity = ledgerDao.getDefaultLedger(userId)
            if (entity != null) {
                BaseResult.Success(entity.toDomain())
            } else {
                BaseResult.Error(Exception("未找到默认记账簿"))
            }
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取默认记账簿失败: ${e.message}"))
        }
    }
    
    override suspend fun getLedgerById(ledgerId: String): BaseResult<Ledger> {
        return try {
            val entity = ledgerDao.getLedgerById(ledgerId)
            if (entity != null) {
                BaseResult.Success(entity.toDomain())
            } else {
                BaseResult.Error(Exception("记账簿不存在"))
            }
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取记账簿失败: ${e.message}"))
        }
    }
    
    override suspend fun createLedger(
        userId: String,
        name: String,
        description: String?,
        color: String,
        icon: String,
        isDefault: Boolean
    ): BaseResult<Ledger> {
        return try {
            val now = Clock.System.now()
            
            // 如果设置为默认，需要先清除其他默认标记
            if (isDefault) {
                ledgerDao.clearDefaultLedgers(userId, now.epochSeconds)
            }
            
            val ledger = LedgerEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                description = description,
                color = color,
                icon = icon,
                isDefault = isDefault,
                displayOrder = ledgerDao.getNextDisplayOrder(userId),
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
            
            ledgerDao.insertLedger(ledger)
            BaseResult.Success(ledger.toDomain())
        } catch (e: Exception) {
            BaseResult.Error(Exception("创建记账簿失败: ${e.message}"))
        }
    }
    
    override suspend fun updateLedger(ledger: Ledger): BaseResult<Unit> {
        return try {
            val updatedLedger = LedgerEntity.fromDomain(
                ledger.copy(updatedAt = Clock.System.now())
            )
            ledgerDao.updateLedger(updatedLedger)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("更新记账簿失败: ${e.message}"))
        }
    }
    
    override suspend fun deleteLedger(ledgerId: String): BaseResult<Unit> {
        return try {
            val now = Clock.System.now()
            ledgerDao.deactivateLedger(ledgerId, now.epochSeconds)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("删除记账簿失败: ${e.message}"))
        }
    }
    
    override suspend fun setDefaultLedger(userId: String, ledgerId: String): BaseResult<Unit> {
        return try {
            val now = Clock.System.now()
            ledgerDao.setDefaultLedger(userId, ledgerId, now.epochSeconds)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("设置默认记账簿失败: ${e.message}"))
        }
    }
    
    override suspend fun updateLedgerOrder(ledgerId: String, newOrder: Int): BaseResult<Unit> {
        return try {
            val now = Clock.System.now()
            ledgerDao.updateLedgerOrder(ledgerId, newOrder, now.epochSeconds)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("更新记账簿顺序失败: ${e.message}"))
        }
    }
    
    override suspend fun ensureDefaultLedger(userId: String): BaseResult<Ledger> {
        return try {
            // 检查是否已有默认记账簿
            val hasDefault = ledgerDao.hasDefaultLedger(userId)
            if (hasDefault) {
                getDefaultLedger(userId)
            } else {
                // 创建默认记账簿
                createLedger(
                    userId = userId,
                    name = Ledger.DEFAULT_LEDGER_NAME,
                    description = Ledger.DEFAULT_LEDGER_DESCRIPTION,
                    color = "#3A7AFE",
                    icon = "book",
                    isDefault = true
                )
            }
        } catch (e: Exception) {
            BaseResult.Error(Exception("确保默认记账簿失败: ${e.message}"))
        }
    }
    
    override suspend fun hasDefaultLedger(userId: String): BaseResult<Boolean> {
        return try {
            val hasDefault = ledgerDao.hasDefaultLedger(userId)
            BaseResult.Success(hasDefault)
        } catch (e: Exception) {
            BaseResult.Error(Exception("检查默认记账簿失败: ${e.message}"))
        }
    }
    
    override suspend fun updateLedgersOrder(ledgerOrders: List<Pair<String, Int>>): BaseResult<Unit> {
        return try {
            val now = Clock.System.now()
            ledgerOrders.forEach { (ledgerId, order) ->
                ledgerDao.updateLedgerOrder(ledgerId, order, now.epochSeconds)
            }
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("批量更新记账簿顺序失败: ${e.message}"))
        }
    }
}
