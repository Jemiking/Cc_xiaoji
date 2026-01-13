package com.ccxiaoji.feature.schedule.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.usecase.CreateScheduleUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.DeleteScheduleUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.GetActiveShiftsUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.GetMonthScheduleUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.GetScheduleStatisticsUseCase
import com.ccxiaoji.feature.schedule.presentation.theme.ThemeManager
import com.ccxiaoji.feature.schedule.presentation.uikit.DayCellSize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
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
 *
 * Phase 2a 重构：采用轻量 MVI 架构
 * - CalendarUiState: 统一状态容器
 * - CalendarIntent: 用户意图
 * - CalendarEffect: 一次性副作用
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getMonthScheduleUseCase: GetMonthScheduleUseCase,
    private val getScheduleStatisticsUseCase: GetScheduleStatisticsUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
    private val getActiveShiftsUseCase: GetActiveShiftsUseCase,
    private val createScheduleUseCase: CreateScheduleUseCase,
    private val themeManager: ThemeManager
) : ViewModel() {

    // ==================== Effect 通道 ====================
    private val _effect = Channel<CalendarEffect>(Channel.BUFFERED)
    val effect: Flow<CalendarEffect> = _effect.receiveAsFlow()

    // ==================== 内部状态 ====================
    
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

    // 可用班次列表 (用于 ShiftDock 快速选择)
    val shifts: StateFlow<List<Shift>> = getActiveShiftsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ShiftDock 可见性状态
    private val _isShiftDockVisible = MutableStateFlow(false)
    val isShiftDockVisible: StateFlow<Boolean> = _isShiftDockVisible.asStateFlow()

    // ShiftDock 当前选中的班次 ID (null 表示"休息")
    private val _selectedShiftId = MutableStateFlow<Long?>(null)
    val selectedShiftId: StateFlow<Long?> = _selectedShiftId.asStateFlow()
    
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

    // ==================== MVI Intent 处理 ====================

    /**
     * 处理用户意图（MVI 模式入口）
     */
    fun onIntent(intent: CalendarIntent) {
        when (intent) {
            is CalendarIntent.SelectDate -> selectDate(intent.date)
            is CalendarIntent.NavigateMonth -> navigateMonth(intent.direction)
            is CalendarIntent.JumpToMonth -> navigateToYearMonth(intent.yearMonth)
            CalendarIntent.ToggleViewMode -> toggleViewMode()
            CalendarIntent.NavigateToToday -> navigateToToday()
            is CalendarIntent.DeleteSchedule -> deleteSchedule(intent.date)
            is CalendarIntent.RequestEdit -> emitEffect(CalendarEffect.NavigateToEdit(intent.date))
            CalendarIntent.ClearError -> clearError()
            // ShiftDock 交互
            is CalendarIntent.ShowShiftDock -> showShiftDock()
            is CalendarIntent.HideShiftDock -> hideShiftDock()
            is CalendarIntent.SelectShiftInDock -> selectShiftInDock(intent.shiftId)
            is CalendarIntent.ApplyShiftToDate -> applyShiftToDate(intent.date, intent.shiftId)
            is CalendarIntent.DateLongPressed -> onDateLongPressed(intent.date)
        }
    }

    private fun emitEffect(effect: CalendarEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun navigateMonth(direction: MonthDirection) {
        when (direction) {
            MonthDirection.PREVIOUS -> navigateToPreviousMonth()
            MonthDirection.NEXT -> navigateToNextMonth()
        }
    }

    // ==================== 公开方法（保留向后兼容）====================
    
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

    // ==================== ShiftDock 交互方法 ====================

    /**
     * 显示 ShiftDock
     */
    fun showShiftDock() {
        _isShiftDockVisible.value = true
    }

    /**
     * 隐藏 ShiftDock
     */
    fun hideShiftDock() {
        _isShiftDockVisible.value = false
        _selectedShiftId.value = null
    }

    /**
     * 在 ShiftDock 中选择班次
     */
    private fun selectShiftInDock(shiftId: Long?) {
        _selectedShiftId.value = shiftId
        // 如果有选中日期，立即应用班次
        _selectedDate.value?.let { date ->
            applyShiftToDate(date, shiftId)
        }
    }

    /**
     * 长按日期时的处理
     */
    private fun onDateLongPressed(date: LocalDate) {
        _selectedDate.value = date
        showShiftDock()
        emitEffect(CalendarEffect.TriggerHapticFeedback)
    }

    /**
     * 将班次应用到指定日期
     */
    private fun applyShiftToDate(date: LocalDate, shiftId: Long?) {
        viewModelScope.launch {
            try {
                if (shiftId == null) {
                    // 选择"休息"，删除该日期的排班
                    val existingSchedule = schedules.value.find { it.date == date }
                    if (existingSchedule != null) {
                        deleteScheduleUseCase(existingSchedule.id)
                        emitEffect(CalendarEffect.ShowSnackbar("已设为休息"))
                    }
                } else {
                    // 应用班次
                    val shift = shifts.value.find { it.id == shiftId }
                    if (shift != null) {
                        val existingSchedule = schedules.value.find { it.date == date }
                        val schedule = Schedule(
                            id = existingSchedule?.id ?: 0L,
                            date = date,
                            shift = shift
                        )
                        createScheduleUseCase.createOrUpdateSchedule(schedule)
                        emitEffect(CalendarEffect.ShowSnackbar("已设为${shift.name}"))
                    }
                }
                emitEffect(CalendarEffect.TriggerHapticFeedback)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "设置班次失败：${e.message}") }
            }
        }
    }
    
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
 * 日历UI状态（扩展版 - Phase 2a）
 *
 * 注意：当前仅包含 isLoading 和 errorMessage 用于过渡期兼容。
 * 未来可扩展为完整的统一状态容器。
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
    COMPACT;     // 紧凑模式：紧凑的正方形格子，显示更多日期

    /**
     * 转换为 UI Kit 的 DayCellSize
     */
    fun toDayCellSize(): DayCellSize = when (this) {
        COMFORTABLE -> DayCellSize.Comfortable
        COMPACT -> DayCellSize.Compact
    }
}

