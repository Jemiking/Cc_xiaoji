package com.ccxiaoji.feature.schedule.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.usecase.GetActiveShiftsUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.GetStatisticsUseCase
import com.ccxiaoji.feature.schedule.presentation.ui.statistics.TimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * 排班统计界面的ViewModel
 */
@HiltViewModel
class ScheduleStatisticsViewModel @Inject constructor(
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val getActiveShiftsUseCase: GetActiveShiftsUseCase
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(ScheduleStatisticsUiState())
    val uiState: StateFlow<ScheduleStatisticsUiState> = _uiState.asStateFlow()
    
    // 统计数据
    private val _statistics = MutableStateFlow<ScheduleStatistics?>(null)
    val statistics: StateFlow<ScheduleStatistics?> = _statistics.asStateFlow()
    
    // 班次列表
    val shifts: StateFlow<List<Shift>> = getActiveShiftsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 周起始日设置（默认周一）
    private val _weekStartDay = MutableStateFlow(DayOfWeek.MONDAY)
    val weekStartDay: StateFlow<DayOfWeek> = _weekStartDay.asStateFlow()
    
    init {
        // 初始加载本月统计
        loadStatistics()
    }
    
    /**
     * 更新时间范围
     */
    fun updateTimeRange(range: TimeRange) {
        _uiState.update { it.copy(timeRange = range) }
        loadStatistics()
    }
    
    /**
     * 更新自定义日期范围
     */
    fun updateCustomDateRange(startDate: LocalDate, endDate: LocalDate) {
        _uiState.update {
            it.copy(
                customStartDate = startDate,
                customEndDate = if (endDate.isBefore(startDate)) startDate else endDate
            )
        }
        if (_uiState.value.timeRange == TimeRange.CUSTOM) {
            loadStatistics()
        }
    }
    
    /**
     * 加载统计数据
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val (startDate, endDate) = getDateRange()
                val stats = getStatisticsUseCase(startDate, endDate)
                _statistics.value = stats
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "加载统计数据失败"
                    )
                }
            }
        }
    }
    
    /**
     * 获取日期范围
     */
    private fun getDateRange(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return when (_uiState.value.timeRange) {
            TimeRange.THIS_WEEK -> {
                val startOfWeek = today.with(TemporalAdjusters.previousOrSame(_weekStartDay.value))
                val endOfWeek = startOfWeek.plusDays(6)
                startOfWeek to endOfWeek
            }
            TimeRange.THIS_MONTH -> {
                val yearMonth = YearMonth.now()
                yearMonth.atDay(1) to yearMonth.atEndOfMonth()
            }
            TimeRange.LAST_MONTH -> {
                val lastMonth = YearMonth.now().minusMonths(1)
                lastMonth.atDay(1) to lastMonth.atEndOfMonth()
            }
            TimeRange.CUSTOM -> {
                _uiState.value.customStartDate to _uiState.value.customEndDate
            }
        }
    }
}

/**
 * 统计界面UI状态
 */
data class ScheduleStatisticsUiState(
    val timeRange: TimeRange = TimeRange.THIS_MONTH,
    val customStartDate: LocalDate = LocalDate.now().minusMonths(1),
    val customEndDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)