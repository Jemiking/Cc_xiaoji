package com.ccxiaoji.feature.ledger.presentation.screen.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TimePeriod
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatSelectChip

@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
    ) {
        // 常用时间段
        PeriodGroup(
            title = "常用时间段",
            periods = listOf(TimePeriod.TODAY, TimePeriod.THIS_WEEK, TimePeriod.THIS_MONTH),
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )
        
        // 对比分析
        PeriodGroup(
            title = "对比分析", 
            periods = listOf(TimePeriod.LAST_MONTH, TimePeriod.LAST_QUARTER, TimePeriod.LAST_YEAR),
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )
        
        // 长期分析
        PeriodGroup(
            title = "长期分析",
            periods = listOf(TimePeriod.RECENT_3_MONTHS, TimePeriod.RECENT_6_MONTHS, TimePeriod.THIS_QUARTER, TimePeriod.THIS_YEAR),
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )
        
        // 自定义
        PeriodGroup(
            title = "自定义",
            periods = listOf(TimePeriod.CUSTOM),
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )
    }
}

@Composable
private fun PeriodGroup(
    title: String,
    periods: List<TimePeriod>,
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // 对于4个或以下的选项使用行布局，否则使用网格布局
        if (periods.size <= 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                periods.forEach { period ->
                    PeriodChip(
                        period = period,
                        isSelected = selectedPeriod == period,
                        onPeriodSelected = onPeriodSelected,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // 使用网格布局处理更多选项
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                periods.chunked(2).forEach { rowPeriods ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        rowPeriods.forEach { period ->
                            PeriodChip(
                                period = period,
                                isSelected = selectedPeriod == period,
                                onPeriodSelected = onPeriodSelected,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // 如果这行只有一个元素，添加空的占位符
                        if (rowPeriods.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodChip(
    period: TimePeriod,
    isSelected: Boolean,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val label = when (period) {
        TimePeriod.TODAY -> "今日"
        TimePeriod.THIS_WEEK -> "本周"
        TimePeriod.THIS_MONTH -> "本月"
        TimePeriod.LAST_MONTH -> "上月"
        TimePeriod.LAST_QUARTER -> "上季度"
        TimePeriod.LAST_YEAR -> "去年"
        TimePeriod.RECENT_3_MONTHS -> "近3月"
        TimePeriod.RECENT_6_MONTHS -> "近半年"
        TimePeriod.THIS_QUARTER -> "本季度"
        TimePeriod.THIS_YEAR -> "本年"
        TimePeriod.CUSTOM -> "自定义日期..."
    }
    
    
    FlatSelectChip(
        label = label,
        selected = isSelected,
        onSelectedChange = { shouldSelect ->
            if (shouldSelect || period == TimePeriod.CUSTOM) {
                onPeriodSelected(period)
            }
        },
        modifier = modifier,
        contentColor = if (isSelected) {
            DesignTokens.BrandColors.Ledger
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    )
}