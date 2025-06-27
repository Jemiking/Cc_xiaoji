package com.ccxiaoji.feature.plan.domain.usecase.performance

import com.ccxiaoji.feature.plan.util.test.PerformanceTestDataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 运行性能测试用例
 * 生成测试数据并进行性能分析
 */
class RunPerformanceTestUseCase @Inject constructor(
    private val testDataGenerator: PerformanceTestDataGenerator
) {
    
    /**
     * 执行性能测试
     * @param planCount 要生成的计划数量
     * @return 性能测试结果
     */
    suspend operator fun invoke(planCount: Int = 1000): PerformanceTestResult {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            
            try {
                // 生成测试数据
                testDataGenerator.generateTestPlans(
                    totalCount = planCount,
                    maxDepth = 4,
                    maxChildrenPerPlan = 5
                )
                
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                
                PerformanceTestResult(
                    success = true,
                    planCount = planCount,
                    duration = duration,
                    averageSpeed = planCount.toDouble() / (duration / 1000.0),
                    message = "性能测试完成"
                )
            } catch (e: Exception) {
                PerformanceTestResult(
                    success = false,
                    planCount = 0,
                    duration = 0,
                    averageSpeed = 0.0,
                    message = "性能测试失败: ${e.message}"
                )
            }
        }
    }
}

/**
 * 性能测试结果
 */
data class PerformanceTestResult(
    val success: Boolean,
    val planCount: Int,
    val duration: Long, // 毫秒
    val averageSpeed: Double, // 每秒生成数量
    val message: String
)