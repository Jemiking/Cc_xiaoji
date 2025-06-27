package com.ccxiaoji.feature.plan.domain.usecase.plan

import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import javax.inject.Inject

/**
 * 删除计划用例
 * 会级联删除所有子计划
 */
class DeletePlanUseCase @Inject constructor(
    private val planRepository: PlanRepository
) {
    /**
     * 执行用例
     * @param planId 要删除的计划ID
     */
    suspend operator fun invoke(planId: String): Result<Unit> {
        return try {
            planRepository.deletePlan(planId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}