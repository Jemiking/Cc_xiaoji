package com.ccxiaoji.feature.todo.domain.model

import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * TaskTest - Task 模型单元测试
 *
 * 测试重点：getEffectiveReminderTime() 方法的提醒时间计算逻辑
 *
 * 测试覆盖场景：
 * 1. 使用全局配置（reminderEnabled=null, reminderMinutesBefore=null）
 * 2. 单任务禁用提醒（reminderEnabled=false）
 * 3. 自定义提前分钟数（reminderMinutesBefore=60）
 * 4. 绝对时间优先（reminderAt 优先级最高）
 * 5. 边界情况（无截止时间、全局关闭等）
 */
class TaskTest {

    /**
     * 测试场景1：使用全局配置
     *
     * 条件：
     * - reminderEnabled = null（继承全局）
     * - reminderAt = null（无绝对时间）
     * - reminderMinutesBefore = null（继承全局）
     * - globalEnabled = true, globalMinutes = 30
     *
     * 预期结果：截止时间前30分钟提醒
     */
    @Test
    fun `getEffectiveReminderTime - 使用全局配置`() {
        val task = Task(
            id = "test_1",
            title = "测试任务",
            description = null,
            dueAt = Instant.parse("2025-10-05T15:00:00Z"),
            priority = 0,
            completed = false,
            completedAt = null,
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = null,
            reminderAt = null,
            reminderMinutesBefore = null
        )

        val result = task.getEffectiveReminderTime(
            globalMinutes = 30,
            globalEnabled = true
        )

        // 断言：应该是截止时间前30分钟（14:30）
        assertEquals(
            Instant.parse("2025-10-05T14:30:00Z"),
            result
        )
    }

    /**
     * 测试场景2：单任务禁用提醒
     *
     * 条件：
     * - reminderEnabled = false（强制禁用）
     * - globalEnabled = true（全局启用也无效）
     *
     * 预期结果：返回 null，不提醒
     */
    @Test
    fun `getEffectiveReminderTime - 单任务禁用`() {
        val task = Task(
            id = "test_2",
            title = "不需要提醒的任务",
            description = null,
            dueAt = Instant.parse("2025-10-05T15:00:00Z"),
            priority = 0,
            completed = false,
            completedAt = null,
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = false,  // 强制禁用
            reminderAt = null,
            reminderMinutesBefore = null
        )

        val result = task.getEffectiveReminderTime(
            globalMinutes = 30,
            globalEnabled = true  // 全局启用也无效
        )

        // 断言：应该返回 null
        assertNull(result)
    }

    /**
     * 测试场景3：自定义提前分钟数
     *
     * 条件：
     * - reminderEnabled = true（启用提醒）
     * - reminderMinutesBefore = 60（提前60分钟）
     * - globalMinutes = 30（全局30分钟，应被覆盖）
     *
     * 预期结果：截止时间前60分钟提醒
     */
    @Test
    fun `getEffectiveReminderTime - 自定义分钟数`() {
        val task = Task(
            id = "test_3",
            title = "重要会议",
            description = "需要提前1小时准备",
            dueAt = Instant.parse("2025-10-05T15:00:00Z"),
            priority = 2,  // 高优先级
            completed = false,
            completedAt = null,
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = true,
            reminderAt = null,
            reminderMinutesBefore = 60  // 自定义60分钟
        )

        val result = task.getEffectiveReminderTime(
            globalMinutes = 30,  // 全局配置应被忽略
            globalEnabled = true
        )

        // 断言：应该是截止时间前60分钟（14:00）
        assertEquals(
            Instant.parse("2025-10-05T14:00:00Z"),
            result
        )
    }

    /**
     * 测试场景4：绝对时间优先
     *
     * 条件：
     * - reminderAt = 前一天 09:00（绝对时间）
     * - reminderMinutesBefore = 60（即使设置了分钟数，也应忽略）
     *
     * 预期结果：使用绝对时间，而非相对时间
     */
    @Test
    fun `getEffectiveReminderTime - 绝对时间优先`() {
        val task = Task(
            id = "test_4",
            title = "项目截止",
            description = "需要前一天早上提醒",
            dueAt = Instant.parse("2025-10-06T18:00:00Z"),
            priority = 2,
            completed = false,
            completedAt = null,
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = true,
            reminderAt = Instant.parse("2025-10-05T09:00:00Z"),  // 绝对时间
            reminderMinutesBefore = 60  // 应被忽略
        )

        val result = task.getEffectiveReminderTime(
            globalMinutes = 30,
            globalEnabled = true
        )

        // 断言：应该使用绝对时间（10月5日 09:00），而不是相对时间
        assertEquals(
            Instant.parse("2025-10-05T09:00:00Z"),
            result
        )
    }

    /**
     * 测试场景5：无截止时间
     *
     * 条件：
     * - dueAt = null（无截止时间）
     * - reminderEnabled = true
     *
     * 预期结果：返回 null（没有截止时间无法计算提醒时间）
     */
    @Test
    fun `getEffectiveReminderTime - 无截止时间`() {
        val task = Task(
            id = "test_5",
            title = "无截止时间的任务",
            description = null,
            dueAt = null,  // 无截止时间
            priority = 0,
            completed = false,
            completedAt = null,
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = true,
            reminderAt = null,
            reminderMinutesBefore = 30
        )

        val result = task.getEffectiveReminderTime(
            globalMinutes = 30,
            globalEnabled = true
        )

        // 断言：应该返回 null
        assertNull(result)
    }

    /**
     * 测试场景6：全局提醒关闭，单任务未显式启用
     *
     * 条件：
     * - reminderEnabled = null（继承全局）
     * - globalEnabled = false（全局关闭）
     *
     * 预期结果：返回 null（继承全局关闭状态）
     */
    @Test
    fun `getEffectiveReminderTime - 全局关闭且单任务未启用`() {
        val task = Task(
            id = "test_6",
            title = "普通任务",
            description = null,
            dueAt = Instant.parse("2025-10-05T15:00:00Z"),
            priority = 0,
            completed = false,
            completedAt = null,
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = null,  // 继承全局
            reminderAt = null,
            reminderMinutesBefore = null
        )

        val result = task.getEffectiveReminderTime(
            globalMinutes = 30,
            globalEnabled = false  // 全局关闭
        )

        // 断言：应该返回 null
        assertNull(result)
    }

    /**
     * 测试场景7：单任务强制启用，即使全局关闭
     *
     * 条件：
     * - reminderEnabled = true（强制启用）
     * - globalEnabled = false（全局关闭，应被覆盖）
     *
     * 预期结果：仍然提醒（单任务配置优先级更高）
     */
    @Test
    fun `getEffectiveReminderTime - 单任务强制启用覆盖全局关闭`() {
        val task = Task(
            id = "test_7",
            title = "重要任务",
            description = "即使全局关闭也要提醒",
            dueAt = Instant.parse("2025-10-05T15:00:00Z"),
            priority = 2,
            completed = false,
            completedAt = null,
            createdAt = Instant.parse("2025-10-04T10:00:00Z"),
            updatedAt = Instant.parse("2025-10-04T10:00:00Z"),
            reminderEnabled = true,  // 强制启用
            reminderAt = null,
            reminderMinutesBefore = 45
        )

        val result = task.getEffectiveReminderTime(
            globalMinutes = 30,
            globalEnabled = false  // 全局关闭，应被覆盖
        )

        // 断言：应该是截止时间前45分钟（14:15）
        assertEquals(
            Instant.parse("2025-10-05T14:15:00Z"),
            result
        )
    }
}
