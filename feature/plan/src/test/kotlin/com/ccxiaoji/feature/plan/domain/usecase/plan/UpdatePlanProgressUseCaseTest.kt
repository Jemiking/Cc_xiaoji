package com.ccxiaoji.feature.plan.domain.usecase.plan

import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test

/**
 * UpdatePlanProgressUseCase 单元测试
 * 测试更新计划进度用例的各种场景，包括自动状态更新
 */
class UpdatePlanProgressUseCaseTest {

    @MockK
    private lateinit var planRepository: PlanRepository
    
    private lateinit var updatePlanProgressUseCase: UpdatePlanProgressUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        updatePlanProgressUseCase = UpdatePlanProgressUseCase(planRepository)
    }

    @Test
    fun `成功更新进度 - 进度为50不改变状态`() = runTest {
        // Given - 准备测试数据
        val planId = "test-plan"
        val progress = 50f
        val existingPlan = Plan(
            id = planId,
            title = "测试计划",
            status = PlanStatus.IN_PROGRESS,
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 30f
        )
        
        coJustRun { planRepository.updatePlanProgress(planId, progress) }
        coEvery { planRepository.getPlanById(planId) } returns existingPlan
        // 状态不变，不需要更新状态

        // When - 执行更新操作
        val result = updatePlanProgressUseCase(planId, progress)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(Unit)
        coVerify(exactly = 1) { planRepository.updatePlanProgress(planId, progress) }
        coVerify(exactly = 1) { planRepository.getPlanById(planId) }
        coVerify(exactly = 0) { planRepository.updatePlanStatus(any(), any()) }
    }

    @Test
    fun `成功更新进度 - 进度为0且状态为NOT_STARTED不改变状态`() = runTest {
        // Given - 进度为0且状态为NOT_STARTED的计划
        val planId = "not-started-plan"
        val progress = 0f
        val existingPlan = Plan(
            id = planId,
            title = "未开始计划",
            status = PlanStatus.NOT_STARTED,
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 0f
        )
        
        coJustRun { planRepository.updatePlanProgress(planId, progress) }
        coEvery { planRepository.getPlanById(planId) } returns existingPlan

        // When - 执行更新操作
        val result = updatePlanProgressUseCase(planId, progress)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.updatePlanProgress(planId, progress) }
        coVerify(exactly = 1) { planRepository.getPlanById(planId) }
        coVerify(exactly = 0) { planRepository.updatePlanStatus(any(), any()) }
    }

    @Test
    fun `成功更新进度 - 进度为100自动设置为COMPLETED`() = runTest {
        // Given - 进度更新为100%的计划
        val planId = "completing-plan"
        val progress = 100f
        val existingPlan = Plan(
            id = planId,
            title = "即将完成的计划",
            status = PlanStatus.IN_PROGRESS,
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 80f
        )
        
        coJustRun { planRepository.updatePlanProgress(planId, progress) }
        coEvery { planRepository.getPlanById(planId) } returns existingPlan
        coJustRun { planRepository.updatePlanStatus(planId, PlanStatus.COMPLETED) }

        // When - 执行更新操作
        val result = updatePlanProgressUseCase(planId, progress)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.updatePlanProgress(planId, progress) }
        coVerify(exactly = 1) { planRepository.getPlanById(planId) }
        coVerify(exactly = 1) { planRepository.updatePlanStatus(planId, PlanStatus.COMPLETED) }
    }

    @Test
    fun `成功更新进度 - 进度从0变为50自动设置为IN_PROGRESS`() = runTest {
        // Given - 进度从0变为50的计划
        val planId = "starting-plan"
        val progress = 50f
        val existingPlan = Plan(
            id = planId,
            title = "开始执行的计划",
            status = PlanStatus.NOT_STARTED,
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 0f
        )
        
        coJustRun { planRepository.updatePlanProgress(planId, progress) }
        coEvery { planRepository.getPlanById(planId) } returns existingPlan
        coJustRun { planRepository.updatePlanStatus(planId, PlanStatus.IN_PROGRESS) }

        // When - 执行更新操作
        val result = updatePlanProgressUseCase(planId, progress)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.updatePlanProgress(planId, progress) }
        coVerify(exactly = 1) { planRepository.getPlanById(planId) }
        coVerify(exactly = 1) { planRepository.updatePlanStatus(planId, PlanStatus.IN_PROGRESS) }
    }

    @Test
    fun `成功更新进度 - 已完成计划进度设置为100不改变状态`() = runTest {
        // Given - 已完成的计划进度设置为100%
        val planId = "completed-plan"
        val progress = 100f
        val existingPlan = Plan(
            id = planId,
            title = "已完成计划",
            status = PlanStatus.COMPLETED,
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 100f
        )
        
        coJustRun { planRepository.updatePlanProgress(planId, progress) }
        coEvery { planRepository.getPlanById(planId) } returns existingPlan

        // When - 执行更新操作
        val result = updatePlanProgressUseCase(planId, progress)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.updatePlanProgress(planId, progress) }
        coVerify(exactly = 1) { planRepository.getPlanById(planId) }
        coVerify(exactly = 0) { planRepository.updatePlanStatus(any(), any()) }
    }

    @Test
    fun `更新进度失败 - 进度值超出范围(大于100)`() = runTest {
        // Given - 进度值超出范围
        val planId = "test-plan"
        val invalidProgress = 150f

        // When - 执行更新操作
        val result = updatePlanProgressUseCase(planId, invalidProgress)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("进度必须在0-100之间")
        coVerify(exactly = 0) { planRepository.updatePlanProgress(any(), any()) }
        coVerify(exactly = 0) { planRepository.getPlanById(any()) }
    }

    @Test
    fun `更新进度失败 - 进度值小于0`() = runTest {
        // Given - 进度值为负数
        val planId = "test-plan"
        val invalidProgress = -10f

        // When - 执行更新操作
        val result = updatePlanProgressUseCase(planId, invalidProgress)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("进度必须在0-100之间")
        coVerify(exactly = 0) { planRepository.updatePlanProgress(any(), any()) }
        coVerify(exactly = 0) { planRepository.getPlanById(any()) }
    }

    @Test
    fun `更新进度失败 - 计划不存在`() = runTest {
        // Given - 不存在的计划
        val planId = "non-existent-plan"
        val progress = 50f
        
        coJustRun { planRepository.updatePlanProgress(planId, progress) }
        coEvery { planRepository.getPlanById(planId) } returns null

        // When - 执行更新操作
        val result = updatePlanProgressUseCase(planId, progress)

        // Then - 验证结果（计划不存在时仍然成功，因为进度已更新）
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.updatePlanProgress(planId, progress) }
        coVerify(exactly = 1) { planRepository.getPlanById(planId) }
        coVerify(exactly = 0) { planRepository.updatePlanStatus(any(), any()) }
    }

    @Test
    fun `更新进度失败 - Repository更新进度抛出异常`() = runTest {
        // Given - Repository更新进度时抛出异常
        val planId = "test-plan"
        val progress = 50f
        val exception = RuntimeException("数据库更新失败")
        
        coEvery { planRepository.updatePlanProgress(planId, progress) } throws exception

        // When - 执行更新操作
        val result = updatePlanProgressUseCase(planId, progress)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        coVerify(exactly = 1) { planRepository.updatePlanProgress(planId, progress) }
        coVerify(exactly = 0) { planRepository.getPlanById(any()) }
    }

    @Test
    fun `更新进度失败 - Repository获取计划抛出异常`() = runTest {
        // Given - Repository获取计划时抛出异常
        val planId = "test-plan"
        val progress = 50f
        val exception = RuntimeException("数据库查询失败")
        
        coJustRun { planRepository.updatePlanProgress(planId, progress) }
        coEvery { planRepository.getPlanById(planId) } throws exception

        // When - 执行更新操作
        val result = updatePlanProgressUseCase(planId, progress)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        coVerify(exactly = 1) { planRepository.updatePlanProgress(planId, progress) }
        coVerify(exactly = 1) { planRepository.getPlanById(planId) }
    }

    @Test
    fun `更新进度失败 - Repository更新状态抛出异常`() = runTest {
        // Given - Repository更新状态时抛出异常
        val planId = "test-plan"
        val progress = 100f
        val existingPlan = Plan(
            id = planId,
            title = "测试计划",
            status = PlanStatus.IN_PROGRESS,
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 80f
        )
        val exception = RuntimeException("状态更新失败")
        
        coJustRun { planRepository.updatePlanProgress(planId, progress) }
        coEvery { planRepository.getPlanById(planId) } returns existingPlan
        coEvery { planRepository.updatePlanStatus(planId, PlanStatus.COMPLETED) } throws exception

        // When - 执行更新操作
        val result = updatePlanProgressUseCase(planId, progress)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        coVerify(exactly = 1) { planRepository.updatePlanProgress(planId, progress) }
        coVerify(exactly = 1) { planRepository.getPlanById(planId) }
        coVerify(exactly = 1) { planRepository.updatePlanStatus(planId, PlanStatus.COMPLETED) }
    }

    @Test
    fun `边界条件测试 - 进度为0和100的边界值`() = runTest {
        // Given - 边界值测试
        val planId = "boundary-plan"
        val existingPlan = Plan(
            id = planId,
            title = "边界测试计划",
            status = PlanStatus.IN_PROGRESS,
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 50f
        )
        
        coJustRun { planRepository.updatePlanProgress(planId, 0f) }
        coJustRun { planRepository.updatePlanProgress(planId, 100f) }
        coEvery { planRepository.getPlanById(planId) } returns existingPlan
        coJustRun { planRepository.updatePlanStatus(planId, PlanStatus.IN_PROGRESS) }
        coJustRun { planRepository.updatePlanStatus(planId, PlanStatus.COMPLETED) }

        // When - 测试边界值
        val result0 = updatePlanProgressUseCase(planId, 0f)
        val result100 = updatePlanProgressUseCase(planId, 100f)

        // Then - 验证结果
        assertThat(result0.isSuccess).isTrue()
        assertThat(result100.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.updatePlanProgress(planId, 0f) }
        coVerify(exactly = 1) { planRepository.updatePlanProgress(planId, 100f) }
    }

    @Test
    fun `状态转换逻辑测试 - 各种状态组合`() = runTest {
        // Given - 不同状态的计划
        val plans = mapOf(
            "cancelled-plan" to Plan(
                id = "cancelled-plan",
                title = "已取消计划",
                status = PlanStatus.CANCELLED,
                startDate = LocalDate(2024, 1, 1),
                endDate = LocalDate(2024, 12, 31),
                progress = 20f
            )
        )
        
        val planId = "cancelled-plan"
        val progress = 50f
        
        coJustRun { planRepository.updatePlanProgress(planId, progress) }
        coEvery { planRepository.getPlanById(planId) } returns plans[planId]
        coJustRun { planRepository.updatePlanStatus(planId, PlanStatus.IN_PROGRESS) }

        // When - 执行更新操作
        val result = updatePlanProgressUseCase(planId, progress)

        // Then - 验证结果（已取消的计划也可以重新开始）
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.updatePlanStatus(planId, PlanStatus.IN_PROGRESS) }
    }
}