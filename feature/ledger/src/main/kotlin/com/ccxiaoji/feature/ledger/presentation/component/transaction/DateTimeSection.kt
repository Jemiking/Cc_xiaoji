package com.ccxiaoji.feature.ledger.presentation.component.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * 日期时间选择区域组件
 *
 * 展示交易的日期和时间信息，支持仅显示日期或同时显示日期和时间。
 *
 * @param selectedDate 选中的日期
 * @param selectedTime 选中的时间
 * @param enableTimeRecording 是否启用时间记录，true时显示时间，false时仅显示日期
 * @param onDateTimeClick 点击日期时间卡片的回调函数
 * @param modifier 修饰符，用于自定义组件布局
 * @param dateFormat 日期格式化函数，支持自定义显示格式
 * @param timeFormat 时间格式化函数，支持自定义显示格式
 * @param icon 卡片右侧显示的图标
 *
 * @sample DateTimeSectionPreview
 */
@Composable
fun DateTimeSection(
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    enableTimeRecording: Boolean,
    onDateTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
    dateFormat: (LocalDate) -> String = { it.toString() },
    timeFormat: (LocalTime) -> String = {
        "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
    },
    icon: ImageVector = Icons.Default.DateRange
) {
    Card(
        onClick = onDateTimeClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayText = buildString {
                append(dateFormat(selectedDate))
                if (enableTimeRecording) {
                    append(" ")
                    append(timeFormat(selectedTime))
                }
            }

            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, name = "Date Only")
@Composable
private fun DateTimeSectionDateOnlyPreview() {
    DateTimeSection(
        selectedDate = LocalDate.parse("2024-01-15"),
        selectedTime = LocalTime.parse("14:30:00"),
        enableTimeRecording = false,
        onDateTimeClick = {}
    )
}

@Preview(showBackground = true, name = "Date and Time")
@Composable
private fun DateTimeSectionFullPreview() {
    DateTimeSection(
        selectedDate = LocalDate.parse("2024-01-15"),
        selectedTime = LocalTime.parse("14:30:00"),
        enableTimeRecording = true,
        onDateTimeClick = {}
    )
}

@Preview(showBackground = true, name = "Custom Format")
@Composable
private fun DateTimeSectionCustomFormatPreview() {
    DateTimeSection(
        selectedDate = LocalDate.parse("2024-01-15"),
        selectedTime = LocalTime.parse("14:30:00"),
        enableTimeRecording = true,
        onDateTimeClick = {},
        dateFormat = { date ->
            "${date.year}年${date.monthNumber}月${date.dayOfMonth}日"
        },
        timeFormat = { time ->
            "${time.hour}时${time.minute}分"
        }
    )
}

@Preview(showBackground = true, name = "Today")
@Composable
private fun DateTimeSectionTodayPreview() {
    DateTimeSection(
        selectedDate = LocalDate.parse("2024-11-14"),
        selectedTime = LocalTime.parse("09:45:00"),
        enableTimeRecording = true,
        onDateTimeClick = {},
        dateFormat = { "今天" }
    )
}