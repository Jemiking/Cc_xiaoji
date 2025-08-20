package com.ccxiaoji.feature.ledger.presentation.screen.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode
import com.ccxiaoji.feature.ledger.presentation.component.DynamicCategoryIcon
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.Clock

/**
 * 图标显示模式设置组件
 */
@Composable
fun IconDisplayModeSection(
    modifier: Modifier = Modifier,
    uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
) {
    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()
    
    ModernCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.medium, vertical = DesignTokens.Spacing.small),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium)
        ) {
            Text(
                text = "界面设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 图标风格切换
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "分类图标风格",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (uiPreferences.iconDisplayMode == IconDisplayMode.EMOJI) {
                            "使用传统的emoji表情符号"
                        } else {
                            "使用现代的Material Design图标"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = uiPreferences.iconDisplayMode == IconDisplayMode.MATERIAL,
                    onCheckedChange = { useMaterial ->
                        uiStyleViewModel.updateIconDisplayMode(
                            if (useMaterial) IconDisplayMode.MATERIAL else IconDisplayMode.EMOJI
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DesignTokens.BrandColors.Ledger,
                        checkedTrackColor = DesignTokens.BrandColors.Ledger.copy(alpha = 0.5f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 预览示例
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.medium)
                ) {
                    Text(
                        text = "预览效果",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 示例分类预览
                        val sampleCategories = listOf(
                            Category(
                                id = "sample1",
                                name = "餐饮",
                                type = Category.Type.EXPENSE,
                                icon = "🍔",
                                color = "#FF5722",
                                createdAt = Clock.System.now(),
                                updatedAt = Clock.System.now()
                            ),
                            Category(
                                id = "sample2",
                                name = "购物",
                                type = Category.Type.EXPENSE,
                                icon = "🛍️",
                                color = "#E91E63",
                                createdAt = Clock.System.now(),
                                updatedAt = Clock.System.now()
                            ),
                            Category(
                                id = "sample3",
                                name = "工资",
                                type = Category.Type.INCOME,
                                icon = "💰",
                                color = "#4CAF50",
                                createdAt = Clock.System.now(),
                                updatedAt = Clock.System.now()
                            )
                        )
                        
                        sampleCategories.forEach { category ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                DynamicCategoryIcon(
                                    category = category,
                                    iconDisplayMode = uiPreferences.iconDisplayMode,
                                    size = 28.dp,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}