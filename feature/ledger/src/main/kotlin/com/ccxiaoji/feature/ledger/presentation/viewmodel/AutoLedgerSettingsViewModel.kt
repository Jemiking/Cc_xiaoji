package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.repository.AutoLedgerSettingsRepository
import com.ccxiaoji.feature.ledger.data.manager.DeduplicationManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.ccxiaoji.feature.ledger.domain.usecase.AutoLedgerManager
import com.ccxiaoji.shared.notification.api.NotificationEventRepository
import com.ccxiaoji.shared.notification.api.NotificationAccessController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// 自动模式（顶层声明，便于 UiState 与 UI 文件直接引用）
enum class AutoMode { SEMI, ALIPAY_AUTO, GEN_AUTO }

data class AutoLedgerSettingsUiState(
    val globalEnabled: Boolean = false,
    val notificationListenerEnabled: Boolean = false,
    // 开发者模式（Release 由 DataStore 控制；Debug 恒为 true）
    val developerModeEnabled: Boolean = false,
    val emitWithoutKeywords: Boolean = true,
    val emitGroupSummary: Boolean = false,
    val logUnmatchedNotifications: Boolean = false,
    val autoCreateEnabled: Boolean = true,
    val autoCreateConfidenceThreshold: Float = 0.85f,
    val minAmountCents: Int = 20,
    // 监听健康统计
    val listenerConnected: Boolean = false,
    val listenerConnectCount: Int = 0,
    val listenerDisconnectCount: Int = 0,
    val listenerTotalConnectedMs: Long = 0,
    val dedupEnabled: Boolean = true,
    val dedupWindowSec: Int = 20,
    val dedupDebugParseOnSkip: Boolean = false,
    // 模式与方案A（支付宝）
    val selectedMode: AutoMode = AutoMode.SEMI,
    val alipayAutoOn: Boolean = false,
    val alipayDefaultAccountId: String? = null,
    val defaultExpenseCategoryId: String? = null,
    val defaultIncomeCategoryId: String? = null,
    // 供选择的数据
    val accounts: List<com.ccxiaoji.feature.ledger.presentation.quickadd.AccountOption> = emptyList(),
    val expenseCategories: List<com.ccxiaoji.feature.ledger.presentation.quickadd.CategoryOption> = emptyList(),
    val incomeCategories: List<com.ccxiaoji.feature.ledger.presentation.quickadd.CategoryOption> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val debugRecentTx: List<String> = emptyList()
)

