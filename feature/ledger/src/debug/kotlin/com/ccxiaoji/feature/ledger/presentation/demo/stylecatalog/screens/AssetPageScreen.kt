package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ViewGroup
import com.ccxiaoji.common.util.DeviceUtils

/**
 * 资产页面（AssetPageScreen）
 * 精确还原 FinanceDashboard 蓝图的 Demo 页面。
 * 顶部右上省略号点击暂不处理。
 */
@Composable
fun AssetPageScreen(navController: NavController) {
    val t = Tokens

    val ctx = LocalContext.current
    val enableInterop = remember(ctx) { DeviceUtils.isLongShotInteropRecommended(ctx) }

    Surface(color = t.Bg) {
        if (enableInterop) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    val host = androidx.core.widget.NestedScrollView(context).apply {
                        isFillViewport = true
                        overScrollMode = android.view.View.OVER_SCROLL_ALWAYS
                        isVerticalScrollBarEnabled = true
                    }
                    val compose = androidx.compose.ui.platform.ComposeView(context)
                    host.addView(
                        compose,
                        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    )
                    host.tag = compose
                    host
                },
                update = { host ->
                    val compose = host.tag as androidx.compose.ui.platform.ComposeView
                    compose.setContent {
                        MaterialTheme {
                            AssetPageContent(t)
                        }
                    }
                }
            )
        } else {
            AssetPageContent(t)
        }
    }
}

@Composable
private fun AssetPageContent(t: Tokens) {
    Box(modifier = Modifier.fillMaxSize().background(t.Bg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 顶部右上省略号命中区（44dp），无动作
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {}, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Filled.MoreVert, contentDescription = null, tint = t.Text.copy(alpha = 0.72f))
                }
            }

            OverviewCard(t)
            StatsCard(t)
            CreditSection(t)
            AssetSection(t)
            Spacer(Modifier.height(24.dp))
        }
    }
}

// Tokens from blueprint
private object Tokens {
    val Bg = Color(0xFFF6F7F9)
    val Card = Color(0xFFFFFFFF)
    val Text = Color(0xFF1A1F24)
    val Muted = Color(0xFF6B7280)
    val PillBg = Color(0xFFF2F3F5)
    val BarTrack = Color(0xFFE8F0FF)
    val BarFill = Color(0xFF2F6FED)

    val RadiusCard = 30.dp
    val RadiusPill = 18.dp
    val RadiusIcon = 8.dp
    val RadiusBar = 5.dp
}

@Composable
private fun OverviewCard(t: Tokens) {
    Surface(
        color = t.Card,
        shape = RoundedCornerShape(Tokens.RadiusCard),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 30.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("净资产", color = t.Muted, fontSize = 22.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "-¥2320.49",
                style = TextStyle(
                    color = t.Text,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum"
                )
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column { Text("总资产", color = t.Muted, fontSize = 14.sp, fontWeight = FontWeight.Medium); Text("-¥2140.72", color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
                Column(horizontalAlignment = Alignment.End) { Text("总负债", color = t.Muted, fontSize = 14.sp, fontWeight = FontWeight.Medium); Text("-¥179.77", color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun StatsCard(t: Tokens) {
    Surface(
        color = t.Card,
        shape = RoundedCornerShape(Tokens.RadiusCard),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCell(
                title = "总借入",
                value = "¥0.00",
                leading = { Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = t.Muted, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.weight(1f)
            )
            StatCell(
                title = "总借出",
                value = "¥0.00",
                leading = { Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = t.Muted, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCell(title: String, value: String, leading: (@Composable () -> Unit)? = null, modifier: Modifier = Modifier) {
    Surface(color = Tokens.Card, shape = RoundedCornerShape(22.dp), tonalElevation = 0.dp, shadowElevation = 0.dp, modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leading?.invoke()
            Column { Text(title, color = Tokens.Muted, fontSize = 13.sp); Text(value, color = Tokens.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun CreditSection(t: Tokens) {
    Surface(color = t.Card, shape = RoundedCornerShape(Tokens.RadiusCard), shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("信用卡", color = t.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("-¥179.77", color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFC5C8CE), modifier = Modifier.size(20.dp).padding(start = 4.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth().height(60.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("今日免息", color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("弹花呗 37天", color = t.Muted, fontSize = 14.sp)
            }

            Spacer(Modifier.height(4.dp))
            val items = listOf(
                DebtVM("信用卡", "-¥12386.52", listOf("30天内还款", "不计入", "可用：¥35613.48"), 0.25785f, brand = "A"),
                DebtVM("弹花呗", "-¥132.86", listOf("5天内还款", "可用：¥1367.14"), 0.08857f, brand = "花"),
                DebtVM("榕花呗", "-¥46.91", listOf("5天内还款", "可用：¥1453.09"), 0.03127f, brand = "榕"),
                DebtVM("京东白条", "-¥1155.34", listOf("12天内还款", "不计入", "可用：¥0.00"), 1.0f, brand = "J")
            )
            items.forEachIndexed { i, vm ->
                DebtItem(vm, t)
                if (i != items.lastIndex) Spacer(Modifier.height(12.dp))
            }
        }
    }
}

private data class DebtVM(val title: String, val amount: String, val pills: List<String>, val percent: Float, val brand: String)

@Composable
private fun DebtItem(vm: DebtVM, t: Tokens) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 56dp 圆角图标位，放品牌首字母占位
                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(Tokens.RadiusIcon)).background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) {
                    Text(vm.brand, color = t.Muted, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(vm.title, color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        vm.pills.take(3).forEach { Pill(it, t) }
                    }
                }
            }
            // 固定宽度的金额列 160dp，右对齐
            Box(modifier = Modifier.width(160.dp), contentAlignment = Alignment.TopEnd) {
                Text(vm.amount, color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(10.dp))
        // 进度条（10dp 高，圆角 5dp）
        Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(Tokens.RadiusBar)).background(t.BarTrack)) {
            Box(modifier = Modifier.fillMaxWidth(vm.percent.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(Tokens.RadiusBar)).background(t.BarFill))
        }
    }
}

@Composable
private fun Pill(text: String, t: Tokens) {
    Surface(color = t.PillBg, shape = RoundedCornerShape(Tokens.RadiusPill), tonalElevation = 0.dp) {
        Text(text = text, color = t.Text, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
    }
}

@Composable
private fun AssetSection(t: Tokens) {
    Surface(color = t.Card, shape = RoundedCornerShape(Tokens.RadiusCard), shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("资金", color = t.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("-¥2140.72", color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFC5C8CE), modifier = Modifier.size(20.dp).padding(start = 4.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            val rows = listOf(
                "骋建行卡" to "¥65.10",
                "榕建行" to "-¥2426.92",
                "榕工资卡" to "¥5.55",
                "骋工资卡" to "¥58.00",
                "骋支付宝" to "¥34.79",
                "榕支付宝" to "¥0.00",
                "骋微信零钱" to "¥217.59",
                "榕微信零钱" to "-¥94.83"
            )
            rows.forEachIndexed { i, (label, amount) ->
                AccountRow(label, amount, t)
                if (i != rows.lastIndex) Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun AccountRow(label: String, amount: String, t: Tokens) {
    Row(modifier = Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFE5E7EB)), contentAlignment = Alignment.Center) {
                // 品牌字母占位（取名称首字符）
                Text(label.firstOrNull()?.toString() ?: "·", color = t.Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(12.dp))
            Text(label, color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(amount, color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
