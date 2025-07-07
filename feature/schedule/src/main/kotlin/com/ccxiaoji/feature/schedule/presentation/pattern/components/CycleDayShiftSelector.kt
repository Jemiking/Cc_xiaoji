package com.ccxiaoji.feature.schedule.presentation.pattern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 循环天班次选择器 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleDayShiftSelector(
    dayIndex: Int,
    cycleDays: Int,
    shifts: List<Shift>,
    selectedShiftId: Long?,
    onShiftSelect: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedShift = shifts.find { it.id == selectedShiftId }
    
    OutlinedCard(
        onClick = { expanded = true },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.schedule_pattern_day_number, dayIndex + 1),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (selectedShift != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                Color(selectedShift.color),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                    Text(
                        selectedShift.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Text(
                    stringResource(R.string.schedule_pattern_rest),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.schedule_pattern_rest)) },
                onClick = {
                    onShiftSelect(null)
                    expanded = false
                }
            )
            
            shifts.forEach { shift ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        Color(shift.color),
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                            Text(shift.name)
                        }
                    },
                    onClick = {
                        onShiftSelect(shift.id)
                        expanded = false
                    }
                )
            }
        }
    }
}