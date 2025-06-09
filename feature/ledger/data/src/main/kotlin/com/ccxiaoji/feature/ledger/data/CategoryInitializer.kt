package com.ccxiaoji.feature.ledger.data

import com.ccxiaoji.feature.ledger.data.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 分类初始化服务
 * 用于在应用启动时初始化默认分类
 */
@Singleton
class CategoryInitializer @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    
    /**
     * 初始化默认分类
     * 如果数据库中已有分类，则不执行任何操作
     */
    suspend fun initializeDefaultCategories() {
        categoryRepository.initializeDefaultCategories()
    }
}