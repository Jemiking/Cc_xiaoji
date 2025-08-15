package com.ccxiaoji.feature.plan.presentation.template

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ccxiaoji.feature.plan.domain.model.Template
import com.ccxiaoji.feature.plan.presentation.components.DatePickerField
import com.ccxiaoji.feature.plan.presentation.components.ParentPlanSelector
import kotlinx.datetime.*

/**
 * 应用模板对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyTemplateDialog(
    template: Template,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (title: String, startDate: LocalDate, parentPlanId: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(template.title) }
    var startDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
    var parentPlanId by remember { mutableStateOf<String?>(null) }
    var showParentSelector by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 标题
                Text(
                    text = "应用模板",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                // 模板信息
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "预计天数：${template.duration}天",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "包含计划：${template.structure.totalPlanCount}个",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // 计划标题
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("计划标题") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(template.title) },
                    enabled = !isLoading
                )
                
                // 开始日期
                DatePickerField(
                    date = startDate,
                    onDateChange = { startDate = it },
                    label = "开始日期",
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 父计划选择
                OutlinedCard(
                    onClick = { showParentSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (parentPlanId != null) "已选择父计划" else "选择父计划（可选）",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "选择",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 预计结束日期
                val endDate = startDate.plus(DatePeriod(days = template.duration))
                Text(
                    text = "预计结束日期：${endDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            onConfirm(title.ifBlank { template.title }, startDate, parentPlanId)
                        },
                        enabled = title.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("应用模板")
                        }
                    }
                }
            }
        }
    }
    
    // 父计划选择器
    if (showParentSelector) {
        ParentPlanSelector(
            currentPlanId = null,
            selectedParentId = parentPlanId,
            onParentSelected = { parentId, _ ->
                parentPlanId = parentId
                showParentSelector = false
            }
        )
    }
}