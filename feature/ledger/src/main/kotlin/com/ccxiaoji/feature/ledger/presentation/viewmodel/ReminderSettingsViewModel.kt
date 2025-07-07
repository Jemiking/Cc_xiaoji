package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.ReminderSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderSettingsUiState(
    val enableDailyReminder: Boolean = false,
    val dailyReminderTime: String = "20:00",
    val enableWeekendReminder: Boolean = true,
    val enableMonthEndReminder: Boolean = false,
    val monthEndReminderDays: Int = 2,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReminderSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReminderSettingsUiState())
    val uiState: StateFlow<ReminderSettingsUiState> = _uiState.asStateFlow()
    
    companion object {
        private val ENABLE_DAILY_REMINDER_KEY = booleanPreferencesKey("ledger_enable_daily_reminder")
        private val DAILY_REMINDER_TIME_KEY = stringPreferencesKey("ledger_daily_reminder_time")
        private val ENABLE_WEEKEND_REMINDER_KEY = booleanPreferencesKey("ledger_enable_weekend_reminder")
        private val ENABLE_MONTH_END_REMINDER_KEY = booleanPreferencesKey("ledger_enable_month_end_reminder")
        private val MONTH_END_REMINDER_DAYS_KEY = intPreferencesKey("ledger_month_end_reminder_days")
    }
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                ReminderSettingsUiState(
                    enableDailyReminder = preferences[ENABLE_DAILY_REMINDER_KEY] ?: false,
                    dailyReminderTime = preferences[DAILY_REMINDER_TIME_KEY] ?: "20:00",
                    enableWeekendReminder = preferences[ENABLE_WEEKEND_REMINDER_KEY] ?: true,
                    enableMonthEndReminder = preferences[ENABLE_MONTH_END_REMINDER_KEY] ?: false,
                    monthEndReminderDays = preferences[MONTH_END_REMINDER_DAYS_KEY] ?: 2
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun updateEnableDailyReminder(enabled: Boolean) {
        _uiState.update { it.copy(enableDailyReminder = enabled) }
    }
    
    fun updateDailyReminderTime(time: String) {
        _uiState.update { it.copy(dailyReminderTime = time) }
    }
    
    fun updateEnableWeekendReminder(enabled: Boolean) {
        _uiState.update { it.copy(enableWeekendReminder = enabled) }
    }
    
    fun updateEnableMonthEndReminder(enabled: Boolean) {
        _uiState.update { it.copy(enableMonthEndReminder = enabled) }
    }
    
    fun increaseMonthEndReminderDays() {
        val currentDays = _uiState.value.monthEndReminderDays
        if (currentDays < 7) {
            _uiState.update { it.copy(monthEndReminderDays = currentDays + 1) }
        }
    }
    
    fun decreaseMonthEndReminderDays() {
        val currentDays = _uiState.value.monthEndReminderDays
        if (currentDays > 1) {
            _uiState.update { it.copy(monthEndReminderDays = currentDays - 1) }
        }
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            val state = _uiState.value
            dataStore.edit { preferences ->
                preferences[ENABLE_DAILY_REMINDER_KEY] = state.enableDailyReminder
                preferences[DAILY_REMINDER_TIME_KEY] = state.dailyReminderTime
                preferences[ENABLE_WEEKEND_REMINDER_KEY] = state.enableWeekendReminder
                preferences[ENABLE_MONTH_END_REMINDER_KEY] = state.enableMonthEndReminder
                preferences[MONTH_END_REMINDER_DAYS_KEY] = state.monthEndReminderDays
            }
        }
    }
}