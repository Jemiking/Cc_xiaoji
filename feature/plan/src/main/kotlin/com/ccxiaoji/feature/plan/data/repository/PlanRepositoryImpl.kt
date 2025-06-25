package com.ccxiaoji.feature.plan.data.repository

import com.ccxiaoji.feature.plan.data.local.dao.MilestoneDao
import com.ccxiaoji.feature.plan.data.local.dao.PlanDao
import com.ccxiaoji.feature.plan.data.local.entity.PlanEntity
import com.ccxiaoji.feature.plan.domain.model.Milestone
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.domain.model.ReminderSettings
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import com.ccxiaoji.feature.plan.util.core.safeDbCall
import com.ccxiaoji.feature.plan.util.performance.TreeStructureOptimizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

/**
 * 计划仓库实现类
 */
class PlanRepositoryImpl @Inject constructor(
    private val planDao: PlanDao,
    private val milestoneDao: MilestoneDao
) : PlanRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    override fun getAllPlansTree(): Flow<List<Plan>> {
        return planDao.getAllPlans().map { entities ->
            buildPlanTree(entities)
        }
    }
    
    override fun getRootPlans(): Flow<List<Plan>> {
        return planDao.getRootPlans().map { entities ->
            entities.map { entity ->
                entityToPlan(entity, emptyList())
            }
        }
    }
    
    override suspend fun getPlanById(planId: String): Plan? {
        val entity = planDao.getPlanById(planId) ?: return null
        val children = getChildPlans(planId)
        val milestones = milestoneDao.getMilestonesByPlanIdOnce(planId)
        return entityToPlan(entity, children, milestones)
    }
    
    override suspend fun getChildPlans(parentId: String): List<Plan> {
        val childEntities = planDao.getChildPlans(parentId)
        return childEntities.map { entity ->
            val children = getChildPlans(entity.id)
            val milestones = milestoneDao.getMilestonesByPlanIdOnce(entity.id)
            entityToPlan(entity, children, milestones)
        }
    }
    
    override fun getPlansByStatus(status: PlanStatus): Flow<List<Plan>> {
        return planDao.getPlansByStatus(status.name).map { entities ->
            entities.map { entity ->
                entityToPlan(entity, emptyList())
            }
        }
    }
    
    override fun searchPlans(query: String): Flow<List<Plan>> {
        return planDao.searchPlans(query).map { entities ->
            entities.map { entity ->
                entityToPlan(entity, emptyList())
            }
        }
    }
    
    override suspend fun createPlan(plan: Plan): String {
        val planId = plan.id.ifEmpty { UUID.randomUUID().toString() }
        val entity = planToEntity(plan.copy(id = planId))
        planDao.insertPlan(entity)
        
        // 插入里程碑
        if (plan.milestones.isNotEmpty()) {
            val milestoneEntities = plan.milestones.map { milestone ->
                milestoneToEntity(milestone.copy(planId = planId))
            }
            milestoneDao.insertMilestones(milestoneEntities)
        }
        
        return planId
    }
    
    override suspend fun updatePlan(plan: Plan) {
        val entity = planToEntity(plan)
        planDao.updatePlan(entity)
    }
    
    override suspend fun updatePlanProgress(planId: String, progress: Float) {
        planDao.updatePlanProgress(planId, progress.coerceIn(0f, 100f))
    }
    
    override suspend fun updatePlanStatus(planId: String, status: PlanStatus) {
        planDao.updatePlanStatus(planId, status.name)
    }
    
    override suspend fun deletePlan(planId: String) {
        planDao.deletePlanById(planId)
    }
    
    override suspend fun deletePlans(planIds: List<String>) {
        planDao.deletePlansByIds(planIds)
    }
    
    override suspend fun movePlan(planId: String, newParentId: String?) {
        planDao.updatePlanParent(planId, newParentId)
    }
    
    override suspend fun reorderPlans(planIds: List<String>) {
        val planOrders = planIds.mapIndexed { index, planId ->
            planId to index
        }
        planDao.updatePlansOrder(planOrders)
    }
    
    override suspend fun deleteAllPlans() {
        safeDbCall {
            // 先删除所有里程碑
            milestoneDao.deleteAllMilestones()
            // 再删除所有计划
            planDao.deleteAllPlans()
        }
    }
    
    /**
     * 构建计划树形结构 - 优化版本
     * 使用TreeStructureOptimizer提高性能
     */
    private suspend fun buildPlanTree(entities: List<PlanEntity>): List<Plan> {
        if (entities.isEmpty()) return emptyList()
        
        // 批量获取所有里程碑，减少数据库查询
        val allPlanIds = entities.map { it.id }
        val allMilestones = milestoneDao.getMilestonesByPlanIds(allPlanIds)
        val milestonesMap = allMilestones.groupBy { it.planId }
        
        // 使用优化的树形构建算法
        return TreeStructureOptimizer.buildOptimizedTree(entities, milestonesMap)
    }
    
    /**
     * 构建计划树形结构 - 旧版本（保留以便回滚）
     */
    @Deprecated("使用buildPlanTree替代，性能更好")
    private suspend fun buildPlanTreeOld(entities: List<PlanEntity>): List<Plan> {
        val planMap = mutableMapOf<String, Plan>()
        val rootPlans = mutableListOf<Plan>()
        
        // 第一遍：转换所有实体为Plan对象
        entities.forEach { entity ->
            val milestones = milestoneDao.getMilestonesByPlanIdOnce(entity.id)
            val plan = entityToPlan(entity, emptyList(), milestones)
            planMap[entity.id] = plan
        }
        
        // 第二遍：构建树形结构
        entities.forEach { entity ->
            val plan = planMap[entity.id]!!
            if (entity.parentId != null) {
                val parent = planMap[entity.parentId]
                if (parent != null) {
                    val updatedParent = parent.copy(
                        children = parent.children + plan
                    )
                    planMap[entity.parentId] = updatedParent
                }
            } else {
                rootPlans.add(plan)
            }
        }
        
        // 返回根计划（已包含完整的子计划树）
        return rootPlans.map { planMap[it.id]!! }
    }
    
    /**
     * 实体转换为领域模型
     */
    private fun entityToPlan(
        entity: PlanEntity,
        children: List<Plan> = emptyList(),
        milestoneEntities: List<com.ccxiaoji.feature.plan.data.local.entity.MilestoneEntity> = emptyList()
    ): Plan {
        val tags = try {
            json.decodeFromString<List<String>>(entity.tags)
        } catch (e: Exception) {
            emptyList()
        }
        
        val reminderSettings = entity.reminderSettings?.let { settingsJson ->
            try {
                json.decodeFromString<ReminderSettings>(settingsJson)
            } catch (e: Exception) {
                null
            }
        }
        
        val milestones = milestoneEntities.map { milestoneEntity ->
            Milestone(
                id = milestoneEntity.id,
                planId = milestoneEntity.planId,
                title = milestoneEntity.title,
                description = milestoneEntity.description,
                targetDate = Instant.fromEpochMilliseconds(milestoneEntity.targetDate)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date,
                isCompleted = milestoneEntity.isCompleted,
                completedDate = milestoneEntity.completedDate?.let {
                    Instant.fromEpochMilliseconds(it)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                },
                orderIndex = milestoneEntity.orderIndex
            )
        }
        
        return Plan(
            id = entity.id,
            parentId = entity.parentId,
            title = entity.title,
            description = entity.description,
            startDate = Instant.fromEpochMilliseconds(entity.startDate)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date,
            endDate = Instant.fromEpochMilliseconds(entity.endDate)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date,
            status = PlanStatus.valueOf(entity.status),
            progress = entity.progress,
            color = entity.color,
            priority = entity.priority,
            tags = tags,
            children = children,
            milestones = milestones,
            reminderSettings = reminderSettings,
            orderIndex = entity.orderIndex,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    /**
     * 领域模型转换为实体
     */
    private fun planToEntity(plan: Plan): PlanEntity {
        val tagsJson = json.encodeToString(plan.tags)
        val reminderSettingsJson = plan.reminderSettings?.let {
            json.encodeToString(it)
        }
        
        return PlanEntity(
            id = plan.id,
            parentId = plan.parentId,
            title = plan.title,
            description = plan.description,
            startDate = plan.startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            endDate = plan.endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            status = plan.status.name,
            progress = plan.progress,
            color = plan.color,
            priority = plan.priority,
            tags = tagsJson,
            createdAt = plan.createdAt,
            updatedAt = plan.updatedAt,
            syncStatus = "LOCAL",
            isTemplate = false,
            templateId = null,
            orderIndex = plan.orderIndex,
            reminderSettings = reminderSettingsJson
        )
    }
    
    /**
     * 里程碑转换为实体
     */
    private fun milestoneToEntity(milestone: Milestone): com.ccxiaoji.feature.plan.data.local.entity.MilestoneEntity {
        return com.ccxiaoji.feature.plan.data.local.entity.MilestoneEntity(
            id = milestone.id.ifEmpty { UUID.randomUUID().toString() },
            planId = milestone.planId,
            title = milestone.title,
            targetDate = milestone.targetDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            isCompleted = milestone.isCompleted,
            completedDate = milestone.completedDate?.atStartOfDayIn(TimeZone.currentSystemDefault())?.toEpochMilliseconds(),
            description = milestone.description,
            orderIndex = milestone.orderIndex,
            createdAt = System.currentTimeMillis(),
            syncStatus = "LOCAL"
        )
    }
}