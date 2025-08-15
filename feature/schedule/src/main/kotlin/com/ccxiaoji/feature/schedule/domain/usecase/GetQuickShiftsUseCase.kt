package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 获取快速选择班次用例
 * 
 * 返回最常用的班次列表，用于快速选择功能
 */
class GetQuickShiftsUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    
    /**
     * 获取快速选择班次列表
     * 
     * @param limit 返回的班次数量限制，默认为3
     * @return 常用班次列表
     */
    operator fun invoke(limit: Int = 3): Flow<List<Shift>> {
        return repository.getActiveShifts().map { shifts ->
            // 获取所有激活的班次
            val activeShifts = shifts.filter { it.isActive }
            
            // 如果班次数量少于等于限制，直接返回所有班次
            if (activeShifts.size <= limit) {
                activeShifts
            } else {
                // 否则返回前N个班次（按创建时间排序）
                // TODO: 未来可以根据使用频率排序
                activeShifts.take(limit)
            }
        }
    }
    
    /**
     * 获取班次使用统计（预留接口）
     * 
     * 未来可以实现基于使用频率的排序
     */
    suspend fun getShiftUsageStatistics(): Map<Long, Int> {
        // TODO: 实现班次使用频率统计
        // 可以通过查询schedule表中各班次的使用次数来实现
        return emptyMap()
    }
}