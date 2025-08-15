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
     * 应用模板
     * 参数：templateId
     */
    const val APPLY_TEMPLATE = "apply_template/{templateId}"
    
    /**
     * 计划筛选
     */
    const val PLAN_FILTER = "plan_filter"
    
    /**
     * 创建模板
     * 参数：planId
     */
    const val CREATE_TEMPLATE = "create_template/{planId}"
    
    /**
     * 进度分析
     */
    const val PROGRESS_ANALYSIS = "progress_analysis"
    
    /**
     * 设置
     */
    const val SETTINGS = "settings"
    
    /**
     * 父计划选择
     * 参数：selectedParentId（可选）, currentPlanId（可选）
     */
    const val PARENT_PLAN_SELECTION = "parent_plan_selection?selectedParentId={selectedParentId}&currentPlanId={currentPlanId}"
    
    /**
     * 主题选择
     */
    const val THEME_SELECTION = "theme_selection"
    
    /**
     * 删除计划
     * 参数：planId
     */
    const val DELETE_PLAN = "delete_plan/{planId}"
    
    /**
     * 删除里程碑
     * 参数：planId, milestoneId
     */
    const val DELETE_MILESTONE = "delete_milestone/{planId}/{milestoneId}"
    
    /**
     * 日期选择器
     * 参数：initialDate（可选）
     */
    const val DATE_PICKER = "date_picker/{initialDate}"
    
    /**
     * 颜色选择器
     * 参数：initialColor（可选）
     */
    const val COLOR_PICKER = "color_picker/{initialColor}"
}

/**
 * 导航参数
 */
object NavArgs {
    const val PLAN_ID = "planId"
    const val PARENT_ID = "parentId"
    const val TEMPLATE_ID = "templateId"
    const val SELECTED_PARENT_ID = "selectedParentId"
    const val CURRENT_PLAN_ID = "currentPlanId"
}