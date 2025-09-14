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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.ExperimentalFoundationApi
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    transactionId: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: AddTransactionViewModel = hiltViewModel(),
    uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
) {
    println("[AddTransactionScreen] created")
    println("   - transactionId: '$transactionId'")
    println("   - 是否为编辑模式: ${!transactionId.isNullOrBlank()}")
    println("   - viewModel: $viewModel")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    // 统一系统返回：优先回调，否则向上返回
    BackHandler { onNavigateBack?.invoke() ?: navController.navigateUp() }
    // 币种：优先账户币种，其次默认 CNY，支持本地覆盖
    val accountCurrency = uiState.selectedAccount?.currency
    var userCurrencyOverride by rememberSaveable { mutableStateOf(false) }
    var selectedCurrency by rememberSaveable { mutableStateOf(accountCurrency ?: "CNY") }
    LaunchedEffect(accountCurrency) {
        if (!userCurrencyOverride) {
            selectedCurrency = accountCurrency ?: "CNY"
        }
    }
    
    // 布局微调参数
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
        
        // === Tab鍒囨崲鍖哄煙 ===
        tabRowHeight = 40.0f,
        tabRowWidth = 200.0f,
        tabCornerRadius = 8.0f,
        tabVerticalPadding = 8.0f,
        
        // === 杈撳叆鍖哄煙甯冨眬 ===
        inputAreaHeight = 315.4261f,
        inputAreaCornerRadius = 0.0f,
        inputAreaPadding = 0.0f,
        
        // === 澶囨敞鍖哄煙缁嗚妭 ===
        noteFieldTopPadding = 0.0f,
        noteFieldBottomPadding = 0.0f,
        noteFieldHorizontalPadding = 0.0f,
        noteFieldContentPadding = 0.0f,
        noteTextSize = 14.0f,
        noteToAmountSpacing = 0.0f,
        
        // === 閲戦鏄剧ず鍖哄煙 ===
        amountTextSize = 25.841871f,
        amountTextPadding = 15.795361f,
        accountTextSize = 15.110469f,
        accountTextLeftPadding = 15.944222f,
        accountToNoteSpacing = 0.0f,
        amountToKeypadSpacing = 0.0f,
        
        // === 閿洏鍖哄煙 ===
        keypadButtonSize = 48.0f,
        keypadButtonSpacing = 8.0f,
        keypadRowSpacing = 3.4232678f,
        keypadButtonCornerRadius = 10.182958f,
        keypadTextSize = 16.85329f,
        keypadBottomPadding = 16.0f,
        keypadHorizontalPadding = 10.775346f,
        
        // === 鏁翠綋甯冨眬鏉冮噸 ===
        categoryGridWeight = 1.0f
    )

    // 使用实际的分类数据（DAO 已过滤隐藏分类）
    val currentCategories = remember(uiState.categoryGroups) {
        uiState.categoryGroups.map { it.parent }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 头部：返回键与 收入/支出/转账 切换在同一行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 左侧：返回键
                IconButton(onClick = { onNavigateBack?.invoke() ?: navController.navigateUp() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, 
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 中间：收支/转账 切换
                TabRow(
                    selectedTabIndex = when (uiState.transactionType) {
                        TransactionType.EXPENSE -> 0
                        TransactionType.INCOME -> 1
                        TransactionType.TRANSFER -> 2
                        TransactionType.ALL -> 0 // 默认显示支出 Tab
                        else -> 0 // 默认值
                    },
                    modifier = Modifier
                        .width((adjustmentParams.tabRowWidth * 1.5f).dp) // 增加宽度以容纳三个 Tab
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
                
                // 右侧：占位，保持布局对齐
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
                // 支出/收入模式：展示分类网格（支持卡内展开子分类）
                var expandedParentId by rememberSaveable { mutableStateOf<String?>(null) }
                val parentIndexMap = remember(currentCategories) {
                    currentCategories.withIndex().associate { it.value.id to it.index }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(adjustmentParams.gridColumnCount),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(adjustmentParams.categoryGridWeight)
                        .padding(horizontal = adjustmentParams.categoryGridPadding.dp),
                    verticalArrangement = Arrangement.spacedBy(adjustmentParams.categoryVerticalSpacing.dp),
                    horizontalArrangement = Arrangement.spacedBy(adjustmentParams.categoryHorizontalSpacing.dp)
                ) {
                    val cols = adjustmentParams.gridColumnCount
                    val total = currentCategories.size
                    var i = 0
                    while (i < total) {
                        val end = minOf(i + cols - 1, total - 1)
                        // 一行内的父分类卡片
                        for (j in i..end) {
                            val category = currentCategories[j]
                            item {
                                val group = uiState.categoryGroups.firstOrNull { it.parent.id == category.id }
                                val hasChildren = group?.children?.isNotEmpty() == true
                                val isSelected = uiState.selectedCategoryInfo?.let { sel ->
                                    // 仅当真正选中了父分类本身时，高亮父分类
                                    sel.categoryId == category.id
                                } ?: false
                                ProductionCategoryCard(
                                    category = category,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (hasChildren) {
                                            if (expandedParentId == category.id) {
                                                // 已展开：仅切换为收起，不改变当前选中（可能是父或子）
                                                expandedParentId = null
                                            } else {
                                                // 未展开：选中父分类并展开
                                                viewModel.selectCategory(category)
                                                expandedParentId = category.id
                                            }
                                        } else {
                                            // 无子类：直接选择父分类
                                            expandedParentId = null
                                            viewModel.selectCategory(category)
                                        }
                                    },
                                    params = adjustmentParams,
                                    iconDisplayMode = uiPreferences.iconDisplayMode
                                )
                            }
                        }
                        // 鑻ュ綋鍓嶈鍖呭惈灞曞紑鐨勭埗鍒嗙被锛屽垯鍦ㄨ琛屼笅鏂规彃鍏ヤ竴涓法鍒楃殑瀛愬垎绫绘
                        val rowContainsExpanded = expandedParentId?.let { pid ->
                            val idx = parentIndexMap[pid]
                            idx != null && idx in i..end
                        } ?: false
                        if (rowContainsExpanded) {
                            item(span = { GridItemSpan(cols) }) {
                                val bringIntoViewRequester = remember { BringIntoViewRequester() }
                                val children = uiState.categoryGroups.firstOrNull { it.parent.id == expandedParentId }?.children ?: emptyList()
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                        .bringIntoViewRequester(bringIntoViewRequester)
                                ) {
                                    // 全宽子分类网格（自动换行，无横向滚动）
                                    FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        children.forEach { child ->
                                            CategoryChip(
                                                category = child,
                                                isSelected = uiState.selectedCategoryInfo?.categoryId == child.id,
                                                onClick = {
                                                    // 选中子分类，但不收起卡片，便于查看高频状态
                                                    viewModel.selectCategory(child)
                                                }
                                            )
                                        }
                                    }
                                }
                                // 展开时自动滚动，确保卡片完全可见
                                LaunchedEffect(expandedParentId) {
                                    bringIntoViewRequester.bringIntoView()
                                }
                            }
                        }
                        i = end + 1
                    }
                }
            }
            
            // 底部：输入区域（方案 B：上部可滚动 + 底部固定键盘）
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
                            placeholder = { Text("在此输入备注...", fontSize = adjustmentParams.noteTextSize.sp) },
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
                                Icon(Icons.Default.UnfoldMore, contentDescription = "閫夋嫨甯佺", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    // 绗簩琛岋細宸?璁拌处绨垮浘鏍?+ 鍔熻兘鍥炬爣锛涘彸 璐︽埛鏂囧瓧鎸夐挳
                    Spacer(Modifier.height(0.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.showLedgerSelector() }) { Icon(Icons.Default.MenuBook, contentDescription = "choose ledger") }
                            IconButton(onClick = { viewModel.showDateTimePicker() }) { Icon(Icons.Default.DateRange, contentDescription = "choose date") }
                            IconButton(onClick = { scope.launch { snackbarHostState.showSnackbar("reimburse WIP") } }) { Icon(Icons.Default.Receipt, contentDescription = "reimburse") }
                            IconButton(onClick = { scope.launch { snackbarHostState.showSnackbar("image WIP") } }) { Icon(Icons.Default.Image, contentDescription = "image") }
                        }
                        // 璐︽埛閫夋嫨锛堣交閲忎笅鎷夎彍鍗曪級
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
                    // 计算保存禁用提示文案
                    val disabledHint: String? = if (!uiState.canSave) {
                        when {
                            uiState.amountError != null -> uiState.amountError
                            uiState.amountText.isBlank() -> "请输入有效金额"
                            uiState.amountText.toDoubleOrNull()?.let { it <= 0 } == true -> "金额必须大于0"
                            uiState.selectedLedger == null -> "请选择账本"
                            uiState.transactionType == TransactionType.TRANSFER && uiState.fromAccount == null -> "请选择转出账户"
                            uiState.transactionType == TransactionType.TRANSFER && uiState.toAccount == null -> "请选择转入账户"
                            uiState.transactionType == TransactionType.TRANSFER && uiState.fromAccount == uiState.toAccount -> "转出和转入账户不能相同"
                            uiState.transactionType != TransactionType.TRANSFER && uiState.selectedAccount == null -> "请选择账户"
                            uiState.transactionType != TransactionType.TRANSFER && uiState.selectedCategoryInfo == null -> "请选择分类"
                            else -> null
                        }
                    } else null

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
                            println("[UI] 用户点击保存按钮")
                            scope.launch {
                                println("[UI] 开始调用 viewModel.saveTransaction")
                                viewModel.saveTransaction {
                                    println("[UI] saveTransaction 成功，准备返回上一页")
                                    // onSuccess 可能在 IO 线程回调；切回主线程执行导航
                                    scope.launch {
                                        onNavigateBack?.invoke() ?: navController.navigateUp()
                                    }
                                }
                            }
                        },
                        saveEnabled = uiState.canSave,
                        disabledHint = disabledHint,
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
        
        // 璁拌处绨块€夋嫨鍣ㄥ璇濇
        LedgerSelectorDialog(
            isVisible = uiState.showLedgerSelector,
            ledgers = uiState.ledgers,
            selectedLedgerId = uiState.selectedLedger?.id,
            onLedgerSelected = viewModel::selectLedger,
            onDismiss = viewModel::hideLedgerSelector
        )
        
        // 鍚屾鐩爣閫夋嫨鍣ㄥ璇濇
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
                enableTimeSelection = uiState.enableTimeRecording  // 传入设置状态
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
    
    // === Tab 切换区域 ===
    val tabRowHeight: Float = 40.0f,
    val tabRowWidth: Float = 200.0f,
    val tabCornerRadius: Float = 8.0f,
    val tabVerticalPadding: Float = 8.0f,
    
    // === 输入区域布局 ===
    val inputAreaHeight: Float = 315.4261f,
    val inputAreaCornerRadius: Float = 0.0f,
    val inputAreaPadding: Float = 0.0f,
    
    // === 备注区域节点 ===
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
    saveEnabled: Boolean,
    disabledHint: String?,
    params: LayoutAdjustmentParams
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(params.keypadRowSpacing.dp)
    ) {
        // 第一行：1 2 3 删除
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
            ProductionKeypadButton("-", Modifier.weight(1f), params) { onMinusClick() }
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
            ProductionKeypadButton(
                text = "保存",
                modifier = Modifier.weight(1f),
                params = params,
                buttonColor = DesignTokens.BrandColors.Error,
                enabled = saveEnabled
            ) { onSaveClick() }
        }
        if (!saveEnabled && !disabledHint.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = disabledHint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun ProductionKeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    params: LayoutAdjustmentParams,
    buttonColor: Color? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(params.keypadButtonSize.dp),
        enabled = enabled,
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
    // 仅有 2 种模式：月历 / 下拉框
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
                // 妯″紡鍒囨崲鍣紙鍙湁2涓€夐」锛?
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
                            text = "鏈堝巻",
                            isSelected = pickerMode == DatePickerMode.CALENDAR,
                            onClick = { pickerMode = DatePickerMode.CALENDAR },
                            modifier = Modifier.weight(1f)
                        )
                        DatePickerModeTab(
                            text = "下拉",
                            isSelected = pickerMode == DatePickerMode.DROPDOWN,
                            onClick = { pickerMode = DatePickerMode.DROPDOWN },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // 鏃ユ湡閫夋嫨鍖哄煙
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
                    else -> {}  // 涓嶅啀鏀寔鍏朵粬妯″紡
                }
                
                // 鏉′欢鏄剧ず鏃堕棿閫夋嫨鍖哄煙
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
    
    // 鏃堕棿閫夋嫨鍣ㄥ璇濇锛堝彧鍦ㄥ紑鍚椂闂磋褰曟椂鏄剧ず锛?
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

