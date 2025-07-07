package com.ccxiaoji.feature.schedule.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import com.ccxiaoji.feature.schedule.domain.usecase.ManageShiftUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class EditShiftViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val manageShiftUseCase: ManageShiftUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val shiftId: Long? = savedStateHandle.get<String>("shiftId")?.toLongOrNull()
    
    data class EditShiftUiState(
        val isLoading: Boolean = false,
        val name: String = "",
        val startTime: LocalTime = LocalTime.of(9, 0),
        val endTime: LocalTime = LocalTime.of(18, 0),
        val selectedColor: Int = Shift.PRESET_COLORS.first(),
        val description: String = "",
        val nameError: String? = null,
        val timeError: String? = null,
        val isSaved: Boolean = false
    )
    
    private val _uiState = MutableStateFlow(EditShiftUiState())
    val uiState: StateFlow<EditShiftUiState> = _uiState.asStateFlow()
    
    init {
        shiftId?.let { loadShift(it) }
    }
    
    private fun loadShift(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                scheduleRepository.getShiftById(id)?.let { shift ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        name = shift.name,
                        startTime = shift.startTime,
                        endTime = shift.endTime,
                        selectedColor = shift.color,
                        description = shift.description ?: ""
                    )
                } ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nameError = "加载班次失败：${e.message}"
                )
            }
        }
    }
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = if (name.isBlank()) "班次名称不能为空" else null
        )
    }
    
    fun updateStartTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(
            startTime = time,
            timeError = validateTimes(time, _uiState.value.endTime)
        )
    }
    
    fun updateEndTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(
            endTime = time,
            timeError = validateTimes(_uiState.value.startTime, time)
        )
    }
    
    fun updateColor(color: Int) {
        _uiState.value = _uiState.value.copy(selectedColor = color)
    }
    
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }
    
    private fun validateTimes(startTime: LocalTime, endTime: LocalTime): String? {
        return if (endTime.isBefore(startTime) || endTime == startTime) {
            "结束时间必须晚于开始时间"
        } else {
            null
        }
    }
    
    fun saveShift() {
        val state = _uiState.value
        
        if (state.name.isBlank()) {
            _uiState.value = state.copy(nameError = "班次名称不能为空")
            return
        }
        
        val timeError = validateTimes(state.startTime, state.endTime)
        if (timeError != null) {
            _uiState.value = state.copy(timeError = timeError)
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            
            try {
                val shift = Shift(
                    id = shiftId ?: 0,
                    name = state.name.trim(),
                    startTime = state.startTime,
                    endTime = state.endTime,
                    color = state.selectedColor,
                    description = state.description.ifBlank { null }
                )
                
                if (shiftId == null) {
                    manageShiftUseCase.createShift(shift)
                } else {
                    manageShiftUseCase.updateShift(shift)
                }
                
                _uiState.value = state.copy(
                    isLoading = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    nameError = "保存失败：${e.message}"
                )
            }
        }
    }
}