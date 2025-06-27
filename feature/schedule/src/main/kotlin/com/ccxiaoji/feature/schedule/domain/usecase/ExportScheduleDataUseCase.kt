package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.first
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 导出排班数据用例
 */
class ExportScheduleDataUseCase @Inject constructor(
    private val repository: ScheduleRepository,
    private val getStatisticsUseCase: GetStatisticsUseCase
) {
    
    /**
     * 导出为CSV格式
     */
    suspend fun exportToCsv(
        startDate: LocalDate,
        endDate: LocalDate,
        outputFile: File
    ): Result<File> {
        return try {
            val schedules = getSchedulesInRange(startDate, endDate)
            val csvContent = buildCsvContent(schedules, startDate, endDate)
            
            outputFile.writeText(csvContent)
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 导出为JSON格式
     */
    suspend fun exportToJson(
        startDate: LocalDate,
        endDate: LocalDate,
        outputFile: File
    ): Result<File> {
        return try {
            val schedules = getSchedulesInRange(startDate, endDate)
            val statistics = getStatisticsUseCase(startDate, endDate)
            
            val jsonContent = buildJsonContent(schedules, statistics, startDate, endDate)
            outputFile.writeText(jsonContent)
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 导出统计报表
     */
    suspend fun exportStatisticsReport(
        startDate: LocalDate,
        endDate: LocalDate,
        outputFile: File
    ): Result<File> {
        return try {
            val statistics = getStatisticsUseCase(startDate, endDate)
            val reportContent = buildStatisticsReport(statistics, startDate, endDate)
            
            outputFile.writeText(reportContent)
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun getSchedulesInRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Schedule> {
        val schedules = mutableListOf<Schedule>()
        var currentMonth = startDate.withDayOfMonth(1)
        val endMonth = endDate.withDayOfMonth(1)
        
        while (!currentMonth.isAfter(endMonth)) {
            val monthSchedules = repository.getSchedulesByMonth(
                currentMonth.year.let { year ->
                    currentMonth.month.let { month ->
                        java.time.YearMonth.of(year, month)
                    }
                }
            ).first()
            
            schedules.addAll(
                monthSchedules.filter { schedule ->
                    !schedule.date.isBefore(startDate) && !schedule.date.isAfter(endDate)
                }
            )
            
            currentMonth = currentMonth.plusMonths(1)
        }
        
        return schedules.sortedBy { it.date }
    }
    
    private fun buildCsvContent(
        schedules: List<Schedule>,
        startDate: LocalDate,
        endDate: LocalDate
    ): String {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        
        val header = "日期,星期,班次名称,开始时间,结束时间,工时,备注,实际开始时间,实际结束时间\n"
        
        val rows = schedules.joinToString("\n") { schedule ->
            val dayOfWeek = when (schedule.date.dayOfWeek) {
                java.time.DayOfWeek.MONDAY -> "星期一"
                java.time.DayOfWeek.TUESDAY -> "星期二"
                java.time.DayOfWeek.WEDNESDAY -> "星期三"
                java.time.DayOfWeek.THURSDAY -> "星期四"
                java.time.DayOfWeek.FRIDAY -> "星期五"
                java.time.DayOfWeek.SATURDAY -> "星期六"
                java.time.DayOfWeek.SUNDAY -> "星期日"
            }
            
            buildString {
                append(schedule.date.format(dateFormatter)).append(",")
                append(dayOfWeek).append(",")
                append(schedule.shift.name).append(",")
                append(schedule.shift.startTime.format(timeFormatter)).append(",")
                append(schedule.shift.endTime.format(timeFormatter)).append(",")
                append(schedule.shift.duration).append(",")
                append(schedule.note ?: "").append(",")
                append(schedule.actualStartTime?.format(timeFormatter) ?: "").append(",")
                append(schedule.actualEndTime?.format(timeFormatter) ?: "")
            }
        }
        
        return buildString {
            appendLine("排班数据导出")
            appendLine("导出时间: ${LocalDate.now().format(dateFormatter)}")
            appendLine("数据范围: ${startDate.format(dateFormatter)} 至 ${endDate.format(dateFormatter)}")
            appendLine()
            append(header)
            append(rows)
        }
    }
    
    private fun buildJsonContent(
        schedules: List<Schedule>,
        statistics: ScheduleStatistics,
        startDate: LocalDate,
        endDate: LocalDate
    ): String {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        
        // 手动构建JSON（简化版，实际项目应使用JSON库）
        return buildString {
            appendLine("{")
            appendLine("  \"exportInfo\": {")
            appendLine("    \"exportDate\": \"${LocalDate.now().format(dateFormatter)}\",")
            appendLine("    \"startDate\": \"${startDate.format(dateFormatter)}\",")
            appendLine("    \"endDate\": \"${endDate.format(dateFormatter)}\"")
            appendLine("  },")
            
            appendLine("  \"statistics\": {")
            appendLine("    \"totalDays\": ${statistics.totalDays},")
            appendLine("    \"workDays\": ${statistics.workDays},")
            appendLine("    \"restDays\": ${statistics.restDays},")
            appendLine("    \"totalHours\": ${statistics.totalHours},")
            appendLine("    \"shiftDistribution\": {")
            val shiftEntries = statistics.shiftDistribution.entries.joinToString(",\n") { (shift, days) ->
                "      \"$shift\": $days"
            }
            appendLine(shiftEntries)
            appendLine("    }")
            appendLine("  },")
            
            appendLine("  \"schedules\": [")
            val scheduleEntries = schedules.joinToString(",\n") { schedule ->
                buildString {
                    append("    {\n")
                    append("      \"date\": \"${schedule.date.format(dateFormatter)}\",\n")
                    append("      \"shift\": \"${schedule.shift.name}\",\n")
                    append("      \"startTime\": \"${schedule.shift.startTime.format(timeFormatter)}\",\n")
                    append("      \"endTime\": \"${schedule.shift.endTime.format(timeFormatter)}\",\n")
                    append("      \"duration\": ${schedule.shift.duration},\n")
                    append("      \"note\": ${schedule.note?.let { "\"$it\"" } ?: "null"}\n")
                    append("    }")
                }
            }
            appendLine(scheduleEntries)
            appendLine("  ]")
            appendLine("}")
        }
    }
    
    private fun buildStatisticsReport(
        statistics: ScheduleStatistics,
        startDate: LocalDate,
        endDate: LocalDate
    ): String {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        return buildString {
            appendLine("===============================")
            appendLine("        排班统计报表")
            appendLine("===============================")
            appendLine()
            appendLine("统计期间: ${startDate.format(dateFormatter)} 至 ${endDate.format(dateFormatter)}")
            appendLine("生成时间: ${LocalDate.now().format(dateFormatter)}")
            appendLine()
            appendLine("【总体统计】")
            appendLine("总天数: ${statistics.totalDays} 天")
            appendLine("工作天数: ${statistics.workDays} 天")
            appendLine("休息天数: ${statistics.restDays} 天")
            appendLine("总工时: ${statistics.totalHours} 小时")
            appendLine("平均每日工时: ${"%.1f".format(
                if (statistics.workDays > 0) statistics.totalHours / statistics.workDays else 0.0
            )} 小时")
            appendLine()
            appendLine("【班次分布】")
            statistics.shiftDistribution.forEach { (shift, days) ->
                val percentage = if (statistics.totalDays > 0) {
                    (days * 100.0 / statistics.totalDays)
                } else 0.0
                appendLine("$shift: $days 天 (${"%.1f".format(percentage)}%)")
            }
            appendLine()
            appendLine("===============================")
        }
    }
}