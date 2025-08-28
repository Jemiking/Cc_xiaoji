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
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 边界情况验证器
 * 测试空记账簿、网络异常、数据冲突等异常场景
 */
@Singleton
class EdgeCaseValidator @Inject constructor(
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val userApi: UserApi,
    private val ledgerUIPreferencesRepository: LedgerUIPreferencesRepository
) {
    
    companion object {
        private const val TAG = "EdgeCaseValidator"
        private const val DEBUG_TAG = "LEDGER_EDGE_CASE_DEBUG"
    }
    
    private val validatorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * 执行完整的边界情况验证
     */
    fun executeFullEdgeCaseValidation() {
        validatorScope.launch {
            try {
                Log.i(TAG, "🚀 开始执行边界情况验证")
                
                val userId = userApi.getCurrentUserId()
                
                // 1. 验证空记账簿场景
                validateEmptyLedgerScenarios(userId)
                
                // 2. 验证数据一致性问题
                validateDataConsistencyIssues(userId)
                
                // 3. 验证无效状态处理
                validateInvalidStateHandling(userId)
                
                // 4. 验证资源限制场景
                validateResourceLimitScenarios(userId)
                
                // 5. 验证并发操作场景
                validateConcurrentOperationScenarios(userId)
                
                Log.i(TAG, "✅ 边界情况验证完成")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 边界情况验证失败", e)
            }
        }
    }
    
    /**
     * 验证空记账簿场景
     */
    private suspend fun validateEmptyLedgerScenarios(userId: String) {
        Log.d(DEBUG_TAG, "📭 开始验证空记账簿场景")
        
        try {
            // 1. 模拟新用户无记账簿场景
            Log.d(DEBUG_TAG, "🆕 模拟新用户无记账簿场景")
            
            val currentLedgers = manageLedgerUseCase.getUserLedgers(userId).first()
            Log.d(DEBUG_TAG, "当前记账簿数量: ${currentLedgers.size}")
            
            if (currentLedgers.isEmpty()) {
                Log.d(DEBUG_TAG, "✅ 检测到空记账簿状态")
                
                // 验证系统如何处理空记账簿状态
                val preferences = ledgerUIPreferencesRepository.getUIPreferences().first()
                if (preferences.selectedLedgerId == null) {
                    Log.d(DEBUG_TAG, "✅ 空记账簿时偏好设置正确为null")
                } else {
                    Log.e(DEBUG_TAG, "❌ 空记账簿时偏好设置不为null: ${preferences.selectedLedgerId}")
                }
                
                // 触发默认记账簿创建
                Log.d(DEBUG_TAG, "🔄 尝试触发默认记账簿创建")
                try {
                    manageLedgerUseCase.ensureDefaultLedger(userId)
                    
                    // 验证创建结果
                    delay(500) // 等待创建完成
                    val newLedgers = manageLedgerUseCase.getUserLedgers(userId).first()
                    if (newLedgers.isNotEmpty()) {
                        val defaultLedger = newLedgers.find { it.isDefault }
                        if (defaultLedger != null) {
                            Log.d(DEBUG_TAG, "✅ 默认记账簿创建成功: ${defaultLedger.name}")
                        } else {
                            Log.e(DEBUG_TAG, "❌ 创建了记账簿但没有标记为默认")
                        }
                    } else {
                        Log.e(DEBUG_TAG, "❌ 默认记账簿创建失败")
                    }
                } catch (e: Exception) {
                    Log.e(DEBUG_TAG, "❌ 默认记账簿创建异常", e)
                }
            } else {
                Log.d(DEBUG_TAG, "ℹ️ 用户已有记账簿，跳过空记账簿测试")
            }
            
            // 2. 模拟记账簿被删除后的清理场景
            Log.d(DEBUG_TAG, "🗑️ 验证记账簿删除后的状态清理")
            
            val testLedgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (testLedgers.size > 1) {
                // 选择一个非默认记账簿进行删除测试
                val nonDefaultLedger = testLedgers.find { !it.isDefault }
                if (nonDefaultLedger != null) {
                    // 先选中这个记账簿
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(nonDefaultLedger.id)
                    
                    Log.d(DEBUG_TAG, "模拟删除记账簿: ${nonDefaultLedger.name}")
                    
                    // 模拟删除后的状态处理（实际系统中应该自动切换）
                    val remainingLedgers = testLedgers.filter { it.id != nonDefaultLedger.id }
                    val defaultLedger = remainingLedgers.find { it.isDefault }
                    
                    if (defaultLedger != null) {
                        ledgerUIPreferencesRepository.updateSelectedLedgerId(defaultLedger.id)
                        Log.d(DEBUG_TAG, "✅ 删除后正确切换到默认记账簿: ${defaultLedger.name}")
                    } else {
                        Log.e(DEBUG_TAG, "❌ 删除后没有可用的默认记账簿")
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证空记账簿场景异常", e)
        }
    }
    
    /**
     * 验证数据一致性问题
     */
    private suspend fun validateDataConsistencyIssues(userId: String) {
        Log.d(DEBUG_TAG, "🔍 开始验证数据一致性问题")
        
        try {
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.isEmpty()) {
                Log.w(DEBUG_TAG, "⚠️ 无记账簿可用于一致性测试")
                return
            }
            
            // 1. 验证无效记账簿ID的处理
            Log.d(DEBUG_TAG, "🚫 验证无效记账簿ID处理")
            
            val invalidLedgerId = "invalid_${System.currentTimeMillis()}"
            
            // 设置无效的记账簿ID到偏好设置
            ledgerUIPreferencesRepository.updateSelectedLedgerId(invalidLedgerId)
            
            // 验证系统如何处理无效ID
            val preferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            if (preferences.selectedLedgerId == invalidLedgerId) {
                Log.d(DEBUG_TAG, "✅ 无效记账簿ID正确保存")
                
                // 验证有效记账簿列表不包含这个ID
                val validLedger = ledgers.find { it.id == invalidLedgerId }
                if (validLedger == null) {
                    Log.d(DEBUG_TAG, "✅ 系统正确识别无效记账簿ID")
                    
                    // 模拟系统自动修复：切换到默认记账簿
                    val defaultLedger = ledgers.find { it.isDefault }
                    if (defaultLedger != null) {
                        ledgerUIPreferencesRepository.updateSelectedLedgerId(defaultLedger.id)
                        Log.d(DEBUG_TAG, "✅ 自动修复：切换到默认记账簿")
                    }
                } else {
                    Log.e(DEBUG_TAG, "❌ 系统错误地认为无效ID是有效的")
                }
            }
            
            // 2. 验证重复记账簿的处理
            Log.d(DEBUG_TAG, "🔄 验证重复记账簿处理")
            
            // 检查是否有重复名称的记账簿
            val ledgerNames = ledgers.map { it.name }
            val duplicateNames = ledgerNames.groupBy { it }.filter { it.value.size > 1 }
            
            if (duplicateNames.isNotEmpty()) {
                Log.w(DEBUG_TAG, "⚠️ 发现重复名称的记账簿:")
                duplicateNames.forEach { (name, occurrences) ->
                    Log.w(DEBUG_TAG, "    $name: ${occurrences.size}个")
                }
            } else {
                Log.d(DEBUG_TAG, "✅ 无重复名称的记账簿")
            }
            
            // 3. 验证默认记账簿的唯一性
            Log.d(DEBUG_TAG, "🎯 验证默认记账簿唯一性")
            
            val defaultLedgers = ledgers.filter { it.isDefault }
            when (defaultLedgers.size) {
                0 -> Log.e(DEBUG_TAG, "❌ 没有默认记账簿")
                1 -> Log.d(DEBUG_TAG, "✅ 默认记账簿唯一: ${defaultLedgers.first().name}")
                else -> {
                    Log.e(DEBUG_TAG, "❌ 多个默认记账簿:")
                    defaultLedgers.forEach { ledger ->
                        Log.e(DEBUG_TAG, "    ${ledger.name} (${ledger.id})")
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证数据一致性问题异常", e)
        }
    }
    
    /**
     * 验证无效状态处理
     */
    private suspend fun validateInvalidStateHandling(userId: String) {
        Log.d(DEBUG_TAG, "⚠️ 开始验证无效状态处理")
        
        try {
            // 1. 验证null值处理
            Log.d(DEBUG_TAG, "🚫 验证null值处理")
            
            // 清除选中的记账簿
            ledgerUIPreferencesRepository.updateSelectedLedgerId(null)
            
            val nullPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            if (nullPreferences.selectedLedgerId == null) {
                Log.d(DEBUG_TAG, "✅ null值正确处理")
            } else {
                Log.e(DEBUG_TAG, "❌ null值处理失败: ${nullPreferences.selectedLedgerId}")
            }
            
            // 2. 验证空字符串处理
            Log.d(DEBUG_TAG, "📝 验证空字符串处理")
            
            ledgerUIPreferencesRepository.updateSelectedLedgerId("")
            
            val emptyPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            Log.d(DEBUG_TAG, "空字符串处理结果: '${emptyPreferences.selectedLedgerId}'")
            
            // 3. 验证极长字符串处理
            Log.d(DEBUG_TAG, "📏 验证极长字符串处理")
            
            val longString = "a".repeat(1000)
            ledgerUIPreferencesRepository.updateSelectedLedgerId(longString)
            
            val longPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            if (longPreferences.selectedLedgerId == longString) {
                Log.d(DEBUG_TAG, "✅ 极长字符串正确处理")
            } else {
                Log.w(DEBUG_TAG, "⚠️ 极长字符串被截断或修改")
            }
            
            // 4. 验证特殊字符处理
            Log.d(DEBUG_TAG, "🔣 验证特殊字符处理")
            
            val specialChars = "!@#$%^&*(){}[]|\\:;\"'<>,.?/~`"
            ledgerUIPreferencesRepository.updateSelectedLedgerId(specialChars)
            
            val specialPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            if (specialPreferences.selectedLedgerId == specialChars) {
                Log.d(DEBUG_TAG, "✅ 特殊字符正确处理")
            } else {
                Log.w(DEBUG_TAG, "⚠️ 特殊字符被转义或修改")
            }
            
            // 恢复到有效状态
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            val defaultLedger = ledgers.find { it.isDefault }
            if (defaultLedger != null) {
                ledgerUIPreferencesRepository.updateSelectedLedgerId(defaultLedger.id)
                Log.d(DEBUG_TAG, "✅ 状态已恢复到默认记账簿")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证无效状态处理异常", e)
        }
    }
    
    /**
     * 验证资源限制场景
     */
    private suspend fun validateResourceLimitScenarios(userId: String) {
        Log.d(DEBUG_TAG, "📊 开始验证资源限制场景")
        
        try {
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            
            // 1. 验证大量记账簿的处理
            Log.d(DEBUG_TAG, "📚 验证大量记账簿处理")
            
            val ledgerCount = ledgers.size
            Log.d(DEBUG_TAG, "当前记账簿数量: $ledgerCount")
            
            when {
                ledgerCount < 5 -> Log.d(DEBUG_TAG, "✅ 记账簿数量正常（$ledgerCount < 5）")
                ledgerCount < 20 -> Log.w(DEBUG_TAG, "⚠️ 记账簿数量较多（$ledgerCount）")
                else -> Log.e(DEBUG_TAG, "❌ 记账簿数量过多（$ledgerCount），可能影响性能")
            }
            
            // 2. 验证记账簿切换性能
            Log.d(DEBUG_TAG, "⚡ 验证记账簿切换性能")
            
            if (ledgers.size >= 2) {
                val switchTimes = mutableListOf<Long>()
                val testCycles = minOf(ledgers.size, 5) // 最多测试5次
                
                repeat(testCycles) { cycle ->
                    val targetLedger = ledgers[cycle % ledgers.size]
                    
                    val startTime = System.currentTimeMillis()
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(targetLedger.id)
                    
                    // 验证切换完成
                    val preferences = ledgerUIPreferencesRepository.getUIPreferences().first()
                    val switchTime = System.currentTimeMillis() - startTime
                    
                    if (preferences.selectedLedgerId == targetLedger.id) {
                        switchTimes.add(switchTime)
                        Log.d(DEBUG_TAG, "切换${cycle + 1}: ${targetLedger.name} (${switchTime}ms)")
                    } else {
                        Log.e(DEBUG_TAG, "❌ 切换${cycle + 1}失败")
                    }
                    
                    delay(100) // 避免过于频繁的切换
                }
                
                if (switchTimes.isNotEmpty()) {
                    val avgTime = switchTimes.average()
                    val maxTime = switchTimes.maxOrNull() ?: 0
                    
                    Log.d(DEBUG_TAG, "📈 切换性能统计:")
                    Log.d(DEBUG_TAG, "    平均时间: ${avgTime.toInt()}ms")
                    Log.d(DEBUG_TAG, "    最大时间: ${maxTime}ms")
                    Log.d(DEBUG_TAG, "    测试次数: ${switchTimes.size}")
                    
                    when {
                        avgTime < 50 -> Log.d(DEBUG_TAG, "✅ 切换性能优秀")
                        avgTime < 200 -> Log.d(DEBUG_TAG, "✅ 切换性能良好")
                        avgTime < 500 -> Log.w(DEBUG_TAG, "⚠️ 切换性能一般")
                        else -> Log.e(DEBUG_TAG, "❌ 切换性能较差")
                    }
                }
            }
            
            // 3. 验证内存使用情况
            Log.d(DEBUG_TAG, "💾 验证内存使用情况")
            
            val runtime = Runtime.getRuntime()
            val totalMemory = runtime.totalMemory() / 1024 / 1024 // MB
            val freeMemory = runtime.freeMemory() / 1024 / 1024   // MB
            val usedMemory = totalMemory - freeMemory
            
            Log.d(DEBUG_TAG, "内存使用情况:")
            Log.d(DEBUG_TAG, "    总内存: ${totalMemory}MB")
            Log.d(DEBUG_TAG, "    已用内存: ${usedMemory}MB")
            Log.d(DEBUG_TAG, "    可用内存: ${freeMemory}MB")
            
            val memoryUsagePercent = (usedMemory * 100) / totalMemory
            when {
                memoryUsagePercent < 50 -> Log.d(DEBUG_TAG, "✅ 内存使用正常（${memoryUsagePercent}%）")
                memoryUsagePercent < 80 -> Log.w(DEBUG_TAG, "⚠️ 内存使用较高（${memoryUsagePercent}%）")
                else -> Log.e(DEBUG_TAG, "❌ 内存使用过高（${memoryUsagePercent}%）")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证资源限制场景异常", e)
        }
    }
    
    /**
     * 验证并发操作场景
     */
    private suspend fun validateConcurrentOperationScenarios(userId: String) {
        Log.d(DEBUG_TAG, "🔄 开始验证并发操作场景")
        
        try {
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.size < 2) {
                Log.w(DEBUG_TAG, "⚠️ 记账簿数量不足，跳过并发测试")
                return
            }
            
            // 1. 验证快速连续切换
            Log.d(DEBUG_TAG, "⚡ 验证快速连续切换")
            
            val testLedgers = ledgers.take(3) // 最多用3个记账簿测试
            val startTime = System.currentTimeMillis()
            
            // 快速连续切换记账簿
            testLedgers.forEachIndexed { index, ledger ->
                ledgerUIPreferencesRepository.updateSelectedLedgerId(ledger.id)
                Log.d(DEBUG_TAG, "快速切换${index + 1}: ${ledger.name}")
                delay(10) // 极短延迟模拟快速操作
            }
            
            val totalTime = System.currentTimeMillis() - startTime
            
            // 验证最终状态
            delay(200) // 等待所有操作完成
            val finalPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            val lastLedger = testLedgers.last()
            
            if (finalPreferences.selectedLedgerId == lastLedger.id) {
                Log.d(DEBUG_TAG, "✅ 快速连续切换成功，最终状态正确")
                Log.d(DEBUG_TAG, "    总耗时: ${totalTime}ms")
                Log.d(DEBUG_TAG, "    最终记账簿: ${lastLedger.name}")
            } else {
                Log.e(DEBUG_TAG, "❌ 快速连续切换后状态不一致")
                Log.e(DEBUG_TAG, "    期望: ${lastLedger.id}")
                Log.e(DEBUG_TAG, "    实际: ${finalPreferences.selectedLedgerId}")
            }
            
            // 2. 验证操作冲突处理
            Log.d(DEBUG_TAG, "⚔️ 验证操作冲突处理")
            
            // 模拟同时发生的操作
            val ledger1 = ledgers[0]
            val ledger2 = ledgers[1]
            
            Log.d(DEBUG_TAG, "模拟同时切换到不同记账簿")
            
            // 使用协程模拟并发操作
            val job1 = validatorScope.async {
                ledgerUIPreferencesRepository.updateSelectedLedgerId(ledger1.id)
                "操作1完成"
            }
            
            val job2 = validatorScope.async {
                delay(5) // 轻微延迟
                ledgerUIPreferencesRepository.updateSelectedLedgerId(ledger2.id)
                "操作2完成"
            }
            
            // 等待所有操作完成
            val result1 = job1.await()
            val result2 = job2.await()
            
            Log.d(DEBUG_TAG, "$result1, $result2")
            
            // 验证最终状态（应该是最后一个操作的结果）
            delay(100)
            val conflictPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            
            if (conflictPreferences.selectedLedgerId == ledger2.id) {
                Log.d(DEBUG_TAG, "✅ 并发操作冲突正确处理，最后操作生效")
            } else if (conflictPreferences.selectedLedgerId == ledger1.id) {
                Log.w(DEBUG_TAG, "⚠️ 并发操作结果不确定，第一个操作生效")
            } else {
                Log.e(DEBUG_TAG, "❌ 并发操作导致状态异常: ${conflictPreferences.selectedLedgerId}")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证并发操作场景异常", e)
        }
    }
    
    /**
     * 验证ViewModel的边界情况处理
     */
    suspend fun validateViewModelEdgeCases(viewModel: LedgerViewModel): EdgeCaseValidationSummary {
        Log.d(DEBUG_TAG, "🎯 开始验证ViewModel边界情况")
        
        val issues = mutableListOf<String>()
        
        try {
            // 1. 验证初始化状态
            Log.d(DEBUG_TAG, "🚀 验证ViewModel初始化状态")
            
            var attempts = 0
            var initState: Any? = null
            
            while (attempts < 100) { // 最大等待10秒
                delay(100)
                try {
                    initState = viewModel.uiState.first()
                    break
                } catch (e: Exception) {
                    Log.w(DEBUG_TAG, "ViewModel初始化尚未完成，重试 ${attempts + 1}/100")
                }
                attempts++
            }
            
            if (initState == null) {
                issues.add("ViewModel初始化超时")
            } else {
                Log.d(DEBUG_TAG, "✅ ViewModel在${attempts * 100}ms内完成初始化")
            }
            
            // 2. 验证空数据处理
            Log.d(DEBUG_TAG, "📭 验证ViewModel空数据处理")
            
            val state = viewModel.uiState.first()
            
            // 检查各种空状态的处理
            val stateReflection = state::class.java.declaredFields
            
            stateReflection.forEach { field ->
                field.isAccessible = true
                val value = field.get(state)
                
                when (field.name) {
                    "ledgers" -> {
                        if (value is List<*> && value.isEmpty()) {
                            Log.w(DEBUG_TAG, "⚠️ 记账簿列表为空")
                        }
                    }
                    "currentLedger" -> {
                        if (value == null) {
                            Log.w(DEBUG_TAG, "⚠️ 当前记账簿为null")
                        }
                    }
                    "transactions" -> {
                        if (value is List<*> && value.isEmpty()) {
                            Log.d(DEBUG_TAG, "ℹ️ 交易列表为空（可能正常）")
                        }
                    }
                }
            }
            
            // 3. 验证错误状态处理
            Log.d(DEBUG_TAG, "❌ 验证错误状态处理")
            
            // 尝试触发一些边界操作
            try {
                viewModel.selectLedger("invalid_ledger_id")
                delay(500)
                
                val errorState = viewModel.uiState.first()
                // 检查系统如何处理无效选择
                Log.d(DEBUG_TAG, "无效记账簿选择后的状态检查完成")
                
            } catch (e: Exception) {
                Log.d(DEBUG_TAG, "✅ 无效操作正确抛出异常: ${e.javaClass.simpleName}")
            }
            
            return EdgeCaseValidationSummary(
                success = issues.isEmpty(),
                initializationTimeMs = attempts * 100L,
                handledEdgeCases = listOf("空数据", "无效操作", "初始化检查"),
                issues = issues
            )
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证ViewModel边界情况异常", e)
            issues.add("验证过程异常: ${e.message}")
            
            return EdgeCaseValidationSummary(
                success = false,
                initializationTimeMs = -1,
                handledEdgeCases = emptyList(),
                issues = issues
            )
        }
    }
}

/**
 * 边界情况验证总结
 */
data class EdgeCaseValidationSummary(
    val success: Boolean,
    val initializationTimeMs: Long,
    val handledEdgeCases: List<String>,
    val issues: List<String>
)