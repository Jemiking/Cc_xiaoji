package com.ccxiaoji.feature.ledger.debug

import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 运行时数据流验证器
 * 用于在应用运行时验证记账簿功能的数据流完整性
 */
@Singleton
class RuntimeDataFlowValidator @Inject constructor(
    private val defaultLedgerValidator: DefaultLedgerMechanismValidator,
    private val statePersistenceValidator: StatePersistenceValidator
) {
    
    companion object {
        private const val TAG = "RuntimeDataFlowValidator"
        private const val DEBUG_TAG = "LEDGER_DATAFLOW_DEBUG"
    }
    
    private val validatorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * 执行完整的记账簿数据流验证
     * 可以在调试模式下调用，验证所有关键功能
     */
    fun executeFullValidation(viewModel: LedgerViewModel) {
        validatorScope.launch {
            try {
                Log.i(TAG, "🚀 开始执行完整的记账簿数据流验证")
                
                // 1. 验证初始状态
                validateInitialState(viewModel)
                
                // 2. 验证默认记账簿机制（使用专门的验证器）
                validateDefaultLedgerMechanismWithValidator(viewModel)
                
                // 3. 验证记账簿切换（如果有多个记账簿）
                validateLedgerSwitching(viewModel)
                
                // 4. 验证状态持久化
                validateStatePersistence(viewModel)
                
                // 5. 验证数据一致性
                validateDataConsistency(viewModel)
                
                Log.i(TAG, "✅ 记账簿数据流验证完成")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 记账簿数据流验证失败", e)
            }
        }
    }
    
    /**
     * 验证初始状态
     */
    private suspend fun validateInitialState(viewModel: LedgerViewModel) {
        Log.d(DEBUG_TAG, "📋 开始验证初始状态")
        
        val state = viewModel.uiState.first()
        
        // 验证记账簿列表
        if (state.ledgers.isEmpty()) {
            Log.w(DEBUG_TAG, "⚠️ 记账簿列表为空")
        } else {
            Log.d(DEBUG_TAG, "✅ 记账簿列表: ${state.ledgers.size}个记账簿")
            state.ledgers.forEachIndexed { index, ledger ->
                Log.d(DEBUG_TAG, "  [$index] ${ledger.name} (${ledger.id}) ${if (ledger.isDefault) "[默认]" else ""}")
            }
        }
        
        // 验证当前选中记账簿
        if (state.currentLedger == null) {
            Log.w(DEBUG_TAG, "⚠️ 当前记账簿为空")
        } else {
            Log.d(DEBUG_TAG, "✅ 当前记账簿: ${state.currentLedger.name}")
        }
        
        // 验证交易数据
        Log.d(DEBUG_TAG, "✅ 交易数据: ${state.transactions.size}条交易")
        
        // 验证月度统计
        Log.d(DEBUG_TAG, "✅ 月度统计: 收入=${state.monthlyIncome}, 支出=${state.monthlyExpense}")
    }
    
    /**
     * 使用专门的验证器验证默认记账簿机制
     */
    private suspend fun validateDefaultLedgerMechanismWithValidator(viewModel: LedgerViewModel) {
        Log.d(DEBUG_TAG, "📚 开始验证默认记账簿机制（使用专门验证器）")
        
        try {
            // 使用专门的默认记账簿验证器
            val validationSummary = defaultLedgerValidator.validateViewModelDefaultLedger(viewModel)
            
            if (validationSummary.success) {
                Log.d(DEBUG_TAG, "✅ 默认记账簿机制验证通过")
                Log.d(DEBUG_TAG, "    记账簿数量: ${validationSummary.ledgerCount}")
                Log.d(DEBUG_TAG, "    有默认记账簿: ${validationSummary.hasDefaultLedger}")
                Log.d(DEBUG_TAG, "    默认记账簿已选中: ${validationSummary.defaultLedgerSelected}")
                Log.d(DEBUG_TAG, "    默认记账簿名称: ${validationSummary.defaultLedgerName}")
            } else {
                Log.e(DEBUG_TAG, "❌ 默认记账簿机制验证失败")
                validationSummary.issues.forEach { issue ->
                    Log.e(DEBUG_TAG, "    问题: $issue")
                }
            }
            
            // 同时运行完整的默认记账簿机制验证
            defaultLedgerValidator.executeFullDefaultLedgerValidation()
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 默认记账簿机制验证异常", e)
        }
    }
    
    /**
     * 验证记账簿切换
     */
    private suspend fun validateLedgerSwitching(viewModel: LedgerViewModel) {
        Log.d(DEBUG_TAG, "🔄 开始验证记账簿切换")
        
        val state = viewModel.uiState.first()
        
        if (state.ledgers.size < 2) {
            Log.w(DEBUG_TAG, "⚠️ 记账簿数量不足（${state.ledgers.size} < 2），跳过切换测试")
            return
        }
        
        val fromLedger = state.currentLedger ?: state.ledgers.first()
        val toLedger = state.ledgers.find { it.id != fromLedger.id } ?: return
        
        Log.d(DEBUG_TAG, "🔄 执行切换: ${fromLedger.name} -> ${toLedger.name}")
        
        // 记录切换前状态
        val beforeTransactionCount = state.transactions.size
        val beforeMonthlyIncome = state.monthlyIncome
        val beforeMonthlyExpense = state.monthlyExpense
        
        // 执行切换
        val switchStartTime = System.currentTimeMillis()
        viewModel.selectLedger(toLedger.id)
        
        // 等待切换完成
        var attempts = 0
        var switchCompleted = false
        while (attempts < 50 && !switchCompleted) { // 最大等待5秒
            kotlinx.coroutines.delay(100)
            val currentState = viewModel.uiState.first()
            if (currentState.selectedLedgerId == toLedger.id && 
                !currentState.isLoading && 
                !currentState.isLedgerLoading) {
                switchCompleted = true
            }
            attempts++
        }
        
        val switchEndTime = System.currentTimeMillis()
        val switchDuration = switchEndTime - switchStartTime
        
        if (!switchCompleted) {
            Log.e(DEBUG_TAG, "❌ 记账簿切换超时（${switchDuration}ms）")
            return
        }
        
        // 验证切换后状态
        val afterState = viewModel.uiState.first()
        val afterTransactionCount = afterState.transactions.size
        val afterMonthlyIncome = afterState.monthlyIncome
        val afterMonthlyExpense = afterState.monthlyExpense
        
        Log.d(DEBUG_TAG, "✅ 记账簿切换完成（${switchDuration}ms）")
        Log.d(DEBUG_TAG, "    交易数变化: $beforeTransactionCount -> $afterTransactionCount")
        Log.d(DEBUG_TAG, "    收入变化: $beforeMonthlyIncome -> $afterMonthlyIncome")
        Log.d(DEBUG_TAG, "    支出变化: $beforeMonthlyExpense -> $afterMonthlyExpense")
        
        // 验证状态一致性
        if (afterState.currentLedger?.id == toLedger.id) {
            Log.d(DEBUG_TAG, "✅ 记账簿切换状态一致")
        } else {
            Log.e(DEBUG_TAG, "❌ 记账簿切换状态不一致")
        }
        
        // 验证性能
        when {
            switchDuration > 3000 -> Log.e(DEBUG_TAG, "❌ 切换性能差（${switchDuration}ms > 3000ms）")
            switchDuration > 1000 -> Log.w(DEBUG_TAG, "⚠️ 切换性能一般（${switchDuration}ms > 1000ms）")
            else -> Log.d(DEBUG_TAG, "✅ 切换性能良好（${switchDuration}ms）")
        }
        
        // 切换回原记账簿
        viewModel.selectLedger(fromLedger.id)
        kotlinx.coroutines.delay(500) // 等待切换完成
        Log.d(DEBUG_TAG, "🔄 已切换回原记账簿: ${fromLedger.name}")
    }
    
    /**
     * 验证状态持久化
     */
    private suspend fun validateStatePersistence(viewModel: LedgerViewModel) {
        Log.d(DEBUG_TAG, "💾 开始验证状态持久化")
        
        try {
            val state = viewModel.uiState.first()
            val currentLedger = state.currentLedger
            
            if (currentLedger == null) {
                Log.w(DEBUG_TAG, "⚠️ 当前记账簿为空，跳过状态持久化验证")
                return
            }
            
            // 使用专门的状态持久化验证器验证ViewModel持久化行为
            val validationSummary = statePersistenceValidator.validateViewModelPersistence(viewModel)
            
            if (validationSummary.success) {
                Log.d(DEBUG_TAG, "✅ ViewModel状态持久化验证通过")
                Log.d(DEBUG_TAG, "    初始记账簿ID: ${validationSummary.initialLedgerId}")
                Log.d(DEBUG_TAG, "    偏好设置记账簿ID: ${validationSummary.preferredLedgerId}")
                Log.d(DEBUG_TAG, "    状态恢复正确: ${validationSummary.stateRestoredCorrectly}")
            } else {
                Log.e(DEBUG_TAG, "❌ ViewModel状态持久化验证失败")
                validationSummary.issues.forEach { issue ->
                    Log.e(DEBUG_TAG, "    问题: $issue")
                }
            }
            
            // 执行完整的状态持久化验证（这会测试所有持久化功能）
            statePersistenceValidator.executeFullPersistenceValidation()
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 状态持久化验证异常", e)
        }
    }
    
    /**
     * 验证数据一致性
     */
    private suspend fun validateDataConsistency(viewModel: LedgerViewModel) {
        Log.d(DEBUG_TAG, "🔍 开始验证数据一致性")
        
        val state = viewModel.uiState.first()
        
        if (state.currentLedger == null) {
            Log.w(DEBUG_TAG, "⚠️ 当前记账簿为空，跳过数据一致性验证")
            return
        }
        
        val currentLedgerId = state.currentLedger.id
        
        // 验证交易数据是否属于当前记账簿
        val invalidTransactions = state.transactions.filter { it.ledgerId != currentLedgerId }
        if (invalidTransactions.isNotEmpty()) {
            Log.e(DEBUG_TAG, "❌ 发现${invalidTransactions.size}条不属于当前记账簿的交易")
            invalidTransactions.take(3).forEach { transaction ->
                Log.e(DEBUG_TAG, "    无效交易: ${transaction.id} (记账簿: ${transaction.ledgerId})")
            }
        } else {
            Log.d(DEBUG_TAG, "✅ 所有交易数据都属于当前记账簿")
        }
        
        // 验证月度统计的合理性
        val hasTransactions = state.transactions.isNotEmpty()
        val hasMonthlyData = state.monthlyIncome > 0 || state.monthlyExpense > 0
        
        if (hasTransactions && !hasMonthlyData) {
            Log.w(DEBUG_TAG, "⚠️ 有交易但无月度统计数据")
        } else if (!hasTransactions && hasMonthlyData) {
            Log.w(DEBUG_TAG, "⚠️ 无交易但有月度统计数据")
        } else {
            Log.d(DEBUG_TAG, "✅ 交易数据与月度统计一致")
        }
    }
    
    /**
     * 快速数据流检查
     * 适用于频繁调用的场景
     */
    fun quickDataFlowCheck(viewModel: LedgerViewModel) {
        validatorScope.launch {
            try {
                val state = viewModel.uiState.first()
                
                val checkResults = mutableListOf<String>()
                
                // 基本状态检查
                if (state.ledgers.isEmpty()) checkResults.add("❌ 无记账簿")
                if (state.currentLedger == null) checkResults.add("❌ 无当前记账簿")
                if (state.isLoading) checkResults.add("⏳ 正在加载")
                
                // 数据一致性检查
                val currentLedgerId = state.currentLedger?.id
                if (currentLedgerId != null) {
                    val invalidTransactions = state.transactions.count { it.ledgerId != currentLedgerId }
                    if (invalidTransactions > 0) {
                        checkResults.add("❌ ${invalidTransactions}条无效交易")
                    }
                }
                
                if (checkResults.isEmpty()) {
                    Log.d(DEBUG_TAG, "✅ 快速检查通过")
                } else {
                    Log.w(DEBUG_TAG, "⚠️ 快速检查发现问题: ${checkResults.joinToString(", ")}")
                }
                
            } catch (e: Exception) {
                Log.e(DEBUG_TAG, "❌ 快速检查异常", e)
            }
        }
    }
}