package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// 日期范围类型
enum class DateRangeType {
    THIS_MONTH,    // 本月
    LAST_MONTH,    // 上月
    THIS_YEAR,     // 今年
    LAST_YEAR,     // 去年
    CUSTOM_RANGE,  // 自定义范围
    ALL            // 全部
}

// 筛选状态数据类
data class FilterState(
    val dateRangeType: DateRangeType = DateRangeType.THIS_MONTH,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val selectedLedgers: Set<String> = setOf("日常账本")  // 默认选中日常账本
)

@Composable
fun FilterDialog(
    isVisible: Boolean,
    currentFilter: FilterState = FilterState(),
    onDismiss: () -> Unit,
    onConfirm: (FilterState) -> Unit
) {
    if (!isVisible) return

    var filterState by remember(isVisible) {
        mutableStateOf(
            // 初始化时计算默认日期
            currentFilter.let { state ->
                if (state.startDate == null || state.endDate == null) {
                    val (start, end) = calculateDateRange(state.dateRangeType)
                    state.copy(startDate = start, endDate = end)
                } else {
                    state
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            color = Color(0xFFF5F5F5)  // 背景灰色
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部栏
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 返回按钮
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "返回",
                                tint = Color.Black
                            )
                        }

                        // 标题
                        Text(
                            text = "自定义筛选",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )

                        // 确定按钮
                        TextButton(
                            onClick = { onConfirm(filterState) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF007AFF)  // iOS 蓝色
                            )
                        ) {
                            Text(
                                text = "确定",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 内容区域
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // 账单日期部分
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "账单日期",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // 快捷日期选项 - 第一行
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                DateQuickButton(
                                    text = "本月",
                                    isSelected = filterState.dateRangeType == DateRangeType.THIS_MONTH,
                                    onClick = {
                                        val (start, end) = calculateDateRange(DateRangeType.THIS_MONTH)
                                        filterState = filterState.copy(
                                            dateRangeType = DateRangeType.THIS_MONTH,
                                            startDate = start,
                                            endDate = end
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                DateQuickButton(
                                    text = "上月",
                                    isSelected = filterState.dateRangeType == DateRangeType.LAST_MONTH,
                                    onClick = {
                                        val (start, end) = calculateDateRange(DateRangeType.LAST_MONTH)
                                        filterState = filterState.copy(
                                            dateRangeType = DateRangeType.LAST_MONTH,
                                            startDate = start,
                                            endDate = end
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                DateQuickButton(
                                    text = "今年",
                                    isSelected = filterState.dateRangeType == DateRangeType.THIS_YEAR,
                                    onClick = {
                                        val (start, end) = calculateDateRange(DateRangeType.THIS_YEAR)
                                        filterState = filterState.copy(
                                            dateRangeType = DateRangeType.THIS_YEAR,
                                            startDate = start,
                                            endDate = end
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                DateQuickButton(
                                    text = "去年",
                                    isSelected = filterState.dateRangeType == DateRangeType.LAST_YEAR,
                                    onClick = {
                                        val (start, end) = calculateDateRange(DateRangeType.LAST_YEAR)
                                        filterState = filterState.copy(
                                            dateRangeType = DateRangeType.LAST_YEAR,
                                            startDate = start,
                                            endDate = end
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 第二行 - 全部按钮
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                DateQuickButton(
                                    text = "全部",
                                    isSelected = filterState.dateRangeType == DateRangeType.ALL,
                                    onClick = {
                                        val (start, end) = calculateDateRange(DateRangeType.ALL)
                                        filterState = filterState.copy(
                                            dateRangeType = DateRangeType.ALL,
                                            startDate = start,
                                            endDate = end
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.weight(3f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 自定义日期部分
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "自定义日期",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 开始日期
                                DatePickerField(
                                    label = "开始日期",
                                    value = filterState.startDate?.let { formatDate(it) } ?: "",
                                    onClick = {
                                        // TODO: 打开日期选择器
                                        // 选择后自动切换到CUSTOM_RANGE
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = "—",
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = Color.Gray
                                )

                                // 截止日期
                                DatePickerField(
                                    label = "截止日期",
                                    value = filterState.endDate?.let { formatDate(it) } ?: "",
                                    onClick = {
                                        // TODO: 打开日期选择器
                                        // 选择后自动切换到CUSTOM_RANGE
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 账本成员部分
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = "账本成员",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Help,
                                    contentDescription = "帮助",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Gray
                                )
                            }

                            Text(
                                text = "选择账本",
                                fontSize = 13.sp,
                                color = Color(0xFF999999),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // 日常账本选项
                            LedgerItem(
                                title = "日常账本",
                                subtitle = "初始账本",
                                isSelected = filterState.selectedLedgers.contains("日常账本"),
                                onClick = {
                                    filterState = if (filterState.selectedLedgers.contains("日常账本")) {
                                        filterState.copy(
                                            selectedLedgers = filterState.selectedLedgers - "日常账本"
                                        )
                                    } else {
                                        filterState.copy(
                                            selectedLedgers = filterState.selectedLedgers + "日常账本"
                                        )
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

// 日期范围计算函数
private fun calculateDateRange(type: DateRangeType): Pair<LocalDate, LocalDate> {
    val today = LocalDate.now()
    return when (type) {
        DateRangeType.THIS_MONTH -> {
            val firstDay = today.withDayOfMonth(1)
            val lastDay = today.withDayOfMonth(today.lengthOfMonth())
            firstDay to lastDay
        }
        DateRangeType.LAST_MONTH -> {
            val lastMonth = today.minusMonths(1)
            val firstDay = lastMonth.withDayOfMonth(1)
            val lastDay = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
            firstDay to lastDay
        }
        DateRangeType.THIS_YEAR -> {
            val firstDay = today.withDayOfYear(1)
            val lastDay = today.withMonth(12).withDayOfMonth(31)
            firstDay to lastDay
        }
        DateRangeType.LAST_YEAR -> {
            val lastYear = today.minusYears(1)
            val firstDay = lastYear.withDayOfYear(1)
            val lastDay = lastYear.withMonth(12).withDayOfMonth(31)
            firstDay to lastDay
        }
        DateRangeType.ALL -> {
            // 全部：从2020年到今天（或根据实际需求调整）
            LocalDate.of(2020, 1, 1) to today
        }
        DateRangeType.CUSTOM_RANGE -> {
            // 自定义范围：默认为今天
            today to today
        }
    }
}

// 日期格式化函数
private fun formatDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

@Composable
private fun DateQuickButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isSelected) Color(0xFFE8F4FF) else Color(0xFFF5F5F5)
            )
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) Color(0xFF007AFF) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isSelected) Color(0xFF007AFF) else Color.Black
        )
    }
}

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF5F5F5))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF999999)
        )
        Text(
            text = value.ifEmpty { "选择日期" },
            fontSize = 14.sp,
            color = if (value.isEmpty()) Color(0xFFCCCCCC) else Color.Black,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun LedgerItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 账本图标（用简单的圆形代替）
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF4A90E2)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "账",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }
        }

        // 选中标记
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF007AFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "已选中",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}