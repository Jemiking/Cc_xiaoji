package com.ccxiaoji.feature.habit.domain.usecase

import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 搜索习惯用例
 * 在习惯标题和描述中搜索
 */
class SearchHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    /**
     * 搜索习惯
     * @param query 搜索关键词
     * @return 匹配的习惯列表
     */
    operator fun invoke(query: String): Flow<List<HabitWithStreak>> {
        return if (query.isBlank()) {
            repository.getHabitsWithStreaks()
        } else {
            repository.getHabitsWithStreaks().map { habits ->
                habits.filter { habitWithStreak ->
                    habitWithStreak.habit.title.contains(query, ignoreCase = true) ||
                    habitWithStreak.habit.description?.contains(query, ignoreCase = true) == true
                }
            }
        }
    }
}