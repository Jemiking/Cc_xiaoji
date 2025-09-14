package com.ccxiaoji.feature.ledger.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CurrencySelectionViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 币种选择页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionScreen(
    navController: NavController,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: CurrencySelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 系统返回
    BackHandler { onNavigateBack?.invoke() ?: navController.popBackStack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择默认币种") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack?.invoke() ?: navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = DesignTokens.Spacing.small)
        ) {
            // 常用币种部分
            item {
                Text(
                    text = "常用币种",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = DesignTokens.Spacing.medium,
                        vertical = DesignTokens.Spacing.small
                    )
                )
            }
            
            items(uiState.commonCurrencies) { currency ->
                CurrencyItem(
                    currency = currency,
                    isSelected = currency.code == uiState.selectedCurrency,
                    onClick = {
                        viewModel.selectCurrency(currency.code)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_currency", currency.code)
                        onNavigateBack?.invoke() ?: navController.popBackStack()
                    }
                )
            }
            
            // 其他币种部分
            if (uiState.otherCurrencies.isNotEmpty()) {
                item {
                    Divider(
                        modifier = Modifier.padding(vertical = DesignTokens.Spacing.medium)
                    )
                    Text(
                        text = "其他币种",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = DesignTokens.Spacing.medium,
                            vertical = DesignTokens.Spacing.small
                        )
                    )
                }
                
                items(uiState.otherCurrencies) { currency ->
                    CurrencyItem(
                        currency = currency,
                        isSelected = currency.code == uiState.selectedCurrency,
                    onClick = {
                        viewModel.selectCurrency(currency.code)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_currency", currency.code)
                        onNavigateBack?.invoke() ?: navController.popBackStack()
                    }
                )
                }
            }
        }
    }
}

/**
 * 币种项
 */
@Composable
private fun CurrencyItem(
    currency: CurrencyInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { 
            Text(
                text = currency.code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ) 
        },
        supportingContent = {
            Text(
                text = currency.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            if (currency.symbol != null) {
                Text(
                    text = currency.symbol,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}

/**
 * 币种信息
 */
data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String? = null
)
