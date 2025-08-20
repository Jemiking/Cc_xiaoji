package com.ccxiaoji.feature.ledger.debug

import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
import com.ccxiaoji.feature.ledger.domain.repository.LedgerUIPreferencesRepository
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
 * 默认记账簿机制验证器
 * 专门用于验证应用启动时默认记账簿的创建和选择逻辑
 */
@Singleton
class DefaultLedgerMechanismValidator @Inject constructor(
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val userApi: UserApi,
    private val ledgerUIPreferencesRepository: LedgerUIPreferencesRepository
) {
    
    companion object {
        private const val TAG = "DefaultLedgerValidator"
        private const val DEBUG_TAG = "LEDGER_DEFAULT_DEBUG"
    }
    
    private val validatorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * 执行完整的默认记账簿机制验证
     */
    fun executeFullDefaultLedgerValidation() {
        validatorScope.launch {
            try {
                Log.i(TAG, "🚀 开始执行默认记账簿机制验证")
                
                val userId = userApi.getCurrentUserId()
                
                // 1. 验证默认记账簿创建机制
                validateDefaultLedgerCreation(userId)
                
                // 2. 验证默认记账簿属性
                validateDefaultLedgerProperties(userId)
                
                // 3. 验证默认记账簿选择逻辑
                validateDefaultLedgerSelection(userId)
                
                // 4. 验证偏好设置持久化
                validateDefaultLedgerPreferences(userId)
                
                // 5. 验证幂等性
                validateIdempotency(userId)
                
                Log.i(TAG, "✅ 默认记账簿机制验证完成")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 默认记账簿机制验证失败", e)
            }
        }
    }
    
    /**
     * 验证默认记账簿创建机制
     */
    private suspend fun validateDefaultLedgerCreation(userId: String) {
        Log.d(DEBUG_TAG, "📝 开始验证默认记账簿创建机制")
        
        try {
            // 检查是否有默认记账簿
            val defaultLedgerResult = manageLedgerUseCase.getDefaultLedger(userId)
            
            when (defaultLedgerResult) {
                is com.ccxiaoji.common.base.BaseResult.Success -> {
                    val defaultLedger = defaultLedgerResult.data
                    Log.d(DEBUG_TAG, "✅ 默认记账簿存在: ${defaultLedger.name}")
                    
                    // 验证默认记账簿是否正确标记
                    if (defaultLedger.isDefault) {
                        Log.d(DEBUG_TAG, "✅ 默认记账簿正确标记为默认")
                    } else {
                        Log.e(DEBUG_TAG, "❌ 默认记账簿未正确标记为默认")
                    }
                }
                is com.ccxiaoji.common.base.BaseResult.Error -> {
                    Log.w(DEBUG_TAG, "⚠️ 未找到默认记账簿，尝试创建")
                    
                    // 尝试确保默认记账簿存在
                    val ensureResult = manageLedgerUseCase.ensureDefaultLedger(userId)
                    when (ensureResult) {
                        is com.ccxiaoji.common.base.BaseResult.Success -> {
                            val newDefaultLedger = ensureResult.data
                            Log.d(DEBUG_TAG, "✅ 成功创建默认记账簿: ${newDefaultLedger.name}")
                        }
                        is com.ccxiaoji.common.base.BaseResult.Error -> {
                            Log.e(DEBUG_TAG, "❌ 创建默认记账簿失败: ${ensureResult.exception.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证默认记账簿创建机制异常", e)
        }
    }
    
    /**
     * 验证默认记账簿属性
     */
    private suspend fun validateDefaultLedgerProperties(userId: String) {
        Log.d(DEBUG_TAG, "🔍 开始验证默认记账簿属性")
        
        try {
            val defaultLedgerResult = manageLedgerUseCase.getDefaultLedger(userId)
            
            when (defaultLedgerResult) {
                is com.ccxiaoji.common.base.BaseResult.Success -> {
                    val defaultLedger = defaultLedgerResult.data
                    
                    // 验证名称
                    if (defaultLedger.name == Ledger.DEFAULT_LEDGER_NAME) {
                        Log.d(DEBUG_TAG, "✅ 默认记账簿名称正确: ${defaultLedger.name}")
                    } else {
                        Log.e(DEBUG_TAG, "❌ 默认记账簿名称错误: 期望=${Ledger.DEFAULT_LEDGER_NAME}, 实际=${defaultLedger.name}")
                    }
                    
                    // 验证描述
                    if (defaultLedger.description == Ledger.DEFAULT_LEDGER_DESCRIPTION) {
                        Log.d(DEBUG_TAG, "✅ 默认记账簿描述正确")
                    } else {
                        Log.w(DEBUG_TAG, "⚠️ 默认记账簿描述异常: ${defaultLedger.description}")
                    }
                    
                    // 验证图标
                    if (defaultLedger.icon == "book") {
                        Log.d(DEBUG_TAG, "✅ 默认记账簿图标正确: ${defaultLedger.icon}")
                    } else {
                        Log.w(DEBUG_TAG, "⚠️ 默认记账簿图标异常: ${defaultLedger.icon}")
                    }
                    
                    // 验证颜色
                    if (defaultLedger.color == "#3A7AFE") {
                        Log.d(DEBUG_TAG, "✅ 默认记账簿颜色正确: ${defaultLedger.color}")
                    } else {
                        Log.w(DEBUG_TAG, "⚠️ 默认记账簿颜色异常: ${defaultLedger.color}")
                    }
                    
                    // 验证激活状态
                    if (defaultLedger.isActive) {
                        Log.d(DEBUG_TAG, "✅ 默认记账簿处于激活状态")
                    } else {
                        Log.e(DEBUG_TAG, "❌ 默认记账簿未激活")
                    }
                    
                    // 验证用户归属
                    if (defaultLedger.userId == userId) {
                        Log.d(DEBUG_TAG, "✅ 默认记账簿用户归属正确")
                    } else {
                        Log.e(DEBUG_TAG, "❌ 默认记账簿用户归属错误: 期望=$userId, 实际=${defaultLedger.userId}")
                    }
                }
                is com.ccxiaoji.common.base.BaseResult.Error -> {
                    Log.e(DEBUG_TAG, "❌ 无法获取默认记账簿进行属性验证")
                }
            }
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证默认记账簿属性异常", e)
        }
    }
    
    /**
     * 验证默认记账簿选择逻辑
     */
    private suspend fun validateDefaultLedgerSelection(userId: String) {
        Log.d(DEBUG_TAG, "🎯 开始验证默认记账簿选择逻辑")
        
        try {
            // 获取所有记账簿
            val allLedgers = manageLedgerUseCase.getUserLedgers(userId).first()
            
            if (allLedgers.isEmpty()) {
                Log.e(DEBUG_TAG, "❌ 用户没有任何记账簿")
                return
            }
            
            Log.d(DEBUG_TAG, "📊 用户记账簿总数: ${allLedgers.size}")
            
            // 查找默认记账簿
            val defaultLedgers = allLedgers.filter { it.isDefault }
            
            when {
                defaultLedgers.isEmpty() -> {
                    Log.e(DEBUG_TAG, "❌ 没有标记为默认的记账簿")
                }
                defaultLedgers.size == 1 -> {
                    val defaultLedger = defaultLedgers.first()
                    Log.d(DEBUG_TAG, "✅ 找到唯一默认记账簿: ${defaultLedger.name}")
                    
                    // 验证显示顺序
                    if (defaultLedger.displayOrder == 0) {
                        Log.d(DEBUG_TAG, "✅ 默认记账簿显示顺序正确")
                    } else {
                        Log.w(DEBUG_TAG, "⚠️ 默认记账簿显示顺序异常: ${defaultLedger.displayOrder}")
                    }
                }
                defaultLedgers.size > 1 -> {
                    Log.e(DEBUG_TAG, "❌ 发现多个默认记账簿，数据不一致")
                    defaultLedgers.forEach { ledger ->
                        Log.e(DEBUG_TAG, "  重复默认记账簿: ${ledger.name} (${ledger.id})")
                    }
                }
            }
            
            // 验证记账簿排序逻辑
            val sortedLedgers = allLedgers.sortedWith(
                compareBy<Ledger> { !it.isDefault }.thenBy { it.displayOrder }.thenBy { it.createdAt }
            )
            
            if (sortedLedgers.first().isDefault) {
                Log.d(DEBUG_TAG, "✅ 记账簿排序逻辑正确，默认记账簿排在首位")
            } else {
                Log.w(DEBUG_TAG, "⚠️ 记账簿排序逻辑异常，默认记账簿未排在首位")
            }
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证默认记账簿选择逻辑异常", e)
        }
    }
    
    /**
     * 验证默认记账簿偏好设置持久化
     */
    private suspend fun validateDefaultLedgerPreferences(userId: String) {
        Log.d(DEBUG_TAG, "💾 开始验证默认记账簿偏好设置")
        
        try {
            val defaultLedgerResult = manageLedgerUseCase.getDefaultLedger(userId)
            
            when (defaultLedgerResult) {
                is com.ccxiaoji.common.base.BaseResult.Success -> {
                    val defaultLedger = defaultLedgerResult.data
                    
                    // 获取当前偏好设置
                    val preferences = ledgerUIPreferencesRepository.getUIPreferences().first()
                    
                    // 如果没有选择记账簿，应该自动选择默认记账簿
                    if (preferences.selectedLedgerId == null) {
                        Log.d(DEBUG_TAG, "⚠️ 偏好设置中没有选中记账簿，应该自动选择默认记账簿")
                        
                        // 设置默认记账簿为选中状态
                        ledgerUIPreferencesRepository.updateSelectedLedgerId(defaultLedger.id)
                        Log.d(DEBUG_TAG, "✅ 已自动设置默认记账簿为选中状态")
                        
                    } else if (preferences.selectedLedgerId == defaultLedger.id) {
                        Log.d(DEBUG_TAG, "✅ 偏好设置中正确选中了默认记账簿")
                        
                    } else {
                        Log.d(DEBUG_TAG, "ℹ️ 偏好设置中选中了其他记账簿: ${preferences.selectedLedgerId}")
                    }
                }
                is com.ccxiaoji.common.base.BaseResult.Error -> {
                    Log.e(DEBUG_TAG, "❌ 无法获取默认记账簿进行偏好设置验证")
                }
            }
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证默认记账簿偏好设置异常", e)
        }
    }
    
    /**
     * 验证默认记账簿创建的幂等性
     */
    private suspend fun validateIdempotency(userId: String) {
        Log.d(DEBUG_TAG, "🔁 开始验证默认记账簿创建幂等性")
        
        try {
            // 第一次调用
            val firstCallResult = manageLedgerUseCase.ensureDefaultLedger(userId)
            val firstCallSuccess = firstCallResult is com.ccxiaoji.common.base.BaseResult.Success
            
            // 第二次调用
            val secondCallResult = manageLedgerUseCase.ensureDefaultLedger(userId)
            val secondCallSuccess = secondCallResult is com.ccxiaoji.common.base.BaseResult.Success
            
            if (firstCallSuccess && secondCallSuccess) {
                val firstLedger = (firstCallResult as com.ccxiaoji.common.base.BaseResult.Success).data
                val secondLedger = (secondCallResult as com.ccxiaoji.common.base.BaseResult.Success).data
                
                if (firstLedger.id == secondLedger.id) {
                    Log.d(DEBUG_TAG, "✅ 默认记账簿创建具有幂等性，重复调用返回相同记账簿")
                } else {
                    Log.e(DEBUG_TAG, "❌ 默认记账簿创建缺乏幂等性，重复调用创建了不同记账簿")
                }
                
                // 验证只有一个默认记账簿
                val allLedgers = manageLedgerUseCase.getUserLedgers(userId).first()
                val defaultCount = allLedgers.count { it.isDefault }
                
                if (defaultCount == 1) {
                    Log.d(DEBUG_TAG, "✅ 幂等性验证通过，只有一个默认记账簿")
                } else {
                    Log.e(DEBUG_TAG, "❌ 幂等性验证失败，默认记账簿数量: $defaultCount")
                }
            } else {
                Log.e(DEBUG_TAG, "❌ 无法完成幂等性验证，默认记账簿创建失败")
            }
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证默认记账簿幂等性异常", e)
        }
    }
    
    /**
     * 针对ViewModel的默认记账簿验证
     */
    suspend fun validateViewModelDefaultLedger(viewModel: LedgerViewModel): DefaultLedgerValidationSummary {
        Log.d(DEBUG_TAG, "🎯 开始验证ViewModel中的默认记账簿")
        
        val issues = mutableListOf<String>()
        
        try {
            // 等待ViewModel初始化完成
            var attempts = 0
            while (attempts < 100) { // 最大等待10秒
                kotlinx.coroutines.delay(100)
                val state = viewModel.uiState.first()
                if (!state.isLedgerLoading && state.ledgers.isNotEmpty()) {
                    break
                }
                attempts++
            }
            
            val state = viewModel.uiState.first()
            
            // 检查记账簿列表
            if (state.ledgers.isEmpty()) {
                issues.add("ViewModel中记账簿列表为空")
            } else {
                Log.d(DEBUG_TAG, "✅ ViewModel记账簿列表: ${state.ledgers.size}个记账簿")
            }
            
            // 检查默认记账簿
            val defaultLedgers = state.ledgers.filter { it.isDefault }
            when {
                defaultLedgers.isEmpty() -> {
                    issues.add("ViewModel中没有默认记账簿")
                }
                defaultLedgers.size > 1 -> {
                    issues.add("ViewModel中有多个默认记账簿")
                }
                else -> {
                    val defaultLedger = defaultLedgers.first()
                    Log.d(DEBUG_TAG, "✅ ViewModel默认记账簿: ${defaultLedger.name}")
                    
                    // 检查是否被选中
                    if (state.currentLedger?.id == defaultLedger.id) {
                        Log.d(DEBUG_TAG, "✅ 默认记账簿已被正确选中")
                    } else {
                        issues.add("默认记账簿未被选中，当前记账簿: ${state.currentLedger?.name}")
                    }
                }
            }
            
            // 检查加载状态
            if (state.isLedgerLoading) {
                issues.add("记账簿仍在加载中")
            }
            
            return DefaultLedgerValidationSummary(
                success = issues.isEmpty(),
                ledgerCount = state.ledgers.size,
                hasDefaultLedger = defaultLedgers.isNotEmpty(),
                defaultLedgerSelected = state.currentLedger?.isDefault == true,
                defaultLedgerName = defaultLedgers.firstOrNull()?.name,
                issues = issues
            )
            
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "❌ 验证ViewModel默认记账簿异常", e)
            issues.add("验证过程异常: ${e.message}")
            
            return DefaultLedgerValidationSummary(
                success = false,
                ledgerCount = 0,
                hasDefaultLedger = false,
                defaultLedgerSelected = false,
                defaultLedgerName = null,
                issues = issues
            )
        }
    }
}

/**
 * 默认记账簿验证总结
 */
data class DefaultLedgerValidationSummary(
    val success: Boolean,
    val ledgerCount: Int,
    val hasDefaultLedger: Boolean,
    val defaultLedgerSelected: Boolean,
    val defaultLedgerName: String?,
    val issues: List<String>
)