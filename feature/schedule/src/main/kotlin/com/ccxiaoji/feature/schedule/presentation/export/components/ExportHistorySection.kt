package com.ccxiaoji.feature.schedule.presentation.export.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.export.ExportInfo
import com.ccxiaoji.ui.theme.DesignTokens
import java.io.File

/**
 * 导出历史部分 - 扁平化设计
 */
@Composable
fun ExportHistorySection(
    exportHistory: List<ExportInfo>,
    onShare: (File) -> Unit,
    onDelete: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
    ) {
        Text(
            stringResource(R.string.schedule_export_history),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        exportHistory.forEach { exportInfo ->
            ExportHistoryItem(
                exportInfo = exportInfo,
                onShare = onShare,
                onDelete = onDelete
            )
        }
    }
}