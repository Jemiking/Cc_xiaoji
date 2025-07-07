package com.ccxiaoji.feature.schedule.presentation.shift

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.viewmodel.ShiftViewModel
import com.ccxiaoji.feature.schedule.presentation.shift.components.*
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
    val editingShift by viewModel.editingShift.collectAsState()
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
            TopAppBar(
                title = { Text(stringResource(R.string.schedule_shift_manage_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.schedule_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                    items(shifts) { shift ->
                        ShiftCard(
                            shift = shift,
                            onEdit = { onNavigateToEditShift(shift.id) },
                            onDelete = { viewModel.deleteShift(shift) }
                        )
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