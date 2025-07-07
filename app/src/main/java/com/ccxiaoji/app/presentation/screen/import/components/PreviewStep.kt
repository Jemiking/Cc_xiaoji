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
import com.ccxiaoji.app.presentation.viewmodel.DataImportUiState
import com.ccxiaoji.common.data.import.DataModule
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 预览步骤 - 扁平化设计
 */
@Composable
fun PreviewStep(
    uiState: DataImportUiState,
    onToggleModule: (DataModule) -> Unit,
    onToggleSelectAll: () -> Unit,
    onUpdateConfig: (skipExisting: Boolean?, createBackup: Boolean?) -> Unit,
    onStartImport: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val validation = uiState.validation ?: return
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DesignTokens.Spacing.medium)
    ) {
        // 文件信息卡片
        FileInfoCard(
            fileSize = validation.fileSize,
            moduleCount = validation.dataModules.size
        )
        
        // 错误信息
        if (validation.errors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            validation.errors.forEach { error ->
                ModernCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(DesignTokens.Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                        Text(
                            error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
        
        // 模块选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "选择要导入的模块",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onToggleSelectAll) {
                Text(
                    if (uiState.selectedModules.size == validation.dataModules.size) "取消全选" else "全选",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
        
        validation.dataModules.forEach { module ->
            DataModuleCard(
                module = module,
                selected = uiState.selectedModules.contains(module),
                onToggle = { onToggleModule(module) }
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
        
        // 导入选项
        Text(
            "导入选项",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
        
        ImportOptionsCard(
            skipExisting = uiState.importConfig.skipExisting,
            createBackup = uiState.importConfig.createBackup,
            onSkipExistingChange = { onUpdateConfig(it, null) },
            onCreateBackupChange = { onUpdateConfig(null, it) }
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
        
        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp
                )
            ) {
                Text("取消")
            }
            
            FlatButton(
                onClick = onStartImport,
                modifier = Modifier.weight(1f),
                enabled = uiState.selectedModules.isNotEmpty()
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                Text("开始导入")
            }
        }
    }
}