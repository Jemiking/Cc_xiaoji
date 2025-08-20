package com.ccxiaoji.feature.ledger.presentation.screen.demo

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AddTransactionViewModel
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatButton
import kotlinx.coroutines.launch

data class CategoryGridItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val isIncome: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionCategoryFirstScreen(
    navController: NavController,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    // 模拟分类数据（基于参考界面）
    val expenseCategories = remember {
        listOf(
            CategoryGridItem("buy_food", "买菜", Icons.Default.ShoppingCart),
            CategoryGridItem("breakfast", "早餐", Icons.Default.Restaurant),
            CategoryGridItem("dining", "下馆子", Icons.Default.Fastfood),
            CategoryGridItem("grocery", "柴米油盐", Icons.Default.Kitchen),
            CategoryGridItem("fruit", "水果", Icons.Default.LocalGroceryStore),
            CategoryGridItem("snack", "零食", Icons.Default.Fastfood),
            CategoryGridItem("drink", "饮料", Icons.Default.LocalBar),
            CategoryGridItem("clothes", "衣服", Icons.Default.Store),
            CategoryGridItem("transport", "交通", Icons.Default.DirectionsCar),
            CategoryGridItem("travel", "旅行", Icons.Default.Flight),
            CategoryGridItem("phone", "话费网费", Icons.Default.Phone),
            CategoryGridItem("alcohol", "烟酒", Icons.Default.LocalBar),
            CategoryGridItem("study", "学习", Icons.Default.School),
            CategoryGridItem("daily", "日用品", Icons.Default.ShoppingBasket),
            CategoryGridItem("housing", "住房", Icons.Default.Home),
            CategoryGridItem("beauty", "美妆", Icons.Default.Face),
            CategoryGridItem("medical", "医疗", Icons.Default.MedicalServices),
            CategoryGridItem("gift", "发红包", Icons.Default.CardGiftcard),
            CategoryGridItem("entertainment", "娱乐", Icons.Default.SportsEsports),
            CategoryGridItem("social", "请客送礼", Icons.Default.Cake),
            CategoryGridItem("digital", "电器数码", Icons.Default.Computer),
            CategoryGridItem("utility", "水电煤", Icons.Default.Power),
            CategoryGridItem("other", "其它", Icons.Default.MoreHoriz),
            CategoryGridItem("market", "超市", Icons.Default.Store)
        )
    }
    
    val incomeCategories = remember {
        listOf(
            CategoryGridItem("salary", "工资", Icons.Default.Work, true),
            CategoryGridItem("bonus", "奖金", Icons.Default.EmojiEvents, true),
            CategoryGridItem("investment", "投资", Icons.Default.TrendingUp, true),
            CategoryGridItem("business", "生意", Icons.Default.Business, true),
            CategoryGridItem("part_time", "兼职", Icons.Default.Schedule, true),
            CategoryGridItem("gift_income", "礼金", Icons.Default.Redeem, true),
            CategoryGridItem("other_income", "其它收入", Icons.Default.MoreHoriz, true)
        )
    }
    
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var amountText by remember { mutableStateOf("0.0") }
    var noteText by remember { mutableStateOf("") }
    
    val currentCategories = if (uiState.isIncome) incomeCategories else expenseCategories
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "方案六：分类优先布局",
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
        ) {
            // 顶部：收入/支出切换
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.Spacing.medium),
                horizontalArrangement = Arrangement.Center
            ) {
                TabRow(
                    selectedTabIndex = if (uiState.isIncome) 1 else 0,
                    modifier = Modifier.width(200.dp),
                    indicator = { },
                    divider = { }
                ) {
                    Tab(
                        selected = !uiState.isIncome,
                        onClick = { viewModel.setIncomeType(false) },
                        modifier = Modifier.background(
                            if (!uiState.isIncome) Color.Black else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                    ) {
                        Text(
                            text = "支出",
                            color = if (!uiState.isIncome) Color.White else Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    Tab(
                        selected = uiState.isIncome,
                        onClick = { viewModel.setIncomeType(true) },
                        modifier = Modifier.background(
                            if (uiState.isIncome) Color.Black else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                    ) {
                        Text(
                            text = "收入",
                            color = if (uiState.isIncome) Color.White else Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 中间：分类网格（5列布局）
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                items(currentCategories) { category ->
                    CategoryGridItemCard(
                        category = category,
                        isSelected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id }
                    )
                }
            }
            
            // 底部：输入区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(DesignTokens.Spacing.medium)
            ) {
                // 备注输入
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("点此输入备注...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                
                // 金额和账户信息行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$amountText CNY",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isIncome) DesignTokens.BrandColors.Success else DesignTokens.BrandColors.Error
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.selectedAccount?.name ?: "现金",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "今天 15:49",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
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
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                
                // 数字键盘
                NumberKeypad(
                    onNumberClick = { number ->
                        if (amountText == "0.0") {
                            amountText = number
                        } else {
                            amountText += number
                        }
                    },
                    onDotClick = {
                        if (!amountText.contains(".")) {
                            amountText += "."
                        }
                    },
                    onDeleteClick = {
                        if (amountText.length > 1) {
                            amountText = amountText.dropLast(1)
                        } else {
                            amountText = "0.0"
                        }
                    },
                    onPlusClick = { /* TODO: 连续记账 */ },
                    onMinusClick = { /* TODO: 切换正负 */ },
                    onAgainClick = { /* TODO: 再记一笔 */ }
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 保存按钮
                FlatButton(
                    text = "保存",
                    onClick = { 
                        scope.launch {
                            // TODO: 保存交易逻辑
                            navController.navigateUp()
                        }
                    },
                    enabled = selectedCategoryId != null && amountText != "0.0",
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DesignTokens.BrandColors.Ledger
                )
            }
        }
    }
}

@Composable
private fun CategoryGridItemCard(
    category: CategoryGridItem,
    isSelected: Boolean,
    onClick: () -> Unit
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
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignTokens.Spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) {
                    DesignTokens.BrandColors.Ledger
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
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
private fun NumberKeypad(
    onNumberClick: (String) -> Unit,
    onDotClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPlusClick: () -> Unit,
    onMinusClick: () -> Unit,
    onAgainClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 第一行：1 2 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton("1", Modifier.weight(1f)) { onNumberClick("1") }
            KeypadButton("2", Modifier.weight(1f)) { onNumberClick("2") }
            KeypadButton("3", Modifier.weight(1f)) { onNumberClick("3") }
            KeypadButton("×", Modifier.weight(1f)) { onDeleteClick() }
        }
        
        // 第二行：4 5 6 -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton("4", Modifier.weight(1f)) { onNumberClick("4") }
            KeypadButton("5", Modifier.weight(1f)) { onNumberClick("5") }
            KeypadButton("6", Modifier.weight(1f)) { onNumberClick("6") }
            KeypadButton("−", Modifier.weight(1f)) { onMinusClick() }
        }
        
        // 第三行：7 8 9 +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton("7", Modifier.weight(1f)) { onNumberClick("7") }
            KeypadButton("8", Modifier.weight(1f)) { onNumberClick("8") }
            KeypadButton("9", Modifier.weight(1f)) { onNumberClick("9") }
            KeypadButton("+", Modifier.weight(1f)) { onPlusClick() }
        }
        
        // 第四行：再记 0 . 
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton("再记", Modifier.weight(1f)) { onAgainClick() }
            KeypadButton("0", Modifier.weight(1f)) { onNumberClick("0") }
            KeypadButton(".", Modifier.weight(1f)) { onDotClick() }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}