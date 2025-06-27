package com.ccxiaoji.feature.plan.presentation.navigation

/**
 * 导航目的地定义
 */
object PlanDestinations {
    /**
     * 计划列表（主页）
     */
    const val PLAN_LIST = "plan_list"
    
    /**
     * 计划详情
     * 参数：planId
     */
    const val PLAN_DETAIL = "plan_detail/{planId}"
    
    /**
     * 创建计划
     * 参数：parentId（可选）
     */
    const val CREATE_PLAN = "create_plan?parentId={parentId}"
    
    /**
     * 编辑计划
     * 参数：planId
     */
    const val EDIT_PLAN = "edit_plan/{planId}"
    
    /**
     * 模板列表
     */
    const val TEMPLATE_LIST = "template_list"
    
    /**
     * 模板详情
     * 参数：templateId
     */
    const val TEMPLATE_DETAIL = "template_detail/{templateId}"
    
    /**
     * 从模板创建计划
     * 参数：templateId
     */
    const val CREATE_FROM_TEMPLATE = "create_from_template/{templateId}"
    
    /**
     * 进度分析
     */
    const val PROGRESS_ANALYSIS = "progress_analysis"
    
    /**
     * 设置
     */
    const val SETTINGS = "settings"
}

/**
 * 导航参数
 */
object NavArgs {
    const val PLAN_ID = "planId"
    const val PARENT_ID = "parentId"
    const val TEMPLATE_ID = "templateId"
}