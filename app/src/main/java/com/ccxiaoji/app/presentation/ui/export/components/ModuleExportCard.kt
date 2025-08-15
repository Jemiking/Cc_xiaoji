package com.ccxiaoji.app.presentation.ui.export.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.app.presentation.ui.export.ExportModule
import com.ccxiaoji.app.presentation.ui.export.ModuleStats
import java.text.SimpleDateFormat
import java.util.*

/**
 * 模块导出选择卡片
 */
@Composable
fun ModuleExportCard(
    module: ExportModule,
    stats: ModuleStats?,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (module.isAvailable) 1f else 0.6f)
            .clickable(enabled = module.isAvailable) {
                onSelectionChange(!isSelected)
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected && module.isAvailable) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 复选框
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChange,
                enabled = module.isAvailable
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 图标
            Icon(
                imageVector = module.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (module.isAvailable) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 模块信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = module.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (!module.isAvailable) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "开发中",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 统计信息
                if (stats != null && stats.totalRecords > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "${stats.totalRecords}条记录",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        if (stats.estimatedSize != "-") {
                            Text(
                                text = "约${stats.estimatedSize}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        stats.lastModified?.let { timestamp ->
                            val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                            Text(
                                text = "更新: ${dateFormat.format(Date(timestamp))}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}