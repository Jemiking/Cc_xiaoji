package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design tokens for the Book Report replica screens.
 * Values follow the spec we aligned on for a 1:1 rebuild.
 */
object ReportTokens {
    object Palette {
        // Base
        val PageBackground = Color(0xFFF7F7F7)  // 略亮
        val Card = Color(0xFFFFFFFF)
        val Divider = Color(0xFFF0F0F0)

        // Text
        val TextPrimary = Color(0xFF1A1A1A)
        val TextSecondary = Color(0xFF666666)
        val TextMuted = Color(0xFF999999)  // 调浅用于百分比

        // Semantic amounts
        val ExpenseRed = Color(0xFFE53935)
        val IncomeGreen = Color(0xFF4CAF50)
        val TotalGray = Color(0xFF8C8C8C)

        // Accents
        val AccentOrange = Color(0xFFFF8A00)
        val IconGray = Color(0xFF9E9E9E)

        // Misc
        val ChipContainer = Color(0xFFF2F2F2)
        val ProgressTrack = Color(0xFFF0F0F0)
        val RowAlt = Color(0xFFFAFAFA)

        // Category palette (12 colors) - 提高饱和度
        val Category = listOf(
            Color(0xFFFF9500),  // 橙色（电器数码）
            Color(0xFF4A90E2),  // 蓝色（学习）
            Color(0xFF7ED321),  // 绿色（衣服）
            Color(0xFFF5A623),  // 金黄（下馆子）
            Color(0xFF9013FE),  // 紫色（超市）
            Color(0xFFFFD600),  // 黄色（买菜）
            Color(0xFF50E3C2),  // 青色
            Color(0xFF7B68EE),  // 浅紫
            Color(0xFFFF6B6B),  // 珊瑚红
            Color(0xFF4ECDC4),  // 青绿
            Color(0xFFFA8072),  // 鲑鱼红
            Color(0xFF9370DB)   // 中紫
        )
    }

    object Metrics {
        // Spacing
        val PagePadding: Dp = 16.dp
        val CardCorner: Dp = 12.dp
        val CardGap: Dp = 10.dp
        val CardPadding: Dp = 16.dp  // 调整
        val DividerThickness: Dp = 0.5.dp
        val TouchTarget: Dp = 40.dp
        val Icon: Dp = 24.dp

        // Bars / Chips
        val SegmentedHeight: Dp = 32.dp
        val SegmentedRadius: Dp = 16.dp

        // Charts
        val BarChartHeight: Dp = 152.dp
        val BarWidth: Dp = 3.dp
        val BarGap: Dp = 2.dp
        val BarRadius: Dp = 4.dp

        val DonutDiameter: Dp = 200.dp  // 增大以容纳标注
        val DonutThickness: Dp = 32.dp  // 增加厚度改善触控体验
        val DonutChartHeight: Dp = 220.dp  // 整个图表区域高度

        // Lists / Rows
        val CategoryRowHeight: Dp = 46.dp  // 减小
        val CategoryIcon: Dp = 28.dp
        val CategoryIconGap: Dp = 8.dp  // 减小
        val ProgressHeight: Dp = 4.dp  // 减小
        val ProgressRadius: Dp = 0.dp  // 直角
        val CategoryItemGap: Dp = 6.dp  // 新增

        // Table
        val TableHeaderHeight: Dp = 32.dp
        val TableRowHeight: Dp = 32.dp
    }

    object Type {
        val Title = 15.sp
        val NumberLarge = 20.sp
        val Body = 14.sp  // 增大
        val Caption = 12.sp  // 用于百分比
        val Small = 11.sp  // 用于说明文字
    }
}
