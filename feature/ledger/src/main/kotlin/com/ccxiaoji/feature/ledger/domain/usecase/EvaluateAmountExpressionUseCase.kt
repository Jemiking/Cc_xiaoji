package com.ccxiaoji.feature.ledger.domain.usecase

import javax.inject.Inject

/**
 * 金额表达式解析结果
 */
data class AmountEvaluationResult(
    val error: String?,
    val evaluatedAmount: Double?
)

/**
 * 金额表达式解析与计算UseCase
 *
 * 支持的语法：
 * - 数字与小数点（最多两位小数）
 * - 一元 +/- （表达式开头或连续运算符时）
 * - 二元 +/- （数字之间的加减运算）
 *
 * 示例：
 * - "100" -> 100.0
 * - "100+50" -> 150.0
 * - "100-30" -> 70.0
 * - "100+50-30" -> 120.0
 * - "-100" -> -100.0 (但会被后续验证拒绝)
 * - "100.50+0.5" -> 101.0
 */
class EvaluateAmountExpressionUseCase @Inject constructor() {

    /**
     * 解析并计算金额表达式
     *
     * @param expression 金额表达式字符串
     * @return 解析结果，包含错误信息或计算结果
     */
    operator fun invoke(expression: String): AmountEvaluationResult {
        if (expression.isBlank()) {
            return AmountEvaluationResult(null, null)
        }

        // 扫描生成 token：numbers 与 ops
        val operators = mutableListOf<Char>()
        val numbers = mutableListOf<String>()
        val currentNumber = StringBuilder()

        fun flushNumber() {
            if (currentNumber.isNotEmpty()) {
                numbers.add(currentNumber.toString())
                currentNumber.clear()
            }
        }

        var index = 0
        while (index < expression.length) {
            val char = expression[index]

            when (char) {
                '+', '-' -> {
                    if (index == 0) {
                        // 允许一元 +/- 在开头
                        currentNumber.append(char)
                    } else {
                        val prevChar = expression[index - 1]
                        if (prevChar == '+' || prevChar == '-') {
                            // 连续操作符：仅允许后一元负号（形如 1+-2）
                            if (char == '-' && currentNumber.isEmpty()) {
                                currentNumber.append(char)
                            } else {
                                return AmountEvaluationResult("表达式不合法", null)
                            }
                        } else {
                            // 需要前面已有数字（不以点或符号结尾）
                            if (currentNumber.isEmpty()) {
                                return AmountEvaluationResult("表达式不合法", null)
                            }
                            if (currentNumber.last() == '.') {
                                return AmountEvaluationResult("小数点位置不合法", null)
                            }
                            flushNumber()
                            operators.add(char)
                        }
                    }
                }

                '.' -> {
                    // 当前操作数只能有一个 '.'
                    if (currentNumber.contains('.')) {
                        return AmountEvaluationResult("每个数最多一个小数点", null)
                    }
                    // '.' 可出现在一元符号之后（例如 -.5）或数字之后
                    if (currentNumber.isEmpty() ||
                        (currentNumber.length == 1 && (currentNumber[0] == '+' || currentNumber[0] == '-'))) {
                        currentNumber.append('0') // 规范化为 0.x
                    }
                    currentNumber.append('.')
                }

                in '0'..'9' -> {
                    // 小数位限制：小数点后最多两位
                    val dotIndex = currentNumber.indexOf('.')
                    if (dotIndex != -1 && currentNumber.length - dotIndex - 1 >= 2) {
                        return AmountEvaluationResult("小数位最多两位", null)
                    }
                    currentNumber.append(char)
                }

                else -> {
                    return AmountEvaluationResult("含有非法字符", null)
                }
            }
            index++
        }

        // 末尾必须是数字，不能是操作符或裸 '.'
        if (currentNumber.isEmpty()) {
            return AmountEvaluationResult("表达式不完整", null)
        }
        if (currentNumber.last() == '.') {
            return AmountEvaluationResult("小数点位置不合法", null)
        }
        flushNumber()

        // 计算：从左到右
        if (numbers.isEmpty()) {
            return AmountEvaluationResult("请输入金额", null)
        }
        if (numbers.size != operators.size + 1) {
            return AmountEvaluationResult("表达式不合法", null)
        }

        // 解析第一个数字
        var accumulator = numbers[0].toDoubleOrNull()
            ?: return AmountEvaluationResult("金额格式不正确", null)

        // 执行运算
        for (i in operators.indices) {
            val nextNumber = numbers[i + 1].toDoubleOrNull()
                ?: return AmountEvaluationResult("金额格式不正确", null)

            when (operators[i]) {
                '+' -> accumulator += nextNumber
                '-' -> accumulator -= nextNumber
            }
        }

        // 验证结果
        if (accumulator <= 0.0) {
            return AmountEvaluationResult("金额必须大于0", null)
        }

        // 保留两位小数用于展示/入库
        val rounded = kotlin.math.round(accumulator * 100.0) / 100.0

        return AmountEvaluationResult(null, rounded)
    }
}