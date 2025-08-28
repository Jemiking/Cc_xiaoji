package com.ccxiaoji.feature.schedule.presentation.demo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.presentation.calendar.CalendarView
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import com.ccxiaoji.feature.schedule.presentation.debug.DefaultDebugParams
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

/**
 * 扁平化设计风格Demo
 */
@Composable
fun FlatStyleDemo() {
    val (schedules, selectedDate) = rememberDemoData()
    val params = remember { DefaultDebugParams.default }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 扁平化统计卡片：无阴影，使用边框
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FlatStatItem("工作天数", "2天", Color(0xFF2196F3))
                FlatStatItem("休息天数", "0天", Color(0xFF4CAF50))
                FlatStatItem("总工时", "18小时", Color(0xFFFF9800))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 扁平化日历
        CalendarView(
            yearMonth = YearMonth.now(),
            selectedDate = selectedDate,
            schedules = schedules,
            weekStartDay = java.time.DayOfWeek.MONDAY,
            viewMode = CalendarViewMode.COMFORTABLE,
            debugParams = params.calendarView.copy(
                cellSpacing = 0.dp,
                cornerRadius = 8.dp
            ),
            onDateSelected = {},
            onDateLongClick = {},
            onMonthNavigate = {}
        )
    }
}

@Composable
private fun FlatStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 拟物化设计风格Demo
 */
@Composable
fun SkeuomorphismStyleDemo() {
    val (schedules, selectedDate) = rememberDemoData()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 拟物化统计卡片：强阴影，渐变背景
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.25f)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SkeuomorphismStatItem("工作天数", "2天", Color(0xFF2196F3))
                    SkeuomorphismStatItem("休息天数", "0天", Color(0xFF4CAF50))
                    SkeuomorphismStatItem("总工时", "18小时", Color(0xFFFF9800))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 拟物化日历占位
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .shadow(6.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "拟物化日历视图\n(具有质感和立体感)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SkeuomorphismStatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        // 立体按钮效果
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f),
            shadowElevation = 4.dp,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 新拟物化设计风格Demo
 */
@Composable
fun NeumorphismStyleDemo() {
    val (schedules, selectedDate) = rememberDemoData()
    val backgroundColor = MaterialTheme.colorScheme.surface
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 新拟物化统计卡片：内凹效果
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = backgroundColor,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                backgroundColor.copy(alpha = 0.1f),
                                backgroundColor.copy(alpha = 0.9f),
                                backgroundColor.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NeumorphismStatItem("工作天数", "2天", Color(0xFF2196F3), backgroundColor)
                    NeumorphismStatItem("休息天数", "0天", Color(0xFF4CAF50), backgroundColor)
                    NeumorphismStatItem("总工时", "18小时", Color(0xFFFF9800), backgroundColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 新拟物化日历占位
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                backgroundColor.copy(alpha = 0.05f),
                                backgroundColor,
                                backgroundColor.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "新拟物化日历视图\n(柔和的浮雕效果)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun NeumorphismStatItem(label: String, value: String, color: Color, backgroundColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        // 内凹效果的数值显示
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            shadowElevation = 0.dp,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                backgroundColor.copy(alpha = 0.5f),
                                backgroundColor,
                                backgroundColor.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

/**
 * 毛玻璃效果设计风格Demo
 */
@Composable
fun GlassmorphismStyleDemo() {
    val (schedules, selectedDate) = rememberDemoData()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea).copy(alpha = 0.3f),
                        Color(0xFFf093fb).copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 毛玻璃统计卡片
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .blur(0.5.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GlassStatItem("工作天数", "2天", Color(0xFF2196F3))
                        GlassStatItem("休息天数", "0天", Color(0xFF4CAF50))
                        GlassStatItem("总工时", "18小时", Color(0xFFFF9800))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 毛玻璃日历占位
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(0.3.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "毛玻璃日历视图\n(透明磨砂质感)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassStatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.15f),
            shadowElevation = 0.dp,
            border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f)),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * 极简主义设计风格Demo
 */
@Composable
fun MinimalismStyleDemo() {
    val (schedules, selectedDate) = rememberDemoData()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 极简统计：纯文字
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MinimalStatItem("工作", "2")
            MinimalStatItem("休息", "0")  
            MinimalStatItem("工时", "18")
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // 极简分割线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.Black.copy(alpha = 0.1f))
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 极简日历占位
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "日历",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Light
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Color.Black, shape = androidx.compose.foundation.shape.CircleShape)
                )
                Text(
                    text = "极简设计：去除一切装饰",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MinimalStatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            color = Color.Black,
            fontWeight = FontWeight.Light
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black.copy(alpha = 0.6f)
        )
    }
}

/**
 * 共享的示例数据
 */
@Composable
private fun rememberDemoData(): Pair<List<Schedule>, LocalDate> {
    val today = LocalDate.now()
    val sampleShift = remember {
        Shift(
            id = 1L,
            name = "日班",
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(18, 0),
            color = 0xFF2196F3.toInt(),
            description = "演示用"
        )
    }
    val schedules = remember {
        listOf(
            Schedule(id = 1L, date = today, shift = sampleShift),
            Schedule(id = 2L, date = today.plusDays(1), shift = sampleShift)
        )
    }
    return Pair(schedules, today)
}