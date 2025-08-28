package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.CategoryWithStats
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
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
    
    // ========== 二级分类支持 ==========
    
    /**
     * 获取分类树结构
     * @param userId 用户ID
     * @param type 分类类型（收入/支出）
     * @return 分类组列表（每个组包含父分类和子分类）
     */
    suspend fun getCategoryTree(userId: String, type: String): List<CategoryGroup>
    
    /**
     * 获取所有叶子分类（二级分类）
     * @param userId 用户ID
     * @param type 分类类型
     * @return 只返回可以选择的二级分类
     */
    suspend fun getLeafCategories(userId: String, type: String): List<Category>
    
    /**
     * 获取父分类列表（一级分类）
     * @param userId 用户ID  
     * @param type 分类类型
     * @return 一级分类列表
     */
    suspend fun getParentCategories(userId: String, type: String): List<Category>
    
    /**
     * 批量创建分类树
     * @param groups 分类组列表
     */
    suspend fun createCategoryTree(groups: List<CategoryGroup>)
    
    /**
     * 获取分类的完整路径
     * @param categoryId 分类ID
     * @return 完整路径字符串，如"餐饮/早餐"
     */
    suspend fun getCategoryPath(categoryId: String): String?
    
    /**
     * 获取分类的完整信息（包含父分类信息）
     * @param categoryId 分类ID
     * @return 包含父分类信息的完整分类信息
     */
    suspend fun getCategoryFullInfo(categoryId: String): SelectedCategoryInfo?
    
    /**
     * 切换分类的启用状态
     * @param categoryId 分类ID
     * @param isActive 是否启用
     */
    suspend fun toggleCategoryStatus(categoryId: String, isActive: Boolean)
    
    /**
     * 创建子分类
     * @param parentId 父分类ID
     * @param name 子分类名称
     * @param icon 图标
     * @param color 颜色（默认继承父分类）
     */
    suspend fun createSubcategory(
        parentId: String,
        name: String,
        icon: String,
        color: String? = null
    ): String
    
    /**
     * 获取常用分类（按使用频率排序）
     * @param userId 用户ID
     * @param type 分类类型
     * @param limit 返回数量限制
     */
    suspend fun getFrequentCategories(
        userId: String,
        type: String,
        limit: Int = 5
    ): List<Category>
    
    /**
     * 增加分类使用计数
     * @param categoryId 分类ID
     */
    suspend fun incrementCategoryUsage(categoryId: String)
    
    // ========== 默认分类支持 ==========
    
    /**
     * 根据名称查找分类
     * @param name 分类名称
     * @return 分类对象，如果不存在则返回null
     */
    suspend fun findCategoryByName(name: String): Category?
    
    /**
     * 创建或获取默认"其他"分类
     * 如果"其他"分类不存在，则创建一个
     * @param userId 用户ID
     * @return 其他分类的ID
     */
    suspend fun getOrCreateDefaultOtherCategory(userId: String): String
    
}