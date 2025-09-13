package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.*

@Composable
fun DemoOverviewCard(
    income: Double,
    expense: Double,
    balance: Double,
    style: DemoStyle,
    density: DemoDensity
) {
    // 当样式为钱迹风格时，使用专用的QianjiInspiredSpecs实现
    if (style == DemoStyle.QianjiInspired) {
        val specs = com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl.QianjiInspiredSpecs()
        specs.headerSpec.RenderOverviewCard(
            income = income,
            expense = expense,
            balance = balance,
            modifier = Modifier.fillMaxWidth()
        )
        return
    }
    
    // 其他样式使用通用实现
    val colors = LocalDemoColors.current
    val shapes = LocalDemoShapes.current
    val elevations = LocalDemoElevations.current
    val densitySettings = LocalDemoDensitySettings.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = densitySettings.itemSpacing),
        shape = RoundedCornerShape(shapes.cornerRadiusLarge),
        elevation = CardDefaults.cardElevation(defaultElevation = elevations.level2),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(densitySettings.cardPadding),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OverviewItem(
                label = "收入",
                amount = income,
                color = Color(0xFF4CAF50),
                density = density
            )
            
            Divider(
                modifier = Modifier
                    .height(densitySettings.rowHeight)
                    .width(densitySettings.dividerThickness),
                color = colors.outlineVariant
            )
            
            OverviewItem(
                label = "支出",
                amount = expense,
                color = Color(0xFFFF5252),
                density = density
            )
            
            Divider(
                modifier = Modifier
                    .height(densitySettings.rowHeight)
                    .width(densitySettings.dividerThickness),
                color = colors.outlineVariant
            )
            
            OverviewItem(
                label = "结余",
                amount = balance,
                color = colors.primary,
                density = density
            )
        }
    }
}

@Composable
private fun OverviewItem(
    label: String,
    amount: Double,
    color: Color,
    density: DemoDensity
) {
    val densitySettings = LocalDemoDensitySettings.current
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(densitySettings.itemSpacing / 2))
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DemoFilterBar(
    enabled: Boolean,
    style: DemoStyle,
    density: DemoDensity
) {
    val colors = LocalDemoColors.current
    val shapes = LocalDemoShapes.current
    val densitySettings = LocalDemoDensitySettings.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
        shape = RoundedCornerShape(shapes.cornerRadiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(densitySettings.cellPadding),
            horizontalArrangement = Arrangement.spacedBy(densitySettings.itemSpacing)
        ) {
            FilterChip(
                selected = false,
                onClick = { },
                label = { Text("日期", style = MaterialTheme.typography.labelMedium) },
                enabled = enabled,
                leadingIcon = {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(densitySettings.iconSize - 4.dp)
                    )
                }
            )
            
            FilterChip(
                selected = false,
                onClick = { },
                label = { Text("分类", style = MaterialTheme.typography.labelMedium) },
                enabled = enabled,
                leadingIcon = {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(densitySettings.iconSize - 4.dp)
                    )
                }
            )
            
            FilterChip(
                selected = false,
                onClick = { },
                label = { Text("账户", style = MaterialTheme.typography.labelMedium) },
                enabled = enabled,
                leadingIcon = {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(densitySettings.iconSize - 4.dp)
                    )
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(
                onClick = { },
                enabled = enabled
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "搜索",
                    modifier = Modifier.size(densitySettings.iconSize)
                )
            }
        }
    }
}

