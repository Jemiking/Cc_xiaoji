package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryWithParent
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.*
import java.util.UUID

/**
 * 分类仓库实现单元测试
 */
class CategoryRepositoryImplTest {
    
    private lateinit var categoryDao: CategoryDao
    private lateinit var userApi: UserApi
    private lateinit var repository: CategoryRepositoryImpl
    
    private val testUserId = "test_user_123"
    
    @Before
    fun setup() {
        categoryDao = mockk()
        userApi = mockk()
        repository = CategoryRepositoryImpl(categoryDao, userApi)
        
        every { userApi.getCurrentUserId() } returns testUserId
    }
    
    @Test
    fun `获取分类树结构_正确组织父子关系`() = runTest {
        // Given
        val parentEntity = CategoryEntity(
            id = "parent_1",
            userId = testUserId,
            name = "餐饮",
            type = "EXPENSE",
            icon = "🍔",
            color = "#FF9800",
            parentId = null,
            displayOrder = 0,
            isSystem = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isDeleted = false,
            syncStatus = SyncStatus.SYNCED
        )
        
        val childEntity1 = CategoryEntity(
            id = "child_1",
            userId = testUserId,
            name = "早餐",
            type = "EXPENSE",
            icon = "☕",
            color = "#FF9800",
            parentId = "parent_1",
            displayOrder = 0,
            isSystem = false,
            usageCount = 5,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isDeleted = false,
            syncStatus = SyncStatus.SYNCED
        )
        
        val childEntity2 = CategoryEntity(
            id = "child_2",
            userId = testUserId,
            name = "午餐",
            type = "EXPENSE",
            icon = "🍜",
            color = "#FF9800",
            parentId = "parent_1",
            displayOrder = 1,
            isSystem = false,
            usageCount = 3,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isDeleted = false,
            syncStatus = SyncStatus.SYNCED
        )
        
        every { categoryDao.getCategoriesByTypeWithLevels(testUserId, "EXPENSE") } returns listOf(parentEntity, childEntity1, childEntity2)
        
        // When
        val result = repository.getCategoryTree(testUserId, "EXPENSE")
        
        // Then
        assertEquals(1, result.size)
        val group = result[0]
        assertEquals("餐饮", group.parent.name)
        assertEquals(2, group.children.size)
        assertEquals("早餐", group.children[0].name)
        assertEquals("午餐", group.children[1].name)
    }
    
    @Test
    fun `获取叶子分类_只返回二级分类`() = runTest {
        // Given
        val entities = listOf(
            CategoryEntity(
                id = "parent_1",
                userId = testUserId,
                name = "餐饮",
                type = "EXPENSE",
                icon = "🍔",
                color = "#FF9800",
                parentId = null,
                displayOrder = 0,
                isSystem = false,
                usageCount = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            ),
            CategoryEntity(
                id = "child_1",
                userId = testUserId,
                name = "早餐",
                type = "EXPENSE",
                icon = "☕",
                color = "#FF9800",
                parentId = "parent_1",
                displayOrder = 0,
                isSystem = false,
                usageCount = 5,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            )
        )
        
        every { categoryDao.getLeafCategories(testUserId, "EXPENSE") } returns listOf(entities[1]) // 只返回子分类
        
        // When
        val result = repository.getLeafCategories(testUserId, "EXPENSE")
        
        // Then
        assertEquals(1, result.size)
        assertEquals("早餐", result[0].name)
        assertEquals(2, result[0].level)
    }
    
