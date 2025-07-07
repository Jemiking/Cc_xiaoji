package com.ccxiaoji.feature.plan.presentation.screen.analysis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.ChartType
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 图表类型选择器 - 扁平化设计
 */
@Composable
fun ChartTypeSelector(
    selectedType: ChartType,
    onTypeChange: (ChartType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
    ) {
        ScrollableTabRow(
            selectedTabIndex = ChartType.values().indexOf(selectedType),
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.0f),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[ChartType.values().indexOf(selectedType)]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            ChartType.values().forEach { type ->
                Tab(
                    selected = selectedType == type,
                    onClick = { onTypeChange(type) },
                    text = {
                        Text(
                            text = getChartTypeLabel(type),
                            color = if (selectedType == type) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                )
            }
        }
    }
}

/**
 * 获取图表类型标签
 */
@Composable
private fun getChartTypeLabel(type: ChartType) = when (type) {
    ChartType.STATUS_PIE -> "状态分布"
    ChartType.PROGRESS_BAR -> "进度分布"
    ChartType.MONTHLY_TREND -> "月度趋势"
    ChartType.TAG_ANALYSIS -> "标签分析"
}