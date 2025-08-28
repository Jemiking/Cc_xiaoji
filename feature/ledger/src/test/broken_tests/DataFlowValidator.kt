package com.ccxiaoji.feature.ledger.test

import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import android.util.Log

/**
 * 数据流完整性验证器
 * 用于测试记账簿切换时的数据流完整性
 */
class DataFlowValidator {
    
    companion object {
        private const val TAG = "DataFlowValidator"
    }
    
    /**
     * 验证记账簿切换的完整数据流
     */
    suspend fun validateLedgerSwitchDataFlow(
        viewModel: LedgerViewModel,
        fromLedgerId: String?,
        toLedgerId: String
    ): DataFlowValidationResult = runTest {
        
        Log.i(TAG, "开始验证记账簿切换数据流: $fromLedgerId -> $toLedgerId")
        
        val issues = mutableListOf<ValidationIssue>()
        
        try {
            // 1. 记录切换前的状态
            val beforeState = viewModel.uiState.first()
            Log.d(TAG, "切换前状态: 记账簿=${beforeState.currentLedger?.name}, 交易数=${beforeState.transactions.size}")
            
            // 2. 执行记账簿切换
            val switchStartTime = System.currentTimeMillis()
            viewModel.selectLedger(toLedgerId)
            
            // 3. 等待状态更新完成（最大等待3秒）
            var attempts = 0
            var stateUpdated = false
            while (attempts < 30 && !stateUpdated) {
                kotlinx.coroutines.delay(100)
                val currentState = viewModel.uiState.first()
                if (currentState.selectedLedgerId == toLedgerId && !currentState.isLoading) {
                    stateUpdated = true
                }
                attempts++
            }
            
            if (!stateUpdated) {
                issues.add(ValidationIssue(
                    type = IssueType.STATE_UPDATE_TIMEOUT,
                    message = "记账簿切换后状态更新超时",
                    severity = IssueSeverity.HIGH
                ))
            }
            
            val switchEndTime = System.currentTimeMillis()
            val switchDuration = switchEndTime - switchStartTime
            
            // 4. 验证切换后的状态
            val afterState = viewModel.uiState.first()
            Log.d(TAG, "切换后状态: 记账簿=${afterState.currentLedger?.name}, 交易数=${afterState.transactions.size}")
            
            // 5. 状态一致性验证
            validateStateConsistency(afterState, toLedgerId, issues)
            
            // 6. 数据完整性验证
            validateDataIntegrity(beforeState.transactions, afterState.transactions, toLedgerId, issues)
            
            // 7. 性能验证
            validatePerformance(switchDuration, issues)
            
            // 8. UI响应性验证
            validateUIResponsiveness(afterState, issues)
            
            Log.i(TAG, "数据流验证完成: 发现${issues.size}个问题")
            
            return@runTest DataFlowValidationResult(
                success = issues.none { it.severity == IssueSeverity.HIGH },
                switchDuration = switchDuration,
                issues = issues,
                beforeTransactionCount = beforeState.transactions.size,
                afterTransactionCount = afterState.transactions.size,
                ledgerSwitched = afterState.selectedLedgerId == toLedgerId
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "数据流验证异常", e)
            issues.add(ValidationIssue(
                type = IssueType.VALIDATION_ERROR,
                message = "验证过程中发生异常: ${e.message}",
                severity = IssueSeverity.HIGH
            ))
            
            return@runTest DataFlowValidationResult(
                success = false,
                switchDuration = 0,
                issues = issues,
                beforeTransactionCount = 0,
                afterTransactionCount = 0,
                ledgerSwitched = false
            )
        }
    }
    
    /**
     * 验证状态一致性
     */
    private fun validateStateConsistency(
        state: com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUiState,
        expectedLedgerId: String,
        issues: MutableList<ValidationIssue>
    ) {
        // 验证选中的记账簿ID一致性
        if (state.selectedLedgerId != expectedLedgerId) {
            issues.add(ValidationIssue(
                type = IssueType.STATE_INCONSISTENCY,
                message = "selectedLedgerId不一致: 期望=$expectedLedgerId, 实际=${state.selectedLedgerId}",
                severity = IssueSeverity.HIGH
            ))
        }
        
        // 验证当前记账簿对象一致性
        if (state.currentLedger?.id != expectedLedgerId) {
            issues.add(ValidationIssue(
                type = IssueType.STATE_INCONSISTENCY,
                message = "currentLedger.id不一致: 期望=$expectedLedgerId, 实际=${state.currentLedger?.id}",
                severity = IssueSeverity.HIGH
            ))
        }
        
        // 验证加载状态
        if (state.isLoading || state.isLedgerLoading) {
            issues.add(ValidationIssue(
                type = IssueType.LOADING_STATE,
                message = "切换完成后仍在加载中",
                severity = IssueSeverity.MEDIUM
            ))
        }
    }
    
    /**
     * 验证数据完整性
     */
    private fun validateDataIntegrity(
        beforeTransactions: List<Transaction>,
        afterTransactions: List<Transaction>,
        ledgerId: String,
        issues: MutableList<ValidationIssue>
    ) {
        // 验证交易数据是否属于正确的记账簿
        val invalidTransactions = afterTransactions.filter { it.ledgerId != ledgerId }
        if (invalidTransactions.isNotEmpty()) {
            issues.add(ValidationIssue(
                type = IssueType.DATA_INTEGRITY,
                message = "发现${invalidTransactions.size}条不属于当前记账簿的交易",
                severity = IssueSeverity.HIGH
            ))
        }
        
        // 验证数据变化合理性
        if (beforeTransactions.isNotEmpty() && afterTransactions.isEmpty()) {
            issues.add(ValidationIssue(
                type = IssueType.DATA_LOSS,
                message = "切换后交易数据为空，可能存在数据丢失",
                severity = IssueSeverity.MEDIUM
            ))
        }
    }
    
