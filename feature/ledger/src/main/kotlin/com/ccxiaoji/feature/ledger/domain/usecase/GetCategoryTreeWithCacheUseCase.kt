package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.data.cache.CategoryCacheManager
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * 带缓存的获取分类树UseCase
 * 优先从缓存获取数据，提高性能
 */
class GetCategoryTreeWithCacheUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val cacheManager: CategoryCacheManager
) {
    /**
     * 获取分类树（带缓存）
     * @param userId 用户ID
     * @param type 分类类型
     * @param forceRefresh 是否强制刷新缓存
     * @return 分类树列表
     */
    suspend operator fun invoke(
        userId: String,
        type: String,
        forceRefresh: Boolean = false
    ): List<CategoryGroup> {
        val cacheKey = cacheManager.generateCacheKey(userId, type)
        
        // 如果不强制刷新，尝试从缓存获取
        if (!forceRefresh) {
            cacheManager.getCachedCategoryTree(cacheKey)?.let {
                return it
            }
        }
        
        // 从数据库获取
        val tree = categoryRepository.getCategoryTree(userId, type)
        
        // 更新缓存
        cacheManager.cacheCategoryTree(cacheKey, tree)
        
        return tree
    }
    
    /**
     * 刷新缓存
     * 在分类数据变化后调用
     */
    suspend fun refreshCache(userId: String) {
        cacheManager.clearUserCache(userId)
    }
}