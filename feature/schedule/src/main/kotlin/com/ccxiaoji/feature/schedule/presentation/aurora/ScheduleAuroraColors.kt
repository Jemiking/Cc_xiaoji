package com.ccxiaoji.feature.schedule.presentation.aurora

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Aurora Calm 设计系统颜色定义
 *
 * 设计理念：柔光极简 + 蓝紫渐变主色调
 * 班次使用语义色彩系统，品牌色为Aurora渐变
 */
@Immutable
object ScheduleAuroraColors {

    // ============================================
    // Brand Colors (品牌色 - Aurora 渐变)
    // ============================================
    val PrimaryBlue = Color(0xFF4F8CFF)
    val PrimaryPurple = Color(0xFF7B61FF)

    /** 稳定的Aurora渐变Brush单例，避免高频重组时重复创建 */
    val AuroraGradient: Brush = Brush.linearGradient(
        colors = listOf(PrimaryBlue, PrimaryPurple)
    )

    /** 用于水平方向的Aurora渐变 */
    val AuroraGradientHorizontal: Brush = Brush.horizontalGradient(
        colors = listOf(PrimaryBlue, PrimaryPurple)
    )

    // ============================================
    // Surface Colors - Light Mode (表面色 - 浅色模式)
    // ============================================
    val BackgroundLight = Color(0xFFF6F7FB)
    val CardSurfaceLight = Color(0xFFFFFFFF)
    val BorderLight = Color(0xFFE6E8F0)
    val WeekendBackground = Color(0xFFF1F5FF)

    // ============================================
    // Surface Colors - Dark Mode (表面色 - 深色模式)
    // ============================================
    val BackgroundDark = Color(0xFF070A14)
    val CardSurfaceDark = Color(0xFF0E1326)
    val CardSurfaceElevatedDark = Color(0xFF141B34)
    val BorderDark = Color(0xFF27304A)

    // ============================================
    // Text Colors - Light Mode (文本色 - 浅色模式)
    // ============================================
    val TextPrimaryLight = Color(0xFF0F172A)
    val TextSecondaryLight = Color(0xFF5B6477)
    val TextTertiaryLight = Color(0xFF8B95AA)

    // ============================================
    // Text Colors - Dark Mode (文本色 - 深色模式)
    // ============================================
    val TextPrimaryDark = Color(0xFFEAF0FF)
    val TextSecondaryDark = Color(0xFFB8C0D9)
    val TextTertiaryDark = Color(0xFF7F8AA8)

    // ============================================
    // Shift Semantic Colors (班次语义色)
    // 每个班次类型都有独特的视觉身份
    // ============================================

    /** 早班 - 日出橙 (Sunrise) */
    object Morning {
        val primary = Color(0xFFFF8A34)
        val background = Color(0xFFFFF1E6)
        val onPrimary = Color(0xFFFFFFFF)
        // 深色模式调整
        val primaryDark = Color(0xFFFFAB6B)
        val backgroundDark = Color(0xFF33281A)
    }

    /** 中班 - 晴空青 (Daylight) */
    object Afternoon {
        val primary = Color(0xFF23B5D3)
        val background = Color(0xFFE7F8FD)
        val onPrimary = Color(0xFFFFFFFF)
        // 深色模式调整
        val primaryDark = Color(0xFF5ECCE3)
        val backgroundDark = Color(0xFF162B30)
    }

    /** 晚班 - 深靛紫 (Midnight) */
    object Night {
        val primary = Color(0xFF3F37C9)
        val background = Color(0xFFECEBFF)
        val onPrimary = Color(0xFFFFFFFF)
        // 深色模式调整
        val primaryDark = Color(0xFF7B73E8)
        val backgroundDark = Color(0xFF1A1833)
    }

