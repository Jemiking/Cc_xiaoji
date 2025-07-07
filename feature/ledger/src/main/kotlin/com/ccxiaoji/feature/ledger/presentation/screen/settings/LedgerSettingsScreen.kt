package com.ccxiaoji.feature.ledger.presentation.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Observer
import com.ccxiaoji.feature.ledger.presentation.screen.settings.components.*
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerSettingsViewModel
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.coroutines.launch

/**
 * 记账设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategoryManagement: () -> Unit,
    onNavigateToAccountManagement: () -> Unit,
    onNavigateToBudgetManagement: () -> Unit,
    onNavigateToDataExport: () -> Unit,
    onNavigateToRecurringTransactions: () -> Unit,
    onNavigateToCurrencySelection: () -> Unit = {},
    onNavigateToAccountSelection: () -> Unit = {},
    onNavigateToReminderSettings: () -> Unit = {},
    onNavigateToHomeDisplaySettings: () -> Unit = {},
    viewModel: LedgerSettingsViewModel = hiltViewModel(),
    navController: androidx.navigation.NavController? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 处理币种选择结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val currencyObserver = Observer<String> { currency ->
                if (currency != null) {
                    viewModel.updateBasicSettings(
                        settings.basicSettings.copy(defaultCurrency = currency)
                    )
                    savedStateHandle.remove<String>("selected_currency")
                }
            }
            
            val accountObserver = Observer<Long> { accountId ->
                if (accountId != null) {
                    viewModel.updateBasicSettings(
                        settings.basicSettings.copy(defaultAccountId = accountId)
                    )
                    savedStateHandle.remove<Long>("selected_account_id")
                }
            }
            
            val reminderUpdatedObserver = Observer<Boolean> { updated ->
                if (updated == true) {
                    // 提醒设置已更新，重新加载设置
                    viewModel.loadSettings()
                    savedStateHandle.remove<Boolean>("reminder_settings_updated")
                }
            }
            
            val homeDisplayUpdatedObserver = Observer<Boolean> { updated ->
                if (updated == true) {
                    // 首页显示设置已更新，重新加载设置
                    viewModel.loadSettings()
                    savedStateHandle.remove<Boolean>("home_display_settings_updated")
                }
            }
            
            savedStateHandle.getLiveData<String>("selected_currency").observe(lifecycleOwner, currencyObserver)
            savedStateHandle.getLiveData<Long>("selected_account_id").observe(lifecycleOwner, accountObserver)
            savedStateHandle.getLiveData<Boolean>("reminder_settings_updated").observe(lifecycleOwner, reminderUpdatedObserver)
            savedStateHandle.getLiveData<Boolean>("home_display_settings_updated").observe(lifecycleOwner, homeDisplayUpdatedObserver)
            
            onDispose {
                savedStateHandle.getLiveData<String>("selected_currency").removeObserver(currencyObserver)
                savedStateHandle.getLiveData<Long>("selected_account_id").removeObserver(accountObserver)
                savedStateHandle.getLiveData<Boolean>("reminder_settings_updated").removeObserver(reminderUpdatedObserver)
                savedStateHandle.getLiveData<Boolean>("home_display_settings_updated").removeObserver(homeDisplayUpdatedObserver)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "记账设置",
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    FlatButton(
                        text = "重置",
                        onClick = {
                            viewModel.resetAllSettings()
                            scope.launch {
                                snackbarHostState.showSnackbar("已重置所有设置")
                            }
                        },
                        backgroundColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(end = DesignTokens.Spacing.small)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = DesignTokens.Spacing.small),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            // 基础设置部分
            item {
                SettingSectionHeader(title = "基础设置")
            }
            
            item {
                BasicSettingsSection(
                    basicSettings = settings.basicSettings,
                    accounts = uiState.accounts,
                    onUpdateBasicSettings = viewModel::updateBasicSettings,
                    onNavigateToCurrencySelection = onNavigateToCurrencySelection,
                    onNavigateToAccountSelection = onNavigateToAccountSelection,
                    onNavigateToReminderSettings = onNavigateToReminderSettings,
                    onNavigateToHomeDisplaySettings = onNavigateToHomeDisplaySettings
                )
            }
            
            // 高级设置部分
            item {
                SettingSectionHeader(title = "高级设置")
            }
            
            item {
                AdvancedSettingsSection(
                    advancedSettings = settings.advancedSettings,
                    onUpdateAdvancedSettings = viewModel::updateAdvancedSettings,
                    onNavigateToCategoryManagement = onNavigateToCategoryManagement,
                    onNavigateToAccountManagement = onNavigateToAccountManagement,
                    onNavigateToBudgetManagement = onNavigateToBudgetManagement,
                    onNavigateToDataExport = onNavigateToDataExport
                )
            }
            
            // 自动化设置部分
            item {
                SettingSectionHeader(title = "自动化设置")
            }
            
            item {
                AutomationSettingsSection(
                    automationSettings = settings.automationSettings,
                    onUpdateAutomationSettings = viewModel::updateAutomationSettings,
                    onNavigateToRecurringTransactions = onNavigateToRecurringTransactions
                )
            }
        }
    }
}