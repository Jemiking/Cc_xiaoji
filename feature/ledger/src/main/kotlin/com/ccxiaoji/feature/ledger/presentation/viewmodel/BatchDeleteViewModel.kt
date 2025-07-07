package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class BatchDeleteUiState(
    val selectedCount: Int = 0,
    val isDeleting: Boolean = false
)

@HiltViewModel
class BatchDeleteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BatchDeleteUiState())
    val uiState: StateFlow<BatchDeleteUiState> = _uiState.asStateFlow()
    
    init {
        // 获取传递过来的选中数量
        val selectedCount = savedStateHandle.get<Int>("selectedCount") ?: 0
        _uiState.update { it.copy(selectedCount = selectedCount) }
    }
}