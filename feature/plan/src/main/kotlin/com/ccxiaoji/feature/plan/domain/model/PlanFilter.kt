package com.ccxiaoji.feature.plan.domain.model

import kotlinx.datetime.LocalDate

/**
 * 计划筛选条件
 */
data class PlanFilter(
    val statuses: Set<PlanStatus> = emptySet(),
    val startDateRange: DateRange? = null,
    val endDateRange: DateRange? = null,
    val tags: Set<String> = emptySet(),
    val hasChildren: Boolean? = null
) {
    /**
     * 是否有激活的筛选条件
     */
    val isActive: Boolean
        get() = statuses.isNotEmpty() || 
                startDateRange != null || 
                endDateRange != null || 
                tags.isNotEmpty() || 
                hasChildren != null
}

/**
 * 日期范围
 */
data class DateRange(
    val start: LocalDate,
    val end: LocalDate
)

/**
 * 排序方式
 */
enum class PlanSortBy {
    NAME_ASC,           // 名称升序
    NAME_DESC,          // 名称降序
    CREATE_TIME_ASC,    // 创建时间升序
    CREATE_TIME_DESC,   // 创建时间降序
    UPDATE_TIME_ASC,    // 更新时间升序
    UPDATE_TIME_DESC,   // 更新时间降序
    START_DATE_ASC,     // 开始日期升序
    START_DATE_DESC,    // 开始日期降序
    END_DATE_ASC,       // 结束日期升序
    END_DATE_DESC,      // 结束日期降序
    PROGRESS_ASC,       // 进度升序
    PROGRESS_DESC       // 进度降序
}