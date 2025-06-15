package com.ccxiaoji.app.data.mapper

import com.ccxiaoji.app.domain.usecase.excel.ScheduleData
import com.ccxiaoji.core.common.util.DateConverter
import com.ccxiaoji.feature.schedule.api.ScheduleInfo
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import javax.inject.Inject

/**
 * 排班数据映射器
 * 负责处理排班模块相关的数据转换
 * 
 * 主要职责：
 * 1. API数据类型 → 业务数据类型
 * 2. 日期类型转换（java.time ↔ kotlinx.datetime）
 * 3. 字段映射和默认值处理
 */
class ScheduleDataMapper @Inject constructor() {
    
    /**
     * 将API返回的排班信息转换为Excel导出数据
     * @param scheduleInfo API排班信息（使用java.time.LocalDate）
     * @param kotlinDate 对应的kotlinx日期（用于时间戳计算）
     * @return Excel导出用的排班数据
     */
    fun mapScheduleInfoToExportData(
        scheduleInfo: ScheduleInfo,
        kotlinDate: LocalDate
    ): ScheduleData {
        return ScheduleData(
            date = kotlinDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            shiftName = scheduleInfo.shiftName,
            startTime = scheduleInfo.startTime,
            endTime = scheduleInfo.endTime,
            duration = DateConverter.calculateHours(scheduleInfo.startTime, scheduleInfo.endTime),
            color = scheduleInfo.color,
            note = if (scheduleInfo.isRestDay) "休息日" else null
        )
    }
    
    /**
     * 批量转换排班信息
     * @param scheduleInfoList API排班信息列表
     * @param dateMapping 日期映射表（java.time.LocalDate → kotlinx.datetime.LocalDate）
     * @return Excel导出用的排班数据列表
     */
    fun mapScheduleInfoListToExportData(
        scheduleInfoList: List<Pair<ScheduleInfo, LocalDate>>
    ): List<ScheduleData> {
        return scheduleInfoList.map { (info, kotlinDate) ->
            mapScheduleInfoToExportData(info, kotlinDate)
        }
    }
    
    /**
     * 将java.time.LocalDate转换为kotlinx.datetime.LocalDate
     * 便捷方法，避免重复调用DateConverter
     */
    fun convertDate(javaDate: java.time.LocalDate): LocalDate {
        return DateConverter.toKotlinDate(javaDate)
    }
    
    /**
     * 将kotlinx.datetime.LocalDate转换为java.time.LocalDate
     * 便捷方法，避免重复调用DateConverter
     */
    fun convertDate(kotlinDate: LocalDate): java.time.LocalDate {
        return DateConverter.toJavaDate(kotlinDate)
    }
    
    /**
     * 计算并格式化工时
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 格式化的工时字符串，如"8小时"
     */
    fun formatWorkHours(startTime: String, endTime: String): String {
        val hours = DateConverter.calculateHours(startTime, endTime)
        return if (hours == hours.toInt().toDouble()) {
            "${hours.toInt()}小时"
        } else {
            "${hours}小时"
        }
    }
}