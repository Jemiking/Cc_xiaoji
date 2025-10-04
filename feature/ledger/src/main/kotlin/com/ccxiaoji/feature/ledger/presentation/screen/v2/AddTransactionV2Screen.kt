package com.ccxiaoji.feature.ledger.presentation.screen.v2

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.presentation.component.LedgerSelector
import com.ccxiaoji.feature.ledger.presentation.component.LedgerSelectorDialog
import com.ccxiaoji.feature.ledger.presentation.component.SyncTargetSelectorDialog
// Date/time dialog implemented locally for V2
import com.ccxiaoji.feature.ledger.presentation.component.DynamicCategoryIcon
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AddTransactionViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionType
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionV2Screen(
    navController: NavController,
    transactionId: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: AddTransactionViewModel = hiltViewModel(),
    uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiPrefs by uiStyleViewModel.uiPreferences.collectAsState()

    // analytics: open
    LaunchedEffect(Unit) {
        android.util.Log.d("AddTxnV2", "add_txn_open entry=ledger uiStyle=${uiPrefs.uiStyle} ")
    }

    BackHandler { onNavigateBack?.invoke() ?: navController.navigateUp() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack?.invoke() ?: navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                title = {
                    // 记账类型切换（支出/收入/转账）
                    val selectedIndex = when (uiState.transactionType) {
                        TransactionType.EXPENSE -> 0
                        TransactionType.INCOME -> 1
                        TransactionType.TRANSFER -> 2
                        else -> 0
                    }
                    TabRow(selectedTabIndex = selectedIndex, indicator = {}, divider = {}) {
                        Tab(
                            selected = uiState.transactionType == TransactionType.EXPENSE,
                            onClick = {
                                viewModel.setTransactionType(TransactionType.EXPENSE)
                                android.util.Log.d("AddTxnV2", "add_txn_switch_tab to=expense")
                            }
                        ) { Text("支出", modifier = Modifier.padding(vertical = 6.dp)) }
                        Tab(
                            selected = uiState.transactionType == TransactionType.INCOME,
                            onClick = {
                                viewModel.setTransactionType(TransactionType.INCOME)
                                android.util.Log.d("AddTxnV2", "add_txn_switch_tab to=income")
                            }
                        ) { Text("收入", modifier = Modifier.padding(vertical = 6.dp)) }
                        Tab(
                            selected = uiState.transactionType == TransactionType.TRANSFER,
                            onClick = {
                                viewModel.setTransactionType(TransactionType.TRANSFER)
                                android.util.Log.d("AddTxnV2", "add_txn_switch_tab to=transfer")
                            }
                        ) { Text("转账", modifier = Modifier.padding(vertical = 6.dp)) }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // 账本/日期/同步目标入口 + 账户选择
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LedgerSelector(
                        selectedLedger = uiState.selectedLedger,
                        onClick = {
                            viewModel.showLedgerSelector()
                            android.util.Log.d("AddTxnV2", "add_txn_ledger_change open_selector")
                        },
                        modifier = Modifier.widthIn(max = 200.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        viewModel.showDateTimePicker()
                        android.util.Log.d("AddTxnV2", "add_txn_time_change open_picker")
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                    }
                    if (uiState.hasLinkOptions) {
                        IconButton(onClick = { viewModel.showLinkTargetSelector() }) {
                            Icon(Icons.Default.Link, contentDescription = "选择同步目标")
                        }
                    }
                }
                if (uiState.transactionType == TransactionType.TRANSFER) {
                    // 转账：选择转出/转入账户
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        var fromMenu by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { fromMenu = true }) { Text(uiState.fromAccount?.name ?: "转出账户") }
                            DropdownMenu(expanded = fromMenu, onDismissRequest = { fromMenu = false }) {
                                uiState.accounts.forEach { acc ->
                                    DropdownMenuItem(text = { Text(acc.name) }, onClick = {
                                        viewModel.setFromAccount(acc)
                                        fromMenu = false
                                    })
                                }
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        var toMenu by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { toMenu = true }) { Text(uiState.toAccount?.name ?: "转入账户") }
                            DropdownMenu(expanded = toMenu, onDismissRequest = { toMenu = false }) {
                                uiState.accounts.filter { it.id != uiState.fromAccount?.id }.forEach { acc ->
                                    DropdownMenuItem(text = { Text(acc.name) }, onClick = {
                                        viewModel.setToAccount(acc)
                                        toMenu = false
                                    })
                                }
                            }
                        }
                    }
                } else {
                    // 普通：选择账户（简易文本按钮 + 下拉）
                    var accountMenu by remember { mutableStateOf(false) }
                    Box {
                    TextButton(onClick = {
                        accountMenu = true
                        android.util.Log.d("AddTxnV2", "add_txn_account_change open_menu")
                    }) {
                        Text(text = uiState.selectedAccount?.name ?: "选择账户")
                    }
                    DropdownMenu(expanded = accountMenu, onDismissRequest = { accountMenu = false }) {
                        uiState.accounts.forEach { acc ->
                            DropdownMenuItem(text = { Text(acc.name) }, onClick = {
                                viewModel.selectAccount(acc)
                                accountMenu = false
                                android.util.Log.d("AddTxnV2", "add_txn_account_change accountId=${acc.id}")
                            })
                        }
                    }
                }
            }
            }

            // 分类区（简化：优先展示“常用分类”，否则展示顶层分类与其首个子类）
            Spacer(Modifier.height(8.dp))
            val categories: List<Category> = remember(uiState.categoryGroups, uiState.frequentCategories) {
                val frequent = uiState.frequentCategories
                if (frequent.isNotEmpty()) frequent
                else uiState.categoryGroups.flatMap { group ->
                    if (group.children.isNotEmpty()) group.children else listOf(group.parent)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(categories, key = { it.id }) { category ->
                    CategoryChipV2(
                        category = category,
                        isSelected = uiState.selectedCategoryInfo?.categoryId == category.id,
                        iconDisplayMode = uiPrefs.iconDisplayMode,
                        onClick = {
                            // 若为父类且有子类，优先选择其首个子类
                            val children = uiState.categoryGroups.firstOrNull { it.parent.id == category.id }?.children
                            val target = if (!children.isNullOrEmpty()) children.first() else category
                            viewModel.selectCategory(target)
                            android.util.Log.d("AddTxnV2", "add_txn_select_category categoryId=${target.id}")
                        }
                    )
                }
            }

            // 备注 + 金额区（右侧金额，大字）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = viewModel::updateNote,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("点此输入备注...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (uiState.amountText.isBlank()) "0.00" else uiState.amountText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.isIncome) DesignTokens.BrandColors.Success else DesignTokens.BrandColors.Error
                )
            }

            Spacer(Modifier.height(8.dp))

            // 数字键盘 + 保存按钮
            NumberPadV2(
                onNumber = {
                    viewModel.updateAmount((uiState.amountText.takeIf { it != "0" && it != "0.0" } ?: "") + it)
                    android.util.Log.d("AddTxnV2", "add_txn_amount_change len=${uiState.amountText.length}")
                },
                onDot = {
                    val t = uiState.amountText
                    if (!t.contains('.')) {
                        viewModel.updateAmount(t + ".")
                        android.util.Log.d("AddTxnV2", "add_txn_amount_change dot_added")
                    }
                },
                onBackspace = {
                    val t = uiState.amountText
                    if (t.length > 1) viewModel.updateAmount(t.dropLast(1)) else viewModel.updateAmount("")
                    android.util.Log.d("AddTxnV2", "add_txn_amount_change backspace")
                },
                onPlus = { viewModel.updateAmount(uiState.amountText + "+") },
                onMinus = { viewModel.updateAmount(uiState.amountText + "-") },
                onSave = {
                    android.util.Log.d("AddTxnV2", "add_txn_save_tap canSave=${uiState.canSave} amount=${uiState.amountText} type=${uiState.transactionType}")
                    if (uiState.canSave) {
                        viewModel.saveTransaction {
                            android.util.Log.d("AddTxnV2", "add_txn_save_result success=true")
                            onNavigateBack?.invoke() ?: navController.navigateUp()
                        }
                    }
                },
                saveEnabled = uiState.canSave
            )
        }
    }

    // 账本选择对话框
    LedgerSelectorDialog(
        isVisible = uiState.showLedgerSelector,
        ledgers = uiState.ledgers,
        selectedLedgerId = uiState.selectedLedger?.id,
        onLedgerSelected = { ledger ->
            viewModel.selectLedger(ledger)
            android.util.Log.d("AddTxnV2", "add_txn_ledger_change ledgerId=${ledger.id}")
        },
        onDismiss = viewModel::hideLedgerSelector
    )

    // 同步目标选择器
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

    // 日期/时间选择（正式对话框样式）
    if (uiState.showDateTimePicker) {
        V2DateTimePickerDialog(
            selectedDate = uiState.selectedDate,
            selectedTime = uiState.selectedTime,
            enableTimeSelection = uiState.enableTimeRecording,
            onDateSelected = {
                viewModel.updateDate(it)
                android.util.Log.d("AddTxnV2", "add_txn_time_change date=${it}")
            },
            onTimeSelected = {
                viewModel.updateTime(it)
                android.util.Log.d("AddTxnV2", "add_txn_time_change time=${it.hour}:${it.minute}")
            },
            onDismiss = { viewModel.hideDateTimePicker() }
        )
    }
}

