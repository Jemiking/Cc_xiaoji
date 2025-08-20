package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 平衡增强设计风格的总览卡片
 * 增强总览卡片视觉权重，使用渐变背景和图标
 */
@Composable
fun BalancedOverviewCard(
    monthlyIncome: Double,
    monthlyExpense: Double,
    modifier: Modifier = Modifier
) {
    val balance = monthlyIncome - monthlyExpense
    
    // 美化的渐变卡片设计
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f),
                            DesignTokens.BrandColors.Ledger.copy(alpha = 0.05f),
                            Color.White
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 顶部标题区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    DesignTokens.BrandColors.Ledger.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "💰",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "本月财务概览",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DesignTokens.BrandColors.Ledger
                        )
                    }
                    
                    // 趋势图标
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                if (balance >= 0) DesignTokens.BrandColors.Success.copy(alpha = 0.2f)
                                else DesignTokens.BrandColors.Error.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (balance >= 0) "📈" else "📉",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 主要数据区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 收入卡片
                    FinanceItem(
                        label = "收入",
                        amount = monthlyIncome,
                        color = DesignTokens.BrandColors.Success,
                        icon = "💚",
                        isPositive = true
                    )
                    
                    // 支出卡片  
                    FinanceItem(
                        label = "支出",
                        amount = monthlyExpense,
                        color = DesignTokens.BrandColors.Error,
                        icon = "💸",
                        isPositive = false
                    )
                    
                    // 结余卡片
                    FinanceItem(
                        label = "结余",
                        amount = kotlin.math.abs(balance),
                        color = if (balance >= 0) DesignTokens.BrandColors.Ledger else DesignTokens.BrandColors.Error,
                        icon = if (balance >= 0) "💎" else "⚠️",
                        isPositive = balance >= 0
                    )
                }
            }
        }
    }
}

/**
 * 财务数据项组件
 */
@Composable
private fun FinanceItem(
    label: String,
    amount: Double,
    color: Color,
    icon: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        // 图标区域
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color.copy(alpha = 0.15f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 标签
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 金额
        Text(
            text = "${if (isPositive) "+" else "-"}¥${String.format("%.0f", amount)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}