@HiltViewModel
class AutoLedgerSettingsViewModel @Inject constructor(
    private val settingsRepository: AutoLedgerSettingsRepository,
    private val autoLedgerManager: AutoLedgerManager,
    private val notificationEventRepository: NotificationEventRepository,
    private val notificationAccessController: NotificationAccessController,
    private val dataStore: DataStore<Preferences>,
    private val deduplicationManager: DeduplicationManager,
    private val transactionDao: com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao,
    private val accountDao: com.ccxiaoji.feature.ledger.data.local.dao.AccountDao,
    private val categoryDao: com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao,
    private val userApi: com.ccxiaoji.shared.user.api.UserApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AutoLedgerSettingsUiState())
    val uiState: StateFlow<AutoLedgerSettingsUiState> = _uiState.asStateFlow()

    init {
        // 订阅开发者模式（Release 使用；Debug 变体始终视为已开启）
        viewModelScope.launch {
            val KEY = booleanPreferencesKey("developer_mode_enabled")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(developerModeEnabled = prefs[KEY] ?: false)
            }
        }

        // 订阅总开关
        viewModelScope.launch {
            settingsRepository.globalEnabled().collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(globalEnabled = enabled, loading = false)
            }
        }
        // 监听层诊断（含连接健康）
        viewModelScope.launch {
            notificationAccessController.diagnostics().collectLatest { d ->
                _uiState.value = _uiState.value.copy(
                    notificationListenerEnabled = d.isConnected,
                    listenerConnected = d.isConnected,
                    listenerConnectCount = d.connectCount,
                    listenerDisconnectCount = d.disconnectCount,
                    listenerTotalConnectedMs = d.totalConnectedMs
                )
            }
        }

        // 订阅监听层透传配置
        viewModelScope.launch {
            val KEY = booleanPreferencesKey("auto_ledger_emit_without_keywords")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(emitWithoutKeywords = prefs[KEY] ?: true)
            }
        }

        // 订阅群组摘要透传配置
        viewModelScope.launch {
            val KEY = booleanPreferencesKey("auto_ledger_emit_group_summary")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(emitGroupSummary = prefs[KEY] ?: false)
            }
        }

        // 订阅未匹配通知日志开关
        viewModelScope.launch {
            val KEY = booleanPreferencesKey("auto_ledger_log_unmatched")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(logUnmatchedNotifications = prefs[KEY] ?: false)
            }
        }

        // 订阅去重开关
        viewModelScope.launch {
            val KEY = booleanPreferencesKey("auto_ledger_dedup_enabled")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(dedupEnabled = prefs[KEY] ?: true)
            }
        }

        // 订阅去重窗口（秒）
        viewModelScope.launch {
            val KEY = androidx.datastore.preferences.core.intPreferencesKey("auto_ledger_dedup_window_sec")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(dedupWindowSec = prefs[KEY] ?: 20)
            }
        }

        // 订阅去重调试：去重跳过时仍解析
        viewModelScope.launch {
            val KEY = booleanPreferencesKey("auto_ledger_dedup_debug_parse_on_skip")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(dedupDebugParseOnSkip = prefs[KEY] ?: false)
            }
        }

        // 订阅自动创建开关（默认关闭）
        viewModelScope.launch {
            val KEY = booleanPreferencesKey("auto_ledger_autocreate_enabled")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(autoCreateEnabled = prefs[KEY] ?: false)
                recomputeSelectedMode()
            }
        }

        // 订阅自动创建阈值（0.5~0.95）
        viewModelScope.launch {
            val KEY = androidx.datastore.preferences.core.floatPreferencesKey("auto_ledger_autocreate_confidence_threshold")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(autoCreateConfidenceThreshold = prefs[KEY] ?: 0.85f)
            }
        }

        // 订阅最小金额阈值（单位：分）
        viewModelScope.launch {
            val KEY = androidx.datastore.preferences.core.intPreferencesKey("auto_ledger_min_amount_cents")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(minAmountCents = prefs[KEY] ?: 20)
            }
        }

        // 订阅方案A（支付宝自动入账）相关键位
        viewModelScope.launch {
            val KEY = booleanPreferencesKey("auto_ledger_alipay_auto_on")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(alipayAutoOn = prefs[KEY] ?: false)
                recomputeSelectedMode()
            }
        }
        viewModelScope.launch {
            val KEY = androidx.datastore.preferences.core.stringPreferencesKey("auto_ledger_alipay_default_account_id")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(alipayDefaultAccountId = prefs[KEY])
            }
        }
        viewModelScope.launch {
            val KEY = androidx.datastore.preferences.core.stringPreferencesKey("auto_ledger_default_expense_category_id")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(defaultExpenseCategoryId = prefs[KEY])
            }
        }
        viewModelScope.launch {
            val KEY = androidx.datastore.preferences.core.stringPreferencesKey("auto_ledger_default_income_category_id")
            dataStore.data.collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(defaultIncomeCategoryId = prefs[KEY])
            }
        }

        // 订阅账户/分类选项（供方案A选择默认项）
        viewModelScope.launch {
            val userId = userApi.getCurrentUserId()
            accountDao.getAccountsByUser(userId).collectLatest { list ->
                val opts = list.map { com.ccxiaoji.feature.ledger.presentation.quickadd.AccountOption(it.id, it.name ?: it.id) }
                _uiState.value = _uiState.value.copy(accounts = opts)
            }
        }
        viewModelScope.launch {
            val userId = userApi.getCurrentUserId()
            categoryDao.getCategoriesByType(userId, "EXPENSE").collectLatest { list ->
                val opts = list.map { com.ccxiaoji.feature.ledger.presentation.quickadd.CategoryOption(it.id, it.name) }
                _uiState.value = _uiState.value.copy(expenseCategories = opts)
            }
        }
        viewModelScope.launch {
            val userId = userApi.getCurrentUserId()
            categoryDao.getCategoriesByType(userId, "INCOME").collectLatest { list ->
                val opts = list.map { com.ccxiaoji.feature.ledger.presentation.quickadd.CategoryOption(it.id, it.name) }
                _uiState.value = _uiState.value.copy(incomeCategories = opts)
            }
        }
    }

    fun requestRebind() {
        viewModelScope.launch {
            try {
                val ok = notificationAccessController.requestRebind()
                if (!ok) {
                    _uiState.value = _uiState.value.copy(error = "未授权或系统拒绝重连，请点击‘去授权’")
                } else {
                    _uiState.value = _uiState.value.copy(error = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun openNotificationAccessSettings() {
        viewModelScope.launch {
            try { notificationAccessController.openNotificationAccessSettings() } catch (_: Exception) {}
        }
    }

    fun openPromptChannelSettings() {
        viewModelScope.launch {
            try { notificationAccessController.openChannelSettings("auto_ledger_prompt") } catch (_: Exception) {}
        }
    }

    fun openStatusChannelSettings() {
        viewModelScope.launch {
            try { notificationAccessController.openChannelSettings("auto_ledger_status") } catch (_: Exception) {}
        }
    }

    fun toggleGlobalEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setGlobalEnabled(enabled)
                if (enabled) {
                    autoLedgerManager.start()
                } else {
                    autoLedgerManager.stop()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleEmitWithoutKeywords(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val KEY = booleanPreferencesKey("auto_ledger_emit_without_keywords")
                dataStore.edit { it[KEY] = enabled }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleEmitGroupSummary(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val KEY = booleanPreferencesKey("auto_ledger_emit_group_summary")
                dataStore.edit { it[KEY] = enabled }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleLogUnmatched(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val KEY = booleanPreferencesKey("auto_ledger_log_unmatched")
                dataStore.edit { it[KEY] = enabled }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * 切换开发者模式（仅 Release 生效；Debug 下忽略写入并默认开启）
     */
    fun toggleDeveloperMode() {
        viewModelScope.launch {
            try {
                val KEY = booleanPreferencesKey("developer_mode_enabled")
                val current = uiState.value.developerModeEnabled
                dataStore.edit { it[KEY] = !current }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * 恢复调试相关设置为推荐安全默认值
     */
    fun resetDeveloperSettings() {
        viewModelScope.launch {
            try {
                dataStore.edit { it ->
                    // 监听层过滤/日志
                    it[booleanPreferencesKey("auto_ledger_emit_without_keywords")] = true
                    it[booleanPreferencesKey("auto_ledger_emit_group_summary")] = false
                    it[booleanPreferencesKey("auto_ledger_log_unmatched")] = false
                    // 自动创建参数
                    it[androidx.datastore.preferences.core.floatPreferencesKey("auto_ledger_autocreate_confidence_threshold")] = 0.85f
                    it[androidx.datastore.preferences.core.intPreferencesKey("auto_ledger_min_amount_cents")] = 20
                    // 去重
                    it[booleanPreferencesKey("auto_ledger_dedup_enabled")] = true
                    it[androidx.datastore.preferences.core.intPreferencesKey("auto_ledger_dedup_window_sec")] = 20
                    it[booleanPreferencesKey("auto_ledger_dedup_debug_parse_on_skip")] = false
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleAutoCreate(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val KEY = booleanPreferencesKey("auto_ledger_autocreate_enabled")
                dataStore.edit { it[KEY] = enabled }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun recomputeSelectedMode() {
        val s = _uiState.value
        val mode = when {
            s.alipayAutoOn -> AutoMode.ALIPAY_AUTO
            s.autoCreateEnabled -> AutoMode.GEN_AUTO
            else -> AutoMode.SEMI
        }
        _uiState.value = s.copy(selectedMode = mode)
    }

    fun setSelectedMode(mode: AutoMode) {
        viewModelScope.launch {
            try {
                when (mode) {
                    AutoMode.SEMI -> {
                        // 关闭自动创建与支付宝自动
                        dataStore.edit {
                            it[booleanPreferencesKey("auto_ledger_autocreate_enabled")] = false
                            it[booleanPreferencesKey("auto_ledger_alipay_auto_on")] = false
                        }
                    }
                    AutoMode.ALIPAY_AUTO -> {
                        dataStore.edit {
                            it[booleanPreferencesKey("auto_ledger_alipay_auto_on")] = true
                            it[booleanPreferencesKey("auto_ledger_autocreate_enabled")] = false
                        }
                    }
                    AutoMode.GEN_AUTO -> {
                        dataStore.edit {
                            it[booleanPreferencesKey("auto_ledger_autocreate_enabled")] = true
                            it[booleanPreferencesKey("auto_ledger_alipay_auto_on")] = false
                        }
                    }
                }
                recomputeSelectedMode()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleAlipayAuto(on: Boolean) {
        viewModelScope.launch {
            try {
                dataStore.edit { it[booleanPreferencesKey("auto_ledger_alipay_auto_on")] = on }
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun setAlipayDefaultAccount(id: String) {
        viewModelScope.launch {
            try {
                dataStore.edit { it[androidx.datastore.preferences.core.stringPreferencesKey("auto_ledger_alipay_default_account_id")] = id }
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun setDefaultExpenseCategory(id: String) {
        viewModelScope.launch {
            try {
                dataStore.edit { it[androidx.datastore.preferences.core.stringPreferencesKey("auto_ledger_default_expense_category_id")] = id }
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun setDefaultIncomeCategory(id: String) {
        viewModelScope.launch {
            try {
                dataStore.edit { it[androidx.datastore.preferences.core.stringPreferencesKey("auto_ledger_default_income_category_id")] = id }
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun updateAutoCreateThreshold(value: Float) {
        viewModelScope.launch {
            try {
                val KEY = androidx.datastore.preferences.core.floatPreferencesKey("auto_ledger_autocreate_confidence_threshold")
                val v = value.coerceIn(0.5f, 0.95f)
                dataStore.edit { it[KEY] = v }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateMinAmountCents(value: Int) {
        viewModelScope.launch {
            try {
                val KEY = androidx.datastore.preferences.core.intPreferencesKey("auto_ledger_min_amount_cents")
                val v = value.coerceIn(0, 10_000_000)
                dataStore.edit { it[KEY] = v }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleDedupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val KEY = booleanPreferencesKey("auto_ledger_dedup_enabled")
                dataStore.edit { it[KEY] = enabled }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateDedupWindowSec(value: Int) {
        viewModelScope.launch {
            try {
                val KEY = androidx.datastore.preferences.core.intPreferencesKey("auto_ledger_dedup_window_sec")
                val v = value.coerceIn(1, 600)
                dataStore.edit { it[KEY] = v }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleDedupDebugParse(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val KEY = booleanPreferencesKey("auto_ledger_dedup_debug_parse_on_skip")
                dataStore.edit { it[KEY] = enabled }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearDedupCache(onResult: (Int) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val cleared = deduplicationManager.clearAll()
                onResult(cleared)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun printRecentTransactions(limit: Int = 5) {
        viewModelScope.launch {
            try {
                val list = transactionDao.getLatestTransactions(limit)
                val lines = list.map { t ->
                    "id=${t.id}\n金额=${t.amountCents} 分  账簿=${t.ledgerId}\n账户=${t.accountId} 分类=${t.categoryId} 时间=${t.createdAt}"
                }
                _uiState.value = _uiState.value.copy(debugRecentTx = lines)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearRecentPreview() {
        _uiState.value = _uiState.value.copy(debugRecentTx = emptyList())
    }
}
