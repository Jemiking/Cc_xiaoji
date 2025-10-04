package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DemoScreen
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardBackupScreenNew(navController: NavController) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("卡片备份") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, null) }
                },
                actions = { IconButton(onClick = { }) { Icon(Icons.Filled.MoreVert, null) } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(DemoScreen.AddCard.route) },
                containerColor = Color(0xFF3B82F6)
            ) { Text("＋添加卡片", color = Color.White) }
        }
    ) { padding ->
        val gray = Color(0xFFF3F4F6)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gray)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            // 搜索框（静态）
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    text = "搜索：银行、卡号、户名、备注",
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            Spacer(Modifier.height(12.dp))

            // 示例卡片（静态占位）
            listOf(
                Triple("农业银行", "储蓄卡 崔芳榕", "6228 4801 5710 7413 674"),
                Triple("光大银行", "储蓄卡 海口金贸支行", "6214 9211 0594 5147"),
                Triple("机场工资卡-交通银行", "储蓄卡", "6222 6211 1000 9367 897"),
                Triple("建行工资卡", "储蓄卡 木容杰", "6217 0029 2011 3972 278"),
                Triple("中国银行", "储蓄卡 木容杰", "6216 6326 0000 2247 079"),
                Triple("建设银行", "储蓄卡", "6217 0035 2001 9926 235")
            ).forEach { (bank, sub, number) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(bank, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text(sub, color = Color(0xFF9CA3AF), fontSize = 12.sp)
                        Text(number, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(72.dp)) // 为 FAB 留空
        }
    }
}

