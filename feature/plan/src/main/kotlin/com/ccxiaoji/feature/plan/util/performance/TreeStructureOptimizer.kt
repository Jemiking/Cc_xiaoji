package com.ccxiaoji.feature.plan.util.performance

import com.ccxiaoji.feature.plan.data.local.entity.PlanEntity
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.util.concurrent.ConcurrentHashMap

/**
 * 树形结构优化器
 * 提供高性能的树形数据处理算法
 */
object TreeStructureOptimizer {
    
    /**
     * 优化的树形结构构建算法
     * 使用HashMap减少查找时间复杂度
     * 
     * @param entities 扁平的实体列表
     * @param milestonesMap 里程碑映射表（可选）
     * @return 构建好的树形结构
     */
    fun buildOptimizedTree(
        entities: List<PlanEntity>,
        milestonesMap: Map<String, List<com.ccxiaoji.feature.plan.data.local.entity.MilestoneEntity>> = emptyMap()
    ): List<Plan> {
        if (entities.isEmpty()) return emptyList()
        
        // 使用HashMap存储所有计划，提高查找效率
        val planMap = ConcurrentHashMap<String, MutablePlan>()
        val rootPlans = mutableListOf<MutablePlan>()
        
        // 第一遍：创建所有计划对象
        entities.forEach { entity ->
            planMap[entity.id] = MutablePlan.fromEntity(entity, milestonesMap[entity.id] ?: emptyList())
        }
        
        // 第二遍：建立父子关系
        entities.forEach { entity ->
            val plan = planMap[entity.id] ?: return@forEach
            
            if (entity.parentId == null) {
                rootPlans.add(plan)
            } else {
                val parent = planMap[entity.parentId]
                parent?.children?.add(plan)
            }
        }
        
        // 转换为不可变的Plan对象
        return rootPlans.map { it.toPlan() }
    }
    
    /**
     * 并行构建树形结构
     * 适用于大数据量场景
     */
    suspend fun buildTreeParallel(entities: List<PlanEntity>): List<Plan> = coroutineScope {
        if (entities.isEmpty()) return@coroutineScope emptyList()
        
        // 按父ID分组
        val groupedByParent = entities.groupBy { it.parentId }
        val rootEntities = groupedByParent[null] ?: emptyList()
        
        // 并行构建每个根节点的子树
        rootEntities.map { rootEntity ->
            async {
                buildSubtree(rootEntity, groupedByParent)
            }
        }.awaitAll()
    }
    
    /**
     * 递归构建子树
     */
    private fun buildSubtree(
        entity: PlanEntity,
        groupedByParent: Map<String?, List<PlanEntity>>
    ): Plan {
        val children = groupedByParent[entity.id]?.map { childEntity ->
            buildSubtree(childEntity, groupedByParent)
        } ?: emptyList()
        
        return entity.toDomainModel(children)
    }
    
    /**
     * 优化的进度计算算法
     * 使用缓存避免重复计算
     */
    fun calculateProgressWithCache(
        plan: Plan,
        cache: MutableMap<String, Float> = mutableMapOf()
    ): Float {
        // 检查缓存
        cache[plan.id]?.let { return it }
        
        val progress = if (plan.children.isEmpty()) {
            plan.progress
        } else {
            plan.children.map { child ->
                calculateProgressWithCache(child, cache)
            }.average().toFloat()
        }
        
        // 存入缓存
        cache[plan.id] = progress
        return progress
    }
    
    /**
     * 批量更新进度
     * 减少数据库访问次数
     */
    fun batchUpdateProgress(
        plans: List<Plan>,
        updates: Map<String, Float>
    ): List<Plan> {
        return plans.map { plan ->
            val newProgress = updates[plan.id]
            if (newProgress != null) {
                plan.copy(
                    progress = newProgress,
                    children = batchUpdateProgress(plan.children, updates)
                )
            } else {
                plan.copy(
                    children = batchUpdateProgress(plan.children, updates)
                )
            }
        }
    }
    
    /**
     * 计算树的深度
     * 用于性能分析
     */
    fun calculateTreeDepth(plans: List<Plan>): Int {
        if (plans.isEmpty()) return 0
        
        return plans.maxOf { plan ->
            1 + calculateTreeDepth(plan.children)
        }
    }
    
    /**
     * 计算树中的节点总数
     */
    fun countNodes(plans: List<Plan>): Int {
        return plans.sumOf { plan ->
            1 + countNodes(plan.children)
        }
    }
    
    /**
     * 扁平化树结构
     * 使用尾递归优化
     */
    tailrec fun flattenTree(
        plans: List<Plan>,
        accumulator: MutableList<Plan> = mutableListOf()
    ): List<Plan> {
        if (plans.isEmpty()) return accumulator
        
        val remaining = mutableListOf<Plan>()
        plans.forEach { plan ->
            accumulator.add(plan)
            remaining.addAll(plan.children)
        }
        
        return flattenTree(remaining, accumulator)
    }
}

/**
 * 可变的计划类，用于构建树形结构
 */
private data class MutablePlan(
    val id: String,
    val parentId: String?,
    val entity: PlanEntity,
    val milestones: List<com.ccxiaoji.feature.plan.data.local.entity.MilestoneEntity> = emptyList(),
    val children: MutableList<MutablePlan> = mutableListOf()
) {
    fun toPlan(): Plan {
        return entity.toDomainModel(children.map { it.toPlan() }, milestones)
    }
    
    companion object {
        fun fromEntity(
            entity: PlanEntity,
            milestones: List<com.ccxiaoji.feature.plan.data.local.entity.MilestoneEntity> = emptyList()
        ): MutablePlan {
            return MutablePlan(
                id = entity.id,
                parentId = entity.parentId,
                entity = entity,
                milestones = milestones
            )
        }
    }
}

/**
 * PlanEntity扩展函数：转换为领域模型
 */
private fun PlanEntity.toDomainModel(
    children: List<Plan> = emptyList(),
    milestoneEntities: List<com.ccxiaoji.feature.plan.data.local.entity.MilestoneEntity> = emptyList()
): Plan {
    val startDateTime = Instant.fromEpochMilliseconds(startDate)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val endDateTime = Instant.fromEpochMilliseconds(endDate)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    
    // 转换里程碑
    val milestones = milestoneEntities.map { entity ->
        com.ccxiaoji.feature.plan.domain.model.Milestone(
            id = entity.id,
            planId = entity.planId,
            title = entity.title,
            description = entity.description,
            targetDate = Instant.fromEpochMilliseconds(entity.targetDate)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date,
            isCompleted = entity.isCompleted,
            completedDate = entity.completedDate?.let { 
                Instant.fromEpochMilliseconds(it)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
            },
            orderIndex = entity.orderIndex
        )
    }
    
    return Plan(
        id = id,
        parentId = parentId,
        title = title,
        description = description,
        startDate = startDateTime.date,
        endDate = endDateTime.date,
        status = PlanStatus.valueOf(status),
        progress = progress,
        color = color,
        priority = priority,
        tags = try {
            Json.Default.decodeFromString<List<String>>(tags)
        } catch (e: Exception) {
            emptyList()
        },
        children = children,
        milestones = milestones,
        reminderSettings = null, // 提醒设置需要单独处理
        orderIndex = orderIndex,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}