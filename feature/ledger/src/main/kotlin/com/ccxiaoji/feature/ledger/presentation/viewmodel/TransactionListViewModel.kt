package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.common.base.BaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionListUiState(
    val items: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val offset: Int = 0,
    val pageSize: Int = 20,
    val totalCount: Int? = null,
    val endReached: Boolean = false,
    val accountId: String? = null,
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    fun setFilters(accountId: String? = null, startDateMillis: Long? = null, endDateMillis: Long? = null) {
        _uiState.value = _uiState.value.copy(
            accountId = accountId,
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            items = emptyList(),
            offset = 0,
            endReached = false,
            totalCount = null,
            error = null
        )
    }

    fun loadInitial() {
        _uiState.value = _uiState.value.copy(items = emptyList(), offset = 0, endReached = false, totalCount = null)
        loadPage(reset = true)
    }

    fun refresh() {
        loadInitial()
    }

    fun loadNextPage() {
        if (_uiState.value.isLoading || _uiState.value.endReached) return
        loadPage(reset = false)
    }

    private fun loadPage(reset: Boolean) {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val offset = if (reset) 0 else state.offset
                val limit = state.pageSize
                repository.getTransactionsPaginated(
                    offset = offset,
                    limit = limit,
                    accountId = state.accountId,
                    startDate = state.startDateMillis,
                    endDate = state.endDateMillis
                ).collect { result ->
                    when (result) {
                        is BaseResult.Success -> {
                            val (pageItems, total) = result.data
                            val newList = if (reset) pageItems else state.items + pageItems
                            val newOffset = offset + pageItems.size
                            val endReached = total <= newOffset || pageItems.isEmpty()
                            _uiState.value = _uiState.value.copy(
                                items = newList,
                                isLoading = false,
                                totalCount = total,
                                offset = newOffset,
                                endReached = endReached
                            )
                        }
                        is BaseResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.exception.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}

