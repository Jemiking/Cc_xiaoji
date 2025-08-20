package com.ccxiaoji.feature.schedule.presentation.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.launch

/**
 * UI Demo 页面：展示 4 种日历单元格布局方案，供选择参考
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarUiDemoScreen(
    onNavigateBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun showInfo(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
        try { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() } catch (_: Throwable) {}
    }

    val scheduledDays = remember { setOf(2, 5, 12, 19, 21, 30) }
    val shiftName = "白班"
    val shiftColor = Color(0xFF4CAF50)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("排班日历 UI Demo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Log.d("UiDemoNav", "CalendarUiDemoScreen composed")
        LaunchedEffect(Unit) {
            showInfo("已进入 UI Demo。点击任意日期方格预览反馈")
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            DemoSection(title = "方案 A：左上日期 + 下方班次标签") {
                DemoGrid { day ->
                    CellVariantA(
                        day = day,
                        hasSchedule = day in scheduledDays,
                        shiftName = shiftName,
                        shiftColor = shiftColor,
                        onClick = {
                            val hs = day in scheduledDays
                            showInfo("方案A：${day}号 ${if (hs) "有排班($shiftName)" else "无排班"}")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            DemoSection(title = "方案 B：居中日期 + 点状指示") {
                DemoGrid { day ->
                    CellVariantB(
                        day = day,
                        hasSchedule = day in scheduledDays,
                        shiftColor = shiftColor,
                        onClick = {
                            val hs = day in scheduledDays
                            showInfo("方案B：${day}号 ${if (hs) "有排班" else "无排班"}")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            DemoSection(title = "方案 C：右上日期 + 底部横条") {
                DemoGrid { day ->
                    CellVariantC(
                        day = day,
                        hasSchedule = day in scheduledDays,
                        shiftColor = shiftColor,
                        onClick = {
                            val hs = day in scheduledDays
                            showInfo("方案C：${day}号 ${if (hs) "有排班" else "无排班"}")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            DemoSection(title = "方案 D：同一行 日期+班次简写") {
                DemoGrid { day ->
                    CellVariantD(
                        day = day,
                        hasSchedule = day in scheduledDays,
                        shiftAbbr = "早",
                        shiftColor = shiftColor,
                        onClick = {
                            val hs = day in scheduledDays
                            showInfo("方案D：${day}号 ${if (hs) "有排班(早)" else "无排班"}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoSection(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 6.dp)
    )
    content()
}

@Composable
private fun DemoGrid(cell: @Composable (Int) -> Unit) {
    val days = remember { (1..31).toList() }
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(bottom = 6.dp)
    ) {
        items(days) { day -> cell(day) }
    }
}

// 方案 A：左上日期 + 下方班次标签
@Composable
private fun CellVariantA(day: Int, hasSchedule: Boolean, shiftName: String, shiftColor: Color, onClick: () -> Unit = {}) {
    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp)
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                maxLines = 1,
                softWrap = false,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            if (hasSchedule) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .background(shiftColor.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = shiftName,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = shiftColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

// 方案 B：居中日期 + 点状指示
@Composable
private fun CellVariantB(day: Int, hasSchedule: Boolean, shiftColor: Color, onClick: () -> Unit = {}) {
    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )
            if (hasSchedule) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(shiftColor, shape = MaterialTheme.shapes.small)
                )
            }
        }
    }
}

// 方案 C：右上日期 + 底部横条
@Composable
private fun CellVariantC(day: Int, hasSchedule: Boolean, shiftColor: Color, onClick: () -> Unit = {}) {
    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    ) {
        Box(Modifier.fillMaxSize()) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
            )
            if (hasSchedule) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(shiftColor.copy(alpha = 0.6f))
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}

// 方案 D：同一行 日期 + 班次简写
@Composable
private fun CellVariantD(day: Int, hasSchedule: Boolean, shiftAbbr: String, shiftColor: Color, onClick: () -> Unit = {}) {
    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    ) {
        Box(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
                if (hasSchedule) {
                    Text(
                        text = shiftAbbr,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        color = shiftColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
