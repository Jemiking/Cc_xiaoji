package com.ccxiaoji.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset

/**
 * CC小记设计令牌系统
 * 统一管理设计规范，确保全应用的视觉一致性
 */
object DesignTokens {
    
    /**
     * 品牌颜色系统 - 极简柔和色调
     */
    object BrandColors {
        // 主色调 - 柔和蓝
        val Primary = Color(0xFF5E7CE0)           // 柔和主蓝
        val PrimaryLight = Color(0xFF8FA3E8)      // 柔和浅蓝
        val PrimaryDark = Color(0xFF4A63B8)       // 柔和深蓝
        val PrimaryContainer = Color(0xFFF0F4FF)  // 极浅蓝容器
        
        // 功能色彩 - 降低饱和度
        val Success = Color(0xFF66BB6A)           // 柔和绿(记账收入)
        val SuccessLight = Color(0xFF90C891)      // 柔和浅绿
        val Warning = Color(0xFFFFB74D)           // 柔和橙(预算警告)
        val WarningLight = Color(0xFFFFCC80)      // 柔和浅橙
        val Error = Color(0xFFEF5350)             // 柔和红(支出/错误)
        val ErrorLight = Color(0xFFFF867C)        // 柔和浅红
        val Info = Color(0xFF42A5F5)              // 柔和蓝(信息)
        val InfoLight = Color(0xFF90CAF9)         // 柔和浅蓝
        
        // 模块主题色 - 扁平化色调
        val Ledger = Color(0xFF66BB6A)            // 记账 - 柔和绿
        val LedgerLight = Color(0xFF90C891)       
        val Todo = Color(0xFF5E7CE0)              // 待办 - 柔和蓝
        val TodoLight = Color(0xFF8FA3E8)
        val Habit = Color(0xFFAB47BC)             // 习惯 - 柔和紫
        val HabitLight = Color(0xFFCE93D8)
        val Schedule = Color(0xFFFFB74D)          // 排班 - 柔和橙
        val ScheduleLight = Color(0xFFFFCC80)
        val Plan = Color(0xFF8D6E63)              // 计划 - 柔和棕
        val PlanLight = Color(0xFFBCAAA4)
    }
    
    /**
     * 间距系统
     */
    object Spacing {
        val none = 0.dp
        val xs = 4.dp        // 极小间距 - 图标和文字间
        val small = 8.dp     // 小间距 - 相关元素间
        val medium = 16.dp   // 标准间距 - 卡片内边距
        val large = 24.dp    // 大间距 - 卡片间距
        val xl = 32.dp       // 超大间距 - 章节间距
        val xxl = 48.dp      // 巨大间距 - 页面顶部间距
    }
    
    /**
     * 圆角系统 - 极简设计
     */
    object BorderRadius {
        val none = 0.dp
        val small = 4.dp     // 小圆角 - 按钮
        val medium = 8.dp    // 标准圆角 - 卡片
        val large = 12.dp    // 大圆角 - 重要卡片
        val xl = 16.dp       // 超大圆角 - 特殊容器
        val full = 50.dp     // 完全圆角 - 头像、标签
    }
    
    /**
     * 阴影系统 - 极简扁平
     */
    object Elevation {
        val none = 0.dp
        val small = 0.dp     // 无阴影 - 普通卡片使用边框
        val medium = 1.dp    // 极轻阴影 - 悬浮按钮
        val large = 2.dp     // 轻阴影 - 对话框
        val xl = 4.dp        // 标准阴影 - 导航抽屉
    }
    
    /**
     * 渐变系统
     */
    object BrandGradients {
        val Primary = Brush.linearGradient(
            colors = listOf(
                BrandColors.Primary,
                BrandColors.PrimaryLight
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
        
        val Success = Brush.linearGradient(
            colors = listOf(
                BrandColors.Success,
                BrandColors.SuccessLight
            )
        )
        
        val Warning = Brush.linearGradient(
            colors = listOf(
                BrandColors.Warning,
                BrandColors.WarningLight
            )
        )
        
        val Error = Brush.linearGradient(
            colors = listOf(
                BrandColors.Error,
                BrandColors.ErrorLight
            )
        )
        
        // 模块渐变
        val ModuleLedger = Brush.linearGradient(
            colors = listOf(
                BrandColors.Ledger,
                BrandColors.LedgerLight
            )
        )
        
        val ModuleTodo = Brush.linearGradient(
            colors = listOf(
                BrandColors.Todo,
                BrandColors.TodoLight
            )
        )
        
        val ModuleHabit = Brush.linearGradient(
            colors = listOf(
                BrandColors.Habit,
                BrandColors.HabitLight
            )
        )
        
        val ModuleSchedule = Brush.linearGradient(
            colors = listOf(
                BrandColors.Schedule,
                BrandColors.ScheduleLight
            )
        )
        
        val ModulePlan = Brush.linearGradient(
            colors = listOf(
                BrandColors.Plan,
                BrandColors.PlanLight
            )
        )
        
        // 毛玻璃背景渐变
        val GlassBackground = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.8f),
                Color.White.copy(alpha = 0.6f)
            )
        )
        
        val GlassBackgroundDark = Brush.linearGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.8f),
                Color.Black.copy(alpha = 0.6f)
            )
        )
    }
}