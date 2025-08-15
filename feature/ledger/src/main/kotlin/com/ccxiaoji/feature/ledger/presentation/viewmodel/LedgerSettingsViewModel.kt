package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AutomationSettings
import com.ccxiaoji.feature.ledger.domain.model.BasicSettings
import com.ccxiaoji.feature.ledger.domain.model.HomeDisplaySettings
import com.ccxiaoji.feature.ledger.domain.model.LedgerSettings
import com.ccxiaoji.feature.ledger.domain.model.ReminderSettings
import com.ccxiaoji.feature.ledger.domain.model.AdvancedSettings
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 记账设置视图模型
 */
@HiltViewModel
class LedgerSettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val accountRepository: AccountRepository,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(LedgerSettingsUiState())
    val uiState: StateFlow<LedgerSettingsUiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow(LedgerSettings())
    val settings: StateFlow<LedgerSettings> = _settings.asStateFlow()

    init {
        loadSettings()
        loadAccounts()
    }

    /**
     * 加载设置
     */
    fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                LedgerSettings(
                    basicSettings = loadBasicSettings(preferences),
                    advancedSettings = loadAdvancedSettings(preferences),
                    automationSettings = loadAutomationSettings(preferences)
                )
            }.collect { settings ->
                _settings.value = settings
            }
        }
    }

    /**
     * 加载账户列表
     */
    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getAccounts().collect { accounts ->
                _uiState.value = _uiState.value.copy(accounts = accounts)
            }
        }
    }

    /**
     * 更新基础设置
     */
    fun updateBasicSettings(basicSettings: BasicSettings) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(basicSettings = basicSettings)
            saveBasicSettings(basicSettings)
        }
    }

    /**
     * 更新高级设置
     */
    fun updateAdvancedSettings(advancedSettings: AdvancedSettings) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(advancedSettings = advancedSettings)
            saveAdvancedSettings(advancedSettings)
        }
    }

    /**
     * 更新自动化设置
     */
    fun updateAutomationSettings(automationSettings: AutomationSettings) {
        viewModelScope.launch {
            _settings.value = _settings.value.copy(automationSettings = automationSettings)
            saveAutomationSettings(automationSettings)
        }
    }

    /**
     * 从Preferences加载基础设置
     */
    private fun loadBasicSettings(preferences: Preferences): BasicSettings {
        return BasicSettings(
            defaultAccountId = preferences[PreferencesKeys.DEFAULT_ACCOUNT_ID],
            defaultCurrency = preferences[PreferencesKeys.DEFAULT_CURRENCY] ?: "CNY",
            homeDisplaySettings = HomeDisplaySettings(
                showTodayExpense = preferences[PreferencesKeys.SHOW_TODAY_EXPENSE] ?: true,
                showTodayIncome = preferences[PreferencesKeys.SHOW_TODAY_INCOME] ?: true,
                showMonthExpense = preferences[PreferencesKeys.SHOW_MONTH_EXPENSE] ?: true,
                showMonthIncome = preferences[PreferencesKeys.SHOW_MONTH_INCOME] ?: true,
                showAccountBalance = preferences[PreferencesKeys.SHOW_ACCOUNT_BALANCE] ?: true,
                showBudgetProgress = preferences[PreferencesKeys.SHOW_BUDGET_PROGRESS] ?: true,
                showRecentTransactions = preferences[PreferencesKeys.SHOW_RECENT_TRANSACTIONS] ?: true,
                recentTransactionCount = preferences[PreferencesKeys.RECENT_TRANSACTION_COUNT] ?: 5
            ),
            reminderSettings = ReminderSettings(
                enableDailyReminder = preferences[PreferencesKeys.ENABLE_DAILY_REMINDER] ?: false,
                dailyReminderTime = preferences[PreferencesKeys.DAILY_REMINDER_TIME] ?: "20:00",
                enableWeekendReminder = preferences[PreferencesKeys.ENABLE_WEEKEND_REMINDER] ?: true,
                enableMonthEndReminder = preferences[PreferencesKeys.ENABLE_MONTH_END_REMINDER] ?: false,
                monthEndReminderDays = preferences[PreferencesKeys.MONTH_END_REMINDER_DAYS] ?: 2
            )
        )
    }

    /**
     * 从Preferences加载高级设置
     */
    private fun loadAdvancedSettings(preferences: Preferences): AdvancedSettings {
        return AdvancedSettings(
            decimalPlaces = preferences[PreferencesKeys.DECIMAL_PLACES] ?: 2,
            enableCategoryIcons = preferences[PreferencesKeys.ENABLE_CATEGORY_ICONS] ?: true,
            enableAccountIcons = preferences[PreferencesKeys.ENABLE_ACCOUNT_ICONS] ?: true,
            showDeletedRecords = preferences[PreferencesKeys.SHOW_DELETED_RECORDS] ?: false,
            defaultDateSelection = preferences[PreferencesKeys.DEFAULT_DATE_SELECTION] ?: 0
        )
    }

    /**
     * 从Preferences加载自动化设置
     */
    private fun loadAutomationSettings(preferences: Preferences): AutomationSettings {
        return AutomationSettings(
            enableSmartCategorization = preferences[PreferencesKeys.ENABLE_SMART_CATEGORIZATION] ?: false,
            smartCategorizationThreshold = preferences[PreferencesKeys.SMART_CATEGORIZATION_THRESHOLD] ?: 0.8f,
            enableSmartSuggestions = preferences[PreferencesKeys.ENABLE_SMART_SUGGESTIONS] ?: true,
            enableAutoRecurring = preferences[PreferencesKeys.ENABLE_AUTO_RECURRING] ?: true,
            autoCategorizationRules = emptyList() // TODO: 从数据库加载规则
        )
    }

    /**
     * 保存基础设置
     */
    private suspend fun saveBasicSettings(basicSettings: BasicSettings) {
        dataStore.edit { preferences ->
            basicSettings.defaultAccountId?.let {
                preferences[PreferencesKeys.DEFAULT_ACCOUNT_ID] = it
            }
            preferences[PreferencesKeys.DEFAULT_CURRENCY] = basicSettings.defaultCurrency
            
            // 保存首页显示设置
            preferences[PreferencesKeys.SHOW_TODAY_EXPENSE] = basicSettings.homeDisplaySettings.showTodayExpense
            preferences[PreferencesKeys.SHOW_TODAY_INCOME] = basicSettings.homeDisplaySettings.showTodayIncome
            preferences[PreferencesKeys.SHOW_MONTH_EXPENSE] = basicSettings.homeDisplaySettings.showMonthExpense
            preferences[PreferencesKeys.SHOW_MONTH_INCOME] = basicSettings.homeDisplaySettings.showMonthIncome
            preferences[PreferencesKeys.SHOW_ACCOUNT_BALANCE] = basicSettings.homeDisplaySettings.showAccountBalance
            preferences[PreferencesKeys.SHOW_BUDGET_PROGRESS] = basicSettings.homeDisplaySettings.showBudgetProgress
            preferences[PreferencesKeys.SHOW_RECENT_TRANSACTIONS] = basicSettings.homeDisplaySettings.showRecentTransactions
            preferences[PreferencesKeys.RECENT_TRANSACTION_COUNT] = basicSettings.homeDisplaySettings.recentTransactionCount
            
            // 保存提醒设置
            preferences[PreferencesKeys.ENABLE_DAILY_REMINDER] = basicSettings.reminderSettings.enableDailyReminder
            preferences[PreferencesKeys.DAILY_REMINDER_TIME] = basicSettings.reminderSettings.dailyReminderTime
            preferences[PreferencesKeys.ENABLE_WEEKEND_REMINDER] = basicSettings.reminderSettings.enableWeekendReminder
            preferences[PreferencesKeys.ENABLE_MONTH_END_REMINDER] = basicSettings.reminderSettings.enableMonthEndReminder
            preferences[PreferencesKeys.MONTH_END_REMINDER_DAYS] = basicSettings.reminderSettings.monthEndReminderDays
        }
    }

    /**
     * 保存高级设置
     */
    private suspend fun saveAdvancedSettings(advancedSettings: AdvancedSettings) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DECIMAL_PLACES] = advancedSettings.decimalPlaces
            preferences[PreferencesKeys.ENABLE_CATEGORY_ICONS] = advancedSettings.enableCategoryIcons
            preferences[PreferencesKeys.ENABLE_ACCOUNT_ICONS] = advancedSettings.enableAccountIcons
            preferences[PreferencesKeys.SHOW_DELETED_RECORDS] = advancedSettings.showDeletedRecords
            preferences[PreferencesKeys.DEFAULT_DATE_SELECTION] = advancedSettings.defaultDateSelection
        }
    }

    /**
     * 保存自动化设置
     */
    private suspend fun saveAutomationSettings(automationSettings: AutomationSettings) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_SMART_CATEGORIZATION] = automationSettings.enableSmartCategorization
            preferences[PreferencesKeys.SMART_CATEGORIZATION_THRESHOLD] = automationSettings.smartCategorizationThreshold
            preferences[PreferencesKeys.ENABLE_SMART_SUGGESTIONS] = automationSettings.enableSmartSuggestions
            preferences[PreferencesKeys.ENABLE_AUTO_RECURRING] = automationSettings.enableAutoRecurring
        }
    }

    /**
     * 重置所有设置
     */
    fun resetAllSettings() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            _settings.value = LedgerSettings()
        }
    }

    /**
     * DataStore键定义
     */
    private object PreferencesKeys {
        // 基础设置键
        val DEFAULT_ACCOUNT_ID = longPreferencesKey("ledger_default_account_id")
        val DEFAULT_CURRENCY = stringPreferencesKey("ledger_default_currency")
        
        // 首页显示设置键
        val SHOW_TODAY_EXPENSE = booleanPreferencesKey("ledger_show_today_expense")
        val SHOW_TODAY_INCOME = booleanPreferencesKey("ledger_show_today_income")
        val SHOW_MONTH_EXPENSE = booleanPreferencesKey("ledger_show_month_expense")
        val SHOW_MONTH_INCOME = booleanPreferencesKey("ledger_show_month_income")
        val SHOW_ACCOUNT_BALANCE = booleanPreferencesKey("ledger_show_account_balance")
        val SHOW_BUDGET_PROGRESS = booleanPreferencesKey("ledger_show_budget_progress")
        val SHOW_RECENT_TRANSACTIONS = booleanPreferencesKey("ledger_show_recent_transactions")
        val RECENT_TRANSACTION_COUNT = intPreferencesKey("ledger_recent_transaction_count")
        
        // 提醒设置键
        val ENABLE_DAILY_REMINDER = booleanPreferencesKey("ledger_enable_daily_reminder")
        val DAILY_REMINDER_TIME = stringPreferencesKey("ledger_daily_reminder_time")
        val ENABLE_WEEKEND_REMINDER = booleanPreferencesKey("ledger_enable_weekend_reminder")
        val ENABLE_MONTH_END_REMINDER = booleanPreferencesKey("ledger_enable_month_end_reminder")
        val MONTH_END_REMINDER_DAYS = intPreferencesKey("ledger_month_end_reminder_days")
        
        // 高级设置键
        val DECIMAL_PLACES = intPreferencesKey("ledger_decimal_places")
        val ENABLE_CATEGORY_ICONS = booleanPreferencesKey("ledger_enable_category_icons")
        val ENABLE_ACCOUNT_ICONS = booleanPreferencesKey("ledger_enable_account_icons")
        val SHOW_DELETED_RECORDS = booleanPreferencesKey("ledger_show_deleted_records")
        val DEFAULT_DATE_SELECTION = intPreferencesKey("ledger_default_date_selection")
        
        // 自动化设置键
        val ENABLE_SMART_CATEGORIZATION = booleanPreferencesKey("ledger_enable_smart_categorization")
        val SMART_CATEGORIZATION_THRESHOLD = floatPreferencesKey("ledger_smart_categorization_threshold")
        val ENABLE_SMART_SUGGESTIONS = booleanPreferencesKey("ledger_enable_smart_suggestions")
        val ENABLE_AUTO_RECURRING = booleanPreferencesKey("ledger_enable_auto_recurring")
    }
}

/**
 * 记账设置UI状态
 */
data class LedgerSettingsUiState(
    val isLoading: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val error: String? = null
)