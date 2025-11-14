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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.presentation.component.DynamicCategoryIcon
import com.ccxiaoji.feature.ledger.presentation.component.EntityEditorScaffold
import com.ccxiaoji.feature.ledger.presentation.component.rememberEntityEditorState
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CategoryEditorViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.Clock

/**
 * 统一的分类编辑器界面
 *
 * 通过参数化设计，支持新增和编辑两种模式：
 * - 新增模式：categoryId 为 null，需要提供 categoryType
 * - 编辑模式：categoryId 不为 null，从数据库加载现有分类
 *
 * @param categoryId 分类ID，为空表示新增模式
 * @param categoryType 分类类型（INCOME/EXPENSE），仅新增模式需要
 * @param navController 导航控制器
 * @param onNavigateBack 返回导航回调
 * @param viewModel 分类编辑器 ViewModel
 */
@Composable
fun CategoryEditorScreen(
    categoryId: String? = null,
    categoryType: String? = null,
    navController: NavController,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: CategoryEditorViewModel = hiltViewModel()
) {
    // 初始化 ViewModel 参数
    LaunchedEffect(categoryId, categoryType) {
        viewModel.initialize(categoryId, categoryType)
    }

    // 收集业务数据状态和UI元状态
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val editorStateFlow by viewModel.editorState.collectAsStateWithLifecycle()

    // 获取UI样式偏好
    val uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()

    // 处理保存成功
    LaunchedEffect(formState.saveSuccess) {
        if (formState.saveSuccess) {
            // 设置结果到上一个界面
            val key = if (editorStateFlow.isEditMode) "category_updated" else "category_added"
            navController.previousBackStackEntry?.savedStateHandle?.set(key, true)
            onNavigateBack?.invoke() ?: navController.popBackStack()
        }
    }

    // 同步EntityEditorState（由 EntityEditorScaffold 统一处理返回、未保存确认、遮罩等）
    val editorState = rememberEntityEditorState(
        isEditMode = editorStateFlow.isEditMode,
        isLoading = editorStateFlow.isLoading,
        isSaving = editorStateFlow.isSaving,
        saveEnabled = editorStateFlow.saveEnabled,
        hasUnsavedChanges = editorStateFlow.hasUnsavedChanges,
        showDiscardConfirm = true
    )

    // 标题由模式与类型决定
    val title = when {
        editorStateFlow.isEditMode -> "编辑分类"
        formState.type == Category.Type.INCOME -> "添加收入分类"
        else -> "添加支出分类"
    }

    // 保存按钮文案：编辑模式显示"保存"，新增模式显示"添加"
    val saveButtonText = stringResource(
        if (editorStateFlow.isEditMode) R.string.save else R.string.add
    )

    EntityEditorScaffold(
        title = title,
        state = editorState,
        onBack = { onNavigateBack?.invoke() ?: navController.popBackStack() },
        onSave = viewModel::saveCategory,
        saveButtonText = saveButtonText
    ) {
        when {
            // 编辑模式下加载失败
            editorStateFlow.isEditMode && formState.loadError != null -> {
                ErrorContent(
                    error = formState.loadError!!,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // 主界面内容（初始化时由通用遮罩覆盖）
            else -> {
                CategoryEditorContent(
                    formState = formState,
                    uiPreferences = uiPreferences,
                    onNameChanged = viewModel::updateName,
                    onIconSelected = viewModel::updateIcon,
                    onColorSelected = viewModel::updateColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}


/**
 * 主要内容区域
 */
@Composable
private fun CategoryEditorContent(
    formState: CategoryFormState,
    uiPreferences: com.ccxiaoji.feature.ledger.domain.model.LedgerUIPreferences,
    onNameChanged: (String) -> Unit,
    onIconSelected: (String) -> Unit,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 分类名称输入
        CategoryNameField(
            name = formState.name,
            onNameChanged = onNameChanged,
            isEnabled = !formState.isSystemCategory,
            error = formState.nameError,
            isSystemCategory = formState.isSystemCategory
        )

        // 图标选择器
        CategoryIconSelector(
            selectedIcon = formState.selectedIcon,
            categoryType = formState.type,
            iconDisplayMode = uiPreferences.iconDisplayMode,
            onIconSelected = onIconSelected
        )

        // 颜色选择器
        CategoryColorSelector(
            selectedColor = formState.selectedColor,
            onColorSelected = onColorSelected
        )

        // 错误提示卡片
        formState.generalError?.let { error ->
            ErrorCard(error = error)
        }
    }
}

/**
 * 分类名称输入字段
 */
@Composable
private fun CategoryNameField(
    name: String,
    onNameChanged: (String) -> Unit,
    isEnabled: Boolean,
    error: String?,
    isSystemCategory: Boolean
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChanged,
        label = { Text("分类名称") },
        placeholder = { Text("例如：餐饮、交通、购物") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        enabled = isEnabled,
        isError = error != null,
        supportingText = {
            when {
                error != null -> Text(error)
                isSystemCategory -> Text(
                    "系统分类名称不可修改",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        },
        singleLine = true
    )
}

/**
 * 图标选择器
 */
@Composable
private fun CategoryIconSelector(
    selectedIcon: String,
    categoryType: Category.Type,
    iconDisplayMode: com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode,
    onIconSelected: (String) -> Unit
) {
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
            val icons = when (categoryType) {
                Category.Type.EXPENSE -> Category.DEFAULT_EXPENSE_ICONS
                Category.Type.INCOME -> Category.DEFAULT_INCOME_ICONS
            }

            items(
                items = icons,
                key = { it }
            ) { icon ->
                IconItem(
                    icon = icon,
                    isSelected = icon == selectedIcon,
                    onClick = { onIconSelected(icon) },
                    categoryType = categoryType,
                    iconDisplayMode = iconDisplayMode
                )
            }
        }
    }
}

/**
 * 颜色选择器
 */
@Composable
private fun CategoryColorSelector(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
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
            items(
                items = Category.DEFAULT_COLORS,
                key = { it }
            ) { color ->
                ColorItem(
                    color = color,
                    isSelected = color == selectedColor,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

/**
 * 图标选项项
 */
@Composable
private fun IconItem(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    categoryType: Category.Type,
    iconDisplayMode: com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode
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
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // 创建临时分类对象用于预览
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
                iconDisplayMode = iconDisplayMode,
                size = 24.dp,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 颜色选项项
 */
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
            .clickable(onClick = onClick)
    ) {
        if (isSelected) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "已选中",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


/**
 * 错误内容
 */
@Composable
private fun ErrorContent(
    error: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * 错误提示卡片
 */
@Composable
private fun ErrorCard(
    error: String
) {
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


/**
 * 安全的颜色解析
 */
private fun parseColorSafe(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        // 返回默认颜色
        Color(0xFF6200EE)
    }
}

/**
 * 分类编辑器 UI 状态
 */
data class CategoryEditorUiState(
    // 模式标识
    val isEditMode: Boolean = false,

    // 分类数据
    val category: Category? = null,
    val type: Category.Type = Category.Type.EXPENSE,
    val name: String = "",
    val selectedIcon: String = "",
    val selectedColor: String = "",

    // 状态标识
    val isInitializing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSystemCategory: Boolean = false,

    // 验证和错误
    val nameError: String? = null,
    val generalError: String? = null,
    val loadError: String? = null,

    // 交互状态
    val hasUnsavedChanges: Boolean = false,
    val canSave: Boolean = false,
    val saveSuccess: Boolean = false,
    val showUnsavedChangesDialog: Boolean = false
)