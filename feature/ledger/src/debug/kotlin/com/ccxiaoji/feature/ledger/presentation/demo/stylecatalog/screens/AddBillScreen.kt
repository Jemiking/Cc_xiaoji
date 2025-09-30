package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components.*
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.AddBillState
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.BillTab

/**
 * 记账主屏幕
 * 全屏Dialog形式展示
 */
@Composable
fun AddBillScreen(
    onDismiss: () -> Unit,
    onSave: (AddBillState) -> Unit
) {
    var state by remember { mutableStateOf(AddBillState()) }
    val calculator = remember { AmountCalculator() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                AddBillTopBar(
                    selectedTab = state.selectedTab,
                    onTabSelected = { newTab ->
                        state = state.copy(
                            selectedTab = newTab,
                            selectedCategory = null
                        )
                    },
                    onClose = onDismiss,
                    onAdd = {
                        // TODO: 添加自定义分类功能
                        Log.d("AddBillScreen", "添加分类按钮点击")
                    }
                )
            },
            bottomBar = {
                Column {
                    AmountInputSection(
                        amount = state.amount,
                        note = state.note,
                        onNoteChange = { state = state.copy(note = it) },
                        account = state.account,
                        dateTime = state.dateTime
                    )

                    NumberKeyboard(
                        onNumberClick = { digit ->
                            val newAmount = calculator.input(digit)
                            state = state.copy(amount = newAmount)
                        },
                        onOperatorClick = { operator ->
                            val newAmount = calculator.operator(operator)
                            state = state.copy(amount = newAmount)
                        },
                        onDeleteClick = {
                            val newAmount = calculator.delete()
                            state = state.copy(amount = newAmount)
                        },
                        onSaveClick = {
                            if (state.selectedCategory != null) {
                                onSave(state)
                                onDismiss()
                            } else {
                                Log.d("AddBillScreen", "请选择分类")
                            }
                        },
                        onSaveAndNewClick = {
                            if (state.selectedCategory != null) {
                                onSave(state)
                                // 重置状态，保留Tab
                                state = AddBillState(selectedTab = state.selectedTab)
                                calculator.reset()
                            } else {
                                Log.d("AddBillScreen", "请选择分类")
                            }
                        }
                    )
                }
            },
            containerColor = Color.White
        ) { padding ->
            // 分类网格
            val categories = CategoryData.getCategoriesByTab(state.selectedTab)

            CategoryGrid(
                categories = categories,
                selectedCategory = state.selectedCategory,
                onCategorySelected = { category ->
                    state = state.copy(selectedCategory = category)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White)
            )
        }
    }
}

/**
 * 金额计算器
 * 处理数字输入、运算符、删除等逻辑
 */
class AmountCalculator {
    private var expression = ""
    private var currentNumber = "0"
    private var hasDecimal = false

    /**
     * 输入数字或小数点
     */
    fun input(digit: String): String {
        when {
            digit == "." -> {
                if (!hasDecimal) {
                    currentNumber += "."
                    hasDecimal = true
                }
            }
            currentNumber == "0" && digit != "." -> {
                currentNumber = digit
            }
            else -> {
                currentNumber += digit
            }
        }
        return evaluate()
    }

    /**
     * 输入运算符
     */
    fun operator(op: String): String {
        if (currentNumber.isNotEmpty()) {
            expression += currentNumber + op
            currentNumber = ""
            hasDecimal = false
        }
        return evaluate()
    }

    /**
     * 删除最后一个字符
     */
    fun delete(): String {
        if (currentNumber.isNotEmpty()) {
            if (currentNumber.last() == '.') {
                hasDecimal = false
            }
            currentNumber = currentNumber.dropLast(1)
            if (currentNumber.isEmpty()) {
                currentNumber = "0"
            }
        } else if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
        }
        return evaluate()
    }

    /**
     * 重置计算器
     */
    fun reset() {
        expression = ""
        currentNumber = "0"
        hasDecimal = false
    }

    /**
     * 计算表达式
     */
    private fun evaluate(): String {
        return try {
            val fullExpression = expression + currentNumber
            if (fullExpression.isEmpty() || fullExpression == "0") {
                return "0.0"
            }

            // 简单表达式计算（只支持 + 和 -）
            val result = calculateExpression(fullExpression)
            String.format("%.2f", result)
        } catch (e: Exception) {
            currentNumber.ifEmpty { "0.0" }
        }
    }

    /**
     * 计算带加减的表达式
     */
    private fun calculateExpression(expr: String): Double {
        if (expr.isEmpty()) return 0.0

        var result = 0.0
        var currentNum = ""
        var operation = '+'

        for (i in expr.indices) {
            val char = expr[i]

            when {
                char.isDigit() || char == '.' -> {
                    currentNum += char
                }
                char == '+' || char == '-' -> {
                    if (currentNum.isNotEmpty()) {
                        val num = currentNum.toDoubleOrNull() ?: 0.0
                        result = when (operation) {
                            '+' -> result + num
                            '-' -> result - num
                            else -> num
                        }
                        currentNum = ""
                    }
                    operation = char
                }
            }
        }

        // 处理最后一个数字
        if (currentNum.isNotEmpty()) {
            val num = currentNum.toDoubleOrNull() ?: 0.0
            result = when (operation) {
                '+' -> result + num
                '-' -> result - num
                else -> num
            }
        }

        return result
    }
}