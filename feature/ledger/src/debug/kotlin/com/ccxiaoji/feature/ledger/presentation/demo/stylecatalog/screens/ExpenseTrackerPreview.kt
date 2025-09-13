package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DemoScreen
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl.QianjiInspiredSpecs
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerPreviewScreen(navController: NavController) {
    val Gray100 = Color(0xFFF3F4F6)
    val BorderGray100 = Color(0xFFF0F0F0)
    val Red500 = Color(0xFFEF4444)

    val screenPadding = 16.dp
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 使用原生 Drawer（推动式 + 原生全屏滑动手势），不再强制设 DrawerSheet 宽度
    SetStatusBar(color = Color.Transparent, darkIcons = drawerState.targetValue == DrawerValue.Open)

    DismissibleNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DismissibleDrawerSheet(drawerContainerColor = Color.White) {
                DrawerContent(navController)
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Gray100)) {
            // 顶部叠层：内容区顶部状态栏高度覆以蓝色
            val statusBarH = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarH)
                    .zIndex(1f)
                    .align(Alignment.TopStart)
            ) {
                // 抽屉自身提供左侧白底，这里仅绘制蓝色覆盖内容区
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(QianjiInspiredSpecs.Colors.Blue500))
            }

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(QianjiInspiredSpecs.Colors.Blue500)
                        .padding(horizontal = screenPadding, vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Filled.Menu, contentDescription = "open drawer", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "2023-07", color = Color.White, fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(text = "本月", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconPlaceholder(); IconPlaceholder(); IconPlaceholder()
                            }
                        }
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(text = "月支出", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                            Text(text = "¥7947.38", color = Color.White, fontSize = 28.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "月收入 ¥4768.00", color = Color.White, fontSize = 14.sp)
                            Text(text = "本月结余 -¥3179.38", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = screenPadding)) {
                    // 预算卡片（白卡）
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column { Text("预算"); Text("剩余: --", color = Color(0xFF6B7280), fontSize = 14.sp) }
                            Column(horizontalAlignment = Alignment.End) { Text("周期: 月", color = Color(0xFF9CA3AF), fontSize = 12.sp); Text("总额: 未设置", color = Color(0xFF6B7280), fontSize = 14.sp) }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 简单分组（示例）
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("07.31 周一")
                                Text("合计 ¥33.71")
                            }
                            Divider(color = BorderGray100)
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Red500))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) { Text("日用品"); Text("香薰", color = Color(0xFF6B7280), fontSize = 14.sp) }
                                Column(horizontalAlignment = Alignment.End) { Text("-¥12.89", color = Red500); Text("· 微信零钱", color = Color(0xFF9CA3AF), fontSize = 12.sp) }
                            }
                        }
                    }

                    Spacer(Modifier.height(100.dp))
                }
            }

            FloatingAddButton(background = QianjiInspiredSpecs.Colors.Blue500, bottomPadding = 32.dp, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun IconPlaceholder() {
    Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(16.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.5f)))
    }
}

@Composable
private fun FloatingAddButton(background: Color, bottomPadding: Dp, modifier: Modifier = Modifier) {
    FloatingActionButton(onClick = { }, containerColor = background, contentColor = Color.White, shape = CircleShape, modifier = modifier.padding(bottom = bottomPadding).size(56.dp)) {
        Text("+", fontSize = 24.sp)
    }
}

@Composable
private fun DrawerContent(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF111827)))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "请叫我摸先生")
                Text(text = "已使用1938天", color = Color(0xFF6B7280), fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        DrawerRow("我的账本", Icons.Filled.AccountCircle) { navController.navigate(DemoScreen.Books.route) }
        DrawerRow("卡片备份", Icons.Filled.CreditCard) { navController.navigate(DemoScreen.CardBackup.route) }
        DrawerRow("分期·周期", Icons.Filled.Autorenew) { navController.navigate(DemoScreen.Installments.route) }
        DrawerRow("设置·关于", Icons.Filled.Settings) { navController.navigate(com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DemoScreen.SettingsAbout.route) }
    }
}

@Composable
private fun DrawerRow(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(56.dp).padding(start = 16.dp, end = 16.dp).clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = Color(0xFF6B7280))
        Text(text)
    }
}