/**
 * 月份导航方向
 */
enum class MonthDirection {
    PREVIOUS,
    NEXT
}

/**
 * 日历用户意图（MVI Intent）
 *
 * 封装所有用户操作，通过 ViewModel.onIntent() 统一处理。
 */
sealed interface CalendarIntent {
    /** 选择日期 */
    data class SelectDate(val date: LocalDate) : CalendarIntent

    /** 导航月份 */
    data class NavigateMonth(val direction: MonthDirection) : CalendarIntent

    /** 跳转到指定年月 */
    data class JumpToMonth(val yearMonth: YearMonth) : CalendarIntent

    /** 切换视图模式 */
    data object ToggleViewMode : CalendarIntent

    /** 导航到今天 */
    data object NavigateToToday : CalendarIntent

    /** 删除排班 */
    data class DeleteSchedule(val date: LocalDate) : CalendarIntent

    /** 请求编辑排班 */
    data class RequestEdit(val date: LocalDate) : CalendarIntent

    /** 清除错误 */
    data object ClearError : CalendarIntent

    // ==================== ShiftDock 相关 Intent ====================

    /** 显示 ShiftDock */
    data object ShowShiftDock : CalendarIntent

    /** 隐藏 ShiftDock */
    data object HideShiftDock : CalendarIntent

    /** 在 ShiftDock 中选择班次 */
    data class SelectShiftInDock(val shiftId: Long?) : CalendarIntent

    /** 将班次应用到日期 */
    data class ApplyShiftToDate(val date: LocalDate, val shiftId: Long?) : CalendarIntent

    /** 日期长按 */
    data class DateLongPressed(val date: LocalDate) : CalendarIntent
}

/**
 * 日历一次性副作用（MVI Effect）
 *
 * 用于处理导航、弹窗等一次性事件，避免状态重复消费。
 */
sealed interface CalendarEffect {
    /** 显示 Snackbar 提示 */
    data class ShowSnackbar(val message: String) : CalendarEffect

    /** 导航到编辑页面 */
    data class NavigateToEdit(val date: LocalDate) : CalendarEffect

    /** 触发震动反馈 */
    data object TriggerHapticFeedback : CalendarEffect
}
