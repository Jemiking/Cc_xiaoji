package com.ccxiaoji.feature.ledger.domain.usecase

import android.util.Log
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * 加载指定收支方向的分类数据UseCase
 *
 * 职责：
 * 1. 加载分类树结构
 * 2. 加载常用分类列表
 * 3. 智能选择默认分类（4级回退算法）
 *
 * @author Claude Code
 * @since 2025-11-14
 */
class LoadCategoriesForDirectionUseCase @Inject constructor(
    private val getCategoryTree: GetCategoryTreeUseCase,
    private val getFrequentCategories: GetFrequentCategoriesUseCase,
    private val categoryRepository: CategoryRepository
) {
    companion object {
        private const val TAG = "LoadCategoriesForDir"

        // 默认分类选择排除关键词
        private const val FALLBACK_CATEGORY_ZH = "其他"
        private const val FALLBACK_CATEGORY_EN = "Other"
        private const val UNCATEGORIZED_EN = "Uncategorized"

        // 默认常用分类数量
        private const val DEFAULT_FREQUENT_LIMIT = 5
    }

    /**
     * 加载分类数据并智能选择默认分类
     *
     * @param userId 用户ID
     * @param isIncome 是否为收入类型
     * @param currentSelectedCategory 当前已选分类（如有）
     * @param frequentLimit 常用分类数量限制，默认5个
     * @return 包含分类树、常用分类和智能默认选择的结果
     */
    suspend operator fun invoke(
        userId: String,
        isIncome: Boolean,
        currentSelectedCategory: SelectedCategoryInfo? = null,
        frequentLimit: Int = DEFAULT_FREQUENT_LIMIT
    ): LoadCategoriesResult {
        val direction = if (isIncome) "INCOME" else "EXPENSE"

        Log.d(TAG, "加载分类数据：userId=$userId, direction=$direction")

        // 1. 并行加载分类树和常用分类
        val categoryGroups = getCategoryTree(userId, direction)
        val frequentCategories = getFrequentCategories(userId, direction, frequentLimit)

        Log.d(TAG, "加载完成：分类树${categoryGroups.size}组，常用分类${frequentCategories.size}个")

        // 调试信息：打印分类树结构
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            categoryGroups.forEachIndexed { idx, group ->
                Log.d(TAG, "  组$idx: ${group.parent.name} (id=${group.parent.id}, 子分类=${group.children.size}个)")
            }
        }

        // 2. 智能选择默认分类
        val defaultCategory = selectDefaultCategory(
            currentSelected = currentSelectedCategory,
            frequentCategories = frequentCategories,
            categoryGroups = categoryGroups
        )

        Log.d(TAG, "默认分类选择结果：${defaultCategory?.let { "${it.categoryName}(${it.categoryId})" } ?: "null"}")

        return LoadCategoriesResult(
            categoryGroups = categoryGroups,
            frequentCategories = frequentCategories,
            defaultSelectedCategory = defaultCategory
        )
    }

    /**
     * 智能选择默认分类（4级回退算法）
     *
     * 算法优先级：
     * 1. [最高] 保持当前已选分类（如果存在且有效）
     * 2. [高] 常用分类的父分类（排除"其他"类兜底分类）
     * 3. [中] 分类树中第一个有子分类的父分类
     * 4. [低] 分类树中第一个非"其他"的父分类
     * 5. [兜底] 分类树中第一个分类
     */
    private suspend fun selectDefaultCategory(
        currentSelected: SelectedCategoryInfo?,
        frequentCategories: List<Category>,
        categoryGroups: List<CategoryGroup>
    ): SelectedCategoryInfo? {
        // 级别1：保持当前选择
        if (currentSelected != null && currentSelected.categoryId.isNotEmpty()) {
            Log.d(TAG, "  选择策略1：保持当前选择 - ${currentSelected.categoryName}")
            return currentSelected
        }

        // 级别2：从常用分类中选择
        val fromFrequent = selectFromFrequentCategories(frequentCategories)
        if (fromFrequent != null) {
            Log.d(TAG, "  选择策略2：常用分类 - ${fromFrequent.categoryName}")
            return fromFrequent
        }

        // 级别3：有子分类的父分类
        val withChildren = categoryGroups.firstOrNull { it.children.isNotEmpty() }
        if (withChildren != null) {
            val category = categoryRepository.getCategoryFullInfo(withChildren.parent.id)
            if (category != null) {
                Log.d(TAG, "  选择策略3：有子分类的父分类 - ${category.categoryName}")
                return category
            }
        }

        // 级别4：非"其他"的父分类
        val nonFallback = categoryGroups.firstOrNull { group ->
            !isFallbackCategory(group.parent.name)
        }
        if (nonFallback != null) {
            val category = categoryRepository.getCategoryFullInfo(nonFallback.parent.id)
            if (category != null) {
                Log.d(TAG, "  选择策略4：非兜底父分类 - ${category.categoryName}")
                return category
            }
        }

        // 级别5：兜底选择第一个
        val firstCategory = categoryGroups.firstOrNull()?.let { group ->
            categoryRepository.getCategoryFullInfo(group.parent.id)
        }
        if (firstCategory != null) {
            Log.d(TAG, "  选择策略5：兜底第一个 - ${firstCategory.categoryName}")
        }
        return firstCategory
    }

    /**
     * 从常用分类中智能选择
     *
     * 选择规则：
     * 1. 跳过名称包含"其他"、"Other"、"Uncategorized"的分类
     * 2. 优先选择父分类（如果该分类有父分类）
     * 3. 否则选择该分类本身
     */
    private suspend fun selectFromFrequentCategories(
        frequentCategories: List<Category>
    ): SelectedCategoryInfo? {
        for (category in frequentCategories) {
            val info = categoryRepository.getCategoryFullInfo(category.id) ?: continue

            // 检查父分类名是否为兜底分类
            val parentName = info.parentName?.trim()
            if (parentName != null && isFallbackCategory(parentName)) {
                Log.d(TAG, "    跳过常用分类：${info.categoryName}（父分类为兜底）")
                continue
            }

            // 检查分类本身是否为兜底分类
            val categoryName = info.categoryName.trim()
            if (isFallbackCategory(categoryName)) {
                Log.d(TAG, "    跳过常用分类：${categoryName}（自身为兜底）")
                continue
            }

            // 优先返回父分类
            if (info.parentId != null) {
                val parentInfo = categoryRepository.getCategoryFullInfo(info.parentId)
                if (parentInfo != null && !isFallbackCategory(parentInfo.categoryName)) {
                    Log.d(TAG, "    选中常用分类的父分类：${parentInfo.categoryName}")
                    return parentInfo
                }
            }

            // 返回分类本身
            Log.d(TAG, "    选中常用分类：${info.categoryName}")
            return info
        }
        return null
    }

    /**
     * 判断是否为兜底分类
     *
     * 兜底分类特征：
     * - 包含"其他"（中文）
     * - 等于"Other"（英文，不区分大小写）
     * - 等于"Uncategorized"（英文，不区分大小写）
     */
    private fun isFallbackCategory(name: String?): Boolean {
        if (name.isNullOrBlank()) return false

        val trimmed = name.trim()
        return when {
            trimmed.contains(FALLBACK_CATEGORY_ZH) -> true
            trimmed.equals(FALLBACK_CATEGORY_EN, ignoreCase = true) -> true
            trimmed.equals(UNCATEGORIZED_EN, ignoreCase = true) -> true
            else -> false
        }
    }
}

/**
 * 分类加载结果数据类
 *
 * @property categoryGroups 分类树结构，包含父分类和子分类的层级关系
 * @property frequentCategories 常用分类列表，按使用频率排序
 * @property defaultSelectedCategory 智能选择的默认分类，可能为null
 */
data class LoadCategoriesResult(
    val categoryGroups: List<CategoryGroup>,
    val frequentCategories: List<Category>,
    val defaultSelectedCategory: SelectedCategoryInfo?
)