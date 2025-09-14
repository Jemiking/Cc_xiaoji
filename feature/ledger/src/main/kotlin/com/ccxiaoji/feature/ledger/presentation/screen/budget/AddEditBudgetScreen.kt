package com.ccxiaoji.feature.ledger.presentation.screen.budget

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.presentation.component.DynamicCategoryIcon
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AddEditBudgetViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.Clock
import com.ccxiaoji.ui.components.FlatButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetScreen(
    categoryId: String? = null,
    navController: NavController,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: AddEditBudgetViewModel = hiltViewModel()
) {
    val TAG = "AddEditBudgetScreen"
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 获取图标显示模式
    val uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()
    
    // 调试初始化信息
    LaunchedEffect(categoryId) {
        Log.d(TAG, "AddEditBudgetScreen初始化，categoryId: $categoryId")
        viewModel.init(categoryId)
    }
    
    // 调试状态变化
    LaunchedEffect(uiState) {
        Log.d(TAG, "UIState更新 - 编辑模式: ${uiState.isEditMode}, 加载中: ${uiState.isLoading}, 可保存: ${uiState.canSave}")
        if (uiState.errorMessage != null) {
            Log.e(TAG, "错误信息: ${uiState.errorMessage}")
        }
        if (uiState.saveSuccess) {
            Log.d(TAG, "保存成功")
        }
    }
    
    // 系统返回
    BackHandler { onNavigateBack?.invoke() ?: navController.navigateUp() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.isEditMode) "编辑预算" else "添加预算",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack?.invoke() ?: navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 分类选择
            BudgetTypeSection(
                selectedCategoryId = uiState.selectedCategoryId,
                selectedCategory = uiState.selectedCategory,
                onSelectTotal = { viewModel.selectTotalBudget() },
                onSelectCategory = { navController.navigate("select_category") }
            )
            
            // 金额输入
            OutlinedTextField(
                value = uiState.amountText,
                onValueChange = viewModel::updateAmount,
                label = { Text("预算金额") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("¥") },
                colors = OutlinedTextFieldDefaults.colors(),
                shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
                singleLine = true,
                isError = uiState.amountError != null,
                supportingText = uiState.amountError?.let { { Text(it) } }
            )
            
            // 预警阈值
            AlertThresholdSection(
                alertThreshold = uiState.alertThreshold,
                onThresholdChange = viewModel::updateAlertThreshold
            )
            
            // 备注输入
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(),
                shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 保存按钮
            FlatButton(
                text = "保存",
                onClick = { 
                    Log.d(TAG, "点击保存按钮")
                    Log.d(TAG, "当前状态 - 金额: '${uiState.amountText}', 分类: ${uiState.selectedCategory?.name}, 编辑模式: ${uiState.isEditMode}")
                    viewModel.saveBudget {
                        Log.d(TAG, "保存成功，导航返回")
                        navController.navigateUp()
                    }
                },
                enabled = uiState.canSave,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DesignTokens.BrandColors.Ledger
            )
        }
    }
    
    // 分类选择结果处理
    LaunchedEffect(navController.currentBackStackEntry) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<String>("selected_category_id")
            ?.let { selectedId ->
                viewModel.selectCategory(selectedId)
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>("selected_category_id")
            }
    }
}

@Composable
private fun BudgetTypeSection(
    selectedCategoryId: String?,
    selectedCategory: CategoryEntity?,
    onSelectTotal: () -> Unit,
    onSelectCategory: () -> Unit
) {
    Column {
        Text(
            text = "预算类型",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectTotal() },
                shape = RoundedCornerShape(DesignTokens.BorderRadius.medium),
                color = if (selectedCategoryId == null) {
                    DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                border = if (selectedCategoryId == null) {
                    androidx.compose.foundation.BorderStroke(1.dp, DesignTokens.BrandColors.Ledger.copy(alpha = 0.5f))
                } else null
            ) {
                Text(
                    text = "总预算",
                    modifier = Modifier.padding(
                        horizontal = DesignTokens.Spacing.medium,
                        vertical = DesignTokens.Spacing.small
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selectedCategoryId == null) FontWeight.Medium else null,
                    color = if (selectedCategoryId == null) {
                        DesignTokens.BrandColors.Ledger
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectCategory() },
                shape = RoundedCornerShape(DesignTokens.BorderRadius.medium),
                color = if (selectedCategoryId != null) {
                    DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ) {
                Text(
                    text = selectedCategory?.name ?: "分类预算",
                    modifier = Modifier.padding(
                        horizontal = DesignTokens.Spacing.medium,
                        vertical = DesignTokens.Spacing.small
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selectedCategoryId != null) FontWeight.Medium else null,
                    color = if (selectedCategoryId != null) {
                        DesignTokens.BrandColors.Ledger
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun AlertThresholdSection(
    alertThreshold: Float,
    onThresholdChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "预警阈值",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(alertThreshold * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = DesignTokens.BrandColors.Warning
            )
        }
        Slider(
            value = alertThreshold,
            onValueChange = onThresholdChange,
            valueRange = 0.5f..0.95f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = DesignTokens.BrandColors.Warning,
                activeTrackColor = DesignTokens.BrandColors.Warning,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    navController: NavController,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: AddEditBudgetViewModel = hiltViewModel()
) {
    val categories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "选择分类",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack?.invoke() ?: navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    onSelect = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_category_id", category.id)
                        onNavigateBack?.invoke() ?: navController.navigateUp()
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: CategoryEntity,
    onSelect: () -> Unit
) {
    // 获取图标显示模式
    val uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()
    
    val categoryColor = when (category.type) {
        "INCOME" -> DesignTokens.BrandColors.Success
        "EXPENSE" -> DesignTokens.BrandColors.Error
        else -> MaterialTheme.colorScheme.primary
    }
    
    Surface(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignTokens.BorderRadius.medium),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                // 将CategoryEntity转换为Category对象
                val categoryModel = Category(
                    id = category.id,
                    name = category.name,
                    type = if (category.type == "INCOME") Category.Type.INCOME else Category.Type.EXPENSE,
                    icon = category.icon,
                    color = category.color,
                    level = category.level,
                    parentId = category.parentId,
                    isSystem = category.isSystem,
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now()
                )
                DynamicCategoryIcon(
                    category = categoryModel,
                    iconDisplayMode = uiPreferences.iconDisplayMode,
                    size = 20.dp,
                    tint = categoryColor
                )
            }
            
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
            
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