    /** 休息 - 静谧青绿 (Nature) */
    object Rest {
        val primary = Color(0xFF2A9D8F)
        val background = Color(0xFFE6FAF6)
        val onPrimary = Color(0xFFFFFFFF)
        // 深色模式调整
        val primaryDark = Color(0xFF5FBFB3)
        val backgroundDark = Color(0xFF162B28)
    }

    // ============================================
    // Helper Functions (辅助函数)
    // ============================================

    /**
     * 根据班次类型获取主色
     * @param shiftType 0=休息, 1=早班, 2=中班, 3=晚班
     * @param isDarkMode 是否深色模式
     */
    fun getShiftPrimaryColor(shiftType: Int, isDarkMode: Boolean = false): Color {
        return when (shiftType) {
            0 -> if (isDarkMode) Rest.primaryDark else Rest.primary
            1 -> if (isDarkMode) Morning.primaryDark else Morning.primary
            2 -> if (isDarkMode) Afternoon.primaryDark else Afternoon.primary
            3 -> if (isDarkMode) Night.primaryDark else Night.primary
            else -> if (isDarkMode) TextSecondaryDark else TextSecondaryLight
        }
    }

    /**
     * 根据班次类型获取背景色
     */
    fun getShiftBackgroundColor(shiftType: Int, isDarkMode: Boolean = false): Color {
        return when (shiftType) {
            0 -> if (isDarkMode) Rest.backgroundDark else Rest.background
            1 -> if (isDarkMode) Morning.backgroundDark else Morning.background
            2 -> if (isDarkMode) Afternoon.backgroundDark else Afternoon.background
            3 -> if (isDarkMode) Night.backgroundDark else Night.background
            else -> if (isDarkMode) BackgroundDark else BackgroundLight
        }
    }

    /**
     * 获取班次类型的渐变画刷（用于特殊强调）
     */
    fun getShiftGradient(shiftType: Int): Brush {
        val (start, end) = when (shiftType) {
            1 -> Morning.primary to Color(0xFFFFB74D)
            2 -> Afternoon.primary to Color(0xFF4DD0E1)
            3 -> Night.primary to Color(0xFF7C4DFF)
            0 -> Rest.primary to Color(0xFF4DB6AC)
            else -> PrimaryBlue to PrimaryPurple
        }
        return Brush.linearGradient(colors = listOf(start, end))
    }
}

/**
 * Aurora 主题色彩配置
 * 用于 CompositionLocal 提供
 */
@Immutable
data class AuroraColorScheme(
    val background: Color,
    val cardSurface: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val primaryBlue: Color,
    val primaryPurple: Color,
    val isDark: Boolean
) {
    companion object {
        val Light = AuroraColorScheme(
            background = ScheduleAuroraColors.BackgroundLight,
            cardSurface = ScheduleAuroraColors.CardSurfaceLight,
            border = ScheduleAuroraColors.BorderLight,
            textPrimary = ScheduleAuroraColors.TextPrimaryLight,
            textSecondary = ScheduleAuroraColors.TextSecondaryLight,
            textTertiary = ScheduleAuroraColors.TextTertiaryLight,
            primaryBlue = ScheduleAuroraColors.PrimaryBlue,
            primaryPurple = ScheduleAuroraColors.PrimaryPurple,
            isDark = false
        )

        val Dark = AuroraColorScheme(
            background = ScheduleAuroraColors.BackgroundDark,
            cardSurface = ScheduleAuroraColors.CardSurfaceDark,
            border = ScheduleAuroraColors.BorderDark,
            textPrimary = ScheduleAuroraColors.TextPrimaryDark,
            textSecondary = ScheduleAuroraColors.TextSecondaryDark,
            textTertiary = ScheduleAuroraColors.TextTertiaryDark,
            primaryBlue = Color(0xFF6AA6FF), // 深色模式下稍亮
            primaryPurple = Color(0xFF9A86FF),
            isDark = true
        )
    }
}

val LocalAuroraColors = staticCompositionLocalOf { AuroraColorScheme.Light }
