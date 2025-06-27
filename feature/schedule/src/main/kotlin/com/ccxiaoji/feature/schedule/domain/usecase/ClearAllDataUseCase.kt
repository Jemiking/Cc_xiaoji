package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.data.local.dao.ScheduleDao
import com.ccxiaoji.feature.schedule.data.local.dao.ShiftDao
import com.ccxiaoji.feature.schedule.data.local.dao.ExportHistoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 清除所有数据用例
 * 
 * 负责清除应用中的所有数据
 */
class ClearAllDataUseCase @Inject constructor(
    private val shiftDao: ShiftDao,
    private val scheduleDao: ScheduleDao,
    private val exportHistoryDao: ExportHistoryDao
) {
    
    /**
     * 执行清除所有数据
     * 
     * @return 清除结果
     */
    suspend operator fun invoke(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 清除所有排班数据
            scheduleDao.deleteAllSchedules()
            
            // 清除所有班次数据
            shiftDao.deleteAllShifts()
            
            // 清除所有导出历史
            exportHistoryDao.deleteAllHistory()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取数据统计信息
     * 
     * @return 各类数据的数量
     */
    suspend fun getDataStatistics(): DataStatistics = withContext(Dispatchers.IO) {
        DataStatistics(
            shiftCount = shiftDao.getAllShiftsList().size,
            scheduleCount = scheduleDao.getAllSchedules().size,
            exportHistoryCount = exportHistoryDao.getAllHistory().size
        )
    }
}

/**
 * 数据统计信息
 */
data class DataStatistics(
    val shiftCount: Int,
    val scheduleCount: Int,
    val exportHistoryCount: Int
) {
    val totalCount: Int
        get() = shiftCount + scheduleCount + exportHistoryCount
}