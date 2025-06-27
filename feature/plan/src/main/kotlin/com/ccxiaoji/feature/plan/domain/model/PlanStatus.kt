package com.ccxiaoji.feature.plan.domain.model

/**
 * 计划状态枚举
 */
enum class PlanStatus {
    /** 未开始 */
    NOT_STARTED,
    
    /** 进行中 */
    IN_PROGRESS,
    
    /** 已完成 */
    COMPLETED,
    
    /** 已取消 */
    CANCELLED
}