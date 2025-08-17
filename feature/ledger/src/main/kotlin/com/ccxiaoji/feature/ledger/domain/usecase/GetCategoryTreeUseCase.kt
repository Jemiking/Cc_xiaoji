package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.data.cache.CategoryCache
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * 获取分类树结构的UseCase
 * 用于获取完整的二级分类树
 * 包含缓存优化以提升性能
 */
class GetCategoryTreeUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val categoryCache: CategoryCache
) {
    /**
     * 执行获取分类树（带缓存）
     * @param userId 用户ID
     * @param type 分类类型（INCOME/EXPENSE）
     * @return 分类组列表
     */
    suspend operator fun invoke(
        userId: String,
        type: String
    ): List<CategoryGroup> {
        println("🔧 [GetCategoryTreeUseCase] 获取分类树，用户ID: $userId, 类型: $type")
        
        // 先尝试从缓存获取
        categoryCache.getCategoryTree(userId, type)?.let { cachedTree ->
            println("🔧 [GetCategoryTreeUseCase] 从缓存获取到分类树，组数: ${cachedTree.size}")
            return cachedTree
        }
        
        println("🔧 [GetCategoryTreeUseCase] 缓存未命中，从数据库获取")
        
        // 缓存未命中，从数据库获取
        val tree = categoryRepository.getCategoryTree(userId, type)
        
        println("🔧 [GetCategoryTreeUseCase] 从数据库获取到分类树，组数: ${tree.size}")
        
        // 更新缓存
        categoryCache.setCategoryTree(userId, type, tree)
        println("🔧 [GetCategoryTreeUseCase] 已更新缓存")
        
        return tree
    }
    
    /**
     * 获取支出分类树
     */
    suspend fun getExpenseTree(userId: String): List<CategoryGroup> {
        return invoke(userId, "EXPENSE")
    }
    
    /**
     * 获取收入分类树
     */
    suspend fun getIncomeTree(userId: String): List<CategoryGroup> {
        return invoke(userId, "INCOME")
    }
    
    /**
     * 强制刷新分类树（不使用缓存）
     */
    suspend fun refreshCategoryTree(userId: String, type: String): List<CategoryGroup> {
        // 使缓存失效
        categoryCache.invalidateCategoryTree(userId, type)
        
        // 重新获取
        return invoke(userId, type)
    }
}