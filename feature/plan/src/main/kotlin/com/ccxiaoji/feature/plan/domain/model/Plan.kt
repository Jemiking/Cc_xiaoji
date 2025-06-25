package com.ccxiaoji.feature.plan.domain.model

import kotlinx.datetime.LocalDate

/**
 * 计划领域模型
 * 
 * @property id 计划ID
 * @property parentId 父计划ID（null表示顶级计划）
 * @property title 计划标题
 * @property description 计划描述
 * @property startDate 开始日期
 * @property endDate 结束日期
 * @property status 计划状态
 * @property progress 进度（0-100）
 * @property color 颜色标识
 * @property priority 优先级（0-10，数字越大优先级越高）
 * @property tags 标签列表
 * @property children 子计划列表
 * @property milestones 里程碑列表
 * @property reminderSettings 提醒设置
 * @property orderIndex 同级排序索引
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
data class Plan(
    val id: String,
    val parentId: String? = null,
    val title: String,
    val description: String = "",
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: PlanStatus = PlanStatus.NOT_STARTED,
    val progress: Float = 0f,
    val color: String = "#6650a4",
    val priority: Int = 0,
    val tags: List<String> = emptyList(),
    val children: List<Plan> = emptyList(),
    val milestones: List<Milestone> = emptyList(),
    val reminderSettings: ReminderSettings? = null,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 是否有子计划
     */
    val hasChildren: Boolean
        get() = children.isNotEmpty()
    
    /**
     * 是否是顶级计划
     */
    val isRootPlan: Boolean
        get() = parentId == null
    
    /**
     * 计算总进度（包含子计划）
     */
    fun calculateTotalProgress(): Float {
        return if (children.isEmpty()) {
            progress
        } else {
            children.map { it.calculateTotalProgress() }.average().toFloat()
        }
    }
}