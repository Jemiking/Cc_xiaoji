package com.ccxiaoji.feature.schedule.presentation.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * 排班主页调试控制面板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugControlPanel(
    params: DebugCalendarParams,
    onParamsChange: (DebugCalendarParams) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "排班主页调试控制台",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        item {
            // 预设方案选择
            PresetSelector(
                onPresetSelected = { preset ->
                    onParamsChange(preset)
                }
            )
        }
        
        item {
            // 导入导出功能
            ImportExportPanel(
                currentParams = params,
                onParamsImported = onParamsChange
            )
        }
        
        item {
            Divider()
        }
        
        // 1. TopAppBar 参数控制
        item {
            ExpandableSection(
                title = "顶部导航栏",
                icon = Icons.Default.Menu
            ) {
                TopAppBarControls(
                    params = params.topAppBar,
                    onParamsChange = { newParams ->
                        onParamsChange(params.copy(topAppBar = newParams))
                    },
                    onReset = {
                        onParamsChange(params.copy(topAppBar = TopAppBarParams()))
                    }
                )
            }
        }
        
        // 2. 统计卡片参数控制
        item {
            ExpandableSection(
                title = "统计卡片",
                icon = Icons.Default.Analytics
            ) {
                StatisticsCardControls(
                    params = params.statisticsCard,
                    onParamsChange = { newParams ->
                        onParamsChange(params.copy(statisticsCard = newParams))
                    },
                    onReset = {
                        onParamsChange(params.copy(statisticsCard = StatisticsCardParams()))
                    }
                )
            }
        }
        
        // 3. 日历视图参数控制
        item {
            ExpandableSection(
                title = "日历视图",
                icon = Icons.Default.CalendarMonth
            ) {
                CalendarViewControls(
                    params = params.calendarView,
                    onParamsChange = { newParams ->
                        onParamsChange(params.copy(calendarView = newParams))
                    },
                    onReset = {
                        onParamsChange(params.copy(calendarView = CalendarViewParams()))
                    }
                )
            }
        }
        
        // 4. 详情卡片参数控制
        item {
            ExpandableSection(
                title = "详情卡片",
                icon = Icons.Default.Info
            ) {
                DetailCardControls(
                    params = params.detailCard,
                    onParamsChange = { newParams ->
                        onParamsChange(params.copy(detailCard = newParams))
                    },
                    onReset = {
                        onParamsChange(params.copy(detailCard = DetailCardParams()))
                    }
                )
            }
        }
        
        // 5. 布局参数控制
        item {
            ExpandableSection(
                title = "布局间距",
                icon = Icons.Default.GridView
            ) {
                LayoutControls(
                    params = params.layout,
                    onParamsChange = { newParams ->
                        onParamsChange(params.copy(layout = newParams))
                    },
                    onReset = {
                        onParamsChange(params.copy(layout = LayoutParams()))
                    }
                )
            }
        }
        
        // 6. FAB参数控制
        item {
            ExpandableSection(
                title = "浮动按钮",
                icon = Icons.Default.Add
            ) {
                FabControls(
                    params = params.fab,
                    onParamsChange = { newParams ->
                        onParamsChange(params.copy(fab = newParams))
                    },
                    onReset = {
                        onParamsChange(params.copy(fab = FabParams()))
                    }
                )
            }
        }
        
        // 7. 主题色彩控制
        item {
            ExpandableSection(
                title = "主题色彩",
                icon = Icons.Default.Palette
            ) {
                ThemeControls(
                    params = params.theme,
                    onParamsChange = { newParams ->
                        onParamsChange(params.copy(theme = newParams))
                    },
                    onReset = {
                        onParamsChange(params.copy(theme = ThemeParams()))
                    }
                )
            }
        }
    }
}

/**
 * 预设方案选择器
 */
@Composable
private fun PresetSelector(
    onPresetSelected: (DebugCalendarParams) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "预设方案",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onPresetSelected(DefaultDebugParams.default) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("默认")
                }
                OutlinedButton(
                    onClick = { onPresetSelected(DefaultDebugParams.modern) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("现代")
                }
                OutlinedButton(
                    onClick = { onPresetSelected(DefaultDebugParams.compact) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("紧凑")
                }
            }
        }
    }
}

