package com.ccxiaoji.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IconPicker(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val expenseIcons = listOf(
        "🍜", "🍔", "🍕", "☕", "🍱", // 餐饮
        "🚇", "🚌", "🚗", "✈️", "🚕", // 交通
        "🛍️", "👔", "👠", "💄", "🎁", // 购物
        "🎮", "🎬", "🎵", "📺", "🎯", // 娱乐
        "🏥", "💊", "🩺", "🦷", "👓", // 医疗
        "📚", "✏️", "🎓", "💻", "📖", // 教育
        "🏠", "🛏️", "🚿", "🔑", "🏢", // 居住
        "💡", "💧", "🔥", "❄️", "🌡️", // 水电
        "📱", "📞", "💬", "📧", "🌐", // 通讯
        "🏃", "🏋️", "⚽", "🏀", "🎾", // 运动
        "🐕", "🐈", "🌸", "🎨", "✂️", // 其他
        "📌", "⭐", "💼", "🔧", "📦"  // 通用
    )

    val incomeIcons = listOf(
        "💰", "💵", "💴", "💶", "💷", // 现金
        "🎁", "🏆", "🥇", "🎖️", "💎", // 奖励
        "📈", "📊", "💹", "🏦", "💳", // 投资
        "💼", "🏢", "👔", "🤝", "📋", // 工作
        "💸", "🪙", "💲", "🏅", "🎯"  // 其他收入
    )

    val icons = expenseIcons + incomeIcons

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(icons.distinct()) { icon ->
            IconItem(
                icon = icon,
                isSelected = icon == selectedIcon,
                onClick = { onIconSelected(icon) }
            )
        }
    }
}

@Composable
private fun IconItem(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}