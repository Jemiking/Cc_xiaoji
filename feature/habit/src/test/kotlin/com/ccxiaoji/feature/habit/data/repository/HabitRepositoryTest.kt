package com.ccxiaoji.feature.habit.data.repository

import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.habit.data.local.dao.HabitDao
import com.ccxiaoji.feature.habit.data.local.entity.HabitEntity
import com.ccxiaoji.feature.habit.data.local.entity.HabitRecordEntity
import com.ccxiaoji.shared.user.api.UserApi
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import org.junit.Before
import org.junit.Test

class HabitRepositoryTest {

    @MockK
    private lateinit var habitDao: HabitDao

    @MockK
    private lateinit var userApi: UserApi

    private lateinit var habitRepository: HabitRepositoryImpl

    private val testUserId = "test-user-123"

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { userApi.getCurrentUserId() } returns testUserId
        habitRepository = HabitRepositoryImpl(habitDao, userApi)
    }

    @Test
    fun `获取用户的所有习惯`() = runTest {
        // Given
        val habitEntities = createTestHabitEntities()
        coEvery { habitDao.getHabitsByUser(testUserId) } returns flowOf(habitEntities)

        // When
        val result = habitRepository.getHabits().first()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].title).isEqualTo("早起")
        assertThat(result[1].title).isEqualTo("运动")
        coVerify(exactly = 1) { habitDao.getHabitsByUser(testUserId) }
    }

    @Test
    fun `获取活跃习惯数量`() = runTest {
        // Given
        val habitEntities = createTestHabitEntities()
        coEvery { habitDao.getHabitsByUser(testUserId) } returns flowOf(habitEntities)

        // When
        val result = habitRepository.getActiveHabitsCount().first()

        // Then
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `获取今日已打卡习惯数量`() = runTest {
        // Given
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayStart = today.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val todayRecords = listOf(
            createTestHabitRecordEntity("1", "habit1", todayStart),
            createTestHabitRecordEntity("2", "habit2", todayStart),
            createTestHabitRecordEntity("3", "habit1", todayStart) // 同一个习惯的多次记录
        )
        
        coEvery { 
            habitDao.getUserHabitRecordsByDateRange(testUserId, todayStart, todayStart) 
        } returns flowOf(todayRecords)

        // When
        val result = habitRepository.getTodayCheckedHabitsCount().first()

        // Then
        assertThat(result).isEqualTo(2) // 去重后的习惯数
    }

    @Test
    fun `搜索习惯`() = runTest {
        // Given
        val query = "运动"
        val searchResults = listOf(
            createTestHabitEntity("1", "每日运动"),
            createTestHabitEntity("2", "运动30分钟")
        )
        coEvery { habitDao.searchHabits(testUserId, "%$query%") } returns flowOf(searchResults)

        // When
        val result = habitRepository.searchHabits(query).first()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result.all { it.title.contains(query) }).isTrue()
        coVerify(exactly = 1) { habitDao.searchHabits(testUserId, "%$query%") }
    }

    @Test
    fun `创建新习惯`() = runTest {
        // Given
        val title = "冥想"
        val description = "每日冥想10分钟"
        val period = "daily"
        val target = 1
        val color = "#9C27B0"
        val icon = "🧘"
        
        val habitEntitySlot = slot<HabitEntity>()
        coEvery { habitDao.insertHabit(capture(habitEntitySlot)) } returns Unit

        // When
        val result = habitRepository.createHabit(
            title = title,
            description = description,
            period = period,
            target = target,
            color = color,
            icon = icon
        )

        // Then
        assertThat(result.title).isEqualTo(title)
        assertThat(result.description).isEqualTo(description)
        assertThat(result.period).isEqualTo(period)
        assertThat(result.target).isEqualTo(target)
        assertThat(result.color).isEqualTo(color)
        assertThat(result.icon).isEqualTo(icon)
        
        val capturedEntity = habitEntitySlot.captured
        assertThat(capturedEntity.userId).isEqualTo(testUserId)
        assertThat(capturedEntity.syncStatus).isEqualTo(SyncStatus.PENDING_SYNC)
        coVerify(exactly = 1) { habitDao.insertHabit(any()) }
    }

    @Test
    fun `更新习惯信息`() = runTest {
        // Given
        val habitId = "habit-123"
        val existingHabit = createTestHabitEntity(habitId, "原标题")
        val newTitle = "新标题"
        val newDescription = "新描述"
        
        coEvery { habitDao.getHabitById(habitId) } returns existingHabit
        coEvery { habitDao.updateHabit(any()) } returns Unit

        // When
        habitRepository.updateHabit(
            habitId = habitId,
            title = newTitle,
            description = newDescription,
            period = "daily",
            target = 1,
            color = "#3A7AFE",
            icon = null
        )

        // Then
        coVerify(exactly = 1) { habitDao.getHabitById(habitId) }
        coVerify(exactly = 1) { habitDao.updateHabit(any()) }
    }

    @Test
    fun `习惯打卡 - 首次打卡`() = runTest {
        // Given
        val habitId = "habit-123"
        val date = LocalDate(2024, 1, 1)
        val recordDate = date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        coEvery { habitDao.getHabitRecordByDate(habitId, recordDate) } returns null
        
        val recordSlot = slot<HabitRecordEntity>()
        coEvery { habitDao.insertHabitRecord(capture(recordSlot)) } returns Unit

        // When
        habitRepository.checkInHabit(habitId, date)

        // Then
        val capturedRecord = recordSlot.captured
        assertThat(capturedRecord.habitId).isEqualTo(habitId)
        assertThat(capturedRecord.recordDate).isEqualTo(recordDate)
        assertThat(capturedRecord.count).isEqualTo(1)
        assertThat(capturedRecord.syncStatus).isEqualTo(SyncStatus.PENDING_SYNC)
        
        coVerify(exactly = 1) { habitDao.getHabitRecordByDate(habitId, recordDate) }
        coVerify(exactly = 1) { habitDao.insertHabitRecord(any()) }
    }

    @Test
    fun `习惯打卡 - 重复打卡增加计数`() = runTest {
        // Given
        val habitId = "habit-123"
        val date = LocalDate(2024, 1, 1)
        val recordDate = date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val existingRecord = createTestHabitRecordEntity("record-1", habitId, recordDate, count = 2)
        coEvery { habitDao.getHabitRecordByDate(habitId, recordDate) } returns existingRecord
        
        val recordSlot = slot<HabitRecordEntity>()
        coEvery { habitDao.updateHabitRecord(capture(recordSlot)) } returns Unit

        // When
        habitRepository.checkInHabit(habitId, date)

        // Then
        val updatedRecord = recordSlot.captured
        assertThat(updatedRecord.count).isEqualTo(3) // 原本2次，现在3次
        
        coVerify(exactly = 1) { habitDao.getHabitRecordByDate(habitId, recordDate) }
        coVerify(exactly = 1) { habitDao.updateHabitRecord(any()) }
        coVerify(exactly = 0) { habitDao.insertHabitRecord(any()) }
    }

    @Test
    fun `软删除习惯`() = runTest {
        // Given
        val habitId = "habit-123"
        coEvery { habitDao.softDeleteHabit(any(), any()) } returns Unit

        // When
        habitRepository.deleteHabit(habitId)

        // Then
        coVerify(exactly = 1) { habitDao.softDeleteHabit(habitId, any()) }
    }

    @Test
    fun `获取习惯连续打卡信息`() = runTest {
        // Given
        val habitEntities = listOf(createTestHabitEntity("habit1", "冥想"))
        coEvery { habitDao.getHabitsByUser(testUserId) } returns flowOf(habitEntities)
        coEvery { habitDao.getCurrentStreak(any(), any()) } returns 7
        coEvery { habitDao.getHabitRecordsByDateRangeSync(any(), any(), any()) } returns listOf()

        // When
        val result = habitRepository.getHabitsWithStreaks().first()

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].habit.title).isEqualTo("冥想")
        assertThat(result[0].currentStreak).isEqualTo(7)
    }

    private fun createTestHabitEntities(): List<HabitEntity> {
        return listOf(
            createTestHabitEntity("1", "早起"),
            createTestHabitEntity("2", "运动")
        )
    }

    private fun createTestHabitEntity(
        id: String,
        title: String
    ): HabitEntity {
        val now = System.currentTimeMillis()
        return HabitEntity(
            id = id,
            userId = testUserId,
            title = title,
            description = null,
            period = "daily",
            target = 1,
            color = "#3A7AFE",
            icon = null,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED,
            isDeleted = false
        )
    }

    private fun createTestHabitRecordEntity(
        id: String,
        habitId: String,
        recordDate: Long,
        count: Int = 1
    ): HabitRecordEntity {
        val now = System.currentTimeMillis()
        return HabitRecordEntity(
            id = id,
            habitId = habitId,
            recordDate = recordDate,
            count = count,
            note = null,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED,
            isDeleted = false
        )
    }
}