package com.ccxiaoji.feature.schedule.presentation.pattern.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 循环排班配置部分 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyclePatternSection(
    shifts: List<Shift>,
    cycleDays: Int,
    cyclePattern: Map<Int, Long?>,
    onCycleDaysChange: (Int) -> Unit,
    onPatternChange: (Int, Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            Text(
                stringResource(R.string.schedule_pattern_cycle_settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 循环天数选择
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                Text(
                    stringResource(R.string.schedule_pattern_cycle_period),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.schedule_pattern_cycle_days),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 减少按钮
                        OutlinedIconButton(
                            onClick = { 
                                if (cycleDays > 2) onCycleDaysChange(cycleDays - 1) 
                            },
                            enabled = cycleDays > 2,
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.outlinedIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                width = 1.dp,
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                )
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = stringResource(R.string.schedule_pattern_decrease_days)
                            )
                        }
                        
                        // 显示当前天数
                        Surface(
                            modifier = Modifier
                                .widthIn(min = 60.dp)
                                .height(40.dp),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${cycleDays}${stringResource(R.string.schedule_pattern_cycle_days_unit)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        // 增加按钮
                        OutlinedIconButton(
                            onClick = { 
                                if (cycleDays < 365) onCycleDaysChange(cycleDays + 1) 
                            },
                            enabled = cycleDays < 365,
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.outlinedIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                width = 1.dp,
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                )
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.schedule_pattern_increase_days)
                            )
                        }
                    }
                }
                
                // 支持范围提示
                Text(
                    stringResource(R.string.schedule_pattern_cycle_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            
            // 每天的班次选择
            (0 until cycleDays).forEach { dayIndex ->
                CycleDayShiftSelector(
                    dayIndex = dayIndex,
                    cycleDays = cycleDays,
                    shifts = shifts,
                    selectedShiftId = cyclePattern[dayIndex],
                    onShiftSelect = { shiftId ->
                        onPatternChange(dayIndex, shiftId)
                    }
                )
            }
        }
    }
}