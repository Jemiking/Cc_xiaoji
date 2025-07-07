package com.ccxiaoji.feature.plan.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 颜色选择页面 - 替代原ColorPickerDialog
 * 支持预设颜色选择和自定义颜色输入
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerScreen(
    initialColor: String?, // 格式: "#RRGGBB"
    navController: NavController
) {
    // 解析初始颜色
    var selectedColor by remember { 
        mutableStateOf(
            initialColor?.takeIf { it != "null" && isValidHexColor(it) } ?: "#6650a4"
        )
    }
    var customColorText by remember { mutableStateOf(selectedColor) }
    var showCustomInput by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("选择颜色")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(DesignTokens.Spacing.large),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 当前选择的颜色预览
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        Text(
                            text = "当前颜色",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Text(
                            text = selectedColor.uppercase(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    
                    // 颜色预览
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(selectedColor)))
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                    )
                }
            }
            
            // 预设颜色部分
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.medium)
                ) {
                    Text(
                        text = "预设颜色",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presetColors) { colorHex ->
                            ColorItem(
                                colorHex = colorHex,
                                isSelected = selectedColor.equals(colorHex, ignoreCase = true),
                                onClick = {
                                    selectedColor = colorHex
                                    customColorText = colorHex
                                }
                            )
                        }
                    }
                }
            }
            
            // 自定义颜色输入
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.medium)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "自定义颜色",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Switch(
                            checked = showCustomInput,
                            onCheckedChange = { showCustomInput = it }
                        )
                    }
                    
                    if (showCustomInput) {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                        
                        CustomColorInput(
                            value = customColorText,
                            onValueChange = { text ->
                                customColorText = text
                                if (isValidHexColor(text)) {
                                    selectedColor = text
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // 取消按钮
                FlatButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text("取消")
                }
                
                // 确定按钮
                FlatButton(
                    onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_color", selectedColor)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isValidHexColor(selectedColor)
                ) {
                    Text("确定")
                }
            }
        }
    }
}

/**
 * 颜色项组件
 */
@Composable
private fun ColorItem(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.Gray
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                },
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "已选择",
                tint = if (isColorLight(color)) Color.Black else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 自定义颜色输入框
 */
@Composable
private fun CustomColorInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    val isValid = isValidHexColor(value)
    val borderColor = if (isValid) {
        MaterialTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.error
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 颜色预览
        if (isValid) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(value)))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        // 输入框
        BasicTextField(
            value = value,
            onValueChange = { text ->
                // 限制输入长度和格式
                if (text.length <= 7 && text.all { it.isLetterOrDigit() || it == '#' }) {
                    onValueChange(text.uppercase())
                }
            },
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )
        
        // 错误提示
        if (!isValid && value.isNotEmpty()) {
            Text(
                text = "无效格式",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * 验证十六进制颜色值是否有效
 */
private fun isValidHexColor(color: String): Boolean {
    return try {
        color.matches(Regex("^#[0-9A-Fa-f]{6}$")) && 
        android.graphics.Color.parseColor(color) != 0
    } catch (e: Exception) {
        false
    }
}

/**
 * 判断颜色是否为浅色
 */
private fun isColorLight(color: Color): Boolean {
    val androidColor = color.toArgb()
    val red = android.graphics.Color.red(androidColor)
    val green = android.graphics.Color.green(androidColor)
    val blue = android.graphics.Color.blue(androidColor)
    // 使用标准的亮度计算公式
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
    return luminance > 0.5
}

/**
 * 预设颜色列表
 */
private val presetColors = listOf(
    // Material Design 3 颜色
    "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3",
    "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
    "#FFEB3B", "#FFC107", "#FF9800", "#FF5722", "#795548", "#9E9E9E",
    
    // 深色变体
    "#B71C1C", "#880E4F", "#4A148C", "#311B92", "#1A237E", "#0D47A1",
    "#01579B", "#006064", "#004D40", "#1B5E20", "#33691E", "#827717",
    "#F57F17", "#FF6F00", "#E65100", "#BF360C", "#3E2723", "#424242",
    
    // 浅色变体
    "#FFCDD2", "#F8BBD0", "#E1BEE7", "#D1C4E9", "#C5CAE9", "#BBDEFB",
    "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9", "#DCEDC8", "#F0F4C3",
    "#FFF9C4", "#FFECB3", "#FFE0B2", "#FFCCBC", "#D7CCC8", "#F5F5F5"
)