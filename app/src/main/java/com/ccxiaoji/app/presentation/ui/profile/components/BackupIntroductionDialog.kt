package com.ccxiaoji.app.presentation.ui.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * 首次使用备份功能的引导对话框
 * 向用户介绍新的备份格式和优势
 */
@Composable
fun BackupIntroductionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 标题和图标
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Backup,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "全新备份格式",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 主要说明
                Text(
                    text = "我们升级了数据备份系统，带来更好的用户体验：",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 优势列表
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AdvantageItem(
                        icon = Icons.Default.Speed,
                        title = "性能提升",
                        description = "导出速度提升 200%，体积减少 70%"
                    )
                    
                    AdvantageItem(
                        icon = Icons.Default.PhoneAndroid,
                        title = "完美兼容",
                        description = "彻底解决 Android 兼容性问题，不再崩溃"
                    )
                    
                    AdvantageItem(
                        icon = Icons.Default.TableChart,
                        title = "通用格式",
                        description = "CSV 格式可被 Excel、Numbers、WPS 正常打开"
                    )
                    
                    AdvantageItem(
                        icon = Icons.Default.Archive,
                        title = "单文件备份",
                        description = "打包为 .zip 文件，便于分享和存储"
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 格式变更说明
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "格式变更说明",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "新版本使用 CSV+ZIP 格式替代 Excel 格式。旧的 Excel 文件仍可正常导入，无需担心数据丢失。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("稍后提醒")
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("立即体验")
                    }
                }
            }
        }
    }
}

/**
 * 优势展示项组件
 */
@Composable
private fun AdvantageItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 格式变更详细说明对话框
 * 用于用户点击详细了解时显示
 */
@Composable
fun FormatChangeDetailDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 标题
                Text(
                    text = "格式变更详细说明",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 变更对比
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FormatComparisonItem(
                        label = "旧格式",
                        format = "Excel (.xlsx)",
                        issues = listOf(
                            "Android 兼容性问题导致崩溃",
                            "文件体积较大",
                            "导出速度较慢",
                            "依赖复杂的 Excel 库"
                        ),
                        isOld = true
                    )
                    
                    Divider()
                    
                    FormatComparisonItem(
                        label = "新格式",
                        format = "CSV + ZIP (.zip)",
                        issues = listOf(
                            "完美 Android 兼容，零崩溃",
                            "文件体积小 70%",
                            "导出速度快 200%",
                            "可被 Excel/Numbers/WPS 打开"
                        ),
                        isOld = false
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 兼容性保证
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "向后兼容保证",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• 旧的 Excel 文件可以正常导入\n• 数据完全不会丢失\n• 自动转换为新格式\n• 支持混合格式导入",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 确认按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("我了解了")
                }
            }
        }
    }
}

/**
 * 格式对比项组件
 */
@Composable
private fun FormatComparisonItem(
    label: String,
    format: String,
    issues: List<String>,
    isOld: Boolean
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isOld) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isOld) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$label: $format",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isOld) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        issues.forEach { issue ->
            Row(
                modifier = Modifier.padding(start = 28.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "• ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = issue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}