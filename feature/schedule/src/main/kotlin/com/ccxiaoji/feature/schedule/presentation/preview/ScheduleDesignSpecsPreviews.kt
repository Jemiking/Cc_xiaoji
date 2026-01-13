package com.ccxiaoji.feature.schedule.presentation.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.presentation.uikit.*
import com.ccxiaoji.ui.theme.CcXiaoJiTheme
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * Schedule 模块设计规范预览
 *
 * 用于可视化验证 ScheduleDesignSpecs 中定义的设计令牌
 */

// ==================== 班次颜色预览 ====================

/**
 * 班次类型参数提供器
 */
class ShiftTypeProvider : PreviewParameterProvider<ShiftType> {
    override val values: Sequence<ShiftType> = ShiftType.entries.asSequence()
}

@Preview(
    name = "班次颜色 - 浅色模式",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun ShiftColorsLightPreview() {
    CcXiaoJiTheme(darkTheme = false) {
        ShiftColorCatalog(isDarkTheme = false)
    }
}

@Preview(
    name = "班次颜色 - 深色模式",
    showBackground = true,
    backgroundColor = 0xFF121212
)
@Composable
private fun ShiftColorsDarkPreview() {
    CcXiaoJiTheme(darkTheme = true) {
        ShiftColorCatalog(isDarkTheme = true)
    }
}

@Composable
private fun ShiftColorCatalog(isDarkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "班次语义色",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        ShiftType.entries.forEach { shiftType ->
            ShiftColorItem(
                shiftType = shiftType,
                color = ScheduleDesignSpecs.ShiftColors.getColor(shiftType, isDarkTheme),
                icon = ScheduleDesignSpecs.ShiftColors.getIcon(shiftType)
            )
        }
    }
}

@Composable
private fun ShiftColorItem(
    shiftType: ShiftType,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 颜色指示器
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // 班次名称
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = shiftType.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = shiftType.displayName(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 颜色值
        Text(
            text = color.toHexString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== 模块主色预览 ====================

@Preview(
    name = "模块主色",
    showBackground = true
)
@Composable
private fun ModuleColorsPreview() {
    CcXiaoJiTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Schedule 模块主色",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ColorSwatch(
                name = "Primary",
                color = ScheduleDesignSpecs.ModuleColors.Primary
            )
            ColorSwatch(
                name = "PrimaryLight",
                color = ScheduleDesignSpecs.ModuleColors.PrimaryLight
            )
            ColorSwatch(
                name = "PrimaryContainer",
                color = ScheduleDesignSpecs.ModuleColors.PrimaryContainer
            )
        }
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp, 32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(4.dp)
                )
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = color.toHexString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== 日历格子尺寸预览 ====================

@Preview(
    name = "日历格子尺寸对比",
    showBackground = true,
    widthDp = 360
)
@Composable
private fun CalendarCellSizesPreview() {
    CcXiaoJiTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "日历格子尺寸",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Comfortable 模式
            Text(
                text = "Comfortable 模式 (${ScheduleDesignSpecs.CalendarCellSizes.ComfortableHeight})",
                style = MaterialTheme.typography.labelMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(7) { day ->
                    DayCellPreview(
                        day = day + 1,
                        height = ScheduleDesignSpecs.CalendarCellSizes.ComfortableHeight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Compact 模式
            Text(
                text = "Compact 模式 (${ScheduleDesignSpecs.CalendarCellSizes.CompactHeight})",
                style = MaterialTheme.typography.labelMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(7) { day ->
                    DayCellPreview(
                        day = day + 1,
                        height = ScheduleDesignSpecs.CalendarCellSizes.CompactHeight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCellPreview(
    day: Int,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(ScheduleDesignSpecs.Corners.DayCell))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// ==================== 日期状态样式预览 ====================

@Preview(
    name = "日期状态样式",
    showBackground = true
)
@Composable
private fun DateStateStylesPreview() {
    CcXiaoJiTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "日期状态样式",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 普通日期
                DateStateCell(label = "普通", isToday = false, isSelected = false, isWeekend = false)

                // 今日
                DateStateCell(label = "今日", isToday = true, isSelected = false, isWeekend = false)

                // 选中
                DateStateCell(label = "选中", isToday = false, isSelected = true, isWeekend = false)

                // 周末
                DateStateCell(label = "周末", isToday = false, isSelected = false, isWeekend = true)
            }
        }
    }
}

@Composable
private fun DateStateCell(
    label: String,
    isToday: Boolean,
    isSelected: Boolean,
    isWeekend: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(ScheduleDesignSpecs.Corners.DayCell))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = ScheduleDesignSpecs.DateStateStyles.SelectedBorderWidth,
                            color = ScheduleDesignSpecs.DateStateStyles.SelectedBorderColor,
                            shape = RoundedCornerShape(ScheduleDesignSpecs.Corners.DayCell)
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "15",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (isWeekend) ScheduleDesignSpecs.DateStateStyles.WeekendAlpha else 1f
                    )
                )
                if (isToday) {
                    Box(
                        modifier = Modifier
                            .size(ScheduleDesignSpecs.DateStateStyles.TodayIndicatorSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

// ==================== 班次标签预览 ====================

@Preview(
    name = "班次标签 (Shift Tag)",
    showBackground = true
)
@Composable
private fun ShiftTagPreview() {
    CcXiaoJiTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "班次标签 (Shift Tag/Pill)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ShiftType.entries.forEach { shiftType ->
                ShiftTagItem(shiftType = shiftType)
            }
        }
    }
}

@Composable
private fun ShiftTagItem(shiftType: ShiftType) {
    val color = shiftType.color()
    Box(
        modifier = Modifier
            .height(ScheduleDesignSpecs.ShiftTagSpecs.Height)
            .clip(ScheduleDesignSpecs.ShiftTagSpecs.Shape)
            .background(color)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = shiftType.displayName(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// ==================== 辅助函数 ====================

/**
 * 班次类型显示名称
 */
private fun ShiftType.displayName(): String = when (this) {
    ShiftType.MORNING -> "早班"
    ShiftType.AFTERNOON -> "中班"
    ShiftType.NIGHT -> "晚班"
    ShiftType.SPECIAL -> "特殊班"
    ShiftType.OVERTIME -> "加班"
    ShiftType.REST -> "休息"
}

/**
 * Color 转十六进制字符串
 */
private fun Color.toHexString(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return "#${red.toString(16).padStart(2, '0')}${green.toString(16).padStart(2, '0')}${blue.toString(16).padStart(2, '0')}".uppercase()
}
