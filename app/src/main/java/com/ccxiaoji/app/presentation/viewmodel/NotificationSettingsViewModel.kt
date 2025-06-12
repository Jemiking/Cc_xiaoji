package com.ccxiaoji.app.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    companion object {
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_TASK_DUE_REMINDER = booleanPreferencesKey("task_due_reminder")
        private val KEY_TASK_REMINDER_MINUTES = intPreferencesKey("task_reminder_minutes")
        private val KEY_HABIT_REMINDER = booleanPreferencesKey("habit_reminder")
        private val KEY_HABIT_REMINDER_TIME = stringPreferencesKey("habit_reminder_time")
        private val KEY_BUDGET_ALERTS = booleanPreferencesKey("budget_alerts")
        private val KEY_BUDGET_ALERT_THRESHOLD = intPreferencesKey("budget_alert_threshold")
        private val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        private val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val KEY_DO_NOT_DISTURB_ENABLED = booleanPreferencesKey("do_not_disturb_enabled")
        private val KEY_DO_NOT_DISTURB_START = stringPreferencesKey("do_not_disturb_start")
        private val KEY_DO_NOT_DISTURB_END = stringPreferencesKey("do_not_disturb_end")
    }
    
    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _uiState.update {
                    it.copy(
                        notificationsEnabled = preferences[KEY_NOTIFICATIONS_ENABLED] ?: true,
                        taskDueReminder = preferences[KEY_TASK_DUE_REMINDER] ?: true,
                        taskReminderMinutes = preferences[KEY_TASK_REMINDER_MINUTES] ?: 30,
                        habitReminder = preferences[KEY_HABIT_REMINDER] ?: true,
                        habitReminderTime = preferences[KEY_HABIT_REMINDER_TIME] ?: "20:00",
                        budgetAlerts = preferences[KEY_BUDGET_ALERTS] ?: true,
                        budgetAlertThreshold = preferences[KEY_BUDGET_ALERT_THRESHOLD] ?: 80,
                        vibrationEnabled = preferences[KEY_VIBRATION_ENABLED] ?: true,
                        soundEnabled = preferences[KEY_SOUND_ENABLED] ?: true,
                        doNotDisturbEnabled = preferences[KEY_DO_NOT_DISTURB_ENABLED] ?: false,
                        doNotDisturbStart = preferences[KEY_DO_NOT_DISTURB_START] ?: "22:00",
                        doNotDisturbEnd = preferences[KEY_DO_NOT_DISTURB_END] ?: "08:00"
                    )
                }
            }
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
            }
        }
    }
    
    fun setTaskDueReminder(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_TASK_DUE_REMINDER] = enabled
            }
        }
    }
    
    fun setTaskReminderMinutes(minutes: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_TASK_REMINDER_MINUTES] = minutes
            }
        }
    }
    
    fun setHabitReminder(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_HABIT_REMINDER] = enabled
            }
        }
    }
    
    fun setHabitReminderTime(time: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_HABIT_REMINDER_TIME] = time
            }
        }
    }
    
    fun setBudgetAlerts(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_BUDGET_ALERTS] = enabled
            }
        }
    }
    
    fun setBudgetAlertThreshold(threshold: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_BUDGET_ALERT_THRESHOLD] = threshold
            }
        }
    }
    
    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_VIBRATION_ENABLED] = enabled
            }
        }
    }
    
    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_SOUND_ENABLED] = enabled
            }
        }
    }
    
    fun setDoNotDisturbEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_DO_NOT_DISTURB_ENABLED] = enabled
            }
        }
    }
    
    fun setDoNotDisturbTime(start: String, end: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_DO_NOT_DISTURB_START] = start
                preferences[KEY_DO_NOT_DISTURB_END] = end
            }
        }
    }
    
    fun showTaskReminderTimePicker() {
        // TODO: Show time picker dialog
    }
    
    fun showHabitReminderTimePicker() {
        // TODO: Show time picker dialog
    }
    
    fun showBudgetThresholdPicker() {
        // TODO: Show threshold picker dialog
    }
    
    fun showDoNotDisturbSettings() {
        // TODO: Show do not disturb settings dialog
    }
}

data class NotificationSettingsUiState(
    val notificationsEnabled: Boolean = true,
    val taskDueReminder: Boolean = true,
    val taskReminderMinutes: Int = 30,
    val habitReminder: Boolean = true,
    val habitReminderTime: String = "20:00",
    val budgetAlerts: Boolean = true,
    val budgetAlertThreshold: Int = 80,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val doNotDisturbEnabled: Boolean = false,
    val doNotDisturbStart: String = "22:00",
    val doNotDisturbEnd: String = "08:00"
)