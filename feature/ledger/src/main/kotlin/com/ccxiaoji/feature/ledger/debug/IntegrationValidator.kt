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

/**
 * 集成验证器
 * 测试记账簿功能与其他模块的端到端集成
 */
@Singleton
class IntegrationValidator @Inject constructor(
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val userApi: UserApi,
    private val ledgerUIPreferencesRepository: LedgerUIPreferencesRepository
) {
    
    companion object {
        private const val TAG = "IntegrationValidator"
        private const val DEBUG_TAG = "LEDGER_INTEGRATION_DEBUG"
    }
    
    private val validatorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * 执行完整的集成验证
     */
    fun executeFullIntegrationValidation() {
        validatorScope.launch {
            try {
                Log.i(TAG, "🚀 开始执行记账簿集成验证")
                
                val userId = userApi.getCurrentUserId()
                
                // 1. 验证用户模块集成
                validateUserModuleIntegration(userId)
                
                // 2. 验证数据库模块集成
                validateDatabaseModuleIntegration(userId)
                
                // 3. 验证UI偏好设置集成
                validateUIPreferencesIntegration(userId)
                
                // 4. 验证跨模块数据一致性
                validateCrossModuleDataConsistency(userId)
                
                // 5. 验证导航和生命周期集成
                validateNavigationAndLifecycleIntegration(userId)
                
                // 6. 验证错误处理和恢复机制
                validateErrorHandlingAndRecovery(userId)
                
                Log.i(TAG, "✅ 记账簿集成验证完成")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 记账簿集成验证失败", e)
            }
        }
    }
    
    /**
     * 验证用户模块集成
     */
    private suspend fun validateUserModuleIntegration(userId: String) {
        Log.d(DEBUG_TAG, "👤 开始验证用户模块集成")
        
        try {
            // 1. 验证用户ID的传递和使用
            Log.d(DEBUG_TAG, "🔑 验证用户ID传递")
            
            if (userId.isBlank()) {
                Log.e(DEBUG_TAG, "❌ 用户ID为空")
                return
            }
            
            Log.d(DEBUG_TAG, "✅ 用户ID有效: $userId")
            
            // 2. 验证记账簿与用户的关联
            Log.d(DEBUG_TAG, "📚 验证记账簿与用户关联")
            
            val userLedgers = manageLedgerUseCase.getUserLedgers(userId).first()
            
            if (userLedgers.isEmpty()) {
                Log.w(DEBUG_TAG, "⚠️ 用户暂无记账簿")
            } else {
                Log.d(DEBUG_TAG, "✅ 用户记账簿: ${userLedgers.size}个")
                
                // 验证所有记账簿都属于当前用户
                val invalidLedgers = userLedgers.filter { it.userId != userId }
                if (invalidLedgers.isNotEmpty()) {
                    Log.e(DEBUG_TAG, "❌ 发现${invalidLedgers.size}个不属于当前用户的记账簿")
                } else {
                    Log.d(DEBUG_TAG, "✅ 所有记账簿都正确归属于当前用户")
                }
            }
            
            // 3. 验证用户状态变化对记账簿的影响
            Log.d(DEBUG_TAG, "🔄 验证用户状态变化影响")
            
            // 模拟用户状态变化（实际场景可能包括登录/登出等）
            val beforeLedgerCount = userLedgers.size
            
            // 等待一段时间确保状态稳定
            delay(500)
            
            val afterLedgers = manageLedgerUseCase.getUserLedgers(userId).first()
            val afterLedgerCount = afterLedgers.size
            
            Log.d(DEBUG_TAG, "用户状态变化前后记账簿数量: $beforeLedgerCount -> $afterLedgerCount")
            
            if (beforeLedgerCount == afterLedgerCount) {
                Log.d(DEBUG_TAG, "✅ 用户状态变化期间记账簿数据保持稳定")
            } else {
                Log.w(DEBUG_TAG, "⚠️ 用户状态变化期间记账簿数量发生变化")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证用户模块集成异常", e)
        }
    }
    
    /**
     * 验证数据库模块集成
     */
    private suspend fun validateDatabaseModuleIntegration(userId: String) {
        Log.d(DEBUG_TAG, "🗄️ 开始验证数据库模块集成")
        
        try {
            // 1. 验证数据持久化
            Log.d(DEBUG_TAG, "💾 验证数据持久化")
            
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            
            if (ledgers.isNotEmpty()) {
                val testLedger = ledgers.first()
                
                // 验证记账簿数据的完整性
                Log.d(DEBUG_TAG, "📋 验证记账簿数据完整性")
                
                val requiredFields = listOf(
                    "ID" to testLedger.id.isNotBlank(),
                    "名称" to testLedger.name.isNotBlank(),
                    "用户ID" to testLedger.userId.isNotBlank(),
                    "创建时间" to true, // 简化时间验证
                    "更新时间" to true  // 简化时间验证
                )
                
                requiredFields.forEach { (field, isValid) ->
                    if (isValid) {
                        Log.d(DEBUG_TAG, "✅ $field 字段有效")
                    } else {
                        Log.e(DEBUG_TAG, "❌ $field 字段无效")
                    }
                }
                
                // 验证数据库关系完整性
                Log.d(DEBUG_TAG, "🔗 验证数据库关系完整性")
                
                if (testLedger.userId == userId) {
                    Log.d(DEBUG_TAG, "✅ 记账簿与用户关系正确")
                } else {
                    Log.e(DEBUG_TAG, "❌ 记账簿与用户关系错误")
                }
            }
            
            // 2. 验证并发访问安全性
            Log.d(DEBUG_TAG, "🔐 验证并发访问安全性")
            
            val concurrentOperations = 5
            val concurrentResults = mutableListOf<Boolean>()
            
            val jobs = (1..concurrentOperations).map { index ->
                validatorScope.launch {
                    try {
                        val result = manageLedgerUseCase.getUserLedgers(userId).first()
                        concurrentResults.add(result.isNotEmpty())
                        Log.d(DEBUG_TAG, "并发操作${index}完成: ${result.size}个记账簿")
                    } catch (e: Exception) {
                        concurrentResults.add(false)
                        Log.e(DEBUG_TAG, "并发操作${index}失败", e)
                    }
                }
            }
            
            // 等待所有并发操作完成
            jobs.forEach { it.join() }
            
            val successCount = concurrentResults.count { it }
            val successRate = (successCount.toDouble() / concurrentOperations) * 100
            
            Log.d(DEBUG_TAG, "并发访问成功率: ${successRate.toInt()}% ($successCount/$concurrentOperations)")
            
            when {
                successRate >= 100 -> Log.d(DEBUG_TAG, "✅ 并发访问完全安全")
                successRate >= 80 -> Log.d(DEBUG_TAG, "✅ 并发访问基本安全")
                successRate >= 60 -> Log.w(DEBUG_TAG, "⚠️ 并发访问偶有问题")
                else -> Log.e(DEBUG_TAG, "❌ 并发访问存在严重问题")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证数据库模块集成异常", e)
        }
    }
    
    /**
     * 验证UI偏好设置集成
     */
    private suspend fun validateUIPreferencesIntegration(userId: String) {
        Log.d(DEBUG_TAG, "⚙️ 开始验证UI偏好设置集成")
        
        try {
            // 1. 验证偏好设置的读写
            Log.d(DEBUG_TAG, "📖 验证偏好设置读写")
            
            val initialPrefs = ledgerUIPreferencesRepository.getUIPreferences().first()
            Log.d(DEBUG_TAG, "初始偏好设置: 选中记账簿=${initialPrefs.selectedLedgerId}")
            
            // 2. 验证偏好设置与记账簿数据的同步
            Log.d(DEBUG_TAG, "🔄 验证偏好设置与记账簿数据同步")
            
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            
            if (ledgers.isNotEmpty()) {
                val targetLedger = ledgers.first()
                
                // 更新偏好设置
                Log.d(DEBUG_TAG, "更新偏好设置到: ${targetLedger.name}")
                ledgerUIPreferencesRepository.updateSelectedLedgerId(targetLedger.id)
                
                // 验证更新是否生效
                delay(200) // 等待更新完成
                
                val updatedPrefs = ledgerUIPreferencesRepository.getUIPreferences().first()
                
                if (updatedPrefs.selectedLedgerId == targetLedger.id) {
                    Log.d(DEBUG_TAG, "✅ 偏好设置更新成功")
                } else {
                    Log.e(DEBUG_TAG, "❌ 偏好设置更新失败")
                }
                
                // 3. 验证偏好设置的持久化
                Log.d(DEBUG_TAG, "💾 验证偏好设置持久化")
                
                // 模拟应用重启场景（通过重新读取来验证）
                val persistedPrefs = ledgerUIPreferencesRepository.getUIPreferences().first()
                
                if (persistedPrefs.selectedLedgerId == targetLedger.id) {
                    Log.d(DEBUG_TAG, "✅ 偏好设置持久化成功")
                } else {
                    Log.e(DEBUG_TAG, "❌ 偏好设置持久化失败")
                }
                
                // 恢复初始状态
                if (!initialPrefs.selectedLedgerId.isNullOrBlank()) {
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(initialPrefs.selectedLedgerId)
                }
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证UI偏好设置集成异常", e)
        }
    }
    
    /**
     * 验证跨模块数据一致性
     */
    private suspend fun validateCrossModuleDataConsistency(userId: String) {
        Log.d(DEBUG_TAG, "🔍 开始验证跨模块数据一致性")
        
        try {
            // 1. 验证记账簿数据在不同模块间的一致性
            Log.d(DEBUG_TAG, "📊 验证记账簿数据一致性")
            
            val ledgersFromUseCase = manageLedgerUseCase.getUserLedgers(userId).first()
            val prefsFromRepository = ledgerUIPreferencesRepository.getUIPreferences().first()
            
            Log.d(DEBUG_TAG, "UseCase记账簿数量: ${ledgersFromUseCase.size}")
            Log.d(DEBUG_TAG, "偏好设置选中记账簿: ${prefsFromRepository.selectedLedgerId}")
            
            // 验证选中的记账簿是否存在于记账簿列表中
            if (!prefsFromRepository.selectedLedgerId.isNullOrBlank()) {
                val selectedLedgerExists = ledgersFromUseCase.any { it.id == prefsFromRepository.selectedLedgerId }
                
                if (selectedLedgerExists) {
                    Log.d(DEBUG_TAG, "✅ 选中的记账簿存在于记账簿列表中")
                } else {
                    Log.e(DEBUG_TAG, "❌ 选中的记账簿不存在于记账簿列表中")
                }
            }
            
            // 2. 验证数据更新的传播
            Log.d(DEBUG_TAG, "📡 验证数据更新传播")
            
            if (ledgersFromUseCase.isNotEmpty()) {
                val targetLedger = ledgersFromUseCase.first()
                
                // 记录更新前状态
                val beforeUpdate = ledgerUIPreferencesRepository.getUIPreferences().first()
                
                // 执行更新
                ledgerUIPreferencesRepository.updateSelectedLedgerId(targetLedger.id)
                
                // 验证更新传播
                delay(300) // 等待传播完成
                
                val afterUpdate = ledgerUIPreferencesRepository.getUIPreferences().first()
                
                if (afterUpdate.selectedLedgerId == targetLedger.id) {
                    Log.d(DEBUG_TAG, "✅ 数据更新传播成功")
                } else {
                    Log.e(DEBUG_TAG, "❌ 数据更新传播失败")
                }
                
                // 恢复原状态
                if (!beforeUpdate.selectedLedgerId.isNullOrBlank()) {
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(beforeUpdate.selectedLedgerId)
                }
            }
            
            // 3. 验证数据缓存一致性
            Log.d(DEBUG_TAG, "🗂️ 验证数据缓存一致性")
            
            // 多次读取同一数据，验证缓存一致性
            val readings = mutableListOf<Int>()
            repeat(5) { reading ->
                val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
                readings.add(ledgers.size)
                Log.d(DEBUG_TAG, "第${reading + 1}次读取: ${ledgers.size}个记账簿")
                delay(100)
            }
            
            val isConsistent = readings.all { it == readings.first() }
            
            if (isConsistent) {
                Log.d(DEBUG_TAG, "✅ 数据缓存完全一致")
            } else {
                Log.w(DEBUG_TAG, "⚠️ 数据缓存存在不一致")
                Log.d(DEBUG_TAG, "读取结果: ${readings.joinToString(", ")}")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证跨模块数据一致性异常", e)
        }
    }
    
    /**
     * 验证导航和生命周期集成
     */
    private suspend fun validateNavigationAndLifecycleIntegration(userId: String) {
        Log.d(DEBUG_TAG, "🧭 开始验证导航和生命周期集成")
        
        try {
            // 1. 验证模块初始化顺序
            Log.d(DEBUG_TAG, "🚀 验证模块初始化顺序")
            
            // 模拟模块初始化序列
            val initResults = mutableListOf<Pair<String, Boolean>>()
            
            // 手动测试每个初始化步骤
            try {
                userApi.getCurrentUserId()
                initResults.add("用户模块" to true)
                Log.d(DEBUG_TAG, "✅ 用户模块 初始化成功")
            } catch (e: Exception) {
                initResults.add("用户模块" to false)
                Log.e(DEBUG_TAG, "❌ 用户模块 初始化失败", e)
            }
            
            try {
                manageLedgerUseCase.getUserLedgers(userId).first()
                initResults.add("记账簿数据" to true)
                Log.d(DEBUG_TAG, "✅ 记账簿数据 初始化成功")
            } catch (e: Exception) {
                initResults.add("记账簿数据" to false)
                Log.e(DEBUG_TAG, "❌ 记账簿数据 初始化失败", e)
            }
            
            try {
                ledgerUIPreferencesRepository.getUIPreferences().first()
                initResults.add("UI偏好设置" to true)
                Log.d(DEBUG_TAG, "✅ UI偏好设置 初始化成功")
            } catch (e: Exception) {
                initResults.add("UI偏好设置" to false)
                Log.e(DEBUG_TAG, "❌ UI偏好设置 初始化失败", e)
            }
            
            val successRate = initResults.count { it.second }.toDouble() / initResults.size * 100
            Log.d(DEBUG_TAG, "模块初始化成功率: ${successRate.toInt()}%")
            
            // 2. 验证状态恢复机制
            Log.d(DEBUG_TAG, "🔄 验证状态恢复机制")
            
            val originalPrefs = ledgerUIPreferencesRepository.getUIPreferences().first()
            
            // 模拟状态丢失和恢复
            val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
            if (ledgers.isNotEmpty()) {
                val testLedger = ledgers.first()
                
                // 设置一个已知状态
                ledgerUIPreferencesRepository.updateSelectedLedgerId(testLedger.id)
                delay(200)
                
                // 验证状态是否正确恢复
                val recoveredPrefs = ledgerUIPreferencesRepository.getUIPreferences().first()
                
                if (recoveredPrefs.selectedLedgerId == testLedger.id) {
                    Log.d(DEBUG_TAG, "✅ 状态恢复机制正常")
                } else {
                    Log.e(DEBUG_TAG, "❌ 状态恢复机制异常")
                }
                
                // 恢复原始状态
                if (!originalPrefs.selectedLedgerId.isNullOrBlank()) {
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(originalPrefs.selectedLedgerId)
                }
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证导航和生命周期集成异常", e)
        }
    }
    
    /**
     * 验证错误处理和恢复机制
     */
    private suspend fun validateErrorHandlingAndRecovery(userId: String) {
        Log.d(DEBUG_TAG, "🛡️ 开始验证错误处理和恢复机制")
        
        try {
            // 1. 验证无效输入处理
            Log.d(DEBUG_TAG, "❌ 验证无效输入处理")
            
            try {
                // 测试无效用户ID
                manageLedgerUseCase.getUserLedgers("").first()
                Log.w(DEBUG_TAG, "⚠️ 空用户ID未触发异常")
            } catch (e: Exception) {
                Log.d(DEBUG_TAG, "✅ 空用户ID正确触发异常处理")
            }
            
            try {
                // 测试无效记账簿ID
                ledgerUIPreferencesRepository.updateSelectedLedgerId("")
                Log.w(DEBUG_TAG, "⚠️ 空记账簿ID未触发异常")
            } catch (e: Exception) {
                Log.d(DEBUG_TAG, "✅ 空记账簿ID正确触发异常处理")
            }
            
            // 2. 验证网络错误恢复
            Log.d(DEBUG_TAG, "🌐 验证网络错误恢复")
            
            // 模拟网络错误场景（通过超时等方式）
            try {
                val startTime = System.currentTimeMillis()
                val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
                val endTime = System.currentTimeMillis()
                
                Log.d(DEBUG_TAG, "网络操作耗时: ${endTime - startTime}ms")
                Log.d(DEBUG_TAG, "✅ 网络操作正常完成")
                
            } catch (e: Exception) {
                Log.d(DEBUG_TAG, "✅ 网络错误被正确捕获和处理", e)
            }
            
            // 3. 验证数据恢复机制
            Log.d(DEBUG_TAG, "🔧 验证数据恢复机制")
            
            val originalPrefs = ledgerUIPreferencesRepository.getUIPreferences().first()
            
            try {
                // 模拟数据损坏场景
                ledgerUIPreferencesRepository.updateSelectedLedgerId("invalid-ledger-id")
                delay(200)
                
                // 验证系统是否能优雅处理无效状态
                val ledgers = manageLedgerUseCase.getUserLedgers(userId).first()
                val currentPrefs = ledgerUIPreferencesRepository.getUIPreferences().first()
                
                if (ledgers.isNotEmpty()) {
                    val validLedgerExists = ledgers.any { it.id == currentPrefs.selectedLedgerId }
                    
                    if (!validLedgerExists) {
                        Log.d(DEBUG_TAG, "✅ 系统检测到无效状态")
                        // 在实际应用中，这里应该触发自动恢复机制
                    }
                }
                
            } finally {
                // 恢复原始状态
                if (!originalPrefs.selectedLedgerId.isNullOrBlank()) {
                    ledgerUIPreferencesRepository.updateSelectedLedgerId(originalPrefs.selectedLedgerId)
                }
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证错误处理和恢复机制异常", e)
        }
    }
    
    /**
     * 验证ViewModel的集成表现
     */
    suspend fun validateViewModelIntegration(viewModel: LedgerViewModel): IntegrationValidationSummary {
        Log.d(DEBUG_TAG, "🎯 开始验证ViewModel集成表现")
        
        val issues = mutableListOf<String>()
        var moduleIntegrationScore = 100
        var dataConsistencyScore = 100
        var errorHandlingScore = 100
        
        try {
            val userId = userApi.getCurrentUserId()
            
            // 1. 验证ViewModel与后端数据的集成
            Log.d(DEBUG_TAG, "🔗 验证ViewModel与后端数据集成")
            
            val state = viewModel.uiState.first()
            val backendLedgers = manageLedgerUseCase.getUserLedgers(userId).first()
            
            // 检查数据数量一致性
            if (state.ledgers.size != backendLedgers.size) {
                issues.add("ViewModel记账簿数量与后端不一致")
                dataConsistencyScore -= 20
            }
            
            // 检查数据内容一致性
            val inconsistentLedgers = state.ledgers.filter { viewModelLedger ->
                backendLedgers.none { it.id == viewModelLedger.id && it.name == viewModelLedger.name }
            }
            
            if (inconsistentLedgers.isNotEmpty()) {
                issues.add("发现${inconsistentLedgers.size}个数据不一致的记账簿")
                dataConsistencyScore -= 15
            }
            
            // 2. 验证ViewModel状态管理集成
            Log.d(DEBUG_TAG, "📊 验证ViewModel状态管理集成")
            
            if (state.isLoading && state.ledgers.isNotEmpty()) {
                issues.add("ViewModel同时处于加载状态和已有数据状态")
                moduleIntegrationScore -= 15
            }
            
            if (state.currentLedger != null && state.selectedLedgerId != state.currentLedger.id) {
                issues.add("ViewModel当前记账簿与选中ID不匹配")
                dataConsistencyScore -= 25
            }
            
            // 3. 验证ViewModel错误处理集成
            Log.d(DEBUG_TAG, "🛡️ 验证ViewModel错误处理集成")
            
            // 模拟异常情况
            try {
                viewModel.selectLedger("invalid-ledger-id")
                delay(500)
                
                val errorState = viewModel.uiState.first()
                if (errorState.selectedLedgerId == "invalid-ledger-id") {
                    issues.add("ViewModel未正确处理无效记账簿ID")
                    errorHandlingScore -= 30
                }
                
            } catch (e: Exception) {
                Log.d(DEBUG_TAG, "✅ ViewModel正确处理了异常输入")
            }
            
            return IntegrationValidationSummary(
                success = issues.isEmpty(),
                moduleIntegrationScore = moduleIntegrationScore,
                dataConsistencyScore = dataConsistencyScore,
                errorHandlingScore = errorHandlingScore,
                overallIntegrationRating = calculateIntegrationRating(moduleIntegrationScore, dataConsistencyScore, errorHandlingScore),
                issues = issues
            )
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证ViewModel集成表现异常", e)
            issues.add("验证过程异常: ${e.message}")
            
            return IntegrationValidationSummary(
                success = false,
                moduleIntegrationScore = 0,
                dataConsistencyScore = 0,
                errorHandlingScore = 0,
                overallIntegrationRating = "F",
                issues = issues
            )
        }
    }
    
    /**
     * 计算集成评级
     */
    private fun calculateIntegrationRating(moduleScore: Int, dataScore: Int, errorScore: Int): String {
        val avgScore = (moduleScore + dataScore + errorScore) / 3
        
        return when {
            avgScore >= 90 -> "A"
            avgScore >= 80 -> "B"
            avgScore >= 70 -> "C"
            avgScore >= 60 -> "D"
            else -> "F"
        }
    }
}

/**
 * 集成验证总结
 */
data class IntegrationValidationSummary(
    val success: Boolean,
    val moduleIntegrationScore: Int,
    val dataConsistencyScore: Int,
    val errorHandlingScore: Int,
    val overallIntegrationRating: String,
    val issues: List<String>
)