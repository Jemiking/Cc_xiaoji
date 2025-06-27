package com.ccxiaoji.feature.plan.domain.usecase.progress

import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import com.ccxiaoji.feature.plan.util.performance.ProgressCalculationCache
import javax.inject.Inject

/**
 * 计算计划进度用例
 * 递归计算包含子计划的总进度
 * 使用缓存优化性能
 */
class CalculatePlanProgressUseCase @Inject constructor(
    private val planRepository: PlanRepository,
    private val progressCache: ProgressCalculationCache
) {
    /**
     * 执行用例
     * @param planId 计划ID
     * @return 计算后的进度（0-100）
     */
    suspend operator fun invoke(planId: String): Result<Float> {
        return try {
            val progress = calculateProgress(planId)
            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 递归计算进度（带缓存）
     */
    private suspend fun calculateProgress(planId: String): Float {
        // 先检查缓存
        progressCache.getCachedProgress(planId)?.let { return it }
        
        // 获取计划数据
        val plan = planRepository.getPlanById(planId) ?: return 0f
        
        // 计算进度
        val progress = if (plan.children.isEmpty()) {
            // 如果没有子计划，直接返回自身进度
            plan.progress
        } else {
            // 如果有子计划，计算所有子计划的平均进度
            val childProgresses = plan.children.map { child ->
                calculateProgress(child.id)
            }
            
            if (childProgresses.isNotEmpty()) {
                childProgresses.average().toFloat()
            } else {
                plan.progress
            }
        }
        
        // 缓存计算结果
        progressCache.cacheProgress(planId, progress)
        
        return progress
    }
    
    /**
     * 清除指定计划的进度缓存
     * 当计划进度更新时应调用此方法
     */
    suspend fun invalidateCache(planId: String) {
        progressCache.invalidateCache(planId)
    }
    
    /**
     * 清除所有进度缓存
     */
    suspend fun clearAllCache() {
        progressCache.clearAllCache()
    }
}