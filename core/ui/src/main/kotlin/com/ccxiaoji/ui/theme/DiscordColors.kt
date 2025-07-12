package com.ccxiaoji.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Discord风格配色系统
 * 基于Discord 2024最新配色方案
 */
object DiscordColors {
    // 深色主题
    object Dark {
        // 背景层级 - 从深到浅
        val BackgroundDeepest = Color(0xFF1e1f22)   // 最深层背景
        val BackgroundSidebar = Color(0xFF2b2d31)   // 侧边栏背景
        val BackgroundSecondary = Color(0xFF2b2d31) // 次要背景（与侧边栏相同）
        val BackgroundPrimary = Color(0xFF313338)   // 主内容区背景
        val SurfaceDefault = Color(0xFF383a40)      // 默认表面（卡片/按钮）
        val SurfaceHover = Color(0xFF404249)        // 悬浮状态
        val SurfaceActive = Color(0xFF41434a)       // 激活状态
        val SurfaceSelected = Color(0xFF43444b)     // 选中状态
        
        // 文字色阶
        val TextPrimary = Color(0xFFf2f3f5)         // 标题/重要文字
        val TextNormal = Color(0xFFdbdee1)          // 普通文字
        val TextSecondary = Color(0xFFb5bac1)       // 次要文字
        val TextMuted = Color(0xFF949ba4)           // 辅助文字
        val TextLink = Color(0xFF00b0f4)            // 链接文字
        
        // 其他
        val Divider = Color(0xFF3f4147)             // 分隔线
        val Border = Color(0xFF3f4147)              // 边框线（与分隔线相同）
        val ScrollbarThumb = Color(0xFF1a1b1e)      // 滚动条
        val SelectionIndicator = Color.White        // 选中指示器
    }
    
    // 浅色主题
    object Light {
        // 背景层级
        val BackgroundDeepest = Color(0xFFf2f3f5)   // 最浅层背景
        val BackgroundSidebar = Color(0xFFe3e5e8)   // 侧边栏背景
        val BackgroundSecondary = Color(0xFFf9f9f9) // 次要背景
        val BackgroundPrimary = Color(0xFFffffff)   // 主内容区背景
        val SurfaceDefault = Color(0xFFf2f3f5)      // 默认表面（卡片/按钮）
        val SurfaceHover = Color(0xFFebedef)        // 悬浮状态
        val SurfaceActive = Color(0xFFe3e5e8)       // 激活状态
        val SurfaceSelected = Color(0xFFdfe1e5)     // 选中状态
        
        // 文字色阶
        val TextPrimary = Color(0xFF060607)         // 标题/重要文字
        val TextNormal = Color(0xFF2e3338)          // 普通文字
        val TextSecondary = Color(0xFF4e5058)       // 次要文字
        val TextMuted = Color(0xFF6d6f78)           // 辅助文字
        val TextLink = Color(0xFF0068e0)            // 链接文字
        
        // 其他
        val Divider = Color(0xFFe3e5e8)             // 分隔线
        val Border = Color(0xFFe3e5e8)              // 边框线（与分隔线相同）
        val ScrollbarThumb = Color(0xFFc4c7ce)      // 滚动条
        val SelectionIndicator = Color(0xFF060607)  // 选中指示器
    }
    
    // 品牌色（通用）
    val Blurple = Color(0xFF5865F2)             // Discord标志性紫色
    val Green = Color(0xFF57F287)               // 成功/在线
    val Yellow = Color(0xFFFEE75C)              // 警告/待办
    val Red = Color(0xFFED4245)                 // 危险/错误
    val Fuchsia = Color(0xFFEB459E)             // 特殊状态
    val Orange = Color(0xFFf47b67)              // 提醒
    
    // 模块配色
    val ModuleColors = mapOf(
        "home" to Blurple,
        "ledger" to Green,
        "todo" to Yellow,
        "habit" to Fuchsia,
        "schedule" to Color(0xFF5865F2),
        "plan" to Color(0xFF57F287)
    )
    
    // 为了向后兼容，保留原来的引用（默认使用深色）
    val BackgroundDeepest = Dark.BackgroundDeepest
    val BackgroundSidebar = Dark.BackgroundSidebar
    val BackgroundSecondary = Dark.BackgroundSecondary
    val BackgroundPrimary = Dark.BackgroundPrimary
    val SurfaceDefault = Dark.SurfaceDefault
    val SurfaceHover = Dark.SurfaceHover
    val SurfaceActive = Dark.SurfaceActive
    val SurfaceSelected = Dark.SurfaceSelected
    val TextPrimary = Dark.TextPrimary
    val TextNormal = Dark.TextNormal
    val TextSecondary = Dark.TextSecondary
    val TextMuted = Dark.TextMuted
    val TextLink = Dark.TextLink
    val Divider = Dark.Divider
    val Border = Dark.Border
    val ScrollbarThumb = Dark.ScrollbarThumb
    val SelectionIndicator = Dark.SelectionIndicator
}