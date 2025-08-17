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
 * 用于缓存分类数据，减少数据库查询，提高性能
 */
@Singleton
class CategoryCacheManager @Inject constructor() {
    
    // 分类树缓存
    private val categoryTreeCache = mutableMapOf<String, List<CategoryGroup>>()
    
    // 分类信息缓存
    private val categoryInfoCache = mutableMapOf<String, SelectedCategoryInfo>()
    
    // 常用分类缓存
    private val frequentCategoriesCache = mutableMapOf<String, List<Category>>()
    
    // 缓存时间戳
    private var lastCacheTime = 0L
    
    // 缓存有效期（5分钟）
    private val CACHE_VALIDITY_DURATION = 5 * 60 * 1000L
    
    // 线程安全锁
    private val mutex = Mutex()
    
    /**
     * 获取缓存的分类树
     * @param key 缓存键（userId_type）
     * @return 缓存的分类树，如果不存在或过期返回null
     */
    suspend fun getCachedCategoryTree(key: String): List<CategoryGroup>? = mutex.withLock {
        if (isCacheValid()) {
            return categoryTreeCache[key]
        }
        return null
    }
    
    /**
     * 缓存分类树
     * @param key 缓存键（userId_type）
     * @param tree 分类树数据
     */
    suspend fun cacheCategoryTree(key: String, tree: List<CategoryGroup>) = mutex.withLock {
        categoryTreeCache[key] = tree
        updateCacheTime()
    }
    
    /**
     * 获取缓存的分类信息
     * @param categoryId 分类ID
     * @return 缓存的分类信息，如果不存在或过期返回null
     */
    suspend fun getCachedCategoryInfo(categoryId: String): SelectedCategoryInfo? = mutex.withLock {
        if (isCacheValid()) {
            return categoryInfoCache[categoryId]
        }
        return null
    }
    
    /**
     * 缓存分类信息
     * @param info 分类信息
     */
    suspend fun cacheCategoryInfo(info: SelectedCategoryInfo) = mutex.withLock {
        categoryInfoCache[info.categoryId] = info
        updateCacheTime()
    }
    
    /**
     * 获取缓存的常用分类
     * @param key 缓存键（userId_type）
     * @return 缓存的常用分类，如果不存在或过期返回null
     */
    suspend fun getCachedFrequentCategories(key: String): List<Category>? = mutex.withLock {
        if (isCacheValid()) {
            return frequentCategoriesCache[key]
        }
        return null
    }
    
    /**
     * 缓存常用分类
     * @param key 缓存键（userId_type）
     * @param categories 常用分类列表
     */
    suspend fun cacheFrequentCategories(key: String, categories: List<Category>) = mutex.withLock {
        frequentCategoriesCache[key] = categories
        updateCacheTime()
    }
    
    /**
     * 清除所有缓存
     * 在分类数据发生变化时调用
     */
    suspend fun clearCache() = mutex.withLock {
        categoryTreeCache.clear()
        categoryInfoCache.clear()
        frequentCategoriesCache.clear()
        lastCacheTime = 0L
    }
    
    /**
     * 清除特定用户的缓存
     * @param userId 用户ID
     */
    suspend fun clearUserCache(userId: String) = mutex.withLock {
        // 清除包含该用户ID的缓存键
        categoryTreeCache.keys.filter { it.startsWith(userId) }.forEach {
            categoryTreeCache.remove(it)
        }
        frequentCategoriesCache.keys.filter { it.startsWith(userId) }.forEach {
            frequentCategoriesCache.remove(it)
        }
    }
    
    /**
     * 清除特定分类的缓存
     * @param categoryId 分类ID
     */
    suspend fun clearCategoryCache(categoryId: String) = mutex.withLock {
        categoryInfoCache.remove(categoryId)
    }
    
    /**
     * 检查缓存是否有效
     */
    private fun isCacheValid(): Boolean {
        return System.currentTimeMillis() - lastCacheTime < CACHE_VALIDITY_DURATION
    }
    
    /**
     * 更新缓存时间
     */
    private fun updateCacheTime() {
        lastCacheTime = System.currentTimeMillis()
    }
    
    /**
     * 生成缓存键
     * @param userId 用户ID
     * @param type 分类类型
     * @return 缓存键
     */
    fun generateCacheKey(userId: String, type: String): String {
        return "${userId}_${type}"
    }
}