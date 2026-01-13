package com.ccxiaoji.feature.schedule.presentation.aurora

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Aurora 风格统计卡片
 *
 * 设计规格：
 * - 尺寸: 160x96dp
 * - 圆角: 20dp
 * - 数字: 34sp Bold (Aurora渐变)
 * - 标签: 12sp Medium
 * - 底部: 18dp高度的迷你折线图
 */
@Composable
fun AuroraStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trendPoints: List<Float> = emptyList(), // 归一化到 0f-1f
    accentColor: Color? = null, // 自定义强调色
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val cardColor = if (isDarkMode) {
        ScheduleAuroraColors.CardSurfaceDark
    } else {
        ScheduleAuroraColors.CardSurfaceLight
    }

    val textSecondary = if (isDarkMode) {
        ScheduleAuroraColors.TextSecondaryDark
    } else {
        ScheduleAuroraColors.TextSecondaryLight
    }

    val lineColor = accentColor ?: ScheduleAuroraColors.PrimaryBlue

    Box(
        modifier = modifier
            .size(160.dp, 96.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 标签
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSecondary
                )
            )

            // 数值 (Aurora渐变)
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    brush = ScheduleAuroraColors.AuroraGradient
                )
            )

            // 迷你折线图
            if (trendPoints.isNotEmpty() && trendPoints.size >= 2) {
                Sparkline(
                    data = trendPoints,
                    color = lineColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

/**
 * 带图标的统计卡片变体
 */
@Composable
fun AuroraStatCardWithIcon(
    label: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = ScheduleAuroraColors.PrimaryBlue,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val cardColor = if (isDarkMode) {
        ScheduleAuroraColors.CardSurfaceDark
    } else {
        ScheduleAuroraColors.CardSurfaceLight
    }

    val textSecondary = if (isDarkMode) {
        ScheduleAuroraColors.TextSecondaryDark
    } else {
        ScheduleAuroraColors.TextSecondaryLight
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标容器
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary
                    )
                )
                Text(
                    text = value,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                )
            }
        }
    }
}

/**
 * 迷你折线图 (Sparkline)
 */
@Composable
fun Sparkline(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 2f
) {
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas

        val path = Path()
        val stepX = size.width / (data.size - 1)
        val padding = 2.dp.toPx()

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = padding + (1f - value) * (size.height - 2 * padding)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

/**
 * 环形进度图 (用于班次分布)
 */
@Composable
fun AuroraDonutChart(
    segments: List<Pair<Float, Color>>, // (比例, 颜色)
    centerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 12f,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val radius = (minOf(size.width, size.height) - strokeWidth.dp.toPx()) / 2
            val center = Offset(size.width / 2, size.height / 2)
            var startAngle = -90f // 从顶部开始

            segments.forEach { (ratio, color) ->
                val sweepAngle = ratio * 360f
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth.dp.toPx(), cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }

        // 中心内容
        centerContent()
    }
}

// ============================================
// Previews
// ============================================

@Preview(showBackground = true, backgroundColor = 0xFFF6F7FB)
@Composable
private fun PreviewAuroraStatCard() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        AuroraStatCard(
            label = "本月工时",
            value = "168h",
            trendPoints = listOf(0.2f, 0.4f, 0.3f, 0.7f, 0.5f, 0.8f, 0.6f)
        )

        AuroraStatCard(
            label = "夜班天数",
            value = "8",
            trendPoints = listOf(0.5f, 0.3f, 0.6f, 0.4f, 0.7f),
            accentColor = ScheduleAuroraColors.Night.primary
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F7FB)
@Composable
private fun PreviewDonutChart() {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .size(120.dp)
    ) {
        AuroraDonutChart(
            segments = listOf(
                0.4f to ScheduleAuroraColors.Morning.primary,
                0.25f to ScheduleAuroraColors.Afternoon.primary,
                0.2f to ScheduleAuroraColors.Night.primary,
                0.15f to ScheduleAuroraColors.Rest.primary
            ),
            centerContent = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "23",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScheduleAuroraColors.TextPrimaryLight
                    )
                    Text(
                        text = "工作日",
                        fontSize = 10.sp,
                        color = ScheduleAuroraColors.TextSecondaryLight
                    )
                }
            }
        )
    }
}
