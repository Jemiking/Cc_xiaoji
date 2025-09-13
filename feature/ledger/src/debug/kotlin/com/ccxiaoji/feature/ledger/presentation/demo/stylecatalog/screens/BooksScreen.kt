package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(navController: NavController) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的账本") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { /* add */ }) { Icon(Icons.Filled.Add, contentDescription = null) }
                    IconButton(onClick = { /* help */ }) { Icon(Icons.Filled.HelpOutline, contentDescription = null) }
                }
            )
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
            // 主账本（蓝卡）
            BookCoverCard(
                title = "日常账本",
                subtitle = "日常账本",
                bg = Color(0xFF3B82F6),
                onMoreClick = { navController.navigate(com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DemoScreen.LedgerSettings.route) }
            )
            Spacer(Modifier.height(16.dp))
            // 其它示例卡
            BookCoverCard("泰国旅行", "（示例数据）", bg = Color(0xFF0EA5E9))
            Spacer(Modifier.height(16.dp))
            BookCoverCard("结婚账簿", "（示例数据）", bg = Color(0xFF60A5FA))
            Spacer(Modifier.height(16.dp))
            BookCoverCard("新房装修", "（示例数据）", bg = Color(0xFF94A3B8))

            Spacer(Modifier.height(20.dp))
            // 升级 Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFEDD5))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column { 
                    Text("当前为示例数据", color = Color(0xFF9A3412))
                    Text("请升级为 VIP 账户后再体验", color = Color(0xFF9A3412))
                }
                Button(onClick = { /* upgrade */ }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB923C))) {
                    Text("马上升级")
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun BookCoverCard(title: String, subtitle: String, bg: Color, onMoreClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(bg)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.9f))
            }
            IconButton(onClick = onMoreClick, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Filled.MoreVert, contentDescription = null, tint = Color.White)
            }
        }
    }
}
