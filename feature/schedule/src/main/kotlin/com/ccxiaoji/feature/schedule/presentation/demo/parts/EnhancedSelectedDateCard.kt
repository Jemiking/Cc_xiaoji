package com.ccxiaoji.feature.schedule.presentation.demo.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.presentation.utils.ShiftColorMapper
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import com.ccxiaoji.feature.schedule.R
 

@Composable
fun EnhancedSelectedDateCard(
    date: LocalDate,
    schedules: List<Schedule>,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val borderColor = if (schedules.isNotEmpty()) MaterialTheme.colorScheme.outline.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp).animateContentSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // 标题与统计（统计分两行，弱化色）
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    val count = schedules.size
                    val total = totalDuration(schedules)
                    Text(
                        text = "班次数：$count",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "总时长：${formatDuration(total)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 班次列表（最多展示3条，超出可展开）
            var expanded by remember { mutableStateOf(false) }
            val sorted = remember(schedules) { schedules.sortedBy { it.shift.startTime ?: java.time.LocalTime.MIN } }
            val (listToShow, moreCount) = remember(sorted) {
                val first = sorted.take(3)
                val more = (sorted.size - first.size).coerceAtLeast(0)
                first to more
            }
            // 计算强度条长度基准（使用全量数据，避免展开后跳变）
            val maxDur = sorted.maxOfOrNull { durationHours(it) }?.coerceAtLeast(1f) ?: 1f
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // 折叠部分（最多3条）
                listToShow.forEach { sch ->
                    ShiftRowEnhanced(s = sch, maxHours = maxDur)
                }
                // 展开部分（其余条目，带动画）
                AnimatedVisibility(
                    visible = expanded && sorted.size > 3,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        sorted.drop(3).forEach { sch ->
                            ShiftRowEnhanced(s = sch, maxHours = maxDur)
                        }
                    }
                }
                // 展开/收起按钮
                if (moreCount > 0 && !expanded) {
                    TextButton(onClick = { expanded = true }, modifier = Modifier.align(Alignment.End)) {
                        Text(stringResource(R.string.schedule_card_more_format, moreCount))
                    }
                } else if (expanded && sorted.size > 3) {
                    TextButton(onClick = { expanded = false }, modifier = Modifier.align(Alignment.End)) {
                        Text(stringResource(R.string.schedule_card_collapse))
                    }
                }
            }

            // 空态提示
            if (schedules.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(R.string.schedule_calendar_no_schedule), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (onEdit != null) {
                            Text(text = stringResource(R.string.schedule_card_add_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 底部操作区：右对齐 删除/编辑/添加
            val showAddBtn = schedules.isEmpty() && onEdit != null
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (schedules.isEmpty() && onEdit != null) {
                    // 无排班时提供明显的“添加排班”按钮
                    FilledTonalButton(onClick = { onEdit() }) {
                        Text(text = stringResource(R.string.schedule_calendar_add_schedule))
                    }
                } else {
                    if (onDelete != null && schedules.isNotEmpty()) {
                        IconButton(onClick = { onDelete() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除当日排班",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (onEdit != null) {
                        IconButton(onClick = { onEdit() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "编辑当日排班"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShiftRowEnhanced(s: Schedule, maxHours: Float) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        // 左侧彩色圆标
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(ShiftColorMapper.getBackgroundColorForShift(s.shift.color, 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = abbrev(s.shift.name, 1), color = ShiftColorMapper.getColorForShift(s.shift.color), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        // 中部名称+时间
        Column(modifier = Modifier.weight(1f)) {
            Text(text = s.shift.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            val timeText = if (s.shift.startTime != null && s.shift.endTime != null) "${s.shift.startTime} - ${s.shift.endTime}" else ""
            if (timeText.isNotEmpty()) {
                Text(text = timeText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        // 右侧强度条（相对容器宽度）
        val color = ShiftColorMapper.getColorForShift(s.shift.color)
        val hours = durationHours(s)
        val targetFraction = (hours / maxHours).coerceIn(0.15f, 1f)
        val animatedFraction by animateFloatAsState(targetValue = targetFraction, label = "barFraction")
        Box(modifier = Modifier.weight(0.35f), contentAlignment = Alignment.CenterEnd) {
            // 背景轨道
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
            )
            // 填充条
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color.copy(alpha = 0.9f))
            )
        }
    }
}

private fun totalDuration(items: List<Schedule>): Duration {
    var total = Duration.ZERO
    items.forEach { s ->
        val st = s.shift.startTime
        val et = s.shift.endTime
        if (st != null && et != null) {
            val d = if (et.isBefore(st)) Duration.between(st, et.plusHours(24)) else Duration.between(st, et)
            total = total.plus(d)
        }
    }
    return total
}

private fun durationHours(s: Schedule): Float {
    val st = s.shift.startTime
    val et = s.shift.endTime
    return if (st != null && et != null) {
        val d = if (et.isBefore(st)) Duration.between(st, et.plusHours(24)) else Duration.between(st, et)
        d.toMinutes().toFloat() / 60f
    } else 0f
}

private fun formatDuration(d: Duration): String {
    val hours = d.toHours()
    val minutes = (d.toMinutes() % 60)
    return if (minutes == 0L) "${hours}小时" else "${hours}小时${minutes}分"
}

private fun abbrev(name: String, count: Int = 2): String = when {
    name.contains("夜") -> "夜"
    name.contains("晚") -> "晚"
    name.contains("早") -> "早"
    name.contains("日") -> "日"
    else -> name.take(count)
}
