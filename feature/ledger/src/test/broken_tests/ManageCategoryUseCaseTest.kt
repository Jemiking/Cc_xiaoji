package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.*
import java.util.UUID

/**
 * 管理分类UseCase单元测试
 */
class ManageCategoryUseCaseTest {
    
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var manageCategoryUseCase: ManageCategoryUseCase
    
    @Before
    fun setup() {
        categoryRepository = mock()
        manageCategoryUseCase = ManageCategoryUseCase(categoryRepository)
    }
    
    @Test
    fun `创建父分类成功`() = runTest {
        // Given
        val userId = "test_user"
        val name = "餐饮"
        val type = "EXPENSE"
        val icon = "🍔"
        val color = "#FF9800"
        
        whenever(categoryRepository.getParentCategories(userId, type))
            .thenReturn(emptyList())
        whenever(categoryRepository.createCategory(any(), any(), any(), any(), isNull()))
            .thenReturn(123L)
        
        // When
        val result = manageCategoryUseCase.createParentCategory(userId, name, type, icon, color)
        
        // Then
        assertEquals(123L, result)
        verify(categoryRepository).createCategory(name, type, icon, color, null)
    }
    
    @Test
    fun `创建父分类失败_名称已存在`() = runTest {
        // Given
        val userId = "test_user"
        val name = "餐饮"
        val type = "EXPENSE"
        
        val existingCategory = Category(
            id = "existing_id",
            userId = userId,
            name = name,
            type = Category.Type.EXPENSE,
            icon = "🍔",
            color = "#FF9800",
            parentId = null,
            level = 1,
            path = name,
            displayOrder = 0,
            isActive = true,
            isSystem = false,
            usageCount = 0
        )
        
        whenever(categoryRepository.getParentCategories(userId, type))
            .thenReturn(listOf(existingCategory))
        
        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                manageCategoryUseCase.createParentCategory(userId, name, type, "🍔", "#FF9800")
            }
        }
    }
    
    @Test
    fun `创建子分类成功`() = runTest {
        // Given
        val parentId = "parent_123"
        val name = "早餐"
        val icon = "☕"
        val color = "#FF9800"
        val userId = "test_user"
        
        val parentCategory = Category(
            id = parentId,
            userId = userId,
            name = "餐饮",
            type = Category.Type.EXPENSE,
            icon = "🍔",
            color = "#FF9800",
            parentId = null,
            level = 1,
            path = "餐饮",
            displayOrder = 0,
            isActive = true,
            isSystem = false,
            usageCount = 0
        )
        
        whenever(categoryRepository.getCategoryById(parentId))
            .thenReturn(parentCategory)
        whenever(categoryRepository.getCategoryTree(parentId, "EXPENSE"))
            .thenReturn(listOf(CategoryGroup(parentCategory, emptyList())))
        whenever(categoryRepository.createSubcategory(parentId, name, icon, color))
            .thenReturn("child_456")
        
        // When
        val result = manageCategoryUseCase.createSubcategory(parentId, name, icon, color)
        
        // Then
        assertEquals("child_456", result)
        verify(categoryRepository).createSubcategory(parentId, name, icon, color)
    }
    
    @Test
    fun `检查并初始化默认分类_用户无分类时创建`() = runTest {
        // Given
        val userId = "test_user"
        
        whenever(categoryRepository.getCategoryTree(userId, "EXPENSE"))
            .thenReturn(emptyList())
        whenever(categoryRepository.getCategoryTree(userId, "INCOME"))
            .thenReturn(emptyList())
        whenever(categoryRepository.createCategory(any(), any(), any(), any(), any()))
            .thenReturn(1L)
        whenever(categoryRepository.createSubcategory(any(), any(), any(), any()))
            .thenReturn(UUID.randomUUID().toString())
        
        // When
        manageCategoryUseCase.checkAndInitializeDefaultCategories(userId)
        
        // Then
        // 验证创建了默认分类
        verify(categoryRepository, atLeast(1)).createCategory(any(), eq("EXPENSE"), any(), any(), isNull())
        verify(categoryRepository, atLeast(1)).createCategory(any(), eq("INCOME"), any(), any(), isNull())
    }
    
    @Test
    fun `检查并初始化默认分类_用户有分类时跳过`() = runTest {
        // Given
        val userId = "test_user"
        
        val existingCategory = Category(
            id = "existing",
            userId = userId,
            name = "已有分类",
            type = Category.Type.EXPENSE,
            icon = "📝",
            color = "#000000",
            parentId = null,
            level = 1,
            path = "已有分类",
            displayOrder = 0,
            isActive = true,
            isSystem = false,
            usageCount = 0
        )
        
        whenever(categoryRepository.getCategoryTree(userId, "EXPENSE"))
            .thenReturn(listOf(CategoryGroup(existingCategory, emptyList())))
        whenever(categoryRepository.getCategoryTree(userId, "INCOME"))
            .thenReturn(emptyList())
        
        // When
        manageCategoryUseCase.checkAndInitializeDefaultCategories(userId)
        
        // Then
        // 验证没有创建新分类
        verify(categoryRepository, never()).createCategory(any(), any(), any(), any(), any())
    }
    
    @Test
    fun `更新分类信息`() = runTest {
        // Given
        val category = Category(
            id = "cat_123",
            userId = "test_user",
            name = "原名称",
            type = Category.Type.EXPENSE,
            icon = "📝",
            color = "#000000",
            parentId = null,
            level = 1,
            path = "原名称",
            displayOrder = 0,
            isActive = true,
            isSystem = false,
            usageCount = 0
        )
        
        val newName = "新名称"
        val newIcon = "🎯"
        val newColor = "#FF0000"
        
        // When
        manageCategoryUseCase.updateCategory(category, newName, newIcon, newColor)
        
        // Then
        val expectedCategory = category.copy(
            name = newName,
            icon = newIcon,
            color = newColor
        )
        verify(categoryRepository).updateCategory(expectedCategory)
    }
    
    @Test
    fun `删除分类`() = runTest {
        // Given
        val categoryId = "cat_123"
        
        // When
        manageCategoryUseCase.deleteCategory(categoryId)
        
        // Then
        verify(categoryRepository).deleteCategory(categoryId)
    }
    
    @Test
    fun `切换分类状态`() = runTest {
        // Given
        val categoryId = "cat_123"
        val isActive = false
        
        // When
        manageCategoryUseCase.toggleCategoryStatus(categoryId, isActive)
        
        // Then
        verify(categoryRepository).toggleCategoryStatus(categoryId, isActive)
    }
    
    @Test
    fun `获取分类完整信息`() = runTest {
        // Given
        val categoryId = "cat_123"
        val expectedInfo = SelectedCategoryInfo(
            categoryId = categoryId,
            categoryName = "早餐",
            categoryIcon = "☕",
            categoryColor = "#FF9800",
            parentId = "parent_123",
            parentName = "餐饮",
            parentIcon = "🍔",
            parentColor = "#FF9800",
            fullPath = "餐饮/早餐"
        )
        
        whenever(categoryRepository.getCategoryFullInfo(categoryId))
            .thenReturn(expectedInfo)
        
        // When
        val result = manageCategoryUseCase.getCategoryFullInfo(categoryId)
        
        // Then
        assertEquals(expectedInfo, result)
    }
}