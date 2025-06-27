package com.ccxiaoji.feature.plan.api

import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PlanApi接口的实现类
 * 提供计划模块的对外功能实现
 */
@Singleton
class PlanApiImpl @Inject constructor(
    private val planRepository: PlanRepository
) : PlanApi {
    
    override suspend fun getPlanCount(): Int {
        return planRepository.getAllPlansTree().first().flattenPlans().size
    }
    
    override suspend fun getTodayPlans(): List<PlanSummary> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val allPlans = planRepository.getAllPlansTree().first().flattenPlans()
        
        // 筛选出今日需要关注的计划
        return allPlans.filter { plan ->
            // 今天开始的计划
            plan.startDate == today ||
            // 今天结束的计划
            plan.endDate == today ||
            // 正在进行中且包含今天的计划
            (plan.status == PlanStatus.IN_PROGRESS && 
             plan.startDate <= today && 
             plan.endDate >= today)
        }.map { it.toPlanSummary() }
    }
    
    override suspend fun getInProgressPlans(limit: Int): List<PlanSummary> {
        val allPlans = planRepository.getAllPlansTree().first().flattenPlans()
        
        return allPlans
            .filter { it.status == PlanStatus.IN_PROGRESS }
            .sortedByDescending { it.priority }
            .take(limit)
            .map { it.toPlanSummary() }
    }
    
    override suspend fun getUpcomingDeadlinePlans(): List<PlanSummary> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val sevenDaysLater = today.plus(DatePeriod(days = 7))
        val allPlans = planRepository.getAllPlansTree().first().flattenPlans()
        
        return allPlans.filter { plan ->
            plan.status != PlanStatus.COMPLETED &&
            plan.status != PlanStatus.CANCELLED &&
            plan.endDate >= today &&
            plan.endDate <= sevenDaysLater
        }.sortedBy { it.endDate }
        .map { it.toPlanSummary() }
    }
    
    override suspend fun getPlanSummary(planId: String): PlanSummary? {
        return planRepository.getPlanById(planId)?.toPlanSummary()
    }
    
    
    /**
     * 递归展平计划树
     */
    private fun List<Plan>.flattenPlans(): List<Plan> {
        val result = mutableListOf<Plan>()
        forEach { plan ->
            result.add(plan)
            result.addAll(plan.children.flattenPlans())
        }
        return result
    }
    
    /**
     * 将Plan领域模型转换为PlanSummary DTO
     */
    private fun Plan.toPlanSummary(): PlanSummary {
        return PlanSummary(
            id = id,
            title = title,
            description = description,
            startDate = startDate,
            endDate = endDate,
            progress = progress,
            status = status.toPlanStatusDto(),
            priority = priority,
            hasChildren = hasChildren
        )
    }
    
    /**
     * 将PlanStatus转换为PlanStatusDto
     */
    private fun PlanStatus.toPlanStatusDto(): PlanStatusDto {
        return when (this) {
            PlanStatus.NOT_STARTED -> PlanStatusDto.NOT_STARTED
            PlanStatus.IN_PROGRESS -> PlanStatusDto.IN_PROGRESS
            PlanStatus.COMPLETED -> PlanStatusDto.COMPLETED
            PlanStatus.CANCELLED -> PlanStatusDto.CANCELLED
        }
    }
    
    override fun navigateToPlanModule() {
        // 导航到计划模块主页面
        // 注意：这个方法主要用于模块间导航，实际导航由上层控制
        // 这里只提供导航标识符，实际导航在NavGraph中实现
    }
    
    override fun navigateToPlanDetail(planId: String) {
        // 导航到计划详情页面
        // 注意：这个方法主要用于模块间导航，实际导航由上层控制
        // 导航路径：plan_detail/{planId}
    }
    
    override fun navigateToCreatePlan() {
        // 导航到创建计划页面
        // 注意：这个方法主要用于模块间导航，实际导航由上层控制
        // 导航路径：create_plan
    }
    
    @androidx.compose.runtime.Composable
    override fun getPlanScreen() {
        com.ccxiaoji.feature.plan.presentation.navigation.PlanNavigation(
            navController = androidx.navigation.compose.rememberNavController()
        )
    }
}