package com.ccxiaoji.feature.ledger.presentation.screen.ledger.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.presentation.component.DynamicCategoryIcon
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // 获取图标显示模式
    val uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()
    
    // 🔍 DEBUG: 交易列表图标调试 - UI偏好设置
    println("🔍 [TransactionItem] 交易列表图标调试:")
    println("   - 交易ID: ${transaction.id}")
    println("   - 当前图标显示模式: ${uiPreferences.iconDisplayMode}")
    println("   - UI偏好设置获取成功: ${uiPreferences != null}")
    println("   ─────────────────────────")
    
    // 交易项显示逻辑
    
    // 使用语义化颜色
    val amountColor = when (transaction.categoryDetails?.type) {
        "INCOME" -> DesignTokens.BrandColors.Success
        "EXPENSE" -> DesignTokens.BrandColors.Error
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    // 使用预定义的分类颜色
    val categoryColor = when (transaction.categoryDetails?.type) {
        "INCOME" -> DesignTokens.BrandColors.Success
        "EXPENSE" -> DesignTokens.BrandColors.Error
        else -> MaterialTheme.colorScheme.primary
    }
    
    Box(modifier = modifier) {
        ModernCard(
            onClick = onItemClick,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onItemClick,
                    onLongClick = {
                        if (!isSelectionMode) {
                            showMenu = true
                        }
                    }
                ),
            backgroundColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            },
            borderColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            },
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null, // Handled by card onClick
                            colors = CheckboxDefaults.colors(
                                checkedColor = DesignTokens.BrandColors.Todo
                            )
                        )
                    }
                    
                    // 分类图标 - 扁平背景
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = categoryColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // 根据分类信息创建临时Category对象用于DynamicCategoryIcon
                        transaction.categoryDetails?.let { categoryDetails ->
                            // 🔍 DEBUG: 交易列表图标调试 - 分类数据
                            println("🔍 [TransactionItem] 分类详情解析:")
                            println("   - 交易ID: ${transaction.id}")
                            println("   - 分类名称: ${categoryDetails.name}")
                            println("   - 分类类型: ${categoryDetails.type}")
                            println("   - 分类图标: ${categoryDetails.icon}")
                            println("   - 分类颜色: ${categoryDetails.color}")
                            
                            val tempCategory = Category(
                                id = "temp_${transaction.id}",
                                name = categoryDetails.name,
                                type = if (categoryDetails.type == "INCOME") Category.Type.INCOME else Category.Type.EXPENSE,
                                icon = categoryDetails.icon,
                                color = categoryDetails.color,
                                level = 1, // 默认设为1级分类
                                parentId = null, // CategoryDetails中没有parentId
                                isSystem = false,
                                createdAt = Clock.System.now(),
                                updatedAt = Clock.System.now()
                            )
                            
                            // 🔍 DEBUG: 交易列表图标调试 - Category对象创建
                            println("🔍 [TransactionItem] 临时Category对象创建:")
                            println("   - 临时ID: ${tempCategory.id}")
                            println("   - 转换后类型: ${tempCategory.type}")
                            println("   - 图标数据: ${tempCategory.icon}")
                            println("   - 准备调用DynamicCategoryIcon")
                            println("   - 图标显示模式: ${uiPreferences.iconDisplayMode}")
                            println("   ─────────────────────────")
                            
                            DynamicCategoryIcon(
                                category = tempCategory,
                                iconDisplayMode = uiPreferences.iconDisplayMode,
                                size = 20.dp,
                                tint = categoryColor
                            )
                        } ?: run {
                            // 🔍 DEBUG: 交易列表图标调试 - 无分类信息
                            println("🔍 [TransactionItem] 警告: 无分类信息!")
                            println("   - 交易ID: ${transaction.id}")
                            println("   - categoryDetails为null，使用备用图标")
                            println("   ─────────────────────────")
                            
                            // 备用图标（没有分类信息时）
                            Text(
                                text = "📝",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            text = transaction.categoryDetails?.name ?: stringResource(R.string.uncategorized),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        transaction.note?.let { note ->
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                                .toJavaLocalDateTime()
                                .format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Text(
                    text = if (transaction.categoryDetails?.type == "INCOME") {
                        stringResource(R.string.amount_format_positive, stringResource(R.string.currency_symbol), transaction.amountYuan)
                    } else {
                        stringResource(R.string.amount_format_negative, stringResource(R.string.currency_symbol), transaction.amountYuan)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = amountColor
                )
            }
        }
        
        // Dropdown Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.copy)) },
                onClick = {
                    onCopy()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.FileCopy, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit)) },
                onClick = {
                    onEdit()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete)) },
                onClick = {
                    onDelete()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}