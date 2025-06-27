package com.ccxiaoji.feature.plan.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ccxiaoji.feature.plan.data.local.entity.PlanEntity
import kotlinx.coroutines.flow.Flow

/**
 * 计划数据访问对象
 */
@Dao
interface PlanDao {
    
    /**
     * 插入计划
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PlanEntity): Long
    
    /**
     * 批量插入计划
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<PlanEntity>)
    
    /**
     * 更新计划
     */
    @Update
    suspend fun updatePlan(plan: PlanEntity)
    
    /**
     * 批量更新计划
     */
    @Update
    suspend fun updatePlans(plans: List<PlanEntity>)
    
    /**
     * 删除计划
     */
    @Delete
    suspend fun deletePlan(plan: PlanEntity)
    
    /**
     * 根据ID删除计划
     */
    @Query("DELETE FROM plan_table WHERE id = :planId")
    suspend fun deletePlanById(planId: String)
    
    /**
     * 批量删除计划
     */
    @Query("DELETE FROM plan_table WHERE id IN (:planIds)")
    suspend fun deletePlansByIds(planIds: List<String>)
    
    /**
     * 获取所有计划
     */
    @Query("SELECT * FROM plan_table ORDER BY order_index ASC")
    fun getAllPlans(): Flow<List<PlanEntity>>
    
    /**
     * 获取所有顶级计划（parent_id为null）
     */
    @Query("SELECT * FROM plan_table WHERE parent_id IS NULL ORDER BY order_index ASC")
    fun getRootPlans(): Flow<List<PlanEntity>>
    
    /**
     * 根据ID获取计划
     */
    @Query("SELECT * FROM plan_table WHERE id = :planId")
    suspend fun getPlanById(planId: String): PlanEntity?
    
    /**
     * 获取子计划
     */
    @Query("SELECT * FROM plan_table WHERE parent_id = :parentId ORDER BY order_index ASC")
    suspend fun getChildPlans(parentId: String): List<PlanEntity>
    
    /**
     * 根据状态获取计划
     */
    @Query("SELECT * FROM plan_table WHERE status = :status ORDER BY priority DESC, created_at DESC")
    fun getPlansByStatus(status: String): Flow<List<PlanEntity>>
    
    /**
     * 搜索计划（标题或描述）
     */
    @Query("""
        SELECT * FROM plan_table 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY priority DESC, created_at DESC
    """)
    fun searchPlans(query: String): Flow<List<PlanEntity>>
    
    /**
     * 更新计划进度
     */
    @Query("UPDATE plan_table SET progress = :progress, updated_at = :updatedAt WHERE id = :planId")
    suspend fun updatePlanProgress(planId: String, progress: Float, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 更新计划状态
     */
    @Query("UPDATE plan_table SET status = :status, updated_at = :updatedAt WHERE id = :planId")
    suspend fun updatePlanStatus(planId: String, status: String, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 更新计划的父ID（移动计划）
     */
    @Query("UPDATE plan_table SET parent_id = :newParentId, updated_at = :updatedAt WHERE id = :planId")
    suspend fun updatePlanParent(planId: String, newParentId: String?, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 批量更新计划排序
     */
    @Transaction
    suspend fun updatePlansOrder(planOrders: List<Pair<String, Int>>) {
        planOrders.forEach { (planId, orderIndex) ->
            updatePlanOrder(planId, orderIndex)
        }
    }
    
    /**
     * 更新单个计划的排序
     */
    @Query("UPDATE plan_table SET order_index = :orderIndex WHERE id = :planId")
    suspend fun updatePlanOrder(planId: String, orderIndex: Int)
    
    /**
     * 获取计划数量
     */
    @Query("SELECT COUNT(*) FROM plan_table")
    suspend fun getPlanCount(): Int
    
    /**
     * 根据标签获取计划
     */
    @Query("SELECT * FROM plan_table WHERE tags LIKE '%' || :tag || '%' ORDER BY created_at DESC")
    fun getPlansByTag(tag: String): Flow<List<PlanEntity>>
    
    /**
     * 清空所有计划数据
     */
    @Query("DELETE FROM plan_table")
    suspend fun deleteAllPlans()
}