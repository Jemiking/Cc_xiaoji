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
 * UpdatePlanUseCase 单元测试
 * 测试更新计划用例的各种场景
 */
class UpdatePlanUseCaseTest {

    @MockK
    private lateinit var planRepository: PlanRepository
    
    private lateinit var updatePlanUseCase: UpdatePlanUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        updatePlanUseCase = UpdatePlanUseCase(planRepository)
    }

    @Test
    fun `成功更新计划 - 所有字段验证正确`() = runTest {
        // Given - 准备有效的计划数据
        val testPlan = Plan(
            id = "existing-plan-id",
            title = "更新后的计划标题",
            description = "更新后的描述",
            startDate = LocalDate(2024, 2, 1),
            endDate = LocalDate(2024, 11, 30),
            status = PlanStatus.IN_PROGRESS,
            progress = 45f,
            priority = 8
        )
        
        coJustRun { planRepository.updatePlan(testPlan) }

        // When - 执行更新操作
        val result = updatePlanUseCase(testPlan)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(Unit)
        coVerify(exactly = 1) { planRepository.updatePlan(testPlan) }
    }

    @Test
    fun `更新计划失败 - 计划ID为空白`() = runTest {
        // Given - ID为空白的计划
        val invalidPlan = Plan(
            id = "   ", // 空白ID
            title = "有效标题",
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31)
        )

        // When - 执行更新操作
        val result = updatePlanUseCase(invalidPlan)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("计划ID不能为空")
        coVerify(exactly = 0) { planRepository.updatePlan(any()) }
    }

    @Test
    fun `更新计划失败 - 标题为空`() = runTest {
        // Given - 标题为空的计划
        val invalidPlan = Plan(
            id = "valid-id",
            title = "", // 空标题
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31)
        )

        // When - 执行更新操作
        val result = updatePlanUseCase(invalidPlan)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("计划标题不能为空")
        coVerify(exactly = 0) { planRepository.updatePlan(any()) }
    }

    @Test
    fun `更新计划失败 - 开始日期晚于结束日期`() = runTest {
        // Given - 日期顺序错误的计划
        val invalidPlan = Plan(
            id = "valid-id",
            title = "有效标题",
            description = "描述",
            startDate = LocalDate(2024, 6, 15), // 开始日期晚于结束日期
            endDate = LocalDate(2024, 3, 10)
        )

        // When - 执行更新操作
        val result = updatePlanUseCase(invalidPlan)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("开始日期不能晚于结束日期")
        coVerify(exactly = 0) { planRepository.updatePlan(any()) }
    }

    @Test
    fun `更新计划失败 - 进度值大于100`() = runTest {
        // Given - 进度值超出范围的计划
        val invalidPlan = Plan(
            id = "valid-id",
            title = "有效标题",
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 120f // 超出范围
        )

        // When - 执行更新操作
        val result = updatePlanUseCase(invalidPlan)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("进度必须在0-100之间")
        coVerify(exactly = 0) { planRepository.updatePlan(any()) }
    }

    @Test
    fun `更新计划失败 - 进度值小于0`() = runTest {
        // Given - 进度值为负数的计划
        val invalidPlan = Plan(
            id = "valid-id",
            title = "有效标题",
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = -5f // 负数
        )

        // When - 执行更新操作
        val result = updatePlanUseCase(invalidPlan)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("进度必须在0-100之间")
        coVerify(exactly = 0) { planRepository.updatePlan(any()) }
    }

    @Test
    fun `更新计划失败 - Repository抛出异常`() = runTest {
        // Given - Repository会抛出异常
        val testPlan = Plan(
            id = "valid-id",
            title = "有效标题",
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31)
        )
        val exception = RuntimeException("数据库更新失败")
        
        coEvery { planRepository.updatePlan(testPlan) } throws exception

        // When - 执行更新操作
        val result = updatePlanUseCase(testPlan)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        coVerify(exactly = 1) { planRepository.updatePlan(testPlan) }
    }

    @Test
    fun `边界条件测试 - 进度值为0和100`() = runTest {
        // Given - 进度为边界值的计划们
        val planWith0Progress = Plan(
            id = "plan-0",
            title = "进度0的计划",
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 0f
        )
        
        val planWith100Progress = Plan(
            id = "plan-100",
            title = "进度100的计划",
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 100f
        )
        
        coJustRun { planRepository.updatePlan(planWith0Progress) }
        coJustRun { planRepository.updatePlan(planWith100Progress) }

        // When - 执行更新操作
        val result1 = updatePlanUseCase(planWith0Progress)
        val result2 = updatePlanUseCase(planWith100Progress)

        // Then - 验证结果
        assertThat(result1.isSuccess).isTrue()
        assertThat(result2.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.updatePlan(planWith0Progress) }
        coVerify(exactly = 1) { planRepository.updatePlan(planWith100Progress) }
    }

    @Test
    fun `边界条件测试 - 开始日期等于结束日期`() = runTest {
        // Given - 开始日期等于结束日期的计划
        val sameDate = LocalDate(2024, 8, 20)
        val testPlan = Plan(
            id = "same-date-plan",
            title = "单日计划更新",
            description = "更新的单日计划",
            startDate = sameDate,
            endDate = sameDate
        )
        
        coJustRun { planRepository.updatePlan(testPlan) }

        // When - 执行更新操作
        val result = updatePlanUseCase(testPlan)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.updatePlan(testPlan) }
    }

    @Test
    fun `边界条件测试 - 最小ID长度`() = runTest {
        // Given - 最小有效ID长度的计划
        val testPlan = Plan(
            id = "a", // 最小有效ID
            title = "有效标题",
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31)
        )
        
        coJustRun { planRepository.updatePlan(testPlan) }

        // When - 执行更新操作
        val result = updatePlanUseCase(testPlan)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.updatePlan(testPlan) }
    }

    @Test
    fun `成功更新不同状态的计划`() = runTest {
        // Given - 不同状态的计划
        val completedPlan = Plan(
            id = "completed-plan",
            title = "已完成计划",
            status = PlanStatus.COMPLETED,
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 100f
        )
        
        val cancelledPlan = Plan(
            id = "cancelled-plan",
            title = "已取消计划",
            status = PlanStatus.CANCELLED,
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 30f
        )
        
        coJustRun { planRepository.updatePlan(completedPlan) }
        coJustRun { planRepository.updatePlan(cancelledPlan) }

        // When - 执行更新操作
        val result1 = updatePlanUseCase(completedPlan)
        val result2 = updatePlanUseCase(cancelledPlan)

        // Then - 验证结果
        assertThat(result1.isSuccess).isTrue()
        assertThat(result2.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.updatePlan(completedPlan) }
        coVerify(exactly = 1) { planRepository.updatePlan(cancelledPlan) }
    }
}