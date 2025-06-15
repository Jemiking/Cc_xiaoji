package com.ccxiaoji.feature.schedule.presentation.viewmodel

// AndroidX
import androidx.lifecycle.viewModelScope

// 项目内部模块
import com.ccxiaoji.core.common.base.BaseViewModel
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.usecase.GetShiftsUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.ManageShiftUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.ShiftNameExistsException

// 第三方库
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 班次管理ViewModel
 * 处理班次的增删改查操作
 */
@HiltViewModel
class ShiftViewModel @Inject constructor(
    private val getShiftsUseCase: GetShiftsUseCase,
    private val manageShiftUseCase: ManageShiftUseCase
) : BaseViewModel() {

    // ========== 状态管理 ==========

    // 班次列表
    val shifts: StateFlow<List<Shift>> = getShiftsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI状态
    private val _uiState = MutableStateFlow(ShiftUiState())
    val uiState: StateFlow<ShiftUiState> = _uiState.asStateFlow()

    // 当前编辑的班次
    private val _editingShift = MutableStateFlow<Shift?>(null)
    val editingShift: StateFlow<Shift?> = _editingShift.asStateFlow()

    // ========== 班次对话框管理 ==========

    /**
     * 显示创建班次对话框
     */
    fun showCreateShiftDialog() {
        _editingShift.value = null
        _uiState.update { it.copy(showShiftDialog = true) }
    }

    /**
     * 显示编辑班次对话框
     */
    fun showEditShiftDialog(shift: Shift) {
        _editingShift.value = shift
        _uiState.update { it.copy(showShiftDialog = true) }
    }

    /**
     * 隐藏班次对话框
     */
    fun hideShiftDialog() {
        _editingShift.value = null
        _uiState.update { it.copy(showShiftDialog = false) }
    }

    // ========== 班次操作 ==========

    /**
     * 保存班次（创建或更新）
     */
    fun saveShift(shift: Shift) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                if (shift.id == 0L) {
                    // 创建新班次
                    manageShiftUseCase.createShift(shift)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            showShiftDialog = false,
                            successMessage = "班次创建成功"
                        )
                    }
                } else {
                    // 更新现有班次
                    manageShiftUseCase.updateShift(shift)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            showShiftDialog = false,
                            successMessage = "班次更新成功"
                        )
                    }
                }
                _editingShift.value = null
            } catch (e: ShiftNameExistsException) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "班次名称已存在"
                    )
                }
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
     * 删除班次
     */
    fun deleteShift(shift: Shift) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                manageShiftUseCase.deleteShift(shift.id)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "班次删除成功"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "删除失败：${e.message}"
                    )
                }
            }
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 清除消息
     */
    fun clearMessages() {
        _uiState.update { 
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }
}

/**
 * 班次管理UI状态
 */
data class ShiftUiState(
    val isLoading: Boolean = false,
    val showShiftDialog: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)