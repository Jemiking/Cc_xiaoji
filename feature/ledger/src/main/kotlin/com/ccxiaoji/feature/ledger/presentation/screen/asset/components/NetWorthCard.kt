package com.ccxiaoji.feature.ledger.presentation.screen.asset.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.domain.model.NetWorthData
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * 净资产卡片
 */
@Composable
fun NetWorthCard(data: NetWorthData) {
    val decimalFormat = remember { DecimalFormat("#,##0.00") }
    
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
                text = "净资产",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 净资产金额
            Text(
                text = "¥${decimalFormat.format(data.netWorth)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (data.netWorth >= BigDecimal.ZERO) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            
            // 变化率
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = DesignTokens.Spacing.xs)
            ) {
                Icon(
                    imageVector = if (data.netWorthChange >= 0) {
                        Icons.AutoMirrored.Filled.TrendingUp
                    } else {
                        Icons.AutoMirrored.Filled.TrendingDown
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (data.netWorthChange >= 0) {
                        DesignTokens.BrandColors.Success
                    } else {
                        DesignTokens.BrandColors.Error
                    }
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text(
                    text = "${if (data.netWorthChange >= 0) "+" else ""}${String.format("%.2f", data.netWorthChange)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (data.netWorthChange >= 0) {
                        DesignTokens.BrandColors.Success
                    } else {
                        DesignTokens.BrandColors.Error
                    }
                )
                Text(
                    text = " 较上月",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 资产和负债
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 总资产
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "总资产",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¥${decimalFormat.format(data.totalAssets)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = DesignTokens.Spacing.xs)
                    )
                    Text(
                        text = "${if (data.assetsChange >= 0) "+" else ""}${String.format("%.2f", data.assetsChange)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (data.assetsChange >= 0) {
                            DesignTokens.BrandColors.Success
                        } else {
                            DesignTokens.BrandColors.Error
                        },
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                // 分隔线
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                
                // 总负债
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "总负债",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¥${decimalFormat.format(data.totalLiabilities)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = DesignTokens.Spacing.xs)
                    )
                    Text(
                        text = "${if (data.liabilitiesChange >= 0) "+" else ""}${String.format("%.2f", data.liabilitiesChange)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (data.liabilitiesChange >= 0) {
                            DesignTokens.BrandColors.Error
                        } else {
                            DesignTokens.BrandColors.Success
                        },
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}