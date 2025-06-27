package com.ccxiaoji.feature.plan.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ccxiaoji.feature.plan.data.local.entity.MilestoneEntity
import kotlinx.coroutines.flow.Flow

/**
 * 里程碑数据访问对象
 */
@Dao
interface MilestoneDao {
    
    /**
     * 插入里程碑
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity): Long
    
    /**
     * 批量插入里程碑
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<MilestoneEntity>)
    
    /**
     * 更新里程碑
     */
    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)
    
    /**
     * 删除里程碑
     */
    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)
    
    /**
     * 根据ID删除里程碑
     */
    @Query("DELETE FROM milestone_table WHERE id = :milestoneId")
    suspend fun deleteMilestoneById(milestoneId: String)
    
    /**
     * 删除某个计划的所有里程碑
     */
    @Query("DELETE FROM milestone_table WHERE plan_id = :planId")
    suspend fun deleteMilestonesByPlanId(planId: String)
    
    /**
     * 获取某个计划的所有里程碑
     */
    @Query("SELECT * FROM milestone_table WHERE plan_id = :planId ORDER BY order_index ASC")
    fun getMilestonesByPlanId(planId: String): Flow<List<MilestoneEntity>>
    
    /**
     * 获取某个计划的所有里程碑（一次性）
     */
    @Query("SELECT * FROM milestone_table WHERE plan_id = :planId ORDER BY order_index ASC")
    suspend fun getMilestonesByPlanIdOnce(planId: String): List<MilestoneEntity>
    
    /**
     * 批量获取多个计划的所有里程碑
     */
    @Query("SELECT * FROM milestone_table WHERE plan_id IN (:planIds) ORDER BY plan_id, order_index ASC")
    suspend fun getMilestonesByPlanIds(planIds: List<String>): List<MilestoneEntity>
    
    /**
     * 根据ID获取里程碑
     */
    @Query("SELECT * FROM milestone_table WHERE id = :milestoneId")
    suspend fun getMilestoneById(milestoneId: String): MilestoneEntity?
    
    /**
     * 更新里程碑完成状态
     */
    @Query("UPDATE milestone_table SET is_completed = :isCompleted, completed_date = :completedDate WHERE id = :milestoneId")
    suspend fun updateMilestoneCompletion(milestoneId: String, isCompleted: Boolean, completedDate: Long?)
    
    /**
     * 获取即将到期的里程碑（未完成且目标日期在指定范围内）
     */
    @Query("""
        SELECT * FROM milestone_table 
        WHERE is_completed = 0 
        AND target_date BETWEEN :startDate AND :endDate
        ORDER BY target_date ASC
    """)
    fun getUpcomingMilestones(startDate: Long, endDate: Long): Flow<List<MilestoneEntity>>
    
    /**
     * 获取已逾期的里程碑（未完成且目标日期已过）
     */
    @Query("""
        SELECT * FROM milestone_table 
        WHERE is_completed = 0 
        AND target_date < :currentDate
        ORDER BY target_date ASC
    """)
    fun getOverdueMilestones(currentDate: Long): Flow<List<MilestoneEntity>>
    
    /**
     * 清空所有里程碑数据
     */
    @Query("DELETE FROM milestone_table")
    suspend fun deleteAllMilestones()
}