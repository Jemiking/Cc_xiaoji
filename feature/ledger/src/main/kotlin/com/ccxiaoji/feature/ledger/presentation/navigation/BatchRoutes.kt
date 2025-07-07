package com.ccxiaoji.feature.ledger.presentation.navigation

/**
 * 批量操作相关路由
 */
object BatchUpdateCategoryRoute {
    const val route = "batch_update_category?selectedCount={selectedCount}"
    fun createRoute(selectedCount: Int) = "batch_update_category?selectedCount=$selectedCount"
}

object BatchUpdateAccountRoute {
    const val route = "batch_update_account?selectedCount={selectedCount}"
    fun createRoute(selectedCount: Int) = "batch_update_account?selectedCount=$selectedCount"
}

object BatchDeleteRoute {
    const val route = "batch_delete?selectedCount={selectedCount}"
    fun createRoute(selectedCount: Int) = "batch_delete?selectedCount=$selectedCount"
}