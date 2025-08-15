package com.ccxiaoji.feature.plan.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ccxiaoji.feature.plan.presentation.screen.PlanListScreen
import com.ccxiaoji.feature.plan.presentation.screen.CreatePlanScreen
import com.ccxiaoji.feature.plan.presentation.screen.EditPlanScreen
import com.ccxiaoji.feature.plan.presentation.screen.PlanDetailScreen
import com.ccxiaoji.feature.plan.presentation.screen.TemplateListScreen
import com.ccxiaoji.feature.plan.presentation.screen.TemplateDetailScreen
import com.ccxiaoji.feature.plan.presentation.screen.ProgressAnalysisScreen
import com.ccxiaoji.feature.plan.presentation.screen.SettingsScreen
import com.ccxiaoji.feature.plan.presentation.screen.ParentPlanSelectionScreen
import com.ccxiaoji.feature.plan.presentation.screen.template.ApplyTemplateScreen
import com.ccxiaoji.feature.plan.presentation.screen.template.CreateTemplateScreen
import com.ccxiaoji.feature.plan.presentation.screen.filter.PlanFilterScreen
import com.ccxiaoji.feature.plan.presentation.screen.settings.ThemeSelectionScreen
import com.ccxiaoji.feature.plan.presentation.screen.DatePickerScreen
import com.ccxiaoji.feature.plan.presentation.screen.ColorPickerScreen

/**
 * 计划模块导航图
 */
