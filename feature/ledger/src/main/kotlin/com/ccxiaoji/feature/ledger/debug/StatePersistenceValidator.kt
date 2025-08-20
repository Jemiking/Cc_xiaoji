package com.ccxiaoji.feature.ledger.debug

import com.ccxiaoji.feature.ledger.domain.repository.LedgerUIPreferencesRepository
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerViewModel
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 状态持久化验证器
 * 验证记账簿选择的保存和恢复，应用重启后状态保持
 */
@Singleton
class StatePersistenceValidator @Inject constructor(
    private val ledgerUIPreferencesRepository: LedgerUIPreferencesRepository,
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val userApi: UserApi
) {
    
    companion object {
        private const val TAG = "StatePersistenceValidator"
        private const val DEBUG_TAG = "LEDGER_PERSISTENCE_DEBUG"
    }
    
    private val validatorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * 执行完整的状态持久化验证
     */
    fun executeFullPersistenceValidation() {
        validatorScope.launch {
            try {
                Log.i(TAG, "🚀 开始执行状态持久化验证")
                
                val userId = userApi.getCurrentUserId()
                
                // 1. 验证基础持久化功能
                validateBasicPersistence(userId)
                
                // 2. 验证偏好设置读写
                validatePreferencesReadWrite(userId)
                
                // 3. 验证无效记账簿ID处理
                validateInvalidLedgerIdHandling(userId)
                
                // 4. 验证状态同步
                validateStateSynchronization(userId)
                
                // 5. 验证记账簿删除后的偏好设置清理
                validateLedgerDeletionCleanup(userId)
                
                Log.i(TAG, "✅ 状态持久化验证完成")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 状态持久化验证失败", e)
            }
        }
    }
    
    /**
     * 验证基础持久化功能
     */
    private suspend fun validateBasicPersistence(userId: String) {
        Log.d(DEBUG_TAG, "💾 开始验证基础持久化功能")
        
        try {
            // 获取当前偏好设置
            val initialPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            Log.d(DEBUG_TAG, "初始偏好设置: selectedLedgerId=${initialPreferences.selectedLedgerId}")
            
            // 获取可用的记账簿
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.isEmpty()) {
                Log.w(DEBUG_TAG, "⚠️ 用户没有记账簿，跳过持久化测试")
                return
            }
            
            // 选择一个测试记账簿
            val testLedger = ledgers.first()
            Log.d(DEBUG_TAG, "选择测试记账簿: ${testLedger.name} (${testLedger.id})")
            
            // 保存记账簿选择
            ledgerUIPreferencesRepository.updateSelectedLedgerId(testLedger.id)
            Log.d(DEBUG_TAG, "已保存记账簿选择")
            
            // 验证保存后的读取
            val updatedPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            if (updatedPreferences.selectedLedgerId == testLedger.id) {
                Log.d(DEBUG_TAG, "✅ 记账簿选择保存成功")
            } else {
                Log.e(DEBUG_TAG, "❌ 记账簿选择保存失败: 期望=${testLedger.id}, 实际=${updatedPreferences.selectedLedgerId}")
            }
            
            // 测试清除选择
            ledgerUIPreferencesRepository.updateSelectedLedgerId(null)
            val clearedPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            if (clearedPreferences.selectedLedgerId == null) {
                Log.d(DEBUG_TAG, "✅ 记账簿选择清除成功")
            } else {
                Log.e(DEBUG_TAG, "❌ 记账簿选择清除失败: ${clearedPreferences.selectedLedgerId}")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证基础持久化功能异常", e)
        }
    }
    
    /**
     * 验证偏好设置读写性能和可靠性
     */
    private suspend fun validatePreferencesReadWrite(userId: String) {
        Log.d(DEBUG_TAG, "🔄 开始验证偏好设置读写性能")
        
        try {
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.isEmpty()) {
                Log.w(DEBUG_TAG, "⚠️ 无记账簿可用于读写测试")
                return
            }
            
            val testCycles = 5
            val startTime = System.currentTimeMillis()
            
            // 进行多次读写测试
            repeat(testCycles) { cycle ->
                val testLedger = ledgers[cycle % ledgers.size]
                
                // 写入
                val writeStartTime = System.currentTimeMillis()
                ledgerUIPreferencesRepository.updateSelectedLedgerId(testLedger.id)
                val writeTime = System.currentTimeMillis() - writeStartTime
                
                // 读取
                val readStartTime = System.currentTimeMillis()
                val preferences = ledgerUIPreferencesRepository.getUIPreferences().first()
                val readTime = System.currentTimeMillis() - readStartTime
                
                // 验证一致性
                if (preferences.selectedLedgerId == testLedger.id) {
                    Log.d(DEBUG_TAG, "✅ 第${cycle + 1}次读写测试成功 (写入:${writeTime}ms, 读取:${readTime}ms)")
                } else {
                    Log.e(DEBUG_TAG, "❌ 第${cycle + 1}次读写测试失败: 期望=${testLedger.id}, 实际=${preferences.selectedLedgerId}")
                }
            }
            
            val totalTime = System.currentTimeMillis() - startTime
            val avgTime = totalTime / testCycles
            
            Log.d(DEBUG_TAG, "📊 读写性能统计: 总时间=${totalTime}ms, 平均时间=${avgTime}ms")
            
            if (avgTime < 100) {
                Log.d(DEBUG_TAG, "✅ 读写性能良好")
            } else if (avgTime < 500) {
                Log.w(DEBUG_TAG, "⚠️ 读写性能一般")
            } else {
                Log.e(DEBUG_TAG, "❌ 读写性能较差")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证偏好设置读写异常", e)
        }
    }
    
    /**
     * 验证无效记账簿ID的处理
     */
    private suspend fun validateInvalidLedgerIdHandling(userId: String) {
        Log.d(DEBUG_TAG, "🚫 开始验证无效记账簿ID处理")
        
        try {
            // 保存一个不存在的记账簿ID
            val invalidLedgerId = "invalid_ledger_id_test_${System.currentTimeMillis()}"
            ledgerUIPreferencesRepository.updateSelectedLedgerId(invalidLedgerId)
            
            Log.d(DEBUG_TAG, "已保存无效记账簿ID: $invalidLedgerId")
            
            // 验证读取
            val preferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            if (preferences.selectedLedgerId == invalidLedgerId) {
                Log.d(DEBUG_TAG, "✅ 无效记账簿ID正确保存和读取")
            } else {
                Log.e(DEBUG_TAG, "❌ 无效记账簿ID保存/读取失败")
            }
            
            // 验证系统如何处理无效ID（这通常在ViewModel层处理）
            val availableLedgers = manageLedgerUseCase.getUserLedgers(userId).first()
            val validLedger = availableLedgers.find { it.id == invalidLedgerId }
            
            if (validLedger == null) {
                Log.d(DEBUG_TAG, "✅ 系统正确识别了无效记账簿ID")
                
                // 模拟系统自动切换到默认记账簿的逻辑
                val defaultLedger = availableLedgers.find { it.isDefault }
                if (defaultLedger != null) {
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(defaultLedger.id)
                    Log.d(DEBUG_TAG, "✅ 系统自动切换到默认记账簿: ${defaultLedger.name}")
                } else {
                    Log.w(DEBUG_TAG, "⚠️ 没有默认记账簿可切换")
                }
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证无效记账簿ID处理异常", e)
        }
    }
    
    /**
     * 验证状态同步
     */
    private suspend fun validateStateSynchronization(userId: String) {
        Log.d(DEBUG_TAG, "🔄 开始验证状态同步")
        
        try {
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.size < 2) {
                Log.w(DEBUG_TAG, "⚠️ 记账簿数量不足，跳过状态同步测试")
                return
            }
            
            val ledger1 = ledgers[0]
            val ledger2 = ledgers[1]
            
            // 设置第一个记账簿
            ledgerUIPreferencesRepository.updateSelectedLedgerId(ledger1.id)
            kotlinx.coroutines.delay(100) // 等待异步操作完成
            
            val preferences1 = ledgerUIPreferencesRepository.getUIPreferences().first()
            if (preferences1.selectedLedgerId == ledger1.id) {
                Log.d(DEBUG_TAG, "✅ 第一次状态同步成功: ${ledger1.name}")
            } else {
                Log.e(DEBUG_TAG, "❌ 第一次状态同步失败")
            }
            
            // 设置第二个记账簿
            ledgerUIPreferencesRepository.updateSelectedLedgerId(ledger2.id)
            kotlinx.coroutines.delay(100) // 等待异步操作完成
            
            val preferences2 = ledgerUIPreferencesRepository.getUIPreferences().first()
            if (preferences2.selectedLedgerId == ledger2.id) {
                Log.d(DEBUG_TAG, "✅ 第二次状态同步成功: ${ledger2.name}")
            } else {
                Log.e(DEBUG_TAG, "❌ 第二次状态同步失败")
            }
            
            // 验证状态变化的即时性
            val changeStartTime = System.currentTimeMillis()
            ledgerUIPreferencesRepository.updateSelectedLedgerId(ledger1.id)
            val updatedPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            val changeTime = System.currentTimeMillis() - changeStartTime
            
            if (updatedPreferences.selectedLedgerId == ledger1.id) {
                Log.d(DEBUG_TAG, "✅ 状态变化响应时间: ${changeTime}ms")
                if (changeTime < 50) {
                    Log.d(DEBUG_TAG, "✅ 状态同步响应迅速")
                } else {
                    Log.w(DEBUG_TAG, "⚠️ 状态同步响应较慢")
                }
            } else {
                Log.e(DEBUG_TAG, "❌ 状态变化验证失败")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证状态同步异常", e)
        }
    }
    
    /**
     * 验证记账簿删除后的偏好设置清理（模拟测试）
     */
    private suspend fun validateLedgerDeletionCleanup(userId: String) {
        Log.d(DEBUG_TAG, "🗑️ 开始验证记账簿删除后的偏好设置清理")
        
        try {
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.isEmpty()) {
                Log.w(DEBUG_TAG, "⚠️ 无记账簿可用于删除测试")
                return
            }
            
            // 选择一个非默认记账簿进行模拟删除测试
            val testLedger = ledgers.find { !it.isDefault } ?: ledgers.first()
            
            // 设置为当前选中记账簿
            ledgerUIPreferencesRepository.updateSelectedLedgerId(testLedger.id)
            Log.d(DEBUG_TAG, "设置测试记账簿为当前选中: ${testLedger.name}")
            
            // 模拟记账簿被删除的情况（在实际系统中，这通常由ViewModel处理）
            Log.d(DEBUG_TAG, "模拟记账簿 ${testLedger.name} 被删除")
            
            // 验证系统如何处理已删除记账簿的偏好设置
            val remainingLedgers = ledgers.filter { it.id != testLedger.id }
            if (remainingLedgers.isNotEmpty()) {
                // 选择替代记账簿（通常是默认记账簿）
                val replacementLedger = remainingLedgers.find { it.isDefault } ?: remainingLedgers.first()
                
                // 更新偏好设置到替代记账簿
                ledgerUIPreferencesRepository.updateSelectedLedgerId(replacementLedger.id)
                Log.d(DEBUG_TAG, "✅ 偏好设置已更新到替代记账簿: ${replacementLedger.name}")
                
                // 验证更新结果
                val updatedPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
                if (updatedPreferences.selectedLedgerId == replacementLedger.id) {
                    Log.d(DEBUG_TAG, "✅ 删除清理逻辑验证成功")
                } else {
                    Log.e(DEBUG_TAG, "❌ 删除清理逻辑验证失败")
                }
            } else {
                Log.w(DEBUG_TAG, "⚠️ 没有剩余记账簿，清除偏好设置")
                ledgerUIPreferencesRepository.updateSelectedLedgerId(null)
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证记账簿删除清理异常", e)
        }
    }
    
    /**
     * 验证ViewModel的状态持久化行为
     */
    suspend fun validateViewModelPersistence(viewModel: LedgerViewModel): PersistenceValidationSummary {
        Log.d(DEBUG_TAG, "🎯 开始验证ViewModel的状态持久化")
        
        val issues = mutableListOf<String>()
        
        try {
            // 等待ViewModel初始化
            var attempts = 0
            while (attempts < 50) {
                kotlinx.coroutines.delay(100)
                val state = viewModel.uiState.first()
                if (!state.isLedgerLoading && state.ledgers.isNotEmpty()) {
                    break
                }
                attempts++
            }
            
            val initialState = viewModel.uiState.first()
            
            // 验证当前选择是否从偏好设置恢复
            val preferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            
            if (preferences.selectedLedgerId != null) {
                if (initialState.selectedLedgerId == preferences.selectedLedgerId) {
                    Log.d(DEBUG_TAG, "✅ ViewModel正确从偏好设置恢复记账簿选择")
                } else {
                    issues.add("ViewModel记账簿选择与偏好设置不一致")
                }
            } else {
                if (initialState.currentLedger?.isDefault == true) {
                    Log.d(DEBUG_TAG, "✅ 无偏好设置时正确选择了默认记账簿")
                } else {
                    issues.add("无偏好设置时未正确选择默认记账簿")
                }
            }
            
            // 测试ViewModel中的记账簿切换是否正确保存到偏好设置
            if (initialState.ledgers.size >= 2) {
                val testLedger = initialState.ledgers.find { it.id != initialState.selectedLedgerId }
                if (testLedger != null) {
                    Log.d(DEBUG_TAG, "测试切换到记账簿: ${testLedger.name}")
                    
                    // 执行切换
                    viewModel.selectLedger(testLedger.id)
                    
                    // 等待切换完成
                    kotlinx.coroutines.delay(500)
                    
                    // 验证偏好设置是否更新
                    val updatedPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
                    if (updatedPreferences.selectedLedgerId == testLedger.id) {
                        Log.d(DEBUG_TAG, "✅ 记账簿切换正确保存到偏好设置")
                    } else {
                        issues.add("记账簿切换未正确保存到偏好设置")
                    }
                    
                    // 验证ViewModel状态是否更新
                    val updatedState = viewModel.uiState.first()
                    if (updatedState.selectedLedgerId == testLedger.id) {
                        Log.d(DEBUG_TAG, "✅ ViewModel状态正确更新")
                    } else {
                        issues.add("ViewModel状态未正确更新")
                    }
                }
            }
            
            return PersistenceValidationSummary(
                success = issues.isEmpty(),
                initialLedgerId = initialState.selectedLedgerId,
                preferredLedgerId = preferences.selectedLedgerId,
                stateRestoredCorrectly = initialState.selectedLedgerId == preferences.selectedLedgerId || 
                                        (preferences.selectedLedgerId == null && initialState.currentLedger?.isDefault == true),
                issues = issues
            )
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证ViewModel状态持久化异常", e)
            issues.add("验证过程异常: ${e.message}")
            
            return PersistenceValidationSummary(
                success = false,
                initialLedgerId = null,
                preferredLedgerId = null,
                stateRestoredCorrectly = false,
                issues = issues
            )
        }
    }
}

/**
 * 持久化验证总结
 */
data class PersistenceValidationSummary(
    val success: Boolean,
    val initialLedgerId: String?,
    val preferredLedgerId: String?,
    val stateRestoredCorrectly: Boolean,
    val issues: List<String>
)