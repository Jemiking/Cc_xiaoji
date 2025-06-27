package com.ccxiaoji.feature.plan.api

/**
 * 计划模块对外API接口
 * 提供给其他模块使用的核心功能接口
 */
interface PlanApi {
    
    /**
     * 获取计划总数
     * @return 计划总数
     */
    suspend fun getPlanCount(): Int
    
    /**
     * 获取今日需关注的计划
     * @return 今日计划列表
     */
    suspend fun getTodayPlans(): List<PlanSummary>
    
    /**
     * 获取正在进行中的计划
     * @param limit 获取数量限制，默认10个
     * @return 进行中的计划列表
     */
    suspend fun getInProgressPlans(limit: Int = 10): List<PlanSummary>
    
    /**
     * 获取即将到期的计划（7天内）
     * @return 即将到期的计划列表
     */
    suspend fun getUpcomingDeadlinePlans(): List<PlanSummary>
    
    /**
     * 根据ID获取计划摘要信息
     * @param planId 计划ID
     * @return 计划摘要信息，如果不存在返回null
     */
    suspend fun getPlanSummary(planId: String): PlanSummary?
    
    /**
     * 导航到计划模块主页
     */
    fun navigateToPlanModule()
    
    /**
     * 导航到计划详情页
     * @param planId 计划ID
     */
    fun navigateToPlanDetail(planId: String)
    
    /**
     * 导航到创建计划页面
     */
    fun navigateToCreatePlan()
    
    /**
     * 获取计划模块主页面
     * @return 计划列表页面的Composable函数
     */
    @androidx.compose.runtime.Composable
    fun getPlanScreen(): Unit
}