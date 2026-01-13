package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.YearMonth

/**
 * Schedule 模块统一顶栏
 *
 * 提供一致的顶栏样式，支持：
 * - 标准导航顶栏
 * - 月份选择顶栏（用于日历页面）
 *
 * @param title 标题
 * @param modifier Modifier
 * @param navigationIcon 导航图标（默认返回箭头）
 * @param onNavigationClick 导航点击回调
 * @param actions 操作按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null) {
                navigationIcon()
            } else if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/**
 * 日历月份选择顶栏 - AnimatedMonthSelector 风格
 *
 * 设计要点（对齐 UIDemo AnimatedMonthSelector）：
 * - 外层容器：surfaceVariant + 12dp 圆角
 * - 导航按钮：32dp 圆形 + surface 背景
 * - 月份标题：220ms fadeIn/fadeOut 动画
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCalendarTopAppBar(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTitleClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    onNavigationClick: (() -> Unit)? = null,
    onTodayClick: (() -> Unit)? = null,
    canGoPreviousMonth: Boolean = true,
    canGoNextMonth: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            MonthNavigator(
                yearMonth = yearMonth,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onTitleClick = onTitleClick,
                canGoPrev = canGoPreviousMonth,
                canGoNext = canGoNextMonth
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null) {
                navigationIcon()
            } else if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        },
        actions = {
            if (onTodayClick != null) {
                IconButton(onClick = onTodayClick) {
                    Icon(
                        imageVector = Icons.Filled.Today,
                        contentDescription = "今天"
                    )
                }
            }
            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/**
 * 月份导航器 - AnimatedMonthSelector 风格
 *
 * 圆形导航按钮 + 220ms 淡入淡出月份动画
 */
@Composable
private fun MonthNavigator(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTitleClick: (() -> Unit)? = null,
    canGoPrev: Boolean = true,
    canGoNext: Boolean = true
) {
    val monthText = String.format("%04d年%02d月", yearMonth.year, yearMonth.monthValue)
    val contentDesc = buildString {
        append("当前显示 $monthText")
        if (canGoPrev) append("，可切换到上个月")
        if (canGoNext) append("，可切换到下个月")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics { contentDescription = contentDesc }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 上个月：圆形按钮（AnimatedMonthSelector 风格）
        MonthNavButton(
            enabled = canGoPrev,
            onClick = onPreviousMonth,
            contentDescription = "上个月"
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }

        // 月份标题：220ms 淡入淡出（AnimatedMonthSelector 风格）
        @Suppress("UnusedContentLambdaTargetStateParameter")
        AnimatedContent(
            targetState = yearMonth.year * 100 + yearMonth.monthValue,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = ScheduleDesignSpecs.Motion.Duration.FlatSwitch,
                        easing = FastOutSlowInEasing
                    )
                ) togetherWith fadeOut(
                    animationSpec = tween(
                        durationMillis = ScheduleDesignSpecs.Motion.Duration.FlatSwitch,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            label = "MonthTextAnimation"
        ) { _ ->
            Text(
                text = monthText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(min = 120.dp)
                    .then(
                        if (onTitleClick != null) {
                            Modifier.clickable(
                                onClickLabel = "选择年月",
                                onClick = onTitleClick
                            )
                        } else Modifier
                    )
            )
        }

        // 下个月：圆形按钮（AnimatedMonthSelector 风格）
        MonthNavButton(
            enabled = canGoNext,
            onClick = onNextMonth,
            contentDescription = "下个月"
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * 圆形月份导航按钮 - AnimatedMonthSelector 风格
 */
@Composable
private fun MonthNavButton(
    enabled: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .alpha(if (enabled) 1f else 0.38f)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        onClickLabel = contentDescription,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * 大标题顶栏
 *
 * 用于页面主标题显示，如设置页面。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleLargeTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    LargeTopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null) {
                navigationIcon()
            } else if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
