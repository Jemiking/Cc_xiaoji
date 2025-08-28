package com.ccxiaoji.feature.ledger.domain.service

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
import com.ccxiaoji.feature.ledger.domain.repository.LedgerUIPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 默认记账簿初始化服务
 * 
 * 负责确保用户始终拥有可用的默认记账簿，并处理各种边界情况
 */
@Singleton
class DefaultLedgerInitializationService @Inject constructor(
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val ledgerUIPreferencesRepository: LedgerUIPreferencesRepository
) {
    
    companion object {}
    
    /**
     * 执行完整的默认记账簿初始化
     * 
     * @param userId 用户ID
     * @return 初始化结果，包含默认记账簿或错误信息
     */
    suspend fun initializeDefaultLedger(userId: String): DefaultLedgerInitResult {
        try {
            // 1. 确保默认记账簿存在
            val ensureResult = manageLedgerUseCase.ensureDefaultLedger(userId)
            
            val defaultLedger = when (ensureResult) {
                is BaseResult.Success -> {
                    ensureResult.data
                }
                is BaseResult.Error -> {
                    return DefaultLedgerInitResult.Failure(
                        message = "无法创建默认记账簿: ${ensureResult.exception.message}",
                        exception = ensureResult.exception
                    )
                }
            }
            
            // 2. 验证记账簿列表的完整性
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            
            if (ledgers.isEmpty()) {
                return DefaultLedgerInitResult.Failure(
                    message = "记账簿列表为空，可能存在数据问题",
                    exception = Exception("Empty ledger list after initialization")
                )
            }
            
            // 3. 验证默认记账簿在列表中
            val defaultInList = ledgers.find { it.isDefault }
            if (defaultInList == null) {
                
                // 尝试将第一个记账簿设为默认
                val firstLedger = ledgers.first()
                val setDefaultResult = manageLedgerUseCase.setDefaultLedger(userId, firstLedger.id)
                
                when (setDefaultResult) {
                    is BaseResult.Success -> {
                    }
                    is BaseResult.Error -> {
                    }
                }
            } else {
            }
            
            // 4. 更新UI偏好设置
            val preferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            val currentSelectedId = preferences.selectedLedgerId
            
            // 如果没有选中的记账簿，或选中的记账簿不在列表中，选择默认记账簿
            val actualDefaultLedger = ledgers.find { it.isDefault } ?: ledgers.first()
            val selectedLedgerExists = currentSelectedId?.let { id ->
                ledgers.any { it.id == id }
            } ?: false
            
            if (currentSelectedId.isNullOrBlank() || !selectedLedgerExists) {
                ledgerUIPreferencesRepository.updateSelectedLedgerId(actualDefaultLedger.id)
            } else {
            }
            
            // 5. 生成初始化报告
            val report = DefaultLedgerInitReport(
                totalLedgers = ledgers.size,
                defaultLedger = actualDefaultLedger,
                selectedLedgerId = actualDefaultLedger.id,
                wasCreated = ensureResult is BaseResult.Success && 
                            ensureResult.data.name == Ledger.DEFAULT_LEDGER_NAME,
                issues = mutableListOf<String>().apply {
                    if (defaultInList == null) {
                        add("默认记账簿标记不一致，已自动修复")
                    }
                    if (!selectedLedgerExists) {
                        add("选中记账簿无效，已重置为默认记账簿")
                    }
                }
            )
            
            return DefaultLedgerInitResult.Success(report)
            
        } catch (e: Exception) {
            return DefaultLedgerInitResult.Failure(
                message = "初始化过程发生异常: ${e.message}",
                exception = e
            )
        }
    }
    
    /**
     * 快速检查默认记账簿状态
     * 
     * @param userId 用户ID
     * @return 检查结果
     */
    suspend fun checkDefaultLedgerStatus(userId: String): DefaultLedgerStatusCheck {
        return try {
            val hasDefault = manageLedgerUseCase.getDefaultLedger(userId)
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            val preferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            
            when (hasDefault) {
                is BaseResult.Success -> {
                    val defaultLedger = hasDefault.data
                    val selectedLedgerValid = preferences.selectedLedgerId?.let { id ->
                        ledgers.any { it.id == id }
                    } ?: false
                    
                    DefaultLedgerStatusCheck(
                        hasDefaultLedger = true,
                        defaultLedgerName = defaultLedger.name,
                        totalLedgers = ledgers.size,
                        selectedLedgerValid = selectedLedgerValid,
                        isHealthy = ledgers.isNotEmpty() && selectedLedgerValid
                    )
                }
                is BaseResult.Error -> {
                    DefaultLedgerStatusCheck(
                        hasDefaultLedger = false,
                        defaultLedgerName = null,
                        totalLedgers = ledgers.size,
                        selectedLedgerValid = false,
                        isHealthy = false
                    )
                }
            }
        } catch (e: Exception) {
            DefaultLedgerStatusCheck(
                hasDefaultLedger = false,
                defaultLedgerName = null,
                totalLedgers = 0,
                selectedLedgerValid = false,
                isHealthy = false
            )
        }
    }
}

/**
 * 默认记账簿初始化结果
 */
sealed class DefaultLedgerInitResult {
    data class Success(val report: DefaultLedgerInitReport) : DefaultLedgerInitResult()
    data class Failure(val message: String, val exception: Exception) : DefaultLedgerInitResult()
}

/**
 * 默认记账簿初始化报告
 */
data class DefaultLedgerInitReport(
    val totalLedgers: Int,
    val defaultLedger: Ledger,
    val selectedLedgerId: String,
    val wasCreated: Boolean,
    val issues: List<String>
)

/**
 * 默认记账簿状态检查结果
 */
data class DefaultLedgerStatusCheck(
    val hasDefaultLedger: Boolean,
    val defaultLedgerName: String?,
    val totalLedgers: Int,
    val selectedLedgerValid: Boolean,
    val isHealthy: Boolean
)
