package com.ccxiaoji.feature.habit.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.habit.R
import com.ccxiaoji.feature.habit.presentation.viewmodel.HabitViewModel
import com.ccxiaoji.feature.habit.presentation.components.HabitCard
import com.ccxiaoji.feature.habit.presentation.components.SimpleHabitStatistics
import com.ccxiaoji.feature.habit.presentation.screen.demo.HabitRedesignDemo
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(
    viewModel: HabitViewModel = hiltViewModel(),
    onNavigateToAddHabit: () -> Unit = {},
    onNavigateToEditHabit: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDemo by remember { mutableStateOf(false) }
    
    var showStatistics by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 显示错误/成功消息
    LaunchedEffect(errorState) {
        errorState?.let { error ->
            snackbarHostState.showSnackbar(
                message = error.message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // 与记账模块保持一致的固定宽度
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                Text(
                    text = stringResource(R.string.nav_habit),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.habit_show_list)) },
                    selected = !showStatistics && !showDemo,
                    onClick = {
                        showDemo = false
                        showStatistics = false
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.habit_show_stats)) },
                    selected = showStatistics && !showDemo,
                    onClick = {
                        showDemo = false
                        showStatistics = true
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.habit_demo_preview)) },
                    selected = showDemo,
                    onClick = {
                        showStatistics = false
                        showDemo = true
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Visibility, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier,
            topBar = if (showTopBar) {
                {
                    TopAppBar(
                        title = { 
                            Text(
                                text = stringResource(R.string.nav_habit),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.habit_open_drawer))
                            }
                        },
                        actions = { /* 右上角功能已迁移至侧边栏 */ }
                    )
                }
            } else {
                {} // 空的Composable
            },
            floatingActionButton = {
                if (!showStatistics && !showDemo) {
                    FloatingActionButton(
                        onClick = onNavigateToAddHabit,
                        containerColor = DesignTokens.BrandColors.Habit,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 1.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = stringResource(R.string.add_habit),
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showDemo) {
                HabitRedesignDemo(modifier = Modifier.fillMaxSize())
            } else if (showStatistics) {
                // 统计视图 - 简化版
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(DesignTokens.Spacing.medium)
                ) {
                    SimpleHabitStatistics(
                        habits = uiState.habits,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // 列表视图
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text(stringResource(R.string.habit_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.habit_search)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.habit_clear))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )
            
            if (uiState.habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            stringResource(R.string.habit_no_match)
                        } else {
                            stringResource(R.string.habit_no_habits)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.habits) { habitWithStreak ->
                        HabitCard(
                            habitWithStreak = habitWithStreak,
                            isCheckedToday = uiState.checkedToday.contains(habitWithStreak.habit.id),
                            onCheckIn = { viewModel.checkInHabit(habitWithStreak.habit.id) },
                            onEdit = { onNavigateToEditHabit(habitWithStreak.habit.id) },
                            onDelete = { viewModel.deleteHabit(habitWithStreak.habit.id) }
                        )
                    }
                }
            }
            }
        }
        }
    }
}
