package com.ccxiaoji.feature.ledger.presentation.screen.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties  
import androidx.compose.ui.draw.clip
import kotlinx.datetime.*
import java.time.format.DateTimeFormatter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.presentation.component.AccountSelector
import com.ccxiaoji.feature.ledger.presentation.component.CategoryPicker
import com.ccxiaoji.feature.ledger.presentation.component.DateTimePicker
import com.ccxiaoji.feature.ledger.presentation.component.LocationPicker
import com.ccxiaoji.feature.ledger.presentation.component.DynamicCategoryIcon
import com.ccxiaoji.feature.ledger.presentation.component.LedgerSelector
import com.ccxiaoji.feature.ledger.presentation.component.LedgerSelectorDialog
import com.ccxiaoji.feature.ledger.presentation.component.SyncTargetSelectorDialog
import com.ccxiaoji.feature.ledger.presentation.screen.ledger.components.CategoryChip
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AddTransactionViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionType
import com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    transactionId: String? = null,
    viewModel: AddTransactionViewModel = hiltViewModel(),
    uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
) {
    println("🔍 [AddTransactionScreen] 组件创建！")
    println("   - transactionId: '$transactionId'")
    println("   - 是否为编辑模式: ${!transactionId.isNullOrBlank()}")
    println("   - viewModel: $viewModel")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    // 币种：优先账户币种，其次默认CNY；支持本页覆盖
    val accountCurrency = uiState.selectedAccount?.currency
    var userCurrencyOverride by rememberSaveable { mutableStateOf(false) }
    var selectedCurrency by rememberSaveable { mutableStateOf(accountCurrency ?: "CNY") }
    LaunchedEffect(accountCurrency) {
        if (!userCurrencyOverride) {
            selectedCurrency = accountCurrency ?: "CNY"
        }
    }
    
    // 硬编码的调试参数
    val adjustmentParams = LayoutAdjustmentParams(
        // === 分类图标区域 ===
        categoryIconSize = 25.930233f,
        categoryHorizontalSpacing = 15.732536f,
        categoryVerticalSpacing = 20.170633f,
        categoryGridPadding = 0.0f,
        categoryTextSize = 10.799782f,
        categoryCardCornerRadius = 8.0f,
        categoryCardPadding = 4.0f,
        gridColumnCount = 6,
        
        // === Tab切换区域 ===
        tabRowHeight = 40.0f,
        tabRowWidth = 200.0f,
        tabCornerRadius = 8.0f,
        tabVerticalPadding = 8.0f,
        
        // === 输入区域布局 ===
        inputAreaHeight = 315.4261f,
        inputAreaCornerRadius = 0.0f,
        inputAreaPadding = 0.0f,
        
        // === 备注区域细节 ===
        noteFieldTopPadding = 0.0f,
        noteFieldBottomPadding = 0.0f,
        noteFieldHorizontalPadding = 0.0f,
        noteFieldContentPadding = 0.0f,
        noteTextSize = 14.0f,
        noteToAmountSpacing = 0.0f,
        
        // === 金额显示区域 ===
        amountTextSize = 25.841871f,
        amountTextPadding = 15.795361f,
        accountTextSize = 15.110469f,
        accountTextLeftPadding = 15.944222f,
        accountToNoteSpacing = 0.0f,
        amountToKeypadSpacing = 0.0f,
        
        // === 键盘区域 ===
        keypadButtonSize = 48.0f,
        keypadButtonSpacing = 8.0f,
        keypadRowSpacing = 3.4232678f,
        keypadButtonCornerRadius = 10.182958f,
        keypadTextSize = 16.85329f,
        keypadBottomPadding = 16.0f,
        keypadHorizontalPadding = 10.775346f,
        
        // === 整体布局权重 ===
        categoryGridWeight = 1.0f
    )

    // 使用真实的分类数据
    val currentCategories = remember(uiState.categoryGroups) {
        // 将CategoryGroup中的父分类（一级分类）提取出来作为网格显示的分类
        uiState.categoryGroups.map { categoryGroup ->
            categoryGroup.parent
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部：返回键和收入/支出切换在同一行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 左侧：返回键
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, 
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 中间：收入/支出切换
                TabRow(
                    selectedTabIndex = when (uiState.transactionType) {
                        TransactionType.EXPENSE -> 0
                        TransactionType.INCOME -> 1
                        TransactionType.TRANSFER -> 2
                        TransactionType.ALL -> 0 // 默认显示支出Tab
                    },
                    modifier = Modifier
                        .width((adjustmentParams.tabRowWidth * 1.5f).dp) // 增加宽度以容纳第三个Tab
                        .height(adjustmentParams.tabRowHeight.dp),
                    indicator = { },
                    divider = { }
                ) {
                    Tab(
                        selected = uiState.transactionType == TransactionType.EXPENSE,
                        onClick = { viewModel.setTransactionType(TransactionType.EXPENSE) },
                        modifier = Modifier.background(
                            if (uiState.transactionType == TransactionType.EXPENSE) Color.Black else Color.Transparent,
                            RoundedCornerShape(adjustmentParams.tabCornerRadius.dp)
                        )
                    ) {
                        Text(
                            text = "支出",
                            color = if (uiState.transactionType == TransactionType.EXPENSE) Color.White else Color.Gray,
                            modifier = Modifier.padding(vertical = adjustmentParams.tabVerticalPadding.dp)
                        )
                    }
                    Tab(
                        selected = uiState.transactionType == TransactionType.INCOME,
                        onClick = { viewModel.setTransactionType(TransactionType.INCOME) },
                        modifier = Modifier.background(
                            if (uiState.transactionType == TransactionType.INCOME) Color.Black else Color.Transparent,
                            RoundedCornerShape(adjustmentParams.tabCornerRadius.dp)
                        )
                    ) {
                        Text(
                            text = "收入",
                            color = if (uiState.transactionType == TransactionType.INCOME) Color.White else Color.Gray,
                            modifier = Modifier.padding(vertical = adjustmentParams.tabVerticalPadding.dp)
                        )
                    }
                    Tab(
                        selected = uiState.transactionType == TransactionType.TRANSFER,
                        onClick = { viewModel.setTransactionType(TransactionType.TRANSFER) },
                        modifier = Modifier.background(
                            if (uiState.transactionType == TransactionType.TRANSFER) Color.Black else Color.Transparent,
                            RoundedCornerShape(adjustmentParams.tabCornerRadius.dp)
                        )
                    ) {
                        Text(
                            text = "转账",
                            color = if (uiState.transactionType == TransactionType.TRANSFER) Color.White else Color.Gray,
                            modifier = Modifier.padding(vertical = adjustmentParams.tabVerticalPadding.dp)
                        )
                    }
                }
                
                // 右侧：空占位，保持布局平衡
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(adjustmentParams.categoryVerticalSpacing.dp))
            
            // 中间：分类网格 / 转账账户选择
            if (uiState.transactionType == TransactionType.TRANSFER) {
                // 转账模式：显示从账户→到账户选择器
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(adjustmentParams.categoryGridWeight)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 从账户选择器
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "从账户",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { viewModel.showFromAccountPicker() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = uiState.fromAccount?.name ?: "请选择转出账户")
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    }
                    
                    // 转账箭头
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "转账",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    // 到账户选择器
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "到账户",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { viewModel.showToAccountPicker() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = uiState.toAccount?.name ?: "请选择转入账户")
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                }
            } else {
                // 支出/收入模式：显示分类网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(adjustmentParams.gridColumnCount),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(adjustmentParams.categoryGridWeight)
                        .padding(horizontal = adjustmentParams.categoryGridPadding.dp),
                    verticalArrangement = Arrangement.spacedBy(adjustmentParams.categoryVerticalSpacing.dp),
                    horizontalArrangement = Arrangement.spacedBy(adjustmentParams.categoryHorizontalSpacing.dp)
                ) {
                    items(currentCategories) { category ->
                        ProductionCategoryCard(
                            category = category,
                            isSelected = uiState.selectedCategoryInfo?.categoryId == category.id,
                            onClick = { 
                                // 点击分类，直接选择该分类
                                viewModel.selectCategory(category)
                            },
                            params = adjustmentParams,
                            iconDisplayMode = uiPreferences.iconDisplayMode
                        )
                    }
                }
            }
            
            // 底部：输入区域（方案B：上方可滚动 + 底部固定键盘）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(
                            topStart = adjustmentParams.inputAreaCornerRadius.dp,
                            topEnd = adjustmentParams.inputAreaCornerRadius.dp
                        )
                    )
            ) {
                // 第一行：备注 + 金额 + 币种
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 0.dp, bottom = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.note,
                            onValueChange = viewModel::updateNote,
                            placeholder = { Text("点此输入备注...", fontSize = adjustmentParams.noteTextSize.sp) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = adjustmentParams.noteTextSize.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                        Spacer(Modifier.width(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (uiState.amountText.isBlank()) "0.00" else uiState.amountText,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isIncome) DesignTokens.BrandColors.Success else DesignTokens.BrandColors.Error
                            )
                            var currencyMenu by remember { mutableStateOf(false) }
                            Row(
                                modifier = Modifier.clickable { currencyMenu = true }.padding(horizontal = 2.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(text = selectedCurrency, style = MaterialTheme.typography.titleSmall)
                                Icon(Icons.Default.UnfoldMore, contentDescription = "选择币种", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            DropdownMenu(expanded = currencyMenu, onDismissRequest = { currencyMenu = false }) {
                                val common = listOf("CNY","USD","EUR","JPY","GBP","HKD","AUD","CAD","SGD","TWD","KRW")
                                common.forEach { code ->
                                    DropdownMenuItem(text = { Text(text = code) }, onClick = {
                                        selectedCurrency = code
                                        userCurrencyOverride = true
                                        currencyMenu = false
                                    })
                                }
                            }
                        }
                    }
                    // 第二行：左 记账簿图标 + 功能图标；右 账户文字按钮
                    Spacer(Modifier.height(0.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.showLedgerSelector() }) { Icon(Icons.Default.MenuBook, contentDescription = "选择记账簿") }
                            IconButton(onClick = { viewModel.showDateTimePicker() }) { Icon(Icons.Default.DateRange, contentDescription = "选择日期") }
                            IconButton(onClick = { scope.launch { snackbarHostState.showSnackbar("报销功能开发中") } }) { Icon(Icons.Default.Receipt, contentDescription = "报销") }
                            IconButton(onClick = { scope.launch { snackbarHostState.showSnackbar("图片功能开发中") } }) { Icon(Icons.Default.Image, contentDescription = "图片") }
                            IconButton(onClick = { scope.launch { snackbarHostState.showSnackbar("标记功能开发中") } }) { Icon(Icons.Default.Label, contentDescription = "标记") }
                        }
                        // 账户选择（轻量下拉菜单）
                        Box {
                            var accountMenu by remember { mutableStateOf(false) }
                            TextButton(onClick = { accountMenu = true }) {
                                Text(text = uiState.selectedAccount?.name ?: "现金")
                            }
                            DropdownMenu(
                                expanded = accountMenu,
                                onDismissRequest = { accountMenu = false }
                            ) {
                                uiState.accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(text = acc.name) },
                                        onClick = {
                                            viewModel.selectAccount(acc)
                                            if (!userCurrencyOverride) {
                                                selectedCurrency = acc.currency
                                            }
                                            accountMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 数字键盘（固定在底部）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = adjustmentParams.keypadHorizontalPadding.dp,
                            end = adjustmentParams.keypadHorizontalPadding.dp,
                            bottom = 10.dp
                        )
                ) {
                    ProductionNumberKeypad(
                        onNumberClick = { number ->
                            val currentAmount = uiState.amountText
                            val newAmount = if (currentAmount == "0.0" || currentAmount == "0") {
                                number
                            } else {
                                currentAmount + number
                            }
                            viewModel.updateAmount(newAmount)
                        },
                        onDotClick = {
                            val currentAmount = uiState.amountText
                            if (!currentAmount.contains(".")) {
                                viewModel.updateAmount(currentAmount + ".")
                            }
                        },
                        onDeleteClick = {
                            val currentAmount = uiState.amountText
                            if (currentAmount.length > 1) {
                                viewModel.updateAmount(currentAmount.dropLast(1))
                            } else {
                                viewModel.updateAmount("0.0")
                            }
                        },
                        onPlusClick = { /* TODO: 加法功能 */ },
                        onMinusClick = { /* TODO: 减法功能 */ },
                        onAgainClick = { /* TODO: 再记功能 */ },
                        onSaveClick = {
                            println("🎯 [UI] 用户点击保存按钮")
                            scope.launch {
                                println("🚀 [UI] 开始调用viewModel.saveTransaction")
                                viewModel.saveTransaction {
                                    println("✅ [UI] saveTransaction成功回调，准备导航")
                                    // 确保导航操作在主线程中执行
                                    scope.launch(Dispatchers.Main) {
                                        println("📍 [UI] 在主线程中执行导航")
                                        navController.navigate("ledger") {
                                            popUpTo("ledger") { inclusive = false }
                                        }
                                        println("📍 [UI] 导航到ledger页面完成")
                                    }
                                }
                            }
                        },
                        params = adjustmentParams
                    )
                }
            }
        }
        
        // 加载指示器
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
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
        
        // 记账簿选择器对话框
        LedgerSelectorDialog(
            isVisible = uiState.showLedgerSelector,
            ledgers = uiState.ledgers,
            selectedLedgerId = uiState.selectedLedger?.id,
            onLedgerSelected = viewModel::selectLedger,
            onDismiss = viewModel::hideLedgerSelector
        )
        
        // 同步目标选择器对话框
        SyncTargetSelectorDialog(
            isVisible = uiState.showLinkTargetSelector,
            availableTargets = uiState.availableLinkTargets,
            selectedTargets = uiState.selectedSyncTargets,
            onTargetToggle = viewModel::toggleSyncTarget,
            onSelectAll = viewModel::selectAllSyncTargets,
            onClearAll = viewModel::clearAllSyncTargets,
            onConfirm = viewModel::hideLinkTargetSelector,
            onDismiss = viewModel::hideLinkTargetSelector
        )
        
        // 转出账户选择器对话框
        if (uiState.showFromAccountPicker) {
            AccountPickerDialog(
                title = "选择转出账户",
                accounts = uiState.accounts,
                selectedAccount = uiState.fromAccount,
                onAccountSelected = { account ->
                    viewModel.setFromAccount(account)
                },
                onDismiss = { viewModel.hideFromAccountPicker() }
            )
        }
        
        // 转入账户选择器对话框
        if (uiState.showToAccountPicker) {
            AccountPickerDialog(
                title = "选择转入账户",
                accounts = uiState.accounts.filter { it.id != uiState.fromAccount?.id }, // 过滤掉转出账户
                selectedAccount = uiState.toAccount,
                onAccountSelected = { account ->
                    viewModel.setToAccount(account)
                },
                onDismiss = { viewModel.hideToAccountPicker() }
            )
        }
        
        // 简化的日期时间选择器对话框
        if (uiState.showDateTimePicker) {
            SimpleDateTimePickerDialog(
                selectedDate = uiState.selectedDate,
                selectedTime = uiState.selectedTime,
                onDateSelected = viewModel::updateDate,
                onTimeSelected = viewModel::updateTime,
                onDismiss = viewModel::hideDateTimePicker,
                enableTimeSelection = uiState.enableTimeRecording  // 传递设置状态
            )
        }
    }
}

