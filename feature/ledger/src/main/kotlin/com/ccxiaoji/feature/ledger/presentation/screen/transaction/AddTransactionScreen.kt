package com.ccxiaoji.feature.ledger.presentation.screen.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.ccxiaoji.feature.ledger.presentation.screen.ledger.components.CategoryChip
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AddTransactionViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    viewModel: AddTransactionViewModel = hiltViewModel(),
    uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.add_transaction),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // 顶部：收入/支出切换
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = adjustmentParams.categoryGridPadding.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TabRow(
                    selectedTabIndex = if (uiState.isIncome) 1 else 0,
                    modifier = Modifier
                        .width(adjustmentParams.tabRowWidth.dp)
                        .height(adjustmentParams.tabRowHeight.dp),
                    indicator = { },
                    divider = { }
                ) {
                    Tab(
                        selected = !uiState.isIncome,
                        onClick = { viewModel.setIncomeType(false) },
                        modifier = Modifier.background(
                            if (!uiState.isIncome) Color.Black else Color.Transparent,
                            RoundedCornerShape(adjustmentParams.tabCornerRadius.dp)
                        )
                    ) {
                        Text(
                            text = "支出",
                            color = if (!uiState.isIncome) Color.White else Color.Gray,
                            modifier = Modifier.padding(vertical = adjustmentParams.tabVerticalPadding.dp)
                        )
                    }
                    Tab(
                        selected = uiState.isIncome,
                        onClick = { viewModel.setIncomeType(true) },
                        modifier = Modifier.background(
                            if (uiState.isIncome) Color.Black else Color.Transparent,
                            RoundedCornerShape(adjustmentParams.tabCornerRadius.dp)
                        )
                    ) {
                        Text(
                            text = "收入",
                            color = if (uiState.isIncome) Color.White else Color.Gray,
                            modifier = Modifier.padding(vertical = adjustmentParams.tabVerticalPadding.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(adjustmentParams.categoryVerticalSpacing.dp))
            
            // 中间：分类网格
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
                        isSelected = uiState.selectedCategoryInfo?.parentId == category.id,
                        onClick = { 
                            // 点击一级分类，显示二级分类选择器
                            viewModel.showCategoryPicker()
                        },
                        params = adjustmentParams,
                        iconDisplayMode = uiPreferences.iconDisplayMode
                    )
                }
            }
            
            // 底部：输入区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(adjustmentParams.inputAreaHeight.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(
                            topStart = adjustmentParams.inputAreaCornerRadius.dp, 
                            topEnd = adjustmentParams.inputAreaCornerRadius.dp
                        )
                    )
                    .padding(adjustmentParams.inputAreaPadding.dp)
            ) {
                // 第一行：备注框（左）和金额数字（右）并排
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = adjustmentParams.noteFieldTopPadding.dp,
                            bottom = adjustmentParams.noteFieldBottomPadding.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(adjustmentParams.noteToAmountSpacing.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：备注输入框
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .padding(horizontal = adjustmentParams.noteFieldHorizontalPadding.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.note,
                            onValueChange = viewModel::updateNote,
                            placeholder = { 
                                Text(
                                    "点此输入备注...",
                                    fontSize = adjustmentParams.noteTextSize.sp
                                ) 
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(adjustmentParams.noteFieldContentPadding.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = adjustmentParams.noteTextSize.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    }
                    
                    // 右侧：金额数字显示
                    Text(
                        text = "${uiState.amountText} CNY",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = adjustmentParams.amountTextSize.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isIncome) DesignTokens.BrandColors.Success else DesignTokens.BrandColors.Error,
                        modifier = Modifier.padding(end = adjustmentParams.amountTextPadding.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(adjustmentParams.accountToNoteSpacing.dp))
                
                // 第二行：记账簿和账户选择器
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 记账簿选择器
                    LedgerSelector(
                        selectedLedger = uiState.selectedLedger,
                        onClick = { viewModel.showLedgerSelector() },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // 账户信息卡片
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Column {
                                Text(
                                    text = uiState.selectedAccount?.name ?: "现金",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "今天 ${uiState.selectedTime?.hour ?: 15}:${uiState.selectedTime?.minute ?: 49}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 第三行：功能按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { /* TODO: 报销功能 */ }) {
                            Icon(Icons.Default.Receipt, contentDescription = "报销")
                        }
                        IconButton(onClick = { /* TODO: 图片功能 */ }) {
                            Icon(Icons.Default.Image, contentDescription = "图片")
                        }
                        IconButton(onClick = { /* TODO: 购物车功能 */ }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "购物车")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(adjustmentParams.amountToKeypadSpacing.dp))
                
                // 数字键盘
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = adjustmentParams.keypadHorizontalPadding.dp)
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
                            scope.launch {
                                viewModel.saveTransaction {
                                    navController.navigateUp()
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
            ProductionKeypadButton("×", Modifier.weight(1f), params) { onDeleteClick() }
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