package com.ccxiaoji.feature.ledger.presentation.screen.ledgerbook.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 添加/编辑记账簿对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLedgerBookDialog(
    ledger: Ledger?,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?, color: String, icon: String) -> Unit
) {
    var name by remember { mutableStateOf(ledger?.name ?: "") }
    var description by remember { mutableStateOf(ledger?.description ?: "") }
    var selectedColor by remember { mutableStateOf(ledger?.color ?: "#3A7AFE") }
    var selectedIcon by remember { mutableStateOf(ledger?.icon ?: "book") }
    
    var nameError by remember { mutableStateOf("") }
    
    val isEditing = ledger != null
    val title = if (isEditing) "编辑记账簿" else "创建记账簿"
    
    // 预定义颜色选择
    val colorOptions = listOf(
        "#3A7AFE", "#FF6B6B", "#4ECDC4", "#45B7D1", 
        "#96CEB4", "#FECA57", "#FF9FF3", "#54A0FF",
        "#5F27CD", "#00D2D3", "#FF9F43", "#EE5A24",
        "#0ABDE3", "#10AC84", "#F79F1F", "#A3CB38"
    )
    
    // 预定义图标选择
    val iconOptions = listOf(
        IconOption("book", "记账本", Icons.Default.MenuBook),
        IconOption("home", "家庭", Icons.Default.Home),
        IconOption("work", "工作", Icons.Default.Work),
        IconOption("school", "学校", Icons.Default.School),
        IconOption("family", "家庭", Icons.Default.FamilyRestroom),
        IconOption("car", "汽车", Icons.Default.DirectionsCar),
        IconOption("health", "健康", Icons.Default.LocalHospital),
        IconOption("travel", "旅行", Icons.Default.Flight),
        IconOption("shopping", "购物", Icons.Default.ShoppingCart),
        IconOption("food", "餐饮", Icons.Default.Restaurant),
        IconOption("entertainment", "娱乐", Icons.Default.Movie),
        IconOption("investment", "投资", Icons.Default.TrendingUp)
    )
    
    fun validateForm(): Boolean {
        nameError = when {
            name.isBlank() -> "请输入记账簿名称"
            name.length > 20 -> "名称不能超过20个字符"
            else -> ""
        }
        return nameError.isEmpty()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // 记账簿名称输入
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            nameError = ""
                        },
                        label = { Text("记账簿名称 *") },
                        placeholder = { Text("例如：日常开销、房贷记录") },
                        singleLine = true,
                        isError = nameError.isNotEmpty(),
                        supportingText = if (nameError.isNotEmpty()) {
                            { Text(nameError, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 记账簿描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述 (可选)") },
                    placeholder = { Text("简短描述记账簿的用途") },
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 颜色选择
                Column {
                    Text(
                        text = "选择颜色",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                        modifier = Modifier.height(80.dp)
                    ) {
                        items(colorOptions) { color ->
                            val isSelected = selectedColor == color
                            
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        try {
                                            Color(android.graphics.Color.parseColor(color))
                                        } catch (e: Exception) {
                                            Color.Gray
                                        }
                                    )
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                        } else Modifier
                                    )
                                    .clickable { selectedColor = color },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 图标选择
                Column {
                    Text(
                        text = "选择图标",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                        modifier = Modifier.height(120.dp)
                    ) {
                        items(iconOptions) { iconOption ->
                            val isSelected = selectedIcon == iconOption.key
                            
                            Card(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { selectedIcon = iconOption.key },
                                colors = if (isSelected) {
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                } else {
                                    CardDefaults.cardColors()
                                },
                                border = if (isSelected) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else null
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconOption.icon,
                                        contentDescription = iconOption.name,
                                        tint = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 预览
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    try {
                                        Color(android.graphics.Color.parseColor(selectedColor))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconOptions.find { it.key == selectedIcon }?.icon 
                                    ?: Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
                        
                        Column {
                            Text(
                                text = name.ifBlank { "记账簿名称" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            if (description.isNotBlank()) {
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (validateForm()) {
                        onSave(
                            name.trim(),
                            description.trim().takeIf { it.isNotBlank() },
                            selectedColor,
                            selectedIcon
                        )
                    }
                }
            ) {
                Text(if (isEditing) "保存" else "创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 图标选项数据类
 */
private data class IconOption(
    val key: String,
    val name: String,
    val icon: ImageVector
)