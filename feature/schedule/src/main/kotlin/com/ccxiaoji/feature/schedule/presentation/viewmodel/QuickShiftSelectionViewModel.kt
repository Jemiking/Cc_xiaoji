package com.ccxiaoji.feature.schedule.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import com.ccxiaoji.feature.schedule.domain.usecase.GetQuickShiftsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 快速班次选择页面ViewModel
 */
@HiltViewModel
class QuickShiftSelectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scheduleRepository: ScheduleRepository,
    private val getQuickShiftsUseCase: GetQuickShiftsUseCase
) : ViewModel() {
    
    // 从导航参数获取选中的日期
    private val selectedDate: LocalDate = LocalDate.parse(
        checkNotNull(savedStateHandle.get<String>("selectedDate"))
    )
    
    private val _uiState = MutableStateFlow(QuickShiftSelectionUiState())
    val uiState: StateFlow<QuickShiftSelectionUiState> = _uiState.asStateFlow()
    
    init {
        loadQuickShifts()
        loadCurrentSchedule()
    }
    
    /**
     * 加载常用班次
     */
    private fun loadQuickShifts() {
        viewModelScope.launch {
            getQuickShiftsUseCase().collect { shifts ->
                _uiState.value = _uiState.value.copy(
                    quickShifts = shifts,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * 加载当前日期的排班
     */
    private fun loadCurrentSchedule() {
        viewModelScope.launch {
            scheduleRepository.getScheduleByDate(selectedDate).collect { schedule ->
                _uiState.value = _uiState.value.copy(
                    currentShiftId = schedule?.shift?.id
                )
            }
        }
    }
}

/**
 * 快速班次选择页面UI状态
 */
data class QuickShiftSelectionUiState(
    val quickShifts: List<Shift> = emptyList(),
    val currentShiftId: Long? = null,
    val isLoading: Boolean = true
)