package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 数字键盘组件
 *
 * 提供数字输入、运算符和操作按钮
 *
 * @param onNumberClick 数字按键回调
 * @param onDotClick 小数点按键回调
 * @param onBackspaceClick 退格按键回调
 * @param onPlusClick 加号按键回调
 * @param onMinusClick 减号按键回调
 * @param onEqualsClick 等号按键回调
 * @param onSaveClick 保存按键回调
 * @param modifier 修饰符
 * @param enabled 是否启用
 * @param showOperators 是否显示运算符
 * @param showSaveButton 是否显示保存按钮
 * @param saveButtonText 保存按钮文本
 * @param saveButtonEnabled 保存按钮是否启用
 */
@Composable
fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onDotClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onPlusClick: (() -> Unit)? = null,
    onMinusClick: (() -> Unit)? = null,
    onEqualsClick: (() -> Unit)? = null,
    onSaveClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showOperators: Boolean = true,
    showSaveButton: Boolean = true,
    saveButtonText: String = "保存",
    saveButtonEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 数字键盘布局
        val keypadLayout = remember {
            listOf(
                listOf("7", "8", "9"),
                listOf("4", "5", "6"),
                listOf("1", "2", "3"),
                listOf(".", "0", "⌫")
            )
        }

        // 前三行数字键
        keypadLayout.take(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        text = key,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNumberClick(key)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = enabled
                    )
                }

                // 运算符列（如果启用）
                if (showOperators) {
                    when (row[0]) {
                        "7" -> OperatorButton(
                            icon = Icons.Default.Add,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPlusClick?.invoke()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = enabled && onPlusClick != null
                        )
                        "4" -> OperatorButton(
                            icon = Icons.Default.Remove,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onMinusClick?.invoke()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = enabled && onMinusClick != null
                        )
                        "1" -> if (onEqualsClick != null) {
                            EqualsButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onEqualsClick()
                                },
                                modifier = Modifier.weight(1f),
                                enabled = enabled
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 最后一行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 小数点
            KeypadButton(
                text = ".",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDotClick()
                },
                modifier = Modifier.weight(1f),
                enabled = enabled
            )

            // 数字0
            KeypadButton(
                text = "0",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNumberClick("0")
                },
                modifier = Modifier.weight(1f),
                enabled = enabled
            )

            // 退格键
            BackspaceButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBackspaceClick()
                },
                modifier = Modifier.weight(1f),
                enabled = enabled
            )

            // 保存按钮（如果启用）
            if (showSaveButton && onSaveClick != null) {
                SaveButton(
                    text = saveButtonText,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSaveClick()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = enabled && saveButtonEnabled
                )
            } else if (showOperators) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * 键盘按钮基础组件
 */
@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.38f))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

/**
 * 运算符按钮
 */
@Composable
private fun OperatorButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

/**
 * 退格按钮
 */
@Composable
private fun BackspaceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Backspace,
            contentDescription = "退格",
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

/**
 * 等号按钮
 */
@Composable
private fun EqualsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "=",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) MaterialTheme.colorScheme.tertiary
                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

/**
 * 保存按钮
 */
@Composable
private fun SaveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (enabled) DesignTokens.BrandColors.Ledger
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) Color.White
                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}