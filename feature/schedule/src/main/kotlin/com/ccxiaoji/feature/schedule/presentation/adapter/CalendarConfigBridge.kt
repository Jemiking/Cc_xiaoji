package com.ccxiaoji.feature.schedule.presentation.adapter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.ccxiaoji.feature.schedule.presentation.demo.CalendarDemoApi
import com.ccxiaoji.feature.schedule.presentation.demo.parts.DisplayMode
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewModel

/**
 * 日历配置桥接器
 * 负责在现有系统和Demo组件之间进行状态同步和配置转换
 */
class CalendarConfigBridge(
    private val viewModel: CalendarViewModel
) {
    
    // 内部状态管理
    private var _displayMode by mutableStateOf(
        CalendarDemoApi.viewModeToDisplayMode(viewModel.viewMode.value)
    )
    
    val displayMode: DisplayMode get() = _displayMode
    
    /**
     * 处理Demo组件的展开请求
     * 同步到ViewModel的ViewMode
     */
    fun onRequestExpand() {
        _displayMode = DisplayMode.Expanded
        // 同步到现有ViewModel
        if (viewModel.viewMode.value == CalendarViewMode.COMPACT) {
            viewModel.toggleViewMode()
        }
    }
    
    /**
     * 处理Demo组件的收缩请求
     * 同步到ViewModel的ViewMode
     */
    fun onRequestCompact() {
        _displayMode = DisplayMode.Compact
        // 同步到现有ViewModel
        if (viewModel.viewMode.value == CalendarViewMode.COMFORTABLE) {
            viewModel.toggleViewMode()
        }
    }
    
    /**
     * 从ViewModel同步状态到Bridge
     * 保持双向数据绑定
     */
    fun syncFromViewModel(viewMode: CalendarViewMode) {
        val expectedDisplayMode = CalendarDemoApi.viewModeToDisplayMode(viewMode)
        if (_displayMode != expectedDisplayMode) {
            _displayMode = expectedDisplayMode
        }
    }
    
    companion object {
        /**
         * 创建配置桥接器的Composable工厂函数
         */
        @Composable
        fun create(viewModel: CalendarViewModel): CalendarConfigBridge {
            return remember(viewModel) {
                CalendarConfigBridge(viewModel)
            }
        }
    }
}

/**
 * 交互事件桥接器
 * 负责将Demo组件的交互事件转发到现有的ViewModel
 */
class CalendarInteractionBridge(
    private val viewModel: CalendarViewModel,
    private val configBridge: CalendarConfigBridge,
    private val onNavigateToScheduleEdit: (java.time.LocalDate) -> Unit,
    private val onDeleteSchedule: (java.time.LocalDate) -> Unit = { date ->
        viewModel.deleteSchedule(date)
    }
) {
    
    /**
     * 处理日期选择
     */
    fun onDateSelected(date: java.time.LocalDate) {
        viewModel.selectDate(date)
    }
    
    /**
     * 处理日期长按（已不再使用快速选择，改为无操作或与点击一致）
     */
    fun onDateLongClick(date: java.time.LocalDate) {
        // 简化处理：与点击一致，仅选中日期
        viewModel.selectDate(date)
    }
    
    /**
     * 处理编辑请求
     */
    fun onEditSchedule(date: java.time.LocalDate) {
        onNavigateToScheduleEdit(date)
    }
    
    /**
     * 处理删除请求
     */
    fun onDeleteSchedule(date: java.time.LocalDate) {
        onDeleteSchedule(date)
    }
    
    /**
     * 处理展开模式请求
     */
    fun onRequestExpand() {
        configBridge.onRequestExpand()
    }
    
    /**
     * 处理收缩模式请求
     */
    fun onRequestCompact() {
        configBridge.onRequestCompact()
    }
    
    companion object {
        /**
         * 创建交互桥接器的工厂函数
         */
        @Composable
        fun create(
            viewModel: CalendarViewModel,
            configBridge: CalendarConfigBridge,
            onNavigateToScheduleEdit: (java.time.LocalDate) -> Unit
        ): CalendarInteractionBridge {
            return remember(viewModel, configBridge, onNavigateToScheduleEdit) {
                CalendarInteractionBridge(
                    viewModel = viewModel,
                    configBridge = configBridge,
                    onNavigateToScheduleEdit = onNavigateToScheduleEdit
                )
            }
        }
    }
}
