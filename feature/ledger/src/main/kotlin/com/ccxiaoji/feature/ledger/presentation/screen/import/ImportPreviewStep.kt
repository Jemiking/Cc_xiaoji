package com.ccxiaoji.feature.ledger.presentation.screen.import

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.importer.ImportPreview
import java.text.SimpleDateFormat
import java.util.*

/**
 * 预览步骤
 */
@Composable
fun PreviewStep(
    preview: ImportPreview,
    onContinue: () -> Unit,
    onConfigure: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 文件信息卡片
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = preview.fileName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${formatFileSize(preview.fileSize)} • ${preview.format} v${preview.version ?: "未知"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 数据统计
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "数据统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 数据类型列表
                preview.dataTypes.entries.forEachIndexed { index, (type, count) ->
                    DataTypeRow(
                        type = mapDataTypeName(type),
                        count = count,
                        icon = getDataTypeIcon(type)
                    )
                    if (index < preview.dataTypes.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 总计
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "总计",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${preview.totalRows} 条记录",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // 日期范围
        preview.dateRange?.let { range ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "数据时间范围",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${formatDate(range.start)} 至 ${formatDate(range.end)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // 错误提示
        if (preview.hasErrors) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "发现 ${preview.errors.size} 个问题",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    
                    if (preview.errors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 150.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            preview.errors.take(5).forEach { error ->
                                Text(
                                    text = "第 ${error.line} 行: ${error.message}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            if (preview.errors.size > 5) {
                                Text(
                                    text = "...还有 ${preview.errors.size - 5} 个问题",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onConfigure,
                modifier = Modifier.weight(1f)
            ) {
                Text("高级设置")
            }
            
            Button(
                onClick = onContinue,
                modifier = Modifier.weight(1f),
                enabled = !preview.hasErrors || preview.errors.isEmpty()
            ) {
                Text("开始导入")
            }
        }
    }
}

/**
 * 数据类型行
 */
@Composable
private fun DataTypeRow(
    type: String,
    count: Int,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(24.dp)) {
                icon()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = type,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = "$count 条",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 辅助函数
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
}

private fun mapDataTypeName(type: String): String {
    return when (type) {
        "HEADER" -> "文件头"
        "ACCOUNT" -> "账户"
        "CATEGORY" -> "分类"
        "TRANSACTION" -> "交易记录"
        "BUDGET" -> "预算"
        "RECURRING" -> "定期交易"
        "SAVINGS" -> "储蓄目标"
        "CREDITBILL" -> "信用卡账单"
        "CREDITPAYMENT" -> "信用卡还款"
        else -> type
    }
}

@Composable
private fun getDataTypeIcon(type: String): @Composable () -> Unit = {
    when (type) {
        "ACCOUNT" -> Icon(Icons.Default.AccountBalance, null, Modifier.size(20.dp))
        "CATEGORY" -> Icon(Icons.Default.Category, null, Modifier.size(20.dp))
        "TRANSACTION" -> Icon(Icons.Default.Receipt, null, Modifier.size(20.dp))
        "BUDGET" -> Icon(Icons.Default.PieChart, null, Modifier.size(20.dp))
        "RECURRING" -> Icon(Icons.Default.Repeat, null, Modifier.size(20.dp))
        "SAVINGS" -> Icon(Icons.Default.Savings, null, Modifier.size(20.dp))
        "CREDITBILL" -> Icon(Icons.Default.CreditCard, null, Modifier.size(20.dp))
        "CREDITPAYMENT" -> Icon(Icons.Default.Payment, null, Modifier.size(20.dp))
        else -> Icon(Icons.Default.Description, null, Modifier.size(20.dp))
    }
}