package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 数字键盘
 * 布局：4列×4行
 */
@Composable
fun NumberKeyboard(
    onNumberClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    onSaveAndNewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFF7F7F7)
    ) {
        Column {
            Divider(color = Color(0xFFE5E5E5), thickness = 1.dp)

            // 第1行：1 2 3 删除
            KeyboardRow {
                NumberKey("1") { onNumberClick("1") }
                NumberKey("2") { onNumberClick("2") }
                NumberKey("3") { onNumberClick("3") }
                OperatorKey(Icons.Default.Backspace, onDeleteClick)
            }

            // 第2行：4 5 6 -
            KeyboardRow {
                NumberKey("4") { onNumberClick("4") }
                NumberKey("5") { onNumberClick("5") }
                NumberKey("6") { onNumberClick("6") }
                OperatorKey("-") { onOperatorClick("-") }
            }

            // 第3行：7 8 9 +
            KeyboardRow {
                NumberKey("7") { onNumberClick("7") }
                NumberKey("8") { onNumberClick("8") }
                NumberKey("9") { onNumberClick("9") }
                OperatorKey("+") { onOperatorClick("+") }
            }

            // 第4行：再记 0 . 保存
            KeyboardRow {
                TextKey("再记", onSaveAndNewClick)
                NumberKey("0") { onNumberClick("0") }
                NumberKey(".") { onNumberClick(".") }
                SaveKey(onSaveClick)
            }
        }
    }
}

/**
 * 键盘行容器
 */
@Composable
private fun KeyboardRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        content()
    }
}

/**
 * 数字键（0-9 和 .）
 */
@Composable
private fun RowScope.NumberKey(
    number: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .background(Color.White)
            .border(0.5.dp, Color(0xFFE5E5E5)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontSize = 24.sp,
            color = Color.Black
        )
    }
}

/**
 * 运算符键（+ - 等）
 */
@Composable
private fun RowScope.OperatorKey(
    operator: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .background(Color(0xFFF7F7F7))
            .border(0.5.dp, Color(0xFFE5E5E5)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = operator,
            fontSize = 24.sp,
            color = Color(0xFF666666)
        )
    }
}

/**
 * 图标运算符键（删除按钮）
 */
@Composable
private fun RowScope.OperatorKey(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .background(Color(0xFFF7F7F7))
            .border(0.5.dp, Color(0xFFE5E5E5)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "删除",
            tint = Color(0xFF666666),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 文字键（再记）
 */
@Composable
private fun RowScope.TextKey(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .background(Color.White)
            .border(0.5.dp, Color(0xFFE5E5E5)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

/**
 * 保存键（红色高亮）
 */
@Composable
private fun RowScope.SaveKey(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .background(Color(0xFFFF6B6B)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "保存",
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}