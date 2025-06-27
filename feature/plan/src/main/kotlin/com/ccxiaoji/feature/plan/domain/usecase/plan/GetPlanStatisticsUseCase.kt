package com.ccxiaoji.feature.plan.domain.usecase.plan

import com.ccxiaoji.feature.plan.domain.model.*
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * 获取计划统计数据用例
 */
class GetPlanStatisticsUseCase @Inject constructor(
    private val planRepository: PlanRepository
) {
    /**
     * 获取统计数据
     */
    operator fun invoke(): Flow<PlanStatistics> {
        return planRepository.getAllPlansTree().map { plans ->
            val flatPlans = flattenPlans(plans)
            
            // 状态统计
            val totalPlans = flatPlans.size
            val completedPlans = flatPlans.count { it.status == PlanStatus.COMPLETED }
            val inProgressPlans = flatPlans.count { it.status == PlanStatus.IN_PROGRESS }
            val notStartedPlans = flatPlans.count { it.status == PlanStatus.NOT_STARTED }
            val cancelledPlans = flatPlans.count { it.status == PlanStatus.CANCELLED }
            
            // 进度统计
            val averageProgress = if (flatPlans.isNotEmpty()) {
                flatPlans.map { it.progress }.average().toFloat()
            } else {
                0f
            }
            
            // 进度分布
            val progressDistribution = calculateProgressDistribution(flatPlans)
            
            // 时间统计
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val overdueePlans = flatPlans.count { 
                it.status != PlanStatus.COMPLETED && 
                it.status != PlanStatus.CANCELLED && 
                it.endDate < today 
            }
            val upcomingDeadlines = flatPlans.count { 
                it.status != PlanStatus.COMPLETED && 
                it.status != PlanStatus.CANCELLED &&
                it.endDate >= today &&
                it.endDate <= today.plus(DatePeriod(days = 7))
            }
            
            // 月度统计（最近6个月）
            val monthlyStats = calculateMonthlyStats(flatPlans)
            
            // 标签统计
            val tagStats = calculateTagStats(flatPlans)
            
            PlanStatistics(
                totalPlans = totalPlans,
                completedPlans = completedPlans,
                inProgressPlans = inProgressPlans,
                notStartedPlans = notStartedPlans,
                cancelledPlans = cancelledPlans,
                averageProgress = averageProgress,
                progressDistribution = progressDistribution,
                overdueePlans = overdueePlans,
                upcomingDeadlines = upcomingDeadlines,
                monthlyStats = monthlyStats,
                tagStats = tagStats
            )
        }
    }
    
    /**
     * 展平计划树结构
     */
    private fun flattenPlans(plans: List<Plan>): List<Plan> {
        val result = mutableListOf<Plan>()
        plans.forEach { plan ->
            result.add(plan)
            result.addAll(flattenPlans(plan.children))
        }
        return result
    }
    
    /**
     * 计算进度分布
     */
    private fun calculateProgressDistribution(plans: List<Plan>): List<ProgressRange> {
        val ranges = listOf(
            "0-20%" to (0f..20f),
            "21-40%" to (21f..40f),
            "41-60%" to (41f..60f),
            "61-80%" to (61f..80f),
            "81-100%" to (81f..100f)
        )
        
        return ranges.map { (label, range) ->
            val count = plans.count { it.progress in range }
            val percentage = if (plans.isNotEmpty()) {
                (count.toFloat() / plans.size) * 100f
            } else {
                0f
            }
            ProgressRange(label, count, percentage)
        }
    }
    
    /**
     * 计算月度统计（最近6个月）
     */
    private fun calculateMonthlyStats(plans: List<Plan>): List<MonthlyStats> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val sixMonthsAgo = today.minus(DatePeriod(months = 6))
        
        val monthlyData = mutableMapOf<String, MutableList<Plan>>()
        
        // 初始化最近6个月
        var currentMonth = sixMonthsAgo
        while (currentMonth <= today) {
            val yearMonth = "${currentMonth.year}-${currentMonth.monthNumber.toString().padStart(2, '0')}"
            monthlyData[yearMonth] = mutableListOf()
            currentMonth = currentMonth.plus(DatePeriod(months = 1))
        }
        
        // 分组计划
        plans.forEach { plan ->
            val createdDate = Instant.fromEpochMilliseconds(plan.createdAt).toLocalDateTime(TimeZone.currentSystemDefault()).date
            val yearMonth = "${createdDate.year}-${createdDate.monthNumber.toString().padStart(2, '0')}"
            if (monthlyData.containsKey(yearMonth)) {
                monthlyData[yearMonth]?.add(plan)
            }
        }
        
        return monthlyData.map { (yearMonth, monthPlans) ->
            MonthlyStats(
                yearMonth = yearMonth,
                created = monthPlans.size,
                completed = monthPlans.count { it.status == PlanStatus.COMPLETED },
                avgProgress = if (monthPlans.isNotEmpty()) {
                    monthPlans.map { it.progress }.average().toFloat()
                } else {
                    0f
                }
            )
        }.sortedBy { it.yearMonth }
    }
    
    /**
     * 计算标签统计
     */
    private fun calculateTagStats(plans: List<Plan>): List<TagStats> {
        val tagMap = mutableMapOf<String, MutableList<Plan>>()
        
        plans.forEach { plan ->
            plan.tags.forEach { tag ->
                tagMap.getOrPut(tag) { mutableListOf() }.add(plan)
            }
        }
        
        return tagMap.map { (tag, tagPlans) ->
            TagStats(
                tag = tag,
                count = tagPlans.size,
                completedCount = tagPlans.count { it.status == PlanStatus.COMPLETED },
                avgProgress = if (tagPlans.isNotEmpty()) {
                    tagPlans.map { it.progress }.average().toFloat()
                } else {
                    0f
                }
            )
        }.sortedByDescending { it.count }
    }
}