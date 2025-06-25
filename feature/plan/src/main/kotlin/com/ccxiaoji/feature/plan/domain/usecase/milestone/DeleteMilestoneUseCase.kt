package com.ccxiaoji.feature.plan.domain.usecase.milestone

import com.ccxiaoji.feature.plan.data.local.dao.MilestoneDao
import javax.inject.Inject

/**
 * 删除里程碑用例
 */
class DeleteMilestoneUseCase @Inject constructor(
    private val milestoneDao: MilestoneDao
) {
    /**
     * 删除指定的里程碑
     * @param milestoneId 里程碑ID
     * @return 删除结果
     */
    suspend operator fun invoke(milestoneId: String): Result<Unit> {
        return try {
            // 检查里程碑是否存在
            val milestone = milestoneDao.getMilestoneById(milestoneId)
                ?: return Result.failure(IllegalArgumentException("里程碑不存在"))
            
            // 删除里程碑
            milestoneDao.deleteMilestone(milestone)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("删除里程碑失败: ${e.message}", e))
        }
    }
}