@Composable
fun PlanNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = PlanDestinations.PLAN_LIST
    ) {
        // 计划列表（主页）
        composable(PlanDestinations.PLAN_LIST) {
            PlanListScreen(
                onNavigateToPlanDetail = { planId ->
                    navController.navigate("plan_detail/$planId")
                },
                onNavigateToCreatePlan = { parentId ->
                    val route = if (parentId != null) {
                        "create_plan?parentId=$parentId"
                    } else {
                        "create_plan"
                    }
                    navController.navigate(route)
                },
                onNavigateToTemplates = {
                    navController.navigate(PlanDestinations.TEMPLATE_LIST)
                },
                onNavigateToAnalysis = {
                    navController.navigate(PlanDestinations.PROGRESS_ANALYSIS)
                },
                onNavigateToSettings = {
                    navController.navigate(PlanDestinations.SETTINGS)
                },
                onNavigateToFilter = {
                    navController.navigate(PlanDestinations.PLAN_FILTER)
                },
                navController = navController
            )
        }
        
        // 计划详情
        composable(
            route = PlanDestinations.PLAN_DETAIL,
            arguments = listOf(
                navArgument(NavArgs.PLAN_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString(NavArgs.PLAN_ID) ?: ""
            PlanDetailScreen(
                planId = planId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { editPlanId ->
                    navController.navigate("edit_plan/$editPlanId")
                },
                onCreateSubPlan = { parentId ->
                    navController.navigate("create_plan?parentId=$parentId")
                },
                onNavigateToPlanDetail = { subPlanId ->
                    navController.navigate("plan_detail/$subPlanId")
                },
                onNavigateToUpdateProgress = { progressPlanId ->
                    navController.navigate("update_progress/$progressPlanId")
                },
                onNavigateToAddEditMilestone = { milestonePlanId, milestoneId ->
                    val route = if (milestoneId != null) {
                        "add_edit_milestone/$milestonePlanId?milestoneId=$milestoneId"
                    } else {
                        "add_edit_milestone/$milestonePlanId"
                    }
                    navController.navigate(route)
                },
                onNavigateToCreateTemplate = { createPlanId ->
                    navController.navigate("create_template/$createPlanId")
                },
                navController = navController
            )
        }
        
        // 创建计划
        composable(
            route = PlanDestinations.CREATE_PLAN,
            arguments = listOf(
                navArgument(NavArgs.PARENT_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val parentId = backStackEntry.arguments?.getString(NavArgs.PARENT_ID)
            CreatePlanScreen(
                onBackClick = { navController.popBackStack() },
                onPlanCreated = { planId ->
                    navController.navigate("plan_detail/$planId") {
                        popUpTo(PlanDestinations.PLAN_LIST)
                    }
                },
                parentPlanId = parentId,
                navController = navController
            )
        }
        
        // 编辑计划
        composable(
            route = PlanDestinations.EDIT_PLAN,
            arguments = listOf(
                navArgument(NavArgs.PLAN_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString(NavArgs.PLAN_ID) ?: ""
            EditPlanScreen(
                planId = planId,
                onBackClick = { navController.popBackStack() },
                onPlanUpdated = {
                    navController.popBackStack()
                }
            )
        }
        
        // 模板列表
        composable(PlanDestinations.TEMPLATE_LIST) {
            TemplateListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlanDetail = { planId ->
                    navController.navigate("plan_detail/$planId") {
                        popUpTo(PlanDestinations.PLAN_LIST)
                    }
                },
                onNavigateToTemplateDetail = { templateId ->
                    navController.navigate("template_detail/$templateId")
                }
            )
        }
        
        // 模板详情
        composable(
            route = PlanDestinations.TEMPLATE_DETAIL,
            arguments = listOf(
                navArgument(NavArgs.TEMPLATE_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString(NavArgs.TEMPLATE_ID) ?: ""
            TemplateDetailScreen(
                templateId = templateId,
                onBack = { navController.popBackStack() },
                onNavigateToPlanDetail = { planId ->
                    navController.navigate("plan_detail/$planId") {
                        popUpTo(PlanDestinations.PLAN_LIST)
                    }
                },
                onNavigateToApplyTemplate = {
                    navController.navigate("apply_template/$templateId")
                },
                navController = navController
            )
        }
        
        // 应用模板
        composable(
            route = PlanDestinations.APPLY_TEMPLATE,
            arguments = listOf(
                navArgument(NavArgs.TEMPLATE_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString(NavArgs.TEMPLATE_ID) ?: ""
            ApplyTemplateScreen(
                navController = navController,
                templateId = templateId
            )
        }
        
        // 计划筛选
        composable(PlanDestinations.PLAN_FILTER) {
            PlanFilterScreen(
                navController = navController
            )
        }
        
        // 创建模板
        composable(
            route = PlanDestinations.CREATE_TEMPLATE,
            arguments = listOf(
                navArgument(NavArgs.PLAN_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString(NavArgs.PLAN_ID) ?: ""
            CreateTemplateScreen(
                navController = navController,
                planId = planId
            )
        }
        
        // 进度分析
        composable(PlanDestinations.PROGRESS_ANALYSIS) {
            ProgressAnalysisScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // 设置
        composable(PlanDestinations.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToThemeSelection = {
                    navController.navigate(PlanDestinations.THEME_SELECTION)
                }
            )
        }
        
        // 主题选择
        composable(PlanDestinations.THEME_SELECTION) {
            ThemeSelectionScreen(navController = navController)
        }
        
        // 父计划选择
        composable(
            route = PlanDestinations.PARENT_PLAN_SELECTION,
            arguments = listOf(
                navArgument(NavArgs.SELECTED_PARENT_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(NavArgs.CURRENT_PLAN_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val selectedParentId = backStackEntry.arguments?.getString(NavArgs.SELECTED_PARENT_ID)
            val currentPlanId = backStackEntry.arguments?.getString(NavArgs.CURRENT_PLAN_ID)
            ParentPlanSelectionScreen(
                navController = navController,
                currentPlanId = currentPlanId
            )
        }
        
        // 更新进度
        composable(
            route = "update_progress/{planId}",
            arguments = listOf(
                navArgument("planId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            com.ccxiaoji.feature.plan.presentation.screen.UpdateProgressScreen(
                planId = planId,
                navController = navController
            )
        }
        
        // 添加/编辑里程碑
        composable(
            route = "add_edit_milestone/{planId}?milestoneId={milestoneId}",
            arguments = listOf(
                navArgument("planId") { type = NavType.StringType },
                navArgument("milestoneId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            val milestoneId = backStackEntry.arguments?.getString("milestoneId")
            com.ccxiaoji.feature.plan.presentation.screen.AddEditMilestoneScreen(
                planId = planId,
                milestoneId = milestoneId,
                navController = navController
            )
        }
        
        // 删除计划
        composable(
            route = PlanDestinations.DELETE_PLAN,
            arguments = listOf(
                navArgument(NavArgs.PLAN_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString(NavArgs.PLAN_ID) ?: ""
            com.ccxiaoji.feature.plan.presentation.screen.delete.DeletePlanScreen(
                planId = planId,
                navController = navController
            )
        }
        
        // 删除里程碑
        composable(
            route = PlanDestinations.DELETE_MILESTONE,
            arguments = listOf(
                navArgument(NavArgs.PLAN_ID) { type = NavType.StringType },
                navArgument("milestoneId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString(NavArgs.PLAN_ID) ?: ""
            val milestoneId = backStackEntry.arguments?.getString("milestoneId") ?: ""
            com.ccxiaoji.feature.plan.presentation.screen.delete.DeleteMilestoneScreen(
                planId = planId,
                milestoneId = milestoneId,
                navController = navController
            )
        }
        
        // 日期选择器
        composable(
            route = PlanDestinations.DATE_PICKER,
            arguments = listOf(
                navArgument("initialDate") { 
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val initialDate = backStackEntry.arguments?.getString("initialDate")
            DatePickerScreen(
                initialDate = initialDate,
                navController = navController
            )
        }
        
        // 颜色选择器
        composable(
            route = PlanDestinations.COLOR_PICKER,
            arguments = listOf(
                navArgument("initialColor") { 
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val initialColor = backStackEntry.arguments?.getString("initialColor")
            ColorPickerScreen(
                initialColor = initialColor,
                navController = navController
            )
        }
    }
}