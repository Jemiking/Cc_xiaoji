package com.ccxiaoji.feature.schedule.presentation.viewmodel

// Android/AndroidX
import androidx.lifecycle.viewModelScope

// 项目内部 - Core模块
import com.ccxiaoji.core.common.base.BaseViewModel

// 项目内部 - Domain层
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.usecase.CreateScheduleUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.DeleteScheduleUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.GetMonthScheduleUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.GetQuickShiftsUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.GetScheduleStatisticsUseCase

// 项目内部 - Presentation层
import com.ccxiaoji.feature.schedule.presentation.ui.calendar.CalendarUiState
import com.ccxiaoji.feature.schedule.presentation.ui.calendar.CalendarViewMode

// 第三方库
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Java/Kotlin标准库
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * 日历视图的ViewModel
 * 管理排班日历的状态和业务逻辑
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getMonthScheduleUseCase: GetMonthScheduleUseCase,
    private val getScheduleStatisticsUseCase: GetScheduleStatisticsUseCase,
    private val getQuickShiftsUseCase: GetQuickShiftsUseCase,
    private val createScheduleUseCase: CreateScheduleUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase
) : BaseViewModel() {
    
    // ========== 日期和时间相关状态 ==========
    // 当前显示的年月
    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()
    
    // 选中的日期
    private val _selectedDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()
    
    // 一周开始日
    private val _weekStartDay = MutableStateFlow(DayOfWeek.MONDAY)
    val weekStartDay: StateFlow<DayOfWeek> = _weekStartDay.asStateFlow()
    
    // ========== 排班数据相关状态 ==========
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
    
    // 快速班次列表
    val quickShifts: StateFlow<List<Shift>> = getQuickShiftsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // ========== UI交互相关状态 ==========
    // 快速选择对话框的日期
    private val _quickSelectDate = MutableStateFlow<LocalDate?>(null)
    val quickSelectDate: StateFlow<LocalDate?> = _quickSelectDate.asStateFlow()
    
    // 视图模式状态
    private val _viewMode = MutableStateFlow(CalendarViewMode.COMPACT)
    val viewMode: StateFlow<CalendarViewMode> = _viewMode.asStateFlow()
    
    // UI状态
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()
    
    init {
        // 延迟加载初始统计信息，确保所有属性都已初始化
        viewModelScope.launch {
            loadMonthlyStatistics()
        }
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
    
    /**
     * 显示快速选择对话框
     */
    fun showQuickSelector(date: LocalDate) {
        _quickSelectDate.value = date
    }
    
    /**
     * 隐藏快速选择对话框
     */
    fun hideQuickSelector() {
        _quickSelectDate.value = null
    }
    
    /**
     * 快速设置排班
     */
    fun quickSetSchedule(date: LocalDate, shift: Shift?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                if (shift != null) {
                    // 创建新排班
                    val schedule = Schedule(
                        id = 0,
                        date = date,
                        shift = shift,
                        note = ""
                    )
                    createScheduleUseCase.createOrUpdateSchedule(schedule)
                } else {
                    // 删除排班
                    createScheduleUseCase.deleteSchedule(date)
                }
                
                // 隐藏快速选择对话框
                hideQuickSelector()
                
                // 刷新统计信息
                loadMonthlyStatistics()
                
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "操作失败：${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 切换视图模式
     */
    fun toggleViewMode() {
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
    
    /**
     * 设置一周开始日
     */
    fun setWeekStartDay(dayOfWeek: DayOfWeek) {
        _weekStartDay.value = dayOfWeek
    }
}