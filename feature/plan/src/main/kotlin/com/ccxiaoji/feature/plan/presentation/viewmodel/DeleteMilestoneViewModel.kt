package com.ccxiaoji.feature.plan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.Milestone
import com.ccxiaoji.feature.plan.domain.usecase.milestone.DeleteMilestoneUseCase
import com.ccxiaoji.feature.plan.domain.usecase.milestone.GetMilestoneByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteMilestoneViewModel @Inject constructor(
    private val getMilestoneByIdUseCase: GetMilestoneByIdUseCase,
    private val deleteMilestoneUseCase: DeleteMilestoneUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DeleteMilestoneUiState())
    val uiState: StateFlow<DeleteMilestoneUiState> = _uiState.asStateFlow()
    
    fun loadMilestone(milestoneId: String) {
        viewModelScope.launch {
            val milestone = getMilestoneByIdUseCase(milestoneId)
            _uiState.value = _uiState.value.copy(
                milestone = milestone,
                milestoneId = milestoneId
            )
        }
    }
    
    fun deleteMilestone() {
        val milestoneId = _uiState.value.milestoneId ?: return
        
        viewModelScope.launch {
            deleteMilestoneUseCase(milestoneId)
        }
    }
}

data class DeleteMilestoneUiState(
    val milestone: Milestone? = null,
    val milestoneId: String? = null
)