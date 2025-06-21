package com.ccxiaoji.feature.habit.data.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.core.database.dao.HabitDao
import com.ccxiaoji.core.database.entity.HabitEntity
import com.ccxiaoji.core.database.entity.HabitCheckInEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Test

class HabitRepositoryImplTest {

    @MockK
    private lateinit var habitDao: HabitDao
    
    private lateinit var habitRepository: HabitRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        habitRepository = HabitRepositoryImpl(habitDao)
    }

    @Test
    fun `getHabits should return habits from dao`() = runTest {
        // Given
        val habitEntities = listOf(
            mockk<HabitEntity>(),
            mockk<HabitEntity>()
        )
        coEvery { habitDao.getAllHabits() } returns flowOf(habitEntities)

        // When
        val result = habitRepository.getHabits().first()

        // Then
        assertThat(result).hasSize(2)
        coVerify(exactly = 1) { habitDao.getAllHabits() }
    }

    @Test
    fun `createHabit should insert habit and return success`() = runTest {
        // Given
        val title = "晨跑"
        val description = "每天早上跑步30分钟"
        val period = "daily"
        val target = 1
        val color = "#FF5722"
        val icon = "🏃"
        
        coEvery { habitDao.insertHabit(any()) } returns Unit

        // When
        val result = habitRepository.createHabit(
            title, description, period, target, color, icon
        )

        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        val habit = (result as BaseResult.Success).data
        assertThat(habit.title).isEqualTo(title)
        assertThat(habit.period).isEqualTo(period)
        assertThat(habit.target).isEqualTo(target)
        coVerify(exactly = 1) { habitDao.insertHabit(any()) }
    }

    @Test
    fun `createHabit should return error when dao fails`() = runTest {
        // Given
        val title = "晨跑"
        val description = "每天早上跑步30分钟"
        val period = "daily"
        val target = 1
        val color = "#FF5722"
        val icon = "🏃"
        val exception = Exception("数据库错误")
        
        coEvery { habitDao.insertHabit(any()) } throws exception

        // When
        val result = habitRepository.createHabit(
            title, description, period, target, color, icon
        )

        // Then
        assertThat(result).isInstanceOf(BaseResult.Error::class.java)
        val error = (result as BaseResult.Error).exception
        assertThat(error.message).contains("数据库错误")
    }

    @Test
    fun `checkInHabit should insert check-in record`() = runTest {
        // Given
        val habitId = "habit123"
        val note = "完成了今天的晨跑"
        
        coEvery { habitDao.insertCheckIn(any()) } returns Unit
        coEvery { habitDao.incrementCompletedCount(habitId) } returns Unit
        coEvery { habitDao.updateLastCompletedAt(eq(habitId), any()) } returns Unit
        coEvery { habitDao.updateStreak(eq(habitId), any(), any()) } returns Unit

        // When
        val result = habitRepository.checkInHabit(habitId, note)

        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        val checkIn = (result as BaseResult.Success).data
        assertThat(checkIn.habitId).isEqualTo(habitId)
        assertThat(checkIn.note).isEqualTo(note)
        
        coVerify(exactly = 1) { habitDao.insertCheckIn(any()) }
        coVerify(exactly = 1) { habitDao.incrementCompletedCount(habitId) }
        coVerify(exactly = 1) { habitDao.updateLastCompletedAt(eq(habitId), any()) }
    }

    @Test
    fun `deleteHabit should delete habit from dao`() = runTest {
        // Given
        val habitId = "habit123"
        
        coEvery { habitDao.deleteHabit(habitId) } returns Unit

        // When
        val result = habitRepository.deleteHabit(habitId)

        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        coVerify(exactly = 1) { habitDao.deleteHabit(habitId) }
    }

    @Test
    fun `deleteHabit should return error when dao fails`() = runTest {
        // Given
        val habitId = "habit123"
        val exception = Exception("删除失败")
        
        coEvery { habitDao.deleteHabit(habitId) } throws exception

        // When
        val result = habitRepository.deleteHabit(habitId)

        // Then
        assertThat(result).isInstanceOf(BaseResult.Error::class.java)
        val error = (result as BaseResult.Error).exception
        assertThat(error.message).contains("删除失败")
    }
}