/**
 * 可展开的参数控制组
 */
@Composable
private fun ExpandableSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            ListItem(
                headlineContent = { Text(title) },
                leadingContent = { Icon(icon, contentDescription = null) },
                trailingContent = { 
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable { expanded = !expanded }
            )
            
            if (expanded) {
                Box(
                    modifier = Modifier.padding(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * TopAppBar 参数控制
 */
@Composable
private fun TopAppBarControls(
    params: TopAppBarParams,
    onParamsChange: (TopAppBarParams) -> Unit,
    onReset: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ResetButton(onReset = onReset)
        
        DpSlider(
            label = "导航栏高度",
            value = params.height,
            valueRange = 48f..80f,
            onValueChange = { onParamsChange(params.copy(height = it.dp)) }
        )
        
        TextUnitSlider(
            label = "标题字体大小",
            value = params.titleFontSize,
            valueRange = 14f..24f,
            onValueChange = { onParamsChange(params.copy(titleFontSize = it.sp)) }
        )
        
        DpSlider(
            label = "操作按钮大小",
            value = params.actionButtonSize,
            valueRange = 40f..56f,
            onValueChange = { onParamsChange(params.copy(actionButtonSize = it.dp)) }
        )
        
        TextUnitSlider(
            label = "今日按钮字体",
            value = params.todayButtonTextSize,
            valueRange = 12f..20f,
            onValueChange = { onParamsChange(params.copy(todayButtonTextSize = it.sp)) }
        )
    }
}

/**
 * 统计卡片参数控制
 */
@Composable
private fun StatisticsCardControls(
    params: StatisticsCardParams,
    onParamsChange: (StatisticsCardParams) -> Unit,
    onReset: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ResetButton(onReset = onReset)
        DpSlider(
            label = "卡片圆角",
            value = params.cornerRadius,
            valueRange = 0f..24f,
            onValueChange = { onParamsChange(params.copy(cornerRadius = it.dp)) }
        )
        
        DpSlider(
            label = "卡片阴影",
            value = params.elevation,
            valueRange = 0f..8f,
            onValueChange = { onParamsChange(params.copy(elevation = it.dp)) }
        )
        
        DpSlider(
            label = "内容边距",
            value = params.padding,
            valueRange = 8f..32f,
            onValueChange = { onParamsChange(params.copy(padding = it.dp)) }
        )
        
        DpSlider(
            label = "项目间距",
            value = params.itemSpacing,
            valueRange = 4f..20f,
            onValueChange = { onParamsChange(params.copy(itemSpacing = it.dp)) }
        )
        
        TextUnitSlider(
            label = "文字大小",
            value = params.textSize,
            valueRange = 10f..18f,
            onValueChange = { onParamsChange(params.copy(textSize = it.sp)) }
        )
    }
}

/**
 * 日历视图参数控制
 */
@Composable
private fun CalendarViewControls(
    params: CalendarViewParams,
    onParamsChange: (CalendarViewParams) -> Unit,
    onReset: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ResetButton(onReset = onReset)
        DpSlider(
            label = "日期格子大小",
            value = params.cellSize,
            valueRange = 32f..64f,
            // 为了立即生效，联动 cellSize 与 rowHeight
            onValueChange = { onParamsChange(params.copy(cellSize = it.dp, rowHeight = it.dp)) }
        )
        
        DpSlider(
            label = "格子间距",
            value = params.cellSpacing,
            valueRange = 0f..8f,
            onValueChange = { onParamsChange(params.copy(cellSpacing = it.dp)) }
        )
        
        DpSlider(
            label = "行高",
            value = params.rowHeight,
            valueRange = 40f..72f,
            onValueChange = { onParamsChange(params.copy(rowHeight = it.dp)) }
        )

        // 日期数字字号
        TextUnitSlider(
            label = "日期数字字体",
            value = params.dateNumberTextSize,
            valueRange = 10f..28f,
            onValueChange = { onParamsChange(params.copy(dateNumberTextSize = it.sp)) }
        )
        
        // LineHeight调试参数组 - 新增
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "📏 LineHeight 精细调试",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        FloatSlider(
            label = "LineHeight倍数",
            value = params.dateNumberLineHeightMultiplier,
            valueRange = 1.0f..2.0f,
            onValueChange = { onParamsChange(params.copy(dateNumberLineHeightMultiplier = it)) }
        )
        
        FloatSlider(
            label = "容器高度倍数",
            value = params.minContainerHeightMultiplier,
            valueRange = 2.0f..4.0f,
            onValueChange = { onParamsChange(params.copy(minContainerHeightMultiplier = it)) }
        )
        
        FloatSlider(
            label = "Bold字体额外空间",
            value = params.fontWeightBoldExtraSpace,
            valueRange = 0.0f..0.5f,
            onValueChange = { onParamsChange(params.copy(fontWeightBoldExtraSpace = it)) }
        )
        
        FloatSlider(
            label = "字符空间微调",
            value = params.characterSpaceAdjustment,
            valueRange = -0.2f..0.2f,
            onValueChange = { onParamsChange(params.copy(characterSpaceAdjustment = it)) }
        )

        // 日期格子圆角
        DpSlider(
            label = "格子圆角",
            value = params.cornerRadius,
            valueRange = 0f..20f,
            onValueChange = { onParamsChange(params.copy(cornerRadius = it.dp)) }
        )
        
        DpSlider(
            label = "日程指示器大小",
            value = params.scheduleIndicatorSize,
            valueRange = 3f..10f,
            onValueChange = { onParamsChange(params.copy(scheduleIndicatorSize = it.dp)) }
        )
        
        FloatSlider(
            label = "其他月份透明度",
            value = params.otherMonthDateOpacity,
            valueRange = 0.2f..0.8f,
            onValueChange = { onParamsChange(params.copy(otherMonthDateOpacity = it)) }
        )
    }
}

/**
 * 详情卡片参数控制
 */
@Composable
private fun DetailCardControls(
    params: DetailCardParams,
    onParamsChange: (DetailCardParams) -> Unit,
    onReset: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ResetButton(onReset = onReset)
        DpSlider(
            label = "卡片圆角",
            value = params.cornerRadius,
            valueRange = 0f..24f,
            onValueChange = { onParamsChange(params.copy(cornerRadius = it.dp)) }
        )
        
        DpSlider(
            label = "内容边距",
            value = params.padding,
            valueRange = 8f..32f,
            onValueChange = { onParamsChange(params.copy(padding = it.dp)) }
        )
        
        TextUnitSlider(
            label = "日期标题字体",
            value = params.dateHeaderTextSize,
            valueRange = 12f..20f,
            onValueChange = { onParamsChange(params.copy(dateHeaderTextSize = it.sp)) }
        )
        
        TextUnitSlider(
            label = "信息文字字体",
            value = params.scheduleInfoTextSize,
            valueRange = 10f..16f,
            onValueChange = { onParamsChange(params.copy(scheduleInfoTextSize = it.sp)) }
        )
    }
}

