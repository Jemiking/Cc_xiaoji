package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.HomeDisplaySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeDisplaySettingsUiState(
    val showTodayExpense: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeDisplaySettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeDisplaySettingsUiState())
    val uiState: StateFlow<HomeDisplaySettingsUiState> = _uiState.asStateFlow()
    
    companion object {
        private val SHOW_TODAY_EXPENSE_KEY = booleanPreferencesKey("ledger_show_today_expense")
    }
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                HomeDisplaySettingsUiState(
                    showTodayExpense = preferences[SHOW_TODAY_EXPENSE_KEY] ?: true
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun updateShowTodayExpense(show: Boolean) {
        _uiState.update { it.copy(showTodayExpense = show) }
    }
    
    
    
    fun saveSettings() {
        viewModelScope.launch {
            val state = _uiState.value
            dataStore.edit { preferences ->
                preferences[SHOW_TODAY_EXPENSE_KEY] = state.showTodayExpense
            }
        }
    }
}