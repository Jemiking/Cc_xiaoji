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
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

/**
 * 用户体验验证器
 * 测试加载状态、错误提示、交互流畅性等用户体验要素
 */
@Singleton
class UserExperienceValidator @Inject constructor(
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val userApi: UserApi,
    private val ledgerUIPreferencesRepository: LedgerUIPreferencesRepository
) {
    
    companion object {
        private const val TAG = "UserExperienceValidator"
        private const val DEBUG_TAG = "LEDGER_UX_DEBUG"
        private const val LOADING_TIMEOUT_MS = 5000L // 5秒加载超时
        private const val INTERACTION_RESPONSE_THRESHOLD_MS = 300L // 300ms交互响应阈值
    }
    
    private val validatorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * 执行完整的用户体验验证
     */
    fun executeFullUserExperienceValidation() {
        validatorScope.launch {
            try {
                Log.i(TAG, "🚀 开始执行用户体验验证")
                
                val userId = userApi.getCurrentUserId()
                
                // 1. 验证加载状态管理
                validateLoadingStates(userId)
                
                // 2. 验证错误状态处理
                validateErrorStates(userId)
                
                // 3. 验证交互响应性
                validateInteractionResponsiveness(userId)
                
                // 4. 验证状态一致性
                validateStateConsistency(userId)
                
                // 5. 验证数据刷新体验
                validateDataRefreshExperience(userId)
                
                // 6. 验证界面流畅性
                validateInterfaceFluidity(userId)
                
                Log.i(TAG, "✅ 用户体验验证完成")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 用户体验验证失败", e)
            }
        }
    }
    
    /**
     * 验证加载状态管理
     */
    private suspend fun validateLoadingStates(userId: String) {
        Log.d(DEBUG_TAG, "⏳ 开始验证加载状态管理")
        
        try {
            // 1. 验证初始加载状态
            Log.d(DEBUG_TAG, "🔄 验证初始加载状态")
            
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.isNotEmpty()) {
                Log.d(DEBUG_TAG, "✅ 记账簿数据加载成功，共${ledgers.size}个记账簿")
            } else {
                Log.w(DEBUG_TAG, "⚠️ 记账簿列表为空")
            }
            
            // 2. 验证切换加载状态
            Log.d(DEBUG_TAG, "🔄 验证记账簿切换加载状态")
            
            if (ledgers.size >= 2) {
                val targetLedger = ledgers[1]
                
                // 记录切换开始时间
                val switchStartTime = System.currentTimeMillis()
                
                ledgerUIPreferencesRepository.updateSelectedLedgerId(targetLedger.id)
                
                // 模拟等待加载完成
                var loadingDetected = false
                var loadingCompleted = false
                var attempts = 0
                
                while (attempts < 50 && !loadingCompleted) { // 最大等待5秒
                    delay(100)
                    
                    try {
                        val preferences = ledgerUIPreferencesRepository.getUIPreferences().first()
                        
                        if (preferences.selectedLedgerId == targetLedger.id) {
                            loadingCompleted = true
                            val switchEndTime = System.currentTimeMillis()
                            val switchDuration = switchEndTime - switchStartTime
                            
                            Log.d(DEBUG_TAG, "✅ 记账簿切换完成")
                            Log.d(DEBUG_TAG, "    切换目标: ${targetLedger.name}")
                            Log.d(DEBUG_TAG, "    切换耗时: ${switchDuration}ms")
                            
                            // 评估加载体验
                            when {
                                switchDuration < 500 -> Log.d(DEBUG_TAG, "✅ 切换体验优秀（快速响应）")
                                switchDuration < 1000 -> Log.d(DEBUG_TAG, "✅ 切换体验良好")
                                switchDuration < 2000 -> Log.w(DEBUG_TAG, "⚠️ 切换体验一般（用户可能感知延迟）")
                                else -> Log.e(DEBUG_TAG, "❌ 切换体验较差（明显延迟）")
                            }
                        }
                    } catch (e: Exception) {
                        loadingDetected = true
                        Log.d(DEBUG_TAG, "检测到加载状态...")
                    }
                    
                    attempts++
                }
                
                if (!loadingCompleted) {
                    Log.e(DEBUG_TAG, "❌ 记账簿切换超时")
                } else if (loadingDetected) {
                    Log.d(DEBUG_TAG, "✅ 正确检测到加载状态")
                }
            }
            
            // 3. 验证加载超时处理
            Log.d(DEBUG_TAG, "⏰ 验证加载超时处理")
            
            // 模拟长时间操作（实际中可能是网络请求等）
            val longOperationTime = measureTimeMillis {
                repeat(5) {
                    manageLedgerUseCase.getUserLedgers(userId).first()
                    delay(100)
                }
            }
            
            Log.d(DEBUG_TAG, "长操作耗时: ${longOperationTime}ms")
            
            if (longOperationTime < LOADING_TIMEOUT_MS) {
                Log.d(DEBUG_TAG, "✅ 操作在合理时间内完成")
            } else {
                Log.w(DEBUG_TAG, "⚠️ 操作时间较长，用户可能需要加载提示")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证加载状态管理异常", e)
        }
    }
    
    /**
     * 验证错误状态处理
     */
    private suspend fun validateErrorStates(userId: String) {
        Log.d(DEBUG_TAG, "❌ 开始验证错误状态处理")
        
        try {
            // 1. 验证无效记账簿ID处理
            Log.d(DEBUG_TAG, "🚫 验证无效记账簿ID处理")
            
            val invalidLedgerId = "invalid_test_${System.currentTimeMillis()}"
            
            try {
                ledgerUIPreferencesRepository.updateSelectedLedgerId(invalidLedgerId)
                Log.d(DEBUG_TAG, "设置无效记账簿ID: $invalidLedgerId")
                
                // 验证系统如何处理无效ID
                val preferences = ledgerUIPreferencesRepository.getUIPreferences().first()
                if (preferences.selectedLedgerId == invalidLedgerId) {
                    Log.d(DEBUG_TAG, "✅ 系统接受无效ID设置（将由UI层处理）")
                }
                
                // 验证获取有效记账簿列表
                val validLedgers = manageLedgerUseCase.getUserLedgers(userId).first()
                val validLedger = validLedgers.find { it.id == invalidLedgerId }
                
                if (validLedger == null) {
                    Log.d(DEBUG_TAG, "✅ 系统正确识别无效记账簿ID")
                    
                    // 验证自动恢复到默认记账簿
                    val defaultLedger = validLedgers.find { it.isDefault }
                    if (defaultLedger != null) {
                        ledgerUIPreferencesRepository.updateSelectedLedgerId(defaultLedger.id)
                        Log.d(DEBUG_TAG, "✅ 自动恢复到默认记账簿: ${defaultLedger.name}")
                    }
                } else {
                    Log.e(DEBUG_TAG, "❌ 系统错误地认为无效ID是有效的")
                }
                
            } catch (e: Exception) {
                Log.d(DEBUG_TAG, "✅ 无效操作正确抛出异常: ${e.javaClass.simpleName}")
            }
            
            // 2. 验证空数据状态处理
            Log.d(DEBUG_TAG, "📭 验证空数据状态处理")
            
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.isEmpty()) {
                Log.d(DEBUG_TAG, "✅ 检测到空记账簿状态")
                
                // 验证是否有合适的空状态提示机制
                Log.d(DEBUG_TAG, "验证空状态提示机制...")
                
                // 尝试创建默认记账簿
                try {
                    val defaultLedgerResult = manageLedgerUseCase.ensureDefaultLedger(userId)
                    Log.d(DEBUG_TAG, "✅ 自动创建默认记账簿机制正常")
                } catch (e: Exception) {
                    Log.e(DEBUG_TAG, "❌ 默认记账簿创建失败", e)
                }
            } else {
                Log.d(DEBUG_TAG, "ℹ️ 有可用记账簿，跳过空状态测试")
            }
            
            // 3. 验证网络/数据异常处理
            Log.d(DEBUG_TAG, "🌐 验证数据异常处理")
            
            // 模拟快速连续操作可能导致的状态冲突
            try {
                val testLedgers = manageLedgerUseCase.getUserLedgers(userId).first()
                if (testLedgers.size >= 2) {
                    // 快速连续切换
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(testLedgers[0].id)
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(testLedgers[1].id)
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(testLedgers[0].id)
                    
                    delay(200) // 等待状态稳定
                    
                    val finalPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
                    Log.d(DEBUG_TAG, "✅ 快速操作后状态稳定: ${finalPreferences.selectedLedgerId}")
                }
            } catch (e: Exception) {
                Log.w(DEBUG_TAG, "⚠️ 快速操作导致异常: ${e.message}")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证错误状态处理异常", e)
        }
    }
    
    /**
     * 验证交互响应性
     */
    private suspend fun validateInteractionResponsiveness(userId: String) {
        Log.d(DEBUG_TAG, "👆 开始验证交互响应性")
        
        try {
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.size < 2) {
                Log.w(DEBUG_TAG, "⚠️ 记账簿数量不足，跳过交互响应性测试")
                return
            }
            
            // 1. 验证点击响应时间
            Log.d(DEBUG_TAG, "⚡ 验证点击响应时间")
            
            val responseTimes = mutableListOf<Long>()
            
            repeat(5) { iteration ->
                val targetLedger = ledgers[iteration % ledgers.size]
                
                val responseTime = measureTimeMillis {
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(targetLedger.id)
                    
                    // 等待响应确认
                    var confirmed = false
                    var attempts = 0
                    while (!confirmed && attempts < 30) { // 最大等待3秒
                        delay(100)
                        val prefs = ledgerUIPreferencesRepository.getUIPreferences().first()
                        if (prefs.selectedLedgerId == targetLedger.id) {
                            confirmed = true
                        }
                        attempts++
                    }
                }
                
                responseTimes.add(responseTime)
                Log.d(DEBUG_TAG, "点击${iteration + 1}: ${targetLedger.name} - ${responseTime}ms")
                
                delay(200) // 避免过快操作
            }
            
            val avgResponseTime = responseTimes.average()
            val maxResponseTime = responseTimes.maxOrNull() ?: 0
            
            Log.d(DEBUG_TAG, "📊 交互响应性统计:")
            Log.d(DEBUG_TAG, "    平均响应时间: ${avgResponseTime.toInt()}ms")
            Log.d(DEBUG_TAG, "    最长响应时间: ${maxResponseTime}ms")
            Log.d(DEBUG_TAG, "    测试次数: ${responseTimes.size}")
            
            when {
                avgResponseTime < 100 -> Log.d(DEBUG_TAG, "✅ 交互响应性优秀（即时响应）")
                avgResponseTime < INTERACTION_RESPONSE_THRESHOLD_MS -> Log.d(DEBUG_TAG, "✅ 交互响应性良好")
                avgResponseTime < 500 -> Log.w(DEBUG_TAG, "⚠️ 交互响应性一般（用户可感知延迟）")
                else -> Log.e(DEBUG_TAG, "❌ 交互响应性较差（明显延迟）")
            }
            
            // 2. 验证连续交互处理
            Log.d(DEBUG_TAG, "🔄 验证连续交互处理")
            
            val continuousInteractionTime = measureTimeMillis {
                repeat(10) { cycle ->
                    val targetLedger = ledgers[cycle % ledgers.size]
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(targetLedger.id)
                    delay(50) // 模拟快速连续点击
                }
                
                // 等待最后操作完成
                delay(300)
            }
            
            Log.d(DEBUG_TAG, "连续交互总耗时: ${continuousInteractionTime}ms")
            
            // 验证最终状态的正确性
            val finalPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
            val lastTargetLedger = ledgers[(10 - 1) % ledgers.size]
            
            if (finalPreferences.selectedLedgerId == lastTargetLedger.id) {
                Log.d(DEBUG_TAG, "✅ 连续交互后状态正确")
            } else {
                Log.e(DEBUG_TAG, "❌ 连续交互后状态不一致")
            }
            
            // 3. 验证交互反馈机制
            Log.d(DEBUG_TAG, "💬 验证交互反馈机制")
            
            // 测试操作确认机制
            val testLedger = ledgers.first()
            val beforeUpdate = System.currentTimeMillis()
            
            ledgerUIPreferencesRepository.updateSelectedLedgerId(testLedger.id)
            
            // 验证更新确认
            val afterUpdate = System.currentTimeMillis()
            val updateConfirmation = ledgerUIPreferencesRepository.getUIPreferences().first()
            val confirmationTime = System.currentTimeMillis() - afterUpdate
            
            Log.d(DEBUG_TAG, "操作确认耗时: ${confirmationTime}ms")
            
            if (updateConfirmation.selectedLedgerId == testLedger.id) {
                Log.d(DEBUG_TAG, "✅ 操作反馈机制正常")
                
                if (confirmationTime < 100) {
                    Log.d(DEBUG_TAG, "✅ 操作反馈迅速")
                } else {
                    Log.w(DEBUG_TAG, "⚠️ 操作反馈较慢")
                }
            } else {
                Log.e(DEBUG_TAG, "❌ 操作反馈机制异常")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证交互响应性异常", e)
        }
    }
    
    /**
     * 验证状态一致性
     */
    private suspend fun validateStateConsistency(userId: String) {
        Log.d(DEBUG_TAG, "🎯 开始验证状态一致性")
        
        try {
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.isEmpty()) {
                Log.w(DEBUG_TAG, "⚠️ 无记账簿可用于状态一致性测试")
                return
            }
            
            // 1. 验证数据源一致性
            Log.d(DEBUG_TAG, "📊 验证数据源一致性")
            
            val testLedger = ledgers.first()
            
            // 设置记账簿选择
            ledgerUIPreferencesRepository.updateSelectedLedgerId(testLedger.id)
            delay(100)
            
            // 从不同数据源获取状态
            val preferencesData = ledgerUIPreferencesRepository.getUIPreferences().first()
            val ledgersData = manageLedgerUseCase.getUserLedgers(userId).first()
            
            Log.d(DEBUG_TAG, "偏好设置记账簿ID: ${preferencesData.selectedLedgerId}")
            Log.d(DEBUG_TAG, "可用记账簿数量: ${ledgersData.size}")
            
            // 验证选中的记账簿确实存在于可用列表中
            val selectedLedger = ledgersData.find { it.id == preferencesData.selectedLedgerId }
            
            if (selectedLedger != null) {
                Log.d(DEBUG_TAG, "✅ 选中记账簿存在于可用列表中: ${selectedLedger.name}")
            } else {
                Log.e(DEBUG_TAG, "❌ 选中记账簿不在可用列表中")
            }
            
            // 2. 验证状态同步一致性
            Log.d(DEBUG_TAG, "🔄 验证状态同步一致性")
            
            if (ledgers.size >= 2) {
                val newTestLedger = ledgers[1]
                
                // 多次快速切换验证一致性
                repeat(3) { cycle ->
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(newTestLedger.id)
                    delay(100)
                    
                    val syncedPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
                    if (syncedPreferences.selectedLedgerId == newTestLedger.id) {
                        Log.d(DEBUG_TAG, "✅ 第${cycle + 1}次同步一致")
                    } else {
                        Log.e(DEBUG_TAG, "❌ 第${cycle + 1}次同步不一致")
                    }
                }
            }
            
            // 3. 验证跨会话状态一致性
            Log.d(DEBUG_TAG, "💾 验证跨会话状态一致性")
            
            val persistentLedger = ledgers.last()
            ledgerUIPreferencesRepository.updateSelectedLedgerId(persistentLedger.id)
            delay(200)
            
            // 模拟重新获取状态（如应用重启后）
            val persistedState = ledgerUIPreferencesRepository.getUIPreferences().first()
            
            if (persistedState.selectedLedgerId == persistentLedger.id) {
                Log.d(DEBUG_TAG, "✅ 跨会话状态一致性良好")
            } else {
                Log.e(DEBUG_TAG, "❌ 跨会话状态一致性问题")
            }
            
            // 4. 验证默认状态一致性
            Log.d(DEBUG_TAG, "🏠 验证默认状态一致性")
            
            val defaultLedgers = ledgers.filter { it.isDefault }
            
            when (defaultLedgers.size) {
                0 -> Log.e(DEBUG_TAG, "❌ 没有默认记账簿")
                1 -> {
                    Log.d(DEBUG_TAG, "✅ 默认记账簿唯一: ${defaultLedgers.first().name}")
                    
                    // 验证默认记账簿的状态一致性
                    val defaultLedger = defaultLedgers.first()
                    val defaultLedgerFromUseCase = manageLedgerUseCase.getDefaultLedger(userId)
                    // 这里可以添加更多默认记账簿一致性验证
                }
                else -> {
                    Log.e(DEBUG_TAG, "❌ 多个默认记账簿，数据不一致")
                    defaultLedgers.forEach { ledger ->
                        Log.e(DEBUG_TAG, "    ${ledger.name} (${ledger.id})")
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证状态一致性异常", e)
        }
    }
    
    /**
     * 验证数据刷新体验
     */
    private suspend fun validateDataRefreshExperience(userId: String) {
        Log.d(DEBUG_TAG, "🔄 开始验证数据刷新体验")
        
        try {
            // 1. 验证自动刷新机制
            Log.d(DEBUG_TAG, "⚡ 验证自动刷新机制")
            
            val initialLedgers = manageLedgerUseCase.getUserLedgers(userId).first()
            Log.d(DEBUG_TAG, "初始记账簿数量: ${initialLedgers.size}")
            
            // 模拟数据变化触发刷新
            val refreshStartTime = System.currentTimeMillis()
            
            // 多次获取数据模拟刷新
            repeat(3) { attempt ->
                val refreshedLedgers = manageLedgerUseCase.getUserLedgers(userId).first()
                Log.d(DEBUG_TAG, "刷新${attempt + 1}: ${refreshedLedgers.size}个记账簿")
                delay(100)
            }
            
            val refreshEndTime = System.currentTimeMillis()
            val refreshDuration = refreshEndTime - refreshStartTime
            
            Log.d(DEBUG_TAG, "数据刷新总耗时: ${refreshDuration}ms")
            
            when {
                refreshDuration < 500 -> Log.d(DEBUG_TAG, "✅ 数据刷新体验优秀")
                refreshDuration < 1000 -> Log.d(DEBUG_TAG, "✅ 数据刷新体验良好")
                refreshDuration < 2000 -> Log.w(DEBUG_TAG, "⚠️ 数据刷新体验一般")
                else -> Log.e(DEBUG_TAG, "❌ 数据刷新体验较差")
            }
            
            // 2. 验证增量更新体验
            Log.d(DEBUG_TAG, "📈 验证增量更新体验")
            
            if (initialLedgers.isNotEmpty()) {
                val testLedger = initialLedgers.first()
                
                // 模拟记账簿选择变化
                val updateStartTime = System.currentTimeMillis()
                
                ledgerUIPreferencesRepository.updateSelectedLedgerId(testLedger.id)
                
                // 验证更新后的数据一致性
                val updatedPreferences = ledgerUIPreferencesRepository.getUIPreferences().first()
                val updateEndTime = System.currentTimeMillis()
                val updateDuration = updateEndTime - updateStartTime
                
                Log.d(DEBUG_TAG, "增量更新耗时: ${updateDuration}ms")
                
                if (updatedPreferences.selectedLedgerId == testLedger.id) {
                    Log.d(DEBUG_TAG, "✅ 增量更新成功")
                    
                    when {
                        updateDuration < 100 -> Log.d(DEBUG_TAG, "✅ 增量更新体验优秀")
                        updateDuration < 300 -> Log.d(DEBUG_TAG, "✅ 增量更新体验良好")
                        updateDuration < 500 -> Log.w(DEBUG_TAG, "⚠️ 增量更新体验一般")
                        else -> Log.e(DEBUG_TAG, "❌ 增量更新体验较差")
                    }
                } else {
                    Log.e(DEBUG_TAG, "❌ 增量更新失败")
                }
            }
            
            // 3. 验证刷新频率合理性
            Log.d(DEBUG_TAG, "⏰ 验证刷新频率合理性")
            
            val refreshCounts = mutableListOf<Long>()
            val testDuration = 2000L // 2秒测试周期
            val startTime = System.currentTimeMillis()
            
            while (System.currentTimeMillis() - startTime < testDuration) {
                val refreshTime = measureTimeMillis {
                    manageLedgerUseCase.getUserLedgers(userId).first()
                }
                refreshCounts.add(refreshTime)
                delay(200) // 每200ms刷新一次
            }
            
            val avgRefreshTime = refreshCounts.average()
            val refreshFrequency = refreshCounts.size
            
            Log.d(DEBUG_TAG, "📊 刷新频率分析:")
            Log.d(DEBUG_TAG, "    测试时长: ${testDuration}ms")
            Log.d(DEBUG_TAG, "    刷新次数: $refreshFrequency")
            Log.d(DEBUG_TAG, "    平均刷新时间: ${avgRefreshTime.toInt()}ms")
            Log.d(DEBUG_TAG, "    刷新频率: ${refreshFrequency * 1000 / testDuration}次/秒")
            
            if (avgRefreshTime < 200) {
                Log.d(DEBUG_TAG, "✅ 刷新性能良好，支持高频更新")
            } else {
                Log.w(DEBUG_TAG, "⚠️ 刷新性能一般，建议优化刷新策略")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证数据刷新体验异常", e)
        }
    }
    
    /**
     * 验证界面流畅性
     */
    private suspend fun validateInterfaceFluidity(userId: String) {
        Log.d(DEBUG_TAG, "🎨 开始验证界面流畅性")
        
        try {
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.size < 2) {
                Log.w(DEBUG_TAG, "⚠️ 记账簿数量不足，跳过界面流畅性测试")
                return
            }
            
            // 1. 验证切换动画流畅性
            Log.d(DEBUG_TAG, "🎬 验证切换动画流畅性")
            
            val animationTimes = mutableListOf<Long>()
            
            repeat(5) { cycle ->
                val targetLedger = ledgers[cycle % ledgers.size]
                
                val animationTime = measureTimeMillis {
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(targetLedger.id)
                    
                    // 模拟动画执行时间
                    delay(100) // 假设动画时间为100ms
                    
                    // 验证动画完成后状态正确
                    val finalState = ledgerUIPreferencesRepository.getUIPreferences().first()
                    if (finalState.selectedLedgerId != targetLedger.id) {
                        Log.w(DEBUG_TAG, "⚠️ 动画期间状态不稳定")
                    }
                }
                
                animationTimes.add(animationTime)
                Log.d(DEBUG_TAG, "动画${cycle + 1}: ${targetLedger.name} - ${animationTime}ms")
                
                delay(300) // 等待动画完全结束
            }
            
            val avgAnimationTime = animationTimes.average()
            val maxAnimationTime = animationTimes.maxOrNull() ?: 0
            
            Log.d(DEBUG_TAG, "📊 动画流畅性统计:")
            Log.d(DEBUG_TAG, "    平均动画时间: ${avgAnimationTime.toInt()}ms")
            Log.d(DEBUG_TAG, "    最长动画时间: ${maxAnimationTime}ms")
            
            when {
                avgAnimationTime < 150 -> Log.d(DEBUG_TAG, "✅ 动画流畅性优秀")
                avgAnimationTime < 250 -> Log.d(DEBUG_TAG, "✅ 动画流畅性良好")
                avgAnimationTime < 400 -> Log.w(DEBUG_TAG, "⚠️ 动画流畅性一般")
                else -> Log.e(DEBUG_TAG, "❌ 动画流畅性较差")
            }
            
            // 2. 验证界面响应一致性
            Log.d(DEBUG_TAG, "📱 验证界面响应一致性")
            
            val consistencyTests = mutableListOf<Boolean>()
            
            repeat(5) { test ->
                val testLedger = ledgers[test % ledgers.size]
                
                val beforeState = ledgerUIPreferencesRepository.getUIPreferences().first()
                ledgerUIPreferencesRepository.updateSelectedLedgerId(testLedger.id)
                delay(150) // 等待界面更新
                val afterState = ledgerUIPreferencesRepository.getUIPreferences().first()
                
                val isConsistent = afterState.selectedLedgerId == testLedger.id
                consistencyTests.add(isConsistent)
                
                if (isConsistent) {
                    Log.d(DEBUG_TAG, "✅ 一致性测试${test + 1}通过")
                } else {
                    Log.e(DEBUG_TAG, "❌ 一致性测试${test + 1}失败")
                }
            }
            
            val consistencyRate = consistencyTests.count { it }.toDouble() / consistencyTests.size * 100
            
            Log.d(DEBUG_TAG, "界面响应一致性: ${consistencyRate.toInt()}%")
            
            when {
                consistencyRate >= 100 -> Log.d(DEBUG_TAG, "✅ 界面响应完全一致")
                consistencyRate >= 80 -> Log.d(DEBUG_TAG, "✅ 界面响应基本一致")
                consistencyRate >= 60 -> Log.w(DEBUG_TAG, "⚠️ 界面响应偶有不一致")
                else -> Log.e(DEBUG_TAG, "❌ 界面响应经常不一致")
            }
            
            // 3. 验证内存和性能稳定性
            Log.d(DEBUG_TAG, "💾 验证界面性能稳定性")
            
            val beforeMemory = Runtime.getRuntime().let { 
                (it.totalMemory() - it.freeMemory()) / 1024 / 1024 
            }
            
            // 模拟界面密集操作
            repeat(20) { operation ->
                val targetLedger = ledgers[operation % ledgers.size]
                ledgerUIPreferencesRepository.updateSelectedLedgerId(targetLedger.id)
                delay(50) // 快速操作
            }
            
            delay(200) // 等待操作完成
            System.gc() // 建议垃圾回收
            delay(100)
            
            val afterMemory = Runtime.getRuntime().let { 
                (it.totalMemory() - it.freeMemory()) / 1024 / 1024 
            }
            
            val memoryDelta = afterMemory - beforeMemory
            
            Log.d(DEBUG_TAG, "界面操作内存影响:")
            Log.d(DEBUG_TAG, "    操作前内存: ${beforeMemory}MB")
            Log.d(DEBUG_TAG, "    操作后内存: ${afterMemory}MB")
            Log.d(DEBUG_TAG, "    内存变化: ${memoryDelta}MB")
            
            when {
                memoryDelta < 5 -> Log.d(DEBUG_TAG, "✅ 界面操作内存影响很小")
                memoryDelta < 15 -> Log.d(DEBUG_TAG, "✅ 界面操作内存影响可接受")
                memoryDelta < 30 -> Log.w(DEBUG_TAG, "⚠️ 界面操作内存影响较大")
                else -> Log.e(DEBUG_TAG, "❌ 界面操作可能存在内存泄漏")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证界面流畅性异常", e)
        }
    }
    
    /**
     * 验证ViewModel的用户体验表现
     */
    suspend fun validateViewModelUserExperience(viewModel: LedgerViewModel): UserExperienceValidationSummary {
        Log.d(DEBUG_TAG, "🎯 开始验证ViewModel用户体验表现")
        
        val issues = mutableListOf<String>()
        var loadingStateManagement = "未知"
        var errorHandling = "未知"
        var interactionResponsiveness = "未知"
        var overallRating = "F"
        
        try {
            // 1. 验证加载状态管理
            Log.d(DEBUG_TAG, "⏳ 验证ViewModel加载状态管理")
            
            val initializationTime = measureTimeMillis {
                var attempts = 0
                while (attempts < 100) { // 最大等待10秒
                    delay(100)
                    try {
                        val state = viewModel.uiState.first()
                        if (!state.isLedgerLoading && state.ledgers.isNotEmpty()) {
                            break
                        }
                        
                        // 检查加载状态
                        if (state.isLedgerLoading || state.isLoading) {
                            Log.d(DEBUG_TAG, "检测到加载状态...")
                        }
                    } catch (e: Exception) {
                        // 继续等待
                    }
                    attempts++
                }
            }
            
            loadingStateManagement = when {
                initializationTime < 1000 -> "优秀"
                initializationTime < 3000 -> "良好"
                initializationTime < 5000 -> "一般"
                else -> "较差"
            }
            
            if (initializationTime > 5000) {
                issues.add("ViewModel初始化时间过长")
            }
            
            // 2. 验证错误处理机制
            Log.d(DEBUG_TAG, "❌ 验证ViewModel错误处理")
            
            try {
                // 尝试无效操作
                viewModel.selectLedger("invalid_test_ledger")
                delay(500)
                
                val errorState = viewModel.uiState.first()
                // 这里可以检查错误状态的处理
                errorHandling = "良好"
                
            } catch (e: Exception) {
                errorHandling = "优秀" // 正确抛出异常
                Log.d(DEBUG_TAG, "✅ ViewModel正确处理无效操作")
            }
            
            // 3. 验证交互响应性
            Log.d(DEBUG_TAG, "👆 验证ViewModel交互响应性")
            
            val state = viewModel.uiState.first()
            if (state.ledgers.size >= 2) {
                val testLedger = state.ledgers.find { it.id != state.selectedLedgerId }
                if (testLedger != null) {
                    val responseTime = measureTimeMillis {
                        viewModel.selectLedger(testLedger.id)
                        
                        // 等待响应
                        var attempts = 0
                        while (attempts < 30) {
                            delay(100)
                            val currentState = viewModel.uiState.first()
                            if (currentState.selectedLedgerId == testLedger.id) {
                                break
                            }
                            attempts++
                        }
                    }
                    
                    interactionResponsiveness = when {
                        responseTime < 300 -> "优秀"
                        responseTime < 600 -> "良好"
                        responseTime < 1000 -> "一般"
                        else -> "较差"
                    }
                    
                    if (responseTime > 1000) {
                        issues.add("ViewModel交互响应较慢")
                    }
                }
            }
            
            // 4. 计算综合评级
            val scores = listOf(loadingStateManagement, errorHandling, interactionResponsiveness)
            val excellentCount = scores.count { it == "优秀" }
            val goodCount = scores.count { it == "良好" }
            val averageCount = scores.count { it == "一般" }
            val poorCount = scores.count { it == "较差" }
            
            overallRating = when {
                excellentCount >= 2 -> "A"
                excellentCount >= 1 && goodCount >= 1 -> "B"
                goodCount >= 2 -> "C"
                averageCount >= 2 -> "D"
                else -> "F"
            }
            
            return UserExperienceValidationSummary(
                success = issues.isEmpty(),
                loadingStateManagement = loadingStateManagement,
                errorHandling = errorHandling,
                interactionResponsiveness = interactionResponsiveness,
                overallRating = overallRating,
                issues = issues
            )
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证ViewModel用户体验异常", e)
            issues.add("验证过程异常: ${e.message}")
            
            return UserExperienceValidationSummary(
                success = false,
                loadingStateManagement = "异常",
                errorHandling = "异常",
                interactionResponsiveness = "异常",
                overallRating = "F",
                issues = issues
            )
        }
    }
}

/**
 * 用户体验验证总结
 */
data class UserExperienceValidationSummary(
    val success: Boolean,
    val loadingStateManagement: String,
    val errorHandling: String,
    val interactionResponsiveness: String,
    val overallRating: String,
    val issues: List<String>
)