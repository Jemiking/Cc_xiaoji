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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.activity.compose.BackHandler
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.presentation.viewmodel.EditCategoryViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 编辑分类页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryScreen(
    categoryId: String,
    navController: NavController,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: EditCategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 系统返回
    BackHandler { onNavigateBack?.invoke() ?: navController.popBackStack() }

    // 处理保存成功
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("category_updated", true)
            onNavigateBack?.invoke() ?: navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑分类") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack?.invoke() ?: navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.category == null) {
            // 加载中
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.category == null) {
            // 加载失败
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error ?: "分类不存在",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            val category = uiState.category ?: return@Scaffold
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
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    ),
                    enabled = !uiState.isLoading && !category.isSystem,
                    isError = uiState.nameError != null,
                    supportingText = {
                        val nameError = uiState.nameError
                        when {
                            nameError != null -> Text(nameError)
                            category.isSystem -> Text(
                                "系统分类名称不可修改",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
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
                        val icons = if (category.type == Category.Type.EXPENSE) {
                            Category.DEFAULT_EXPENSE_ICONS
                        } else {
                            Category.DEFAULT_INCOME_ICONS
                        }
                        items(icons) { icon ->
                            IconItem(
                                icon = icon,
                                isSelected = icon == uiState.selectedIcon,
                                onClick = { viewModel.updateIcon(icon) }
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
        }
        
        // 保存中指示器
        if (uiState.isLoading && uiState.category != null) {
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
    onClick: () -> Unit
) {
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
            Text(
                text = icon,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
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