@Composable
fun DemoStylePreviewCard(
    currentStyle: DemoStyle,
    density: DemoDensity
) {
    val colors = LocalDemoColors.current
    val shapes = LocalDemoShapes.current
    val densitySettings = LocalDemoDensitySettings.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(shapes.cornerRadiusSmall),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(densitySettings.cellPadding)
        ) {
            Text(
                text = "风格预览：${currentStyle.displayName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(densitySettings.itemSpacing))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(densitySettings.itemSpacing)
            ) {
                // 明色预览
                MiniPreview(
                    label = "明色模式",
                    isDark = false,
                    modifier = Modifier.weight(1f)
                )
                
                // 暗色预览
                MiniPreview(
                    label = "暗色模式",
                    isDark = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MiniPreview(
    label: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val style = LocalDemoStyle.current
    val previewColors = getStyleColors(style, isDark)
    val shapes = LocalDemoShapes.current
    val densitySettings = LocalDemoDensitySettings.current
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(shapes.cornerRadiusSmall))
                .background(previewColors.background)
                .border(
                    width = 1.dp,
                    color = previewColors.outline,
                    shape = RoundedCornerShape(shapes.cornerRadiusSmall)
                )
        ) {
            // 迷你交易项示例
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(densitySettings.cellPadding / 2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(previewColors.primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(4.dp)
                            .background(previewColors.onSurface)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(3.dp)
                            .background(previewColors.onSurfaceVariant)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(4.dp)
                        .background(previewColors.secondary)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DemoDateHeader(
    date: String,
    style: DemoStyle,
    density: DemoDensity
) {
    // 当样式为钱迹风格时，使用专用的QianjiInspiredSpecs实现
    if (style == DemoStyle.QianjiInspired) {
        val specs = com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl.QianjiInspiredSpecs()
        
        // 转换日期格式并计算模拟的收支数据
        val dateObj = try {
            java.time.LocalDate.parse(date.replace("年", "-").replace("月", "-").replace("日", ""))
        } catch (e: Exception) {
            java.time.LocalDate.now()
        }
        
        specs.listSpec.RenderGroupHeader(
            date = dateObj,
            totalIncome = 0.0,     // 模拟数据
            totalExpense = 150.0,   // 模拟数据
            modifier = Modifier
        )
        return
    }
    
    // 其他样式使用通用实现
    val colors = LocalDemoColors.current
    val densitySettings = LocalDemoDensitySettings.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = densitySettings.itemSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.titleSmall,
            color = colors.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.width(densitySettings.itemSpacing))
        
        Divider(
            modifier = Modifier.weight(1f),
            color = colors.outlineVariant,
            thickness = densitySettings.dividerThickness
        )
    }
}

@Composable
fun DemoTransactionItem(
    transaction: DemoTransaction,
    style: DemoStyle,
    density: DemoDensity,
    onClick: () -> Unit
) {
    // 当样式为钱迹风格时，使用专用的QianjiInspiredSpecs实现
    if (style == DemoStyle.QianjiInspired) {
        val specs = com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl.QianjiInspiredSpecs()
        specs.listSpec.RenderListItem(
            transaction = transaction,
            modifier = Modifier,
            onClick = onClick,
            onLongClick = { /* 长按功能禁用 */ }
        )
        return
    }
    
    // 其他样式使用通用实现
    val colors = LocalDemoColors.current
    val shapes = LocalDemoShapes.current
    val elevations = LocalDemoElevations.current
    val densitySettings = LocalDemoDensitySettings.current
    
    val cardModifier = when (style) {
        DemoStyle.NeoBrutalism -> Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 0.dp,
                shape = RoundedCornerShape(0.dp),
                spotColor = Color.Black,
                ambientColor = Color.Black
            )
            .offset(x = 2.dp, y = 2.dp)
            .background(Color.Black)
            .offset(x = (-2).dp, y = (-2).dp)
            .clip(RoundedCornerShape(0.dp))
            .background(colors.surface)
            .border(2.dp, Color.Black, RoundedCornerShape(0.dp))
            
        DemoStyle.Glassmorphism -> Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(shapes.cornerRadiusMedium))
            .background(colors.surface.copy(alpha = 0.8f))
            .border(
                1.dp,
                colors.outline.copy(alpha = 0.3f),
                RoundedCornerShape(shapes.cornerRadiusMedium)
            )
            
        else -> Modifier
            .fillMaxWidth()
            .shadow(
                elevation = elevations.level1,
                shape = RoundedCornerShape(shapes.cornerRadiusMedium)
            )
            .clip(RoundedCornerShape(shapes.cornerRadiusMedium))
            .background(colors.surface)
    }
    
    Card(
        modifier = cardModifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(densitySettings.cellPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分类图标
            Icon(
                imageVector = getCategoryIcon(transaction.category?.name ?: "其他"),
                contentDescription = null,
                modifier = Modifier.size(densitySettings.iconSize),
                tint = colors.primary
            )
            
            Spacer(modifier = Modifier.width(densitySettings.itemSpacing))
            
            // 交易信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category?.name ?: "未知分类",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Medium
                )
                
                if (!transaction.note.isNullOrEmpty()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // 标签
                if (transaction.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        transaction.tags.forEach { tag ->
                            TagChip(tag = tag.name, density = density)
                        }
                    }
                }
            }
            
            // 账户
            Text(
                text = transaction.account?.name ?: "未知账户",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(densitySettings.itemSpacing))
            
            // 金额
            Text(
                text = formatCurrency(transaction.amount),
                style = MaterialTheme.typography.titleMedium,
                color = if (transaction.type == TransactionType.EXPENSE) Color(0xFFFF5252) else Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TagChip(
    tag: String,
    density: DemoDensity
) {
    val colors = LocalDemoColors.current
    val shapes = LocalDemoShapes.current
    val densitySettings = LocalDemoDensitySettings.current
    
    Surface(
        shape = RoundedCornerShape(shapes.cornerRadiusSmall),
        color = colors.primaryContainer.copy(alpha = 0.3f)
    ) {
        Text(
            text = tag,
            modifier = Modifier.padding(
                horizontal = densitySettings.cellPadding / 2,
                vertical = 2.dp
            ),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onPrimaryContainer
        )
    }
}

@Composable
fun DemoPaginationBar(
    enabled: Boolean,
    style: DemoStyle,
    density: DemoDensity
) {
    val colors = LocalDemoColors.current
    val densitySettings = LocalDemoDensitySettings.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = densitySettings.itemSpacing)
            .alpha(if (enabled) 1f else 0.5f),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { },
            enabled = enabled
        ) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = "上一页",
                modifier = Modifier.size(densitySettings.iconSize)
            )
        }
        
        Text(
            text = "第 1 页 / 共 3 页",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )
        
        IconButton(
            onClick = { },
            enabled = enabled
        ) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "下一页",
                modifier = Modifier.size(densitySettings.iconSize)
            )
        }
    }
}

private fun getCategoryIcon(category: String) = when (category) {
    "餐饮" -> Icons.Default.Restaurant
    "交通" -> Icons.Default.DirectionsCar
    "购物" -> Icons.Default.ShoppingCart
    "工资" -> Icons.Default.AccountBalanceWallet
    "娱乐" -> Icons.Default.Movie
    else -> Icons.Default.Category
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return formatter.format(amount)
}