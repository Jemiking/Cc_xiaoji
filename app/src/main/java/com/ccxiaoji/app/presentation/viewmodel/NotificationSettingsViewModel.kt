package com.ccxiaoji.app.presentation.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.ccxiaoji.shared.notification.api.NotificationApi
import com.ccxiaoji.feature.todo.domain.usecase.TodoNotificationUseCase
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val notificationApi: NotificationApi,
    private val todoNotificationUseCase: TodoNotificationUseCase,
    private val todoRepository: TodoRepository,
    private val habitRepository: HabitRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "NotificationSettings"

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
        Log.d(TAG, "ViewModel created successfully")
        Log.d(TAG, "NotificationApi: ${notificationApi.javaClass.simpleName}")
        Log.d(TAG, "TodoRepository: ${todoRepository.javaClass.simpleName}")
        Log.d(TAG, "HabitRepository: ${habitRepository.javaClass.simpleName}")
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
            try {
                dataStore.edit { preferences ->
                    preferences[KEY_TASK_DUE_REMINDER] = enabled
                }

                if (enabled) {
                    // 查询所有未完成且有截止时间的任务
                    Log.d(TAG, "Scheduling task reminders via NotificationApi...")
                    val reminderMinutes = dataStore.data.first()[KEY_TASK_REMINDER_MINUTES] ?: 30
                    val advanceMillis = reminderMinutes.toLong() * 60_000L
                    todoRepository.getAllTodos().first()
                        .filter { !it.completed && it.dueAt != null }
                        .forEach { task ->
                            val dueAt = task.dueAt!!
                            val remindAt = Instant.fromEpochMilliseconds(
                                (dueAt.toEpochMilliseconds() - advanceMillis).coerceAtLeast(0L)
                            )
                            Log.d(TAG, "Scheduling reminder for task: ${task.title}, remindAt: $remindAt (dueAt: $dueAt, advance: ${reminderMinutes}m)")
                            todoNotificationUseCase.scheduleTaskReminder(task.id, task.title, remindAt)
                        }
                    Log.d(TAG, "Task reminders scheduled")
                } else {
                    // 取消所有任务提醒（逐条取消）
                    Log.d(TAG, "Cancelling all task reminders via NotificationApi")
                    todoRepository.getAllTodos().first()
                        .filter { it.dueAt != null }
                        .forEach { task ->
                            todoNotificationUseCase.cancelTaskReminder(task.id)
                        }
                    Log.d(TAG, "All task reminders cancelled")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting task due reminder: enabled=$enabled", e)
            }
        }
    }
    
    fun setTaskReminderMinutes(minutes: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_TASK_REMINDER_MINUTES] = minutes
            }

            // 如果提醒已开启，重新安排所有提醒
            val isReminderEnabled = dataStore.data.first()[KEY_TASK_DUE_REMINDER] ?: true
            if (isReminderEnabled) {
                Log.d(TAG, "Task reminder minutes changed to $minutes, rescheduling...")
                setTaskDueReminder(true)
            }
        }
    }
    
    fun setHabitReminder(enabled: Boolean) {
        viewModelScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[KEY_HABIT_REMINDER] = enabled
                }

                if (enabled) {
                    // 获取提醒时间
                    val time = uiState.value.habitReminderTime.split(":")
                    val hour = time[0].toInt()
                    val minute = time[1].toInt()

                    // 查询所有习惯（HabitRepository 会过滤已删除的）
                    Log.d(TAG, "Scheduling habit reminders at $hour:$minute...")
                    habitRepository.getHabits().first()
                        .forEach { habit ->
                            Log.d(TAG, "Scheduling reminder for habit: ${habit.title}")
                            notificationApi.scheduleDailyHabitReminder(
                                habit.id, habit.title, hour, minute
                            )
                        }
                    Log.d(TAG, "Habit reminders scheduled")
                } else {
                    // 取消所有习惯提醒（逐条取消）
                    Log.d(TAG, "Cancelling all habit reminders via NotificationApi")
                    habitRepository.getHabits().first()
                        .forEach { habit ->
                            notificationApi.cancelHabitReminder(habit.id)
                        }
                    Log.d(TAG, "All habit reminders cancelled")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting habit reminder: enabled=$enabled", e)
            }
        }
    }
    
    fun setHabitReminderTime(time: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_HABIT_REMINDER_TIME] = time
            }

            // 如果提醒已开启，重新安排所有提醒
            val isReminderEnabled = dataStore.data.first()[KEY_HABIT_REMINDER] ?: true
            if (isReminderEnabled) {
                Log.d(TAG, "Habit reminder time changed to $time, rescheduling...")
                setHabitReminder(true)
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
        _uiState.update { it.copy(showTaskReminderPicker = true) }
    }
    
    fun hideTaskReminderTimePicker() {
        _uiState.update { it.copy(showTaskReminderPicker = false) }
    }
    
    fun showHabitReminderTimePicker() {
        _uiState.update { it.copy(showHabitReminderPicker = true) }
    }
    
    fun hideHabitReminderTimePicker() {
        _uiState.update { it.copy(showHabitReminderPicker = false) }
    }
    
    fun showBudgetThresholdPicker() {
        _uiState.update { it.copy(showBudgetThresholdPicker = true) }
    }
    
    fun hideBudgetThresholdPicker() {
        _uiState.update { it.copy(showBudgetThresholdPicker = false) }
    }
    
    fun showDoNotDisturbSettings() {
        _uiState.update { it.copy(showDoNotDisturbDialog = true) }
    }
    
    fun hideDoNotDisturbSettings() {
        _uiState.update { it.copy(showDoNotDisturbDialog = false) }
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
    val doNotDisturbEnd: String = "08:00",
    val showTaskReminderPicker: Boolean = false,
    val showHabitReminderPicker: Boolean = false,
    val showBudgetThresholdPicker: Boolean = false,
    val showDoNotDisturbDialog: Boolean = false
)
