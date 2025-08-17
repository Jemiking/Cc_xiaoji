package com.ccxiaoji.feature.ledger.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.repository.SavingsGoalRepository
import com.ccxiaoji.feature.ledger.domain.model.SavingsContribution
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SavingsGoalViewModel @Inject constructor(
    private val repository: SavingsGoalRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "SavingsGoalViewModel"
    }
    
    private val _uiState = MutableStateFlow(SavingsGoalUiState())
    val uiState: StateFlow<SavingsGoalUiState> = _uiState.asStateFlow()
    
    val activeSavingsGoals = repository.getActiveSavingsGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        Log.d(TAG, "SavingsGoalViewModel初始化完成")
        // 监听储蓄目标数据变化
        viewModelScope.launch {
            activeSavingsGoals.collect { goals ->
                Log.d(TAG, "获取到储蓄目标数据：${goals.size}个目标")
                goals.forEach { goal ->
                    Log.d(TAG, "目标：${goal.name}, 进度：${goal.progressPercentage}%")
                }
            }
        }
    }
    
    private val _selectedGoal = MutableStateFlow<SavingsGoal?>(null)
    val selectedGoal: StateFlow<SavingsGoal?> = _selectedGoal.asStateFlow()
    
    private val _contributions = MutableStateFlow<List<SavingsContribution>>(emptyList())
    val contributions: StateFlow<List<SavingsContribution>> = _contributions.asStateFlow()
    
    fun selectGoal(goalId: Long) {
        Log.d(TAG, "选择储蓄目标，ID: $goalId")
        viewModelScope.launch {
            try {
                Log.d(TAG, "开始获取储蓄目标详情")
                val goal = repository.getSavingsGoalById(goalId)
                if (goal != null) {
                    Log.d(TAG, "找到储蓄目标：${goal.name}, 当前金额：${goal.currentAmount}")
                    _selectedGoal.value = goal
                    
                    Log.d(TAG, "开始获取贡献记录")
                    repository.getContributionsByGoalId(goalId).collect { contributionList ->
                        Log.d(TAG, "获取到${contributionList.size}条贡献记录")
                        _contributions.value = contributionList
                    }
                } else {
                    Log.w(TAG, "未找到储蓄目标，ID: $goalId")
                    _selectedGoal.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "选择储蓄目标时发生异常", e)
            }
        }
    }
    
    fun createSavingsGoal(
        name: String,
        targetAmount: Double,
        targetDate: LocalDate?,
        description: String?,
        color: String,
        iconName: String
    ) {
        Log.d(TAG, "开始创建储蓄目标")
        Log.d(TAG, "参数 - 名称: '$name', 目标金额: $targetAmount, 目标日期: $targetDate")
        Log.d(TAG, "参数 - 描述: '$description', 颜色: '$color', 图标: '$iconName'")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            Log.d(TAG, "设置UI状态为加载中")
            
            try {
                // 验证输入参数
                if (name.isBlank()) {
                    Log.e(TAG, "名称为空，无法创建储蓄目标")
                    _uiState.update { it.copy(isLoading = false, error = "目标名称不能为空") }
                    return@launch
                }
                
                if (targetAmount <= 0) {
                    Log.e(TAG, "目标金额无效：$targetAmount")
                    _uiState.update { it.copy(isLoading = false, error = "目标金额必须大于0") }
                    return@launch
                }
                
                Log.d(TAG, "输入验证通过，开始创建储蓄目标对象")
                
                val goal = SavingsGoal(
                    userId = "default_user", // TODO: Get from current user context
                    name = name,
                    targetAmount = targetAmount,
                    targetDate = targetDate,
                    description = description,
                    color = color,
                    iconName = iconName
                )
                
                Log.d(TAG, "储蓄目标对象创建完成，调用repository.createSavingsGoal")
                Log.d(TAG, "目标详情 - ID: ${goal.id}, 用户ID: ${goal.userId}")
                
                repository.createSavingsGoal(goal)
                Log.d(TAG, "储蓄目标创建成功，更新UI状态")
                
                _uiState.update { it.copy(isLoading = false, message = "储蓄目标创建成功") }
                Log.d(TAG, "储蓄目标创建流程完成")
            } catch (e: Exception) {
                Log.e(TAG, "创建储蓄目标时发生异常", e)
                Log.e(TAG, "异常详情 - 类型: ${e.javaClass.simpleName}, 消息: ${e.message}")
                e.printStackTrace()
                _uiState.update { 
                    it.copy(isLoading = false, error = "创建失败: ${e.message}")
                }
            }
        }
    }
    
    fun updateSavingsGoal(goal: SavingsGoal) {
        Log.d(TAG, "开始更新储蓄目标")
        Log.d(TAG, "目标详情 - ID: ${goal.id}, 名称: '${goal.name}', 目标金额: ${goal.targetAmount}")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            Log.d(TAG, "设置UI状态为加载中")
            
            try {
                Log.d(TAG, "调用repository.updateSavingsGoal")
                repository.updateSavingsGoal(goal)
                Log.d(TAG, "储蓄目标更新成功")
                
                _uiState.update { it.copy(isLoading = false, message = "储蓄目标更新成功") }
                Log.d(TAG, "储蓄目标更新流程完成")
            } catch (e: Exception) {
                Log.e(TAG, "更新储蓄目标时发生异常", e)
                Log.e(TAG, "异常详情 - 类型: ${e.javaClass.simpleName}, 消息: ${e.message}")
                _uiState.update { 
                    it.copy(isLoading = false, error = "更新失败: ${e.message}")
                }
            }
        }
    }
    
    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.deleteSavingsGoal(goal)
                _uiState.update { it.copy(isLoading = false, message = "储蓄目标删除成功") }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "删除失败: ${e.message}")
                }
            }
        }
    }
    
    fun addContribution(goalId: Long, amount: Double, note: String?) {
        Log.d(TAG, "开始添加储蓄贡献")
        Log.d(TAG, "参数 - 目标ID: $goalId, 金额: $amount, 备注: '$note'")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            Log.d(TAG, "设置UI状态为加载中")
            
            try {
                if (amount == 0.0) {
                    Log.w(TAG, "金额为0，无需添加贡献")
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }
                
                Log.d(TAG, "创建贡献对象")
                val contribution = SavingsContribution(
                    goalId = goalId,
                    amount = amount,
                    note = note
                )
                
                Log.d(TAG, "调用repository.addContribution")
                repository.addContribution(contribution)
                Log.d(TAG, "贡献添加成功")
                
                // Refresh selected goal to show updated amount
                Log.d(TAG, "刷新目标数据以显示更新后的金额")
                selectGoal(goalId)
                
                val action = if (amount > 0) "存入" else "取出"
                Log.d(TAG, "操作完成：$action ¥${kotlin.math.abs(amount)}")
                _uiState.update { 
                    it.copy(isLoading = false, message = "$action ¥${kotlin.math.abs(amount)} 成功")
                }
            } catch (e: Exception) {
                Log.e(TAG, "添加储蓄贡献时发生异常", e)
                Log.e(TAG, "异常详情 - 类型: ${e.javaClass.simpleName}, 消息: ${e.message}")
                _uiState.update { 
                    it.copy(isLoading = false, error = "操作失败: ${e.message}")
                }
            }
        }
    }
    
    fun deleteContribution(contribution: SavingsContribution) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.deleteContribution(contribution)
                
                // Refresh selected goal to show updated amount
                selectGoal(contribution.goalId)
                
                _uiState.update { it.copy(isLoading = false, message = "记录删除成功") }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "删除失败: ${e.message}")
                }
            }
        }
    }
    
    suspend fun calculateEstimatedCompletionDate(goal: SavingsGoal): LocalDate? {
        if (goal.isCompleted || goal.currentAmount <= 0) return null
        
        // Get recent contributions to calculate average monthly saving
        val contributions = repository.getRecentContributions(goal.id, 30)
        val totalContributions = contributions.filter { it.amount > 0 }.sumOf { it.amount }
        
        if (totalContributions > 0) {
            // Assume same saving rate continues
            val daysInPeriod = 30
            val dailyRate = totalContributions / daysInPeriod
            val remainingDays = (goal.remainingAmount / dailyRate).toInt()
            
            return LocalDate.now().plusDays(remainingDays.toLong())
        }
        
        return null
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

data class SavingsGoalUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)