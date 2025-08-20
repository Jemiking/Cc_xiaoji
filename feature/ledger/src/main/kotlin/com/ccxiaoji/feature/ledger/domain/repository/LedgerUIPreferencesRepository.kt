package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.feature.ledger.domain.model.LedgerUIPreferences
import com.ccxiaoji.feature.ledger.domain.model.LedgerUIStyle
import com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode
import kotlinx.coroutines.flow.Flow

/**
 * 记账UI偏好设置Repository接口
 */
interface LedgerUIPreferencesRepository {
    
    /**
     * 获取UI偏好设置流
     */
    fun getUIPreferences(): Flow<LedgerUIPreferences>
    
    /**
     * 更新UI风格
     */
    suspend fun updateUIStyle(style: LedgerUIStyle)
    
    
    /**
     * 更新动画持续时间
     */
    suspend fun updateAnimationDuration(durationMs: Int)
    
    /**
     * 更新图标显示模式
     */
    suspend fun updateIconDisplayMode(mode: IconDisplayMode)
    
    /**
     * 重置所有UI偏好设置
     */
    suspend fun resetToDefaults()
}