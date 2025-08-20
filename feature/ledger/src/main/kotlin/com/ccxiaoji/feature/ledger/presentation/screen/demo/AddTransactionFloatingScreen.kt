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
import androidx.compose.ui.text.style.TextAlign
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
fun AddTransactionFloatingScreen(
    navController: NavController,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showDetailsSheet by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "方案五：浮动操作布局",
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
        },
        bottomBar = {
            // 浮动工具栏
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                // 功能工具按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 时间按钮
                    FloatingToolButton(
                        icon = Icons.Default.Schedule,
                        label = "时间",
                        hasContent = true, // 总是有时间
                        onClick = { showDetailsSheet = true }
                    )
                    
                    // 位置按钮
                    FloatingToolButton(
                        icon = Icons.Default.LocationOn,
                        label = "位置",
                        hasContent = uiState.selectedLocation != null,
                        onClick = { showDetailsSheet = true }
                    )
                    
                    // 备注按钮
                    FloatingToolButton(
                        icon = Icons.Default.Edit,
                        label = "备注",
                        hasContent = uiState.note.isNotBlank(),
                        onClick = { showDetailsSheet = true }
                    )
                    
                    // 保存按钮
                    FloatingActionButton(
                        onClick = { 
                            scope.launch {
                                viewModel.saveTransaction {
                                    navController.navigateUp()
                                }
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = DesignTokens.BrandColors.Ledger,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 8.dp
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "保存记账",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(DesignTokens.Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部留白
            Spacer(modifier = Modifier.height(24.dp))
            
            // 核心区域 - 金额输入
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.large),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.large)
                ) {
                    // 金额输入 - 大号显示
                    OutlinedTextField(
                        value = uiState.amountText,
                        onValueChange = viewModel::updateAmount,
                        label = { Text("金额", color = MaterialTheme.colorScheme.onPrimaryContainer) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.large),
                        singleLine = true,
                        isError = uiState.amountError != null,
                        supportingText = uiState.amountError?.let { { Text(it) } },
                        textStyle = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        leadingIcon = {
                            Text(
                                text = "¥",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    )
                    
                    // 收入/支出指示器
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                    ) {
                        AssistChip(
                            onClick = { viewModel.setIncomeType(false) },
                            label = { Text("支出") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (!uiState.isIncome) {
                                    DesignTokens.BrandColors.Error.copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                labelColor = if (!uiState.isIncome) {
                                    DesignTokens.BrandColors.Error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            ),
                            border = if (!uiState.isIncome) {
                                androidx.compose.foundation.BorderStroke(
                                    1.dp, DesignTokens.BrandColors.Error
                                )
                            } else {
                                null
                            }
                        )
                        
                        AssistChip(
                            onClick = { viewModel.setIncomeType(true) },
                            label = { Text("收入") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (uiState.isIncome) {
                                    DesignTokens.BrandColors.Success.copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                labelColor = if (uiState.isIncome) {
                                    DesignTokens.BrandColors.Success
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            ),
                            border = if (uiState.isIncome) {
                                androidx.compose.foundation.BorderStroke(
                                    1.dp, DesignTokens.BrandColors.Success
                                )
                            } else {
                                null
                            }
                        )
                    }
                }
            }
            
            // 中间留白区域
            Spacer(modifier = Modifier.height(32.dp))
            
            // 分类选择
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.showCategoryPicker() },
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                    ) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            tint = DesignTokens.BrandColors.Ledger,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "分类",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val categoryInfo = uiState.selectedCategoryInfo
                            Text(
                                text = categoryInfo?.fullPath ?: categoryInfo?.categoryName ?: "点击选择分类",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (categoryInfo != null) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    Icon(
                        Icons.Default.NavigateNext,
                        contentDescription = "选择分类",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 账户选择
            if (uiState.accounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.Spacing.large)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                        ) {
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = DesignTokens.BrandColors.Ledger,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "账户",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                        
                        AccountSelector(
                            accounts = uiState.accounts,
                            selectedAccount = uiState.selectedAccount,
                            onAccountSelected = viewModel::selectAccount,
                            label = "选择账户"
                        )
                    }
                }
            }
            
            // 底部留白，为浮动工具栏留出空间
            Spacer(modifier = Modifier.height(96.dp))
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
        
        // 详细信息底部表单
        if (showDetailsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDetailsSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.large)
                ) {
                    Text(
                        text = "详细信息",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
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
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.medium),
                        maxLines = 3,
                        placeholder = { Text("添加备注信息...") }
                    )
                    
                    // 底部空间
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun FloatingToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    hasContent: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Badge(
                containerColor = if (hasContent) {
                    DesignTokens.BrandColors.Ledger
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (hasContent) {
                        DesignTokens.BrandColors.Ledger
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (hasContent) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}