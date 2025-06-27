package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.model.RecurringFrequency
import com.ccxiaoji.feature.ledger.data.local.entity.RecurringTransactionEntity
import com.ccxiaoji.feature.ledger.data.repository.RecurringTransactionRepository
import com.ccxiaoji.shared.user.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RecurringTransactionViewModel @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val userApi: UserApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecurringTransactionUiState())
    val uiState: StateFlow<RecurringTransactionUiState> = _uiState.asStateFlow()
    
    val recurringTransactions: StateFlow<List<RecurringTransactionEntity>> = 
        recurringTransactionRepository.getAllRecurringTransactions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    fun showAddDialog() {
        _uiState.update { it.copy(showDialog = true, editingTransaction = null) }
    }
    
    fun showEditDialog(transaction: RecurringTransactionEntity) {
        _uiState.update { 
            it.copy(
                showDialog = true, 
                editingTransaction = transaction,
                name = transaction.name,
                amountCents = transaction.amountCents,
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
    }
    
    fun hideDialog() {
        _uiState.update { 
            it.copy(
                showDialog = false,
                editingTransaction = null,
                name = "",
                amountCents = 0,
                accountId = "",
                categoryId = "",
                note = "",
                frequency = RecurringFrequency.MONTHLY,
                dayOfWeek = null,
                dayOfMonth = 1,
                monthOfYear = 1,
                startDate = System.currentTimeMillis(),
                endDate = null
            )
        }
    }
    
    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }
    
    fun updateAmount(amountCents: Int) {
        _uiState.update { it.copy(amountCents = amountCents) }
    }
    
    fun updateAccountId(accountId: String) {
        _uiState.update { it.copy(accountId = accountId) }
    }
    
    fun updateCategoryId(categoryId: String) {
        _uiState.update { it.copy(categoryId = categoryId) }
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
    
    fun updateStartDate(startDate: Long) {
        _uiState.update { it.copy(startDate = startDate) }
    }
    
    fun updateEndDate(endDate: Long?) {
        _uiState.update { it.copy(endDate = endDate) }
    }
    
    fun saveRecurringTransaction() {
        viewModelScope.launch {
            val state = _uiState.value
            
            if (state.editingTransaction != null) {
                recurringTransactionRepository.updateRecurringTransaction(
                    id = state.editingTransaction.id,
                    name = state.name,
                    accountId = state.accountId,
                    amountCents = state.amountCents,
                    categoryId = state.categoryId,
                    note = state.note.ifBlank { null },
                    frequency = state.frequency,
                    dayOfWeek = state.dayOfWeek,
                    dayOfMonth = state.dayOfMonth,
                    monthOfYear = state.monthOfYear,
                    startDate = state.startDate,
                    endDate = state.endDate
                )
            } else {
                recurringTransactionRepository.createRecurringTransaction(
                    name = state.name,
                    accountId = state.accountId,
                    amountCents = state.amountCents,
                    categoryId = state.categoryId,
                    note = state.note.ifBlank { null },
                    frequency = state.frequency,
                    dayOfWeek = state.dayOfWeek,
                    dayOfMonth = state.dayOfMonth,
                    monthOfYear = state.monthOfYear,
                    startDate = state.startDate,
                    endDate = state.endDate
                )
            }
            
            hideDialog()
        }
    }
    
    fun toggleEnabled(transaction: RecurringTransactionEntity) {
        viewModelScope.launch {
            recurringTransactionRepository.toggleEnabled(transaction.id)
        }
    }
    
    fun deleteRecurringTransaction(transaction: RecurringTransactionEntity) {
        viewModelScope.launch {
            recurringTransactionRepository.deleteRecurringTransaction(transaction.id)
        }
    }
    
    fun formatNextExecutionDate(date: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(date))
    }
    
    fun getFrequencyText(transaction: RecurringTransactionEntity): String {
        return when (transaction.frequency) {
            RecurringFrequency.DAILY -> "每天"
            RecurringFrequency.WEEKLY -> {
                val dayName = when (transaction.dayOfWeek) {
                    Calendar.MONDAY -> "周一"
                    Calendar.TUESDAY -> "周二"
                    Calendar.WEDNESDAY -> "周三"
                    Calendar.THURSDAY -> "周四"
                    Calendar.FRIDAY -> "周五"
                    Calendar.SATURDAY -> "周六"
                    Calendar.SUNDAY -> "周日"
                    else -> ""
                }
                "每周$dayName"
            }
            RecurringFrequency.MONTHLY -> "每月${transaction.dayOfMonth}日"
            RecurringFrequency.YEARLY -> "每年${transaction.monthOfYear}月${transaction.dayOfMonth}日"
        }
    }
}

data class RecurringTransactionUiState(
    val showDialog: Boolean = false,
    val editingTransaction: RecurringTransactionEntity? = null,
    val name: String = "",
    val amountCents: Int = 0,
    val accountId: String = "",
    val categoryId: String = "",
    val note: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val dayOfWeek: Int? = null,
    val dayOfMonth: Int? = 1,
    val monthOfYear: Int? = 1,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null
)