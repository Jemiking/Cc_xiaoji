package com.ccxiaoji.feature.plan.util.performance

import androidx.collection.LruCache
import com.ccxiaoji.feature.plan.domain.model.Plan
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 进度计算缓存
 * 用于缓存计划进度计算结果，避免重复计算
 */
@Singleton
class ProgressCalculationCache @Inject constructor() {
    
    // 使用LruCache实现内存缓存，最多缓存500个计划的进度
    private val cache = LruCache<String, CachedProgress>(500)
    
    // 用于并发控制的互斥锁
    private val mutex = Mutex()
    
    /**
     * 获取缓存的进度
     * @param planId 计划ID
     * @return 缓存的进度值，如果不存在或已过期返回null
     */
    suspend fun getCachedProgress(planId: String): Float? = mutex.withLock {
        val cached = cache.get(planId) ?: return null
        
        // 检查缓存是否过期（5分钟）
        if (System.currentTimeMillis() - cached.timestamp > CACHE_EXPIRY_TIME) {
            cache.remove(planId)
            return null
        }
        
        return cached.progress
    }
    
    /**
     * 缓存进度值
     * @param planId 计划ID
     * @param progress 进度值
     */
    suspend fun cacheProgress(planId: String, progress: Float) {
        mutex.withLock {
            cache.put(planId, CachedProgress(progress, System.currentTimeMillis()))
        }
    }
    
    /**
     * 批量缓存进度
     * @param progressMap 进度映射表
     */
    suspend fun cacheProgressBatch(progressMap: Map<String, Float>) {
        mutex.withLock {
            val timestamp = System.currentTimeMillis()
            progressMap.forEach { (planId, progress) ->
                cache.put(planId, CachedProgress(progress, timestamp))
            }
        }
    }
    
    /**
     * 清除指定计划的缓存
     * @param planId 计划ID
     */
    suspend fun invalidateCache(planId: String) {
        mutex.withLock {
            cache.remove(planId)
        }
    }
    
    /**
     * 清除多个计划的缓存
     * @param planIds 计划ID列表
     */
    suspend fun invalidateCacheBatch(planIds: List<String>) {
        mutex.withLock {
            planIds.forEach { planId ->
                cache.remove(planId)
            }
        }
    }
    
    /**
     * 清除所有缓存
     */
    suspend fun clearAllCache() {
        mutex.withLock {
            cache.evictAll()
        }
    }
    
    /**
     * 使用缓存计算进度
     * 如果缓存中存在则直接返回，否则计算并缓存
     */
    suspend fun calculateProgressWithCache(
        plan: Plan,
        calculator: suspend (Plan) -> Float
    ): Float {
        // 先检查缓存
        getCachedProgress(plan.id)?.let { return it }
        
        // 如果没有缓存，则计算
        val progress = if (plan.children.isEmpty()) {
            plan.progress
        } else {
            // 递归计算子计划进度
            val childProgresses = plan.children.map { child ->
                calculateProgressWithCache(child, calculator)
            }
            childProgresses.average().toFloat()
        }
        
        // 缓存结果
        cacheProgress(plan.id, progress)
        
        return progress
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            size = cache.size(),
            maxSize = cache.maxSize(),
            hitCount = cache.hitCount(),
            missCount = cache.missCount(),
            evictionCount = cache.evictionCount()
        )
    }
    
    companion object {
        // 缓存过期时间：5分钟
        private const val CACHE_EXPIRY_TIME = 5 * 60 * 1000L
    }
}

/**
 * 缓存的进度数据
 */
private data class CachedProgress(
    val progress: Float,
    val timestamp: Long
)

/**
 * 缓存统计信息
 */
data class CacheStats(
    val size: Int,
    val maxSize: Int,
    val hitCount: Int,
    val missCount: Int,
    val evictionCount: Int
) {
    val hitRate: Float
        get() = if (hitCount + missCount > 0) {
            hitCount.toFloat() / (hitCount + missCount)
        } else {
            0f
        }
}