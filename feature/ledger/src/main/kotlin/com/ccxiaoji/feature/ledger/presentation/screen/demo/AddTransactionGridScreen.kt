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
fun AddTransactionGridScreen(
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
                        text = "方案四：网格布局",
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
                .padding(DesignTokens.Spacing.small), // 使用更小的外边距
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            // 第一行：账户选择 + 收入/支出类型（2列布局）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                // 账户选择器（占2/3宽度）
                if (uiState.accounts.isNotEmpty()) {
                    Box(modifier = Modifier.weight(2f)) {
                        CompactAccountCard(
                            selectedAccount = uiState.selectedAccount,
                            accounts = uiState.accounts,
                            onAccountSelected = viewModel::selectAccount
                        )
                    }
                }
                
                // 收入/支出切换（占1/3宽度）
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CompactTypeChip(
                        selected = !uiState.isIncome,
                        onClick = { viewModel.setIncomeType(false) },
                        text = "支出",
                        color = DesignTokens.BrandColors.Error
                    )
                    CompactTypeChip(
                        selected = uiState.isIncome,
                        onClick = { viewModel.setIncomeType(true) },
                        text = "收入",
                        color = DesignTokens.BrandColors.Success
                    )
                }
            }
            
            // 第二行：金额输入（完整宽度，重要字段）
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
                supportingText = uiState.amountError?.let { { Text(it, style = MaterialTheme.typography.labelSmall) } },
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
            
            // 第三行：分类选择（完整宽度）
            CompactCategoryCard(
                selectedCategory = uiState.selectedCategoryInfo,
                onClick = { viewModel.showCategoryPicker() }
            )
            
            // 第四行：日期 + 时间 + 位置（3列紧凑布局）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                // 日期
                CompactDateCard(
                    selectedDate = uiState.selectedDate,
                    onDateSelected = viewModel::updateDate,
                    modifier = Modifier.weight(1f)
                )
                
                // 时间
                CompactTimeCard(
                    selectedTime = uiState.selectedTime,
                    onTimeSelected = viewModel::updateTime,
                    modifier = Modifier.weight(1f)
                )
                
                // 位置
                CompactLocationCard(
                    selectedLocation = uiState.selectedLocation,
                    onLocationSelected = viewModel::updateLocation,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 第五行：备注输入（完整宽度）
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                label = { Text("备注", style = MaterialTheme.typography.labelMedium) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small),
                singleLine = true,
                placeholder = { Text("可选", style = MaterialTheme.typography.bodySmall) }
            )
            
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DesignTokens.Spacing.small),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactAccountCard(
    selectedAccount: com.ccxiaoji.feature.ledger.domain.model.Account?,
    accounts: List<com.ccxiaoji.feature.ledger.domain.model.Account>,
    onAccountSelected: (com.ccxiaoji.feature.ledger.domain.model.Account) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            onClick = { expanded = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "账户",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedAccount?.name ?: "选择账户",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedAccount != null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = {
                        onAccountSelected(account)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CompactTypeChip(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { 
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        },
        modifier = Modifier.fillMaxWidth(),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.1f),
            selectedLabelColor = color
        )
    )
}

@Composable
private fun CompactCategoryCard(
    selectedCategory: com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo?,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "分类",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = selectedCategory?.fullPath ?: selectedCategory?.categoryName ?: "选择分类",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedCategory != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Icon(
                Icons.Default.NavigateNext,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompactDateCard(
    selectedDate: kotlinx.datetime.LocalDate,
    onDateSelected: (kotlinx.datetime.LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        onClick = { /* TODO: 实现日期选择逻辑 */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = DesignTokens.BrandColors.Ledger
            )
            Text(
                text = "日期",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${selectedDate.monthNumber}/${selectedDate.dayOfMonth}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CompactTimeCard(
    selectedTime: kotlinx.datetime.LocalTime,
    onTimeSelected: (kotlinx.datetime.LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        onClick = { /* TODO: 实现时间选择逻辑 */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = DesignTokens.BrandColors.Ledger
            )
            Text(
                text = "时间",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${String.format("%02d", selectedTime.hour)}:${String.format("%02d", selectedTime.minute)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CompactLocationCard(
    selectedLocation: com.ccxiaoji.feature.ledger.domain.model.LocationData?,
    onLocationSelected: (com.ccxiaoji.feature.ledger.domain.model.LocationData?) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        onClick = { /* TODO: 实现位置选择逻辑 */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (selectedLocation != null) {
                    DesignTokens.BrandColors.Ledger
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = "位置",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (selectedLocation != null) "已定位" else "未设置",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (selectedLocation != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}