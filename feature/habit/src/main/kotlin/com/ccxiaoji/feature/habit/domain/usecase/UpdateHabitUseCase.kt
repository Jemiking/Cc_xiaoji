package com.ccxiaoji.feature.habit.domain.usecase

import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import javax.inject.Inject

/**
 * 更新习惯用例
 * 处理习惯更新的业务逻辑
 */
class UpdateHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    /**
     * 更新习惯信息
     * @param habitId 习惯ID
     * @param title 标题
     * @param description 描述（可选）
     * @param period 周期（daily/weekly/monthly）
     * @param target 目标次数
     * @param color 颜色
     * @param icon 图标（可选）
     */
    suspend operator fun invoke(
        habitId: String,
        title: String,
        description: String? = null,
        period: String,
        target: Int,
        color: String,
        icon: String? = null
    ) {
        // 验证输入
        if (habitId.isBlank()) {
            throw DomainException.ValidationException("习惯ID不能为空")
        }
        if (title.isBlank()) {
            throw DomainException.ValidationException("标题不能为空")
        }
        if (period !in listOf("daily", "weekly", "monthly")) {
            throw DomainException.ValidationException("无效的周期类型")
        }
        if (target <= 0) {
            throw DomainException.ValidationException("目标次数必须大于0")
        }
        
        repository.updateHabit(
            habitId = habitId,
            title = title.trim(),
            description = description?.trim(),
            period = period,
            target = target,
            color = color,
            icon = icon
        ).getOrThrow()
    }
}