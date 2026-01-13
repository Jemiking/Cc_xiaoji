package com.ccxiaoji.feature.schedule.presentation.aurora

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Aurora 风格日历顶部导航栏
 *
 * 设计规格：
 * - 高度: 96dp (可折叠到64dp)
 * - 月份标题: 28sp Bold
 * - 导航按钮: 40dp圆形
 * - 今天按钮: 胶囊形状
 * - 底部: Aurora弧形渐变装饰 (24dp, 10%透明度)
 */
@Composable
fun AuroraTopHeader(
    currentMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val cardColor = if (isDarkMode) {
        ScheduleAuroraColors.CardSurfaceDark
    } else {
        ScheduleAuroraColors.CardSurfaceLight
    }

    val textPrimary = if (isDarkMode) {
        ScheduleAuroraColors.TextPrimaryDark
    } else {
        ScheduleAuroraColors.TextPrimaryLight
    }

    val textSecondary = if (isDarkMode) {
        ScheduleAuroraColors.TextSecondaryDark
    } else {
        ScheduleAuroraColors.TextSecondaryLight
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(cardColor)
    ) {
        // Aurora弧形渐变装饰 (底部)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ScheduleAuroraColors.PrimaryBlue.copy(alpha = 0.08f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧: 菜单按钮 + 日期
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onMenuClick != null) {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "菜单",
                            tint = textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column {
                    Text(
                        text = currentMonth.format(
                            DateTimeFormatter.ofPattern("yyyy年", Locale.CHINESE)
                        ),
                        color = textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = currentMonth.format(
                            DateTimeFormatter.ofPattern("M月", Locale.CHINESE)
                        ),
                        color = textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 右侧: 今天按钮 + 导航按钮
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 今天按钮
                AuroraTodayButton(
                    onClick = onToday,
                    isDarkMode = isDarkMode
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 上一月
                AuroraNavButton(
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "上一月",
                            tint = textPrimary
                        )
                    },
                    onClick = onPrevMonth,
                    isDarkMode = isDarkMode
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 下一月
                AuroraNavButton(
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "下一月",
                            tint = textPrimary
                        )
                    },
                    onClick = onNextMonth,
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

/**
 * 紧凑型顶部导航栏 (64dp)
 */
@Composable
fun AuroraTopHeaderCompact(
    currentMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val cardColor = if (isDarkMode) {
        ScheduleAuroraColors.CardSurfaceDark
    } else {
        ScheduleAuroraColors.CardSurfaceLight
    }

    val textPrimary = if (isDarkMode) {
        ScheduleAuroraColors.TextPrimaryDark
    } else {
        ScheduleAuroraColors.TextPrimaryLight
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(cardColor)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onMenuClick != null) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "菜单",
                        tint = textPrimary
                    )
                }
            }

            Text(
                text = currentMonth.format(
                    DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINESE)
                ),
                color = textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 右侧
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuroraTodayButton(onClick = onToday, isDarkMode = isDarkMode)

            Spacer(modifier = Modifier.width(8.dp))

            AuroraNavButton(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "上一月",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = onPrevMonth,
                size = 36.dp,
                isDarkMode = isDarkMode
            )

            Spacer(modifier = Modifier.width(4.dp))

            AuroraNavButton(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "下一月",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = onNextMonth,
                size = 36.dp,
                isDarkMode = isDarkMode
            )
        }
    }
}

/**
 * "今天"按钮
 * 视觉尺寸32dp，但点击热区扩展到48dp以满足无障碍要求
 */
@Composable
private fun AuroraTodayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    val bgColor = if (isDarkMode) {
        ScheduleAuroraColors.PrimaryBlue.copy(alpha = 0.2f)
    } else {
        ScheduleAuroraColors.BackgroundLight
    }

    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "今天",
                color = ScheduleAuroraColors.PrimaryBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 导航按钮 (圆形)
 * 视觉尺寸可自定义，但点击热区至少48dp以满足无障碍要求
 */
@Composable
private fun AuroraNavButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    isDarkMode: Boolean = false
) {
    val borderColor = if (isDarkMode) {
        ScheduleAuroraColors.BorderDark
    } else {
        ScheduleAuroraColors.BorderLight
    }

    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(1.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

/**
 * 周标题行 (一 二 三 四 五 六 日)
 */
@Composable
fun AuroraWeekHeader(
    modifier: Modifier = Modifier,
    weekStartDay: java.time.DayOfWeek = java.time.DayOfWeek.MONDAY,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val textColor = if (isDarkMode) {
        ScheduleAuroraColors.TextTertiaryDark
    } else {
        ScheduleAuroraColors.TextTertiaryLight
    }

    val weekendColor = if (isDarkMode) {
        ScheduleAuroraColors.PrimaryPurple.copy(alpha = 0.7f)
    } else {
        ScheduleAuroraColors.PrimaryPurple
    }

    val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")

    // 根据周起始日重新排序
    val orderedDays = when (weekStartDay) {
        java.time.DayOfWeek.SUNDAY -> listOf("日", "一", "二", "三", "四", "五", "六")
        java.time.DayOfWeek.SATURDAY -> listOf("六", "日", "一", "二", "三", "四", "五")
        else -> weekDays
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        orderedDays.forEachIndexed { index, day ->
            val isWeekend = day == "六" || day == "日"
            Text(
                text = day,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isWeekend) weekendColor else textColor,
                letterSpacing = 0.4.sp,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ============================================
// Previews
// ============================================

@Preview(showBackground = true)
@Composable
private fun PreviewAuroraTopHeader() {
    Column {
        AuroraTopHeader(
            currentMonth = YearMonth.now(),
            onPrevMonth = {},
            onNextMonth = {},
            onToday = {},
            onMenuClick = {}
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuroraTopHeaderCompact(
            currentMonth = YearMonth.now(),
            onPrevMonth = {},
            onNextMonth = {},
            onToday = {},
            onMenuClick = {}
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuroraWeekHeader()
    }
}
