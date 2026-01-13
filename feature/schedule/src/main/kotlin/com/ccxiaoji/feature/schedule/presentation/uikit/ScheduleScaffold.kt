package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Schedule 模块统一页面脚手架
 *
 * 整合以下功能：
 * - Scaffold 基础布局
 * - SnackbarHost 消息提示
 * - LoadingOverlay 加载遮罩
 * - 可选的滚动行为支持
 *
 * @param topBar 顶栏内容，通常使用 ScheduleTopAppBar
 * @param modifier Modifier
 * @param isLoading 是否显示加载遮罩
 * @param snackbarHostState Snackbar 状态，用于显示消息
 * @param floatingActionButton 浮动操作按钮
 * @param floatingActionButtonPosition FAB 位置
 * @param bottomBar 底部栏内容
 * @param scrollBehavior TopAppBar 滚动行为（可选）
 * @param content 页面内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScaffold(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    bottomBar: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = if (scrollBehavior != null) {
                Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            } else {
                Modifier
            },
            topBar = topBar,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            bottomBar = bottomBar,
            content = content
        )

        // 加载遮罩层
        ScheduleLoadingOverlay(isLoading = isLoading)
    }
}

/**
 * 带消息处理的 Schedule Scaffold
 *
 * 自动处理成功/错误消息的显示
 *
 * @param topBar 顶栏内容
 * @param modifier Modifier
 * @param isLoading 是否显示加载遮罩
 * @param successMessage 成功消息，非空时自动显示 Snackbar
 * @param errorMessage 错误消息，非空时自动显示 Snackbar
 * @param onSuccessMessageShown 成功消息显示后的回调
 * @param onErrorMessageShown 错误消息显示后的回调
 * @param floatingActionButton FAB
 * @param bottomBar 底部栏
 * @param content 内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScaffoldWithMessages(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    successMessage: String? = null,
    errorMessage: String? = null,
    onSuccessMessageShown: () -> Unit = {},
    onErrorMessageShown: () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    bottomBar: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // 处理成功消息
    successMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            onSuccessMessageShown()
        }
    }

    // 处理错误消息
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
            onErrorMessageShown()
        }
    }

    ScheduleScaffold(
        topBar = topBar,
        modifier = modifier,
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        bottomBar = bottomBar,
        scrollBehavior = scrollBehavior,
        content = content
    )
}

/**
 * 加载状态遮罩层
 *
 * 在页面上方显示半透明遮罩和加载指示器
 */
@Composable
fun ScheduleLoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.clip(MaterialTheme.shapes.medium),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(DesignTokens.Spacing.medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 简化版 Schedule Scaffold - 用于简单页面
 *
 * 提供常用的默认值，减少样板代码
 *
 * @param title 页面标题
 * @param onNavigateBack 返回导航回调
 * @param modifier Modifier
 * @param isLoading 是否显示加载遮罩
 * @param actions TopAppBar 右侧操作按钮
 * @param floatingActionButton FAB
 * @param content 内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSimpleScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    ScheduleScaffold(
        topBar = {
            ScheduleTopAppBar(
                title = title,
                onNavigationClick = onNavigateBack,
                actions = actions
            )
        },
        modifier = modifier,
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
        floatingActionButton = floatingActionButton,
        content = content
    )
}
