package com.ccxiaoji.feature.ledger.presentation.component.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import kotlinx.datetime.Instant

/**
 * 联动目标选择区域组件
 *
 * 展示可同步的目标账本列表，支持多选功能。用于将交易同步到其他关联账本。
 *
 * @param selectedTargets 已选中的账本ID集合
 * @param availableTargets 可选的目标账本列表
 * @param onTargetsChanged 选择状态变化的回调函数，传入变化的账本ID
 * @param modifier 修饰符，用于自定义组件布局
 * @param title 区域标题文本
 * @param emptyMessage 没有可用目标时显示的提示信息
 *
 * @sample LinkTargetsSectionPreview
 */
@Composable
fun LinkTargetsSection(
    selectedTargets: Set<String>,
    availableTargets: List<Ledger>,
    onTargetsChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "同步到其他账本",
    emptyMessage: String = "暂无可同步的账本"
) {
    Column(modifier = modifier) {
        // 标题
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (availableTargets.isEmpty()) {
            // 空状态提示
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            // 目标账本列表
            availableTargets.forEach { target ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onTargetsChanged(target.id)
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = target.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        target.description?.takeIf { it.isNotEmpty() }?.let { desc ->
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    Checkbox(
                        checked = target.id in selectedTargets,
                        onCheckedChange = null // 由Row的clickable处理
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
private fun LinkTargetsSectionEmptyPreview() {
    LinkTargetsSection(
        selectedTargets = emptySet(),
        availableTargets = emptyList(),
        onTargetsChanged = {}
    )
}

@Preview(showBackground = true, name = "Single Target")
@Composable
private fun LinkTargetsSectionSinglePreview() {
    val ledger = Ledger(
        id = "1",
        userId = "user123",
        name = "总账本",
        description = "汇总所有收支",
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z")
    )

    var selected by remember { mutableStateOf(setOf<String>()) }

    LinkTargetsSection(
        selectedTargets = selected,
        availableTargets = listOf(ledger),
        onTargetsChanged = { targetId ->
            selected = if (targetId in selected) {
                selected - targetId
            } else {
                selected + targetId
            }
        }
    )
}

@Preview(showBackground = true, name = "Multiple Targets")
@Composable
private fun LinkTargetsSectionMultiplePreview() {
    val ledgers = listOf(
        Ledger(
            id = "1",
            userId = "user123",
            name = "总账本",
            description = "汇总所有收支",
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-01-01T00:00:00Z")
        ),
        Ledger(
            id = "2",
            userId = "user123",
            name = "家庭账本",
            description = "家庭共同开支",
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-01-01T00:00:00Z")
        ),
        Ledger(
            id = "3",
            userId = "user123",
            name = "投资账本",
            description = null,
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-01-01T00:00:00Z")
        )
    )

    var selected by remember { mutableStateOf(setOf("1", "3")) }

    LinkTargetsSection(
        selectedTargets = selected,
        availableTargets = ledgers,
        onTargetsChanged = { targetId ->
            selected = if (targetId in selected) {
                selected - targetId
            } else {
                selected + targetId
            }
        }
    )
}

@Preview(showBackground = true, name = "Custom Title")
@Composable
private fun LinkTargetsSectionCustomTitlePreview() {
    val ledgers = listOf(
        Ledger(
            id = "1",
            userId = "user123",
            name = "工作账本",
            description = "工作相关收支",
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-01-01T00:00:00Z")
        ),
        Ledger(
            id = "2",
            userId = "user123",
            name = "个人账本",
            description = "个人日常开销",
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-01-01T00:00:00Z")
        )
    )

    LinkTargetsSection(
        selectedTargets = setOf("2"),
        availableTargets = ledgers,
        onTargetsChanged = {},
        title = "选择要同步的账本",
        emptyMessage = "没有找到其他账本"
    )
}