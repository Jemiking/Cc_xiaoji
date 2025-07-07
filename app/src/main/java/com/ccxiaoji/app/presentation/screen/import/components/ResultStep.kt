package com.ccxiaoji.app.presentation.screen.import.components

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
import com.ccxiaoji.common.data.import.ImportResult
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 结果步骤 - 扁平化设计
 */
@Composable
fun ResultStep(
    result: ImportResult?,
    onFinish: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (result == null) return
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DesignTokens.Spacing.medium)
    ) {
        // 结果摘要
        ModernCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (result.success) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            },
            borderColor = if (result.success) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            },
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.Spacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = if (result.success) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                Text(
                    if (result.success) "导入成功" else "导入失败",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (result.success) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 统计信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatisticItem(
                        label = "总计",
                        value = result.totalItems.toString()
                    )
                    StatisticItem(
                        label = "成功",
                        value = result.importedItems.toString(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatisticItem(
                        label = "跳过",
                        value = result.skippedItems.toString(),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    if (result.hasErrors) {
                        StatisticItem(
                            label = "错误",
                            value = result.errors.size.toString(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                if (result.successRate < 1f) {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    LinearProgressIndicator(
                        progress = { result.successRate },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
        
        // 模块详情
        Text(
            "导入详情",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
        
        result.moduleResults.forEach { (module, moduleResult) ->
            ModuleResultCard(module, moduleResult)
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
        }
        
        // 错误列表
        if (result.hasErrors) {
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
            
            Text(
                "错误详情",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            result.errors.forEach { error ->
                ErrorCard(error)
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
        
        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            if (!result.success) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                    Text("重试")
                }
            }
            
            FlatButton(
                onClick = onFinish,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                Text("完成")
            }
        }
    }
}