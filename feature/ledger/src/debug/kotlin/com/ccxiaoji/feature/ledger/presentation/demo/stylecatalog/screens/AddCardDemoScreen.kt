package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DemoScreen
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar
import com.ccxiaoji.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardDemoScreen(navController: NavController) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)

    // 表单状态（demo-only）
    var showTypeDialog by remember { mutableStateOf(false) }
    var showBankSheet by remember { mutableStateOf(false) }

    var type by remember { mutableStateOf(CardTypeSimple.DEBIT) }
    var bank by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var holder by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // 校验：仅数字，长度 4~32 才允许保存
    val digits = remember(number) { number.filter { it.isDigit() } }
    val canSave = digits.length in 4..32

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加卡片", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        val pageBg = Color(0xFFF3F4F6)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
                .padding(padding)
        ) {
            Spacer(Modifier.height(12.dp))

            // 分组卡片
            GroupedCard {
                // 类型（导航样式）
                NavigationRow(
                    title = "类型",
                    value = type.displayName,
                    valueTint = MaterialTheme.colorScheme.onSurface,
                    onClick = { showTypeDialog = true }
                )

                DividerHairline()

                // 银行（快速选择）
                NavigationRow(
                    title = "银行",
                    value = if (bank.isBlank()) "快速选择" else bank,
                    valueTint = if (bank.isBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    onClick = { showBankSheet = true }
                )

                DividerHairline()

                // 卡号输入（仅数字）
                InputRow(
                    placeholder = "卡号(数字，长度4~32)",
                    value = number,
                    onValueChange = { new ->
                        // 仅允许输入数字（保持与截图一致）
                        val filtered = new.filter { it.isDigit() }
                        number = filtered
                    }
                )

                DividerHairline()

                // 户名
                InputRow(
                    placeholder = "户名",
                    value = holder,
                    onValueChange = { holder = it }
                )

                DividerHairline()

                // 备注
                InputRow(
                    placeholder = "备注",
                    value = note,
                    onValueChange = { note = it }
                )

                DividerHairline()

                // 照片（导航样式）
                NavigationRow(
                    title = "照片",
                    value = "",
                    valueTint = MaterialTheme.colorScheme.onSurface,
                    onClick = { navController.navigate(DemoScreen.SettingsAbout.route) } // 占位跳转
                )
            }

            Spacer(Modifier.height(16.dp))

            // 底部保存按钮
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Button(
                    onClick = { /* demo-only: no-op */ },
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Text("保存", fontSize = 16.sp)
                }
            }
        }
    }

    // 类型选择对话框
    if (showTypeDialog) {
        CardTypeSelectDialog(
            current = type,
            onSelect = {
                type = it
                // 与截图一致：不做银行名称联动
                showTypeDialog = false
            },
            onDismiss = { showTypeDialog = false }
        )
    }

    // 银行选择底部弹窗
    BankSelectionBottomSheet(
        show = showBankSheet,
        selected = bank,
        onDismiss = { showBankSheet = false },
        onSelected = { selected ->
            bank = selected
            showBankSheet = false
        }
    )
}

// ===== 小组件：1:1 分组卡 =====
@Composable
private fun GroupedCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp)),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(content = content)
    }
}

// 发丝级分隔线（与截图接近的浅灰细线）
@Composable
private fun DividerHairline() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE5E7EB))
    )
}

// 导航样式的行（左文字 + 右值 + >）
@Composable
private fun NavigationRow(
    title: String,
    value: String,
    valueTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.weight(1f)
        )

        if (value.isNotBlank()) {
            Text(
                text = value,
                fontSize = 16.sp,
                color = valueTint,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(end = 8.dp)
            )
        } else {
            Spacer(Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// 输入行：无边框、仅底部分隔，与截图一致
@Composable
private fun InputRow(
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboard: KeyboardOptions = KeyboardOptions.Default
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            ),
            keyboardOptions = keyboard,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isBlank()) {
                        Text(
                            placeholder,
                            color = Color(0xFF9CA3AF),
                            fontSize = 16.sp
                        )
                    }
                    inner()
                }
            }
        )
    }
}

// ===== 类型选择对话框（无底部按钮，点选即关） =====
@Composable
private fun CardTypeSelectDialog(
    current: CardTypeSimple,
    onSelect: (CardTypeSimple) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.Spacing.large),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "类型",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))

                CardTypeSimple.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable { onSelect(option) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == current,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option.displayName, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

enum class CardTypeSimple(val displayName: String) {
    DEBIT("储蓄卡"),
    CREDIT("信用卡"),
    PASSBOOK("存折"),
    SECURITIES("证券账户"),
    OTHER("其它")
}

// ===== 银行选择底部弹窗（含实时搜索、单选） =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankSelectionBottomSheet(
    show: Boolean,
    selected: String?,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    if (!show) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }

    // 本地银行列表（占位）
    val banks = remember {
        listOf(
            "中国银行", "招商银行", "工商银行", "农业银行", "建设银行", "交通银行",
            "浦发银行", "广发银行", "邮政储蓄银行", "农村信用社", "兴业银行", "中信银行",
            "民生银行", "光大银行", "平安银行", "华夏银行", "北京银行", "上海银行",
            "浙商银行", "恒丰银行", "渤海银行"
        )
    }

    val filtered = remember(query, banks) {
        if (query.isBlank()) banks else banks.filter { it.contains(query) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
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
            // 标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "选择银行",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )
            }

            // 搜索框
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = Color(0xFFF5F5F5)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔎",
                        modifier = Modifier.padding(end = 8.dp),
                        color = Color(0xFF9CA3AF)
                    )
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                if (query.isBlank()) {
                                    Text("搜索", color = Color(0xFF9CA3AF))
                                }
                                inner()
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 列表
            filtered.forEach { name ->
                BankListItem(
                    name = name,
                    selected = name == selected,
                    onClick = { onSelected(name) }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BankListItem(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧圆形占位图标（首字）
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.first().toString(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = name,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

