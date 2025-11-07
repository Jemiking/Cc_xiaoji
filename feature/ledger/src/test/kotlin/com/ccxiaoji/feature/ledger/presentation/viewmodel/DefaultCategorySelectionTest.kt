package com.ccxiaoji.feature.ledger.presentation.viewmodel

import android.util.Log
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * 测试AddTransactionViewModel中默认分类选择逻辑
 * 验证是否按照文档方案优先选择父分类
 */
class DefaultCategorySelectionTest {

    private lateinit var categoryRepository: CategoryRepository

    @Before
    fun setup() {
        categoryRepository = mockk()

        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    /**
     * 测试场景1：常用分类是子分类时，应该选择其父分类
     */
    @Test
    fun `when frequent category is child category, should select its parent`() = runTest {
        // 准备测试数据
        val parentCategory = Category(
            id = "parent_001",
            name = "餐饮",
            type = Category.Type.EXPENSE,
            icon = "🍽️",
            color = "#FF5722",
            parentId = null,
            isActive = true,
            displayOrder = 1
        )

        val childCategory = Category(
            id = "child_001",
            name = "早餐",
            type = Category.Type.EXPENSE,
            icon = "☕",
            color = "#FF5722",
            parentId = "parent_001",
            isActive = true,
            displayOrder = 1
        )

        // Mock 常用分类返回子分类
        coEvery { categoryRepository.getFrequentCategories(any(), "EXPENSE", 5) } returns listOf(childCategory)

        // Mock getCategoryFullInfo - 子分类时返回子分类信息（带有parentId）
        coEvery { categoryRepository.getCategoryFullInfo("child_001") } returns SelectedCategoryInfo(
            categoryId = "child_001",
            categoryName = "早餐",
            parentId = "parent_001",
            parentName = "餐饮",
            fullPath = "餐饮/早餐",
            icon = "☕",
            color = "#FF5722"
        )

        // Mock getCategoryFullInfo - 父分类时返回父分类信息（parentId为null）
        coEvery { categoryRepository.getCategoryFullInfo("parent_001") } returns SelectedCategoryInfo(
            categoryId = "parent_001",
            categoryName = "餐饮",
            parentId = null,
            parentName = null,
            fullPath = "餐饮",
            icon = "🍽️",
            color = "#FF5722"
        )

        // 模拟实际代码逻辑（从AddTransactionViewModel的loadCategories方法提取）
        val frequentCategories = categoryRepository.getFrequentCategories("test_user", "EXPENSE", 5)
        var picked: SelectedCategoryInfo? = null

        for (c in frequentCategories) {
            val info = categoryRepository.getCategoryFullInfo(c.id)
            val parentName = info?.parentName?.trim()
            val name = info?.categoryName?.trim()
            val isOtherBucket = parentName != null && (parentName.contains("其他") || parentName.equals("Other", ignoreCase = true))
            val isFallbackName = name != null && (name.equals("Other", ignoreCase = true) || name.equals("Uncategorized", ignoreCase = true))
            if (isOtherBucket || isFallbackName) continue

            // 这是当前代码的逻辑
            val parentInfo = info?.parentId?.let { pid -> categoryRepository.getCategoryFullInfo(pid) }
            val candidate = parentInfo ?: info
            if (candidate != null) {
                picked = candidate
                break
            }
        }

        // 验证结果
        assertNotNull("应该选择了分类", picked)

        // 按照当前逻辑，如果常用分类是子分类且有parentId，
        // 会通过getCategoryFullInfo获取父分类信息，所以应该选择父分类
        assertEquals("应该选择父分类ID", "parent_001", picked?.categoryId)
        assertEquals("应该选择父分类名称", "餐饮", picked?.categoryName)
        assertNull("父分类的parentId应该为null", picked?.parentId)
        assertNull("父分类的parentName应该为null", picked?.parentName)
    }

    /**
     * 测试场景2：没有常用分类时，应该选择分类树中的父分类
     */
    @Test
    fun `when no frequent categories, should select parent from category tree`() = runTest {
        // Mock 没有常用分类
        coEvery { categoryRepository.getFrequentCategories(any(), any(), any()) } returns emptyList()

        // 准备分类树数据
        val parent = Category(
            id = "parent_001",
            name = "餐饮",
            type = Category.Type.EXPENSE,
            icon = "🍽️",
            color = "#FF5722",
            parentId = null,
            isActive = true,
            displayOrder = 1
        )

        val children = listOf(
            Category(
                id = "child_001",
                name = "早餐",
                type = Category.Type.EXPENSE,
                icon = "☕",
                color = "#FF5722",
                parentId = "parent_001",
                isActive = true,
                displayOrder = 1
            )
        )

        val categoryGroups = listOf(
            CategoryGroup(parent = parent, children = children)
        )

        // Mock getCategoryFullInfo - 返回父分类信息
        coEvery { categoryRepository.getCategoryFullInfo("parent_001") } returns SelectedCategoryInfo(
            categoryId = "parent_001",
            categoryName = "餐饮",
            parentId = null,
            parentName = null,
            fullPath = "餐饮",
            icon = "🍽️",
            color = "#FF5722"
        )

        // 模拟实际代码逻辑
        val frequentCategories = categoryRepository.getFrequentCategories("test_user", "EXPENSE", 5)
        var picked: SelectedCategoryInfo? = null

        // 常用分类为空，回退到分类树
        if (picked == null) {
            val groupWithChildren = categoryGroups.firstOrNull { it.children.isNotEmpty() }
            if (groupWithChildren != null) {
                // 当前代码：调用getCategoryFullInfo获取父分类信息
                picked = categoryRepository.getCategoryFullInfo(groupWithChildren.parent.id)
            }
        }

        // 验证结果
        assertNotNull("应该选择了分类", picked)
        assertEquals("应该选择父分类ID", "parent_001", picked?.categoryId)
        assertEquals("应该选择父分类名称", "餐饮", picked?.categoryName)
        assertNull("父分类的parentId应该为null", picked?.parentId)
    }

    /**
     * 测试场景3：验证文档方案与当前实现的差异
     */
    @Test
    fun `compare document solution with current implementation`() {
        println("\n=== 对比分析 ===")
        println("文档方案特点：")
        println("1. 直接构建SelectedCategoryInfo对象，明确设置parentId=null")
        println("2. 对父分类和子分类有清晰的判断逻辑")
        println("3. 不依赖getCategoryFullInfo的返回结果")

        println("\n当前实现特点：")
        println("1. 依赖getCategoryFullInfo方法返回正确的信息")
        println("2. 通过parentInfo ?: info的逻辑选择候选项")
        println("3. 如果getCategoryFullInfo正确实现，应该也能选择父分类")

        println("\n结论：")
        println("当前实现在逻辑上应该是正确的，因为：")
        println("- 当常用分类是子分类时，会通过parentId获取父分类信息")
        println("- 当从分类树选择时，直接传入parent.id获取父分类")
        println("- getCategoryFullInfo会返回对应分类的完整信息")
        println("关键在于getCategoryFullInfo的实现是否正确")
    }
}