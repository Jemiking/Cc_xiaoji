package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun InstallmentsScreen(navController: NavController) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)
    var selectedTab by remember { mutableStateOf(0) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedTab == 0) "分期管理" else "周期记账") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, null) }
                },
                actions = { IconButton(onClick = { }) { Icon(Icons.Filled.HelpOutline, null) } }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) { Text("＋ 添加", color = Color.White) }
            }
        }
    ) { padding ->
        val gray = Color(0xFFF3F4F6)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gray)
                .padding(padding)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("分期管理") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("周期记账") })
            }
            Spacer(Modifier.height(12.dp))

            if (selectedTab == 0) {
                // 分期管理卡片
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column { Text("日用品-电饭煲+刀砧", fontSize = 18.sp, fontWeight = FontWeight.Medium); Text("京东白条", color = Color(0xFF9CA3AF), fontSize = 12.sp) }
                            Surface(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp)) { Text(" 已完成 ", color = Color(0xFF6B7280), fontSize = 12.sp) }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("¥416.10", fontWeight = FontWeight.SemiBold); Text("本金总额", color = Color(0xFF9CA3AF), fontSize = 12.sp) }
                            Column { Text("¥0.00", fontWeight = FontWeight.SemiBold); Text("利息总额", color = Color(0xFF9CA3AF), fontSize = 12.sp) }
                            Column(horizontalAlignment = Alignment.End) { Text("¥416.10", fontWeight = FontWeight.SemiBold); Text("应还总额", color = Color(0xFF9CA3AF), fontSize = 12.sp) }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("开始日期 2023-11-12", color = Color(0xFF6B7280), fontSize = 12.sp)
                            Text("6/6", color = Color(0xFF6B7280))
                        }
                    }
                }
            } else {
                // 周期记账卡片
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column { Text("支出·交通", fontSize = 18.sp, fontWeight = FontWeight.Medium); Text("轮胎静音棉", color = Color(0xFF9CA3AF), fontSize = 12.sp) }
                            Surface(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp)) { Text(" 已完成 ", color = Color(0xFF6B7280), fontSize = 12.sp) }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column { Text("账单金额", color = Color(0xFF9CA3AF), fontSize = 12.sp); Text("105.34", color = Color(0xFFEF4444), fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                            Column(horizontalAlignment = Alignment.End) { Text("执行次数", color = Color(0xFF9CA3AF), fontSize = 12.sp); Text("3", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                        }
                        Text("每月(8) 记录3次后结束", color = Color(0xFF6B7280), fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                val tips = if (selectedTab == 0) listOf(
                    "1.分期功能，专门用于管理信用卡分期的场景，请按照你的信用卡分期情况设定，然后会每个月自动帮你入账；",
                    "2.入账的账单为支出类型；",
                    "3.分期任务不支持修改，请设置前仔细确认；",
                    "4.分期入账的账单，与手动记录的账单无区别，可以修改、删除；"
                ) else listOf(
                    "1.设定固定周期，自动为你记录账单，周期、结束方式、类别，都可以自行设置；",
                    "2.设定的周期记账支持修改，但是修改只对后面生成的账单有效，不影响已经生成的账单；",
                    "3.周期记账生成的账单，与手动记录的账单无区别，可以再次修改，或者删除"
                )
                tips.forEach { Text(it, color = Color(0xFF6B7280), fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp)) }
            }

            Spacer(Modifier.height(72.dp))
        }
    }
}
