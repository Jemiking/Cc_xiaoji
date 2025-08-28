package com.ccxiaoji.feature.schedule.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewModel
import java.time.LocalDate

/**
 * 排班主页调试器主界面
 * 提供实时调节排班主页所有组件参数的功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDebugScreen(
    onNavigateBack: () -> Unit = {},
    navController: NavController? = null,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    // 调试参数状态
    var debugParams by remember { mutableStateOf(DefaultDebugParams.default) }
    
    // 面板显示状态
    var showControlPanel by remember { mutableStateOf(true) }
    
    // 检测屏幕方向和大小
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth > 600.dp
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "排班主页调试器",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 控制面板开关
                    IconButton(
                        onClick = { showControlPanel = !showControlPanel }
                    ) {
                        Icon(
                            if (showControlPanel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showControlPanel) "隐藏控制面板" else "显示控制面板"
                        )
                    }
                    
                    // 重置按钮
                    IconButton(
                        onClick = { debugParams = DefaultDebugParams.default }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "重置")
                    }
                    
                    // 导出参数按钮
                    IconButton(
                        onClick = { 
                            // TODO: 实现参数导出功能
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "导出参数")
                    }
                }
            )
        }
    ) { paddingValues ->
        
        if (isLandscape && isTablet) {
            // 平板横屏：左右分屏布局
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 左侧：预览区域
                Box(
                    modifier = Modifier
                        .weight(if (showControlPanel) 0.6f else 1f)
                        .fillMaxHeight()
                ) {
                    DebugPreviewArea(
                        params = debugParams,
                        navController = navController,
                        viewModel = viewModel
                    )
                }
                
                // 右侧：控制面板
                if (showControlPanel) {
                    Card(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                            .padding(start = 8.dp),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    ) {
                        DebugControlPanel(
                            params = debugParams,
                            onParamsChange = { debugParams = it }
                        )
                    }
                }
            }
        } else {
            // 手机或平板竖屏：上下布局
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 上部：预览区域
                Box(
                    modifier = Modifier
                        .weight(if (showControlPanel) 0.6f else 1f)
                        .fillMaxWidth()
                ) {
                    DebugPreviewArea(
                        params = debugParams,
                        navController = navController,
                        viewModel = viewModel
                    )
                }
                
                // 下部：控制面板
                if (showControlPanel) {
                    Card(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) {
                        DebugControlPanel(
                            params = debugParams,
                            onParamsChange = { debugParams = it }
                        )
                    }
                }
            }
        }
        
        // 浮动状态指示器
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.BottomStart
        ) {
            DebugStatusIndicator(
                params = debugParams,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * 调试预览区域
 */
@Composable
private fun DebugPreviewArea(
    params: DebugCalendarParams,
    navController: NavController?,
    viewModel: CalendarViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 背景网格（可选，帮助调试布局）
            DebugGrid(
                modifier = Modifier.fillMaxSize(),
                params = params
            )
            
            // 实际的排班页面
            DebugCalendarScreen(
                params = params,
                onNavigateToShiftManage = { /* 调试模式下不实际导航 */ },
                onNavigateToScheduleEdit = { /* 调试模式下不实际导航 */ },
                onNavigateToSchedulePattern = { /* 调试模式下不实际导航 */ },
                onNavigateToStatistics = { /* 调试模式下不实际导航 */ },
                onNavigateToSettings = { /* 调试模式下不实际导航 */ },
                onNavigateToDebug = { 
                    android.util.Log.d("CalendarDebugScreen", "Navigate to Debug - already in debug mode")
                },
                onNavigateToFlatDemo = { 
                    android.util.Log.d("CalendarDebugScreen", "Navigate to FlatDemo from debug")
                    navController?.navigate("calendar_flat_demo")
                },
                onNavigateToStyleDemo = { 
                    android.util.Log.d("CalendarDebugScreen", "Navigate to StyleDemo from debug")
                    navController?.navigate("style_demo")
                },
                viewModel = viewModel,
                navController = navController
            )
            
            // 调试标签层
            DebugLabels(
                params = params,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * 调试网格背景
 */
@Composable
private fun DebugGrid(
    modifier: Modifier = Modifier,
    params: DebugCalendarParams
) {
    // TODO: 可以添加网格线帮助调试布局
    Box(modifier = modifier)
}

/**
 * 调试标签层
 */
@Composable
private fun DebugLabels(
    params: DebugCalendarParams,
    modifier: Modifier = Modifier
) {
    // TODO: 可以添加组件边界和标签
    Box(modifier = modifier)
}

/**
 * 调试状态指示器
 */
@Composable
private fun DebugStatusIndicator(
    params: DebugCalendarParams,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "调试状态",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                "TopAppBar: ${params.topAppBar.height}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                "卡片圆角: ${params.statisticsCard.cornerRadius}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                "日期格子: ${params.calendarView.cellSize}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                "页面边距: ${params.layout.screenHorizontalPadding}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * 调试工具栏（快捷操作）
 */
@Composable
private fun DebugToolbar(
    params: DebugCalendarParams,
    onParamsChange: (DebugCalendarParams) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 快捷预设按钮
            FilterChip(
                onClick = { onParamsChange(DefaultDebugParams.default) },
                label = { Text("默认") },
                selected = false
            )
            
            FilterChip(
                onClick = { onParamsChange(DefaultDebugParams.modern) },
                label = { Text("现代") },
                selected = false
            )
            
            FilterChip(
                onClick = { onParamsChange(DefaultDebugParams.compact) },
                label = { Text("紧凑") },
                selected = false
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 保存/加载按钮
            IconButton(
                onClick = { /* TODO: 保存当前参数 */ }
            ) {
                Icon(Icons.Default.Save, contentDescription = "保存")
            }
            
            IconButton(
                onClick = { /* TODO: 加载保存的参数 */ }
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = "加载")
            }
        }
    }
}