package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerSettingsScreen(navController: NavController) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)
    var hidden by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账本设置") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        val gray = Color(0xFFF3F4F6)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gray)
                .padding(padding)
        ) {
            // 设置分组
            GroupCard {
                TitleRow("修改", onClick = { navController.navigate(com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DemoScreen.EditLedger.route) })
                TitleRow("报表统计")
                TitleRow("分类管理")
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("隐藏账本"); Text("记账时将不能选中，可在 已隐藏账本 页面恢复", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodySmall) }
                    Switch(checked = hidden, onCheckedChange = { hidden = it })
                }
            }

            // 操作分组
            GroupCard {
                TitleRow("迁移账本")
                TitleRow("清除账单")
                TitleRow("删除账本")
            }

            // 成员分组
            GroupCard {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("账本成员")
                    Text("账本权限？", color = Color(0xFF6B7280))
                }
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(10.dp)).background(Color.White)) {
                    OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth().padding(8.dp)) { Text("＋ 邀请成员") }
                }
            }
        }
    }
}

@Composable
private fun GroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun TitleRow(title: String, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title)
        Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFF9CA3AF))
    }
}
