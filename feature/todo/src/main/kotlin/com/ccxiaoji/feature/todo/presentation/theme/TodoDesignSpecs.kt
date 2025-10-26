package com.ccxiaoji.feature.todo.presentation.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Todo 模块局部设计规范（8pt 栅格 + 字体）
 * 仅在 Todo 模块内使用，跨模块请继续沿用 core/ui 的 DesignTokens
 */
object TodoGrid {
    val x1 = 8.dp   // 最小单位
    val x2 = 16.dp  // 标准间距
    val x3 = 24.dp  // 中等间距
    val x4 = 32.dp  // 大间距
    val x5 = 40.dp  // 组件高度（如选择器）
}

object TodoTypography {
    val largeTitle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
    val title = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium)
    val body = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
    val caption = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
}

