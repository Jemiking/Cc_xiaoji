package com.ccxiaoji.feature.ledger.presentation.screen.creditcard.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.components.SectionHeader
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 费用设置部分
 */
@Composable
fun FeeSection(
    annualFee: String,
    annualFeeWaiverThreshold: String,
    interestRate: String,
    onAnnualFeeChange: (String) -> Unit,
    onWaiverThresholdChange: (String) -> Unit,
    onInterestRateChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)) {
        SectionHeader(title = "费用设置")
        
        ModernCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                OutlinedTextField(
                    value = annualFee,
                    onValueChange = onAnnualFeeChange,
                    label = { Text("年费") },
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                OutlinedTextField(
                    value = annualFeeWaiverThreshold,
                    onValueChange = onWaiverThresholdChange,
                    label = { Text("免年费门槛") },
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { 
                        Text(
                            "年消费达到此金额可免年费",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                OutlinedTextField(
                    value = interestRate,
                    onValueChange = onInterestRateChange,
                    label = { Text("日利率") },
                    suffix = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { 
                        Text(
                            "通常为0.05%（万分之五）",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}