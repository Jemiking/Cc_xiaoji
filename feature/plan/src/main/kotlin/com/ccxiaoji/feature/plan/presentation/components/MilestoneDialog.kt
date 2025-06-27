package com.ccxiaoji.feature.plan.presentation.components

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
import com.ccxiaoji.feature.plan.domain.model.Milestone
import com.ccxiaoji.feature.plan.presentation.components.DatePickerField
import kotlinx.datetime.*
import java.util.UUID

/**
 * 里程碑编辑对话框
 * @param milestone 要编辑的里程碑（null表示新建）
 * @param planId 所属计划ID（新建时需要）
 * @param onDismiss 关闭对话框
 * @param onConfirm 确认保存
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestoneDialog(
    milestone: Milestone? = null,
    planId: String = "",
    onDismiss: () -> Unit,
    onConfirm: (Milestone) -> Unit
) {
    var title by remember { mutableStateOf(milestone?.title ?: "") }
    var description by remember { mutableStateOf(milestone?.description ?: "") }
    var targetDate by remember { 
        mutableStateOf(milestone?.targetDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(7, DateTimeUnit.DAY))
    }
    
    var titleError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
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
                    text = if (milestone == null) "添加里程碑" else "编辑里程碑",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                // 里程碑标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        titleError = when {
                            it.isBlank() -> "标题不能为空"
                            it.length > 100 -> "标题不能超过100个字符"
                            else -> null
                        }
                    },
                    label = { Text("里程碑标题 *") },
                    placeholder = { Text("例如：完成第一阶段") },
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // 描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { 
                        if (it.length <= 500) {
                            description = it
                        }
                    },
                    label = { Text("描述（可选）") },
                    placeholder = { Text("详细描述这个里程碑...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    supportingText = {
                        Text("${description.length}/500")
                    }
                )
                
                // 目标日期选择
                DatePickerField(
                    label = "目标日期",
                    date = targetDate,
                    onDateChange = { targetDate = it }
                )
                
                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (title.isNotBlank() && titleError == null) {
                                val newMilestone = Milestone(
                                    id = milestone?.id ?: UUID.randomUUID().toString(),
                                    planId = milestone?.planId ?: planId,
                                    title = title.trim(),
                                    description = description.trim(),
                                    targetDate = targetDate,
                                    isCompleted = milestone?.isCompleted ?: false,
                                    completedDate = milestone?.completedDate
                                )
                                onConfirm(newMilestone)
                            }
                        },
                        enabled = title.isNotBlank() && titleError == null
                    ) {
                        Text(if (milestone == null) "添加" else "保存")
                    }
                }
            }
        }
    }
}

/**
 * 删除里程碑确认对话框
 */
@Composable
fun DeleteMilestoneDialog(
    milestone: Milestone,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除里程碑") },
        text = { 
            Text("确定要删除里程碑「${milestone.title}」吗？此操作不可撤销。")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}