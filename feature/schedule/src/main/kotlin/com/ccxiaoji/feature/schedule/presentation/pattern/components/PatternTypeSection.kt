package com.ccxiaoji.feature.schedule.presentation.pattern.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.pattern.PatternType
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleSectionCard
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleSelectableCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 排班模式选择部分 - 使用 Schedule UI Kit 组件
 */
@Composable
fun PatternTypeSection(
    selectedType: PatternType,
    onTypeChange: (PatternType) -> Unit,
    modifier: Modifier = Modifier
) {
    ScheduleSectionCard(
        title = stringResource(R.string.schedule_pattern_mode),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            PatternType.values().forEach { type ->
                val typeName = when (type) {
                    PatternType.SINGLE -> stringResource(R.string.schedule_pattern_single)
                    PatternType.CYCLE -> stringResource(R.string.schedule_pattern_cycle)
                    PatternType.ROTATION -> stringResource(R.string.schedule_pattern_rotation)
                    PatternType.CUSTOM -> stringResource(R.string.schedule_pattern_custom)
                }
                val isSelected = selectedType == type

                ScheduleSelectableCard(
                    selected = isSelected,
                    onClick = { onTypeChange(type) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            role = Role.RadioButton
                            selected = isSelected
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            typeName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = stringResource(R.string.schedule_pattern_selected),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}