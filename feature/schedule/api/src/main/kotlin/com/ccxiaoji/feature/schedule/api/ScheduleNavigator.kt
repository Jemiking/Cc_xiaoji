package com.ccxiaoji.feature.schedule.api

import java.time.LocalDate

/**
 * 排班模块导航接口
 * 定义模块内部和对外的导航功能
 */
interface ScheduleNavigator {
    /**
     * 导航到排班主页（日历视图）
     */
    fun navigateToScheduleHome()

    /**
     * 导航到班次管理页面
     */
    fun navigateToShiftManagement()

    /**
     * 导航到排班编辑页面
     * @param date 要编辑的日期
     */
    fun navigateToScheduleEdit(date: LocalDate)

    /**
     * 导航到批量排班页面
     */
    fun navigateToSchedulePattern()

    /**
     * 导航到排班统计页面
     */
    fun navigateToScheduleStatistics()

    /**
     * 导航到数据导出页面
     */
    fun navigateToScheduleExport()

    /**
     * 导航到设置页面
     */
    fun navigateToScheduleSettings()

    /**
     * 返回上一页
     */
    fun navigateBack()
}