package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import javax.inject.Inject

/**
 * 管理班次用例
 * 处理班次的创建、更新、删除等操作
 */
class ManageShiftUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    
    /**
     * 创建新班次
     * @return 班次ID
     * @throws ShiftNameExistsException 如果班次名称已存在
     */
    suspend fun createShift(shift: Shift): Long {
        if (repository.isShiftNameExists(shift.name)) {
            throw ShiftNameExistsException("班次名称 '${shift.name}' 已存在")
        }
        return repository.createShift(shift)
    }
    
    /**
     * 更新班次
     * @throws ShiftNameExistsException 如果新名称与其他班次重复
     * @throws ShiftNotFoundException 如果班次不存在
     */
    suspend fun updateShift(shift: Shift) {
        val existingShift = repository.getShiftById(shift.id)
            ?: throw ShiftNotFoundException("班次不存在")
        
        if (shift.name != existingShift.name && 
            repository.isShiftNameExists(shift.name, shift.id)) {
            throw ShiftNameExistsException("班次名称 '${shift.name}' 已被使用")
        }
        
        repository.updateShift(shift)
    }
    
    /**
     * 删除班次
     * @throws ShiftInUseException 如果班次正在被使用
     */
    suspend fun deleteShift(shiftId: Long) {
        // TODO: 检查班次是否正在被使用
        repository.deleteShift(shiftId)
    }
}

/**
 * 班次名称已存在异常
 */
class ShiftNameExistsException(message: String) : Exception(message)

/**
 * 班次不存在异常
 */
class ShiftNotFoundException(message: String) : Exception(message)

/**
 * 班次正在使用中异常
 */
class ShiftInUseException(message: String) : Exception(message)