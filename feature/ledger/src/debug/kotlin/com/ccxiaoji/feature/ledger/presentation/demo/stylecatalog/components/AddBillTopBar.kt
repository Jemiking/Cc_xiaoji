package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.BillTab

/**
 * 记账页面顶部栏
 * 包含：关闭按钮、Tab切换、添加按钮
 */
@Composable
fun AddBillTopBar(
    selectedTab: BillTab,
    onTabSelected: (BillTab) -> Unit,
    onClose: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧：关闭按钮
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 中间：Tab标签组
            TabRow(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )

            // 右侧：添加按钮
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Tab标签行（带下划线动画）
 */
@Composable
private fun TabRow(
    selectedTab: BillTab,
    onTabSelected: (BillTab) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        BillTab.entries.forEach { tab ->
            TabItem(
                text = tab.displayName,
                isSelected = selectedTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

/**
 * 单个Tab项（带下划线）
 */
@Composable
private fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color(0xFF999999)
        )

        // 动画下划线
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(32.dp)
                    .height(3.dp)
                    .background(Color.Black)
            )
        }
    }
}