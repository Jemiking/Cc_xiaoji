package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取分类列表用例
 */
class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    /**
     * 获取所有分类
     */
    operator fun invoke(): Flow<List<Category>> {
        return repository.getCategories()
    }
    
    /**
     * 获取收入分类
     */
    fun getIncomeCategories(): Flow<List<Category>> {
        return repository.getIncomeCategories()
    }
    
    /**
     * 获取支出分类
     */
    fun getExpenseCategories(): Flow<List<Category>> {
        return repository.getExpenseCategories()
    }
}