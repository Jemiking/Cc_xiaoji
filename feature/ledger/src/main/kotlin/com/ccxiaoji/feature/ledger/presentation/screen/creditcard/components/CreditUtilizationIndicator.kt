package com.ccxiaoji.feature.ledger.presentation.screen.creditcard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 信用卡使用率指示器
 */
@Composable
fun CreditUtilizationIndicator(rate: Double) {
    val (backgroundColor, textColor) = when {
        rate <= 30 -> DesignTokens.BrandColors.Success.copy(alpha = 0.1f) to DesignTokens.BrandColors.Success
        rate <= 70 -> DesignTokens.BrandColors.Warning.copy(alpha = 0.1f) to DesignTokens.BrandColors.Warning
        else -> DesignTokens.BrandColors.Error.copy(alpha = 0.1f) to DesignTokens.BrandColors.Error
    }
    
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(DesignTokens.BorderRadius.medium))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${rate.toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "使用率",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}