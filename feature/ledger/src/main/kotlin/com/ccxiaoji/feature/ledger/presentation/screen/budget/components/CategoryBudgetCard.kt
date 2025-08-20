package com.ccxiaoji.feature.ledger.presentation.screen.budget.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.data.local.dao.BudgetWithSpent
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.presentation.component.DynamicCategoryIcon
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.ModernCard
import kotlinx.datetime.Clock
import java.text.NumberFormat

@Composable
fun CategoryBudgetCard(
    budget: BudgetWithSpent,
    category: CategoryEntity,
    currencyFormat: NumberFormat,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    // 获取图标显示模式
    val uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()
    
    val usagePercentage = if (budget.budgetAmountCents > 0) {
        (budget.spentAmountCents.toFloat() / budget.budgetAmountCents.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val isExceeded = budget.spentAmountCents > budget.budgetAmountCents
    
    // 将CategoryEntity转换为Category对象以支持DynamicCategoryIcon
    val categoryModel = Category(
        id = category.id,
        name = category.name,
        type = if (category.type == "INCOME") Category.Type.INCOME else Category.Type.EXPENSE,
        icon = category.icon,
        color = category.color,
        level = category.level,
        parentId = category.parentId,
        isSystem = category.isSystem,
        createdAt = Clock.System.now(), // CategoryEntity没有时间戳，使用当前时间
        updatedAt = Clock.System.now()
    )
    
    // 根据分类类型使用语义化颜色
    val categoryColor = when (category.type) {
        "INCOME" -> DesignTokens.BrandColors.Success
        "EXPENSE" -> DesignTokens.BrandColors.Error
        else -> MaterialTheme.colorScheme.primary
    }
    
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = if (isExceeded) {
            DesignTokens.BrandColors.Error.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分类图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                DynamicCategoryIcon(
                    category = categoryModel,
                    iconDisplayMode = uiPreferences.iconDisplayMode,
                    size = 24.dp,
                    tint = categoryColor
                )
            }
            
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
            
            // 预算信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currencyFormat.format(budget.budgetAmountCents / 100.0),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                
                // 进度条
                LinearProgressIndicator(
                    progress = { usagePercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isExceeded) {
                        DesignTokens.BrandColors.Error
                    } else if (usagePercentage >= budget.alertThreshold) {
                        DesignTokens.BrandColors.Warning
                    } else {
                        categoryColor
                    },
                    trackColor = categoryColor.copy(alpha = 0.1f)
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "已支出: ${currencyFormat.format(budget.spentAmountCents / 100.0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${(usagePercentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isExceeded) {
                            DesignTokens.BrandColors.Error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        }
                    )
                }
            }
            
            // 删除按钮
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = DesignTokens.BrandColors.Error.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}