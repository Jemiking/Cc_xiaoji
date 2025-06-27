package com.ccxiaoji.feature.plan.util.test

import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.domain.model.Milestone
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import kotlinx.datetime.*
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 性能测试数据生成器
 * 用于生成大量测试数据以进行性能测试
 */
@Singleton
class PerformanceTestDataGenerator @Inject constructor(
    private val planRepository: PlanRepository
) {
    
    private val planTitles = listOf(
        "产品开发计划", "市场推广策略", "技术架构升级", "团队建设方案", "客户服务优化",
        "销售目标达成", "品牌建设项目", "运营效率提升", "成本控制方案", "创新研发项目",
        "人才培养计划", "质量管理体系", "供应链优化", "数字化转型", "企业文化建设"
    )
    
    private val descriptions = listOf(
        "提升整体效率，优化流程", "建立完整体系，确保质量", "创新驱动发展，突破瓶颈",
        "强化团队协作，提高产出", "深化改革创新，激发活力"
    )
    
    private val tags = listOf(
        "重要", "紧急", "创新", "优化", "战略", "核心", "基础", "提升", "改进", "突破"
    )
    
    private val colors = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5",
        "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50",
        "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800"
    )
    
    /**
     * 生成指定数量的测试计划
     * @param totalCount 总计划数量
     * @param maxDepth 最大嵌套深度
     * @param maxChildrenPerPlan 每个计划的最大子计划数
     */
    suspend fun generateTestPlans(
        totalCount: Int = 1000,
        maxDepth: Int = 4,
        maxChildrenPerPlan: Int = 5
    ) {
        println("开始生成 $totalCount 个测试计划...")
        
        val startTime = System.currentTimeMillis()
        var generatedCount = 0
        
        // 生成顶级计划
        val rootPlanCount = totalCount / 10 // 约10%为顶级计划
        
        for (i in 1..rootPlanCount) {
            if (generatedCount >= totalCount) break
            
            val rootPlan = generateRandomPlan(null, 0)
            val rootPlanId = planRepository.createPlan(rootPlan)
            generatedCount++
            
            // 递归生成子计划
            generatedCount += generateChildPlans(
                parentId = rootPlanId,
                currentDepth = 1,
                maxDepth = maxDepth,
                maxChildren = maxChildrenPerPlan,
                remainingCount = totalCount - generatedCount
            )
            
            if (generatedCount % 100 == 0) {
                println("已生成 $generatedCount 个计划...")
            }
        }
        
        val endTime = System.currentTimeMillis()
        val duration = (endTime - startTime) / 1000.0
        
        println("✅ 成功生成 $generatedCount 个测试计划")
        println("⏱️ 耗时: ${duration}秒")
        println("📊 平均速度: ${generatedCount / duration} 个/秒")
    }
    
    /**
     * 递归生成子计划
     */
    private suspend fun generateChildPlans(
        parentId: String,
        currentDepth: Int,
        maxDepth: Int,
        maxChildren: Int,
        remainingCount: Int
    ): Int {
        if (currentDepth >= maxDepth || remainingCount <= 0) {
            return 0
        }
        
        var generated = 0
        val childCount = Random.nextInt(1, minOf(maxChildren + 1, remainingCount + 1))
        
        for (i in 1..childCount) {
            if (generated >= remainingCount) break
            
            val childPlan = generateRandomPlan(parentId, currentDepth)
            val childPlanId = planRepository.createPlan(childPlan)
            generated++
            
            // 有一定概率继续生成子计划
            if (Random.nextFloat() < 0.6f && generated < remainingCount) {
                generated += generateChildPlans(
                    parentId = childPlanId,
                    currentDepth = currentDepth + 1,
                    maxDepth = maxDepth,
                    maxChildren = maxChildren,
                    remainingCount = remainingCount - generated
                )
            }
        }
        
        return generated
    }
    
    /**
     * 生成随机计划
     */
    private fun generateRandomPlan(parentId: String?, depth: Int): Plan {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val startDate = today.plus(DatePeriod(days = Random.nextInt(-30, 30)))
        val duration = Random.nextInt(7, 180) // 7天到180天
        val endDate = startDate.plus(DatePeriod(days = duration))
        
        val status = when (Random.nextInt(0, 4)) {
            0 -> PlanStatus.NOT_STARTED
            1 -> PlanStatus.IN_PROGRESS
            2 -> PlanStatus.COMPLETED
            else -> PlanStatus.CANCELLED
        }
        
        val progress = when (status) {
            PlanStatus.NOT_STARTED -> 0f
            PlanStatus.COMPLETED -> 100f
            PlanStatus.CANCELLED -> Random.nextFloat() * 100
            PlanStatus.IN_PROGRESS -> Random.nextFloat() * 90 + 5 // 5-95%
        }
        
        val title = "${planTitles.random()} - L${depth + 1}"
        
        // 生成里程碑
        val milestones = if (Random.nextFloat() < 0.3f) { // 30%概率有里程碑
            generateRandomMilestones(startDate, endDate)
        } else {
            emptyList()
        }
        
        return Plan(
            id = "", // 将由Repository生成
            parentId = parentId,
            title = title,
            description = descriptions.random(),
            startDate = startDate,
            endDate = endDate,
            status = status,
            progress = progress,
            color = colors.random(),
            priority = Random.nextInt(0, 11),
            tags = tags.shuffled().take(Random.nextInt(0, 4)),
            milestones = milestones,
            orderIndex = Random.nextInt(0, 100)
        )
    }
    
    /**
     * 生成随机里程碑
     */
    private fun generateRandomMilestones(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Milestone> {
        val milestoneCount = Random.nextInt(1, 5)
        val milestones = mutableListOf<Milestone>()
        
        val totalDays = startDate.daysUntil(endDate)
        val interval = totalDays / (milestoneCount + 1)
        
        for (i in 1..milestoneCount) {
            val milestoneDate = startDate.plus(DatePeriod(days = interval * i))
            milestones.add(
                Milestone(
                    id = "", // 将由Repository生成
                    planId = "", // 将在创建时设置
                    title = "里程碑 $i",
                    description = "第 $i 阶段完成",
                    targetDate = milestoneDate,
                    isCompleted = Random.nextFloat() < 0.5f,
                    completedDate = if (Random.nextFloat() < 0.5f) milestoneDate else null
                )
            )
        }
        
        return milestones
    }
    
    /**
     * 清理所有测试数据
     */
    suspend fun clearAllTestData() {
        println("⚠️ 清理所有测试数据...")
        // 这里需要实现批量删除的功能
        // 由于现有的API只支持逐个删除，暂时不实现
        println("❌ 批量删除功能尚未实现，请手动清理数据库")
    }
}

/**
 * LocalDate扩展函数：计算两个日期之间的天数
 */
private fun LocalDate.daysUntil(other: LocalDate): Int {
    return (other.toEpochDays() - this.toEpochDays()).toInt()
}