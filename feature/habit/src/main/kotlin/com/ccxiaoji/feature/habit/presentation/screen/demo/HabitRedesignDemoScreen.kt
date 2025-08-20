package com.ccxiaoji.feature.habit.presentation.screen.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.habit.R
import com.ccxiaoji.feature.habit.domain.model.Habit
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import com.ccxiaoji.feature.habit.presentation.utils.HabitColorMapper
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Composable
fun HabitRedesignDemo(modifier: Modifier = Modifier) {
    var habits by remember { mutableStateOf(demoHabits()) }
    var selectedLayout by remember { mutableStateOf(LayoutOption.AdaptiveGrid) }
    var editing by remember { mutableStateOf<HabitWithStreak?>(null) }
    // 调试器参数
    var iconSize by remember { mutableStateOf(72.dp) }
    var offsetX by remember { mutableStateOf(0.dp) }
    var offsetY by remember { mutableStateOf(0.dp) }
    var itemSpacing by remember { mutableStateOf(12.dp) }
    var showDebugger by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize()) {
        // 顶部布局切换 Tabs
        val tabs = listOf(
            LayoutOption.AdaptiveGrid to stringResource(R.string.demo_layout_adaptive),
            LayoutOption.Fixed3Grid to stringResource(R.string.demo_layout_fixed3),
            LayoutOption.SingleColumn to stringResource(R.string.demo_layout_singlecolumn)
        )
        TabRow(selectedTabIndex = tabs.indexOfFirst { it.first == selectedLayout }) {
            tabs.forEachIndexed { index, pair ->
                Tab(
                    selected = selectedLayout == pair.first,
                    onClick = { selectedLayout = pair.first },
                    text = { Text(pair.second) }
                )
            }
        }

        // 调试器面板
        if (showDebugger) {
            DebugPanel(
                iconSize = iconSize,
                onIconSizeChange = { iconSize = it },
                offsetX = offsetX,
                onOffsetXChange = { offsetX = it },
                offsetY = offsetY,
                onOffsetYChange = { offsetY = it },
                spacing = itemSpacing,
                onSpacingChange = { itemSpacing = it }
            )
        }

        when (selectedLayout) {
            LayoutOption.AdaptiveGrid -> IconGrid(
                habits = habits,
                cells = GridCells.Fixed(4),
                spacing = itemSpacing,
                iconSize = iconSize,
                offsetX = offsetX,
                offsetY = offsetY,
                onCheckIn = { id -> habits = habits.map { if (it.habit.id == id) it.copy(completedCount = (it.completedCount + 1).coerceAtMost(it.habit.target)) else it } },
                onEdit = { editing = it }
            )
            LayoutOption.Fixed3Grid -> IconGrid(
                habits = habits,
                cells = GridCells.Fixed(4),
                spacing = itemSpacing,
                iconSize = iconSize,
                offsetX = offsetX,
                offsetY = offsetY,
                onCheckIn = { id -> habits = habits.map { if (it.habit.id == id) it.copy(completedCount = (it.completedCount + 1).coerceAtMost(it.habit.target)) else it } },
                onEdit = { editing = it }
            )
            LayoutOption.SingleColumn -> IconList(
                habits = habits,
                spacing = itemSpacing,
                iconSize = iconSize,
                offsetX = offsetX,
                offsetY = offsetY,
                onCheckIn = { id -> habits = habits.map { if (it.habit.id == id) it.copy(completedCount = (it.completedCount + 1).coerceAtMost(it.habit.target)) else it } },
                onEdit = { editing = it }
            )
        }

        if (editing != null) {
            EditPreviewSheet(habit = editing!!, onDismiss = { editing = null })
        }
    }
}

private enum class LayoutOption { AdaptiveGrid, Fixed3Grid, SingleColumn }

@Composable
private fun IconGrid(
    habits: List<HabitWithStreak>,
    cells: GridCells,
    spacing: Dp,
    iconSize: Dp,
    offsetX: Dp,
    offsetY: Dp,
    onCheckIn: (String) -> Unit,
    onEdit: (HabitWithStreak) -> Unit
) {
    LazyVerticalGrid(
        columns = cells,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(items = habits, key = { it.habit.id }) { item ->
            IconOnlyItem(
                item = item,
                modifier = Modifier.fillMaxWidth(),
                iconSize = iconSize,
                offsetX = offsetX,
                offsetY = offsetY,
                onClick = { onCheckIn(item.habit.id) },
                onLongClick = { onEdit(item) }
            )
        }
    }
}

