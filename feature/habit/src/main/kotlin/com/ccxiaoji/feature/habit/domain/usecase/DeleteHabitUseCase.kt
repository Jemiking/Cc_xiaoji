package com.ccxiaoji.feature.habit.domain.usecase

import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import javax.inject.Inject

/**
 * 删除习惯用例
 */
class DeleteHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    /**
     * 删除指定的习惯
     * @param habitId 习惯ID
     */
    suspend operator fun invoke(habitId: String) {
        if (habitId.isBlank()) {
            throw DomainException.ValidationException("习惯ID不能为空")
        }
        repository.deleteHabit(habitId).getOrThrow()
    }
}