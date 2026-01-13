package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * Schedule 模块设计规范
 *
 * 定义排班助手模块专用的视觉规范，包括：
 * - 班次语义色（固定不变，确保认知惯性）
 * - 日历视觉规范（格子尺寸、状态样式）
 * - 组件尺寸规范
 *
 * @see DesignTokens 全局设计令牌
 */
object ScheduleDesignSpecs {

    // ==================== 模块主色 ====================

    /**
     * Schedule 模块主色
     * 与 DesignTokens.BrandColors.Schedule 保持一致
     */
    object ModuleColors {
        val Primary = DesignTokens.BrandColors.Schedule           // 柔和橙 #FFB74D
        val PrimaryLight = DesignTokens.BrandColors.ScheduleLight // 浅橙 #FFCC80
        val PrimaryContainer = Color(0xFFFFF3E0)                  // 极浅橙容器
        val OnPrimary = Color(0xFF000000)                         // 主色上的文字
        val OnPrimaryContainer = Color(0xFFE65100)                // 容器上的文字
    }

    // ==================== 班次语义色 ====================

    /**
     * 班次类型语义色
     *
     * 设计原则：
     * - 语义层颜色固定不变，不跟随 Dynamic Color
     * - 确保用户对班次类型的认知惯性
     * - 浅色/深色模式有对应的色值调整
     */
    object ShiftColors {

        // ---- 早班 (Morning) ----
        val MorningLight = Color(0xFF4CAF50)    // Green 500
        val MorningDark = Color(0xFF81C784)     // Green 300
        val MorningIcon: ImageVector = Icons.Filled.WbSunny

        // ---- 中班 (Afternoon) ----
        val AfternoonLight = Color(0xFF2196F3)  // Blue 500
        val AfternoonDark = Color(0xFF64B5F6)   // Blue 300
        val AfternoonIcon: ImageVector = Icons.Filled.WbTwilight

        // ---- 晚班 (Night) ----
        val NightLight = Color(0xFF9C27B0)      // Purple 500
        val NightDark = Color(0xFFBA68C8)       // Purple 300
        val NightIcon: ImageVector = Icons.Filled.Bedtime

        // ---- 特殊班 (Special) ----
        val SpecialLight = Color(0xFFFF9800)    // Orange 500
        val SpecialDark = Color(0xFFFFB74D)     // Orange 300
        val SpecialIcon: ImageVector = Icons.Filled.Star

        // ---- 加班 (Overtime) ----
        val OvertimeLight = Color(0xFFF44336)   // Red 500
        val OvertimeDark = Color(0xFFE57373)    // Red 300
        val OvertimeIcon: ImageVector = Icons.Filled.Bolt

        // ---- 休息 (Rest) ----
        val RestLight = Color(0xFF9E9E9E)       // Grey 500
        val RestDark = Color(0xFFBDBDBD)        // Grey 400
        val RestIcon: ImageVector = Icons.Filled.Coffee

        /**
         * 根据班次类型获取颜色
         * @param shiftType 班次类型标识
         * @param isDarkTheme 是否深色主题
         */
        fun getColor(shiftType: ShiftType, isDarkTheme: Boolean): Color {
            return when (shiftType) {
                ShiftType.MORNING -> if (isDarkTheme) MorningDark else MorningLight
                ShiftType.AFTERNOON -> if (isDarkTheme) AfternoonDark else AfternoonLight
                ShiftType.NIGHT -> if (isDarkTheme) NightDark else NightLight
                ShiftType.SPECIAL -> if (isDarkTheme) SpecialDark else SpecialLight
                ShiftType.OVERTIME -> if (isDarkTheme) OvertimeDark else OvertimeLight
                ShiftType.REST -> if (isDarkTheme) RestDark else RestLight
            }
        }

        /**
         * 根据班次类型获取图标
         */
        fun getIcon(shiftType: ShiftType): ImageVector {
            return when (shiftType) {
                ShiftType.MORNING -> MorningIcon
                ShiftType.AFTERNOON -> AfternoonIcon
                ShiftType.NIGHT -> NightIcon
                ShiftType.SPECIAL -> SpecialIcon
                ShiftType.OVERTIME -> OvertimeIcon
                ShiftType.REST -> RestIcon
            }
        }
    }

    /**
     * 班次类型枚举
     */
    enum class ShiftType {
        MORNING,    // 早班
        AFTERNOON,  // 中班
        NIGHT,      // 晚班
        SPECIAL,    // 特殊班
        OVERTIME,   // 加班
        REST        // 休息
    }

