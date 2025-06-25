package com.ccxiaoji.feature.plan.presentation.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.PlanStatistics
import com.ccxiaoji.feature.plan.domain.usecase.plan.GetPlanStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 进度分析页面ViewModel
 */
@HiltViewModel
class ProgressAnalysisViewModel @Inject constructor(
    private val getPlanStatisticsUseCase: GetPlanStatisticsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProgressAnalysisUiState())
    val uiState: StateFlow<ProgressAnalysisUiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics()
    }
    
    /**
     * 加载统计数据
     */
    fun loadStatistics() {
        viewModelScope.launch {
            getPlanStatisticsUseCase()
                .onStart {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "加载统计数据失败"
                    )
                }
                .collect { statistics ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        statistics = statistics,
                        error = null
                    )
                }
        }
    }
    
    /**
     * 切换显示模式
     */
    fun toggleChartType(chartType: ChartType) {
        _uiState.value = _uiState.value.copy(selectedChartType = chartType)
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * 进度分析页面UI状态
 */
data class ProgressAnalysisUiState(
    val isLoading: Boolean = false,
    val statistics: PlanStatistics? = null,
    val selectedChartType: ChartType = ChartType.STATUS_PIE,
    val error: String? = null
)

/**
 * 图表类型
 */
enum class ChartType {
    STATUS_PIE,         // 状态饼图
    PROGRESS_BAR,       // 进度分布柱状图
    MONTHLY_TREND,      // 月度趋势图
    TAG_ANALYSIS        // 标签分析
}