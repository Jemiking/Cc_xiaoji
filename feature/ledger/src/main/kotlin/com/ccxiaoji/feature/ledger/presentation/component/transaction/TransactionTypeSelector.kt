package com.ccxiaoji.feature.ledger.presentation.component.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionType

/**
 * 交易类型选择器组件
 *
 * 提供收支/转账类型的切换选择功能，支持自定义可用类型和标签文案。
 *
 * @param selectedType 当前选中的交易类型
 * @param onTypeSelected 类型选择变化的回调函数
 * @param modifier 修饰符，用于自定义组件布局
 * @param availableTypes 可用的交易类型列表，默认包含支出、收入、转账
 * @param labelProvider 提供每种类型显示标签的函数，支持自定义文案
 *
 * @sample TransactionTypeSelectorPreview
 */
@Composable
fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
    availableTypes: List<TransactionType> = listOf(
        TransactionType.EXPENSE,
        TransactionType.INCOME,
        TransactionType.TRANSFER
    ),
    labelProvider: (TransactionType) -> String = { type ->
        when (type) {
            TransactionType.EXPENSE -> "支出"
            TransactionType.INCOME -> "收入"
            TransactionType.TRANSFER -> "转账"
            else -> ""
        }
    }
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        availableTypes.forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(text = labelProvider(type))
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "Default State")
@Composable
private fun TransactionTypeSelectorPreview() {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    TransactionTypeSelector(
        selectedType = selectedType,
        onTypeSelected = { selectedType = it }
    )
}

@Preview(showBackground = true, name = "Limited Types")
@Composable
private fun TransactionTypeSelectorLimitedPreview() {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    TransactionTypeSelector(
        selectedType = selectedType,
        onTypeSelected = { selectedType = it },
        availableTypes = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
    )
}

@Preview(showBackground = true, name = "Custom Labels")
@Composable
private fun TransactionTypeSelectorCustomLabelsPreview() {
    var selectedType by remember { mutableStateOf(TransactionType.INCOME) }
    TransactionTypeSelector(
        selectedType = selectedType,
        onTypeSelected = { selectedType = it },
        labelProvider = { type ->
            when (type) {
                TransactionType.EXPENSE -> "花钱"
                TransactionType.INCOME -> "赚钱"
                TransactionType.TRANSFER -> "转账"
                else -> ""
            }
        }
    )
}