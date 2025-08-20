package com.ccxiaoji.feature.ledger.presentation.screen.demo

import androidx.compose.animation.*
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
fun AddTransactionSteppedScreen(
    navController: NavController,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var isDetailExpanded by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "方案三：分步填写布局",
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
            // 快速记账核心区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 标题
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "快速记账",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    // 核心三要素 - 第一行：金额输入
                    OutlinedTextField(
                        value = uiState.amountText,
                        onValueChange = viewModel::updateAmount,
                        label = { Text("金额", color = MaterialTheme.colorScheme.onPrimaryContainer) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.medium),
                        singleLine = true,
                        isError = uiState.amountError != null,
                        supportingText = uiState.amountError?.let { { Text(it) } },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        leadingIcon = {
                            Text(
                                text = "¥",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    )
                    
                    // 第二行：分类和账户
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        // 分类选择
                        OutlinedCard(
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.showCategoryPicker() },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(DesignTokens.Spacing.medium),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    tint = DesignTokens.BrandColors.Ledger
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val categoryInfo = uiState.selectedCategoryInfo
                                Text(
                                    text = categoryInfo?.categoryName ?: "分类",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (categoryInfo != null) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                        
                        // 账户选择（简化显示）
                        if (uiState.accounts.isNotEmpty()) {
                            OutlinedCard(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(DesignTokens.Spacing.medium),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = DesignTokens.BrandColors.Ledger
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = uiState.selectedAccount?.name ?: "账户",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (uiState.selectedAccount != null) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // 第三行：收入/支出切换
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
                                selectedContainerColor = DesignTokens.BrandColors.Error.copy(alpha = 0.2f),
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
                                selectedContainerColor = DesignTokens.BrandColors.Success.copy(alpha = 0.2f),
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
                }
            }
            
            // 详细信息折叠区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column {
                    // 可展开的标题栏
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { isDetailExpanded = !isDetailExpanded },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.large),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "详细信息",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Badge(
                                    containerColor = DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f),
                                    contentColor = DesignTokens.BrandColors.Ledger
                                ) {
                                    Text(
                                        text = "可选",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            
                            Icon(
                                imageVector = if (isDetailExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isDetailExpanded) "收起" else "展开",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // 可展开的内容区域
                    AnimatedVisibility(
                        visible = isDetailExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(DesignTokens.Spacing.large),
                            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                        ) {
                            // 完整的账户选择器
                            if (uiState.accounts.isNotEmpty()) {
                                AccountSelector(
                                    accounts = uiState.accounts,
                                    selectedAccount = uiState.selectedAccount,
                                    onAccountSelected = viewModel::selectAccount,
                                    label = "选择账户"
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
                            
                            // 备注输入
                            OutlinedTextField(
                                value = uiState.note,
                                onValueChange = viewModel::updateNote,
                                label = { Text("备注说明") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small),
                                maxLines = 3,
                                placeholder = { Text("添加备注信息...") }
                            )
                        }
                    }
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