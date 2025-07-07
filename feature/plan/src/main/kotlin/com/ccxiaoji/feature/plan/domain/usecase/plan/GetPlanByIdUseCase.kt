package com.ccxiaoji.feature.plan.domain.usecase.plan

import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import javax.inject.Inject

/**
 * 根据ID获取计划用例
 */
class GetPlanByIdUseCase @Inject constructor(
    private val planRepository: PlanRepository
) {
    suspend operator fun invoke(planId: String): Plan? {
        return try {
            planRepository.getPlanById(planId)
        } catch (e: Exception) {
            null
        }
    }
}