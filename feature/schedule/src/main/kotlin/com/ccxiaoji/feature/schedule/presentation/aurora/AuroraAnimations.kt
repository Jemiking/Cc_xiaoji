package com.ccxiaoji.feature.schedule.presentation.aurora

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlin.math.min

/**
 * Aurora Calm 动效规格与工具
 *
 * 设计规格：
 * - 日期点击：0.98缩放(90ms) → spring回弹(~180ms)
 * - 翻月：Shared-Axis X 300ms + stagger行入场
 * - 班次分配：Pill "飞入" 格子 260ms
 * - 页面转场：Container Transform 300ms
 * - 图表加载：300ms ease-out 拉伸
 * - 成功态：Aurora渐变流动 800ms
 * - 骨架屏：shimmer 1200ms 循环
 */
object AuroraAnimations {

    // ============================================
    // 时长常量
    // ============================================
    object Duration {
        const val TapDownMillis = 90
        const val TapBounceBackTargetMillis = 180
        const val MonthSwipeMillis = 300
        const val ShiftFlyMillis = 260
        const val ContainerTransformMillis = 300
        const val ChartLoadMillis = 300
        const val SuccessGradientFlowMillis = 800
        const val SkeletonShimmerMillis = 1200
    }

    // ============================================
    // 缓动函数
    // ============================================
    object Easings {
        val Standard: Easing = FastOutSlowInEasing
        val EaseOut: Easing = LinearOutSlowInEasing
        val Linear: Easing = LinearEasing
        /** 强调型缓动 - 用于共享元素/容器变换 */
        val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    }

    // ============================================
    // Spring 规格
    // ============================================
    object Springs {
        /**
         * 日期点击回弹 spring
         * 手感目标约180ms，带弹性回弹
         */
        fun tapBounceBack(): SpringSpec<Float> = spring(
            dampingRatio = 0.55f,
            stiffness = 1200f,
            visibilityThreshold = 0.001f
        )

        /** 通用弹性 spring */
        fun <T> bouncy(): SpringSpec<T> = spring(
            dampingRatio = 0.65f,
            stiffness = 700f
        )
    }

    // ============================================
    // Tween 规格
    // ============================================
    object Tweens {
        /** 日期按下缩放：90ms ease-out */
        fun <T> tapDown(): FiniteAnimationSpec<T> = tween(
            durationMillis = Duration.TapDownMillis,
            easing = Easings.EaseOut
        )

        /** Shared-Axis X：300ms 标准缓动 */
        fun <T> sharedAxisX(): FiniteAnimationSpec<T> = tween(
            durationMillis = Duration.MonthSwipeMillis,
            easing = Easings.Standard
        )

        /** Pill飞入格子：260ms 强调型 */
        fun <T> shiftFlyIntoCell(): FiniteAnimationSpec<T> = tween(
            durationMillis = Duration.ShiftFlyMillis,
            easing = Easings.Emphasized
        )

        /** Container Transform：300ms 强调型 */
        fun <T> containerTransform(): FiniteAnimationSpec<T> = tween(
            durationMillis = Duration.ContainerTransformMillis,
            easing = Easings.Emphasized
        )

        /** 图表加载拉伸：300ms ease-out */
        fun <T> chartLoadStretch(): FiniteAnimationSpec<T> = tween(
            durationMillis = Duration.ChartLoadMillis,
            easing = Easings.EaseOut
        )
    }

    // ============================================
    // 工具函数
    // ============================================

    /**
     * 计算stagger入场延迟
     * @param index 列表项索引
     * @param itemDelayMillis 每项延迟
     * @param startDelayMillis 起始延迟
     * @param maxDelayMillis 最大延迟
     */
    fun staggerDelayMillis(
        index: Int,
        itemDelayMillis: Int = 24,
        startDelayMillis: Int = 0,
        maxDelayMillis: Int = 200
    ): Int {
        if (index <= 0) return startDelayMillis
        val raw = startDelayMillis + index * itemDelayMillis
        return min(raw, maxDelayMillis)
    }

    /**
     * 点击缩放状态值
     * 按下时缩放到pressedScale，松开后弹簧回弹到1f
     */
    @Composable
    fun rememberTapScale(
        pressed: Boolean,
        enabled: Boolean = true,
        pressedScale: Float = 0.98f,
        downSpec: FiniteAnimationSpec<Float> = Tweens.tapDown(),
        upSpec: FiniteAnimationSpec<Float> = Springs.tapBounceBack()
    ): Float {
        val anim = remember { Animatable(1f) }
        LaunchedEffect(pressed, enabled, pressedScale) {
            if (!enabled) {
                anim.snapTo(1f)
                return@LaunchedEffect
            }
            if (pressed) {
                anim.animateTo(pressedScale, downSpec)
            } else {
                anim.animateTo(1f, upSpec)
            }
        }
        return anim.value
    }

    /**
     * 图表加载进度动画 (0..target)
     * 使用300ms ease-out
     */
    @Composable
    fun animateChartStretch(
        target: Float,
        enabled: Boolean = true,
        label: String = "AuroraChartStretch"
    ): Float {
        val progress by animateFloatAsState(
            targetValue = if (enabled) target else 0f,
            animationSpec = Tweens.chartLoadStretch(),
            label = label
        )
        return progress
    }

    /**
     * 成功态Aurora渐变流动进度 (0..1 循环)
     * 800ms周期
     */
    @Composable
    fun rememberSuccessGradientFlowProgress(
        enabled: Boolean = true,
        durationMillis: Int = Duration.SuccessGradientFlowMillis
    ): Float {
        if (!enabled) return 0f

        val transition = rememberInfiniteTransition(label = "AuroraSuccessFlow")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = durationMillis, easing = Easings.Linear),
                repeatMode = RepeatMode.Restart
            ),
            label = "progress"
        )
        return progress
    }

    /**
     * 骨架屏shimmer进度 (0..1 循环)
     * 1200ms周期
     */
    @Composable
    fun rememberShimmerProgress(
        enabled: Boolean = true,
        durationMillis: Int = Duration.SkeletonShimmerMillis
    ): Float {
        if (!enabled) return 0f

        val transition = rememberInfiniteTransition(label = "AuroraShimmer")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = durationMillis, easing = Easings.Linear),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerProgress"
        )
        return progress
    }
}
