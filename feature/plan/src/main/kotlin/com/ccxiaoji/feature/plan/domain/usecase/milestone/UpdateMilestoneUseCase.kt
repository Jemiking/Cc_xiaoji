package com.ccxiaoji.feature.plan.domain.usecase.milestone

import com.ccxiaoji.feature.plan.util.core.ValidationException
import com.ccxiaoji.feature.plan.data.local.dao.MilestoneDao
import com.ccxiaoji.feature.plan.data.local.entity.MilestoneEntity
import com.ccxiaoji.feature.plan.domain.model.Milestone
import kotlinx.datetime.Clock
import javax.inject.Inject

/**
 * 更新里程碑用例
 */
class UpdateMilestoneUseCase @Inject constructor(
    private val milestoneDao: MilestoneDao
) {
    /**
     * 更新里程碑信息
     * @param milestone 更新后的里程碑信息
     * @return 更新结果
     */
    suspend operator fun invoke(milestone: Milestone): Result<Unit> {
        return try {
            // 验证输入
            validate(milestone)
            
            // 获取现有里程碑
            val existingEntity = milestoneDao.getMilestoneById(milestone.id)
                ?: return Result.failure(IllegalArgumentException("里程碑不存在"))
            
            // 更新实体
            val updatedEntity = existingEntity.copy(
                title = milestone.title.trim(),
                description = milestone.description.trim(),
                targetDate = milestone.targetDate.toEpochDays().toLong(),
                isCompleted = milestone.isCompleted,
                completedDate = milestone.completedDate?.toEpochDays()?.toLong()
            )
            
            // 更新数据库
            milestoneDao.updateMilestone(updatedEntity)
            
            Result.success(Unit)
        } catch (e: ValidationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("更新里程碑失败: ${e.message}", e))
        }
    }
    
    /**
     * 验证里程碑数据
     */
    private fun validate(milestone: Milestone) {
        when {
            milestone.title.isBlank() -> {
                throw ValidationException("里程碑标题不能为空")
            }
            milestone.title.length > 100 -> {
                throw ValidationException("里程碑标题不能超过100个字符")
            }
            milestone.description.length > 500 -> {
                throw ValidationException("里程碑描述不能超过500个字符")
            }
        }
    }
}