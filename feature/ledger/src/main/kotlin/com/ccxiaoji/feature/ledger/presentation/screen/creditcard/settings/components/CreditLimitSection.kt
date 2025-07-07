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
 * 额度设置部分
 */
@Composable
fun CreditLimitSection(
    creditLimit: String,
    cashAdvanceLimit: String,
    onCreditLimitChange: (String) -> Unit,
    onCashAdvanceLimitChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)) {
        SectionHeader(title = "额度设置")
        
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
                    value = creditLimit,
                    onValueChange = onCreditLimitChange,
                    label = { Text("信用额度") },
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    value = cashAdvanceLimit,
                    onValueChange = onCashAdvanceLimitChange,
                    label = { Text("取现额度") },
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { 
                        Text(
                            "通常为信用额度的50%",
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