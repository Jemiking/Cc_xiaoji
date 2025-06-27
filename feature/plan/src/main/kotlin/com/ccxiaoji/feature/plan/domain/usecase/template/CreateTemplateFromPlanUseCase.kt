package com.ccxiaoji.feature.plan.domain.usecase.template

import com.ccxiaoji.feature.plan.domain.model.*
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import com.ccxiaoji.feature.plan.domain.repository.TemplateRepository
import kotlinx.datetime.Clock
import javax.inject.Inject

/**
 * 从现有计划创建模板用例
 * 允许用户将成功的计划保存为模板供以后使用
 */
class CreateTemplateFromPlanUseCase @Inject constructor(
    private val planRepository: PlanRepository,
    private val templateRepository: TemplateRepository
) {
    /**
     * 从现有计划创建模板
     * @param planId 计划ID
     * @param templateTitle 模板标题
     * @param templateDescription 模板描述
     * @param category 模板分类
     * @param isPublic 是否公开
     * @param userId 创建用户ID
     * @return 创建的模板ID
     */
    suspend operator fun invoke(
        planId: String,
        templateTitle: String,
        templateDescription: String,
        category: TemplateCategory,
        isPublic: Boolean = false,
        userId: String = "user"
    ): String {
        // 获取计划及其所有子计划
        val plan = planRepository.getPlanById(planId)
            ?: throw IllegalArgumentException("计划不存在: $planId")
        
        // 计算计划的总天数
        val duration = calculateDuration(plan)
        
        // 构建模板结构
        val structure = buildTemplateStructure(plan)
        
        // 创建模板
        val template = Template(
            title = templateTitle,
            description = templateDescription,
            category = category,
            tags = plan.tags,
            color = plan.color,
            duration = duration,
            structure = structure,
            isSystem = false,
            isPublic = isPublic,
            createdBy = userId,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        return templateRepository.createTemplate(template)
    }
    
    /**
     * 计算计划的总天数
     */
    private fun calculateDuration(plan: Plan): Int {
        val daysBetween = plan.endDate.toEpochDays() - plan.startDate.toEpochDays()
        return daysBetween.toInt() + 1
    }
    
    /**
     * 构建模板结构
     */
    private suspend fun buildTemplateStructure(plan: Plan): TemplateStructure {
        // 创建主计划模板
        val planTemplate = PlanTemplate(
            title = plan.title,
            description = plan.description,
            tags = plan.tags
        )
        
        // 递归构建子计划模板
        val subPlanTemplates = buildSubPlanTemplates(plan, plan.startDate)
        
        // 构建里程碑模板
        val milestoneTemplates = plan.milestones.map { milestone ->
            MilestoneTemplate(
                title = milestone.title,
                description = milestone.description,
                dayOffset = calculateDayOffset(plan.startDate, milestone.targetDate)
            )
        }
        
        return TemplateStructure(
            planTemplate = planTemplate,
            subPlans = subPlanTemplates,
            milestones = milestoneTemplates
        )
    }
    
    /**
     * 递归构建子计划模板
     */
    private suspend fun buildSubPlanTemplates(
        parentPlan: Plan,
        baseStartDate: kotlinx.datetime.LocalDate
    ): List<SubPlanTemplate> {
        val subPlans = planRepository.getChildPlans(parentPlan.id)
        
        return subPlans.map { subPlan ->
            val dayOffset = calculateDayOffset(baseStartDate, subPlan.startDate)
            val duration = calculateDuration(subPlan)
            
            // 递归获取更深层的子计划
            val nestedSubPlans = buildSubPlanTemplates(subPlan, subPlan.startDate)
            
            // 构建里程碑
            val milestones = subPlan.milestones.map { milestone ->
                MilestoneTemplate(
                    title = milestone.title,
                    description = milestone.description,
                    dayOffset = calculateDayOffset(subPlan.startDate, milestone.targetDate)
                )
            }
            
            SubPlanTemplate(
                title = subPlan.title,
                description = subPlan.description,
                dayOffset = dayOffset,
                duration = duration,
                subPlans = nestedSubPlans,
                milestones = milestones
            )
        }
    }
    
    /**
     * 计算日期偏移天数
     */
    private fun calculateDayOffset(
        baseDate: kotlinx.datetime.LocalDate,
        targetDate: kotlinx.datetime.LocalDate
    ): Int {
        return (targetDate.toEpochDays() - baseDate.toEpochDays()).toInt()
    }
}