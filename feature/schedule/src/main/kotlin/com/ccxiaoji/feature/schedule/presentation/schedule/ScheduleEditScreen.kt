package com.ccxiaoji.feature.schedule.presentation.schedule

import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.schedule.presentation.viewmodel.ScheduleEditViewModel
import com.ccxiaoji.feature.schedule.presentation.schedule.components.*
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 排班编辑界面 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditScreen(
    date: String?,
    onNavigateBack: () -> Unit,
    viewModel: ScheduleEditViewModel = hiltViewModel()
) {
    val selectedDate = remember(date) {
        date?.let { LocalDate.parse(it) } ?: LocalDate.now()
    }
    
    val shifts by viewModel.shifts.collectAsState()
    val currentSchedule by viewModel.currentSchedule.collectAsState()
    val selectedShift by viewModel.selectedShift.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 初始化加载当前日期的排班
    LaunchedEffect(selectedDate) {
        viewModel.loadScheduleForDate(selectedDate)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(
                            R.string.schedule_edit_title_with_date, 
                            selectedDate.format(DateTimeFormatter.ofPattern(stringResource(R.string.schedule_calendar_date_format_full)))
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.schedule_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveSchedule(selectedDate)
                            onNavigateBack()
                        },
                        enabled = selectedShift != null
                    ) {
                        Icon(
                            Icons.Default.Check, 
                            contentDescription = stringResource(R.string.schedule_save)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(DesignTokens.Spacing.medium)
        ) {
            // 当前排班信息
            currentSchedule?.let { schedule ->
                CurrentScheduleCard(
                    schedule = schedule,
                    modifier = Modifier.padding(bottom = DesignTokens.Spacing.medium)
                )
            }
            
            // 班次选择提示
            Text(
                text = if (currentSchedule == null) {
                    stringResource(R.string.schedule_edit_select_shift)
                } else {
                    stringResource(R.string.schedule_edit_change_shift)
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = DesignTokens.Spacing.small)
            )
            
            // 班次列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                // 添加"休息"选项
                item {
                    ShiftSelectCard(
                        shift = null,
                        isSelected = selectedShift == null && currentSchedule == null,
                        onClick = { viewModel.selectShift(null) }
                    )
                }
                
                // 班次列表
                items(shifts) { shift ->
                    ShiftSelectCard(
                        shift = shift,
                        isSelected = selectedShift?.id == shift.id,
                        onClick = { viewModel.selectShift(shift) }
                    )
                }
            }
        }
    }
    
    // 错误提示
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
}