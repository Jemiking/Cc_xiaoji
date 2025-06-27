package com.ccxiaoji.feature.schedule.data.local.dao

import androidx.room.*
import com.ccxiaoji.feature.schedule.data.local.entity.ShiftEntity
import kotlinx.coroutines.flow.Flow

/**
 * 班次数据访问对象
 * 提供班次相关的数据库操作方法
 */
@Dao
interface ShiftDao {
    
    /**
     * 获取所有班次
     */
    @Query("SELECT * FROM shifts WHERE is_active = 1 ORDER BY start_time ASC")
    fun getAllShifts(): Flow<List<ShiftEntity>>
    
    /**
     * 根据ID获取班次
     */
    @Query("SELECT * FROM shifts WHERE id = :shiftId")
    suspend fun getShiftById(shiftId: Long): ShiftEntity?
    
    /**
     * 插入新班次
     */
    @Insert
    suspend fun insertShift(shift: ShiftEntity): Long
    
    /**
     * 更新班次
     */
    @Update
    suspend fun updateShift(shift: ShiftEntity)
    
    /**
     * 删除班次（软删除）
     */
    @Query("UPDATE shifts SET is_active = 0, updated_at = :timestamp WHERE id = :shiftId")
    suspend fun deleteShift(shiftId: Long, timestamp: Long = System.currentTimeMillis())
    
    /**
     * 检查班次名称是否已存在
     */
    @Query("SELECT COUNT(*) FROM shifts WHERE name = :name AND is_active = 1 AND id != :excludeId")
    suspend fun checkShiftNameExists(name: String, excludeId: Long = 0): Int
    
    /**
     * 获取默认班次（用于快速排班）
     */
    @Query("SELECT * FROM shifts WHERE is_active = 1 ORDER BY created_at ASC LIMIT 3")
    suspend fun getDefaultShifts(): List<ShiftEntity>
    
    /**
     * 获取所有班次（非Flow版本）
     */
    @Query("SELECT * FROM shifts WHERE is_active = 1 ORDER BY start_time ASC")
    suspend fun getAllShiftsList(): List<ShiftEntity>
    
    /**
     * 删除所有班次
     */
    @Query("DELETE FROM shifts")
    suspend fun deleteAllShifts(): Int
}