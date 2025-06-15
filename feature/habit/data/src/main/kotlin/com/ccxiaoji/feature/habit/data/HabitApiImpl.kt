package com.ccxiaoji.feature.habit.data

import com.ccxiaoji.feature.habit.api.HabitApi
import com.ccxiaoji.feature.habit.api.HabitItem
import com.ccxiaoji.feature.habit.api.HabitStatistics
import com.ccxiaoji.feature.habit.api.HabitNavigator
import com.ccxiaoji.feature.habit.api.ImportHabitsResult
import com.ccxiaoji.feature.habit.data.repository.HabitRepository
import com.ccxiaoji.feature.habit.domain.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// TODO: 编译验证 - 需要执行 ./gradlew :feature:habit:compileDebugKotlin
@Singleton
class HabitApiImpl @Inject constructor(
    private val habitRepository: HabitRepository,
    private val habitNavigator: HabitNavigator
) : HabitApi {
    
    override suspend fun getTodayHabitStatistics(): HabitStatistics {
        val habits = habitRepository.getHabitsWithStreaks().first()
        val todayCheckedCount = habitRepository.getTodayCheckedHabitsCount().first()
        val longestStreak = habits.maxByOrNull { it.longestStreak }?.longestStreak ?: 0
        
        return HabitStatistics(
            totalHabits = habits.size,
            todayCheckedCount = todayCheckedCount,
            todayUncompletedCount = habits.size - todayCheckedCount,
            longestStreak = longestStreak
        )
    }
    
    override suspend fun getAllHabits(): List<HabitItem> {
        return habitRepository.getHabitsWithStreaks().first().map { habitWithStreak ->
            HabitItem(
                id = habitWithStreak.habit.id,
                title = habitWithStreak.habit.title,
                description = habitWithStreak.habit.description,
                period = habitWithStreak.habit.period,
                target = habitWithStreak.habit.target,
                currentStreak = habitWithStreak.currentStreak,
                checkedToday = false // TODO: 需要实现检查今日是否已打卡的逻辑
            )
        }
    }
    
    override fun getUncompletedHabitCount(): Flow<Int> {
        return habitRepository.getActiveHabitsCount().map { total ->
            val checked = habitRepository.getTodayCheckedHabitsCount().first()
            total - checked
        }
    }
    
    override fun getTodayCheckedCount(): Flow<Int> {
        return habitRepository.getTodayCheckedHabitsCount()
    }
    
    override suspend fun getLongestStreak(): Int {
        val habits = habitRepository.getHabitsWithStreaks().first()
        return habits.maxByOrNull { it.longestStreak }?.longestStreak ?: 0
    }
    
    override fun navigateToHabitList() {
        habitNavigator.navigateToHabitList()
    }
    
    override fun navigateToQuickCheckIn() {
        habitNavigator.navigateToQuickCheckIn()
    }
    
    override suspend fun importHabits(
        habits: List<Map<String, Any>>,
        conflictResolution: String
    ): ImportHabitsResult {
        var successCount = 0
        var skippedCount = 0
        var failedCount = 0
        val errors = mutableListOf<String>()
        
        habits.forEach { habitData ->
            try {
                // 解析习惯数据
                val title = habitData["title"] as? String
                if (title.isNullOrBlank()) {
                    failedCount++
                    errors.add("习惯缺少标题")
                    return@forEach
                }
                
                val id = habitData["id"] as? String ?: UUID.randomUUID().toString()
                val description = habitData["description"] as? String
                val period = habitData["period"] as? String ?: "daily"
                val target = (habitData["target"] as? Number)?.toInt() ?: 1
                val color = habitData["color"] as? String ?: "#4CAF50"
                val icon = habitData["icon"] as? String
                val createdAt = (habitData["createdAt"] as? Number)?.let { 
                    Instant.fromEpochMilliseconds(it.toLong()) 
                } ?: Instant.fromEpochMilliseconds(System.currentTimeMillis())
                val updatedAt = (habitData["updatedAt"] as? Number)?.let { 
                    Instant.fromEpochMilliseconds(it.toLong()) 
                } ?: createdAt
                
                // 检查习惯是否已存在
                val existingHabit = try {
                    habitRepository.getHabitById(id)
                } catch (e: Exception) {
                    null
                }
                
                when (conflictResolution) {
                    "SKIP" -> {
                        if (existingHabit != null) {
                            skippedCount++
                            return@forEach
                        }
                    }
                    "REPLACE" -> {
                        // 如果存在则删除旧习惯
                        if (existingHabit != null) {
                            habitRepository.deleteHabit(id)
                        }
                    }
                    "CREATE_NEW" -> {
                        // 总是创建新习惯，使用新ID
                        habitRepository.createHabit(
                            title = title,
                            description = description,
                            period = period,
                            target = target,
                            color = color,
                            icon = icon
                        )
                        successCount++
                        return@forEach
                    }
                }
                
                // 创建习惯
                habitRepository.createHabit(
                    title = title,
                    description = description,
                    period = period,
                    target = target,
                    color = color,
                    icon = icon
                )
                successCount++
                
            } catch (e: Exception) {
                failedCount++
                errors.add("导入习惯失败: ${e.message}")
            }
        }
        
        return ImportHabitsResult(
            totalCount = habits.size,
            successCount = successCount,
            skippedCount = skippedCount,
            failedCount = failedCount,
            errors = errors
        )
    }
}