    // ==================== 日历视觉规范 ====================

    /**
     * 日历格子尺寸规范
     */
    object CalendarCellSizes {
        // Comfortable 模式 - 月视图，展示详细信息
        val ComfortableHeight = 64.dp
        val ComfortableMinHeight = 56.dp
        val ComfortableCellHeight = 72.dp  // 用于 DayCell

        // Medium 模式 - 默认大小
        val MediumHeight = 56.dp
        val MediumCellHeight = 64.dp       // 用于 DayCell

        // Compact 模式 - 紧凑显示，快速浏览
        val CompactHeight = 42.dp
        val CompactMinHeight = 36.dp
        val CompactCellHeight = 48.dp      // 用于 DayCell
    }

    /**
     * 日期状态样式
     */
    object DateStateStyles {
        // 今日标记
        val TodayIndicatorSize = 6.dp

        // 选中边框
        val SelectedBorderWidth = 2.dp
        val SelectedBorderColor = ModuleColors.Primary

        // 透明度
        val WeekendAlpha = 0.5f
        val OtherMonthAlpha = 0.38f
        val DisabledAlpha = 0.38f
    }

    /**
     * 班次标签 (Shift Tag/Pill) 规范
     *
     * UIDemo 风格: Pill-in-Pill 设计
     * - 12dp 圆角（可调为 50% 成 Stadium 形状）
     * - 浅背景 + 深色内容（非纯色块+白字）
     */
    object ShiftTagSpecs {
        val Shape = RoundedCornerShape(12.dp)
        val Height = 20.dp
        val MinHeight = 18.dp
        val MaxHeight = 22.dp
        val HorizontalPadding = 2.dp
        val TextStyle = "labelSmall" // 11sp, Bold
    }

    // ==================== 组件尺寸规范 ====================

    /**
     * 圆角规范
     *
     * UIDemo 风格: 12dp 统一圆角
     */
    object Corners {
        val ShiftCard = 12.dp                                   // UIDemo Standard
        val SectionCard = 12.dp                                 // UIDemo Standard
        val BottomSheet = 28.dp                                 // 更现代的圆润顶部
        val DayCell = 12.dp                                     // UIDemo Standard
    }

    /**
     * 间距规范
     */
    object Spacing {
        val CellPadding = DesignTokens.Spacing.xs            // 4dp
        val CellSpacingComfortable = DesignTokens.Spacing.small  // 8dp
        val CellSpacingCompact = 3.dp
        val SectionPadding = DesignTokens.Spacing.medium     // 16dp
        val CardGap = DesignTokens.Spacing.small             // 8dp
    }

    /**
     * 交互反馈规范
     */
    object Interaction {
        // 长按缩放
        val LongPressScale = 1.05f
        val LongPressElevation = 6.dp

        // 拖拽
        val DragGhostAlpha = 0.8f
        val DragTargetBorderWidth = 2.dp

        // 涟漪
        val RippleAlpha = 0.12f
    }

    // ==================== 动效规范 ====================

    /**
     * Schedule 模块动效令牌
     *
     * 遵循 Material 3 Motion 规范并进行模块化适配
     * 通用动效请优先使用 DesignTokens.Motion（全局一致性）
     * 这里补充 Schedule 模块特有的交互/转场令牌
     */
    object Motion {

        /**
         * 动画时长 (ms)
         *
         * UIDemo 风格: 220ms 统一标准
         */
        object Duration {
            const val FlatSwitch = 220                            // UIDemo 统一标准

            const val BottomSheetEnter = FlatSwitch
            const val BottomSheetExit = 200

            const val CellPress = 150                             // 稍快的触觉反馈
            const val CellSelection = FlatSwitch

            const val MonthSwipe = FlatSwitch
            const val SharedElement = 300                         // 共享元素可稍长

            const val PredictiveBackCommit = FlatSwitch
            const val PredictiveBackCancel = 150

            const val DragLift = 200
            const val DragFeedback = FlatSwitch

            const val DockEnter = FlatSwitch
            const val DockExit = 200
        }

        /**
         * 缓动曲线
         */
        object Easing {
            val Standard: androidx.compose.animation.core.Easing = FastOutSlowInEasing
            val Accelerate: androidx.compose.animation.core.Easing = FastOutLinearInEasing
            val Decelerate: androidx.compose.animation.core.Easing = LinearOutSlowInEasing
            val Linear: androidx.compose.animation.core.Easing = LinearEasing

            // 用于共享元素/容器变换等强调型转场
            val Emphasized: androidx.compose.animation.core.Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        }

