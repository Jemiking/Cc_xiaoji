package com.ccxiaoji.app.data.mapper

import com.ccxiaoji.app.domain.usecase.excel.HabitData
import com.ccxiaoji.feature.habit.api.HabitItem
import javax.inject.Inject

/**
 * 习惯数据映射器
 * 负责处理习惯模块相关的数据转换
 * 
 * 主要职责：
 * 1. API数据类型 → 业务数据类型
 * 2. 缺失字段的默认值处理
 * 3. 周期文本映射
 */
class HabitDataMapper @Inject constructor() {
    
    /**
     * 将习惯记录转换为Excel导出数据
     * @param habit API习惯记录
     * @return Excel导出用的习惯数据
     */
    fun mapHabitToExportData(habit: HabitItem): HabitData {
        return HabitData(
            title = habit.title,
            description = habit.description,
            period = getPeriodText(habit.period),
            target = habit.target,
            color = "#4CAF50", // HabitItem没有color字段，使用默认绿色
            currentStreak = habit.currentStreak,
            longestStreak = habit.currentStreak // HabitItem没有longestStreak字段，使用currentStreak
        )
    }
    
    /**
     * 批量转换习惯记录
     * @param habits API习惯记录列表
     * @return Excel导出用的习惯数据列表
     */
    fun mapHabitListToExportData(habits: List<HabitItem>): List<HabitData> {
        return habits.map { mapHabitToExportData(it) }
    }
    
    /**
     * 获取周期的中文描述
     * @param period 周期类型
     * @return 中文描述
     */
    fun getPeriodText(period: String): String {
        return when (period.lowercase()) {
            "daily" -> "每日"
            "weekly" -> "每周"
            "monthly" -> "每月"
            "yearly" -> "每年"
            else -> period
        }
    }
    
    /**
     * 计算完成率
     * @param currentStreak 当前连续天数
     * @param target 目标天数
     * @return 完成率（0-100）
     */
    fun calculateCompletionRate(currentStreak: Int, target: Int): Double {
        if (target <= 0) return 0.0
        val rate = (currentStreak.toDouble() / target) * 100
        return rate.coerceIn(0.0, 100.0)
    }
    
    /**
     * 获取习惯状态描述
     * @param currentStreak 当前连续天数
     * @param target 目标天数
     * @return 状态描述
     */
    fun getHabitStatusText(currentStreak: Int, target: Int): String {
        val rate = calculateCompletionRate(currentStreak, target)
        return when {
            rate >= 100 -> "已达成目标"
            rate >= 80 -> "即将达成"
            rate >= 50 -> "进展良好"
            rate >= 20 -> "继续努力"
            currentStreak > 0 -> "刚刚开始"
            else -> "未开始"
        }
    }
    
    /**
     * 获取推荐的颜色（基于完成率）
     * @param currentStreak 当前连续天数
     * @param target 目标天数
     * @return 颜色代码
     */
    fun getRecommendedColor(currentStreak: Int, target: Int): String {
        val rate = calculateCompletionRate(currentStreak, target)
        return when {
            rate >= 100 -> "#4CAF50" // 绿色 - 完成
            rate >= 80 -> "#8BC34A"  // 浅绿色 - 即将完成
            rate >= 50 -> "#FFC107"  // 黄色 - 进展中
            rate >= 20 -> "#FF9800"  // 橙色 - 需要努力
            else -> "#F44336"         // 红色 - 刚开始或未开始
        }
    }
    
    /**
     * 格式化连续天数显示
     * @param streak 连续天数
     * @return 格式化文本
     */
    fun formatStreakText(streak: Int): String {
        return when {
            streak == 0 -> "未开始"
            streak == 1 -> "1天"
            streak < 7 -> "${streak}天"
            streak < 30 -> "${streak}天（${streak / 7}周）"
            streak < 365 -> "${streak}天（${streak / 30}个月）"
            else -> "${streak}天（${streak / 365}年）"
        }
    }
}