package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.repository.AutoLedgerSettingsRepository
import com.ccxiaoji.feature.ledger.domain.usecase.AutoLedgerManager
import com.ccxiaoji.shared.notification.api.NotificationAccessController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// 两挡模式：半自动 / 全自动（过渡实现）
enum class AutoMode { SEMI, GEN_AUTO }

data class AutoLedgerSettingsUiState(
    val captureNlEnabled: Boolean = true,
    val captureA11yEnabled: Boolean = false,
    val a11yGranted: Boolean = false,
    val globalEnabled: Boolean = false,
    val notificationListenerEnabled: Boolean = false,
    val listenerConnectCount: Int = 0,
    val listenerDisconnectCount: Int = 0,
    val listenerTotalConnectedMs: Long = 0,
    val selectedMode: AutoMode = AutoMode.SEMI,
    // 兼容旧UI所需字段（隐藏但保留，避免编译冲突）
    val developerModeEnabled: Boolean = false,
    // 开发者设置所需兼容字段（默认值为安全推荐）
    val dedupEnabled: Boolean = true,
    val dedupWindowSec: Int = 20,
    val dedupDebugParseOnSkip: Boolean = false,
    val emitWithoutKeywords: Boolean = true,
    val emitGroupSummary: Boolean = false,
    val logUnmatchedNotifications: Boolean = false,
    val autoCreateConfidenceThreshold: Float = 0.85f,
    val minAmountCents: Int = 100,
    val autoCreateEnabled: Boolean = false,
    val alipayAutoOn: Boolean = false,
    val alipayDefaultAccountId: String? = null,
    val defaultExpenseCategoryId: String? = null,
    val defaultIncomeCategoryId: String? = null,
    val alipayAccountSourceIsLast: Boolean = true,
    val alipayCategoryExpenseSourceIsLast: Boolean = true,
    val alipayCategoryIncomeSourceIsLast: Boolean = true,
    val wechatDefaultAccountId: String? = null,
    val wechatExpenseCategoryId: String? = null,
    val wechatIncomeCategoryId: String? = null,
    val wechatAccountSourceIsLast: Boolean = true,
    val wechatCategoryExpenseSourceIsLast: Boolean = true,
    val wechatCategoryIncomeSourceIsLast: Boolean = true,
    val fixedLedgerEnabled: Boolean = false,
    val fixedLedgerId: String? = null,
    val accounts: List<com.ccxiaoji.feature.ledger.presentation.quickadd.AccountOption> = emptyList(),
    val expenseCategories: List<com.ccxiaoji.feature.ledger.presentation.quickadd.CategoryOption> = emptyList(),
    val incomeCategories: List<com.ccxiaoji.feature.ledger.presentation.quickadd.CategoryOption> = emptyList(),
    val ledgers: List<Pair<String, String>> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val debugRecentTx: List<String> = emptyList()
)