@Composable
private fun IconList(
    habits: List<HabitWithStreak>,
    spacing: Dp,
    iconSize: Dp,
    offsetX: Dp,
    offsetY: Dp,
    onCheckIn: (String) -> Unit,
    onEdit: (HabitWithStreak) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(items = habits, key = { it.habit.id }) { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(iconSize + 24.dp),
                contentAlignment = Alignment.Center
            ) {
                IconOnlyItem(
                    item = item,
                    iconSize = iconSize,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    onClick = { onCheckIn(item.habit.id) },
                    onLongClick = { onEdit(item) }
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun IconOnlyItem(
    item: HabitWithStreak,
    modifier: Modifier = Modifier,
    iconSize: Dp,
    offsetX: Dp,
    offsetY: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val habit = item.habit
    val color = HabitColorMapper.getHabitColor(habit.color, habit.title)
    val completed = item.completedCount >= habit.target
    val bg = if (completed) color.copy(alpha = 0.25f) else color.copy(alpha = 0.12f)
    val borderColor = if (completed) color else Color.Transparent

    Box(
        modifier = modifier
            .size(iconSize)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .clip(CircleShape)
            .background(bg)
            .then(if (borderColor != Color.Transparent) Modifier.border(2.dp, borderColor, CircleShape) else Modifier)
            .offset(x = offsetX, y = offsetY),
        contentAlignment = Alignment.Center
    ) {
        Text(text = habit.icon ?: "🎯", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun DebugPanel(
    iconSize: Dp,
    onIconSizeChange: (Dp) -> Unit,
    offsetX: Dp,
    onOffsetXChange: (Dp) -> Unit,
    offsetY: Dp,
    onOffsetYChange: (Dp) -> Unit,
    spacing: Dp,
    onSpacingChange: (Dp) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = stringResource(R.string.debugger_title), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        LabeledSlider(
            label = stringResource(R.string.debug_icon_size, iconSize.value.toInt()),
            value = iconSize.value,
            valueRange = 40f..96f,
            onValueChange = { onIconSizeChange(it.dp) }
        )
        LabeledSlider(
            label = stringResource(R.string.debug_icon_offset_x, offsetX.value.toInt()),
            value = offsetX.value,
            valueRange = -24f..24f,
            onValueChange = { onOffsetXChange(it.dp) }
        )
        LabeledSlider(
            label = stringResource(R.string.debug_icon_offset_y, offsetY.value.toInt()),
            value = offsetY.value,
            valueRange = -24f..24f,
            onValueChange = { onOffsetYChange(it.dp) }
        )
        LabeledSlider(
            label = stringResource(R.string.debug_item_spacing, spacing.value.toInt()),
            value = spacing.value,
            valueRange = 4f..32f,
            onValueChange = { onSpacingChange(it.dp) }
        )
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPreviewSheet(habit: HabitWithStreak, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.demo_edit_preview_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Text(text = habit.habit.title, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = { /* 这里可跳转真实编辑页，Demo先关闭 */ onDismiss() }) { Text(stringResource(R.string.demo_edit)) }
                OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun demoHabits(): List<HabitWithStreak> {
    val now: Instant = Clock.System.now()
    return listOf(
        HabitWithStreak(
            habit = Habit("1", "晨跑", "清晨 20 分钟慢跑", "daily", 1, "#4CAF50", "🏃", now, now),
            currentStreak = 5,
            completedCount = 0,
            longestStreak = 12
        ),
        HabitWithStreak(
            habit = Habit("2", "阅读", "技术书籍 30 分钟", "daily", 1, "#2196F3", "📚", now, now),
            currentStreak = 2,
            completedCount = 1,
            longestStreak = 8
        ),
        HabitWithStreak(
            habit = Habit("3", "喝水", null, "daily", 8, "#00BCD4", "💧", now, now),
            currentStreak = 10,
            completedCount = 4,
            longestStreak = 15
        ),
        HabitWithStreak(
            habit = Habit("4", "冥想", "睡前 10 分钟", "daily", 1, "#9C27B0", "🧘", now, now),
            currentStreak = 0,
            completedCount = 0,
            longestStreak = 4
        )
    )
}