    /**
     * 验证性能
     */
    private fun validatePerformance(
        switchDuration: Long,
        issues: MutableList<ValidationIssue>
    ) {
        when {
            switchDuration > 5000 -> {
                issues.add(ValidationIssue(
                    type = IssueType.PERFORMANCE,
                    message = "记账簿切换过慢: ${switchDuration}ms > 5000ms",
                    severity = IssueSeverity.HIGH
                ))
            }
            switchDuration > 2000 -> {
                issues.add(ValidationIssue(
                    type = IssueType.PERFORMANCE,
                    message = "记账簿切换较慢: ${switchDuration}ms > 2000ms",
                    severity = IssueSeverity.MEDIUM
                ))
            }
            switchDuration > 1000 -> {
                issues.add(ValidationIssue(
                    type = IssueType.PERFORMANCE,
                    message = "记账簿切换有延迟: ${switchDuration}ms > 1000ms",
                    severity = IssueSeverity.LOW
                ))
            }
        }
    }
    
    /**
     * 验证UI响应性
     */
    private fun validateUIResponsiveness(
        state: com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUiState,
        issues: MutableList<ValidationIssue>
    ) {
        // 验证必要的UI数据是否已加载
        if (state.ledgers.isEmpty()) {
            issues.add(ValidationIssue(
                type = IssueType.UI_RESPONSIVENESS,
                message = "记账簿列表为空",
                severity = IssueSeverity.MEDIUM
            ))
        }
        
        if (state.currentLedger == null) {
            issues.add(ValidationIssue(
                type = IssueType.UI_RESPONSIVENESS,
                message = "当前记账簿对象为空",
                severity = IssueSeverity.HIGH
            ))
        }
    }
    
    /**
     * 验证默认记账簿机制
     */
    suspend fun validateDefaultLedgerMechanism(
        viewModel: LedgerViewModel
    ): DefaultLedgerValidationResult = runTest {
        
        Log.i(TAG, "开始验证默认记账簿机制")
        
        val issues = mutableListOf<ValidationIssue>()
        
        try {
            // 等待初始化完成
            var attempts = 0
            while (attempts < 50) { // 最大等待5秒
                kotlinx.coroutines.delay(100)
                val state = viewModel.uiState.first()
                if (!state.isLedgerLoading && state.ledgers.isNotEmpty()) {
                    break
                }
                attempts++
            }
            
            val state = viewModel.uiState.first()
            
            // 验证是否有默认记账簿
            val defaultLedger = state.ledgers.find { it.isDefault }
            if (defaultLedger == null) {
                issues.add(ValidationIssue(
                    type = IssueType.DEFAULT_LEDGER,
                    message = "未找到默认记账簿",
                    severity = IssueSeverity.HIGH
                ))
            } else {
                Log.d(TAG, "找到默认记账簿: ${defaultLedger.name}")
                
                // 验证默认记账簿是否被正确选中
                if (state.currentLedger?.id != defaultLedger.id) {
                    issues.add(ValidationIssue(
                        type = IssueType.DEFAULT_LEDGER,
                        message = "默认记账簿未被自动选中",
                        severity = IssueSeverity.MEDIUM
                    ))
                }
            }
            
            // 验证默认记账簿名称
            if (defaultLedger?.name != Ledger.DEFAULT_LEDGER_NAME) {
                issues.add(ValidationIssue(
                    type = IssueType.DEFAULT_LEDGER,
                    message = "默认记账簿名称不正确: 期望=${Ledger.DEFAULT_LEDGER_NAME}, 实际=${defaultLedger?.name}",
                    severity = IssueSeverity.LOW
                ))
            }
            
            return@runTest DefaultLedgerValidationResult(
                success = issues.none { it.severity == IssueSeverity.HIGH },
                hasDefaultLedger = defaultLedger != null,
                defaultLedgerSelected = state.currentLedger?.isDefault == true,
                defaultLedgerName = defaultLedger?.name,
                issues = issues
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "默认记账簿验证异常", e)
            issues.add(ValidationIssue(
                type = IssueType.VALIDATION_ERROR,
                message = "验证过程中发生异常: ${e.message}",
                severity = IssueSeverity.HIGH
            ))
            
            return@runTest DefaultLedgerValidationResult(
                success = false,
                hasDefaultLedger = false,
                defaultLedgerSelected = false,
                defaultLedgerName = null,
                issues = issues
            )
        }
    }
}

/**
 * 数据流验证结果
 */
data class DataFlowValidationResult(
    val success: Boolean,
    val switchDuration: Long,
    val issues: List<ValidationIssue>,
    val beforeTransactionCount: Int,
    val afterTransactionCount: Int,
    val ledgerSwitched: Boolean
)

/**
 * 默认记账簿验证结果
 */
data class DefaultLedgerValidationResult(
    val success: Boolean,
    val hasDefaultLedger: Boolean,
    val defaultLedgerSelected: Boolean,
    val defaultLedgerName: String?,
    val issues: List<ValidationIssue>
)

/**
 * 验证问题
 */
data class ValidationIssue(
    val type: IssueType,
    val message: String,
    val severity: IssueSeverity
)

/**
 * 问题类型
 */
enum class IssueType {
    STATE_UPDATE_TIMEOUT,
    STATE_INCONSISTENCY,
    DATA_INTEGRITY,
    DATA_LOSS,
    PERFORMANCE,
    UI_RESPONSIVENESS,
    DEFAULT_LEDGER,
    LOADING_STATE,
    VALIDATION_ERROR
}

/**
 * 问题严重性
 */
enum class IssueSeverity {
    LOW,
    MEDIUM,
    HIGH
}