package com.ccxiaoji.feature.plan.domain.usecase.plan

import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import javax.inject.Inject

/**
 * 清空所有计划数据用例
 * 用于开发测试，清空数据库中的所有计划和相关数据
 */
class DeleteAllPlansUseCase @Inject constructor(
    private val planRepository: PlanRepository
) {
    /**
     * 执行清空所有计划数据
     * @return Result 操作结果
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            planRepository.deleteAllPlans()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}