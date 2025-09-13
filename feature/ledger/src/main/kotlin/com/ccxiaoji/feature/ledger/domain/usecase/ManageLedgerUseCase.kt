package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.model.LedgerWithStats
import com.ccxiaoji.feature.ledger.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 账本管理用例
 *
 * 提供账本的创建、编辑、删除、查询等核心业务操作
 */
class ManageLedgerUseCase @Inject constructor(
    private val ledgerRepository: LedgerRepository
) {
    
    /**
     * 获取用户的所有账本
     */
    fun getUserLedgers(userId: String): Flow<List<Ledger>> {
        return ledgerRepository.getUserLedgers(userId)
    }
    
    /**
     * 获取用户的账本及统计数据
     */
    fun getUserLedgersWithStats(userId: String): Flow<List<LedgerWithStats>> {
        return ledgerRepository.getUserLedgersWithStats(userId)
    }
    
    /**
     * 获取用户的默认账本
     */
    suspend fun getDefaultLedger(userId: String): BaseResult<Ledger> {
        return ledgerRepository.getDefaultLedger(userId)
    }
    
    /**
     * 根据 ID 获取账本
     */
    suspend fun getLedgerById(ledgerId: String): BaseResult<Ledger> {
        return ledgerRepository.getLedgerById(ledgerId)
    }
    
    /**
     * 创建新账本
     */
    suspend fun createLedger(
        userId: String,
        name: String,
        description: String? = null,
        color: String = "#3A7AFE",
        icon: String = "book"
    ): BaseResult<Ledger> {
        // 验证账本名称
        if (name.isBlank()) {
            return BaseResult.Error(Exception("账本名称不能为空"))
        }
        
        if (name.length > 50) {
            return BaseResult.Error(Exception("账本名称不能超过50个字符"))
        }
        
        // 验证颜色格式
        if (!isValidColor(color)) {
            return BaseResult.Error(Exception("颜色格式不正确"))
        }
        
        return ledgerRepository.createLedger(
            userId = userId,
            name = name.trim(),
            description = description?.trim(),
            color = color,
            icon = icon,
            isDefault = false // 新创建的账本不是默认的
        )
    }
    
    /**
     * 更新账本信息
     */
    suspend fun updateLedger(
        ledger: Ledger,
        name: String? = null,
        description: String? = null,
        color: String? = null,
        icon: String? = null
    ): BaseResult<Unit> {
        // 验证参数
        name?.let {
            if (it.isBlank()) {
                return BaseResult.Error(Exception("账本名称不能为空"))
            }
            if (it.length > 50) {
                return BaseResult.Error(Exception("账本名称不能超过50个字符"))
            }
        }
        
        color?.let {
            if (!isValidColor(it)) {
                return BaseResult.Error(Exception("颜色格式不正确"))
            }
        }
        
        val updatedLedger = ledger.copy(
            name = name?.trim() ?: ledger.name,
            description = description?.trim() ?: ledger.description,
            color = color ?: ledger.color,
            icon = icon ?: ledger.icon
        )
        
        return ledgerRepository.updateLedger(updatedLedger)
    }
    
    /**
     * 删除账本
     */
    suspend fun deleteLedger(ledgerId: String, userId: String): BaseResult<Unit> {
        // 检查是否为默认账本
        val defaultLedgerResult = ledgerRepository.getDefaultLedger(userId)
        if (defaultLedgerResult is BaseResult.Success && defaultLedgerResult.data.id == ledgerId) {
            return BaseResult.Error(Exception("无法删除默认账本"))
        }
        
        return ledgerRepository.deleteLedger(ledgerId)
    }
    
    /**
     * 设置默认账本
     */
    suspend fun setDefaultLedger(userId: String, ledgerId: String): BaseResult<Unit> {
        // 验证账本是否存在
        val ledgerResult = ledgerRepository.getLedgerById(ledgerId)
        if (ledgerResult is BaseResult.Error) {
            return ledgerResult
        }
        
        val ledger = (ledgerResult as BaseResult.Success).data
        if (ledger.userId != userId) {
            return BaseResult.Error(Exception("无权限操作此账本"))
        }
        
        return ledgerRepository.setDefaultLedger(userId, ledgerId)
    }
    
    /**
     * 确保用户有默认账本
     */
    suspend fun ensureDefaultLedger(userId: String): BaseResult<Ledger> {
        return ledgerRepository.ensureDefaultLedger(userId)
    }
    
    /**
     * 重新排序账本
     */
    suspend fun reorderLedgers(ledgerOrders: List<Pair<String, Int>>): BaseResult<Unit> {
        if (ledgerOrders.isEmpty()) {
            return BaseResult.Success(Unit)
        }
        
        return ledgerRepository.updateLedgersOrder(ledgerOrders)
    }
    
    /**
     * 验证颜色格式是否正确
     */
    private fun isValidColor(color: String): Boolean {
        return color.matches(Regex("^#[0-9A-Fa-f]{6}$"))
    }
    
    /**
     * 获取推荐的图标列表
     */
    fun getRecommendedIcons(): List<String> {
        return Ledger.PREDEFINED_ICONS
    }
    
    /**
     * 获取推荐的颜色列表
     */
    fun getRecommendedColors(): List<String> {
        return Ledger.PREDEFINED_COLORS
    }
}
