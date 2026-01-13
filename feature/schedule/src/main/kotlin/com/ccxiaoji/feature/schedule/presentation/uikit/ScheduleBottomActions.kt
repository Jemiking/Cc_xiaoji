package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 底部操作栏组件
 *
 * 用于页面底部的操作按钮区域，支持多种布局模式。
 * 常见于确认/取消对话框、编辑页面等场景。
 *
 * 设计要点:
 * - 固定在底部，带有顶部阴影分隔
 * - 主操作按钮突出显示
 * - 支持加载状态禁用
 *
 * @param primaryAction 主操作配置
 * @param modifier Modifier
 * @param secondaryAction 次要操作配置 (可选)
 * @param tertiaryAction 第三操作配置 (可选, 通常是危险操作)
 * @param layout 布局模式
 */
@Composable
fun ScheduleBottomActions(
    primaryAction: ActionConfig,
    modifier: Modifier = Modifier,
    secondaryAction: ActionConfig? = null,
    tertiaryAction: ActionConfig? = null,
    layout: BottomActionsLayout = BottomActionsLayout.Horizontal
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        when (layout) {
            BottomActionsLayout.Horizontal -> {
                HorizontalActions(
                    primaryAction = primaryAction,
                    secondaryAction = secondaryAction,
                    tertiaryAction = tertiaryAction
                )
            }
            BottomActionsLayout.Stacked -> {
                StackedActions(
                    primaryAction = primaryAction,
                    secondaryAction = secondaryAction,
                    tertiaryAction = tertiaryAction
                )
            }
        }
    }
}

/**
 * 水平排列的操作按钮
 */
@Composable
private fun HorizontalActions(
    primaryAction: ActionConfig,
    secondaryAction: ActionConfig?,
    tertiaryAction: ActionConfig?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DesignTokens.Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 第三操作 (危险/删除) - 放在最左边
        if (tertiaryAction != null) {
            TextButton(
                onClick = tertiaryAction.onClick,
                enabled = tertiaryAction.enabled,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(tertiaryAction.text)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 次要操作 (取消)
        if (secondaryAction != null) {
            OutlinedButton(
                onClick = secondaryAction.onClick,
                enabled = secondaryAction.enabled
            ) {
                Text(secondaryAction.text)
            }
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
        }

        // 主操作 (确认/保存)
        Button(
            onClick = primaryAction.onClick,
            enabled = primaryAction.enabled
        ) {
            Text(primaryAction.text)
        }
    }
}

/**
 * 垂直堆叠的操作按钮
 */
@Composable
private fun StackedActions(
    primaryAction: ActionConfig,
    secondaryAction: ActionConfig?,
    tertiaryAction: ActionConfig?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DesignTokens.Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 主操作 (确认/保存) - 放在最上面，最突出
        Button(
            onClick = primaryAction.onClick,
            enabled = primaryAction.enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(primaryAction.text)
        }

        // 次要操作 (取消)
        if (secondaryAction != null) {
            OutlinedButton(
                onClick = secondaryAction.onClick,
                enabled = secondaryAction.enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(secondaryAction.text)
            }
        }

        // 第三操作 (危险/删除)
        if (tertiaryAction != null) {
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            TextButton(
                onClick = tertiaryAction.onClick,
                enabled = tertiaryAction.enabled,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(tertiaryAction.text)
            }
        }
    }
}

/**
 * 操作配置
 */
data class ActionConfig(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)

/**
 * 底部操作栏布局模式
 */
enum class BottomActionsLayout {
    /** 水平排列 - 次要操作在左，主操作在右 */
    Horizontal,
    /** 垂直堆叠 - 主操作在上，次要操作在下 */
    Stacked
}

// ==================== 便捷构造函数 ====================

/**
 * 创建确认/取消双按钮配置
 */
fun confirmCancelActions(
    confirmText: String = "确认",
    cancelText: String = "取消",
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    confirmEnabled: Boolean = true
): Pair<ActionConfig, ActionConfig> {
    return ActionConfig(
        text = confirmText,
        onClick = onConfirm,
        enabled = confirmEnabled
    ) to ActionConfig(
        text = cancelText,
        onClick = onCancel
    )
}

/**
 * 创建保存/取消双按钮配置
 */
fun saveCancelActions(
    saveText: String = "保存",
    cancelText: String = "取消",
    onSave: () -> Unit,
    onCancel: () -> Unit,
    saveEnabled: Boolean = true
): Pair<ActionConfig, ActionConfig> {
    return ActionConfig(
        text = saveText,
        onClick = onSave,
        enabled = saveEnabled
    ) to ActionConfig(
        text = cancelText,
        onClick = onCancel
    )
}
