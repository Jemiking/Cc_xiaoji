package com.ccxiaoji.feature.ledger.debug

import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

/**
 * 记账簿验证协调器
 * 统一执行所有记账簿功能验证，提供完整的测试报告
 */
@Singleton
class LedgerValidationCoordinator @Inject constructor(
    private val runtimeDataFlowValidator: RuntimeDataFlowValidator,
    private val defaultLedgerValidator: DefaultLedgerMechanismValidator,
    private val statePersistenceValidator: StatePersistenceValidator,
    private val edgeCaseValidator: EdgeCaseValidator,
    private val performanceValidator: PerformanceValidator,
    private val userExperienceValidator: UserExperienceValidator,
    private val integrationValidator: IntegrationValidator
) {
    
    companion object {
        private const val TAG = "LedgerValidationCoordinator"
        private const val DEBUG_TAG = "LEDGER_VALIDATION_COORDINATOR"
    }
    
    private val coordinatorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * 执行完整的记账簿验证套件
     * 这是最全面的验证，包含所有验证器的全部功能
     */
    fun executeComprehensiveValidation(viewModel: LedgerViewModel) {
        coordinatorScope.launch {
            try {
                Log.i(TAG, "🚀🚀🚀 开始执行完整记账簿验证套件 🚀🚀🚀")
                val startTime = System.currentTimeMillis()
                
                val validationResults = mutableMapOf<String, ValidationPhaseResult>()
                
                // Phase 1: 运行时数据流验证 (完整验证包含所有子验证器)
                Log.i(TAG, "📋 Phase 1: 运行时数据流验证")
                val runtimeResult = executePhaseWithTiming("运行时数据流验证") {
                    runtimeDataFlowValidator.executeFullValidation(viewModel)
                }
                validationResults["runtime"] = runtimeResult
                
                // Phase 2: 默认记账簿机制验证
                Log.i(TAG, "📚 Phase 2: 默认记账簿机制验证")
                val defaultLedgerResult = executePhaseWithTiming("默认记账簿机制验证") {
                    defaultLedgerValidator.executeFullDefaultLedgerValidation()
                }
                validationResults["defaultLedger"] = defaultLedgerResult
                
                // Phase 3: 状态持久化验证
                Log.i(TAG, "💾 Phase 3: 状态持久化验证")
                val persistenceResult = executePhaseWithTiming("状态持久化验证") {
                    statePersistenceValidator.executeFullPersistenceValidation()
                }
                validationResults["persistence"] = persistenceResult
                
                // Phase 4: 边界情况验证
                Log.i(TAG, "⚠️ Phase 4: 边界情况验证")
                val edgeCaseResult = executePhaseWithTiming("边界情况验证") {
                    edgeCaseValidator.executeFullEdgeCaseValidation()
                }
                validationResults["edgeCase"] = edgeCaseResult
                
                // Phase 5: 性能验证
                Log.i(TAG, "⚡ Phase 5: 性能验证")
                val performanceResult = executePhaseWithTiming("性能验证") {
                    performanceValidator.executeFullPerformanceValidation()
                }
                validationResults["performance"] = performanceResult
                
                // Phase 6: 用户体验验证
                Log.i(TAG, "🎨 Phase 6: 用户体验验证")
                val userExperienceResult = executePhaseWithTiming("用户体验验证") {
                    userExperienceValidator.executeFullUserExperienceValidation()
                }
                validationResults["userExperience"] = userExperienceResult
                
                // Phase 7: 集成验证
                Log.i(TAG, "🔗 Phase 7: 集成验证")
                val integrationResult = executePhaseWithTiming("集成验证") {
                    integrationValidator.executeFullIntegrationValidation()
                }
                validationResults["integration"] = integrationResult
                
                val totalTime = System.currentTimeMillis() - startTime
                
                // 生成完整验证报告
                generateComprehensiveReport(validationResults, totalTime)
                
                Log.i(TAG, "✅✅✅ 完整记账簿验证套件执行完成 ✅✅✅")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌❌❌ 完整记账簿验证套件执行失败 ❌❌❌", e)
            }
        }
    }
    
    /**
     * 执行快速验证套件
     * 适合开发阶段的快速检查
     */
    fun executeQuickValidation(viewModel: LedgerViewModel) {
        coordinatorScope.launch {
            try {
                Log.i(TAG, "🏃‍♂️ 开始执行快速验证套件")
                val startTime = System.currentTimeMillis()
                
                val quickResults = mutableMapOf<String, ValidationPhaseResult>()
                
                // 快速数据流检查
                val quickDataFlowResult = executePhaseWithTiming("快速数据流检查") {
                    runtimeDataFlowValidator.quickDataFlowCheck(viewModel)
                }
                quickResults["quickDataFlow"] = quickDataFlowResult
                
                // ViewModel特定验证
                val viewModelValidationResult = executePhaseWithTiming("ViewModel验证") {
                    validateViewModelSpecifics(viewModel)
                }
                quickResults["viewModel"] = viewModelValidationResult
                
                val totalTime = System.currentTimeMillis() - startTime
                
                // 生成快速验证报告
                generateQuickReport(quickResults, totalTime)
                
                Log.i(TAG, "✅ 快速验证套件执行完成")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 快速验证套件执行失败", e)
            }
        }
    }
    
    /**
     * 执行性能基准测试
     * 专门用于性能回归测试
     */
    fun executePerformanceBenchmark(viewModel: LedgerViewModel) {
        coordinatorScope.launch {
            try {
                Log.i(TAG, "🏁 开始执行性能基准测试")
                val startTime = System.currentTimeMillis()
                
                val benchmarkResults = mutableMapOf<String, Any>()
                
                // 1. ViewModel性能基准测试
                Log.d(DEBUG_TAG, "🎯 ViewModel性能基准测试")
                val viewModelPerformance = performanceValidator.validateViewModelPerformance(viewModel)
                benchmarkResults["viewModelPerformance"] = viewModelPerformance
                
                // 2. 用户体验基准测试
                Log.d(DEBUG_TAG, "🎨 用户体验基准测试")
                val userExperience = userExperienceValidator.validateViewModelUserExperience(viewModel)
                benchmarkResults["userExperience"] = userExperience
                
                // 3. 集成性能基准测试
                Log.d(DEBUG_TAG, "🔗 集成性能基准测试")
                val integrationPerformance = integrationValidator.validateViewModelIntegration(viewModel)
                benchmarkResults["integrationPerformance"] = integrationPerformance
                
                val totalTime = System.currentTimeMillis() - startTime
                
                // 生成性能基准报告
                generatePerformanceBenchmarkReport(benchmarkResults, totalTime)
                
                Log.i(TAG, "✅ 性能基准测试执行完成")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 性能基准测试执行失败", e)
            }
        }
    }
    
    /**
     * 执行带时间测量的验证阶段
     */
    private suspend fun executePhaseWithTiming(phaseName: String, action: suspend () -> Unit): ValidationPhaseResult {
        val startTime = System.currentTimeMillis()
        var success = true
        var errorMessage: String? = null
        
        try {
            action()
            Log.d(DEBUG_TAG, "✅ $phaseName 执行成功")
        } catch (e: Exception) {
            success = false
            errorMessage = e.message
            Log.e(DEBUG_TAG, "❌ $phaseName 执行失败", e)
        }
        
        val duration = System.currentTimeMillis() - startTime
        
        return ValidationPhaseResult(
            phaseName = phaseName,
            success = success,
            durationMs = duration,
            errorMessage = errorMessage
        )
    }
    
    /**
     * 验证ViewModel特定功能
     */
    private suspend fun validateViewModelSpecifics(viewModel: LedgerViewModel) {
        Log.d(DEBUG_TAG, "🎯 验证ViewModel特定功能")
        
        // 快速验证ViewModel的关键功能
        val defaultLedgerSummary = defaultLedgerValidator.validateViewModelDefaultLedger(viewModel)
        val persistenceSummary = statePersistenceValidator.validateViewModelPersistence(viewModel)
        val edgeCaseSummary = edgeCaseValidator.validateViewModelEdgeCases(viewModel)
        val performanceSummary = performanceValidator.validateViewModelPerformance(viewModel)
        val userExperienceSummary = userExperienceValidator.validateViewModelUserExperience(viewModel)
        val integrationSummary = integrationValidator.validateViewModelIntegration(viewModel)
        
        // 汇总ViewModel验证结果
        val allSuccessful = listOf(
            defaultLedgerSummary.success,
            persistenceSummary.success,
            edgeCaseSummary.success,
            performanceSummary.success,
            userExperienceSummary.success,
            integrationSummary.success
        ).all { it }
        
        if (allSuccessful) {
            Log.d(DEBUG_TAG, "✅ ViewModel所有特定功能验证通过")
        } else {
            Log.w(DEBUG_TAG, "⚠️ ViewModel部分功能验证未通过")
        }
    }
    
    /**
     * 生成完整验证报告
     */
    private fun generateComprehensiveReport(results: Map<String, ValidationPhaseResult>, totalTime: Long) {
        Log.i(TAG, "📊📊📊 完整验证报告 📊📊📊")
        Log.i(TAG, "验证开始时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}")
        Log.i(TAG, "总执行时间: ${totalTime}ms (${totalTime / 1000.0}秒)")
        Log.i(TAG, "")
        
        val successfulPhases = results.values.count { it.success }
        val totalPhases = results.size
        val overallSuccessRate = (successfulPhases.toDouble() / totalPhases) * 100
        
        Log.i(TAG, "总体验证结果:")
        Log.i(TAG, "  ✅ 成功阶段: $successfulPhases/$totalPhases")
        Log.i(TAG, "  📈 成功率: ${overallSuccessRate.toInt()}%")
        Log.i(TAG, "  ⏱️ 平均阶段耗时: ${results.values.map { it.durationMs }.average().toInt()}ms")
        Log.i(TAG, "")
        
        Log.i(TAG, "各阶段详细结果:")
        results.entries.forEachIndexed { index, (phase, result) ->
            val status = if (result.success) "✅ PASS" else "❌ FAIL"
            val duration = "${result.durationMs}ms"
            val error = result.errorMessage?.let { " (错误: $it)" } ?: ""
            
            Log.i(TAG, "  ${index + 1}. ${result.phaseName}: $status - $duration$error")
        }
        
        Log.i(TAG, "")
        
        // 性能评级
        val avgDuration = results.values.map { it.durationMs }.average()
        val performanceGrade = when {
            avgDuration < 1000 -> "A (优秀)"
            avgDuration < 3000 -> "B (良好)"
            avgDuration < 5000 -> "C (一般)"
            avgDuration < 10000 -> "D (较差)"
            else -> "F (很差)"
        }
        
        Log.i(TAG, "整体性能评级: $performanceGrade")
        Log.i(TAG, "")
        
        // 建议
        when {
            overallSuccessRate >= 100 -> {
                Log.i(TAG, "🎉 建议: 记账簿功能已完全通过验证，可以发布")
            }
            overallSuccessRate >= 80 -> {
                Log.i(TAG, "✅ 建议: 记账簿功能基本稳定，可考虑发布候选版本")
            }
            overallSuccessRate >= 60 -> {
                Log.w(TAG, "⚠️ 建议: 记账簿功能存在一些问题，建议修复后再次验证")
            }
            else -> {
                Log.e(TAG, "❌ 建议: 记账簿功能存在严重问题，需要全面检修")
            }
        }
        
        Log.i(TAG, "📊📊📊 验证报告结束 📊📊📊")
    }
    
    /**
     * 生成快速验证报告
     */
    private fun generateQuickReport(results: Map<String, ValidationPhaseResult>, totalTime: Long) {
        Log.i(TAG, "📋 快速验证报告")
        Log.i(TAG, "执行时间: ${totalTime}ms")
        
        val allSuccessful = results.values.all { it.success }
        
        if (allSuccessful) {
            Log.i(TAG, "✅ 快速验证: 全部通过")
        } else {
            Log.w(TAG, "⚠️ 快速验证: 发现问题")
            results.values.filter { !it.success }.forEach { result ->
                Log.w(TAG, "  ❌ ${result.phaseName}: ${result.errorMessage}")
            }
        }
    }
    
    /**
     * 生成性能基准报告
     */
    private fun generatePerformanceBenchmarkReport(results: Map<String, Any>, totalTime: Long) {
        Log.i(TAG, "🏁🏁🏁 性能基准报告 🏁🏁🏁")
        Log.i(TAG, "基准测试时间: ${totalTime}ms")
        Log.i(TAG, "")
        
        // ViewModel性能报告
        val viewModelPerf = results["viewModelPerformance"] as? PerformanceValidationSummary
        if (viewModelPerf != null) {
            Log.i(TAG, "ViewModel性能基准:")
            Log.i(TAG, "  🚀 初始化时间: ${viewModelPerf.initializationTimeMs}ms")
            Log.i(TAG, "  🔄 切换时间: ${viewModelPerf.switchingTimeMs}ms")
            Log.i(TAG, "  💾 内存使用: ${viewModelPerf.memoryUsageMB}MB")
            Log.i(TAG, "  📊 性能等级: ${viewModelPerf.performanceGrade}")
            Log.i(TAG, "")
        }
        
        // 用户体验报告
        val userExp = results["userExperience"] as? UserExperienceValidationSummary
        if (userExp != null) {
            Log.i(TAG, "用户体验基准:")
            Log.i(TAG, "  📱 加载状态管理: ${userExp.loadingStateManagement}")
            Log.i(TAG, "  🛡️ 错误处理: ${userExp.errorHandling}")
            Log.i(TAG, "  ⚡ 交互响应性: ${userExp.interactionResponsiveness}")
            Log.i(TAG, "  🌟 整体评级: ${userExp.overallRating}")
            Log.i(TAG, "")
        }
        
        // 集成性能报告
        val integration = results["integrationPerformance"] as? IntegrationValidationSummary
        if (integration != null) {
            Log.i(TAG, "集成性能基准:")
            Log.i(TAG, "  🔗 模块集成: ${integration.moduleIntegrationScore}分")
            Log.i(TAG, "  📊 数据一致性: ${integration.dataConsistencyScore}分")
            Log.i(TAG, "  🛡️ 错误处理: ${integration.errorHandlingScore}分")
            Log.i(TAG, "  🌟 整体评级: ${integration.overallIntegrationRating}")
        }
        
        Log.i(TAG, "🏁🏁🏁 基准报告结束 🏁🏁🏁")
    }
}

/**
 * 验证阶段结果
 */
data class ValidationPhaseResult(
    val phaseName: String,
    val success: Boolean,
    val durationMs: Long,
    val errorMessage: String?
)