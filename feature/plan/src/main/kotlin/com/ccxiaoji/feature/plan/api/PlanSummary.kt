package com.ccxiaoji.feature.plan.api

import kotlinx.datetime.LocalDate

/**
 * 计划摘要数据传输对象
 * 用于在模块间传递计划基本信息
 */
data class PlanSummary(
    val id: String,
    val title: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val progress: Float,
    val status: PlanStatusDto,
    val priority: Int,
    val hasChildren: Boolean
)

/**
 * 计划状态DTO
 * 与domain层的PlanStatus对应，但用于模块间通信
 */
enum class PlanStatusDto {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}