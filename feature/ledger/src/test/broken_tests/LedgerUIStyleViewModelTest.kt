package com.ccxiaoji.feature.ledger.presentation.viewmodel

import com.ccxiaoji.feature.ledger.domain.model.LedgerUIStyle
import com.ccxiaoji.feature.ledger.domain.repository.LedgerUIPreferencesRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * LedgerUIStyleViewModel的单元测试
 * 验证UI风格管理的核心功能
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LedgerUIStyleViewModelTest {

    @MockK
    private lateinit var uiPreferencesRepository: LedgerUIPreferencesRepository

    private lateinit var viewModel: LedgerUIStyleViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        // 设置默认的Repository返回值
        coEvery { uiPreferencesRepository.getUIPreferences() } returns flowOf(
            com.ccxiaoji.feature.ledger.domain.model.LedgerUIPreferences(
                uiStyle = LedgerUIStyle.BALANCED,
                animationDurationMs = 300
            )
        )
        
        viewModel = LedgerUIStyleViewModel(uiPreferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初始状态应该加载正确的UI偏好设置`() = runTest {
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(LedgerUIStyle.BALANCED, uiState.uiStyle)
        assertEquals(300, uiState.animationDurationMs)
    }

    @Test
    fun `更新UI风格应该调用Repository并更新状态`() = runTest {
        // Given
        coEvery { uiPreferencesRepository.updateUIStyle(any()) } returns Unit

        // When
        viewModel.updateUIStyle(LedgerUIStyle.HIERARCHICAL)

        // Then
        coVerify { uiPreferencesRepository.updateUIStyle(LedgerUIStyle.HIERARCHICAL) }
        assertEquals(LedgerUIStyle.HIERARCHICAL, viewModel.uiState.value.uiStyle)
    }

    @Test
    fun `切换UI风格应该在两种风格之间正确切换`() = runTest {
        // Given
        coEvery { uiPreferencesRepository.updateUIStyle(any()) } returns Unit

        // When - 从BALANCED切换到HIERARCHICAL
        viewModel.toggleUIStyle()

        // Then
        coVerify { uiPreferencesRepository.updateUIStyle(LedgerUIStyle.HIERARCHICAL) }
        
        // When - 再次切换应该回到BALANCED
        viewModel.toggleUIStyle()
        
        // Then
        coVerify { uiPreferencesRepository.updateUIStyle(LedgerUIStyle.BALANCED) }
    }


    @Test
    fun `更新动画持续时间应该限制在有效范围内`() = runTest {
        // Given
        coEvery { uiPreferencesRepository.updateAnimationDuration(any()) } returns Unit

        // When - 测试超出范围的值
        viewModel.updateAnimationDuration(50) // 小于最小值100
        
        // Then
        coVerify { uiPreferencesRepository.updateAnimationDuration(100) } // 应该被限制为100

        // When - 测试另一个超出范围的值
        viewModel.updateAnimationDuration(1500) // 大于最大值1000
        
        // Then
        coVerify { uiPreferencesRepository.updateAnimationDuration(1000) } // 应该被限制为1000
    }


    @Test
    fun `获取UI偏好设置应该返回当前状态的LedgerUIPreferences对象`() = runTest {
        // When
        val preferences = viewModel.getUIPreferences()

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(uiState.uiStyle, preferences.uiStyle)
        assertEquals(uiState.animationDurationMs, preferences.animationDurationMs)
    }

    @Test
    fun `重置所有设置应该调用Repository`() = runTest {
        // Given
        coEvery { uiPreferencesRepository.resetToDefaults() } returns Unit

        // When
        viewModel.resetAllSettings()

        // Then
        coVerify { uiPreferencesRepository.resetToDefaults() }
    }

    @Test
    fun `Repository操作失败时应该设置错误状态`() = runTest {
        // Given
        val errorMessage = "网络错误"
        coEvery { uiPreferencesRepository.updateUIStyle(any()) } throws Exception(errorMessage)

        // When
        viewModel.updateUIStyle(LedgerUIStyle.HIERARCHICAL)

        // Then
        val uiState = viewModel.uiState.value
        assert(uiState.error?.contains("保存设置失败") == true)
        assert(uiState.error?.contains(errorMessage) == true)
    }

    @Test
    fun `清除错误状态应该将error设置为null`() = runTest {
        // Given - 先设置一个错误状态
        coEvery { uiPreferencesRepository.updateUIStyle(any()) } throws Exception("测试错误")
        viewModel.updateUIStyle(LedgerUIStyle.HIERARCHICAL)
        assert(viewModel.uiState.value.error != null)

        // When
        viewModel.clearError()

        // Then
        assertEquals(null, viewModel.uiState.value.error)
    }
}