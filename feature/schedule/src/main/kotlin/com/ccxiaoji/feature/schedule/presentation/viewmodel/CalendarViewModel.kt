package com.ccxiaoji.feature.schedule.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.feature.schedule.domain.usecase.DeleteScheduleUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.GetMonthScheduleUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.GetScheduleStatisticsUseCase
import com.ccxiaoji.feature.schedule.presentation.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * 日历视图的ViewModel
 * 管理排班日历的状态和业务逻辑
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getMonthScheduleUseCase: GetMonthScheduleUseCase,
    private val getScheduleStatisticsUseCase: GetScheduleStatisticsUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
    private val themeManager: ThemeManager
) : ViewModel() {
    
    // 在属性初始化完成后，init块会在下面执行
    
    // 当前显示的年月
    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()
    
    // 选中的日期
    private val _selectedDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()
    
    // 当前月份的排班列表
    val schedules: StateFlow<List<Schedule>> = _currentYearMonth
        .flatMapLatest { yearMonth ->
            getMonthScheduleUseCase(yearMonth)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 月度统计信息
    private val _monthlyStatistics = MutableStateFlow<ScheduleStatistics?>(null)
    val monthlyStatistics: StateFlow<ScheduleStatistics?> = _monthlyStatistics.asStateFlow()
    
    // 一周开始日
    val weekStartDay: StateFlow<DayOfWeek> = themeManager.weekStartDay
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DayOfWeek.MONDAY
        )
    
    // 视图模式状态
    private val _viewMode = MutableStateFlow(CalendarViewMode.COMPACT)
    val viewMode: StateFlow<CalendarViewMode> = _viewMode.asStateFlow()
    // 默认模式应用标记与用户覆盖标记
    private var hasAppliedDefault = false
    private var userOverridden = false
    
    // UI状态
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()
    
    init {
        android.util.Log.d("CalendarViewModel", "ViewModel initialized")
        // 订阅默认紧凑模式，仅首次应用，避免后续设置变化打断当前用户选择
        themeManager.defaultCompactMode
            .onEach { compact ->
                if (!hasAppliedDefault && !userOverridden) {
                    _viewMode.value = if (compact) CalendarViewMode.COMPACT else CalendarViewMode.COMFORTABLE
                    hasAppliedDefault = true
                }
            }
            .launchIn(viewModelScope)

        // 加载初始统计
        viewModelScope.launch { loadMonthlyStatistics() }

    }
    
    /**
     * 切换到上一个月
     */
    fun navigateToPreviousMonth() {
        _currentYearMonth.value = _currentYearMonth.value.minusMonths(1)
        loadMonthlyStatistics()
    }
    
    /**
     * 切换到下一个月
     */
    fun navigateToNextMonth() {
        _currentYearMonth.value = _currentYearMonth.value.plusMonths(1)
        loadMonthlyStatistics()
    }
    
    /**
     * 切换到今天
     */
    fun navigateToToday() {
        _currentYearMonth.value = YearMonth.now()
        _selectedDate.value = LocalDate.now()
        loadMonthlyStatistics()
    }
    
    /**
     * 切换到指定年月
     */
    fun navigateToYearMonth(yearMonth: YearMonth) {
        _currentYearMonth.value = yearMonth
        // 清除选中日期，避免显示错误的月份的日期
        _selectedDate.value = null
        loadMonthlyStatistics()
    }
    
    /**
     * 选择日期
     */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }
    
    /**
     * 加载月度统计信息
     */
    private fun loadMonthlyStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val statistics = getScheduleStatisticsUseCase.getMonthlyStatistics(_currentYearMonth.value)
                _monthlyStatistics.value = statistics
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "加载统计信息失败：${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    // 已移除：快速班次选择相关状态与方法
    
    /**
     * 切换视图模式
     */
    fun toggleViewMode() {
        userOverridden = true
        _viewMode.value = when (_viewMode.value) {
            CalendarViewMode.COMFORTABLE -> CalendarViewMode.COMPACT
            CalendarViewMode.COMPACT -> CalendarViewMode.COMFORTABLE
        }
    }
    
    /**
     * 设置视图模式
     */
    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }
    
    /**
     * 删除指定日期的排班
     */
    fun deleteSchedule(date: LocalDate) {
        viewModelScope.launch {
            try {
                // 先获取该日期的排班
                val schedule = schedules.value.find { it.date == date }
                if (schedule != null) {
                    deleteScheduleUseCase(schedule.id)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "删除排班失败：${e.message}"
                )
            }
        }
    }
}

/**
 * 日历UI状态
 */
data class CalendarUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 日历视图模式
 */
enum class CalendarViewMode {
    COMFORTABLE, // 舒适模式：较大的矩形格子，更多显示空间
    COMPACT      // 紧凑模式：紧凑的正方形格子，显示更多日期
}
