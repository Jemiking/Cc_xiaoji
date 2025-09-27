package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.foundation.BorderStroke
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DemoScreen
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSettingsScreen(navController: NavController) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账本设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        val grayBg = Color(0xFFF3F4F6)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(grayBg)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // 分组：设置（标题置于卡片内）
            SettingsCard {
                GroupHeaderInCard("设置")
                SettingsRow(
                    title = "修改",
                    trailing = { Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFC5C8CE)) },
                    onClick = { navController.navigate(DemoScreen.BookEdit.route) }
                )
                Divider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                SettingsRow(
                    title = "报表统计",
                    trailing = { Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFC5C8CE)) },
                    onClick = { navController.navigate(com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DemoScreen.ReportStats.route) }
                )
                Divider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                SettingsRow(
                    title = "分类管理",
                    trailing = { Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFC5C8CE)) },
                    onClick = { navController.navigate(DemoScreen.CategoryManagement.route) }
                )
                Divider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)

                val hidden = remember { mutableStateOf(false) }
                SettingsRow(
                    title = "隐藏账本",
                    subtitle = "记账时将不能选中，可在 已隐藏账本 页面恢复",
                    trailing = {
                        Switch(
                            checked = hidden.value,
                            onCheckedChange = { hidden.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3B82F6),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE5E7EB)
                            )
                        )
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // 分组：操作（标题置于卡片内）
            SettingsCard {
                GroupHeaderInCard("操作")
                SettingsRow(
                    title = "迁移账本",
                    trailing = { Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFC5C8CE)) },
                    onClick = { navController.navigate(DemoScreen.MigrateBook.route) }
                )
                Divider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                SettingsRow(
                    title = "清除账单",
                    trailing = { Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFC5C8CE)) },
                    onClick = { navController.navigate(DemoScreen.ClearBills.route) }
                )
                Divider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                SettingsRow(
                    title = "删除账本",
                    trailing = { Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFC5C8CE)) },
                    onClick = { }
                )
            }

            Spacer(Modifier.height(16.dp))

            // 分组：账本成员
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GroupTitle(text = "账本成员")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "账本权限",
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Outlined.HelpOutline, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp)
                        .clickable { },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3B82F6)),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text("＋ 邀请成员", color = Color(0xFF3B82F6))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun GroupTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFF6B7280),
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 16.sp, color = Color(0xFF111827))
            trailing?.invoke()
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun GroupHeaderInCard(text: String) {
    Text(
        text = text,
        color = Color(0xFF6B7280),
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
    )
}
