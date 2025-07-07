package com.ccxiaoji.feature.habit.domain.usecase

import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import javax.inject.Inject

/**
 * 根据ID获取习惯用例
 */
class GetHabitByIdUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String): HabitWithStreak? {
        return repository.getHabitById(habitId)
    }
}