// === 转账账户选择器对话框 ===
@Composable
fun AccountPickerDialog(
    title: String,
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(accounts) { account ->
                    AccountItemCard(
                        account = account,
                        isSelected = selectedAccount?.id == account.id,
                        onSelected = {
                            onAccountSelected(account)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun AccountItemCard(
    account: Account,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.type.icon,
                    style = MaterialTheme.typography.titleLarge
                )
                Column {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (account.type == AccountType.CREDIT_CARD) {
                        Text(
                            text = "可用: ¥${String.format("%.2f", account.availableCreditYuan ?: 0.0)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DesignTokens.BrandColors.Success
                        )
                    } else {
                        Text(
                            text = "¥${String.format("%.2f", account.balanceYuan)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (account.isDefault) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "默认",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

// === 数据类定义 ===
data class LayoutAdjustmentParams(
    // === 分类图标区域 ===
    val categoryIconSize: Float = 25.930233f,
    val categoryHorizontalSpacing: Float = 15.732536f,
    val categoryVerticalSpacing: Float = 20.170633f,
    val categoryGridPadding: Float = 0.0f,
    val categoryTextSize: Float = 10.799782f,
    val categoryCardCornerRadius: Float = 8.0f,
    val categoryCardPadding: Float = 4.0f,
    val gridColumnCount: Int = 6,
    
    // === Tab切换区域 ===
    val tabRowHeight: Float = 40.0f,
    val tabRowWidth: Float = 200.0f,
    val tabCornerRadius: Float = 8.0f,
    val tabVerticalPadding: Float = 8.0f,
    
    // === 输入区域布局 ===
    val inputAreaHeight: Float = 315.4261f,
    val inputAreaCornerRadius: Float = 0.0f,
    val inputAreaPadding: Float = 0.0f,
    
    // === 备注区域细节 ===
    val noteFieldTopPadding: Float = 0.0f,
    val noteFieldBottomPadding: Float = 0.0f,
    val noteFieldHorizontalPadding: Float = 0.0f,
    val noteFieldContentPadding: Float = 0.0f,
    val noteTextSize: Float = 14.0f,
    val noteToAmountSpacing: Float = 0.0f,
    
    // === 金额显示区域 ===
    val amountTextSize: Float = 25.841871f,
    val amountTextPadding: Float = 15.795361f,
    val accountTextSize: Float = 15.110469f,
    val accountTextLeftPadding: Float = 15.944222f,
    val accountToNoteSpacing: Float = 0.0f,
    val amountToKeypadSpacing: Float = 0.0f,
    
    // === 键盘区域 ===
    val keypadButtonSize: Float = 48.0f,
    val keypadButtonSpacing: Float = 8.0f,
    val keypadRowSpacing: Float = 3.4232678f,
    val keypadButtonCornerRadius: Float = 10.182958f,
    val keypadTextSize: Float = 16.85329f,
    val keypadBottomPadding: Float = 16.0f,
    val keypadHorizontalPadding: Float = 10.775346f,
    
    // === 整体布局权重 ===
    val categoryGridWeight: Float = 1.0f
)


// === 组件定义 ===
@Composable
private fun ProductionCategoryCard(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    params: LayoutAdjustmentParams,
    iconDisplayMode: IconDisplayMode
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp, 
                DesignTokens.BrandColors.Ledger
            )
        } else null,
        shape = RoundedCornerShape(params.categoryCardCornerRadius.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(params.categoryCardPadding.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DynamicCategoryIcon(
                category = category,
                iconDisplayMode = iconDisplayMode,
                size = params.categoryIconSize.dp,
                tint = if (isSelected) {
                    DesignTokens.BrandColors.Ledger
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = params.categoryTextSize.sp
                ),
                textAlign = TextAlign.Center,
                color = if (isSelected) {
                    DesignTokens.BrandColors.Ledger
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun ProductionNumberKeypad(
    onNumberClick: (String) -> Unit,
    onDotClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPlusClick: () -> Unit,
    onMinusClick: () -> Unit,
    onAgainClick: () -> Unit,
    onSaveClick: () -> Unit,
    params: LayoutAdjustmentParams
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(params.keypadRowSpacing.dp)
    ) {
        // 第一行：1 2 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(params.keypadButtonSpacing.dp)
        ) {
            ProductionKeypadButton("1", Modifier.weight(1f), params) { onNumberClick("1") }
            ProductionKeypadButton("2", Modifier.weight(1f), params) { onNumberClick("2") }
            ProductionKeypadButton("3", Modifier.weight(1f), params) { onNumberClick("3") }
            ProductionKeypadIconButton(Icons.AutoMirrored.Filled.ArrowBack, "删除", Modifier.weight(1f), params) { onDeleteClick() }
        }
        
        // 第二行：4 5 6 -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(params.keypadButtonSpacing.dp)
        ) {
            ProductionKeypadButton("4", Modifier.weight(1f), params) { onNumberClick("4") }
            ProductionKeypadButton("5", Modifier.weight(1f), params) { onNumberClick("5") }
            ProductionKeypadButton("6", Modifier.weight(1f), params) { onNumberClick("6") }
            ProductionKeypadButton("−", Modifier.weight(1f), params) { onMinusClick() }
        }
        
        // 第三行：7 8 9 +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(params.keypadButtonSpacing.dp)
        ) {
            ProductionKeypadButton("7", Modifier.weight(1f), params) { onNumberClick("7") }
            ProductionKeypadButton("8", Modifier.weight(1f), params) { onNumberClick("8") }
            ProductionKeypadButton("9", Modifier.weight(1f), params) { onNumberClick("9") }
            ProductionKeypadButton("+", Modifier.weight(1f), params) { onPlusClick() }
        }
        
        // 第四行：再记 0 . 保存
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(params.keypadButtonSpacing.dp)
        ) {
            ProductionKeypadButton("再记", Modifier.weight(1f), params) { onAgainClick() }
            ProductionKeypadButton("0", Modifier.weight(1f), params) { onNumberClick("0") }
            ProductionKeypadButton(".", Modifier.weight(1f), params) { onDotClick() }
            ProductionKeypadButton("保存", Modifier.weight(1f), params, DesignTokens.BrandColors.Error) { onSaveClick() }
        }
    }
}

@Composable
private fun ProductionKeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    params: LayoutAdjustmentParams,
    buttonColor: Color? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(params.keypadButtonSize.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor ?: MaterialTheme.colorScheme.surface,
            contentColor = if (buttonColor != null) Color.White else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(params.keypadButtonCornerRadius.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = params.keypadTextSize.sp
            ),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProductionKeypadIconButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    params: LayoutAdjustmentParams,
    buttonColor: Color? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(params.keypadButtonSize.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor ?: MaterialTheme.colorScheme.surface,
            contentColor = if (buttonColor != null) Color.White else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(params.keypadButtonCornerRadius.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size((params.keypadTextSize + 2).dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDateTimePickerDialog(
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    enableTimeSelection: Boolean = false  // 新增参数：控制是否显示时间选择
) {
    // 只有2种模式：月历、下拉框
    var pickerMode by remember { mutableStateOf(DatePickerMode.CALENDAR) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                if (enableTimeSelection) "选择日期时间" else "选择日期",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = DesignTokens.BrandColors.Ledger
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 模式切换器（只有2个选项）
                Surface(
                    shape = RoundedCornerShape(DesignTokens.BorderRadius.medium),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DatePickerModeTab(
                            text = "月历",
                            isSelected = pickerMode == DatePickerMode.CALENDAR,
                            onClick = { pickerMode = DatePickerMode.CALENDAR },
                            modifier = Modifier.weight(1f)
                        )
                        DatePickerModeTab(
                            text = "下拉框",
                            isSelected = pickerMode == DatePickerMode.DROPDOWN,
                            onClick = { pickerMode = DatePickerMode.DROPDOWN },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // 日期选择区域
                when (pickerMode) {
                    DatePickerMode.CALENDAR -> {
                        CalendarModeContent(
                            selectedDate = selectedDate,
                            onDateSelected = onDateSelected
                        )
                    }
                    DatePickerMode.DROPDOWN -> {
                        DropdownDateSelector(
                            selectedDate = selectedDate,
                            onDateSelected = onDateSelected
                        )
                    }
                    else -> {}  // 不再支持其他模式
                }
                
                // 条件显示时间选择区域
                if (enableTimeSelection) {
                    TimeSelector(
                        selectedTime = selectedTime,
                        onShowTimePicker = { showTimePicker = true }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DesignTokens.BrandColors.Ledger
                )
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
    
    // 时间选择器对话框（只在开启时间记录时显示）
    if (showTimePicker && enableTimeSelection) {
        EnhancedTimePickerDialog(
            selectedTime = selectedTime,
            onTimeSelected = { time ->
                onTimeSelected(time)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

// 选择器模式枚举（只保留2种模式）
private enum class DatePickerMode {
    CALENDAR, DROPDOWN
}

@Composable
private fun DatePickerModeTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
            textAlign = TextAlign.Center
        )
    }
}

// 月历模式内容：集成快捷选择和日历网格
@Composable
private fun CalendarModeContent(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val quickDateOptions = remember(today) {
        listOf(
            "今天" to today,
            "昨天" to today.minus(1, DateTimeUnit.DAY),
            "前天" to today.minus(2, DateTimeUnit.DAY),
            "3天前" to today.minus(3, DateTimeUnit.DAY),
            "一周前" to today.minus(7, DateTimeUnit.DAY)
        )
    }
    
    var currentYearMonth by remember { 
        mutableStateOf(java.time.YearMonth.of(selectedDate.year, selectedDate.monthNumber))
    }
    var tempSelectedDate by remember { mutableStateOf(selectedDate) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 快捷选择按钮组（直接嵌入）
        Text(
            text = "快捷选择",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(80.dp)
        ) {
            items(quickDateOptions) { (label, date) ->
                FilterChip(
                    selected = selectedDate == date,
                    onClick = { 
                        onDateSelected(date)
                        currentYearMonth = java.time.YearMonth.of(date.year, date.monthNumber)
                        tempSelectedDate = date
                    },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f),
                        selectedLabelColor = DesignTokens.BrandColors.Ledger
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }
        }
        
        // 月历网格（直接显示，无需额外点击）
        Text(
            text = "月历选择",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Surface(
            shape = RoundedCornerShape(DesignTokens.BorderRadius.medium),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // 月份导航
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "上个月",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${currentYearMonth.year}年${currentYearMonth.monthValue}月",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "下个月",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 日历网格
                CompactCalendarGrid(
                    yearMonth = currentYearMonth,
                    selectedDate = tempSelectedDate,
                    onDateSelected = { 
                        tempSelectedDate = it
                        onDateSelected(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun DropdownDateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var showYearMenu by remember { mutableStateOf(false) }
    var showMonthMenu by remember { mutableStateOf(false) }
    var showDayMenu by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "下拉框选择",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // 美化的下拉框容器
        Surface(
            shape = RoundedCornerShape(DesignTokens.BorderRadius.medium),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                // 年份下拉框 - 改进版
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        onClick = { showYearMenu = true },
                        shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedDate.year}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                Icons.Default.UnfoldMore,
                                contentDescription = null,
                                tint = DesignTokens.BrandColors.Ledger,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showYearMenu,
                        onDismissRequest = { showYearMenu = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        for (year in (selectedDate.year - 10)..(selectedDate.year + 10)) {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "${year}年",
                                        fontWeight = if (year == selectedDate.year) FontWeight.Bold else FontWeight.Normal,
                                        color = if (year == selectedDate.year) DesignTokens.BrandColors.Ledger else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    val newDate = try {
                                        LocalDate(year, selectedDate.month, selectedDate.dayOfMonth)
                                    } catch (e: Exception) {
                                        val tempDate = LocalDate(year, selectedDate.month, 1)
                                        val maxDayInMonth = tempDate.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth
                                        LocalDate(year, selectedDate.month, minOf(selectedDate.dayOfMonth, maxDayInMonth))
                                    }
                                    onDateSelected(newDate)
                                    showYearMenu = false
                                }
                            )
                        }
                    }
                }
                
                // 月份下拉框 - 改进版
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        onClick = { showMonthMenu = true },
                        shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedDate.monthNumber}月",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                Icons.Default.UnfoldMore,
                                contentDescription = null,
                                tint = DesignTokens.BrandColors.Ledger,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showMonthMenu,
                        onDismissRequest = { showMonthMenu = false },
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        for (month in 1..12) {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "${month}月",
                                        fontWeight = if (month == selectedDate.monthNumber) FontWeight.Bold else FontWeight.Normal,
                                        color = if (month == selectedDate.monthNumber) DesignTokens.BrandColors.Ledger else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    val newDate = try {
                                        LocalDate(selectedDate.year, month, selectedDate.dayOfMonth)
                                    } catch (e: Exception) {
                                        val tempDate = LocalDate(selectedDate.year, month, 1)
                                        val maxDayInMonth = tempDate.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth
                                        LocalDate(selectedDate.year, month, minOf(selectedDate.dayOfMonth, maxDayInMonth))
                                    }
                                    onDateSelected(newDate)
                                    showMonthMenu = false
                                }
                            )
                        }
                    }
                }
                
                // 日期下拉框 - 改进版
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        onClick = { showDayMenu = true },
                        shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedDate.dayOfMonth}日",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                Icons.Default.UnfoldMore,
                                contentDescription = null,
                                tint = DesignTokens.BrandColors.Ledger,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showDayMenu,
                        onDismissRequest = { showDayMenu = false },
                        modifier = Modifier.heightIn(max = 450.dp)
                    ) {
                        val tempDate = LocalDate(selectedDate.year, selectedDate.month, 1)
                        val daysInMonth = tempDate.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth
                        for (day in 1..daysInMonth) {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "${day}日",
                                        fontWeight = if (day == selectedDate.dayOfMonth) FontWeight.Bold else FontWeight.Normal,
                                        color = if (day == selectedDate.dayOfMonth) DesignTokens.BrandColors.Ledger else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onDateSelected(LocalDate(selectedDate.year, selectedDate.month, day))
                                    showDayMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSelector(
    selectedTime: LocalTime,
    onShowTimePicker: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "时间选择",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        OutlinedCard(
            onClick = onShowTimePicker,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "时间",
                        tint = DesignTokens.BrandColors.Ledger
                    )
                    Column {
                        Text(
                            text = "点击设置时间",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatTime(selectedTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "设置",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// 紧凑版日历网格
@Composable
private fun CompactCalendarGrid(
    yearMonth: java.time.YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7
    
    val calendarDays = remember(yearMonth) {
        val days = mutableListOf<LocalDate?>()
        repeat(firstDayOffset) { days.add(null) }
        for (day in 1..daysInMonth) {
            days.add(LocalDate(yearMonth.year, yearMonth.monthValue, day))
        }
        days
    }
    
    Column {
        // 星期标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (day in listOf("六", "日")) {
                        DesignTokens.BrandColors.Warning
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        
        // 日期网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.height(180.dp)
        ) {
            items(calendarDays) { date ->
                if (date != null) {
                    CompactCalendarDateCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == Clock.System.todayIn(TimeZone.currentSystemDefault()),
                        onClick = { onDateSelected(date) }
                    )
                } else {
                    Box(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
private fun CompactCalendarDateCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignTokens.BorderRadius.small))
            .background(
                when {
                    isSelected -> DesignTokens.BrandColors.Ledger
                    isToday -> DesignTokens.BrandColors.Ledger.copy(alpha = 0.2f)
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            fontWeight = when {
                isSelected || isToday -> FontWeight.Bold
                else -> FontWeight.Normal
            },
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isToday -> DesignTokens.BrandColors.Ledger
                date.dayOfWeek.value in listOf(6, 7) -> DesignTokens.BrandColors.Warning
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun SelectedDateTimePreview(
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    showTime: Boolean = true  // 新增参数：控制是否显示时间
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DesignTokens.BrandColors.Ledger.copy(alpha = 0.05f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "已选择",
                style = MaterialTheme.typography.labelMedium,
                color = DesignTokens.BrandColors.Ledger
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (showTime) Arrangement.SpaceBetween else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatDate(selectedDate),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = getDateDescription(selectedDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 只在开启时间记录时显示时间
                if (showTime) {
                    Text(
                        text = formatTime(selectedTime),
                        style = MaterialTheme.typography.titleLarge,
                        color = DesignTokens.BrandColors.Ledger,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// 增强型时间选择器对话框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedTimePickerDialog(
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute,
        is24Hour = true
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(DesignTokens.BorderRadius.large),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = "选择时间",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = DesignTokens.BrandColors.Ledger
                )
                
                // 当前时间显示
                Surface(
                    shape = RoundedCornerShape(DesignTokens.BorderRadius.medium),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%02d", timePickerState.hour),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            text = String.format("%02d", timePickerState.minute),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                // 时间选择器
                Surface(
                    shape = RoundedCornerShape(DesignTokens.BorderRadius.medium),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier.padding(16.dp),
                        colors = TimePickerDefaults.colors(
                            selectorColor = DesignTokens.BrandColors.Ledger,
                            containerColor = Color.Transparent
                        )
                    )
                }
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
                            onTimeSelected(now)
                            onDismiss()
                        }
                    ) {
                        Text("现在")
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("取消")
                        }
                        Button(
                            onClick = {
                                onTimeSelected(LocalTime(timePickerState.hour, timePickerState.minute))
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DesignTokens.BrandColors.Ledger
                            )
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(date: LocalDate): String {
    return "${date.year}年${date.monthNumber}月${date.dayOfMonth}日"
}

private fun formatTime(time: LocalTime): String {
    return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

private fun getDateDescription(date: LocalDate): String {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return when {
        date == today -> "今天"
        date == today.minus(1, DateTimeUnit.DAY) -> "昨天"
        date == today.minus(2, DateTimeUnit.DAY) -> "前天"
        date == today.plus(1, DateTimeUnit.DAY) -> "明天"
        date == today.plus(2, DateTimeUnit.DAY) -> "后天"
        else -> {
            val dayOfWeek = when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "周一"
                DayOfWeek.TUESDAY -> "周二"
                DayOfWeek.WEDNESDAY -> "周三"
                DayOfWeek.THURSDAY -> "周四"
                DayOfWeek.FRIDAY -> "周五"
                DayOfWeek.SATURDAY -> "周六"
                DayOfWeek.SUNDAY -> "周日"
            }
            dayOfWeek
        }
    }
}
