package com.ccxiaoji.feature.schedule.presentation.export.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.export.ExportFormat
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 导出格式选择部分 - 扁平化设计
 */
@Composable
fun ExportFormatSection(
    selectedFormat: ExportFormat,
    onFormatChange: (ExportFormat) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            Text(
                stringResource(R.string.schedule_export_format),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            ExportFormat.values().forEach { format ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = DesignTokens.Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedFormat == format,
                        onClick = { onFormatChange(format) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            when (format) {
                                ExportFormat.CSV -> stringResource(R.string.schedule_export_format_csv)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            when (format) {
                                ExportFormat.CSV -> stringResource(R.string.schedule_export_format_csv_desc)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Icon(
                        when (format) {
                            ExportFormat.CSV -> Icons.Default.TableChart
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}