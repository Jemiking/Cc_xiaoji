package com.ccxiaoji.feature.ledger.presentation.component.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import kotlinx.datetime.Instant

/**
 * 账本选择区域组件
 *
 * 展示当前选中的账本信息，点击后触发账本选择流程。
 *
 * @param selectedLedger 当前选中的账本，null表示未选择
 * @param onLedgerClick 点击账本卡片的回调函数
 * @param modifier 修饰符，用于自定义组件布局
 * @param placeholderText 未选择账本时显示的占位文本
 * @param trailingIcon 卡片右侧显示的图标
 * @param showDescription 是否显示账本描述（如果有）
 *
 * @sample LedgerSelectionSectionPreview
 */
@Composable
fun LedgerSelectionSection(
    selectedLedger: Ledger?,
    onLedgerClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "选择账本",
    trailingIcon: ImageVector = Icons.Default.Book,
    showDescription: Boolean = false
) {
    Card(
        onClick = onLedgerClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = selectedLedger?.name ?: placeholderText,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (showDescription && selectedLedger?.description?.isNotEmpty() == true) {
                    Text(
                        text = selectedLedger.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, name = "No Ledger Selected")
@Composable
private fun LedgerSelectionSectionEmptyPreview() {
    LedgerSelectionSection(
        selectedLedger = null,
        onLedgerClick = {}
    )
}

@Preview(showBackground = true, name = "Daily Ledger")
@Composable
private fun LedgerSelectionSectionDailyPreview() {
    val ledger = Ledger(
        id = "1",
        userId = "user123",
        name = "日常账本",
        description = "记录日常收支",
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z")
    )
    LedgerSelectionSection(
        selectedLedger = ledger,
        onLedgerClick = {}
    )
}

@Preview(showBackground = true, name = "With Description")
@Composable
private fun LedgerSelectionSectionWithDescriptionPreview() {
    val ledger = Ledger(
        id = "2",
        userId = "user123",
        name = "旅行账本",
        description = "2024年日本旅行专用账本",
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z")
    )
    LedgerSelectionSection(
        selectedLedger = ledger,
        onLedgerClick = {},
        showDescription = true
    )
}

@Preview(showBackground = true, name = "Business Ledger")
@Composable
private fun LedgerSelectionSectionBusinessPreview() {
    val ledger = Ledger(
        id = "3",
        userId = "user123",
        name = "生意账本",
        description = "小店经营收支记录",
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z")
    )
    LedgerSelectionSection(
        selectedLedger = ledger,
        onLedgerClick = {},
        showDescription = true,
        placeholderText = "请选择记账本"
    )
}