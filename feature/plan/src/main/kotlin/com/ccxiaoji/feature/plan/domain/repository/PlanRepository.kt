package com.ccxiaoji.feature.plan.domain.repository

import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import kotlinx.coroutines.flow.Flow

/**
 * 计划仓库接口
 * 定义计划相关的数据操作
 */
interface PlanRepository {
    
    /**
     * 获取所有计划（树形结构）
     */
    fun getAllPlansTree(): Flow<List<Plan>>
    
    /**
     * 获取所有顶级计划
     */
    fun getRootPlans(): Flow<List<Plan>>
    
    /**
     * 根据ID获取计划
     */
    suspend fun getPlanById(planId: String): Plan?
    
    /**
     * 获取子计划列表
     */
    suspend fun getChildPlans(parentId: String): List<Plan>
    
    /**
     * 根据状态获取计划
     */
    fun getPlansByStatus(status: PlanStatus): Flow<List<Plan>>
    
    /**
     * 搜索计划
     */
    fun searchPlans(query: String): Flow<List<Plan>>
    
    /**
     * 创建计划
     */
    suspend fun createPlan(plan: Plan): String
    
    /**
     * 更新计划
     */
    suspend fun updatePlan(plan: Plan)
    
    /**
     * 更新计划进度
     */
    suspend fun updatePlanProgress(planId: String, progress: Float)
    
    /**
     * 更新计划状态
     */
    suspend fun updatePlanStatus(planId: String, status: PlanStatus)
    
    /**
     * 删除计划（级联删除子计划）
     */
    suspend fun deletePlan(planId: String)
    
    /**
     * 批量删除计划
     */
    suspend fun deletePlans(planIds: List<String>)
    
    /**
     * 移动计划到新的父计划
     */
    suspend fun movePlan(planId: String, newParentId: String?)
    
    /**
     * 重新排序计划
     */
    suspend fun reorderPlans(planIds: List<String>)
    
    /**
     * 清空所有计划数据
     */
    suspend fun deleteAllPlans()
}