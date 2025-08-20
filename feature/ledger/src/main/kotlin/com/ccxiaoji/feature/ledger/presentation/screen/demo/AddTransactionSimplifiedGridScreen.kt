package com.ccxiaoji.feature.ledger.presentation.screen.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

data class SimplifiedCategoryItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val isIncome: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSimplifiedGridScreen(
    navController: NavController,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    // 简化的常用分类（精选最常用的8-12个）
    val expenseCategories = remember {
        listOf(
            SimplifiedCategoryItem("food", "餐饮", Icons.Default.Restaurant, Color(0xFFFF6B6B)),
            SimplifiedCategoryItem("transport", "交通", Icons.Default.DirectionsCar, Color(0xFF4ECDC4)),
            SimplifiedCategoryItem("shopping", "购物", Icons.Default.ShoppingBag, Color(0xFF45B7D1)),
            SimplifiedCategoryItem("entertainment", "娱乐", Icons.Default.SportsEsports, Color(0xFF96CEB4)),
            SimplifiedCategoryItem("daily", "日用", Icons.Default.Home, Color(0xFFFFA07A)),
            SimplifiedCategoryItem("medical", "医疗", Icons.Default.MedicalServices, Color(0xFFDDA0DD)),
            SimplifiedCategoryItem("education", "教育", Icons.Default.School, Color(0xFF87CEEB)),
            SimplifiedCategoryItem("social", "社交", Icons.Default.People, Color(0xFFFFB6C1)),
            SimplifiedCategoryItem("other", "其他", Icons.Default.MoreHoriz, Color(0xFFD3D3D3))
        )
    }
    
    val incomeCategories = remember {
        listOf(
            SimplifiedCategoryItem("salary", "工资", Icons.Default.Work, Color(0xFF32CD32), true),
            SimplifiedCategoryItem("bonus", "奖金", Icons.Default.EmojiEvents, Color(0xFFFFD700), true),
            SimplifiedCategoryItem("investment", "投资", Icons.Default.TrendingUp, Color(0xFF00CED1), true),
            SimplifiedCategoryItem("business", "生意", Icons.Default.Business, Color(0xFF9370DB), true),
            SimplifiedCategoryItem("gift", "礼金", Icons.Default.Redeem, Color(0xFFFF69B4), true),
            SimplifiedCategoryItem("other_income", "其他", Icons.Default.MoreHoriz, Color(0xFFD3D3D3), true)
        )
    }
    
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var amountText by remember { mutableStateOf("") }
    
    val currentCategories = if (uiState.isIncome) incomeCategories else expenseCategories
    val selectedCategory = currentCategories.find { it.id == selectedCategoryId }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "方案七：简化网格布局",
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 金额显示区域（置顶突出）
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.medium),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCategory != null) {
                        selectedCategory.color.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 收入/支出切换（小型）
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !uiState.isIncome,
                            onClick = { 
                                viewModel.setIncomeType(false)
                                selectedCategoryId = null
                            },
                            label = { Text("支出", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DesignTokens.BrandColors.Error.copy(alpha = 0.2f),
                                selectedLabelColor = DesignTokens.BrandColors.Error
                            )
                        )
                        FilterChip(
                            selected = uiState.isIncome,
                            onClick = { 
                                viewModel.setIncomeType(true)
                                selectedCategoryId = null
                            },
                            label = { Text("收入", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DesignTokens.BrandColors.Success.copy(alpha = 0.2f),
                                selectedLabelColor = DesignTokens.BrandColors.Success
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                    
                    // 金额显示
                    Text(
                        text = if (amountText.isEmpty()) "0" else amountText,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedCategory != null) {
                            selectedCategory.color
                        } else if (uiState.isIncome) {
                            DesignTokens.BrandColors.Success
                        } else {
                            DesignTokens.BrandColors.Error
                        }
                    )
                    
                    // 分类显示
                    if (selectedCategory != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = selectedCategory.icon,
                                contentDescription = null,
                                tint = selectedCategory.color,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = selectedCategory.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = selectedCategory.color,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = "请选择分类",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 分类选择网格（4列，更大的按钮）
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                items(currentCategories) { category ->
                    SimplifiedCategoryCard(
                        category = category,
                        isSelected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id }
                    )
                }
            }
            
            // 底部：简化的输入区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(DesignTokens.Spacing.medium)
            ) {
                // 简化数字键盘（3x4布局）
                SimplifiedNumberKeypad(
                    onNumberClick = { number ->
                        amountText += number
                    },
                    onDotClick = {
                        if (!amountText.contains(".")) {
                            amountText += "."
                        }
                    },
                    onDeleteClick = {
                        if (amountText.isNotEmpty()) {
                            amountText = amountText.dropLast(1)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 保存按钮
                FlatButton(
                    text = "保存记账",
                    onClick = { 
                        scope.launch {
                            // TODO: 保存交易逻辑
                            navController.navigateUp()
                        }
                    },
                    enabled = selectedCategoryId != null && amountText.isNotEmpty() && amountText != "0",
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = selectedCategory?.color ?: DesignTokens.BrandColors.Ledger
                )
            }
        }
    }
}

@Composable
private fun SimplifiedCategoryCard(
    category: SimplifiedCategoryItem,
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
                category.color.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(3.dp, category.color)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignTokens.Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 圆形图标背景
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    modifier = Modifier.size(24.dp),
                    tint = category.color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = if (isSelected) category.color else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun SimplifiedNumberKeypad(
    onNumberClick: (String) -> Unit,
    onDotClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 第一行：1 2 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SimpleKeypadButton("1", Modifier.weight(1f)) { onNumberClick("1") }
            SimpleKeypadButton("2", Modifier.weight(1f)) { onNumberClick("2") }
            SimpleKeypadButton("3", Modifier.weight(1f)) { onNumberClick("3") }
        }
        
        // 第二行：4 5 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SimpleKeypadButton("4", Modifier.weight(1f)) { onNumberClick("4") }
            SimpleKeypadButton("5", Modifier.weight(1f)) { onNumberClick("5") }
            SimpleKeypadButton("6", Modifier.weight(1f)) { onNumberClick("6") }
        }
        
        // 第三行：7 8 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SimpleKeypadButton("7", Modifier.weight(1f)) { onNumberClick("7") }
            SimpleKeypadButton("8", Modifier.weight(1f)) { onNumberClick("8") }
            SimpleKeypadButton("9", Modifier.weight(1f)) { onNumberClick("9") }
        }
        
        // 第四行：. 0 ⌫
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SimpleKeypadButton(".", Modifier.weight(1f)) { onDotClick() }
            SimpleKeypadButton("0", Modifier.weight(1f)) { onNumberClick("0") }
            SimpleKeypadButton("⌫", Modifier.weight(1f)) { onDeleteClick() }
        }
    }
}

@Composable
private fun SimpleKeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}