package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookEditScreen(navController: NavController) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("修改账本") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) { Text("保存", color = Color.White) }
            }
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
            // 顶部大蓝块
            Surface(
                modifier = Modifier.fillMaxWidth().height(156.dp),
                color = Color(0xFF3B82F6),
                shape = RoundedCornerShape(12.dp)
            ) {}

            Spacer(Modifier.height(12.dp))
            // 行列表
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    EditRow(title = "封面图", subtitle = "封面图会作为主界面的背景图来展示")
                    Divider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                    EditRow(title = "显示方式", value = "按月")
                    Divider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                    EditRow(title = "月份起始日", value = "01", withInfoIcon = true)
                }
            }
        }
    }
}

@Composable
private fun EditRow(
    title: String,
    subtitle: String? = null,
    value: String? = null,
    withInfoIcon: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 16.sp, color = Color(0xFF111827))
                if (withInfoIcon) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                value?.let { Text(it, color = Color(0xFF3B82F6)) }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFC5C8CE))
            }
        }
        if (subtitle != null) {
            Text(subtitle, color = Color(0xFF9CA3AF), fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
        }
    }
}