@Composable
private fun CategoryChipV2(
    category: Category,
    isSelected: Boolean,
    iconDisplayMode: com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null,
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DynamicCategoryIcon(
                category = category,
                iconDisplayMode = iconDisplayMode,
                size = 24.dp,
                tint = if (isSelected) DesignTokens.BrandColors.Ledger else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = if (isSelected) DesignTokens.BrandColors.Ledger else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun V2DateTimePickerDialog(
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    enableTimeSelection: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atStartOfDayIn(kotlinx.datetime.TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
    )
    var showTimePicker by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (enableTimeSelection) "选择日期时间" else "选择日期") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DatePicker(state = datePickerState)
                if (enableTimeSelection) {
                    OutlinedButton(onClick = { showTimePicker = true }) { Text("选择时间：${String.format("%02d:%02d", selectedTime.hour, selectedTime.minute)}") }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val millis = datePickerState.selectedDateMillis
                millis?.let {
                    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(it)
                    val date = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
                    onDateSelected(date)
                }
                onDismiss()
            }) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
    // Apply selection on confirm via LaunchedEffect to avoid lifting state: adapt by providing separate confirm path
    // 为简化，这里在关闭前尝试提交日期
    // 补：内联确认逻辑
    DisposableEffect(Unit) {
        onDispose { }
    }
    if (showTimePicker && enableTimeSelection) {
        val timeState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择时间") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(LocalTime(timeState.hour, timeState.minute))
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun NumberPadV2(
    onNumber: (String) -> Unit,
    onDot: () -> Unit,
    onBackspace: () -> Unit,
    onPlus: () -> Unit,
    onMinus: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean
) {
    val rows = listOf(
        listOf("1", "2", "3", "⌫"),
        listOf("4", "5", "6", "-"),
        listOf("7", "8", "9", "+"),
        listOf("0", ".", "保存")
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    when (key) {
                        "保存" -> {
                            Button(
                                onClick = onSave,
                                enabled = saveEnabled,
                                modifier = Modifier.weight(1f)
                            ) { Text("保存") }
                        }
                        "⌫" -> {
                            OutlinedButton(onClick = onBackspace, modifier = Modifier.weight(1f)) { Text("⌫") }
                        }
                        "+" -> {
                            OutlinedButton(onClick = onPlus, modifier = Modifier.weight(1f)) { Text("+") }
                        }
                        "-" -> {
                            OutlinedButton(onClick = onMinus, modifier = Modifier.weight(1f)) { Text("-") }
                        }
                        "." -> {
                            OutlinedButton(onClick = onDot, modifier = Modifier.weight(1f)) { Text(".") }
                        }
                        else -> {
                            OutlinedButton(onClick = { onNumber(key) }, modifier = Modifier.weight(1f)) { Text(key) }
                        }
                    }
                }
            }
        }
    }
}
