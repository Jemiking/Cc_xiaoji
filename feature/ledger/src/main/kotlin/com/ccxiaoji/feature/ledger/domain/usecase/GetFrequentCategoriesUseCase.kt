package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * 获取常用分类的UseCase
 * 基于使用频率返回用户最常用的分类
 */
class GetFrequentCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    /**
     * 获取常用分类
     * @param userId 用户ID
     * @param type 分类类型
     * @param limit 返回数量（默认5个）
     * @return 常用分类列表
     */
    suspend operator fun invoke(
        userId: String,
        type: String,
        limit: Int = 5
    ): List<Category> {
        return categoryRepository.getFrequentCategories(userId, type, limit)
    }
    
    /**
     * 记录分类使用
     * 每次选择分类时调用，用于更新使用频率
     */
    suspend fun recordCategoryUsage(categoryId: String) {
        categoryRepository.incrementCategoryUsage(categoryId)
    }
    
    /**
     * 获取常用支出分类
     */
    suspend fun getFrequentExpenseCategories(
        userId: String,
        limit: Int = 5
    ): List<Category> {
        return invoke(userId, "EXPENSE", limit)
    }
    
    /**
     * 获取常用收入分类
     */
    suspend fun getFrequentIncomeCategories(
        userId: String,
        limit: Int = 5
    ): List<Category> {
        return invoke(userId, "INCOME", limit)
    }
}