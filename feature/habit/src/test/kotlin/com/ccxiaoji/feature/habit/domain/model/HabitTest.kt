package com.ccxiaoji.feature.habit.domain.model

import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * HabitTest - Habit 模型单元测试
 *
 * 测试重点：getEffectiveReminderTime() 方法的提醒时间计算逻辑
 *
 * 测试覆盖场景：
 * 1. 使用全局配置（reminderEnabled=null, reminderTime=null）
 * 2. 单习惯禁用提醒（reminderEnabled=false）
 * 3. 自定义提醒时间（reminderTime="07:00"）
 * 4. 边界情况（全局关闭、单习惯强制启用等）
 */
class HabitTest {

    /**
     * 测试场景1：使用全局配置
     *
     * 条件：
     * - reminderEnabled = null（继承全局）
     * - reminderTime = null（继承全局）
     * - globalEnabled = true, globalTime = "20:00"
     *
     * 预期结果：每天 20:00 提醒
     */
    @Test
    fun `getEffectiveReminderTime - 使用全局配置`() {
        val habit = Habit(
            id = "habit_1",
            title = "晚间阅读",
            description = "每天阅读30分钟",
            period = "daily",
            target = 1,
            color = "#3A7AFE",
            icon = "book",
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = null,
            reminderTime = null
        )

        val result = habit.getEffectiveReminderTime(
            globalTime = "20:00",
            globalEnabled = true
        )

        // 断言：应该是 20:00
        assertEquals(Pair(20, 0), result)
    }

    /**
     * 测试场景2：单习惯禁用提醒
     *
     * 条件：
     * - reminderEnabled = false（强制禁用）
     * - globalEnabled = true（全局启用也无效）
     *
     * 预期结果：返回 null，不提醒
     */
    @Test
    fun `getEffectiveReminderTime - 单习惯禁用`() {
        val habit = Habit(
            id = "habit_2",
            title = "不需要提醒的习惯",
            description = "自主完成",
            period = "daily",
            target = 1,
            color = "#3A7AFE",
            icon = null,
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = false,  // 强制禁用
            reminderTime = null
        )

        val result = habit.getEffectiveReminderTime(
            globalTime = "20:00",
            globalEnabled = true  // 全局启用也无效
        )

        // 断言：应该返回 null
        assertNull(result)
    }

    /**
     * 测试场景3：自定义提醒时间
     *
     * 条件：
     * - reminderEnabled = true（启用提醒）
     * - reminderTime = "07:00"（晨间提醒）
     * - globalTime = "20:00"（全局晚间提醒，应被覆盖）
     *
     * 预期结果：每天 07:00 提醒
     */
    @Test
    fun `getEffectiveReminderTime - 自定义提醒时间`() {
        val habit = Habit(
            id = "habit_3",
            title = "晨跑",
            description = "早上7点跑步",
            period = "daily",
            target = 1,
            color = "#4CAF50",
            icon = "run",
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = true,
            reminderTime = "07:00"  // 自定义时间
        )

        val result = habit.getEffectiveReminderTime(
            globalTime = "20:00",  // 全局配置应被忽略
            globalEnabled = true
        )

        // 断言：应该是 07:00
        assertEquals(Pair(7, 0), result)
    }

    /**
     * 测试场景4：自定义时间（包含分钟）
     *
     * 条件：
     * - reminderTime = "12:30"（中午 12:30）
     *
     * 预期结果：每天 12:30 提醒
     */
    @Test
    fun `getEffectiveReminderTime - 自定义时间包含分钟`() {
        val habit = Habit(
            id = "habit_4",
            title = "午间冥想",
            description = "中午12:30冥想",
            period = "daily",
            target = 1,
            color = "#FF9800",
            icon = "meditation",
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = true,
            reminderTime = "12:30"
        )

        val result = habit.getEffectiveReminderTime(
            globalTime = "20:00",
            globalEnabled = true
        )

        // 断言：应该是 12:30
        assertEquals(Pair(12, 30), result)
    }

    /**
     * 测试场景5：全局关闭，单习惯未显式启用
     *
     * 条件：
     * - reminderEnabled = null（继承全局）
     * - globalEnabled = false（全局关闭）
     *
     * 预期结果：返回 null（继承全局关闭状态）
     */
    @Test
    fun `getEffectiveReminderTime - 全局关闭且单习惯未启用`() {
        val habit = Habit(
            id = "habit_5",
            title = "普通习惯",
            description = null,
            period = "daily",
            target = 1,
            color = "#3A7AFE",
            icon = null,
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = null,  // 继承全局
            reminderTime = null
        )

        val result = habit.getEffectiveReminderTime(
            globalTime = "20:00",
            globalEnabled = false  // 全局关闭
        )

        // 断言：应该返回 null
        assertNull(result)
    }

    /**
     * 测试场景6：单习惯强制启用，即使全局关闭
     *
     * 条件：
     * - reminderEnabled = true（强制启用）
     * - globalEnabled = false（全局关闭，应被覆盖）
     *
     * 预期结果：仍然提醒（单习惯配置优先级更高）
     */
    @Test
    fun `getEffectiveReminderTime - 单习惯强制启用覆盖全局关闭`() {
        val habit = Habit(
            id = "habit_6",
            title = "重要习惯",
            description = "即使全局关闭也要提醒",
            period = "daily",
            target = 1,
            color = "#F44336",
            icon = "star",
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = true,  // 强制启用
            reminderTime = "21:00"
        )

        val result = habit.getEffectiveReminderTime(
            globalTime = "20:00",
            globalEnabled = false  // 全局关闭，应被覆盖
        )

        // 断言：应该是 21:00
        assertEquals(Pair(21, 0), result)
    }

    /**
     * 测试场景7：边界时间（深夜23:59）
     *
     * 条件：
     * - reminderTime = "23:59"（深夜提醒）
     *
     * 预期结果：每天 23:59 提醒
     */
    @Test
    fun `getEffectiveReminderTime - 边界时间深夜23点59分`() {
        val habit = Habit(
            id = "habit_7",
            title = "睡前记录",
            description = "睡前写日记",
            period = "daily",
            target = 1,
            color = "#9C27B0",
            icon = "diary",
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = true,
            reminderTime = "23:59"
        )

        val result = habit.getEffectiveReminderTime(
            globalTime = "20:00",
            globalEnabled = true
        )

        // 断言：应该是 23:59
        assertEquals(Pair(23, 59), result)
    }

    /**
     * 测试场景8：边界时间（凌晨00:00）
     *
     * 条件：
     * - reminderTime = "00:00"（凌晨提醒）
     *
     * 预期结果：每天 00:00 提醒
     */
    @Test
    fun `getEffectiveReminderTime - 边界时间凌晨00点00分`() {
        val habit = Habit(
            id = "habit_8",
            title = "午夜习惯",
            description = "凌晨提醒",
            period = "daily",
            target = 1,
            color = "#607D8B",
            icon = null,
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = true,
            reminderTime = "00:00"
        )

        val result = habit.getEffectiveReminderTime(
            globalTime = "20:00",
            globalEnabled = true
        )

        // 断言：应该是 00:00
        assertEquals(Pair(0, 0), result)
    }
}
