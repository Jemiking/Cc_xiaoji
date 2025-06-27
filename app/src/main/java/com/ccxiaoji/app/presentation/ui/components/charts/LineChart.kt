package com.ccxiaoji.app.presentation.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun LineChart(
    data: Map<LocalDate, Pair<Int, Int>>, // Date to (Income, Expense)
    modifier: Modifier = Modifier
) {
    val incomeColor = Color(0xFF4CAF50)
    val expenseColor = Color(0xFFF44336)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无数据",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
        return
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val leftPadding = 50.dp.toPx()
        val rightPadding = 16.dp.toPx()
        val topPadding = 16.dp.toPx()
        val bottomPadding = 30.dp.toPx()
        
        val chartWidth = canvasWidth - leftPadding - rightPadding
        val chartHeight = canvasHeight - topPadding - bottomPadding
        
        // Sort data by date
        val sortedData = data.entries.sortedBy { it.key }
        
        // Find max value for scaling
        val maxValue = data.values.maxOf { maxOf(it.first, it.second) }
        val minValue = 0
        
        // Draw grid lines
        val horizontalLines = 5
        for (i in 0..horizontalLines) {
            val y = topPadding + (chartHeight / horizontalLines) * i
            drawLine(
                color = gridColor,
                start = Offset(leftPadding, y),
                end = Offset(canvasWidth - rightPadding, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Draw data points and lines
        if (sortedData.isNotEmpty()) {
            val xStep = chartWidth / (sortedData.size - 1).coerceAtLeast(1)
            
            // Income line
            val incomePoints = sortedData.mapIndexed { index, entry ->
                val x = leftPadding + index * xStep
                val y = topPadding + chartHeight - (entry.value.first.toFloat() / maxValue * chartHeight)
                Offset(x, y)
            }
            
            // Expense line
            val expensePoints = sortedData.mapIndexed { index, entry ->
                val x = leftPadding + index * xStep
                val y = topPadding + chartHeight - (entry.value.second.toFloat() / maxValue * chartHeight)
                Offset(x, y)
            }
            
            // Draw income line
            drawPath(
                path = Path().apply {
                    incomePoints.forEachIndexed { index, point ->
                        if (index == 0) moveTo(point.x, point.y)
                        else lineTo(point.x, point.y)
                    }
                },
                color = incomeColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Draw expense line
            drawPath(
                path = Path().apply {
                    expensePoints.forEachIndexed { index, point ->
                        if (index == 0) moveTo(point.x, point.y)
                        else lineTo(point.x, point.y)
                    }
                },
                color = expenseColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Draw data points
            incomePoints.forEach { point ->
                drawCircle(
                    color = incomeColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
            }
            
            expensePoints.forEach { point ->
                drawCircle(
                    color = expenseColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
            }
        }
    }
    
    // Legend
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        LegendItem(color = incomeColor, label = "收入")
        Spacer(modifier = Modifier.width(24.dp))
        LegendItem(color = expenseColor, label = "支出")
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}