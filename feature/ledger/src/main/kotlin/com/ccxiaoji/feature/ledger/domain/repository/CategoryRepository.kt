package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryWithStats
import kotlinx.coroutines.flow.Flow

/**
 * 分类仓库接口
 * 定义所有分类相关的数据操作
 */
interface CategoryRepository {
    /**
     * 获取所有分类
     */
    fun getCategories(): Flow<List<Category>>
    
    /**
     * 获取收入分类
     */
    fun getIncomeCategories(): Flow<List<Category>>
    
    /**
     * 获取支出分类
     */
    fun getExpenseCategories(): Flow<List<Category>>
    
    /**
     * 创建分类
     */
    suspend fun createCategory(
        name: String,
        type: String,
        icon: String,
        color: String,
        parentId: String? = null
    ): Long
    
    /**
     * 更新分类
     */
    suspend fun updateCategory(category: Category)
    
    /**
     * 删除分类
     */
    suspend fun deleteCategory(categoryId: String)
    
    /**
     * 根据类型获取分类
     */
    fun getCategoriesByType(type: Category.Type): Flow<List<Category>>
    
    /**
     * 根据ID获取分类
     */
    suspend fun getCategoryById(categoryId: String): Category?
}