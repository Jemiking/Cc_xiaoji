package com.ccxiaoji.feature.plan.domain.usecase.plan

import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import kotlinx.datetime.Clock
import javax.inject.Inject

/**
 * 更新计划状态用例
 */
class UpdatePlanStatusUseCase @Inject constructor(
    private val planRepository: PlanRepository
) {
    /**
     * 更新指定计划的状态
     * @param planId 计划ID
     * @param status 新状态
     * @return 操作结果
     */
    suspend operator fun invoke(planId: String, status: PlanStatus): Result<Unit> {
        return try {
            // 验证计划是否存在
            val plan = planRepository.getPlanById(planId)
                ?: return Result.failure(IllegalArgumentException("计划不存在"))
            
            // 更新状态
            val updatedPlan = plan.copy(
                status = status,
                updatedAt = Clock.System.now().toEpochMilliseconds()
            )
            
            planRepository.updatePlan(updatedPlan)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}