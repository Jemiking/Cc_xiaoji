package com.ccxiaoji.feature.habit.domain.usecase

import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * 习惯打卡用例
 * 处理习惯打卡的业务逻辑
 */
class CheckInHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    /**
     * 为指定习惯打卡
     * @param habitId 习惯ID
     * @param date 打卡日期（默认为今天）
     */
    suspend operator fun invoke(
        habitId: String,
        date: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    ) {
        if (habitId.isBlank()) {
            throw DomainException.ValidationException("习惯ID不能为空")
        }
        
        repository.checkInHabit(habitId, date).getOrThrow()
    }
}