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
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components.BookItem
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components.BookSelectionBottomSheet
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrateBookScreen(navController: NavController) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)

    // 账本数据（实际应用中应从数据源获取）
    val availableBooks = remember {
        listOf(
            BookItem("1", "日常账本", "初始账本", true),
            BookItem("2", "旅行账本", "2024年创建"),
            BookItem("3", "装修账本", "2023年创建")
        )
    }

    // 状态管理
    var sourceBook by remember { mutableStateOf<BookItem?>(null) }
    var targetBook by remember { mutableStateOf<BookItem?>(null) }
    var showSourceBookSheet by remember { mutableStateOf(false) }
    var showTargetBookSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("迁移账本") },
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("开始迁移", color = Color.White, fontSize = 16.sp)
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

            // 原账本选择卡片
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { showSourceBookSheet = true }
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "原账本",
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            sourceBook?.name ?: "未选择",
                            fontSize = 16.sp,
                            color = if (sourceBook == null) Color(0xFF9CA3AF) else Color(0xFF111827)
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

            // 目标账本选择卡片
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { showTargetBookSheet = true }
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "目标账本",
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            targetBook?.name ?: "未选择",
                            fontSize = 16.sp,
                            color = if (targetBook == null) Color(0xFF9CA3AF) else Color(0xFF111827)
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

            // 说明文字
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    "1. 迁移账本后，原账本中的账单，将会全部迁移到目标账本中，这是一个不可逆的操作，请谨慎操作；",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 21.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "2. 原账本中的分类，会复制到目标账本中，所以账单所属分类并不会发生变化；",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 21.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "3. 如果原账本中有共享成员，也会一并复制到目标账本中。",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 21.sp
                )
            }
        }
    }

    // 原账本选择底部弹窗
    BookSelectionBottomSheet(
        showSheet = showSourceBookSheet,
        onDismiss = { showSourceBookSheet = false },
        onBookSelected = { book ->
            sourceBook = book
        },
        selectedBookId = sourceBook?.id,
        title = "选择原账本",
        books = availableBooks
    )

    // 目标账本选择底部弹窗
    BookSelectionBottomSheet(
        showSheet = showTargetBookSheet,
        onDismiss = { showTargetBookSheet = false },
        onBookSelected = { book ->
            // 确保目标账本不同于原账本
            if (book.id != sourceBook?.id) {
                targetBook = book
            }
        },
        selectedBookId = targetBook?.id,
        title = "选择目标账本",
        books = availableBooks.filter { it.id != sourceBook?.id }
    )
}