/**
 * 布局参数控制
 */
@Composable
private fun LayoutControls(
    params: LayoutParams,
    onParamsChange: (LayoutParams) -> Unit,
    onReset: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ResetButton(onReset = onReset)
        DpSlider(
            label = "页面水平边距",
            value = params.screenHorizontalPadding,
            valueRange = 8f..32f,
            onValueChange = { onParamsChange(params.copy(screenHorizontalPadding = it.dp)) }
        )
        
        DpSlider(
            label = "组件间距",
            value = params.componentSpacing,
            valueRange = 8f..32f,
            onValueChange = { onParamsChange(params.copy(componentSpacing = it.dp)) }
        )
        
        IntSlider(
            label = "月份切换动画时长",
            value = params.monthTransitionDuration,
            valueRange = 100f..500f,
            onValueChange = { onParamsChange(params.copy(monthTransitionDuration = it)) }
        )
    }
}

/**
 * FAB参数控制
 */
@Composable
private fun FabControls(
    params: FabParams,
    onParamsChange: (FabParams) -> Unit,
    onReset: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ResetButton(onReset = onReset)
        DpSlider(
            label = "按钮大小",
            value = params.size,
            valueRange = 40f..72f,
            onValueChange = { onParamsChange(params.copy(size = it.dp)) }
        )
        
        DpSlider(
            label = "阴影高度",
            value = params.elevation,
            valueRange = 0f..12f,
            onValueChange = { onParamsChange(params.copy(elevation = it.dp)) }
        )
        
        DpSlider(
            label = "底部边距",
            value = params.bottomMargin,
            valueRange = 8f..32f,
            onValueChange = { onParamsChange(params.copy(bottomMargin = it.dp)) }
        )
    }
}

