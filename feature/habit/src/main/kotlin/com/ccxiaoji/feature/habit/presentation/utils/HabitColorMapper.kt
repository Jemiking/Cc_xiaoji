package com.ccxiaoji.feature.habit.presentation.utils

import androidx.compose.ui.graphics.Color
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 习惯颜色映射工具
 * 将习惯的颜色字符串映射到预定义的语义颜色
 */
object HabitColorMapper {
    private val colorMap = mapOf(
        "#F44336" to DesignTokens.BrandColors.Error,
        "#E91E63" to DesignTokens.BrandColors.Error,
        "#9C27B0" to DesignTokens.BrandColors.Todo,
        "#673AB7" to DesignTokens.BrandColors.Todo,
        "#3F51B5" to DesignTokens.BrandColors.Primary,
        "#2196F3" to DesignTokens.BrandColors.Primary,
        "#03A9F4" to DesignTokens.BrandColors.Info,
        "#00BCD4" to DesignTokens.BrandColors.Info,
        "#009688" to DesignTokens.BrandColors.Success,
        "#4CAF50" to DesignTokens.BrandColors.Success,
        "#8BC34A" to DesignTokens.BrandColors.Success,
        "#CDDC39" to DesignTokens.BrandColors.Warning,
        "#FFEB3B" to DesignTokens.BrandColors.Warning,
        "#FFC107" to DesignTokens.BrandColors.Warning,
        "#FF9800" to DesignTokens.BrandColors.Warning,
        "#FF5722" to DesignTokens.BrandColors.Error,
        "#795548" to DesignTokens.BrandColors.Plan,
        "#9E9E9E" to DesignTokens.BrandColors.Schedule,
        "#607D8B" to DesignTokens.BrandColors.Ledger
    )
    
    /**
     * 获取习惯的语义颜色
     * 如果找不到映射，返回基于习惯名称的默认颜色
     */
    fun getHabitColor(colorString: String, habitTitle: String): Color {
        // 尝试从映射表获取
        colorMap[colorString.uppercase()]?.let { return it }
        
        // 根据习惯名称生成默认颜色
        return when {
            habitTitle.contains("运动") || habitTitle.contains("锻炼") || habitTitle.contains("健身") -> DesignTokens.BrandColors.Success
            habitTitle.contains("学习") || habitTitle.contains("阅读") || habitTitle.contains("读书") -> DesignTokens.BrandColors.Primary
            habitTitle.contains("工作") || habitTitle.contains("任务") -> DesignTokens.BrandColors.Todo
            habitTitle.contains("睡眠") || habitTitle.contains("休息") -> DesignTokens.BrandColors.Info
            habitTitle.contains("饮食") || habitTitle.contains("喝水") -> DesignTokens.BrandColors.Success
            habitTitle.contains("写作") || habitTitle.contains("日记") -> DesignTokens.BrandColors.Primary
            else -> {
                // 使用标题的哈希值来生成一致的颜色
                val colors = listOf(
                    DesignTokens.BrandColors.Primary,
                    DesignTokens.BrandColors.Success,
                    DesignTokens.BrandColors.Warning,
                    DesignTokens.BrandColors.Info,
                    DesignTokens.BrandColors.Todo
                )
                colors[habitTitle.hashCode().mod(colors.size)]
            }
        }
    }
}