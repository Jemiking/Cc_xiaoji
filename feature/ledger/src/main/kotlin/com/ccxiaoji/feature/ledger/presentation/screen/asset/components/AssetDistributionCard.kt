package com.ccxiaoji.feature.ledger.presentation.screen.asset.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.domain.model.AssetDistribution

/**
 * 资产分布卡片
 */
@Composable
fun AssetDistributionCard(distribution: AssetDistribution) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium)
        ) {
            Text(
                text = "资产分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 资产占比图（简化版本，使用水平条形图）
            if (distribution.assetItems.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(DesignTokens.BorderRadius.large))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    distribution.assetItems.take(5).forEachIndexed { index, item ->
                        Box(
                            modifier = Modifier
                                .weight(item.percentage / 100f)
                                .fillMaxHeight()
                                .background(
                                    color = getColorForIndex(index)
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 图例
                distribution.assetItems.take(5).forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = DesignTokens.Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = getColorForIndex(index),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                        Text(
                            text = item.accountName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${String.format("%.1f", item.percentage)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * 获取指定索引的颜色
 */
@Composable
fun getColorForIndex(index: Int): Color {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        DesignTokens.BrandColors.Success,
        DesignTokens.BrandColors.Warning
    )
    return colors[index % colors.size]
}