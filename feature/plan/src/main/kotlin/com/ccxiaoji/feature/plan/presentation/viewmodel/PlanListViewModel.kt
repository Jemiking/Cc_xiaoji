package com.ccxiaoji.feature.plan.presentation.plan.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanFilter
import com.ccxiaoji.feature.plan.domain.model.PlanSortBy
import com.ccxiaoji.feature.plan.domain.usecase.plan.DeletePlanUseCase
import com.ccxiaoji.feature.plan.domain.usecase.plan.DeleteAllPlansUseCase
import com.ccxiaoji.feature.plan.domain.usecase.plan.GetAllPlansUseCase
import com.ccxiaoji.feature.plan.domain.usecase.plan.SearchPlansUseCase
import com.ccxiaoji.feature.plan.domain.usecase.plan.UpdatePlanProgressUseCase
import com.ccxiaoji.feature.plan.domain.usecase.performance.RunPerformanceTestUseCase
import com.ccxiaoji.feature.plan.domain.usecase.performance.PerformanceTestResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * 计划列表页面ViewModel（性能优化版本）
 * 添加了缓存机制，避免重复计算
 */
@HiltViewModel
class PlanListViewModel @Inject constructor(
    private val getAllPlansUseCase: GetAllPlansUseCase,
    private val searchPlansUseCase: SearchPlansUseCase,
    private val updatePlanProgressUseCase: UpdatePlanProgressUseCase,
    private val deletePlanUseCase: DeletePlanUseCase,
    private val deleteAllPlansUseCase: DeleteAllPlansUseCase,
    private val runPerformanceTestUseCase: RunPerformanceTestUseCase
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(PlanListUiState())
    val uiState: StateFlow<PlanListUiState> = _uiState.asStateFlow()
    
    // 搜索相关
    private val searchQuery = MutableStateFlow("")
    private var searchJob: Job? = null
    
    // 缓存机制
    private val planCache = ConcurrentHashMap<String, Plan>()
    private val expandedStateCache = ConcurrentHashMap<String, Boolean>()
    private var lastSearchParams: SearchParams? = null
    private var cachedPlans: List<Plan>? = null
    
    init {
        loadPlans()
        
        // 监听搜索关键词变化
        viewModelScope.launch {
            searchQuery
                .debounce(300) // 防抖，避免频繁搜索
                .distinctUntilChanged()
                .collect { query ->
                    performSearch()
                }
        }
    }
    
    /**
     * 加载计划列表
     */
    fun loadPlans() {
        performSearch()
    }
    
    /**
     * 执行搜索（带缓存）
     */
    private fun performSearch() {
        val currentParams = SearchParams(
            query = _uiState.value.searchQuery,
            filter = _uiState.value.filter,
            sortBy = _uiState.value.sortBy
        )
        
        // 检查缓存
        if (currentParams == lastSearchParams && cachedPlans != null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                plans = cachedPlans!!,
                error = null
            )
            return
        }
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            searchPlansUseCase(
                query = currentParams.query,
                filter = currentParams.filter,
                sortBy = currentParams.sortBy
            )
                .onStart {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "搜索失败"
                    )
                    // 清除缓存
                    lastSearchParams = null
                    cachedPlans = null
                }
                .collect { plans ->
                    // 更新缓存
                    planCache.clear()
                    plans.forEach { plan ->
                        cachePlan(plan)
                    }
                    
                    // 保存搜索结果
                    lastSearchParams = currentParams
                    cachedPlans = plans
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        plans = plans,
                        error = null
                    )
                }
        }
    }
    
    /**
     * 递归缓存计划及其子计划
     */
    private fun cachePlan(plan: Plan) {
        planCache[plan.id] = plan
        plan.children.forEach { cachePlan(it) }
    }
    
    /**
     * 更新搜索关键词
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchQuery.value = query
    }
    
    /**
     * 更新筛选条件
     */
    fun updateFilter(filter: PlanFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
        // 清除缓存，强制重新搜索
        lastSearchParams = null
        cachedPlans = null
        performSearch()
    }
    
    /**
     * 更新排序方式
     */
    fun updateSortBy(sortBy: PlanSortBy) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy)
        // 清除缓存，强制重新搜索
        lastSearchParams = null
        cachedPlans = null
        performSearch()
    }
    
    /**
     * 更新计划进度（使用缓存优化）
     */
    fun updatePlanProgress(planId: String, progress: Float) {
        // 立即更新UI（乐观更新）
        val cachedPlan = planCache[planId]
        if (cachedPlan != null) {
            val updatedPlan = cachedPlan.copy(progress = progress)
            planCache[planId] = updatedPlan
            
            // 更新UI中的计划
            val updatedPlans = _uiState.value.plans.map { plan ->
                updatePlanInTree(plan, planId, updatedPlan)
            }
            _uiState.value = _uiState.value.copy(plans = updatedPlans)
        }
        
        // 异步更新数据库
        viewModelScope.launch {
            updatePlanProgressUseCase(planId, progress)
                .onFailure { exception ->
                    // 回滚UI更新
                    if (cachedPlan != null) {
                        planCache[planId] = cachedPlan
                        val revertedPlans = _uiState.value.plans.map { plan ->
                            updatePlanInTree(plan, planId, cachedPlan)
                        }
                        _uiState.value = _uiState.value.copy(
                            plans = revertedPlans,
                            error = exception.message ?: "更新进度失败"
                        )
                    }
                }
        }
    }
    
    /**
     * 在树中更新指定计划
     */
    private fun updatePlanInTree(plan: Plan, targetId: String, updatedPlan: Plan): Plan {
        return if (plan.id == targetId) {
            updatedPlan
        } else {
            plan.copy(
                children = plan.children.map { child ->
                    updatePlanInTree(child, targetId, updatedPlan)
                }
            )
        }
    }
    
    /**
     * 删除计划
     */
    fun deletePlan(planId: String) {
        // 清除缓存
        planCache.remove(planId)
        lastSearchParams = null
        cachedPlans = null
        
        viewModelScope.launch {
            deletePlanUseCase(planId)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "删除失败"
                    )
                }
                .onSuccess {
                    // 重新加载数据
                    performSearch()
                }
        }
    }
    
    /**
     * 切换计划展开状态（使用缓存）
     */
    fun togglePlanExpanded(planId: String) {
        val currentExpanded = expandedStateCache[planId] ?: false
        val newExpanded = !currentExpanded
        expandedStateCache[planId] = newExpanded
        
        val expandedPlans = _uiState.value.expandedPlanIds.toMutableSet()
        if (newExpanded) {
            expandedPlans.add(planId)
        } else {
            expandedPlans.remove(planId)
        }
        _uiState.value = _uiState.value.copy(expandedPlanIds = expandedPlans)
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 生成测试数据
     */
    fun generateTestData(count: Int) {
        // 清除所有缓存
        clearAllCaches()
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                performanceTest = PerformanceTestState(
                    isRunning = true,
                    progress = 0f,
                    message = "正在生成${count}条测试数据..."
                )
            )
            
            try {
                val result = runPerformanceTestUseCase(count)
                _uiState.value = _uiState.value.copy(
                    performanceTest = PerformanceTestState(
                        isRunning = false,
                        result = result,
                        message = result.message
                    )
                )
                // 重新加载数据
                loadPlans()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    performanceTest = PerformanceTestState(
                        isRunning = false,
                        message = "生成失败: ${e.message}"
                    ),
                    error = e.message
                )
            }
        }
    }
    
    /**
     * 清空所有数据
     */
    fun clearAllData() {
        // 清除所有缓存
        clearAllCaches()
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                performanceTest = PerformanceTestState(
                    isRunning = true,
                    message = "正在清空所有数据..."
                )
            )
            
            deleteAllPlansUseCase()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        performanceTest = PerformanceTestState(
                            isRunning = false,
                            message = "数据已清空"
                        )
                    )
                    // 重新加载数据
                    loadPlans()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        performanceTest = PerformanceTestState(
                            isRunning = false,
                            message = "清空失败: ${exception.message}"
                        ),
                        error = exception.message
                    )
                }
        }
    }
    
    /**
     * 清除所有缓存
     */
    private fun clearAllCaches() {
        planCache.clear()
        expandedStateCache.clear()
        lastSearchParams = null
        cachedPlans = null
    }
    
    /**
     * 执行性能测试
     */
    fun runPerformanceTest() {
        generateTestData(1000)
    }
    
    /**
     * 清除性能测试状态
     */
    fun clearPerformanceTestState() {
        _uiState.value = _uiState.value.copy(performanceTest = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        // 清理资源
        clearAllCaches()
    }
}

/**
 * 搜索参数（用于缓存比较）
 */
private data class SearchParams(
    val query: String,
    val filter: PlanFilter,
    val sortBy: PlanSortBy
)

/**
 * 计划列表页面UI状态
 */
data class PlanListUiState(
    val isLoading: Boolean = false,
    val plans: List<Plan> = emptyList(),
    val expandedPlanIds: Set<String> = emptySet(),
    val error: String? = null,
    val searchQuery: String = "",
    val filter: PlanFilter = PlanFilter(),
    val sortBy: PlanSortBy = PlanSortBy.UPDATE_TIME_DESC,
    val performanceTest: PerformanceTestState? = null
)

/**
 * 性能测试状态
 */
data class PerformanceTestState(
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val result: PerformanceTestResult? = null,
    val message: String = ""
)