// 閫夋嫨鍣ㄦā寮忔灇涓撅紙鍙繚鐣?绉嶆ā寮忥級
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

// 鏈堝巻妯″紡鍐呭锛氶泦鎴愬揩鎹烽€夋嫨鍜屾棩鍘嗙綉鏍?
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
        // 蹇嵎閫夋嫨鎸夐挳缁勶紙鐩存帴宓屽叆锛?
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
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "prev month",
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
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "next month",
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
            text = "下拉选择",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // 下拉选择容器
        Surface(
            shape = RoundedCornerShape(DesignTokens.BorderRadius.medium),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                // 年份下拉框
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
                        val startYear = selectedDate.year - 50
                        val endYear = selectedDate.year + 50
                        for (year in startYear..endYear) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${year}年",
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
                
                // 月份下拉框
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
                                        text = "${month}月",
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
                
                // 日期下拉框
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
                                        text = "${day}日",
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
            text = "鏃堕棿閫夋嫨",
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
                        contentDescription = "鏃堕棿",
                        tint = DesignTokens.BrandColors.Ledger
                    )
                    Column {
                        Text(
                            text = "鐐瑰嚮璁剧疆鏃堕棿",
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
                    contentDescription = "璁剧疆",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// 绱у噾鐗堟棩鍘嗙綉鏍?
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
        // 鏄熸湡鏍囬
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
            weekDays.forEachIndexed { idx, day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (idx >= 5) {
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
    showTime: Boolean = true // 仅在开启时间记录时显示时间
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
                
                // 仅在开启时间记录时显示时间
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


// 澧炲己鍨嬫椂闂撮€夋嫨鍣ㄥ璇濇
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
            when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "周一"
                DayOfWeek.TUESDAY -> "周二"
                DayOfWeek.WEDNESDAY -> "周三"
                DayOfWeek.THURSDAY -> "周四"
                DayOfWeek.FRIDAY -> "周五"
                DayOfWeek.SATURDAY -> "周六"
                DayOfWeek.SUNDAY -> "周日"
            }
        }
    }
}
