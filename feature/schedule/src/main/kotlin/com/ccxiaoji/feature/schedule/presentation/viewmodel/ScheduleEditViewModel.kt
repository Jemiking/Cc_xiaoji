package com.ccxiaoji.feature.schedule.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 排班编辑界面的ViewModel
 */
@HiltViewModel
class ScheduleEditViewModel @Inject constructor(
    private val getActiveShiftsUseCase: GetActiveShiftsUseCase,
    private val getScheduleByDateUseCase: GetScheduleByDateUseCase,
    private val createScheduleUseCase: CreateScheduleUseCase,
    private val updateScheduleUseCase: UpdateScheduleUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase
) : ViewModel() {
    
    // 所有激活的班次列表
    val shifts: StateFlow<List<Shift>> = getActiveShiftsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 当前日期的排班
    private val _currentSchedule = MutableStateFlow<Schedule?>(null)
    val currentSchedule: StateFlow<Schedule?> = _currentSchedule.asStateFlow()
    
    // 选中的班次
    private val _selectedShift = MutableStateFlow<Shift?>(null)
    val selectedShift: StateFlow<Shift?> = _selectedShift.asStateFlow()
    
    // UI状态
    private val _uiState = MutableStateFlow(ScheduleEditUiState())
    val uiState: StateFlow<ScheduleEditUiState> = _uiState.asStateFlow()
    
    /**
     * 加载指定日期的排班信息
     */
    fun loadScheduleForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                getScheduleByDateUseCase(date).collect { schedule ->
                    _currentSchedule.value = schedule
                    _selectedShift.value = schedule?.shift
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载排班信息失败：${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 选择班次
     */
    fun selectShift(shift: Shift?) {
        _selectedShift.value = shift
    }
    
    /**
     * 保存排班
     */
    fun saveSchedule(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val shift = _selectedShift.value
                val currentSchedule = _currentSchedule.value
                
                if (shift == null) {
                    // 如果选择休息（null），且有现有排班，则删除
                    if (currentSchedule != null) {
                        deleteScheduleUseCase(currentSchedule.id)
                    }
                } else {
                    // 如果有现有排班，更新；否则创建新排班
                    if (currentSchedule != null) {
                        val updatedSchedule = currentSchedule.copy(shift = shift)
                        updateScheduleUseCase(updatedSchedule)
                    } else {
                        val newSchedule = Schedule(
                            date = date,
                            shift = shift,
                            note = null
                        )
                        createScheduleUseCase.createOrUpdateSchedule(newSchedule)
                    }
                }
                
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "保存排班失败：${e.message}"
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
}

/**
 * 排班编辑UI状态
 */
data class ScheduleEditUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)