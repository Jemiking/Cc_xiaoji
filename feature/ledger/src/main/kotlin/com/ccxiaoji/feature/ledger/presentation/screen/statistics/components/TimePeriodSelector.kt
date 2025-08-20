package com.ccxiaoji.feature.ledger.presentation.screen.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TimePeriod
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatSelectChip
import android.util.Log

@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    // 🔍 添加调试日志来监控UI状态
    Log.d("TimePeriodSelector", "🎯 组件重组 - 当前选中: $selectedPeriod")
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
    ) {
        TimePeriod.values().forEach { period ->
            val isSelected = selectedPeriod == period
            val label = when (period) {
                TimePeriod.THIS_MONTH -> "本月"
                TimePeriod.THIS_YEAR -> "本年"
                TimePeriod.CUSTOM -> "自定义"
            }
            
            Log.d("TimePeriodSelector", "🎯 渲染按钮: $label, 选中状态: $isSelected")
            
            FlatSelectChip(
                label = label,
                selected = isSelected,
                onSelectedChange = { shouldSelect ->
                    Log.d("TimePeriodSelector", "🎯 按钮点击事件: $label, shouldSelect: $shouldSelect, period: $period")
                    
                    // 🔧 修复：对于自定义分析，无论按钮是否已选中都应该触发回调
                    // 这样用户可以重新选择日期范围
                    if (shouldSelect || period == TimePeriod.CUSTOM) {
                        Log.d("TimePeriodSelector", "🎯 触发onPeriodSelected回调: $period")
                        onPeriodSelected(period)
                    } else {
                        Log.d("TimePeriodSelector", "🎯 忽略取消选择事件: $period")
                    }
                },
                modifier = Modifier.weight(1f),
                contentColor = if (isSelected) {
                    DesignTokens.BrandColors.Ledger
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}