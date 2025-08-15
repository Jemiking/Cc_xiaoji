package com.ccxiaoji.feature.plan.domain.usecase.milestone

import com.ccxiaoji.feature.plan.util.core.ValidationException
import com.ccxiaoji.feature.plan.data.local.dao.MilestoneDao
import com.ccxiaoji.feature.plan.data.local.entity.MilestoneEntity
import com.ccxiaoji.feature.plan.domain.model.Milestone
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import java.util.UUID
import javax.inject.Inject

/**
 * 创建里程碑用例
 */
class CreateMilestoneUseCase @Inject constructor(
    private val milestoneDao: MilestoneDao
) {
    /**
     * 创建新的里程碑
     * @param planId 所属计划ID
     * @param milestone 里程碑信息
     * @return 创建结果，包含里程碑ID
     */
    suspend operator fun invoke(planId: String, milestone: Milestone): Result<String> {
        return try {
            // 验证输入
            validate(milestone)
            
            // 生成ID
            val milestoneId = UUID.randomUUID().toString()
            val currentTime = Clock.System.now().toEpochMilliseconds()
            
            // 创建实体
            val entity = MilestoneEntity(
                id = milestoneId,
                planId = planId,
                title = milestone.title.trim(),
                description = milestone.description.trim(),
                targetDate = milestone.targetDate.toEpochDays().toLong(),
                isCompleted = false,
                completedDate = null,
                createdAt = currentTime
            )
            
            // 插入数据库
            milestoneDao.insertMilestone(entity)
            
            Result.success(milestoneId)
        } catch (e: ValidationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("创建里程碑失败: ${e.message}", e))
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