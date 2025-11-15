package com.ccxiaoji.feature.ledger.presentation.component.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * 金额输入区域组件
 *
 * 提供金额输入功能，支持表达式输入（如100+50），可以显示计算结果和错误提示。
 *
 * @param amountText 当前输入的文本内容
 * @param evaluatedAmount 表达式求值后的金额，null表示尚未计算或计算失败
 * @param amountError 错误提示信息，null表示无错误
 * @param onAmountChanged 金额文本变化的回调函数
 * @param modifier 修饰符，用于自定义组件布局
 * @param label 输入框的标签文本
 * @param showExpressionResult 是否显示表达式计算结果
 *
 * @sample AmountInputSectionPreview
 */
@Composable
fun AmountInputSection(
    amountText: String,
    evaluatedAmount: Double?,
    amountError: String?,
    onAmountChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "金额",
    showExpressionResult: Boolean = true
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = amountText,
            onValueChange = onAmountChanged,
            label = { Text(label) },
            isError = amountError != null,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                when {
                    // 优先显示错误信息
                    amountError != null -> {
                        Text(text = amountError)
                    }
                    // 显示表达式计算结果
                    showExpressionResult &&
                    evaluatedAmount != null &&
                    amountText.contains(Regex("[+\\-*/]")) -> {
                        Text(text = "= $evaluatedAmount")
                    }
                }
            },
            singleLine = true
        )
    }
}

@Preview(showBackground = true, name = "Empty Input")
@Composable
private fun AmountInputSectionEmptyPreview() {
    var amountText by remember { mutableStateOf("") }
    AmountInputSection(
        amountText = amountText,
        evaluatedAmount = null,
        amountError = null,
        onAmountChanged = { amountText = it }
    )
}

@Preview(showBackground = true, name = "Simple Amount")
@Composable
private fun AmountInputSectionSimplePreview() {
    var amountText by remember { mutableStateOf("100") }
    AmountInputSection(
        amountText = amountText,
        evaluatedAmount = 100.0,
        amountError = null,
        onAmountChanged = { amountText = it }
    )
}

@Preview(showBackground = true, name = "Expression Input")
@Composable
private fun AmountInputSectionExpressionPreview() {
    var amountText by remember { mutableStateOf("100+50") }
    AmountInputSection(
        amountText = amountText,
        evaluatedAmount = 150.0,
        amountError = null,
        onAmountChanged = { amountText = it }
    )
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun AmountInputSectionErrorPreview() {
    var amountText by remember { mutableStateOf("abc") }
    AmountInputSection(
        amountText = amountText,
        evaluatedAmount = null,
        amountError = "请输入有效的金额",
        onAmountChanged = { amountText = it }
    )
}