    @Test
    fun `创建分类_设置正确的层级和路径`() = runTest {
        // Given
        val name = "餐饮"
        val type = "EXPENSE"
        val icon = "🍔"
        val color = "#FF9800"
        
        every { categoryDao.getCategoriesByType(testUserId, type) } returns flowOf(emptyList())
        
        // When
        val result = repository.createCategory(name, type, icon, color, null)
        
        // Then
        verify { categoryDao.insertCategory(match { entity ->
            entity.name == name &&
            entity.type == type &&
            entity.icon == icon &&
            entity.color == color &&
            entity.parentId == null &&
            entity.userId == testUserId
        })
        assertTrue(result > 0)
    }
    
    @Test
    fun `创建子分类_继承父分类属性`() = runTest {
        // Given
        val parentId = "parent_123"
        val name = "早餐"
        val icon = "☕"
        
        val parentEntity = CategoryEntity(
            id = parentId,
            userId = testUserId,
            name = "餐饮",
            type = "EXPENSE",
            icon = "🍔",
            color = "#FF9800",
            parentId = null,
            displayOrder = 0,
            isSystem = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isDeleted = false,
            syncStatus = SyncStatus.SYNCED
        )
        
        every { categoryDao.getCategoryById(parentId) } returns parentEntity
        every { categoryDao.getSubcategories(testUserId, parentId) } returns emptyList()
        
        // When
        val result = repository.createSubcategory(parentId, name, icon, null)
        
        // Then
        verify { categoryDao.insertCategory(match { entity ->
            entity.name == name &&
            entity.icon == icon &&
            entity.color == "#FF9800" && // 继承父分类颜色
            entity.parentId == parentId &&
            entity.type == "EXPENSE" // 继承父分类类型
        })
        assertNotNull(result)
    }
    
    @Test
    fun `获取分类完整信息_包含父分类信息`() = runTest {
        // Given
        val categoryId = "child_123"
        
        val categoryWithParent = CategoryWithParent(
            id = categoryId,
            userId = testUserId,
            name = "早餐",
            type = "EXPENSE",
            icon = "☕",
            color = "#FF9800",
            parentId = "parent_123",
            displayOrder = 0,
            isSystem = false,
            usageCount = 5,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isDeleted = false,
            syncStatus = SyncStatus.SYNCED,
            parentName = "餐饮",
            parentIcon = "🍔",
            parentColor = "#FF9800"
        )
        
        every { categoryDao.getCategoryWithParent(categoryId) } returns categoryWithParent
        
        // When
        val result = repository.getCategoryFullInfo(categoryId)
        
        // Then
        assertNotNull(result)
        assertEquals(categoryId, result?.categoryId)
        assertEquals("早餐", result?.categoryName)
        assertEquals("餐饮", result?.parentName)
        assertEquals("餐饮/早餐", result?.fullPath)
    }
    
    @Test
    fun `获取常用分类_按使用频率排序`() = runTest {
        // Given
        val categories = listOf(
            CategoryEntity(
                id = "cat_1",
                userId = testUserId,
                name = "早餐",
                type = "EXPENSE",
                icon = "☕",
                color = "#FF9800",
                parentId = "parent_1",
                displayOrder = 0,
                isSystem = false,
                usageCount = 10,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            ),
            CategoryEntity(
                id = "cat_2",
                userId = testUserId,
                name = "午餐",
                type = "EXPENSE",
                icon = "🍜",
                color = "#FF9800",
                parentId = "parent_1",
                displayOrder = 1,
                isSystem = false,
                usageCount = 5,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            )
        )
        
        every { categoryDao.getCategoriesByTypeWithLevels(testUserId, "EXPENSE") } returns categories
        
        // When
        val result = repository.getFrequentCategories(testUserId, "EXPENSE", 2)
        
        // Then
        assertEquals(2, result.size)
        assertEquals("早餐", result[0].name) // 使用次数最多的排第一
        assertEquals(10, result[0].usageCount)
    }
    
    @Test
    fun `切换分类状态`() = runTest {
        // Given
        val categoryId = "cat_123"
        val isActive = false
        
        // When
        repository.toggleCategoryStatus(categoryId, isActive)
        
        // Then
        verify { categoryDao.updateCategoryStatus(
            categoryId,
            isActive,
            any()
        ) }
    }
    
    @Test
    fun `增加分类使用计数`() = runTest {
        // Given
        val categoryId = "cat_123"
        
        // When
        repository.incrementCategoryUsage(categoryId)
        
        // Then
        verify { categoryDao.incrementUsageCount(categoryId) }
    }
}