        /**
         * 弹簧参数配置
         */
        object Spring {
            // 轻快型：按压、选中反馈
            const val DampingRatioSnappy = 0.85f
            const val StiffnessSnappy = 800f

            // 弹性型：Dock 吸附、回弹动效
            const val DampingRatioBouncy = 0.75f
            const val StiffnessBouncy = 500f

            fun <T> snappy(): SpringSpec<T> = spring(
                dampingRatio = DampingRatioSnappy,
                stiffness = StiffnessSnappy
            )

            fun <T> bouncy(): SpringSpec<T> = spring(
                dampingRatio = DampingRatioBouncy,
                stiffness = StiffnessBouncy
            )
        }

        // ==================== 动画规格工厂函数 ====================

        fun <T> bottomSheetEnterSpec() = tween<T>(
            durationMillis = Duration.BottomSheetEnter,
            easing = Easing.Emphasized
        )

        fun <T> bottomSheetExitSpec() = tween<T>(
            durationMillis = Duration.BottomSheetExit,
            easing = Easing.Accelerate
        )

        fun <T> cellPressSpec() = tween<T>(
            durationMillis = Duration.CellPress,
            easing = Easing.Decelerate
        )

        fun <T> cellSelectionSpec() = tween<T>(
            durationMillis = Duration.CellSelection,
            easing = Easing.Standard
        )

        fun <T> monthSwipeSpec() = tween<T>(
            durationMillis = Duration.MonthSwipe,
            easing = Easing.Standard
        )

        fun <T> sharedElementSpec() = tween<T>(
            durationMillis = Duration.SharedElement,
            easing = Easing.Emphasized
        )

        fun <T> predictiveBackCommitSpec() = tween<T>(
            durationMillis = Duration.PredictiveBackCommit,
            easing = Easing.Emphasized
        )

        fun <T> predictiveBackCancelSpec() = tween<T>(
            durationMillis = Duration.PredictiveBackCancel,
            easing = Easing.Standard
        )

        fun <T> dragFeedbackSpec() = tween<T>(
            durationMillis = Duration.DragFeedback,
            easing = Easing.Standard
        )

        /**
         * UIDemo 风格的 220ms 状态切换动效（颜色/透明度等）
         */
        fun <T> flatSwitchSpec() = tween<T>(
            durationMillis = Duration.FlatSwitch,
            easing = Easing.Standard
        )
    }

    /**
     * 动画时长 (兼容旧 API，后续可移除)
     */
    @Deprecated("Use Motion.Duration instead", ReplaceWith("Motion.Duration"))
    object Animation {
        const val BottomSheetEnterDuration = Motion.Duration.BottomSheetEnter
        const val BottomSheetExitDuration = Motion.Duration.BottomSheetExit
        const val CellSelectionDuration = Motion.Duration.CellSelection
        const val DragFeedbackDuration = Motion.Duration.DragFeedback
    }
}

// ==================== Composable 辅助函数 ====================

/**
 * 获取当前主题下的班次颜色
 */
@Composable
fun ShiftType.color(): Color {
    val isDark = isSystemInDarkTheme()
    return ScheduleDesignSpecs.ShiftColors.getColor(this, isDark)
}

/**
 * 获取班次类型的图标
 */
fun ShiftType.icon(): ImageVector {
    return ScheduleDesignSpecs.ShiftColors.getIcon(this)
}

/**
 * 日历视图模式
 */
@Stable
enum class CalendarDisplayMode {
    COMFORTABLE,  // 舒适模式：较大格子，显示详细信息
    COMPACT       // 紧凑模式：小格子，显示更多日期
}

/**
 * 根据显示模式获取格子高度
 */
fun CalendarDisplayMode.cellHeight(): Dp {
    return when (this) {
        CalendarDisplayMode.COMFORTABLE -> ScheduleDesignSpecs.CalendarCellSizes.ComfortableHeight
        CalendarDisplayMode.COMPACT -> ScheduleDesignSpecs.CalendarCellSizes.CompactHeight
    }
}

/**
 * 根据显示模式获取格子间距
 */
fun CalendarDisplayMode.cellSpacing(): Dp {
    return when (this) {
        CalendarDisplayMode.COMFORTABLE -> ScheduleDesignSpecs.Spacing.CellSpacingComfortable
        CalendarDisplayMode.COMPACT -> ScheduleDesignSpecs.Spacing.CellSpacingCompact
    }
}

// ==================== 类型别名 ====================

/**
 * 方便导入的类型别名
 */
typealias ShiftType = ScheduleDesignSpecs.ShiftType
