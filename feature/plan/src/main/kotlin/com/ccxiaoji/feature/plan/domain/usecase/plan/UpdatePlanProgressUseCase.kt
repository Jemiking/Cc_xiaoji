package com.ccxiaoji.feature.plan.domain.usecase.plan

import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import javax.inject.Inject

/**
 * 更新计划进度用例
 * 自动更新计划状态
 */
class UpdatePlanProgressUseCase @Inject constructor(
    private val planRepository: PlanRepository
) {
    /**
     * 执行用例
     * @param planId 计划ID
     * @param progress 新的进度值（0-100）
     */
    suspend operator fun invoke(planId: String, progress: Float): Result<Unit> {
        return try {
            require(progress in 0f..100f) { "进度必须在0-100之间" }
            
            // 更新进度
            planRepository.updatePlanProgress(planId, progress)
            
            // 根据进度自动更新状态
            val plan = planRepository.getPlanById(planId)
            if (plan != null) {
                val newStatus = when {
                    progress == 0f && plan.status == PlanStatus.NOT_STARTED -> PlanStatus.NOT_STARTED
                    progress == 100f -> PlanStatus.COMPLETED
                    progress > 0f && progress < 100f -> PlanStatus.IN_PROGRESS
                    else -> plan.status
                }
                
                if (newStatus != plan.status) {
                    planRepository.updatePlanStatus(planId, newStatus)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}