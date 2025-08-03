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
import com.ccxiaoji.feature.schedule.presentation.export.ExportInfo
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * 导出历史项 - 扁平化设计
 */
@Composable
fun ExportHistoryItem(
    exportInfo: ExportInfo,
    onShare: (File) -> Unit,
    onDelete: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 格式图标
            Icon(
                when (exportInfo.format) {
                    ExportFormat.CSV -> Icons.Default.TableChart
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
            
            // 文件信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
            ) {
                Text(
                    exportInfo.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stringResource(
                        R.string.schedule_export_time, 
                        exportInfo.exportTime.format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        )
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 操作按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
            ) {
                IconButton(
                    onClick = { onShare(exportInfo.file) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        Icons.Default.Share, 
                        contentDescription = stringResource(R.string.schedule_export_share)
                    )
                }
                
                IconButton(
                    onClick = { onDelete(exportInfo.file) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = stringResource(R.string.schedule_export_delete)
                    )
                }
            }
        }
    }
}