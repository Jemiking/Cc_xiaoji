package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
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
            DismissibleDrawerSheet(
                drawerContainerColor = Color.White,
                drawerContentColor = Color(0xFF374151),
                modifier = Modifier.fillMaxWidth(0.85f)  // 限制抽屉宽度为屏幕的85%
            ) {
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
    val textColor = Color(0xFF374151)  // 更深的灰色
    val subTextColor = Color(0xFF9CA3AF)  // 更浅的副文字色
    val iconColor = Color(0xFF9CA3AF)  // 调整图标颜色

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)  // 确保背景是白色
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp) // 顶部32dp，底部16dp
    ) {
        // 用户头像区域
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 50.dp) // 调整为50dp
        ) {
            // 黑色圆形头像，含自定义西装图标
            Box(
                modifier = Modifier
                    .size(48.dp) // 调整头像大小
                    .clip(CircleShape)
                    .background(Color(0xFF111827))
                    .drawWithContent {
                        drawContent()
                        // 绘制西装领带图案
                        drawSuitAndTie(this)
                    },
                contentAlignment = Alignment.Center
            ) {
                // 空的Box，图案通过drawWithContent绘制
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                // 用户名 + VIP图标占位符
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "请叫我摸先生",
                        fontSize = 16.sp,
                        color = textColor,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // VIP图标占位符 - TODO: 替换为实际的VIP图标
                    Box(
                        modifier = Modifier
                            .size(20.dp)  // 适合图标的尺寸
                            .border(
                                width = 1.dp,
                                color = Color(0xFF9CA3AF).copy(alpha = 0.3f),  // 半透明边框
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                color = Color(0xFF9CA3AF).copy(alpha = 0.05f),  // 极淡的背景色
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // 占位符内容 - 可选添加临时图标或文字
                        Text(
                            text = "VIP",
                            fontSize = 8.sp,
                            color = Color(0xFF9CA3AF).copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 使用天数
                Text(
                    text = "已使用1953天",
                    fontSize = 13.sp,  // 稍微增大
                    color = subTextColor
                )
            }
        }

        // 菜单项列表
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)  // 添加间距
        ) {
            DrawerRow(
                text = "我的账本",
                icon = Icons.Filled.AccountCircle,  // 使用Filled版本
                rightText = "日常账本",
                onClick = { navController.navigate(DemoScreen.Books.route) }
            )
            DrawerRow(
                text = "报销管理",
                icon = Icons.Filled.Assignment,
                onClick = { /* TODO: 添加报销管理路由 */ }
            )
            DrawerRow(
                text = "搜索账单",
                icon = Icons.Filled.Search,
                onClick = { /* TODO: 添加搜索账单路由 */ }
            )
            DrawerRow(
                text = "卡片备份",
                icon = Icons.Filled.CreditCard,
                onClick = { navController.navigate(DemoScreen.CardBackup.route) }
            )
            DrawerRow(
                text = "分期·周期",
                icon = Icons.Filled.SyncAlt,
                onClick = { navController.navigate(DemoScreen.Installments.route) }
            )
            DrawerRow(
                text = "存钱计划",
                icon = Icons.Filled.Wallet,
                onClick = { /* TODO: 添加存钱计划路由 */ }
            )
            DrawerRow(
                text = "意见反馈",
                icon = Icons.Filled.QuestionAnswer,
                onClick = { /* TODO: 添加意见反馈路由 */ }
            )
            DrawerRow(
                text = "设置·关于",
                icon = Icons.Filled.Settings,
                onClick = { navController.navigate(DemoScreen.SettingsAbout.route) }
            )
        }
    }
}

@Composable
private fun DrawerRow(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    rightText: String? = null,
    onClick: () -> Unit
) {
    val textColor = Color(0xFF374151)  // 更深的文字颜色
    val subTextColor = Color(0xFF9CA3AF)  // 更浅的副文字
    val iconColor = Color(0xFF9CA3AF)  // 图标颜色

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp) // 调整高度
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp), // 调整垂直padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp) // 调整图标大小
        )

        Spacer(modifier = Modifier.width(16.dp)) // 调整间距

        Text(
            text = text,
            fontSize = 16.sp,  // 增大字体
            color = textColor,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        // 右侧附加文字（如"日常账本"）
        rightText?.let {
            Text(
                text = it,
                fontSize = 13.sp,  // 稍微增大
                color = subTextColor
            )
        }
    }
}

// 绘制西装领带图案
private fun drawSuitAndTie(scope: DrawScope) {
    with(scope) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val suitWidth = size.width * 0.45f
        val suitHeight = size.height * 0.5f

        // 绘制西装外轮廓（V形领口）
        val path = Path().apply {
            // 左肩
            moveTo(centerX - suitWidth / 2, centerY - suitHeight / 3)
            // 左领
            lineTo(centerX - suitWidth / 4, centerY + suitHeight / 3)
            // V领底部
            lineTo(centerX, centerY + suitHeight / 4)
            // 右领
            lineTo(centerX + suitWidth / 4, centerY + suitHeight / 3)
            // 右肩
            lineTo(centerX + suitWidth / 2, centerY - suitHeight / 3)
            // 顶部连接
            lineTo(centerX + suitWidth / 3, centerY - suitHeight / 2.5f)
            lineTo(centerX - suitWidth / 3, centerY - suitHeight / 2.5f)
            close()
        }

        drawPath(
            path = path,
            color = Color.White,
            style = Fill
        )

        // 绘制领带
        val tieWidth = size.width * 0.12f
        val tieHeight = size.height * 0.35f

        val tiePath = Path().apply {
            moveTo(centerX - tieWidth / 2, centerY - suitHeight / 4)
            lineTo(centerX + tieWidth / 2, centerY - suitHeight / 4)
            lineTo(centerX + tieWidth / 2.5f, centerY + tieHeight)
            lineTo(centerX, centerY + tieHeight + tieWidth / 2)
            lineTo(centerX - tieWidth / 2.5f, centerY + tieHeight)
            close()
        }

        drawPath(
            path = tiePath,
            color = Color.White,
            style = Fill
        )
    }
}

