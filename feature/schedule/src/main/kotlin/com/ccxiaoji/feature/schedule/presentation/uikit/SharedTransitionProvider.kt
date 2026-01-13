package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 共享元素转场 Scope 的 CompositionLocal
 *
 * 用于在 Schedule 模块内传递 SharedTransitionScope，
 * 使各屏幕组件可以声明共享元素。
 * 使用 staticCompositionLocalOf 因为 Scope 在导航期间不会改变。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/**
 * AnimatedVisibilityScope 的 CompositionLocal
 *
 * 用于传递当前导航目标的 AnimatedVisibilityScope，
 * 配合 SharedTransitionScope 实现跨屏幕共享元素动画。
 */
val LocalAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * 共享元素转场 Key 生成器
 *
 * 为日历与编辑页之间的共享元素生成稳定的唯一 Key。
 */
object SharedElementKeys {
    /**
     * 日期容器共享元素 Key
     * @param dateEpochDay 日期的 epoch day 值
     */
    fun dateContainer(dateEpochDay: Long): String = "date_container_$dateEpochDay"

    /**
     * 日期文字共享元素 Key
     */
    fun dateText(dateEpochDay: Long): String = "date_text_$dateEpochDay"

    /**
     * 班次标签共享元素 Key
     */
    fun shiftPill(dateEpochDay: Long): String = "shift_pill_$dateEpochDay"
}

/**
 * 共享元素转场布局包装器
 *
 * 包裹导航内容，提供 SharedTransitionScope。
 *
 * @param content 导航内容（NavHost）
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ScheduleSharedTransitionLayout(
    content: @Composable SharedTransitionScope.() -> Unit
) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            content()
        }
    }
}
