package com.ccxiaoji.feature.ledger.presentation.screen.demo

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.component.AccountSelector
import com.ccxiaoji.feature.ledger.presentation.component.CategoryPicker
import com.ccxiaoji.feature.ledger.presentation.component.DateTimePicker
import com.ccxiaoji.feature.ledger.presentation.component.LocationPicker
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AddTransactionViewModel
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionCardsScreen(
    navController: NavController,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "方案二：卡片分组布局",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 卡片1：交易基本信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 卡片标题
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = DesignTokens.BrandColors.Ledger,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "交易基本信息",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // 账户选择
                    if (uiState.accounts.isNotEmpty()) {
                        AccountSelector(
                            accounts = uiState.accounts,
                            selectedAccount = uiState.selectedAccount,
                            onAccountSelected = viewModel::selectAccount,
                            label = "选择账户"
                        )
                    }
                    
                    // 收入/支出切换
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        FilterChip(
                            selected = !uiState.isIncome,
                            onClick = { viewModel.setIncomeType(false) },
                            label = { Text("支出") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DesignTokens.BrandColors.Error.copy(alpha = 0.1f),
                                selectedLabelColor = DesignTokens.BrandColors.Error
                            ),
                            leadingIcon = {
                                if (!uiState.isIncome) {
                                    Icon(
                                        Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )
                        FilterChip(
                            selected = uiState.isIncome,
                            onClick = { viewModel.setIncomeType(true) },
                            label = { Text("收入") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DesignTokens.BrandColors.Success.copy(alpha = 0.1f),
                                selectedLabelColor = DesignTokens.BrandColors.Success
                            ),
                            leadingIcon = {
                                if (uiState.isIncome) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )
                    }
                    
                    // 金额输入
                    OutlinedTextField(
                        value = uiState.amountText,
                        onValueChange = viewModel::updateAmount,
                        label = { Text("交易金额") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (uiState.isIncome) {
                                DesignTokens.BrandColors.Success
                            } else {
                                DesignTokens.BrandColors.Error
                            }
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small),
                        singleLine = true,
                        isError = uiState.amountError != null,
                        supportingText = uiState.amountError?.let { { Text(it) } },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        leadingIcon = {
                            Text(
                                text = "¥",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isIncome) {
                                    DesignTokens.BrandColors.Success
                                } else {
                                    DesignTokens.BrandColors.Error
                                }
                            )
                        }
                    )
                }
            }
            
            // 卡片2：分类和描述
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 卡片标题
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = DesignTokens.BrandColors.Ledger,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "分类和描述",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // 分类选择
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.showCategoryPicker() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "交易分类",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val categoryInfo = uiState.selectedCategoryInfo
                                Text(
                                    text = categoryInfo?.fullPath ?: categoryInfo?.categoryName ?: "点击选择分类",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (categoryInfo != null) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            Icon(
                                Icons.Default.NavigateNext,
                                contentDescription = "选择分类",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // 备注输入
                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = viewModel::updateNote,
                        label = { Text("备注说明") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small),
                        maxLines = 3,
                        placeholder = { Text("可选：添加交易备注...") }
                    )
                }
            }
            
            // 卡片3：时间和位置
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 卡片标题
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = DesignTokens.BrandColors.Ledger,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "时间和位置",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // 时间选择器
                    DateTimePicker(
                        selectedDate = uiState.selectedDate,
                        selectedTime = uiState.selectedTime,
                        onDateSelected = viewModel::updateDate,
                        onTimeSelected = viewModel::updateTime
                    )
                    
                    // 位置选择器
                    LocationPicker(
                        selectedLocation = uiState.selectedLocation,
                        onLocationSelected = viewModel::updateLocation
                    )
                }
            }
            
            // 保存按钮
            FlatButton(
                text = "保存记账",
                onClick = { 
                    scope.launch {
                        viewModel.saveTransaction {
                            navController.navigateUp()
                        }
                    }
                },
                enabled = uiState.canSave && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DesignTokens.BrandColors.Ledger
            )
        }
        
        // 加载指示器
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // 分类选择器
        CategoryPicker(
            isVisible = uiState.showCategoryPicker,
            categoryGroups = uiState.categoryGroups,
            selectedCategoryId = uiState.selectedCategoryInfo?.categoryId,
            onCategorySelected = viewModel::selectCategory,
            onDismiss = viewModel::hideCategoryPicker,
            title = if (uiState.isIncome) "选择收入分类" else "选择支出分类"
        )
    }
}