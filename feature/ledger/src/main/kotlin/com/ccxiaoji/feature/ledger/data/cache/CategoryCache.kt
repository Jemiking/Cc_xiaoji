package com.ccxiaoji.feature.ledger.data.cache

import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 分类缓存管理器
 * 用于缓存分类数据，减少数据库查询，提升性能
 */
@Singleton
class CategoryCache @Inject constructor() {
    
    private val mutex = Mutex()
    
    // 分类树缓存 (userId + type) -> CategoryTree
    private val categoryTreeCache = mutableMapOf<String, List<CategoryGroup>>()
    
    // 分类信息缓存 categoryId -> SelectedCategoryInfo
    private val categoryInfoCache = mutableMapOf<String, SelectedCategoryInfo>()
    
    // 常用分类缓存 (userId + type) -> List<Category>
    private val frequentCategoriesCache = mutableMapOf<String, List<Category>>()
    
    // 缓存有效期（毫秒）
    private val cacheValidityDuration = 5 * 60 * 1000L // 5分钟
    
    // 缓存时间戳
    private val cacheTimestamps = mutableMapOf<String, Long>()
    
    /**
     * 获取分类树缓存
     */
    suspend fun getCategoryTree(userId: String, type: String): List<CategoryGroup>? = mutex.withLock {
        val key = "$userId:$type"
        if (isCacheValid(key)) {
            return categoryTreeCache[key]
        }
        return null
    }
    
    /**
     * 设置分类树缓存
     */
    suspend fun setCategoryTree(userId: String, type: String, tree: List<CategoryGroup>) = mutex.withLock {
        val key = "$userId:$type"
        categoryTreeCache[key] = tree
        updateTimestamp(key)
    }
    
    /**
     * 获取分类信息缓存
     */
    suspend fun getCategoryInfo(categoryId: String): SelectedCategoryInfo? = mutex.withLock {
        val key = "info:$categoryId"
        if (isCacheValid(key)) {
            return categoryInfoCache[categoryId]
        }
        return null
    }
    
    /**
     * 设置分类信息缓存
     */
    suspend fun setCategoryInfo(categoryId: String, info: SelectedCategoryInfo) = mutex.withLock {
        val key = "info:$categoryId"
        categoryInfoCache[categoryId] = info
        updateTimestamp(key)
    }
    
    /**
     * 获取常用分类缓存
     */
    suspend fun getFrequentCategories(userId: String, type: String): List<Category>? = mutex.withLock {
        val key = "frequent:$userId:$type"
        if (isCacheValid(key)) {
            return frequentCategoriesCache[key]
        }
        return null
    }
    
    /**
     * 设置常用分类缓存
     */
    suspend fun setFrequentCategories(userId: String, type: String, categories: List<Category>) = mutex.withLock {
        val key = "frequent:$userId:$type"
        frequentCategoriesCache["$userId:$type"] = categories
        updateTimestamp(key)
    }
    
    /**
     * 清除指定用户的所有缓存
     */
    suspend fun clearUserCache(userId: String) = mutex.withLock {
        // 清除分类树缓存
        val treeKeysToRemove = categoryTreeCache.keys.filter { it.startsWith(userId) }
        treeKeysToRemove.forEach { categoryTreeCache.remove(it) }
        
        // 清除常用分类缓存
        val frequentKeysToRemove = frequentCategoriesCache.keys.filter { it.contains(userId) }
        frequentKeysToRemove.forEach { frequentCategoriesCache.remove(it) }
        
        // 清除时间戳
        val timestampKeysToRemove = cacheTimestamps.keys.filter { it.contains(userId) }
        timestampKeysToRemove.forEach { cacheTimestamps.remove(it) }
    }
    
    /**
     * 清除所有缓存
     */
    suspend fun clearAllCache() = mutex.withLock {
        categoryTreeCache.clear()
        categoryInfoCache.clear()
        frequentCategoriesCache.clear()
        cacheTimestamps.clear()
    }
    
    /**
     * 使指定类型的缓存失效
     */
    suspend fun invalidateCategoryTree(userId: String, type: String) = mutex.withLock {
        val key = "$userId:$type"
        categoryTreeCache.remove(key)
        cacheTimestamps.remove(key)
    }
    
    /**
     * 使分类信息缓存失效
     */
    suspend fun invalidateCategoryInfo(categoryId: String) = mutex.withLock {
        val key = "info:$categoryId"
        categoryInfoCache.remove(categoryId)
        cacheTimestamps.remove(key)
    }
    
    /**
     * 检查缓存是否有效
     */
    private fun isCacheValid(key: String): Boolean {
        val timestamp = cacheTimestamps[key] ?: return false
        return (System.currentTimeMillis() - timestamp) < cacheValidityDuration
    }
    
    /**
     * 更新时间戳
     */
    private fun updateTimestamp(key: String) {
        cacheTimestamps[key] = System.currentTimeMillis()
    }
    
    /**
     * 批量预热缓存
     * 在应用启动时调用，预加载常用数据
     */
    suspend fun warmupCache(
        userId: String,
        expenseTree: List<CategoryGroup>,
        incomeTree: List<CategoryGroup>
    ) = mutex.withLock {
        // 缓存分类树
        setCategoryTreeInternal("$userId:EXPENSE", expenseTree)
        setCategoryTreeInternal("$userId:INCOME", incomeTree)
        
        // 缓存所有分类信息
        val allCategories = mutableListOf<Category>()
        expenseTree.forEach { group ->
            group.children.forEach { child ->
                val info = SelectedCategoryInfo(
                    categoryId = child.id,
                    categoryName = child.name,
                    parentId = group.parent.id,
                    parentName = group.parent.name,
                    fullPath = "${group.parent.name}/${child.name}",
                    icon = child.icon,
                    color = child.color
                )
                categoryInfoCache[child.id] = info
                updateTimestamp("info:${child.id}")
            }
        }
        
        incomeTree.forEach { group ->
            group.children.forEach { child ->
                val info = SelectedCategoryInfo(
                    categoryId = child.id,
                    categoryName = child.name,
                    parentId = group.parent.id,
                    parentName = group.parent.name,
                    fullPath = "${group.parent.name}/${child.name}",
                    icon = child.icon,
                    color = child.color
                )
                categoryInfoCache[child.id] = info
                updateTimestamp("info:${child.id}")
            }
        }
    }
    
    private fun setCategoryTreeInternal(key: String, tree: List<CategoryGroup>) {
        categoryTreeCache[key] = tree
        updateTimestamp(key)
    }
}