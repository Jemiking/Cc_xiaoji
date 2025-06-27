package com.ccxiaoji.feature.plan.domain.usecase.plan

import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test

/**
 * CreatePlanUseCase 单元测试
 * 测试创建计划用例的各种场景
 */
class CreatePlanUseCaseTest {

    @MockK
    private lateinit var planRepository: PlanRepository
    
    private lateinit var createPlanUseCase: CreatePlanUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        createPlanUseCase = CreatePlanUseCase(planRepository)
    }

    @Test
    fun `成功创建计划 - 输入验证正确`() = runTest {
        // Given - 准备测试数据
        val testPlan = Plan(
            id = "test-plan-id",
            title = "学习Kotlin",
            description = "深入学习Kotlin编程语言",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            status = PlanStatus.NOT_STARTED,
            progress = 0f,
            priority = 5
        )
        val expectedPlanId = "generated-plan-id"
        
        coEvery { planRepository.createPlan(testPlan) } returns expectedPlanId

        // When - 执行测试
        val result = createPlanUseCase(testPlan)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedPlanId)
        coVerify(exactly = 1) { planRepository.createPlan(testPlan) }
    }

    @Test
    fun `创建计划失败 - 标题为空白`() = runTest {
        // Given - 标题为空白的计划
        val invalidPlan = Plan(
            id = "test-plan-id",
            title = "   ", // 空白标题
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31)
        )

        // When - 执行测试
        val result = createPlanUseCase(invalidPlan)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("计划标题不能为空")
        coVerify(exactly = 0) { planRepository.createPlan(any()) }
    }

    @Test
    fun `创建计划失败 - 开始日期晚于结束日期`() = runTest {
        // Given - 日期顺序错误的计划
        val invalidPlan = Plan(
            id = "test-plan-id",
            title = "测试计划",
            description = "描述",
            startDate = LocalDate(2024, 12, 31), // 开始日期晚于结束日期
            endDate = LocalDate(2024, 1, 1)
        )

        // When - 执行测试
        val result = createPlanUseCase(invalidPlan)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("开始日期不能晚于结束日期")
        coVerify(exactly = 0) { planRepository.createPlan(any()) }
    }

    @Test
    fun `创建计划失败 - 进度值超出范围`() = runTest {
        // Given - 进度值无效的计划
        val invalidPlan = Plan(
            id = "test-plan-id",
            title = "测试计划",
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 150f // 超出范围的进度值
        )

        // When - 执行测试
        val result = createPlanUseCase(invalidPlan)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("进度必须在0-100之间")
        coVerify(exactly = 0) { planRepository.createPlan(any()) }
    }

    @Test
    fun `创建计划失败 - 进度值为负数`() = runTest {
        // Given - 负数进度值的计划
        val invalidPlan = Plan(
            id = "test-plan-id",
            title = "测试计划",
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = -10f // 负数进度值
        )

        // When - 执行测试
        val result = createPlanUseCase(invalidPlan)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("进度必须在0-100之间")
        coVerify(exactly = 0) { planRepository.createPlan(any()) }
    }

    @Test
    fun `创建计划失败 - Repository抛出异常`() = runTest {
        // Given - Repository会抛出异常
        val testPlan = Plan(
            id = "test-plan-id",
            title = "测试计划",
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31)
        )
        val exception = RuntimeException("数据库连接失败")
        
        coEvery { planRepository.createPlan(testPlan) } throws exception

        // When - 执行测试
        val result = createPlanUseCase(testPlan)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        coVerify(exactly = 1) { planRepository.createPlan(testPlan) }
    }

    @Test
    fun `边界条件测试 - 进度为0和100`() = runTest {
        // Given - 进度为边界值的计划们
        val planWith0Progress = Plan(
            id = "test-plan-id-1",
            title = "进度0的计划",
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 0f
        )
        
        val planWith100Progress = Plan(
            id = "test-plan-id-2",
            title = "进度100的计划",
            description = "描述",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            progress = 100f
        )
        
        coEvery { planRepository.createPlan(planWith0Progress) } returns "id-1"
        coEvery { planRepository.createPlan(planWith100Progress) } returns "id-2"

        // When - 执行测试
        val result1 = createPlanUseCase(planWith0Progress)
        val result2 = createPlanUseCase(planWith100Progress)

        // Then - 验证结果
        assertThat(result1.isSuccess).isTrue()
        assertThat(result2.isSuccess).isTrue()
        assertThat(result1.getOrNull()).isEqualTo("id-1")
        assertThat(result2.getOrNull()).isEqualTo("id-2")
        coVerify(exactly = 1) { planRepository.createPlan(planWith0Progress) }
        coVerify(exactly = 1) { planRepository.createPlan(planWith100Progress) }
    }

    @Test
    fun `边界条件测试 - 开始日期等于结束日期`() = runTest {
        // Given - 开始日期等于结束日期的计划
        val sameDate = LocalDate(2024, 6, 15)
        val testPlan = Plan(
            id = "test-plan-id",
            title = "单日计划",
            description = "一天完成的计划",
            startDate = sameDate,
            endDate = sameDate
        )
        
        coEvery { planRepository.createPlan(testPlan) } returns "same-date-plan-id"

        // When - 执行测试
        val result = createPlanUseCase(testPlan)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo("same-date-plan-id")
        coVerify(exactly = 1) { planRepository.createPlan(testPlan) }
    }
}