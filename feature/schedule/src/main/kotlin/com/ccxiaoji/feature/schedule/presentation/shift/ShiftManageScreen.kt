package com.ccxiaoji.feature.schedule.presentation.shift

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.viewmodel.ShiftViewModel
import com.ccxiaoji.feature.schedule.presentation.shift.components.*
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleTopAppBar
import com.ccxiaoji.feature.schedule.presentation.uikit.ShiftRow
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleCard
import com.ccxiaoji.ui.components.FlatFAB
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.coroutines.launch

/**
 * 班次管理界面 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftManageScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditShift: (Long?) -> Unit = {},
    viewModel: ShiftViewModel = hiltViewModel()
) {
    val shifts by viewModel.shifts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 显示消息
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearMessages()
            }
        }
        uiState.successMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearMessages()
            }
        }
    }
    
    Scaffold(
        topBar = {
            ScheduleTopAppBar(
                title = stringResource(R.string.schedule_shift_manage_title),
                onNavigationClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FlatFAB(
                onClick = { onNavigateToEditShift(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.schedule_shift_add)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (shifts.isEmpty()) {
                // 空状态
                ShiftEmptyState(
                    modifier = Modifier.padding(paddingValues)
                )
            } else {
                // 班次列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(DesignTokens.Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    items(shifts, key = { it.id }) { shift ->
                        ScheduleCard {
                            ShiftRow(
                                shift = shift,
                                selected = false,
                                onClick = { onNavigateToEditShift(shift.id) },
                                trailing = {
                                    // 仅保留删除按钮，点击行本身即可编辑
                                    IconButton(
                                        onClick = { viewModel.deleteShift(shift) }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.schedule_delete),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // 加载指示器
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}