package com.ccxiaoji.feature.plan.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ccxiaoji.feature.plan.presentation.plan.list.PlanListScreen
import com.ccxiaoji.feature.plan.presentation.create.CreatePlanScreen
import com.ccxiaoji.feature.plan.presentation.edit.EditPlanScreen
import com.ccxiaoji.feature.plan.presentation.detail.PlanDetailScreen
import com.ccxiaoji.feature.plan.presentation.template.TemplateListScreen
import com.ccxiaoji.feature.plan.presentation.template.detail.TemplateDetailScreen
import com.ccxiaoji.feature.plan.presentation.analysis.ProgressAnalysisScreen
import com.ccxiaoji.feature.plan.presentation.screen.SettingsScreen

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
                }
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
                }
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
                parentPlanId = parentId
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
                }
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
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}