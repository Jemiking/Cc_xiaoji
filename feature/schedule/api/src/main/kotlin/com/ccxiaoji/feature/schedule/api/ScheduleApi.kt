package com.ccxiaoji.feature.schedule.api

import java.time.LocalDate

/**
 * 排班模块对外暴露的API接口
 * 提供给其他模块调用的功能
 */
interface ScheduleApi {
    /**
     * 获取指定日期的排班信息
     * @param date 日期
     * @return 排班信息，如果当天没有排班则返回null
     */
    suspend fun getScheduleByDate(date: LocalDate): ScheduleInfo?

    /**
     * 获取今日的排班信息
     */
    suspend fun getTodaySchedule(): ScheduleInfo?

    /**
     * 获取本月的工作天数
     */
    suspend fun getCurrentMonthWorkDays(): Int

    /**
     * 获取本月的总工时
     */
    suspend fun getCurrentMonthWorkHours(): Double

    /**
     * 导航到排班主页
     */
    fun navigateToScheduleHome()

    /**
     * 导航到添加排班页面
     * @param date 预设日期，可选
     */
    fun navigateToAddSchedule(date: LocalDate? = null)
}

/**
 * 排班信息数据类
 */
data class ScheduleInfo(
    val date: LocalDate,
    val shiftName: String,
    val startTime: String,
    val endTime: String,
    val color: String,
    val isRestDay: Boolean = false
)