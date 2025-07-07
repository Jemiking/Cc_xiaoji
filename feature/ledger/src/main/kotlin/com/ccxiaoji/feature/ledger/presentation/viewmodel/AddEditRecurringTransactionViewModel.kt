package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.model.RecurringFrequency
import com.ccxiaoji.feature.ledger.data.repository.RecurringTransactionRepository
import com.ccxiaoji.shared.user.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditRecurringTransactionUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val amount: String = "",
    val accountId: String = "",
    val categoryId: String = "",
    val note: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val dayOfWeek: Int? = null,
    val dayOfMonth: Int? = 1,
    val monthOfYear: Int? = 1,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val nameError: String? = null,
    val amountError: String? = null,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddEditRecurringTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val userApi: UserApi
) : ViewModel() {
    
    private val recurringId: String? = savedStateHandle.get<String>("recurringId")
    
    private val _uiState = MutableStateFlow(AddEditRecurringTransactionUiState())
    val uiState: StateFlow<AddEditRecurringTransactionUiState> = _uiState.asStateFlow()
    
    init {
        if (recurringId != null) {
            loadRecurringTransaction(recurringId)
        }
    }
    
    private fun loadRecurringTransaction(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val transaction = recurringTransactionRepository.getRecurringTransactionById(id)
                if (transaction != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = transaction.name,
                            amount = (transaction.amountCents / 100.0).toString(),
                            accountId = transaction.accountId,
                            categoryId = transaction.categoryId,
                            note = transaction.note ?: "",
                            frequency = transaction.frequency,
                            dayOfWeek = transaction.dayOfWeek,
                            dayOfMonth = transaction.dayOfMonth,
                            monthOfYear = transaction.monthOfYear,
                            startDate = transaction.startDate,
                            endDate = transaction.endDate
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "定期交易不存在"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载失败：${e.message}"
                    )
                }
            }
        }
    }
    
    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = null
            )
        }
    }
    
    fun updateAmount(amount: String) {
        val filtered = amount.filter { char -> char.isDigit() || char == '.' }
        _uiState.update {
            it.copy(
                amount = filtered,
                amountError = null
            )
        }
    }
    
    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }
    
    fun updateFrequency(frequency: RecurringFrequency) {
        _uiState.update { it.copy(frequency = frequency) }
    }
    
    fun updateDayOfWeek(dayOfWeek: Int?) {
        _uiState.update { it.copy(dayOfWeek = dayOfWeek) }
    }
    
    fun updateDayOfMonth(dayOfMonth: Int?) {
        _uiState.update { it.copy(dayOfMonth = dayOfMonth) }
    }
    
    fun updateMonthOfYear(monthOfYear: Int?) {
        _uiState.update { it.copy(monthOfYear = monthOfYear) }
    }
    
    fun saveRecurringTransaction() {
        viewModelScope.launch {
            // 验证输入
            var hasError = false
            
            if (_uiState.value.name.isBlank()) {
                _uiState.update { it.copy(nameError = "请输入名称") }
                hasError = true
            }
            
            val amountValue = _uiState.value.amount.toDoubleOrNull()
            if (amountValue == null || amountValue <= 0) {
                _uiState.update { it.copy(amountError = "请输入有效金额") }
                hasError = true
            }
            
            if (hasError) return@launch
            
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val currentUser = userApi.getCurrentUser()
                val userId = currentUser?.id ?: "default_user"
                val amountCents = (amountValue!! * 100).toInt()
                
                // TODO: 实际实现中需要提供真实的账户ID和分类ID
                val accountId = _uiState.value.accountId.ifBlank { "default_account" }
                val categoryId = _uiState.value.categoryId.ifBlank { "default_category" }
                
                if (recurringId == null) {
                    // 创建新的定期交易
                    recurringTransactionRepository.createRecurringTransaction(
                        name = _uiState.value.name,
                        accountId = accountId,
                        amountCents = amountCents,
                        categoryId = categoryId,
                        note = _uiState.value.note.ifBlank { null },
                        frequency = _uiState.value.frequency,
                        startDate = _uiState.value.startDate,
                        endDate = _uiState.value.endDate,
                        dayOfWeek = _uiState.value.dayOfWeek,
                        dayOfMonth = _uiState.value.dayOfMonth,
                        monthOfYear = _uiState.value.monthOfYear
                    )
                } else {
                    // 更新现有的定期交易
                    recurringTransactionRepository.updateRecurringTransaction(
                        id = recurringId,
                        name = _uiState.value.name,
                        accountId = accountId,
                        amountCents = amountCents,
                        categoryId = categoryId,
                        note = _uiState.value.note.ifBlank { null },
                        frequency = _uiState.value.frequency,
                        startDate = _uiState.value.startDate,
                        endDate = _uiState.value.endDate,
                        dayOfWeek = _uiState.value.dayOfWeek,
                        dayOfMonth = _uiState.value.dayOfMonth,
                        monthOfYear = _uiState.value.monthOfYear
                    )
                }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "保存失败：${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}