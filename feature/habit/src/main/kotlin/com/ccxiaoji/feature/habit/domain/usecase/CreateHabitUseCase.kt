package com.ccxiaoji.feature.habit.domain.usecase

import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.habit.domain.model.Habit
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import javax.inject.Inject

/**
 * 创建习惯用例
 * 处理习惯创建的业务逻辑
 */
class CreateHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    /**
     * 创建新习惯
     * @param title 标题
     * @param description 描述（可选）
     * @param period 周期（daily/weekly/monthly）
     * @param target 目标次数
     * @param color 颜色
     * @param icon 图标（可选）
     * @return 创建的习惯
     */
    suspend operator fun invoke(
        title: String,
        description: String? = null,
        period: String,
        target: Int,
        color: String = "#3A7AFE",
        icon: String? = null
    ): Habit {
        // 验证输入
        if (title.isBlank()) {
            throw DomainException.ValidationException("标题不能为空")
        }
        if (period !in listOf("daily", "weekly", "monthly")) {
            throw DomainException.ValidationException("无效的周期类型")
        }
        if (target <= 0) {
            throw DomainException.ValidationException("目标次数必须大于0")
        }
        
        val result = repository.createHabit(
            title = title.trim(),
            description = description?.trim(),
            period = period,
            target = target,
            color = color,
            icon = icon
        )
        
        return result.getOrThrow()
    }
}