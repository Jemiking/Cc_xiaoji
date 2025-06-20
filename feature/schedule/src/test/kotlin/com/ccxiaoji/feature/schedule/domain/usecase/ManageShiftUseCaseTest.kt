package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import java.time.LocalTime
import org.junit.Before
import org.junit.Test

class ManageShiftUseCaseTest {

    @MockK
    private lateinit var scheduleRepository: ScheduleRepository

    private lateinit var manageShiftUseCase: ManageShiftUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        // 假设ManageShiftUseCase存在
        manageShiftUseCase = ManageShiftUseCase(scheduleRepository)
    }

    @Test
    fun `创建新班次成功`() = runTest {
        // Given
        val newShift = Shift(
            id = 0L, // 新班次ID为0
            name = "早班",
            color = 0xFFFF5722.toInt(),
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 0),
            isActive = true
        )
        coEvery { scheduleRepository.isShiftNameExists("早班") } returns false
        coEvery { scheduleRepository.createShift(any()) } returns 1L

        // When
        val result = manageShiftUseCase.createShift(newShift)

        // Then
        assertThat(result).isEqualTo(1L)
        coVerify(exactly = 1) { scheduleRepository.isShiftNameExists("早班") }
        coVerify(exactly = 1) { scheduleRepository.createShift(newShift) }
    }

    @Test
    fun `创建班次时名称已存在抛出异常`() = runTest {
        // Given
        val newShift = Shift(
            id = 0L,
            name = "早班",
            color = 0xFFFF5722.toInt(),
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 0),
            isActive = true
        )
        coEvery { scheduleRepository.isShiftNameExists("早班") } returns true

        // When & Then
        try {
            manageShiftUseCase.createShift(newShift)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: ShiftNameExistsException) {
            assertThat(e.message).contains("班次名称 '早班' 已存在")
        }
        coVerify(exactly = 1) { scheduleRepository.isShiftNameExists("早班") }
        coVerify(exactly = 0) { scheduleRepository.createShift(any()) }
    }

    @Test
    fun `更新现有班次成功`() = runTest {
        // Given
        val oldShift = Shift(
            id = 1L,
            name = "早班",
            color = 0xFFFF5722.toInt(),
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 0),
            isActive = true
        )
        val updatedShift = oldShift.copy(
            name = "早班（更新）",
            color = 0xFF4CAF50.toInt(),
            startTime = LocalTime.of(7, 30),
            endTime = LocalTime.of(15, 30)
        )
        coEvery { scheduleRepository.getShiftById(1L) } returns oldShift
        coEvery { scheduleRepository.isShiftNameExists("早班（更新）", 1L) } returns false
        coEvery { scheduleRepository.updateShift(updatedShift) } returns Unit

        // When
        manageShiftUseCase.updateShift(updatedShift)

        // Then
        coVerify(exactly = 1) { scheduleRepository.getShiftById(1L) }
        coVerify(exactly = 1) { scheduleRepository.isShiftNameExists("早班（更新）", 1L) }
        coVerify(exactly = 1) { scheduleRepository.updateShift(updatedShift) }
    }

    @Test
    fun `更新班次时不存在抛出异常`() = runTest {
        // Given
        val shift = Shift(
            id = 999L,
            name = "不存在的班次",
            color = 0xFFFF5722.toInt(),
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 0),
            isActive = true
        )
        coEvery { scheduleRepository.getShiftById(999L) } returns null

        // When & Then
        try {
            manageShiftUseCase.updateShift(shift)
            assertThat(false).isTrue()
        } catch (e: ShiftNotFoundException) {
            assertThat(e.message).contains("班次不存在")
        }
        coVerify(exactly = 1) { scheduleRepository.getShiftById(999L) }
        coVerify(exactly = 0) { scheduleRepository.updateShift(any()) }
    }

    @Test
    fun `更新班次时名称重复抛出异常`() = runTest {
        // Given
        val oldShift = Shift(
            id = 1L,
            name = "早班",
            color = 0xFFFF5722.toInt(),
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 0),
            isActive = true
        )
        val updatedShift = oldShift.copy(name = "晚班")
        coEvery { scheduleRepository.getShiftById(1L) } returns oldShift
        coEvery { scheduleRepository.isShiftNameExists("晚班", 1L) } returns true

        // When & Then
        try {
            manageShiftUseCase.updateShift(updatedShift)
            assertThat(false).isTrue()
        } catch (e: ShiftNameExistsException) {
            assertThat(e.message).contains("班次名称 '晚班' 已被使用")
        }
        coVerify(exactly = 1) { scheduleRepository.getShiftById(1L) }
        coVerify(exactly = 1) { scheduleRepository.isShiftNameExists("晚班", 1L) }
        coVerify(exactly = 0) { scheduleRepository.updateShift(any()) }
    }

    @Test
    fun `删除班次`() = runTest {
        // Given
        val shiftId = 1L
        coEvery { scheduleRepository.deleteShift(shiftId) } returns Unit

        // When
        manageShiftUseCase.deleteShift(shiftId)

        // Then
        coVerify(exactly = 1) { scheduleRepository.deleteShift(shiftId) }
    }
}