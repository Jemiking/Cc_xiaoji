package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.LedgerUIStyle
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 可风格化的月度总览组件
 * 根据当前UI风格显示不同样式的总览卡片
 */
@Composable
fun StyleableMonthlyOverviewBar(
    monthlyIncome: Double,
    monthlyExpense: Double,
    currentStyle: LedgerUIStyle,
    animationDurationMs: Int = 300,
    modifier: Modifier = Modifier
) {
    // 带动画的风格切换
    AnimatedContent(
        targetState = currentStyle,
        transitionSpec = {
            (fadeIn(
                animationSpec = androidx.compose.animation.core.tween(animationDurationMs)
            ) + scaleIn(
                initialScale = 0.92f,
                animationSpec = androidx.compose.animation.core.tween(animationDurationMs)
            )) togetherWith (fadeOut(
                animationSpec = androidx.compose.animation.core.tween(animationDurationMs)
            ) + scaleOut(
                targetScale = 0.92f,
                animationSpec = androidx.compose.animation.core.tween(animationDurationMs)
            ))
        },
        label = "OverviewCardAnimation",
        modifier = modifier
    ) { style ->
        when (style) {
            LedgerUIStyle.BALANCED -> {
                BalancedOverviewCard(
                    monthlyIncome = monthlyIncome,
                    monthlyExpense = monthlyExpense,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            LedgerUIStyle.HIERARCHICAL -> {
                HierarchicalOverviewCard(
                    monthlyIncome = monthlyIncome,
                    monthlyExpense = monthlyExpense,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            LedgerUIStyle.HYBRID -> {
                // 混合风格：概览卡片采用层次化设计
                HierarchicalOverviewCard(
                    monthlyIncome = monthlyIncome,
                    monthlyExpense = monthlyExpense,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}


/**
 * 兼容性的月度总览条（当前设计风格）
 * 保持与原有MonthlyOverviewBar相同的外观，用于向后兼容或作为第三种风格选项
 */
@Composable
fun CurrentStyleOverviewBar(
    monthlyIncome: Double,
    monthlyExpense: Double,
    modifier: Modifier = Modifier
) {
    val balance = monthlyIncome - monthlyExpense
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 收入
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "收入",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "¥%.2f".format(monthlyIncome),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        }
        
        // 分隔线
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        
        // 支出
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "支出",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "¥%.2f".format(monthlyExpense),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        // 分隔线
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        
        // 结余
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "结余",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${if (balance >= 0) "+" else ""}¥%.2f".format(balance),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}
