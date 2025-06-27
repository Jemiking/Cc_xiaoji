package com.ccxiaoji.feature.plan.domain.model

import kotlinx.datetime.LocalDate

/**
 * 里程碑模型
 * 
 * @property id 里程碑ID
 * @property planId 所属计划ID
 * @property title 标题
 * @property description 描述
 * @property targetDate 目标日期
 * @property isCompleted 是否已完成
 * @property completedDate 完成日期
 * @property orderIndex 排序索引
 */
data class Milestone(
    val id: String,
    val planId: String,
    val title: String,
    val description: String = "",
    val targetDate: LocalDate,
    val isCompleted: Boolean = false,
    val completedDate: LocalDate? = null,
    val orderIndex: Int = 0
)