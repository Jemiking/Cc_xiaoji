package com.ccxiaoji.feature.plan.domain.usecase.plan

import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import javax.inject.Inject

/**
 * 更新计划用例
 */
class UpdatePlanUseCase @Inject constructor(
    private val planRepository: PlanRepository
) {
    /**
     * 执行用例
     * @param plan 要更新的计划
     * @return 更新结果
     */
    suspend operator fun invoke(plan: Plan): Result<Unit> {
        return try {
            // 验证计划数据
            require(plan.id.isNotBlank()) { "计划ID不能为空" }
            require(plan.title.isNotBlank()) { "计划标题不能为空" }
            require(plan.startDate <= plan.endDate) { "开始日期不能晚于结束日期" }
            require(plan.progress in 0f..100f) { "进度必须在0-100之间" }
            
            planRepository.updatePlan(plan)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}