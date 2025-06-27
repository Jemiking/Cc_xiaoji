package com.ccxiaoji.feature.plan.domain.usecase.plan

import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test

/**
 * GetAllPlansUseCase 单元测试
 * 测试获取所有计划用例的各种场景，包括树形结构处理
 */
class GetAllPlansUseCaseTest {

    @MockK
    private lateinit var planRepository: PlanRepository
    
    private lateinit var getAllPlansUseCase: GetAllPlansUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        getAllPlansUseCase = GetAllPlansUseCase(planRepository)
    }

    @Test
    fun `成功获取所有计划 - 返回空列表`() = runTest {
        // Given - Repository返回空列表
        val emptyPlanFlow = flowOf(emptyList<Plan>())
        every { planRepository.getAllPlansTree() } returns emptyPlanFlow

        // When - 执行获取操作
        val resultFlow = getAllPlansUseCase()
        val result = resultFlow.first()

        // Then - 验证结果
        assertThat(result).isEmpty()
        verify(exactly = 1) { planRepository.getAllPlansTree() }
    }

    @Test
    fun `成功获取所有计划 - 返回单个顶级计划`() = runTest {
        // Given - Repository返回单个计划
        val singlePlan = Plan(
            id = "single-plan",
            title = "单个计划",
            description = "唯一的计划",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31),
            status = PlanStatus.NOT_STARTED
        )
        val singlePlanFlow = flowOf(listOf(singlePlan))
        every { planRepository.getAllPlansTree() } returns singlePlanFlow

        // When - 执行获取操作
        val resultFlow = getAllPlansUseCase()
        val result = resultFlow.first()

        // Then - 验证结果
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(singlePlan)
        verify(exactly = 1) { planRepository.getAllPlansTree() }
    }

    @Test
    fun `成功获取所有计划 - 返回多个顶级计划`() = runTest {
        // Given - Repository返回多个顶级计划
        val plan1 = Plan(
            id = "plan-1",
            title = "计划1",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 6, 30)
        )
        val plan2 = Plan(
            id = "plan-2",
            title = "计划2",
            startDate = LocalDate(2024, 7, 1),
            endDate = LocalDate(2024, 12, 31)
        )
        val multiplePlansFlow = flowOf(listOf(plan1, plan2))
        every { planRepository.getAllPlansTree() } returns multiplePlansFlow

        // When - 执行获取操作
        val resultFlow = getAllPlansUseCase()
        val result = resultFlow.first()

        // Then - 验证结果
        assertThat(result).hasSize(2)
        assertThat(result).containsExactly(plan1, plan2)
        verify(exactly = 1) { planRepository.getAllPlansTree() }
    }

    @Test
    fun `成功获取所有计划 - 返回树形结构计划`() = runTest {
        // Given - Repository返回树形结构的计划
        val childPlan1 = Plan(
            id = "child-1",
            parentId = "parent-plan",
            title = "子计划1",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 3, 31)
        )
        val childPlan2 = Plan(
            id = "child-2",
            parentId = "parent-plan",
            title = "子计划2",
            startDate = LocalDate(2024, 4, 1),
            endDate = LocalDate(2024, 6, 30)
        )
        val parentPlan = Plan(
            id = "parent-plan",
            title = "父计划",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 6, 30),
            children = listOf(childPlan1, childPlan2)
        )
        val treePlansFlow = flowOf(listOf(parentPlan))
        every { planRepository.getAllPlansTree() } returns treePlansFlow

        // When - 执行获取操作
        val resultFlow = getAllPlansUseCase()
        val result = resultFlow.first()

        // Then - 验证结果
        assertThat(result).hasSize(1)
        val returnedParent = result[0]
        assertThat(returnedParent.id).isEqualTo("parent-plan")
        assertThat(returnedParent.children).hasSize(2)
        assertThat(returnedParent.children.map { it.id }).containsExactly("child-1", "child-2")
        verify(exactly = 1) { planRepository.getAllPlansTree() }
    }

    @Test
    fun `成功获取所有计划 - 多层级树形结构`() = runTest {
        // Given - Repository返回多层级树形结构
        val grandchildPlan = Plan(
            id = "grandchild",
            parentId = "child-1",
            title = "孙子计划",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 1, 31)
        )
        val childPlan1 = Plan(
            id = "child-1",
            parentId = "parent-plan",
            title = "子计划1",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 3, 31),
            children = listOf(grandchildPlan)
        )
        val childPlan2 = Plan(
            id = "child-2",
            parentId = "parent-plan",
            title = "子计划2",
            startDate = LocalDate(2024, 4, 1),
            endDate = LocalDate(2024, 6, 30)
        )
        val parentPlan = Plan(
            id = "parent-plan",
            title = "父计划",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 6, 30),
            children = listOf(childPlan1, childPlan2)
        )
        val multiLevelTreeFlow = flowOf(listOf(parentPlan))
        every { planRepository.getAllPlansTree() } returns multiLevelTreeFlow

        // When - 执行获取操作
        val resultFlow = getAllPlansUseCase()
        val result = resultFlow.first()

        // Then - 验证结果
        assertThat(result).hasSize(1)
        val returnedParent = result[0]
        assertThat(returnedParent.children).hasSize(2)
        val firstChild = returnedParent.children.find { it.id == "child-1" }
        assertThat(firstChild).isNotNull()
        assertThat(firstChild!!.children).hasSize(1)
        assertThat(firstChild.children[0].id).isEqualTo("grandchild")
        verify(exactly = 1) { planRepository.getAllPlansTree() }
    }

    @Test
    fun `成功获取所有计划 - 包含不同状态的计划`() = runTest {
        // Given - Repository返回不同状态的计划
        val notStartedPlan = Plan(
            id = "not-started",
            title = "未开始计划",
            status = PlanStatus.NOT_STARTED,
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 3, 31)
        )
        val inProgressPlan = Plan(
            id = "in-progress",
            title = "进行中计划",
            status = PlanStatus.IN_PROGRESS,
            progress = 50f,
            startDate = LocalDate(2024, 2, 1),
            endDate = LocalDate(2024, 4, 30)
        )
        val completedPlan = Plan(
            id = "completed",
            title = "已完成计划",
            status = PlanStatus.COMPLETED,
            progress = 100f,
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 2, 28)
        )
        val mixedStatusFlow = flowOf(listOf(notStartedPlan, inProgressPlan, completedPlan))
        every { planRepository.getAllPlansTree() } returns mixedStatusFlow

        // When - 执行获取操作
        val resultFlow = getAllPlansUseCase()
        val result = resultFlow.first()

        // Then - 验证结果
        assertThat(result).hasSize(3)
        val statusList = result.map { it.status }
        assertThat(statusList).containsExactly(
            PlanStatus.NOT_STARTED,
            PlanStatus.IN_PROGRESS,
            PlanStatus.COMPLETED
        )
        verify(exactly = 1) { planRepository.getAllPlansTree() }
    }

    @Test
    fun `Flow特性测试 - 数据变化时自动更新`() = runTest {
        // Given - Repository返回会变化的Flow
        val initialPlans = listOf(
            Plan(
                id = "plan-1",
                title = "初始计划",
                startDate = LocalDate(2024, 1, 1),
                endDate = LocalDate(2024, 12, 31)
            )
        )
        val updatedPlans = listOf(
            Plan(
                id = "plan-1",
                title = "更新后的计划",
                startDate = LocalDate(2024, 1, 1),
                endDate = LocalDate(2024, 12, 31),
                progress = 75f
            ),
            Plan(
                id = "plan-2",
                title = "新增计划",
                startDate = LocalDate(2024, 6, 1),
                endDate = LocalDate(2024, 12, 31)
            )
        )
        
        // 创建一个会发生变化的Flow
        val changingFlow = flowOf(initialPlans, updatedPlans)
        every { planRepository.getAllPlansTree() } returns changingFlow

        // When - 执行获取操作
        val resultFlow = getAllPlansUseCase()

        // Then - 验证Flow会返回变化的数据（这里只验证调用）
        verify(exactly = 1) { planRepository.getAllPlansTree() }
        // 注意：实际的Flow变化测试需要在集成测试中进行
    }

    @Test
    fun `返回类型验证 - 确认返回Flow类型`() = runTest {
        // Given - Repository返回Flow
        val testPlansFlow = flowOf(emptyList<Plan>())
        every { planRepository.getAllPlansTree() } returns testPlansFlow

        // When - 执行获取操作
        val resultFlow = getAllPlansUseCase()

        // Then - 验证返回类型
        assertThat(resultFlow).isInstanceOf(Flow::class.java)
        verify(exactly = 1) { planRepository.getAllPlansTree() }
    }

    @Test
    fun `边界条件测试 - 大量计划数据`() = runTest {
        // Given - Repository返回大量计划数据
        val largePlanList = (1..1000).map { index ->
            Plan(
                id = "plan-$index",
                title = "计划$index",
                startDate = LocalDate(2024, 1, 1),
                endDate = LocalDate(2024, 12, 31)
            )
        }
        val largePlansFlow = flowOf(largePlanList)
        every { planRepository.getAllPlansTree() } returns largePlansFlow

        // When - 执行获取操作
        val resultFlow = getAllPlansUseCase()
        val result = resultFlow.first()

        // Then - 验证结果
        assertThat(result).hasSize(1000)
        assertThat(result.first().id).isEqualTo("plan-1")
        assertThat(result.last().id).isEqualTo("plan-1000")
        verify(exactly = 1) { planRepository.getAllPlansTree() }
    }

    @Test
    fun `UseCase简单性验证 - 不做任何数据处理`() = runTest {
        // Given - Repository返回原始数据
        val originalPlan = Plan(
            id = "original",
            title = "原始计划",
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 12, 31)
        )
        val originalFlow = flowOf(listOf(originalPlan))
        every { planRepository.getAllPlansTree() } returns originalFlow

        // When - 执行获取操作
        val resultFlow = getAllPlansUseCase()
        val result = resultFlow.first()

        // Then - 验证UseCase直接返回Repository的数据，不做任何修改
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(originalPlan)
        // 验证UseCase确实是简单的代理，直接返回Repository的Flow
        verify(exactly = 1) { planRepository.getAllPlansTree() }
    }
}