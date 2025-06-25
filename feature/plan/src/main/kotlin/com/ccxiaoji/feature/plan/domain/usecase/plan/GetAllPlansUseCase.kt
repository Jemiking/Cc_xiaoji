package com.ccxiaoji.feature.plan.domain.usecase.plan

import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取所有计划用例
 * 返回树形结构的计划列表
 */
class GetAllPlansUseCase @Inject constructor(
    private val planRepository: PlanRepository
) {
    /**
     * 执行用例
     * @return 计划树形结构的Flow
     */
    operator fun invoke(): Flow<List<Plan>> {
        return planRepository.getAllPlansTree()
    }
}