/**
 * 主题色彩控制
 */
@Composable
private fun ThemeControls(
    params: ThemeParams,
    onParamsChange: (ThemeParams) -> Unit,
    onReset: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ResetButton(onReset = onReset)
        Text(
            "色彩系统",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        ColorItem(
            label = "主品牌色",
            color = params.primaryBrandColor
        )
        
        ColorItem(
            label = "排班模块强调色",
            color = params.scheduleModuleAccentColor
        )
        
        ColorItem(
            label = "工作日颜色",
            color = params.workDayColor
        )
        
        ColorItem(
            label = "休息日颜色",
            color = params.restDayColor
        )
    }
}

// 辅助控件组件
@Composable
private fun DpSlider(
    label: String,
    value: Dp,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${value.value.roundToInt()}dp", style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value.value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
private fun TextUnitSlider(
    label: String,
    value: TextUnit,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${value.value.roundToInt()}sp", style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value.value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
private fun FloatSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${(value * 100).roundToInt()}%", style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
private fun IntSlider(
    label: String,
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${value}ms", style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = valueRange
        )
    }
}

@Composable
private fun ColorItem(
    label: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
    }
}

/**
 * 通用重置按钮
 */
@Composable
private fun ResetButton(onReset: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        OutlinedButton(
            onClick = onReset,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("重置", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * 导入导出面板
 */
@Composable
private fun ImportExportPanel(
    currentParams: DebugCalendarParams,
    onParamsImported: (DebugCalendarParams) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var showMessage by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "参数导入导出",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 导出按钮
                OutlinedButton(
                    onClick = {
                        try {
                            val jsonString = currentParams.toJsonString()
                            clipboardManager.setText(AnnotatedString(jsonString))
                            showMessage = "参数已复制到剪贴板"
                        } catch (e: Exception) {
                            showMessage = "导出失败: ${e.message}"
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导出")
                }
                
                // 导入按钮
                OutlinedButton(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导入")
                }
                
                // 全部重置按钮
                OutlinedButton(
                    onClick = { onParamsImported(DefaultDebugParams.default) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("全部重置")
                }
            }
            
            // 状态消息
            if (showMessage.isNotEmpty()) {
                LaunchedEffect(showMessage) {
                    kotlinx.coroutines.delay(3000)
                    showMessage = ""
                }
                Text(
                    showMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (showMessage.startsWith("导出失败") || showMessage.startsWith("导入失败")) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }
    
    // 导入对话框
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { 
                showImportDialog = false
                importText = ""
            },
            title = { Text("导入参数配置") },
            text = {
                Column {
                    Text("请粘贴导出的参数配置：")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        placeholder = { Text("粘贴JSON配置...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val clipboardText = clipboardManager.getText()?.text ?: ""
                                    importText = clipboardText
                                } catch (e: Exception) {
                                    showMessage = "从剪贴板读取失败"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("从剪贴板粘贴")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            val params = importText.toDebugParams()
                            if (params != null) {
                                onParamsImported(params)
                                showImportDialog = false
                                importText = ""
                                showMessage = "参数导入成功"
                            } else {
                                showMessage = "导入失败：无效的参数格式"
                            }
                        } catch (e: Exception) {
                            showMessage = "导入失败: ${e.message}"
                        }
                    }
                ) {
                    Text("导入")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showImportDialog = false
                        importText = ""
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}
