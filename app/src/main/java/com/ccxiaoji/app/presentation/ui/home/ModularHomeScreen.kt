package com.ccxiaoji.app.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.app.presentation.ui.home.components.*
import com.ccxiaoji.app.presentation.ui.navigation.defaultModules
import com.ccxiaoji.app.presentation.viewmodel.HomeViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModularHomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hiddenModules by viewModel.hiddenModules.collectAsState(initial = emptySet())
    val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "CC小记",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: 通知功能 */ }) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text("3")
                        }
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "通知"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 欢迎语
            item {
                Column {
                    Text(
                        text = getGreeting(currentTime.hour),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currentTime.monthNumber}月${currentTime.dayOfMonth}日 ${getDayOfWeek(currentTime.dayOfWeek.value)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 数据概览
            item {
                QuickStatsCard(
                    todayIncome = uiState.todayIncome,
                    todayExpense = uiState.todayExpense,
                    pendingTasks = uiState.todayTasks,
                    completedTasks = uiState.todayCompletedTasks,
                    habitProgress = if (uiState.activeHabits > 0) 
                        uiState.todayCheckedHabits.toFloat() / uiState.activeHabits 
                    else 0f
                )
            }
            
            // 功能模块标题
            item {
                Text(
                    text = "功能模块",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // 模块网格
            item {
                val visibleModules = defaultModules.filter { module ->
                    module.id !in hiddenModules
                }
                ModuleGrid(
                    modules = visibleModules,
                    onModuleClick = { module ->
                        navController.navigate(module.route)
                    }
                )
            }
            
            // 底部空间
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

private fun getGreeting(hour: Int): String {
    return when (hour) {
        in 5..11 -> "早上好"
        in 12..13 -> "中午好"
        in 14..17 -> "下午好"
        in 18..22 -> "晚上好"
        else -> "夜深了"
    }
}

private fun getDayOfWeek(day: Int): String {
    return when (day) {
        1 -> "星期一"
        2 -> "星期二"
        3 -> "星期三"
        4 -> "星期四"
        5 -> "星期五"
        6 -> "星期六"
        7 -> "星期日"
        else -> ""
    }
}