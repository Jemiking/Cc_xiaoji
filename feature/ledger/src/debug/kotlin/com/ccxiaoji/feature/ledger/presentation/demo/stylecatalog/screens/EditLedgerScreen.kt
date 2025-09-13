package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLedgerScreen(navController: NavController) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("修改账本") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Button(onClick = { }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))) {
                    Text("保存", color = Color.White)
                }
            }
        }
    ) { padding ->
        val gray = Color(0xFFF3F4F6)
        Column(modifier = Modifier.fillMaxSize().background(gray).padding(padding)) {
            Spacer(Modifier.height(12.dp))
            // 封面块
            Box(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF3B82F6)))
            Spacer(Modifier.height(12.dp))

            // 列表项
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingRow("封面图", right = { Text(" ") })
                    Divider()
                    SettingRow("显示方式", right = { Text("按月") })
                    Divider()
                    SettingRow("月份起始日", right = { Text("01") })
                }
            }
        }
    }
}

@Composable
private fun SettingRow(title: String, right: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title)
        Row(content = right)
    }
}
