package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components.BookItem
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components.BookSelectionBottomSheet
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components.CustomDatePickerDialog
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearBillsScreen(navController: NavController) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)

    val selectedBook = remember { mutableStateOf<BookItem?>(null) }

    // 账本数据（实际应用中应从数据源获取）
    val availableBooks = remember {
        listOf(
            BookItem("1", "日常账本", "初始账本", true),
            BookItem("2", "旅行账本", "2024年创建"),
            BookItem("3", "装修账本", "2023年创建")
        )
    }
    val startDate = remember { mutableStateOf<LocalDate?>(null) }
    val endDate = remember { mutableStateOf<LocalDate?>(null) }

    var showBookSelector by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // 判断是否可以开始清除
    val canClear = selectedBook.value != null && startDate.value != null && endDate.value != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("清除账单") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = canClear,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),  // 红色，表示危险操作
                        disabledContainerColor = Color(0xFFE5E7EB)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "开始清除",
                        color = if (canClear) Color.White else Color(0xFF9CA3AF),
                        fontSize = 16.sp
                    )
                }
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

            // 清除的账本选择卡片
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { showBookSelector = true }
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "清除的账本",
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            selectedBook.value?.name ?: "未选择",
                            fontSize = 16.sp,
                            color = if (selectedBook.value == null) Color(0xFF9CA3AF) else Color(0xFF111827)
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFFC5C8CE)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 开始日期选择卡片
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { showStartDatePicker = true }
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "开始日期（包括此日）",
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            startDate.value?.let { formatDateForClearBills(it) } ?: "未选择",
                            fontSize = 16.sp,
                            color = if (startDate.value == null) Color(0xFF9CA3AF) else Color(0xFF111827)
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFFC5C8CE)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 截止日期选择卡片
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { showEndDatePicker = true }
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "截止日期（包括此日）",
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            endDate.value?.let { formatDateForClearBills(it) } ?: "未选择",
                            fontSize = 16.sp,
                            color = if (endDate.value == null) Color(0xFF9CA3AF) else Color(0xFF111827)
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFFC5C8CE)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 警告说明文字
            Text(
                "清除账单是一个非常危险的操作，所有账单清除后，将无法再恢复，请不要随意使用此功能",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                lineHeight = 21.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }

    // 账本选择弹窗
    BookSelectionBottomSheet(
        showSheet = showBookSelector,
        onDismiss = { showBookSelector = false },
        onBookSelected = { book ->
            selectedBook.value = book
        },
        selectedBookId = selectedBook.value?.id,
        title = "选择账本",
        books = availableBooks
    )

    // 开始日期选择器
    if (showStartDatePicker) {
        CustomDatePickerDialog(
            initialDate = startDate.value,
            onDateSelected = { date ->
                startDate.value = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    // 截止日期选择器
    if (showEndDatePicker) {
        CustomDatePickerDialog(
            initialDate = endDate.value,
            onDateSelected = { date ->
                endDate.value = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

// 格式化日期显示
private fun formatDateForClearBills(date: LocalDate): String {
    return "${date.year}/${date.monthValue}/${date.dayOfMonth}"
}