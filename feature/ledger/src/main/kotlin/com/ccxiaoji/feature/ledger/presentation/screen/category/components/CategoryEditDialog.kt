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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode
import com.ccxiaoji.feature.ledger.presentation.component.CategoryIconMapper
import com.ccxiaoji.feature.ledger.presentation.component.DynamicCategoryIcon
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.Clock

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
                    // 获取图标显示模式 - 移动到顶层以便在整个Column中使用
                    val uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
                    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()
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
                            // 预览区域显示选中图标
                            
                            // 创建临时Category对象来支持DynamicCategoryIcon
                            val tempCategory = Category(
                                id = "temp_dialog",
                                name = categoryName.ifEmpty { "预览" },
                                type = if (title.contains("收入")) Category.Type.INCOME else Category.Type.EXPENSE,
                                icon = categoryIcon,
                                color = categoryColor ?: "#6200EE",
                                level = if (parentName != null) 2 else 1,
                                parentId = null,
                                isSystem = false,
                                createdAt = Clock.System.now(),
                                updatedAt = Clock.System.now()
                            )
                            
                            DynamicCategoryIcon(
                                category = tempCategory,
                                iconDisplayMode = uiPreferences.iconDisplayMode,
                                size = 32.dp,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    // 预设图标列表
                    IconSelector(
                        selectedIcon = categoryIcon,
                        iconDisplayMode = uiPreferences.iconDisplayMode,
                        categoryType = if (title.contains("收入")) Category.Type.INCOME else Category.Type.EXPENSE,
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
 * 获取emoji对应的语义分类名称，用于Material图标映射
 * 每个emoji映射到独特的语义名称，确保Material图标多样性
 */
private fun getSemanticNameForEmoji(emoji: String): String {
    return when (emoji) {
        // 餐饮类 - 使用具体的餐饮分类
        "🍔" -> "餐饮"     // Restaurant图标
        "☕" -> "饮品"     // LocalCafe图标
        "🍕" -> "夜宵"     // NightlightRound图标  
        "🥗" -> "午餐"     // LunchDining图标
        "🍜" -> "早餐"     // FreeBreakfast图标
        "🍱" -> "晚餐"     // DinnerDining图标
        "🥡" -> "酒水"     // LocalBar图标
        "🍰" -> "零食"     // Cookie图标
        
        // 交通类 - 使用具体交通工具
        "🚗" -> "交通"     // DirectionsCar图标
        "🚌" -> "公交"     // DirectionsBus图标
        "🚇" -> "地铁"     // Subway图标
        "✈️" -> "飞机"     // Flight图标
        "🚲" -> "停车"     // LocalParking图标
        "⛽" -> "加油"     // LocalGasStation图标
        "🚕" -> "打车"     // LocalTaxi图标
        "🏍️" -> "火车"     // Train图标
        
        // 生活类 - 使用具体生活分类
        "🏠" -> "住房"     // Home图标
        "💡" -> "水电"     // ElectricalServices图标
        "💧" -> "水费"     // WaterDrop图标
        "🔥" -> "燃气"     // LocalFireDepartment图标
        "📱" -> "通讯"     // Phone图标
        "💻" -> "数码"     // PhoneAndroid图标
        "🛒" -> "购物"     // ShoppingBag图标
        "🎮" -> "游戏"     // SportsEsports图标
        
        // 服饰类 - 使用具体服饰分类
        "👕" -> "服装"     // Checkroom图标
        "👗" -> "家电"     // Tv图标
        "👠" -> "鞋靴"     // ShoppingBag图标
        "👜" -> "日用品"   // ShoppingCart图标
        "💄" -> "化妆品"   // Face图标
        "💍" -> "维修"     // BuildCircle图标
        "⌚" -> "家具"     // Chair图标
        "🕶️" -> "眼科"     // Visibility图标
        
        // 学习娱乐类 - 使用具体分类
        "📚" -> "书籍"     // MenuBook图标
        "✏️" -> "文具"     // Edit图标
        "🎨" -> "娱乐"     // Movie图标
        "🎭" -> "摄影"     // CameraAlt图标
        "🎬" -> "电影"     // Theaters图标
        "🎵" -> "音乐"     // MusicNote图标
        "🏃" -> "运动"     // FitnessCenter图标
        "⚽" -> "装修"     // Handyman图标
        
        // 医疗类 - 使用具体医疗分类
        "💊" -> "药品"     // Medication图标
        "🏥" -> "医疗"     // LocalHospital图标
        "💉" -> "体检"     // HealthAndSafety图标
        "🩺" -> "保健"     // Spa图标
        "🦷" -> "牙科"     // Healing图标
        "🏨" -> "物业"     // Apartment图标
        "✂️" -> "理发"     // ContentCut图标
        
        // 礼品其他类 - 使用具体分类
        "🎁" -> "礼品"     // CardGiftcard图标
        "🎂" -> "KTV"      // Mic图标
        "🎉" -> "旅游"     // TravelExplore图标
        "❤️" -> "捐赠"     // VolunteerActivism图标
        "💰" -> "其它支出" // MoreHoriz图标
        "💳" -> "宠物"     // Pets图标
        "📈" -> "教育"     // School图标
        "💼" -> "培训"     // Class图标
        
        // 特殊处理的复合emoji
        "\uD83D\uDC68\u200D\u2695\uFE0F" -> "医疗" // 👨‍⚕️ 男医生
        
        // 默认
        else -> "其它支出"
    }
}

/**
 * 图标选择器组件
 * Material模式下使用语义映射显示不同的Material图标
 */
@Composable
private fun IconSelector(
    selectedIcon: String,
    iconDisplayMode: IconDisplayMode,
    categoryType: Category.Type,
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
                    .clickable { 
                        onIconSelected(icon) 
                    },
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
                    // 使用语义名称创建临时Category，实现Material图标多样性映射
                    val semanticName = getSemanticNameForEmoji(icon)
                    
                    val tempCategory = Category(
                        id = "temp_selector_$icon",
                        name = semanticName, // 使用语义名称而不是固定的"选择器"
                        type = categoryType,
                        icon = icon,
                        color = "#6200EE",
                        level = 1,
                        parentId = null,
                        isSystem = false,
                        createdAt = Clock.System.now(),
                        updatedAt = Clock.System.now()
                    )
                    
                    DynamicCategoryIcon(
                        category = tempCategory,
                        iconDisplayMode = iconDisplayMode,
                        size = 18.dp,
                        tint = MaterialTheme.colorScheme.onSurface
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