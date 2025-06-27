package com.ccxiaoji.feature.plan.domain.usecase.milestone

import com.ccxiaoji.feature.plan.data.local.dao.MilestoneDao
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * 切换里程碑完成状态用例
 */
class ToggleMilestoneUseCase @Inject constructor(
    private val milestoneDao: MilestoneDao
) {
    /**
     * 切换里程碑的完成状态
     * @param milestoneId 里程碑ID
     * @return 操作结果
     */
    suspend operator fun invoke(milestoneId: String): Result<Unit> {
        return try {
            // 获取里程碑
            val milestone = milestoneDao.getMilestoneById(milestoneId)
                ?: return Result.failure(IllegalArgumentException("里程碑不存在"))
            
            // 切换状态
            val now = Clock.System.now()
            val updatedMilestone = if (milestone.isCompleted) {
                // 标记为未完成
                milestone.copy(
                    isCompleted = false,
                    completedDate = null
                )
            } else {
                // 标记为已完成
                val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                milestone.copy(
                    isCompleted = true,
                    completedDate = today.toEpochDays().toLong()
                )
            }
            
            // 更新数据库
            milestoneDao.updateMilestone(updatedMilestone)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("更新里程碑状态失败: ${e.message}", e))
        }
    }
}