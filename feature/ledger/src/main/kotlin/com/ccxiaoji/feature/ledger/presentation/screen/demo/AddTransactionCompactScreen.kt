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
import com.ccxiaoji.feature.ledger.R
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
fun AddTransactionCompactScreen(
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
                        text = "方案一：紧凑型布局",
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
                .padding(horizontal = DesignTokens.Spacing.medium)
                .padding(vertical = DesignTokens.Spacing.small), // 减少垂直内边距
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small) // 减少间距到small
        ) {
            // 第一行：账户选择 + 收入/支出切换（合并在一行）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 账户选择器（占2/3宽度）
                if (uiState.accounts.isNotEmpty()) {
                    Box(modifier = Modifier.weight(2f)) {
                        AccountSelector(
                            accounts = uiState.accounts,
                            selectedAccount = uiState.selectedAccount,
                            onAccountSelected = viewModel::selectAccount,
                            label = "账户"
                        )
                    }
                }
                
                // 收入/支出切换（占1/3宽度）
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = !uiState.isIncome,
                        onClick = { viewModel.setIncomeType(false) },
                        label = { Text("支出", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DesignTokens.BrandColors.Error.copy(alpha = 0.1f),
                            selectedLabelColor = DesignTokens.BrandColors.Error
                        )
                    )
                    FilterChip(
                        selected = uiState.isIncome,
                        onClick = { viewModel.setIncomeType(true) },
                        label = { Text("收入", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DesignTokens.BrandColors.Success.copy(alpha = 0.1f),
                            selectedLabelColor = DesignTokens.BrandColors.Success
                        )
                    )
                }
            }
            
            // 金额输入（紧凑型）
            OutlinedTextField(
                value = uiState.amountText,
                onValueChange = viewModel::updateAmount,
                label = { Text("金额", style = MaterialTheme.typography.labelMedium) },
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
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            
            // 分类选择（简化显示）
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.showCategoryPicker() },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
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
                            text = "分类",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val categoryInfo = uiState.selectedCategoryInfo
                        Text(
                            text = categoryInfo?.fullPath ?: categoryInfo?.categoryName ?: "请选择分类",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (categoryInfo != null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = "选择分类",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 时间和位置（水平布局）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                // 时间选择器（占1/2宽度）
                Box(modifier = Modifier.weight(1f)) {
                    DateTimePicker(
                        selectedDate = uiState.selectedDate,
                        selectedTime = uiState.selectedTime,
                        onDateSelected = viewModel::updateDate,
                        onTimeSelected = viewModel::updateTime
                    )
                }
                
                // 位置选择器（占1/2宽度）
                Box(modifier = Modifier.weight(1f)) {
                    LocationPicker(
                        selectedLocation = uiState.selectedLocation,
                        onLocationSelected = viewModel::updateLocation
                    )
                }
            }
            
            // 备注输入（单行显示）
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                label = { Text("备注", style = MaterialTheme.typography.labelMedium) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small),
                singleLine = true, // 默认单行
                placeholder = { Text("可选", style = MaterialTheme.typography.bodySmall) }
            )
            
            // 保存按钮（减少顶部间距）
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DesignTokens.Spacing.small), // 减少顶部边距
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

// 无需扩展图标