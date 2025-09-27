package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class BookItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val isDefault: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSelectionBottomSheet(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    onBookSelected: (BookItem) -> Unit,
    selectedBookId: String? = null,
    title: String = "选择账本",
    books: List<BookItem> = listOf(
        BookItem("1", "日常账本", "初始账本", true)
    )
) {
    if (showSheet) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        val scope = rememberCoroutineScope()

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = {
                // 自定义拖动手柄
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFC6C6C6))
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = 24.dp)
                            ) {
                                // 管理按钮点击事件
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "管理",
                            fontSize = 15.sp,
                            color = Color(0xFF3B82F6)
                        )
                    }
                }

                // 账本列表
                books.forEach { book ->
                    BookSelectionItem(
                        book = book,
                        isSelected = book.id == selectedBookId,
                        onClick = {
                            scope.launch {
                                onBookSelected(book)
                                sheetState.hide()
                                onDismiss()
                            }
                        }
                    )
                }

                // 底部安全区域
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun BookSelectionItem(
    book: BookItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color(0xFF3B82F6).copy(alpha = 0.12f))
            ) {
                onClick()
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧图标
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF3B82F6)),
            contentAlignment = Alignment.Center
        ) {
            // 图标内容（可以根据需要添加）
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 文字内容
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = book.name,
                fontSize = 16.sp,
                color = Color(0xFF1F2937),
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = book.subtitle,
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                lineHeight = 18.sp
            )
        }

        // RadioButton
        CustomRadioButton(
            selected = isSelected,
            onClick = onClick
        )
    }
}

@Composable
private fun CustomRadioButton(
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp)
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
        ) {
            // 外圈
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (selected) Color(0xFF3B82F6) else Color.Transparent
                    )
                    .then(
                        if (!selected) {
                            Modifier.padding(1.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White)
                                .padding(1.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.Transparent)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 选中时的内部白点
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                    )
                }
            }

            // 未选中时的边框
            if (!selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(50))
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            drawCircle(
                                color = Color(0xFFD1D5DB),
                                radius = size.minDimension / 2,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 2.dp.toPx()
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}