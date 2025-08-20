package com.ccxiaoji.feature.ledger.presentation.screen.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode
import com.ccxiaoji.feature.ledger.presentation.component.DynamicCategoryIcon
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AddCategoryViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.Clock

/**
 * 添加分类页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    categoryType: String, // "EXPENSE" 或 "INCOME"
    navController: NavController,
    viewModel: AddCategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val type = if (categoryType == "INCOME") Category.Type.INCOME else Category.Type.EXPENSE
    
    // 处理保存成功
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("category_added", true)
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (type == Category.Type.EXPENSE) "添加支出分类" else "添加收入分类") 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveCategory() },
                        enabled = !uiState.isLoading && uiState.name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DesignTokens.BrandColors.Ledger
                        )
                    ) {
                        Text("添加")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 名称输入
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("分类名称") },
                placeholder = { Text("例如：餐饮、交通、购物") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                enabled = !uiState.isLoading,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } }
            )
            
            // 图标选择
            Column {
                Text(
                    text = "选择图标",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val icons = if (type == Category.Type.EXPENSE) {
                        Category.DEFAULT_EXPENSE_ICONS
                    } else {
                        Category.DEFAULT_INCOME_ICONS
                    }
                    items(icons) { icon ->
                        IconItem(
                            icon = icon,
                            isSelected = icon == uiState.selectedIcon,
                            onClick = { viewModel.updateIcon(icon) },
                            categoryType = type
                        )
                    }
                }
            }
            
            // 颜色选择
            Column {
                Text(
                    text = "选择颜色",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(Category.DEFAULT_COLORS) { color ->
                        ColorItem(
                            color = color,
                            isSelected = color == uiState.selectedColor,
                            onClick = { viewModel.updateColor(color) }
                        )
                    }
                }
            }
            
            // 错误提示
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // 加载中指示器
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun IconItem(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    categoryType: Category.Type
) {
    // 获取图标显示模式
    val uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()
    
    // 🔧 DEBUG: AddCategoryScreen IconItem
    println("📝 [AddCategoryScreen] IconItem调试:")
    println("   - 图标emoji: $icon")
    println("   - 分类类型: $categoryType")
    println("   - UI偏好图标模式: ${uiPreferences.iconDisplayMode}")
    println("   - 是否被选中: $isSelected")
    
    Surface(
        shape = CircleShape,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            }
        ),
        modifier = Modifier
            .size(48.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // 创建临时Category对象来支持DynamicCategoryIcon
            val tempCategory = Category(
                id = "temp_preview",
                name = "预览",
                type = categoryType,
                icon = icon,
                color = "#6200EE",
                level = 1,
                parentId = null,
                isSystem = false,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now()
            )
            
            DynamicCategoryIcon(
                category = tempCategory,
                iconDisplayMode = uiPreferences.iconDisplayMode,
                size = 24.dp,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ColorItem(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = parseColorSafe(color),
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
        ),
        modifier = Modifier
            .size(40.dp)
            .clickable { onClick() }
    ) {
        if (isSelected) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// 安全的颜色解析
@Composable
private fun parseColorSafe(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
}