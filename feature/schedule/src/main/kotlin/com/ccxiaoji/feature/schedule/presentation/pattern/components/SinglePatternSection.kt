package com.ccxiaoji.feature.schedule.presentation.pattern.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleSectionCard
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
    ScheduleSectionCard(
        title = stringResource(R.string.schedule_pattern_select_shift),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
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