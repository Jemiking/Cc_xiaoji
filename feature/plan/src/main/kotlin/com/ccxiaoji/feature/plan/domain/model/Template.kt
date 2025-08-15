package com.ccxiaoji.feature.plan.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * 计划模板领域模型
 */
data class Template(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: TemplateCategory,
    val tags: List<String> = emptyList(),
    val color: String = "#FF6B6B",
    val duration: Int, // 计划持续天数
    val structure: TemplateStructure,
    val useCount: Int = 0,
    val rating: Float = 0f,
    val isSystem: Boolean = false,
    val isPublic: Boolean = true,
    val createdBy: String = "system",
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC
) {
    /**
     * 计算模板的评级（满分5星）
     */
    val ratingStars: Int
        get() = rating.toInt().coerceIn(0, 5)
    
    /**
     * 是否为用户自定义模板
     */
    val isUserTemplate: Boolean
        get() = !isSystem
    
    /**
     * 获取分类显示名称
     */
    val categoryDisplayName: String
        get() = category.displayName
}

/**
 * 模板结构
 */
data class TemplateStructure(
    val planTemplate: PlanTemplate,
    val subPlans: List<SubPlanTemplate> = emptyList(),
    val milestones: List<MilestoneTemplate> = emptyList()
) {
    /**
     * 计算模板中的总计划数（包括主计划和所有子计划）
     */
    val totalPlanCount: Int
        get() = 1 + countSubPlans(subPlans)
    
    /**
     * 计算模板中的总里程碑数
     */
    val totalMilestoneCount: Int
        get() = milestones.size + subPlans.sumOf { countMilestones(it) }
    
    private fun countSubPlans(subPlans: List<SubPlanTemplate>): Int {
        return subPlans.sumOf { 1 + countSubPlans(it.subPlans) }
    }
    
    private fun countMilestones(subPlan: SubPlanTemplate): Int {
        return subPlan.milestones.size + subPlan.subPlans.sumOf { countMilestones(it) }
    }
}

/**
 * 计划模板
 */
data class PlanTemplate(
    val title: String,
    val description: String,
    val tags: List<String> = emptyList()
)

/**
 * 子计划模板
 */
data class SubPlanTemplate(
    val title: String,
    val description: String,
    val dayOffset: Int, // 相对于主计划开始日期的偏移天数
    val duration: Int, // 持续天数
    val subPlans: List<SubPlanTemplate> = emptyList(), // 支持多级嵌套
    val milestones: List<MilestoneTemplate> = emptyList()
)

/**
 * 里程碑模板
 */
data class MilestoneTemplate(
    val title: String,
    val description: String,
    val dayOffset: Int // 相对于计划开始日期的偏移天数
)

/**
 * 模板分类
 */
enum class TemplateCategory(val displayName: String) {
    WORK("工作"),
    STUDY("学习"),
    LIFE("生活"),
    HEALTH("健康"),
    FITNESS("健身"),
    SKILL("技能"),
    PROJECT("项目"),
    OTHER("其他");
    
    companion object {
        /**
         * 根据字符串获取分类
         */
        fun fromString(value: String): TemplateCategory {
            return values().find { it.name == value } ?: OTHER
        }
    }
}