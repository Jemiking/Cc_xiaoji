package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R

/**
 * Schedule 模块空状态（对齐 UIDemo EmptyState）
 *
 * 设计规范：
 * - 图标：80dp，tint=outline
 * - 标题：titleLarge + Bold
 * - 描述：bodyMedium，muted（onSurfaceVariant）
 * - 动作区：按钮 elevation=0，间距 8dp
 */
enum class ScheduleEmptyStatePreset {
    /** 尚未创建任何班次 */
    NoShifts,
    /** 当前月没有任何排班记录 */
    NoSchedulesThisMonth
}

@Immutable
data class ScheduleEmptyStateAction(
    val label: String,
    val onClick: () -> Unit,
    val leadingIcon: ImageVector? = null
)

@Composable
fun ScheduleEmptyState(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.outline,
    primaryAction: ScheduleEmptyStateAction? = null,
    secondaryAction: ScheduleEmptyStateAction? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 32.dp, horizontal = 16.dp)
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon?.let { iconVector ->
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )

        description?.let { desc ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        val hasActions = actions != null || primaryAction != null || secondaryAction != null
        if (hasActions) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    actions != null -> actions()
                    else -> {
                        primaryAction?.let { action ->
                            Button(
                                onClick = action.onClick,
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp,
                                    hoveredElevation = 0.dp,
                                    focusedElevation = 0.dp
                                )
                            ) {
                                action.leadingIcon?.let { leading ->
                                    Icon(
                                        imageVector = leading,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(action.label)
                            }
                        }

                        secondaryAction?.let { action ->
                            TextButton(onClick = action.onClick) {
                                action.leadingIcon?.let { leading ->
                                    Icon(
                                        imageVector = leading,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = action.label,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleEmptyState(
    preset: ScheduleEmptyStatePreset,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.outline,
    titleOverride: String? = null,
    descriptionOverride: String? = null,
    primaryAction: ScheduleEmptyStateAction? = null,
    secondaryAction: ScheduleEmptyStateAction? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 32.dp, horizontal = 16.dp)
) {
    val (defaultIcon, defaultTitle, defaultDescription) = when (preset) {
        ScheduleEmptyStatePreset.NoShifts -> Triple(
            Icons.Filled.Schedule,
            stringResource(R.string.schedule_shift_empty),
            stringResource(R.string.schedule_shift_empty_hint)
        )
        ScheduleEmptyStatePreset.NoSchedulesThisMonth -> Triple(
            Icons.Filled.CalendarToday,
            stringResource(R.string.schedule_empty_no_schedule_title),
            stringResource(R.string.schedule_empty_no_schedule_desc)
        )
    }

    ScheduleEmptyState(
        title = titleOverride ?: defaultTitle,
        description = descriptionOverride ?: defaultDescription,
        modifier = modifier,
        icon = defaultIcon,
        iconTint = iconTint,
        primaryAction = primaryAction,
        secondaryAction = secondaryAction,
        actions = actions,
        contentPadding = contentPadding
    )
}
