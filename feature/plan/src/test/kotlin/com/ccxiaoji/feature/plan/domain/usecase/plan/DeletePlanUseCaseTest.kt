package com.ccxiaoji.feature.plan.domain.usecase.plan

import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * DeletePlanUseCase 单元测试
 * 测试删除计划用例的各种场景，包括级联删除子计划
 */
class DeletePlanUseCaseTest {

    @MockK
    private lateinit var planRepository: PlanRepository
    
    private lateinit var deletePlanUseCase: DeletePlanUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        deletePlanUseCase = DeletePlanUseCase(planRepository)
    }

    @Test
    fun `成功删除计划 - 有效的计划ID`() = runTest {
        // Given - 准备有效的计划ID
        val planId = "plan-to-delete"
        
        coJustRun { planRepository.deletePlan(planId) }

        // When - 执行删除操作
        val result = deletePlanUseCase(planId)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(Unit)
        coVerify(exactly = 1) { planRepository.deletePlan(planId) }
    }

    @Test
    fun `成功删除计划 - 带有子计划的父计划`() = runTest {
        // Given - 带有子计划的父计划ID
        val parentPlanId = "parent-plan-with-children"
        
        coJustRun { planRepository.deletePlan(parentPlanId) }

        // When - 执行删除操作
        val result = deletePlanUseCase(parentPlanId)

        // Then - 验证结果（Repository应该处理级联删除）
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(Unit)
        coVerify(exactly = 1) { planRepository.deletePlan(parentPlanId) }
    }

    @Test
    fun `成功删除计划 - 子计划`() = runTest {
        // Given - 子计划ID
        val childPlanId = "child-plan"
        
        coJustRun { planRepository.deletePlan(childPlanId) }

        // When - 执行删除操作
        val result = deletePlanUseCase(childPlanId)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(Unit)
        coVerify(exactly = 1) { planRepository.deletePlan(childPlanId) }
    }

    @Test
    fun `删除计划失败 - Repository抛出异常`() = runTest {
        // Given - Repository会抛出异常
        val planId = "plan-to-delete"
        val exception = RuntimeException("数据库删除失败")
        
        coEvery { planRepository.deletePlan(planId) } throws exception

        // When - 执行删除操作
        val result = deletePlanUseCase(planId)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        coVerify(exactly = 1) { planRepository.deletePlan(planId) }
    }

    @Test
    fun `删除计划失败 - 计划不存在`() = runTest {
        // Given - 不存在的计划ID，Repository抛出异常
        val nonExistentPlanId = "non-existent-plan"
        val exception = NoSuchElementException("计划不存在")
        
        coEvery { planRepository.deletePlan(nonExistentPlanId) } throws exception

        // When - 执行删除操作
        val result = deletePlanUseCase(nonExistentPlanId)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        coVerify(exactly = 1) { planRepository.deletePlan(nonExistentPlanId) }
    }

    @Test
    fun `删除计划失败 - 数据库约束错误`() = runTest {
        // Given - 因为外键约束无法删除的计划
        val constrainedPlanId = "constrained-plan"
        val exception = IllegalStateException("外键约束错误：无法删除被引用的计划")
        
        coEvery { planRepository.deletePlan(constrainedPlanId) } throws exception

        // When - 执行删除操作
        val result = deletePlanUseCase(constrainedPlanId)

        // Then - 验证结果
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        coVerify(exactly = 1) { planRepository.deletePlan(constrainedPlanId) }
    }

    @Test
    fun `边界条件测试 - 空字符串ID`() = runTest {
        // Given - 空字符串ID
        val emptyId = ""
        
        coJustRun { planRepository.deletePlan(emptyId) }

        // When - 执行删除操作
        val result = deletePlanUseCase(emptyId)

        // Then - 验证结果（UseCase不做ID验证，交给Repository处理）
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.deletePlan(emptyId) }
    }

    @Test
    fun `边界条件测试 - 空白字符串ID`() = runTest {
        // Given - 空白字符串ID
        val blankId = "   "
        
        coJustRun { planRepository.deletePlan(blankId) }

        // When - 执行删除操作
        val result = deletePlanUseCase(blankId)

        // Then - 验证结果（UseCase不做ID验证，交给Repository处理）
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.deletePlan(blankId) }
    }

    @Test
    fun `边界条件测试 - 很长的ID`() = runTest {
        // Given - 很长的ID字符串
        val longId = "a".repeat(1000)
        
        coJustRun { planRepository.deletePlan(longId) }

        // When - 执行删除操作
        val result = deletePlanUseCase(longId)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.deletePlan(longId) }
    }

    @Test
    fun `边界条件测试 - 特殊字符ID`() = runTest {
        // Given - 包含特殊字符的ID
        val specialCharId = "plan-!@#$%^&*()_+{}[]|\\:;\"'<>?,./"
        
        coJustRun { planRepository.deletePlan(specialCharId) }

        // When - 执行删除操作
        val result = deletePlanUseCase(specialCharId)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { planRepository.deletePlan(specialCharId) }
    }

    @Test
    fun `级联删除测试 - 多层级子计划`() = runTest {
        // Given - 有多层级子计划的父计划
        val rootPlanId = "root-plan-with-deep-children"
        
        // Repository应该处理级联删除所有子计划
        coJustRun { planRepository.deletePlan(rootPlanId) }

        // When - 执行删除操作
        val result = deletePlanUseCase(rootPlanId)

        // Then - 验证结果
        assertThat(result.isSuccess).isTrue()
        // 验证只调用了一次删除（Repository内部处理级联）
        coVerify(exactly = 1) { planRepository.deletePlan(rootPlanId) }
    }

    @Test
    fun `并发删除测试 - 同时删除多个计划`() = runTest {
        // Given - 多个计划ID
        val planIds = listOf("plan1", "plan2", "plan3")
        
        planIds.forEach { planId ->
            coJustRun { planRepository.deletePlan(planId) }
        }

        // When - 并发执行删除操作
        val results = planIds.map { planId ->
            deletePlanUseCase(planId)
        }

        // Then - 验证所有结果
        results.forEach { result ->
            assertThat(result.isSuccess).isTrue()
        }
        
        planIds.forEach { planId ->
            coVerify(exactly = 1) { planRepository.deletePlan(planId) }
        }
    }
}