package com.ccxiaoji.feature.ledger.presentation.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R

/**
 * 通用实体编辑页面框架组件（简化版本）
 *
 * 提供统一的编辑页面结构，包括：
 * - 顶部栏（标题、返回、保存）
 * - 加载和保存状态管理
 * - 未保存变更拦截
 * - Snackbar消息提示
 * - 可扩展的插槽设计
 *
 * @param title 页面标题
 * @param isEditMode 是否为编辑模式
 * @param onBack 返回操作回调
 * @param onSave 保存操作回调
 * @param isSaving 是否正在保存
 * @param modifier 修饰符
 * @param saveButtonText 保存按钮文案，为null时使用默认"保存"文案
 * @param content 页面内容
 */
@Composable
fun EntityEditorScaffold(
    title: String,
    isEditMode: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean = false,
    modifier: Modifier = Modifier,
    saveButtonText: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    // 使用简化版本，创建默认状态
    val state = rememberEntityEditorState(
        isEditMode = isEditMode,
        isSaving = isSaving
    )

    EntityEditorScaffold(
        title = title,
        state = state,
        onBack = onBack,
        onSave = onSave,
        modifier = modifier,
        saveButtonText = saveButtonText,
        content = content
    )
}

/**
 * 通用实体编辑页面框架组件（完整版本）
 *
 * @param title 页面标题
 * @param state 编辑器状态对象
 * @param onBack 返回操作回调
 * @param onSave 保存操作回调
 * @param modifier 修饰符
 * @param saveButtonText 保存按钮文案，为null时使用默认"保存"文案
 * @param snackbarHostState Snackbar宿主状态
 * @param navigationIcon 自定义导航图标
 * @param actions 额外的顶部操作按钮
 * @param bottomBar 底部栏
 * @param floatingActionButton 浮动操作按钮
 * @param content 页面内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntityEditorScaffold(
    title: String,
    state: EntityEditorState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveButtonText: String? = null,
    snackbarHostState: SnackbarHostState = state.snackbarHostState,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    var showDiscardDialog by remember { mutableStateOf(false) }

    // 处理返回导航，检查是否有未保存的变更
    val handleBack: () -> Unit = {
        if (state.showDiscardConfirm && state.hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    // 拦截系统返回键
    BackHandler(enabled = state.showDiscardConfirm && state.hasUnsavedChanges) {
        showDiscardDialog = true
    }

    Scaffold(
        modifier = modifier.testTag(EditorTestTags.SCAFFOLD),
        topBar = {
            EditorTopBar(
                title = title,
                state = state,
                onBack = handleBack,
                onSave = onSave,
                saveButtonText = saveButtonText ?: stringResource(R.string.save),
                navigationIcon = navigationIcon,
                extraActions = actions
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.testTag(EditorTestTags.SNACKBAR_HOST)
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 主要内容区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .testTag(EditorTestTags.CONTENT_AREA)
            ) {
                content()
            }

            // 加载遮罩层
            if (state.isLoading) {
                LoadingOverlay()
            }
        }
    }

    // 丢弃变更确认对话框
    if (showDiscardDialog) {
        DiscardChangesDialog(
            onConfirmDiscard = {
                showDiscardDialog = false
                onBack()
            },
            onDismiss = {
                showDiscardDialog = false
            }
        )
    }
}

/**
 * 编辑器顶部栏
 *
 * @param title 标题
 * @param state 编辑器状态
 * @param onBack 返回回调
 * @param onSave 保存回调
 * @param saveButtonText 保存按钮文案
 * @param navigationIcon 自定义导航图标
 * @param extraActions 额外操作按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTopBar(
    title: String,
    state: EntityEditorState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    saveButtonText: String,
    navigationIcon: (@Composable () -> Unit)?,
    extraActions: @Composable RowScope.() -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (navigationIcon != null) {
                navigationIcon()
            } else {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .testTag(EditorTestTags.BACK_BUTTON)
                        .semantics {
                            role = Role.Button
                            contentDescription = "返回"
                        }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = {
            // 保存按钮
            SaveButton(
                onClick = onSave,
                enabled = state.saveEnabled && !state.isSaving,
                isSaving = state.isSaving,
                text = saveButtonText
            )
            // 额外的操作按钮
            extraActions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * 保存按钮组件
 *
 * @param onClick 点击回调
 * @param enabled 是否启用
 * @param isSaving 是否正在保存
 * @param text 按钮文案
 */
@Composable
private fun SaveButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isSaving: Boolean,
    text: String
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .testTag(EditorTestTags.SAVE_BUTTON)
            .semantics {
                contentDescription = if (isSaving) "${text}中" else text
            }
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 加载遮罩层
 */
@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
            )
            .testTag(EditorTestTags.LOADING_OVERLAY)
            .semantics {
                contentDescription = "加载中"
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}