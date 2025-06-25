package com.ccxiaoji.feature.plan.domain.usecase.plan

import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanFilter
import com.ccxiaoji.feature.plan.domain.model.PlanSortBy
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 搜索计划用例
 */
class SearchPlansUseCase @Inject constructor(
    private val planRepository: PlanRepository
) {
    /**
     * 执行搜索
     * @param query 搜索关键词
     * @param filter 筛选条件
     * @param sortBy 排序方式
     * @return 符合条件的计划列表
     */
    operator fun invoke(
        query: String = "",
        filter: PlanFilter = PlanFilter(),
        sortBy: PlanSortBy = PlanSortBy.UPDATE_TIME_DESC
    ): Flow<List<Plan>> {
        return planRepository.getAllPlansTree().map { plans ->
            var result = plans
            
            // 1. 关键词搜索（搜索标题、描述和标签）
            if (query.isNotBlank()) {
                val searchQuery = query.trim().lowercase()
                result = result.filter { plan ->
                    plan.title.lowercase().contains(searchQuery) ||
                    plan.description.lowercase().contains(searchQuery) ||
                    plan.tags.any { tag -> tag.lowercase().contains(searchQuery) }
                }
            }
            
            // 2. 状态筛选
            if (filter.statuses.isNotEmpty()) {
                result = result.filter { it.status in filter.statuses }
            }
            
            // 3. 开始日期范围筛选
            filter.startDateRange?.let { range ->
                result = result.filter { plan ->
                    plan.startDate >= range.start && plan.startDate <= range.end
                }
            }
            
            // 4. 结束日期范围筛选
            filter.endDateRange?.let { range ->
                result = result.filter { plan ->
                    plan.endDate >= range.start && plan.endDate <= range.end
                }
            }
            
            // 5. 标签筛选
            if (filter.tags.isNotEmpty()) {
                result = result.filter { plan ->
                    plan.tags.any { it in filter.tags }
                }
            }
            
            // 6. 是否有子计划筛选
            filter.hasChildren?.let { hasChildren ->
                result = result.filter { it.hasChildren == hasChildren }
            }
            
            // 7. 排序
            result = when (sortBy) {
                PlanSortBy.NAME_ASC -> result.sortedBy { it.title }
                PlanSortBy.NAME_DESC -> result.sortedByDescending { it.title }
                PlanSortBy.CREATE_TIME_ASC -> result.sortedBy { it.createdAt }
                PlanSortBy.CREATE_TIME_DESC -> result.sortedByDescending { it.createdAt }
                PlanSortBy.UPDATE_TIME_ASC -> result.sortedBy { it.updatedAt }
                PlanSortBy.UPDATE_TIME_DESC -> result.sortedByDescending { it.updatedAt }
                PlanSortBy.START_DATE_ASC -> result.sortedBy { it.startDate }
                PlanSortBy.START_DATE_DESC -> result.sortedByDescending { it.startDate }
                PlanSortBy.END_DATE_ASC -> result.sortedBy { it.endDate }
                PlanSortBy.END_DATE_DESC -> result.sortedByDescending { it.endDate }
                PlanSortBy.PROGRESS_ASC -> result.sortedBy { it.progress }
                PlanSortBy.PROGRESS_DESC -> result.sortedByDescending { it.progress }
            }
            
            result
        }
    }
    
}