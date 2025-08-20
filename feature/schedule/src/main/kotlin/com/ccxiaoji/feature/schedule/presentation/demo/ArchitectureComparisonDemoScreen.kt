package com.ccxiaoji.feature.schedule.presentation.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.presentation.calendar.components.CalendarDayCell
import com.ccxiaoji.feature.schedule.presentation.debug.DefaultDebugParams
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import java.time.LocalDate
import java.time.LocalTime

/**
 * 架构对比Demo页面
 * 展示原有嵌套架构 vs 简化ConstraintLayout架构的效果对比
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchitectureComparisonDemoScreen(
    onNavigateBack: () -> Unit
) {
    var testFontSize by remember { mutableFloatStateOf(14f) }
    val testSchedule = Schedule(
        id = 1,
        date = LocalDate.now(),
        shift = Shift(
            id = 1,
            name = "早班",
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            color = 0xFF2196F3.toInt()  // 蓝色
        )
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("架构对比Demo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 字体大小控制器
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "测试字体大小: ${testFontSize.toInt()}sp",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = testFontSize,
                        onValueChange = { testFontSize = it },
                        valueRange = 10f..24f,
                        steps = 13,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // 对比展示区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 原有架构
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "原有架构",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "ModernCard → Box → Column",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 使用原有的CalendarDayCell
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.height(200.dp)
                        ) {
                            items((1..16).toList()) { day ->
                                CalendarDayCell(
                                    date = LocalDate.now().withDayOfMonth(day.coerceAtMost(28)),
                                    schedule = if (day % 3 == 0) testSchedule else null,
                                    isSelected = day == 15,
                                    isToday = day == 10,
                                    viewMode = CalendarViewMode.COMFORTABLE,
                                    onClick = {},
                                    debugParams = DefaultDebugParams.default.calendarView.copy(
                                        dateNumberTextSize = testFontSize.sp,
                                        rowHeight = 50.dp,
                                        cellSpacing = 2.dp
                                    )
                                )
                            }
                        }
                    }
                }
                
                // 简化架构
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "简化架构",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "ConstraintLayout (直接)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 使用简化的CalendarDayCell
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.height(200.dp)
                        ) {
                            items((1..16).toList()) { day ->
                                SimplifiedCalendarDayCell(
                                    date = LocalDate.now().withDayOfMonth(day.coerceAtMost(28)),
                                    schedule = if (day % 3 == 0) testSchedule else null,
                                    isSelected = day == 15,
                                    isToday = day == 10,
                                    viewMode = CalendarViewMode.COMFORTABLE,
                                    onClick = {},
                                    debugParams = DefaultDebugParams.default.calendarView.copy(
                                        dateNumberTextSize = testFontSize.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            // 对比说明
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "架构对比要点:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("• 原有架构：ModernCard → Box → Column (3层嵌套)")
                    Text("• 简化架构：ConstraintLayout (直接约束)")
                    Text("• 关键差异：wrapContentHeight() vs 固定高度")
                    Text("• 预期效果：简化架构支持更大字体而不被裁剪")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "请调节字体大小观察差异！",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}