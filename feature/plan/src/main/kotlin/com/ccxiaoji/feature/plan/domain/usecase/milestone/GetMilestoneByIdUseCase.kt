package com.ccxiaoji.feature.plan.domain.usecase.milestone

import com.ccxiaoji.feature.plan.domain.model.Milestone
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import javax.inject.Inject

/**
 * 根据ID获取里程碑用例
 */
class GetMilestoneByIdUseCase @Inject constructor(
    private val planRepository: PlanRepository
) {
    suspend operator fun invoke(milestoneId: String): Milestone? {
        return try {
            planRepository.getMilestoneById(milestoneId)
        } catch (e: Exception) {
            null
        }
    }
}