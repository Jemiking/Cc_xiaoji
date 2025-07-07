package com.ccxiaoji.feature.schedule.presentation.pattern.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.pattern.PatternType
import com.ccxiaoji.ui.components.ModernCard
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 排班模式选择部分 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternTypeSection(
    selectedType: PatternType,
    onTypeChange: (PatternType) -> Unit,
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
                stringResource(R.string.schedule_pattern_mode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            PatternType.values().forEach { type ->
                val typeName = when (type) {
                    PatternType.SINGLE -> stringResource(R.string.schedule_pattern_single)
                    PatternType.CYCLE -> stringResource(R.string.schedule_pattern_cycle)
                    PatternType.ROTATION -> stringResource(R.string.schedule_pattern_rotation)
                    PatternType.CUSTOM -> stringResource(R.string.schedule_pattern_custom)
                }
                
                Surface(
                    onClick = { onTypeChange(type) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = if (selectedType == type) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = if (selectedType == type) {
                            2.dp
                        } else {
                            1.dp
                        },
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (selectedType == type) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            }
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            typeName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedType == type) FontWeight.Medium else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (selectedType == type) {
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