package com.ccxiaoji.feature.plan.domain.model

import kotlinx.datetime.LocalDate

/**
 * 计划统计数据
 */
data class PlanStatistics(
    // 总体统计
    val totalPlans: Int,
    val completedPlans: Int,
    val inProgressPlans: Int,
    val notStartedPlans: Int,
    val cancelledPlans: Int,
    
    // 进度统计
    val averageProgress: Float,
    val progressDistribution: List<ProgressRange>,
    
    // 时间统计
    val overdueePlans: Int,
    val upcomingDeadlines: Int,
    val monthlyStats: List<MonthlyStats>,
    
    // 标签统计
    val tagStats: List<TagStats>
) {
    /**
     * 总体完成率
     */
    val completionRate: Float
        get() = if (totalPlans > 0) {
            (completedPlans.toFloat() / totalPlans) * 100f
        } else {
            0f
        }
}

/**
 * 进度区间统计
 */
data class ProgressRange(
    val range: String,      // 例如: "0-20%", "21-40%"
    val count: Int,         // 该区间的计划数量
    val percentage: Float   // 占总计划的百分比
)

/**
 * 月度统计
 */
data class MonthlyStats(
    val yearMonth: String,  // 格式: "2025-06"
    val created: Int,       // 创建的计划数
    val completed: Int,     // 完成的计划数
    val avgProgress: Float  // 平均进度
)

/**
 * 标签统计
 */
data class TagStats(
    val tag: String,
    val count: Int,
    val completedCount: Int,
    val avgProgress: Float
)