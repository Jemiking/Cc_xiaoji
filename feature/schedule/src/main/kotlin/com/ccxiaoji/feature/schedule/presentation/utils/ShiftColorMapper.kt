package com.ccxiaoji.feature.schedule.presentation.utils

import androidx.compose.ui.graphics.Color
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 班次颜色映射工具
 * 用于将班次的整数颜色值映射到设计系统中的语义化颜色
 */
object ShiftColorMapper {
    
    /**
     * 预定义的班次颜色列表
     */
    private val shiftColors = listOf(
        DesignTokens.BrandColors.Success,  // 绿色 - 早班
        DesignTokens.BrandColors.Info,     // 蓝色 - 中班  
        DesignTokens.BrandColors.Error,    // 红色 - 晚班
        DesignTokens.BrandColors.Warning,  // 橙色 - 特殊班
        DesignTokens.BrandColors.Schedule  // 紫色 - 其他班次
    )
    
    /**
     * 根据班次的颜色值获取对应的语义化颜色
     * @param colorInt 班次的整数颜色值
     * @return 对应的语义化颜色
     */
    fun getColorForShift(colorInt: Int): Color {
        // 使用颜色值的哈希码来确定映射到哪个预定义颜色
        val index = kotlin.math.abs(colorInt.hashCode()) % shiftColors.size
        return shiftColors[index]
    }
    
    /**
     * 获取班次颜色的淡化版本（用于背景）
     * @param colorInt 班次的整数颜色值
     * @param alpha 透明度
     * @return 淡化后的颜色
     */
    fun getBackgroundColorForShift(colorInt: Int, alpha: Float = 0.1f): Color {
        return getColorForShift(colorInt).copy(alpha = alpha)
    }
}