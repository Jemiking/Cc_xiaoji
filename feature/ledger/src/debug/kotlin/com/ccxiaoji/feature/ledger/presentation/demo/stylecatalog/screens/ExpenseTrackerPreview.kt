package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DemoScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerPreviewScreen(navController: NavController) {
    val screenPadding = 16.dp
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    DismissibleNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DismissibleDrawerSheet(
                drawerContainerColor = Color.White,
                drawerContentColor = Color(0xFF374151),
                modifier = Modifier.fillMaxWidth(0.72f)
            ) { DrawerContent(navController) }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F6))) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部头部
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF3B82F6),
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = screenPadding, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { scope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() } }) { Icon(Icons.Filled.Menu, contentDescription = "菜单", tint = Color.White) }
                                Spacer(Modifier.width(4.dp))
                                Column {
                                    Text(text = "2023-07", color = Color.White, fontSize = 18.sp)
                                    Text(text = "本月", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconPlaceholder()
                                IconPlaceholder()
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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

                // 内容卡片（示例）
                Column(modifier = Modifier.padding(horizontal = screenPadding)) {
                    Spacer(Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column { Text("预算"); Text("剩余: --", color = Color(0xFF6B7280), fontSize = 14.sp) }
                            Column(horizontalAlignment = Alignment.End) { Text("周期: 月", color = Color(0xFF9CA3AF), fontSize = 12.sp); Text("总额: 未设置", color = Color(0xFF6B7280), fontSize = 14.sp) }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("07.31 周一")
                                Text("合计 ¥33.71")
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("日用品")
                                    Text("香薰", color = Color(0xFF6B7280), fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("-¥12.89", color = Color(0xFFEF4444))
                                    Text("· 微信零钱", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(100.dp))
                }
            }

            // 悬浮添加按钮
            FloatingAddButton(
                background = Color(0xFF3B82F6),
                bottomPadding = 32.dp,
                modifier = Modifier.align(Alignment.BottomCenter),
                navController = navController
            )
        }
    }
}

@Composable
private fun IconPlaceholder() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun FloatingAddButton(
    background: Color,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val (showAddBillDialog, setShowAddBillDialog) = remember { mutableStateOf(false) }
    Box(modifier = modifier.padding(bottom = bottomPadding)) {
        FloatingActionButton(
            onClick = { setShowAddBillDialog(true) },
            containerColor = background,
            contentColor = Color.White
        ) { Text("+", fontSize = 24.sp) }
    }
    // 简化：Demo 不弹窗，仅占位
}

@Composable
private fun DrawerContent(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        DrawerRow(text = "我的账本", icon = Icons.Filled.AccountCircle) { navController.navigate(DemoScreen.Books.route) }
        DrawerRow(text = "报销管理", icon = Icons.Filled.Assignment) { /* TODO */ }
        DrawerRow(text = "搜索账单", icon = Icons.Filled.Search) { /* TODO */ }
        DrawerRow(text = "卡片备份", icon = Icons.Filled.CreditCard) { navController.navigate(DemoScreen.CardBackup.route) }
        DrawerRow(text = "分期·周期", icon = Icons.Filled.SyncAlt) { navController.navigate(DemoScreen.Installments.route) }
        DrawerRow(text = "存钱计划", icon = Icons.Filled.Wallet) { /* TODO */ }
        DrawerRow(text = "意见反馈", icon = Icons.Filled.QuestionAnswer) { /* TODO */ }
        DrawerRow(text = "设置·关于", icon = Icons.Filled.Settings) { navController.navigate(DemoScreen.SettingsAbout.route) }
    }
}

@Composable
private fun DrawerRow(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val textColor = Color(0xFF374151)
    val iconColor = Color(0xFF9CA3AF)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor)
        Spacer(Modifier.width(16.dp))
        Text(text = text, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Normal)
    }
}
