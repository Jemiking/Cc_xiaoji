package com.ccxiaoji.feature.ledger.presentation.screen.creditcard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.screen.creditcard.settings.components.*
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CreditCardSettingsViewModel
import com.ccxiaoji.ui.components.FlatAlertDialog
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.coroutines.launch

/**
 * 信用卡设置界面
 * 允许用户编辑信用卡的详细信息，包括额度、费用、利率等
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardSettingsScreen(
    accountId: String,
    navController: NavController,
    viewModel: CreditCardSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(accountId) {
        viewModel.loadCreditCard(accountId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "信用卡设置",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (uiState.hasChanges) {
                        FlatButton(
                            text = if (uiState.isSaving) "保存中..." else "保存",
                            onClick = { viewModel.saveChanges() },
                            enabled = !uiState.isSaving,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(end = DesignTokens.Spacing.small)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.large)
            ) {
                // 基本信息
                BasicInfoSection(
                    cardName = uiState.cardName,
                    onCardNameChange = viewModel::updateCardName
                )
                
                // 额度设置
                CreditLimitSection(
                    creditLimit = uiState.creditLimit,
                    cashAdvanceLimit = uiState.cashAdvanceLimit,
                    onCreditLimitChange = viewModel::updateCreditLimit,
                    onCashAdvanceLimitChange = viewModel::updateCashAdvanceLimit
                )
                
                // 账单日期设置
                BillingDatesSection(
                    billingDay = uiState.billingDay,
                    paymentDueDay = uiState.paymentDueDay,
                    gracePeriodDays = uiState.gracePeriodDays,
                    onBillingDayChange = viewModel::updateBillingDay,
                    onPaymentDueDayChange = viewModel::updatePaymentDueDay,
                    onGracePeriodDaysChange = viewModel::updateGracePeriodDays
                )
                
                // 费用设置
                FeeSection(
                    annualFee = uiState.annualFee,
                    annualFeeWaiverThreshold = uiState.annualFeeWaiverThreshold,
                    interestRate = uiState.interestRate,
                    onAnnualFeeChange = viewModel::updateAnnualFee,
                    onWaiverThresholdChange = viewModel::updateAnnualFeeWaiverThreshold,
                    onInterestRateChange = viewModel::updateInterestRate
                )
                
                // 提醒设置
                ReminderSection(
                    isReminderEnabled = uiState.isReminderEnabled,
                    onReminderToggle = viewModel::toggleReminder
                )
                
                // 底部间距
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
            }
        }
    }
    
    // 保存成功提示
    uiState.saveSuccessMessage?.let { message ->
        LaunchedEffect(message) {
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.navigateUp()
        }
    }
    
    // 错误提示
    uiState.errorMessage?.let { errorMessage ->
        FlatAlertDialog(
            dialogTitle = "错误",
            dialogText = errorMessage,
            confirmText = "确定",
            onConfirmation = { viewModel.clearError() },
            onDismissRequest = { viewModel.clearError() }
        )
    }
}