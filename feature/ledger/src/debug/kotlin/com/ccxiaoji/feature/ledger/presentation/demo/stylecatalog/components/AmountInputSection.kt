package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 金额输入区域
 * 包含：备注框 + 金额显示 + 快捷按钮
 */
@Composable
fun AmountInputSection(
    amount: String,
    note: String,
    onNoteChange: (String) -> Unit,
    account: String,
    dateTime: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // 备注输入框
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            placeholder = {
                Text(
                    text = "点此输入备注...",
                    color = Color(0xFF999999),
                    fontSize = 14.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp)
        )

        Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)

        // 金额显示
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$amount CNY >",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFF3B30)
            )
        }

        Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)

        // 快捷按钮行
        QuickActionRow(
            account = account,
            dateTime = dateTime
        )

        Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
    }
}

/**
 * 快捷操作按钮行（横向滚动）
 */
@Composable
private fun QuickActionRow(
    account: String,
    dateTime: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickButton(account)
        QuickButton(dateTime)
        QuickButton("报销")
        QuickButton("图片")

        // 旗帜图标
        IconButton(
            onClick = {},
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = "旗帜",
                tint = Color(0xFF8E8E93),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 快捷按钮
 */
@Composable
private fun QuickButton(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
        color = Color.White
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF333333),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}