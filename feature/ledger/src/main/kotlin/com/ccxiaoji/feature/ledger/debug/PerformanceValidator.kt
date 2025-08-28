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
 * 性能验证器
 * 测试大数据量下的记账簿切换性能，验证缓存机制
 */
@Singleton
class PerformanceValidator @Inject constructor(
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val userApi: UserApi,
    private val ledgerUIPreferencesRepository: LedgerUIPreferencesRepository
) {
    
    companion object {
        private const val TAG = "PerformanceValidator"
        private const val DEBUG_TAG = "LEDGER_PERFORMANCE_DEBUG"
        private const val PERFORMANCE_THRESHOLD_MS = 1000L // 1秒性能阈值
        private const val MEMORY_WARNING_THRESHOLD_MB = 100L // 100MB内存警告阈值
    }
    
    private val validatorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * 执行完整的性能验证
     */
    fun executeFullPerformanceValidation() {
        validatorScope.launch {
            try {
                Log.i(TAG, "🚀 开始执行性能验证")
                
                val userId = userApi.getCurrentUserId()
                
                // 1. 验证记账簿加载性能
                validateLedgerLoadingPerformance(userId)
                
                // 2. 验证记账簿切换性能
                validateLedgerSwitchingPerformance(userId)
                
                // 3. 验证缓存机制效果
                validateCacheEffectiveness(userId)
                
                // 4. 验证内存使用情况
                validateMemoryUsage()
                
                // 5. 验证并发性能
                validateConcurrentPerformance(userId)
                
                // 6. 验证大数据量处理性能
                validateLargeDatasetPerformance(userId)
                
                Log.i(TAG, "✅ 性能验证完成")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 性能验证失败", e)
            }
        }
    }
    
    /**
     * 验证记账簿加载性能
     */
    private suspend fun validateLedgerLoadingPerformance(userId: String) {
        Log.d(DEBUG_TAG, "⚡ 开始验证记账簿加载性能")
        
        try {
            val runs = 10
            val loadTimes = mutableListOf<Long>()
            
            repeat(runs) { run ->
                val loadTime = measureTimeMillis {
                    manageLedgerUseCase.getUserLedgers(userId).first()
                }
                loadTimes.add(loadTime)
                Log.d(DEBUG_TAG, "第${run + 1}次加载: ${loadTime}ms")
                
                // 短暂延迟避免缓存影响
                delay(100)
            }
            
            val avgTime = loadTimes.average()
            val minTime = loadTimes.minOrNull() ?: 0
            val maxTime = loadTimes.maxOrNull() ?: 0
            
            Log.d(DEBUG_TAG, "📊 记账簿加载性能统计:")
            Log.d(DEBUG_TAG, "    平均时间: ${avgTime.toInt()}ms")
            Log.d(DEBUG_TAG, "    最短时间: ${minTime}ms")
            Log.d(DEBUG_TAG, "    最长时间: ${maxTime}ms")
            Log.d(DEBUG_TAG, "    测试次数: $runs")
            
            when {
                avgTime < 100 -> Log.d(DEBUG_TAG, "✅ 加载性能优秀")
                avgTime < 300 -> Log.d(DEBUG_TAG, "✅ 加载性能良好")
                avgTime < PERFORMANCE_THRESHOLD_MS -> Log.w(DEBUG_TAG, "⚠️ 加载性能一般")
                else -> Log.e(DEBUG_TAG, "❌ 加载性能较差")
            }
            
            // 验证性能一致性
            val performanceVariance = loadTimes.map { (it - avgTime) * (it - avgTime) }.average()
            val performanceStdDev = kotlin.math.sqrt(performanceVariance)
            
            Log.d(DEBUG_TAG, "📈 性能一致性: 标准差=${performanceStdDev.toInt()}ms")
            if (performanceStdDev < avgTime * 0.3) {
                Log.d(DEBUG_TAG, "✅ 性能一致性良好")
            } else {
                Log.w(DEBUG_TAG, "⚠️ 性能波动较大")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证记账簿加载性能异常", e)
        }
    }
    
    /**
     * 验证记账簿切换性能
     */
    private suspend fun validateLedgerSwitchingPerformance(userId: String) {
        Log.d(DEBUG_TAG, "🔄 开始验证记账簿切换性能")
        
        try {
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.size < 2) {
                Log.w(DEBUG_TAG, "⚠️ 记账簿数量不足，跳过切换性能测试")
                return
            }
            
            val switchTimes = mutableListOf<Long>()
            val testCycles = minOf(ledgers.size * 2, 20) // 最多测试20次
            
            repeat(testCycles) { cycle ->
                val targetLedger = ledgers[cycle % ledgers.size]
                
                val switchTime = measureTimeMillis {
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(targetLedger.id)
                    
                    // 等待切换完成（验证偏好设置已更新）
                    var confirmed = false
                    var attempts = 0
                    while (!confirmed && attempts < 50) {
                        val prefs = ledgerUIPreferencesRepository.getUIPreferences().first()
                        if (prefs.selectedLedgerId == targetLedger.id) {
                            confirmed = true
                        } else {
                            delay(10)
                            attempts++
                        }
                    }
                }
                
                switchTimes.add(switchTime)
                Log.d(DEBUG_TAG, "切换${cycle + 1}: ${targetLedger.name} (${switchTime}ms)")
                
                delay(50) // 避免过于频繁的切换
            }
            
            val avgSwitchTime = switchTimes.average()
            val minSwitchTime = switchTimes.minOrNull() ?: 0
            val maxSwitchTime = switchTimes.maxOrNull() ?: 0
            
            Log.d(DEBUG_TAG, "📊 记账簿切换性能统计:")
            Log.d(DEBUG_TAG, "    平均时间: ${avgSwitchTime.toInt()}ms")
            Log.d(DEBUG_TAG, "    最短时间: ${minSwitchTime}ms")
            Log.d(DEBUG_TAG, "    最长时间: ${maxSwitchTime}ms")
            Log.d(DEBUG_TAG, "    测试次数: $testCycles")
            
            when {
                avgSwitchTime < 50 -> Log.d(DEBUG_TAG, "✅ 切换性能优秀")
                avgSwitchTime < 150 -> Log.d(DEBUG_TAG, "✅ 切换性能良好")
                avgSwitchTime < 500 -> Log.w(DEBUG_TAG, "⚠️ 切换性能一般")
                else -> Log.e(DEBUG_TAG, "❌ 切换性能较差")
            }
            
            // 验证切换性能是否随时间退化
            val firstHalf = switchTimes.take(switchTimes.size / 2).average()
            val secondHalf = switchTimes.drop(switchTimes.size / 2).average()
            val degradation = ((secondHalf - firstHalf) / firstHalf) * 100
            
            Log.d(DEBUG_TAG, "📉 性能退化分析: ${degradation.toInt()}%")
            if (degradation < 10) {
                Log.d(DEBUG_TAG, "✅ 性能稳定，无明显退化")
            } else {
                Log.w(DEBUG_TAG, "⚠️ 检测到性能退化趋势")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证记账簿切换性能异常", e)
        }
    }
    
    /**
     * 验证缓存机制效果
     */
    private suspend fun validateCacheEffectiveness(userId: String) {
        Log.d(DEBUG_TAG, "💾 开始验证缓存机制效果")
        
        try {
            // 1. 冷启动测试（清除缓存状态）
            Log.d(DEBUG_TAG, "🥶 测试冷启动性能")
            
            val coldStartTime = measureTimeMillis {
                manageLedgerUseCase.getUserLedgers(userId).first()
            }
            Log.d(DEBUG_TAG, "冷启动时间: ${coldStartTime}ms")
            
            // 2. 热启动测试（缓存预热）
            Log.d(DEBUG_TAG, "🔥 测试热启动性能")
            
            val warmupRuns = 3
            repeat(warmupRuns) {
                manageLedgerUseCase.getUserLedgers(userId).first()
                delay(50)
            }
            
            val hotStartTimes = mutableListOf<Long>()
            repeat(5) {
                val hotStartTime = measureTimeMillis {
                    manageLedgerUseCase.getUserLedgers(userId).first()
                }
                hotStartTimes.add(hotStartTime)
                delay(50)
            }
            
            val avgHotStartTime = hotStartTimes.average()
            Log.d(DEBUG_TAG, "热启动平均时间: ${avgHotStartTime.toInt()}ms")
            
            // 3. 缓存效果分析
            val cacheImprovement = ((coldStartTime - avgHotStartTime) / coldStartTime) * 100
            Log.d(DEBUG_TAG, "📈 缓存效果分析:")
            Log.d(DEBUG_TAG, "    冷启动: ${coldStartTime}ms")
            Log.d(DEBUG_TAG, "    热启动: ${avgHotStartTime.toInt()}ms")
            Log.d(DEBUG_TAG, "    性能提升: ${cacheImprovement.toInt()}%")
            
            when {
                cacheImprovement > 50 -> Log.d(DEBUG_TAG, "✅ 缓存效果优秀")
                cacheImprovement > 25 -> Log.d(DEBUG_TAG, "✅ 缓存效果良好")
                cacheImprovement > 10 -> Log.w(DEBUG_TAG, "⚠️ 缓存效果一般")
                else -> Log.e(DEBUG_TAG, "❌ 缓存效果不明显")
            }
            
            // 4. 缓存一致性测试
            Log.d(DEBUG_TAG, "🔍 测试缓存一致性")
            
            val consistency = hotStartTimes.map { kotlin.math.abs(it - avgHotStartTime) }.average()
            Log.d(DEBUG_TAG, "缓存一致性偏差: ${consistency.toInt()}ms")
            
            if (consistency < avgHotStartTime * 0.2) {
                Log.d(DEBUG_TAG, "✅ 缓存一致性良好")
            } else {
                Log.w(DEBUG_TAG, "⚠️ 缓存一致性有待改进")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证缓存机制效果异常", e)
        }
    }
    
    /**
     * 验证内存使用情况
     */
    private suspend fun validateMemoryUsage() {
        Log.d(DEBUG_TAG, "💾 开始验证内存使用情况")
        
        try {
            val runtime = Runtime.getRuntime()
            
            // 执行GC获得更准确的内存数据
            System.gc()
            delay(100)
            
            val totalMemory = runtime.totalMemory() / 1024 / 1024 // MB
            val freeMemory = runtime.freeMemory() / 1024 / 1024   // MB
            val usedMemory = totalMemory - freeMemory
            val maxMemory = runtime.maxMemory() / 1024 / 1024     // MB
            
            Log.d(DEBUG_TAG, "📊 内存使用统计:")
            Log.d(DEBUG_TAG, "    已分配内存: ${totalMemory}MB")
            Log.d(DEBUG_TAG, "    已使用内存: ${usedMemory}MB")
            Log.d(DEBUG_TAG, "    可用内存: ${freeMemory}MB")
            Log.d(DEBUG_TAG, "    最大内存: ${maxMemory}MB")
            
            val memoryUsagePercent = (usedMemory * 100) / maxMemory
            Log.d(DEBUG_TAG, "    内存使用率: ${memoryUsagePercent}%")
            
            when {
                usedMemory < 50 -> Log.d(DEBUG_TAG, "✅ 内存使用优秀")
                usedMemory < MEMORY_WARNING_THRESHOLD_MB -> Log.d(DEBUG_TAG, "✅ 内存使用良好")
                usedMemory < 200 -> Log.w(DEBUG_TAG, "⚠️ 内存使用较高")
                else -> Log.e(DEBUG_TAG, "❌ 内存使用过高")
            }
            
            when {
                memoryUsagePercent < 30 -> Log.d(DEBUG_TAG, "✅ 内存使用率健康")
                memoryUsagePercent < 60 -> Log.w(DEBUG_TAG, "⚠️ 内存使用率较高")
                else -> Log.e(DEBUG_TAG, "❌ 内存使用率过高，可能存在内存泄漏")
            }
            
            // 内存压力测试
            Log.d(DEBUG_TAG, "🧪 执行内存压力测试")
            
            val beforeMemory = usedMemory
            
            // 模拟一些内存操作
            val testData = mutableListOf<String>()
            repeat(1000) {
                testData.add("Memory test data item $it with some additional content")
            }
            
            delay(100)
            System.gc()
            delay(100)
            
            val afterMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
            val memoryDelta = afterMemory - beforeMemory
            
            Log.d(DEBUG_TAG, "内存压力测试结果:")
            Log.d(DEBUG_TAG, "    测试前内存: ${beforeMemory}MB")
            Log.d(DEBUG_TAG, "    测试后内存: ${afterMemory}MB")
            Log.d(DEBUG_TAG, "    内存增量: ${memoryDelta}MB")
            
            if (memoryDelta < 10) {
                Log.d(DEBUG_TAG, "✅ 内存管理良好")
            } else if (memoryDelta < 20) {
                Log.w(DEBUG_TAG, "⚠️ 内存增长较多")
            } else {
                Log.e(DEBUG_TAG, "❌ 内存可能存在泄漏")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证内存使用情况异常", e)
        }
    }
    
    /**
     * 验证并发性能
     */
    private suspend fun validateConcurrentPerformance(userId: String) {
        Log.d(DEBUG_TAG, "🔀 开始验证并发性能")
        
        try {
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.size < 2) {
                Log.w(DEBUG_TAG, "⚠️ 记账簿数量不足，跳过并发性能测试")
                return
            }
            
            Log.d(DEBUG_TAG, "🚀 执行并发切换测试")
            
            val concurrentOperations = 10
            val concurrentTime = measureTimeMillis {
                val jobs = (1..concurrentOperations).map { index ->
                    validatorScope.launch {
                        val targetLedger = ledgers[index % ledgers.size]
                        ledgerUIPreferencesRepository.updateSelectedLedgerId(targetLedger.id)
                        delay(10) // 模拟一些处理时间
                    }
                }
                
                // 等待所有操作完成
                jobs.forEach { it.join() }
            }
            
            val avgConcurrentTime = concurrentTime.toDouble() / concurrentOperations
            
            Log.d(DEBUG_TAG, "📊 并发性能统计:")
            Log.d(DEBUG_TAG, "    并发操作数: $concurrentOperations")
            Log.d(DEBUG_TAG, "    总执行时间: ${concurrentTime}ms")
            Log.d(DEBUG_TAG, "    平均单操作时间: ${avgConcurrentTime.toInt()}ms")
            
            // 与串行执行比较
            Log.d(DEBUG_TAG, "🔄 执行串行对比测试")
            
            val serialTime = measureTimeMillis {
                repeat(concurrentOperations) { index ->
                    val targetLedger = ledgers[index % ledgers.size]
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(targetLedger.id)
                    delay(10)
                }
            }
            
            val performanceGain = ((serialTime - concurrentTime).toDouble() / serialTime) * 100
            
            Log.d(DEBUG_TAG, "📈 并发性能分析:")
            Log.d(DEBUG_TAG, "    串行时间: ${serialTime}ms")
            Log.d(DEBUG_TAG, "    并发时间: ${concurrentTime}ms")
            Log.d(DEBUG_TAG, "    性能提升: ${performanceGain.toInt()}%")
            
            when {
                performanceGain > 50 -> Log.d(DEBUG_TAG, "✅ 并发性能优秀")
                performanceGain > 20 -> Log.d(DEBUG_TAG, "✅ 并发性能良好")
                performanceGain > 0 -> Log.w(DEBUG_TAG, "⚠️ 并发性能一般")
                else -> Log.e(DEBUG_TAG, "❌ 并发性能不佳，可能存在阻塞")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证并发性能异常", e)
        }
    }
    
    /**
     * 验证大数据量处理性能
     */
    private suspend fun validateLargeDatasetPerformance(userId: String) {
        Log.d(DEBUG_TAG, "📊 开始验证大数据量处理性能")
        
        try {
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.isEmpty()) {
                Log.w(DEBUG_TAG, "⚠️ 无记账簿可用于大数据量测试")
                return
            }
            
            Log.d(DEBUG_TAG, "📈 模拟大数据量场景")
            
            // 模拟大量记账簿数据访问
            val largeDatasetTime = measureTimeMillis {
                repeat(100) { iteration ->
                    // 模拟频繁的记账簿查询
                    manageLedgerUseCase.getUserLedgers(userId).first()
                    
                    // 模拟记账簿切换
                    val targetLedger = ledgers[iteration % ledgers.size]
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(targetLedger.id)
                    
                    // 验证切换结果
                    ledgerUIPreferencesRepository.getUIPreferences().first()
                    
                    // 每10次操作报告进度
                    if ((iteration + 1) % 10 == 0) {
                        Log.d(DEBUG_TAG, "进度: ${iteration + 1}/100")
                    }
                }
            }
            
            val avgOperationTime = largeDatasetTime.toDouble() / 100
            
            Log.d(DEBUG_TAG, "📊 大数据量性能统计:")
            Log.d(DEBUG_TAG, "    总操作数: 100")
            Log.d(DEBUG_TAG, "    总执行时间: ${largeDatasetTime}ms")
            Log.d(DEBUG_TAG, "    平均操作时间: ${avgOperationTime.toInt()}ms")
            Log.d(DEBUG_TAG, "    每秒操作数: ${(1000 / avgOperationTime).toInt()}")
            
            when {
                avgOperationTime < 50 -> Log.d(DEBUG_TAG, "✅ 大数据量性能优秀")
                avgOperationTime < 150 -> Log.d(DEBUG_TAG, "✅ 大数据量性能良好")
                avgOperationTime < 500 -> Log.w(DEBUG_TAG, "⚠️ 大数据量性能一般")
                else -> Log.e(DEBUG_TAG, "❌ 大数据量性能较差")
            }
            
            // 验证性能线性度
            Log.d(DEBUG_TAG, "📈 验证性能线性度")
            
            val smallDatasetTime = measureTimeMillis {
                repeat(10) { iteration ->
                    manageLedgerUseCase.getUserLedgers(userId).first()
                    val targetLedger = ledgers[iteration % ledgers.size]
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(targetLedger.id)
                    ledgerUIPreferencesRepository.getUIPreferences().first()
                }
            }
            
            val scalabilityRatio = (largeDatasetTime.toDouble() / 100) / (smallDatasetTime.toDouble() / 10)
            
            Log.d(DEBUG_TAG, "📏 性能扩展性分析:")
            Log.d(DEBUG_TAG, "    小数据集平均时间: ${(smallDatasetTime / 10)}ms")
            Log.d(DEBUG_TAG, "    大数据集平均时间: ${avgOperationTime.toInt()}ms")
            Log.d(DEBUG_TAG, "    扩展性比率: ${String.format("%.2f", scalabilityRatio)}")
            
            when {
                scalabilityRatio < 1.2 -> Log.d(DEBUG_TAG, "✅ 性能扩展性优秀")
                scalabilityRatio < 2.0 -> Log.d(DEBUG_TAG, "✅ 性能扩展性良好")
                scalabilityRatio < 3.0 -> Log.w(DEBUG_TAG, "⚠️ 性能扩展性一般")
                else -> Log.e(DEBUG_TAG, "❌ 性能扩展性较差，存在性能瓶颈")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证大数据量处理性能异常", e)
        }
    }
    
    /**
     * 验证ViewModel的性能表现
     */
    suspend fun validateViewModelPerformance(viewModel: LedgerViewModel): PerformanceValidationSummary {
        Log.d(DEBUG_TAG, "🎯 开始验证ViewModel性能表现")
        
        val issues = mutableListOf<String>()
        var initializationTimeMs = -1L
        var switchingTimeMs = -1L
        var memoryUsageMB = -1L
        
        try {
            // 1. 验证初始化性能
            Log.d(DEBUG_TAG, "🚀 验证ViewModel初始化性能")
            
            val initTime = measureTimeMillis {
                var attempts = 0
                while (attempts < 100) { // 最大等待10秒
                    delay(100)
                    try {
                        val state = viewModel.uiState.first()
                        if (!state.isLedgerLoading && state.ledgers.isNotEmpty()) {
                            break
                        }
                    } catch (e: Exception) {
                        // 继续等待初始化
                    }
                    attempts++
                }
            }
            
            initializationTimeMs = initTime
            
            if (initTime > 5000) {
                issues.add("ViewModel初始化时间过长 (${initTime}ms)")
            } else if (initTime > 2000) {
                issues.add("ViewModel初始化时间较长 (${initTime}ms)")
            }
            
            Log.d(DEBUG_TAG, "ViewModel初始化耗时: ${initTime}ms")
            
            // 2. 验证记账簿切换性能
            Log.d(DEBUG_TAG, "🔄 验证ViewModel记账簿切换性能")
            
            val state = viewModel.uiState.first()
            if (state.ledgers.size >= 2) {
                val targetLedger = state.ledgers.find { it.id != state.selectedLedgerId }
                if (targetLedger != null) {
                    val switchTime = measureTimeMillis {
                        viewModel.selectLedger(targetLedger.id)
                        
                        // 等待切换完成
                        var attempts = 0
                        while (attempts < 50) {
                            delay(100)
                            val currentState = viewModel.uiState.first()
                            if (currentState.selectedLedgerId == targetLedger.id && 
                                !currentState.isLoading && 
                                !currentState.isLedgerLoading) {
                                break
                            }
                            attempts++
                        }
                    }
                    
                    switchingTimeMs = switchTime
                    
                    if (switchTime > 3000) {
                        issues.add("记账簿切换时间过长 (${switchTime}ms)")
                    } else if (switchTime > 1000) {
                        issues.add("记账簿切换时间较长 (${switchTime}ms)")
                    }
                    
                    Log.d(DEBUG_TAG, "记账簿切换耗时: ${switchTime}ms")
                }
            }
            
            // 3. 验证内存使用
            Log.d(DEBUG_TAG, "💾 验证ViewModel内存使用")
            
            System.gc()
            delay(100)
            
            val runtime = Runtime.getRuntime()
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
            memoryUsageMB = usedMemory
            
            if (usedMemory > 150) {
                issues.add("内存使用过高 (${usedMemory}MB)")
            } else if (usedMemory > 100) {
                issues.add("内存使用较高 (${usedMemory}MB)")
            }
            
            Log.d(DEBUG_TAG, "当前内存使用: ${usedMemory}MB")
            
            return PerformanceValidationSummary(
                success = issues.isEmpty(),
                initializationTimeMs = initializationTimeMs,
                switchingTimeMs = switchingTimeMs,
                memoryUsageMB = memoryUsageMB,
                performanceGrade = calculatePerformanceGrade(initializationTimeMs, switchingTimeMs, memoryUsageMB),
                issues = issues
            )
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证ViewModel性能表现异常", e)
            issues.add("验证过程异常: ${e.message}")
            
            return PerformanceValidationSummary(
                success = false,
                initializationTimeMs = initializationTimeMs,
                switchingTimeMs = switchingTimeMs,
                memoryUsageMB = memoryUsageMB,
                performanceGrade = "F",
                issues = issues
            )
        }
    }
    
    /**
     * 计算性能等级
     */
    private fun calculatePerformanceGrade(initTime: Long, switchTime: Long, memoryMB: Long): String {
        var score = 100
        
        // 初始化时间评分
        when {
            initTime > 5000 -> score -= 30
            initTime > 2000 -> score -= 15
            initTime > 1000 -> score -= 5
        }
        
        // 切换时间评分
        when {
            switchTime > 3000 -> score -= 25
            switchTime > 1000 -> score -= 10
            switchTime > 500 -> score -= 5
        }
        
        // 内存使用评分
        when {
            memoryMB > 150 -> score -= 20
            memoryMB > 100 -> score -= 10
            memoryMB > 80 -> score -= 5
        }
        
        return when {
            score >= 90 -> "A"
            score >= 80 -> "B"
            score >= 70 -> "C"
            score >= 60 -> "D"
            else -> "F"
        }
    }
}

/**
 * 性能验证总结
 */
data class PerformanceValidationSummary(
    val success: Boolean,
    val initializationTimeMs: Long,
    val switchingTimeMs: Long,
    val memoryUsageMB: Long,
    val performanceGrade: String,
    val issues: List<String>
)