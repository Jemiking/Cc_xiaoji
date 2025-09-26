package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.max

enum class DailyTab { Expense, Income, Total }

data class DailySeries(
    val expense: List<Float>,
    val income: List<Float>,
    val total: List<Float>
)

@Composable
fun DailyBarChartCard(
    series: DailySeries,
    tab: DailyTab,
    onTabChange: (DailyTab) -> Unit,
    periodLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ReportTokens.Palette.Card),
        shape = RoundedCornerShape(ReportTokens.Metrics.CardCorner),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(ReportTokens.Metrics.CardPadding)) {
            // 标题左上角，右侧为图标，样式与“分类报表”一致
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "每日统计",
                    color = ReportTokens.Palette.TextPrimary,
                    fontSize = ReportTokens.Type.Title,
                    fontWeight = FontWeight.Medium
                )
                Row {
                    RoundIconButton(iconTint = ReportTokens.Palette.IconGray) { }
                    Spacer(Modifier.size(8.dp))
                    RoundIconButton(icon = Icons.Filled.MoreHoriz, iconTint = ReportTokens.Palette.IconGray) { }
                }
            }

            Spacer(Modifier.height(12.dp))

            val values: List<Float> = when (tab) {
                DailyTab.Expense -> series.expense
                DailyTab.Income -> series.income
                DailyTab.Total -> series.total
            }
            val color: Color = when (tab) {
                DailyTab.Expense -> ReportTokens.Palette.ExpenseRed
                DailyTab.Income -> ReportTokens.Palette.IncomeGreen
                DailyTab.Total -> ReportTokens.Palette.TotalGray
            }

            BarChartWithAxes(
                values = values,
                barColor = color,
                periodLabel = periodLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ReportTokens.Metrics.BarChartHeight)
            )

            Spacer(Modifier.height(16.dp))
            Segmented3(
                labels = listOf("支出", "收入", "全部"),
                selected = when (tab) {
                    DailyTab.Expense -> 0
                    DailyTab.Income -> 1
                    DailyTab.Total -> 2
                },
                onSelected = { idx ->
                    onTabChange(
                        when (idx) {
                            0 -> DailyTab.Expense
                            1 -> DailyTab.Income
                            else -> DailyTab.Total
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun BarChartWithAxes(values: List<Float>, barColor: Color, periodLabel: String, modifier: Modifier = Modifier) {
    val maxValue = max(values.maxOrNull() ?: 0f, 0f)
    // 轴最大值按 1000 向上取整
    val axisMax = if (maxValue <= 0f) 0f else niceCeil(maxValue)
    // 当峰值超过 5K 时，按 1K 为步长动态增加网格数；否则固定 5 档
    val gridCount = if (axisMax >= 5000f) (axisMax / 1000f).toInt().coerceIn(5, 12) else 5
    // 统一内边距，留出左侧刻度与底部刻度空间
    val leftPad = 36.dp
    val rightPad = 14.dp
    val topPad = 8.dp
    val bottomPad = 24.dp

    Box(modifier = modifier) {
        // 绘制网格、柱、轴线
        Canvas(modifier = Modifier.matchParentSize()) {
            val chartPaddingLeft = leftPad.toPx()
            val chartPaddingRight = rightPad.toPx()
            val chartPaddingTop = topPad.toPx()
            val chartPaddingBottom = bottomPad.toPx()
            val width = size.width - chartPaddingLeft - chartPaddingRight
            val height = size.height - chartPaddingTop - chartPaddingBottom

            // Grid lines（0..gridCount，包括顶部/底部）
            val gridColor = ReportTokens.Palette.Divider.copy(alpha = 0.15f)
            for (i in 0..gridCount) {
                val y = chartPaddingTop + height * (i / gridCount.toFloat())
                drawLine(
                    color = gridColor,
                    start = Offset(chartPaddingLeft, y),
                    end = Offset(chartPaddingLeft + width, y),
                    strokeWidth = 1f
                )
            }

            if (values.isNotEmpty() && maxValue > 0f) {
                // Bars：按天数平均分配横向槽位，保证铺满整个月份
                val slot = width / values.size
                val desiredBarWidth = ReportTokens.Metrics.BarWidth.toPx()
                val barWidth = kotlin.math.min(desiredBarWidth, slot * 0.7f)
                val radius = ReportTokens.Metrics.BarRadius.toPx()
                values.forEachIndexed { index, v ->
                    val h = if (maxValue == 0f) 0f else height * (v / axisMax)
                    val cx = chartPaddingLeft + slot * index + (slot - barWidth) / 2f
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(cx, chartPaddingTop + height - h),
                        size = Size(barWidth, h),
                        cornerRadius = CornerRadius(radius, radius)
                    )
                }
            }

            // Left Y axis line
            drawLine(
                color = ReportTokens.Palette.Divider.copy(alpha = 0.35f),
                start = Offset(chartPaddingLeft, chartPaddingTop),
                end = Offset(chartPaddingLeft, chartPaddingTop + height),
                strokeWidth = 1f
            )

            // X-axis baseline
            drawLine(
                color = ReportTokens.Palette.Divider.copy(alpha = 0.35f),
                start = Offset(chartPaddingLeft, chartPaddingTop + height),
                end = Offset(chartPaddingLeft + width, chartPaddingTop + height),
                strokeWidth = 1f
            )
        }

        // 叠层：纵轴文本（1..N 等分）
        val yLabels = run {
            val step = if (axisMax <= 0f) 0f else axisMax / gridCount
            (1..gridCount).map { i ->
                val v = step * i
                if (axisMax >= 5000f) {
                    // 超过 5K 时按 1K 进位显示（1K,2K,...）
                    "${(v / 1000f).toInt()}K"
                } else {
                    // 低于 5K，显示整数值（200,400,... 或 1K）
                    if (v >= 1000f) "${(v / 1000f).toInt()}K" else v.toInt().toString()
                }
            }
        }
        Column(
            modifier = Modifier
                .matchParentSize()
                .padding(start = 6.dp, top = topPad, bottom = bottomPad)
                .width(leftPad),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // 从上到下显示 gridCount 个标签（不渲染 0）
            for (i in gridCount downTo 1) {
                Text(text = yLabels[i - 1], color = ReportTokens.Palette.TextMuted, fontSize = ReportTokens.Type.Caption)
            }
        }

        // 叠层：横轴 3 个日期标签（起/中/末）
        val (startLabel, midLabel, endLabel) = run {
            val parts = periodLabel.split("-")
            val month = if (parts.size >= 2) parts[1] else periodLabel
            val total = values.size.coerceAtLeast(1)
            val mid = (total + 1) / 2
            fun Int.pad2() = toString().padStart(2, '0')
            val s = "$month-01"
            val m = "$month-${mid.pad2()}"
            val e = "$month-${total.pad2()}"
            Triple(s, m, e)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = leftPad, end = rightPad, bottom = 2.dp)
                .align(Alignment.BottomStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(text = startLabel, color = ReportTokens.Palette.TextMuted, fontSize = ReportTokens.Type.Caption)
            Text(text = midLabel, color = ReportTokens.Palette.TextMuted, fontSize = ReportTokens.Type.Caption)
            Text(text = endLabel, color = ReportTokens.Palette.TextMuted, fontSize = ReportTokens.Type.Caption)
        }
    }
}

@Composable
private fun Segmented3(labels: List<String>, selected: Int, onSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .background(ReportTokens.Palette.ChipContainer, RoundedCornerShape(ReportTokens.Metrics.SegmentedRadius))
            .border(width = 1.dp, color = ReportTokens.Palette.Divider, shape = RoundedCornerShape(ReportTokens.Metrics.SegmentedRadius))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val selectedState = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .background(
                        color = if (selectedState) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(ReportTokens.Metrics.SegmentedRadius)
                    )
                    .clickable { onSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selectedState) ReportTokens.Palette.TextPrimary else ReportTokens.Palette.TextSecondary,
                    fontSize = ReportTokens.Type.Body
                )
            }
        }
    }
}

@Composable
private fun RoundIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.OpenInFull,
    iconTint: Color = ReportTokens.Palette.IconGray,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(color = ReportTokens.Palette.ChipContainer, shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
    }
}

// 将最大值向上取整到最接近的 1000 的倍数（用于 5 等分坐标轴）
private fun niceCeil(v: Float): Float {
    if (v <= 0f) return 0f
    val step = 1000f
    val k = kotlin.math.ceil(v / step)
    return (k * step)
}