@HiltViewModel
class AutoLedgerSettingsViewModel @Inject constructor(
    private val settingsRepository: AutoLedgerSettingsRepository,
    private val autoLedgerManager: AutoLedgerManager,
    private val notificationAccessController: NotificationAccessController,
    private val developerSettingsRepository: com.ccxiaoji.feature.ledger.domain.repository.AutoLedgerDeveloperSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AutoLedgerSettingsUiState())
    val uiState: StateFlow<AutoLedgerSettingsUiState> = _uiState.asStateFlow()

    init {
        // 订阅总开关
        viewModelScope.launch {
            settingsRepository.globalEnabled().collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(globalEnabled = enabled, loading = false)
            }
        }
        // 订阅两挡模式
        viewModelScope.launch {
            settingsRepository.mode().collectLatest { mode ->
                val m = if (mode.equals("FULL", true)) AutoMode.GEN_AUTO else AutoMode.SEMI
                _uiState.value = _uiState.value.copy(
                    selectedMode = m,
                    autoCreateEnabled = (m == AutoMode.GEN_AUTO)
                )
            }
        }
        // 通知监听诊断
        viewModelScope.launch {
            notificationAccessController.diagnostics().collectLatest { d ->
                _uiState.value = _uiState.value.copy(
                    notificationListenerEnabled = d.isConnected,
                    listenerConnectCount = d.connectCount,
                    listenerDisconnectCount = d.disconnectCount,
                    listenerTotalConnectedMs = d.totalConnectedMs
                )
            }
        }
    }

    // —— 对外操作 ——
    // 捕获方式（占位实现）：仅更新本地UI状态，不落DataStore
    fun toggleCaptureNlEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(captureNlEnabled = enabled)
    }
    fun toggleCaptureA11yEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(captureA11yEnabled = enabled)
    }

    fun toggleGlobalEnabled(enabled: Boolean) {
        viewModelScope.launch {
            runCatching { settingsRepository.setGlobalEnabled(enabled) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
            if (enabled) autoLedgerManager.start() else autoLedgerManager.stop()
        }
    }

    fun setSelectedMode(mode: AutoMode) {
        viewModelScope.launch {
            val v = if (mode == AutoMode.GEN_AUTO) "FULL" else "SEMI"
            runCatching { settingsRepository.setMode(v) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun requestRebind() {
        viewModelScope.launch {
            val ok = runCatching { notificationAccessController.requestRebind() }.getOrDefault(false)
            if (!ok) _uiState.value = _uiState.value.copy(error = "未授权或系统拒绝重连，请点击‘去授权’") else _uiState.value = _uiState.value.copy(error = null)
        }
    }
    fun openNotificationAccessSettings() { viewModelScope.launch { runCatching { notificationAccessController.openNotificationAccessSettings() } } }
    // 无障碍设置（占位）：仅尝试打开系统无障碍设置，正式版会通过专用Controller
    fun openAccessibilitySettings() {
        // 这里不直接持有Context，先以错误提示占位，后续接入Controller再实现
        _uiState.value = _uiState.value.copy(error = null)
    }
    fun openPromptChannelSettings() { viewModelScope.launch { runCatching { notificationAccessController.openChannelSettings("auto_ledger_prompt") } } }
    fun openStatusChannelSettings() { viewModelScope.launch { runCatching { notificationAccessController.openChannelSettings("auto_ledger_status") } } }
    
    // 一键自检：聚合现有诊断为简明文案，显示在UI中
    fun runSelfCheck() {
        viewModelScope.launch {
            val s = _uiState.value
            val lines = mutableListOf<String>()
            lines += if (s.notificationListenerEnabled) "✓ 通知监听：已连接" else "✗ 通知监听：未连接（请点击‘去授权’）"
            // 渠道提示：固定文案引导用户到“通知渠道设置”卡片
            lines += "提示：若未弹横幅，请在‘通知渠道设置’中开启‘确认渠道’的横幅/锁屏/悬浮权限。"
            _uiState.value = _uiState.value.copy(error = lines.joinToString("\n"))
        }
    }

    // —— 兼容旧UI的空实现/本地状态更新（不读写DataStore） ——
    fun toggleDeveloperMode() { _uiState.value = _uiState.value.copy(developerModeEnabled = !_uiState.value.developerModeEnabled) }
    fun toggleAutoCreate(enabled: Boolean) { _uiState.value = _uiState.value.copy(autoCreateEnabled = enabled) }
    fun toggleAlipayAuto(on: Boolean) { _uiState.value = _uiState.value.copy(alipayAutoOn = on) }
    fun setAlipayDefaultAccount(id: String) { _uiState.value = _uiState.value.copy(alipayDefaultAccountId = id) }
    fun setDefaultExpenseCategory(id: String) { _uiState.value = _uiState.value.copy(defaultExpenseCategoryId = id) }
    fun setDefaultIncomeCategory(id: String) { _uiState.value = _uiState.value.copy(defaultIncomeCategoryId = id) }
    fun setAlipayAccountSourceIsLast(isLast: Boolean) { _uiState.value = _uiState.value.copy(alipayAccountSourceIsLast = isLast) }
    fun setAlipayCategoryExpenseSourceIsLast(isLast: Boolean) { _uiState.value = _uiState.value.copy(alipayCategoryExpenseSourceIsLast = isLast) }
    fun setAlipayCategoryIncomeSourceIsLast(isLast: Boolean) { _uiState.value = _uiState.value.copy(alipayCategoryIncomeSourceIsLast = isLast) }
    fun setWechatDefaultAccount(id: String) { _uiState.value = _uiState.value.copy(wechatDefaultAccountId = id) }
    fun setWechatExpenseCategory(id: String) { _uiState.value = _uiState.value.copy(wechatExpenseCategoryId = id) }
    fun setWechatIncomeCategory(id: String) { _uiState.value = _uiState.value.copy(wechatIncomeCategoryId = id) }
    fun setWechatAccountSourceIsLast(isLast: Boolean) { _uiState.value = _uiState.value.copy(wechatAccountSourceIsLast = isLast) }
    fun setWechatCategoryExpenseSourceIsLast(isLast: Boolean) { _uiState.value = _uiState.value.copy(wechatCategoryExpenseSourceIsLast = isLast) }
    fun setWechatCategoryIncomeSourceIsLast(isLast: Boolean) { _uiState.value = _uiState.value.copy(wechatCategoryIncomeSourceIsLast = isLast) }
    fun setFixedLedgerEnabled(enabled: Boolean) { _uiState.value = _uiState.value.copy(fixedLedgerEnabled = enabled) }
    fun setFixedLedgerId(id: String) { _uiState.value = _uiState.value.copy(fixedLedgerId = id) }
    fun resetDeveloperSettings() {}
    fun clearDedupCache(onResult: (Int) -> Unit = {}) { onResult(0) }
    fun printRecentTransactions(limit: Int = 5) { _uiState.value = _uiState.value.copy(debugRecentTx = emptyList()) }
    fun clearRecentPreview() { _uiState.value = _uiState.value.copy(debugRecentTx = emptyList()) }
    fun toggleEmitWithoutKeywords(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(emitWithoutKeywords = enabled)
        viewModelScope.launch { runCatching { developerSettingsRepository.setEmitWithoutKeywords(enabled) } }
    }
    fun toggleEmitGroupSummary(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(emitGroupSummary = enabled)
        viewModelScope.launch { runCatching { developerSettingsRepository.setEmitGroupSummary(enabled) } }
    }
    fun toggleLogUnmatched(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(logUnmatchedNotifications = enabled)
        viewModelScope.launch { runCatching { developerSettingsRepository.setLogUnmatched(enabled) } }
    }
    fun updateAutoCreateThreshold(value: Float) { _uiState.value = _uiState.value.copy(autoCreateConfidenceThreshold = value.coerceIn(0.5f, 0.95f)) }
    fun updateMinAmountCents(value: Int) { _uiState.value = _uiState.value.copy(minAmountCents = value.coerceIn(0, 10_000_000)) }
    fun toggleDedupEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(dedupEnabled = enabled)
        viewModelScope.launch { runCatching { developerSettingsRepository.setDedupEnabled(enabled) } }
    }
    fun updateDedupWindowSec(value: Int) {
        val v = value.coerceIn(1, 600)
        _uiState.value = _uiState.value.copy(dedupWindowSec = v)
        viewModelScope.launch { runCatching { developerSettingsRepository.setDedupWindowSec(v) } }
    }
    fun toggleDedupDebugParse(enabled: Boolean) { _uiState.value = _uiState.value.copy(dedupDebugParseOnSkip = enabled) }
}
