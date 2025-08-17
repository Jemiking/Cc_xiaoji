package com.ccxiaoji.feature.ledger.presentation.screen.category.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 分类编辑对话框
 * 支持编辑分类名称、图标和颜色
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditDialog(
    isVisible: Boolean,
    title: String,
    categoryName: String,
    categoryIcon: String,
    categoryColor: String?,
    parentName: String? = null,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    error: String? = null
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(DesignTokens.BorderRadius.large),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.large)
                ) {
                    // 标题
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // 显示父分类（如果有）
                    if (parentName != null) {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                        Text(
                            text = "父分类：$parentName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                    
                    // 分类名称输入
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = onNameChange,
                        label = { Text("分类名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = error != null,
                        supportingText = if (error != null) {
                            { Text(error, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(DesignTokens.BorderRadius.small)
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                    
                    // 图标选择区域
                    Text(
                        text = "选择图标",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    // 当前选中的图标
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = categoryIcon,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    // 预设图标列表
                    IconSelector(
                        selectedIcon = categoryIcon,
                        onIconSelected = onIconChange
                    )
                    
                    // 颜色选择（可选）
                    if (categoryColor != null) {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                        
                        Text(
                            text = "选择颜色",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                        
                        ColorSelector(
                            selectedColor = categoryColor,
                            onColorSelected = onColorChange
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
                    
                    // 按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("取消")
                        }
                        
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                        
                        FlatButton(
                            text = "确定",
                            onClick = onConfirm,
                            enabled = categoryName.isNotBlank(),
                            backgroundColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 图标选择器组件
 */
@Composable
private fun IconSelector(
    selectedIcon: String,
    onIconSelected: (String) -> Unit
) {
    val commonIcons = listOf(
        "🍔", "☕", "🍕", "🥗", "🍜", "🍱", "🥡", "🍰",
        "🚗", "🚌", "🚇", "✈️", "🚲", "⛽", "🚕", "🏍️",
        "🏠", "💡", "💧", "🔥", "📱", "💻", "🛒", "🎮",
        "👕", "👗", "👠", "👜", "💄", "💍", "⌚", "🕶️",
        "📚", "✏️", "🎨", "🎭", "🎬", "🎵", "🏃", "⚽",
        "💊", "🏥", "💉", "🩺", "🦷", "👨‍⚕️", "🏨", "✂️",
        "🎁", "🎂", "🎉", "❤️", "💰", "💳", "📈", "💼"
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(commonIcons) { icon ->
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onIconSelected(icon) },
                shape = RoundedCornerShape(8.dp),
                color = if (icon == selectedIcon) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 颜色选择器组件
 */
@Composable
private fun ColorSelector(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val presetColors = listOf(
        "#E91E63", // Pink
        "#9C27B0", // Purple
        "#673AB7", // Deep Purple
        "#3F51B5", // Indigo
        "#2196F3", // Blue
        "#03A9F4", // Light Blue
        "#00BCD4", // Cyan
        "#009688", // Teal
        "#4CAF50", // Green
        "#8BC34A", // Light Green
        "#CDDC39", // Lime
        "#FFC107", // Amber
        "#FF9800", // Orange
        "#FF5722", // Deep Orange
        "#795548", // Brown
        "#607D8B"  // Blue Grey
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(presetColors) { color ->
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onColorSelected(color) },
                shape = RoundedCornerShape(8.dp),
                color = Color(android.graphics.Color.parseColor(color)),
                border = if (color == selectedColor) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else null
            ) {
                // 颜色块
            }
        }
    }
}

/**
 * 使用 BorderStroke
 */
@Composable
private fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(width, color)
}