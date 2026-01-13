package com.ccxiaoji.feature.schedule.presentation.schedule

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.uikit.LocalAnimatedVisibilityScope
import com.ccxiaoji.feature.schedule.presentation.uikit.LocalSharedTransitionScope
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleDesignSpecs
import com.ccxiaoji.feature.schedule.presentation.uikit.SharedElementKeys
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleTopAppBar
import com.ccxiaoji.feature.schedule.presentation.uikit.ShiftRadioRow
import com.ccxiaoji.feature.schedule.presentation.viewmodel.ScheduleEditViewModel
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 排班编辑界面 - 使用 UI Kit 组件
 */
@OptIn(ExperimentalSharedTransitionApi::class)
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
    val selectedShift by viewModel.selectedShift.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 共享元素转场 Scope
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    // Predictive Back 手势状态
    val predictiveBackProgress = remember { Animatable(0f) }
    val scale by remember { derivedStateOf { 1f - (predictiveBackProgress.value * 0.1f) } }
    val alpha by remember { derivedStateOf { 1f - (predictiveBackProgress.value * 0.3f) } }

    // Predictive Back Handler (Android 14+)
    PredictiveBackHandler { backEvent ->
        try {
            backEvent.collect { event ->
                predictiveBackProgress.snapTo(event.progress)
            }
            // 手势完成，执行返回
            predictiveBackProgress.animateTo(
                targetValue = 1f,
                animationSpec = ScheduleDesignSpecs.Motion.predictiveBackCommitSpec()
            )
            onNavigateBack()
        } catch (e: CancellationException) {
            // 手势取消，重置状态（使用 snapTo 更安全）
            predictiveBackProgress.snapTo(0f)
        } catch (e: Exception) {
            // 其他异常时重置状态
            predictiveBackProgress.snapTo(0f)
        }
    }

    // 初始化加载当前日期的排班
    LaunchedEffect(selectedDate) {
        viewModel.loadScheduleForDate(selectedDate)
    }

    // 共享元素修饰符（用于日期头部区域）
    val sharedDateModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(
                    key = SharedElementKeys.dateContainer(selectedDate.toEpochDay())
                ),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ ->
                    ScheduleDesignSpecs.Motion.sharedElementSpec()
                }
            )
        }
    } else {
        Modifier
    }

    Scaffold(
        modifier = Modifier
            .scale(scale)
            .alpha(alpha),
        topBar = {
            ScheduleTopAppBar(
                title = stringResource(
                    R.string.schedule_edit_title_with_date,
                    selectedDate.format(DateTimeFormatter.ofPattern(stringResource(R.string.schedule_calendar_date_format_full)))
                ),
                onNavigationClick = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveSchedule(selectedDate)
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(R.string.schedule_save)
                        )
                    }
                }
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
            // 日期头部卡片（共享元素目标）
            Box(
                modifier = Modifier
                    .then(sharedDateModifier)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(ScheduleDesignSpecs.Corners.SectionCard))
                    .background(ScheduleDesignSpecs.ModuleColors.PrimaryContainer)
                    .padding(DesignTokens.Spacing.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedDate.format(
                        DateTimeFormatter.ofPattern(
                            stringResource(R.string.schedule_calendar_date_format_full)
                        )
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ScheduleDesignSpecs.ModuleColors.OnPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))

            // 班次列表 - 直接展示选项，选中项已通过 RadioButton 高亮
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                // 添加"休息"选项
                item {
                    ShiftRadioRow(
                        shift = null,
                        selected = selectedShift == null,
                        onSelect = { viewModel.selectShift(null) }
                    )
                }

                // 班次列表
                items(shifts) { shift ->
                    ShiftRadioRow(
                        shift = shift,
                        selected = selectedShift?.id == shift.id,
                        onSelect = { viewModel.selectShift(shift) }
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