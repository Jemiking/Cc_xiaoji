package com.ccxiaoji.feature.plan.domain.usecase.template

import com.ccxiaoji.feature.plan.domain.model.*
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import com.ccxiaoji.feature.plan.domain.repository.TemplateRepository
import com.ccxiaoji.feature.plan.domain.usecase.milestone.CreateMilestoneUseCase
import com.ccxiaoji.feature.plan.domain.usecase.plan.CreatePlanUseCase
import kotlinx.datetime.*
import java.util.UUID
import javax.inject.Inject

/**
 * 应用模板创建计划用例
 */
class ApplyTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository,
    private val createPlanUseCase: CreatePlanUseCase,
    private val createMilestoneUseCase: CreateMilestoneUseCase
) {
    /**
     * 应用模板创建计划
     * @param templateId 模板ID
     * @param title 计划标题（可选，不提供则使用模板标题）
     * @param startDate 开始日期
     * @param parentId 父计划ID（可选）
     * @return 创建的主计划ID
     */
    suspend operator fun invoke(
        templateId: String,
        title: String? = null,
        startDate: LocalDate,
        parentId: String? = null
    ): String {
        // 获取模板
        val templateResult = templateRepository.getTemplateById(templateId)
        val template = when (templateResult) {
            is com.ccxiaoji.common.base.BaseResult.Success -> templateResult.data
            is com.ccxiaoji.common.base.BaseResult.Error -> throw IllegalArgumentException("获取模板失败: ${templateResult.exception.message}")
        }
        
        // 增加模板使用次数
        templateRepository.useTemplate(templateId)
        
        // 计算结束日期
        val endDate = startDate.plus(DatePeriod(days = template.duration - 1))
        
        // 创建主计划
        val mainPlan = Plan(
            id = UUID.randomUUID().toString(),
            title = title ?: template.structure.planTemplate.title,
            description = template.structure.planTemplate.description,
            startDate = startDate,
            endDate = endDate,
            parentId = parentId,
            tags = template.structure.planTemplate.tags,
            color = template.color
        )
        
        val mainPlanId = createPlanUseCase(mainPlan).getOrThrow()
        
        // 递归创建子计划
        createSubPlansFromTemplate(
            subPlanTemplates = template.structure.subPlans,
            parentId = mainPlanId,
            baseStartDate = startDate
        )
        
        // 创建主计划的里程碑
        createMilestonesFromTemplate(
            milestoneTemplates = template.structure.milestones,
            planId = mainPlanId,
            baseStartDate = startDate
        )
        
        return mainPlanId
    }
    
    /**
     * 递归创建子计划
     */
    private suspend fun createSubPlansFromTemplate(
        subPlanTemplates: List<SubPlanTemplate>,
        parentId: String,
        baseStartDate: LocalDate
    ) {
        for (subPlanTemplate in subPlanTemplates) {
            // 计算子计划的开始和结束日期
            val subStartDate = baseStartDate.plus(DatePeriod(days = subPlanTemplate.dayOffset))
            val subEndDate = subStartDate.plus(DatePeriod(days = subPlanTemplate.duration - 1))
            
            // 创建子计划
            val subPlan = Plan(
                id = UUID.randomUUID().toString(),
                title = subPlanTemplate.title,
                description = subPlanTemplate.description,
                startDate = subStartDate,
                endDate = subEndDate,
                parentId = parentId
            )
            
            val subPlanId = createPlanUseCase(subPlan).getOrThrow()
            
            // 递归创建更深层的子计划
            if (subPlanTemplate.subPlans.isNotEmpty()) {
                createSubPlansFromTemplate(
                    subPlanTemplates = subPlanTemplate.subPlans,
                    parentId = subPlanId,
                    baseStartDate = subStartDate
                )
            }
            
            // 创建子计划的里程碑
            if (subPlanTemplate.milestones.isNotEmpty()) {
                createMilestonesFromTemplate(
                    milestoneTemplates = subPlanTemplate.milestones,
                    planId = subPlanId,
                    baseStartDate = subStartDate
                )
            }
        }
    }
    
    /**
     * 创建里程碑
     */
    private suspend fun createMilestonesFromTemplate(
        milestoneTemplates: List<MilestoneTemplate>,
        planId: String,
        baseStartDate: LocalDate
    ) {
        for (milestoneTemplate in milestoneTemplates) {
            val targetDate = baseStartDate.plus(DatePeriod(days = milestoneTemplate.dayOffset))
            
            val milestone = Milestone(
                id = UUID.randomUUID().toString(),
                planId = planId,
                title = milestoneTemplate.title,
                description = milestoneTemplate.description,
                targetDate = targetDate
            )
            
            createMilestoneUseCase(planId, milestone).getOrThrow()
        }
    }
}