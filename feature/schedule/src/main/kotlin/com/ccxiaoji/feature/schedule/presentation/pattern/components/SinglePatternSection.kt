package com.ccxiaoji.feature.schedule.presentation.pattern.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.ui.components.ModernCard
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 单次排班配置部分 - 扁平化设计
 */
@Composable
fun SinglePatternSection(
    shifts: List<Shift>,
    selectedShift: Shift?,
    onShiftSelect: (Shift) -> Unit,
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
                stringResource(R.string.schedule_pattern_select_shift),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            shifts.forEach { shift ->
                ShiftSelectionItem(
                    shift = shift,
                    isSelected = shift == selectedShift,
                    onSelect = { onShiftSelect(shift) }
                )
            }
        }
    }
}