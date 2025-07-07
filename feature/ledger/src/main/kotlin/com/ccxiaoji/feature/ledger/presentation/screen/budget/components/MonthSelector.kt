package com.ccxiaoji.feature.ledger.presentation.screen.budget.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.ModernCard

@Composable
fun MonthSelector(
    selectedYear: Int,
    selectedMonth: Int,
    onMonthChange: (Int, Int) -> Unit
) {
    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.medium, vertical = DesignTokens.Spacing.small),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (selectedMonth == 1) {
                        onMonthChange(selectedYear - 1, 12)
                    } else {
                        onMonthChange(selectedYear, selectedMonth - 1)
                    }
                }
            ) {
                Icon(
                    Icons.Default.ChevronLeft, 
                    contentDescription = "上一月",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "${selectedYear}年${selectedMonth}月",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            IconButton(
                onClick = {
                    if (selectedMonth == 12) {
                        onMonthChange(selectedYear + 1, 1)
                    } else {
                        onMonthChange(selectedYear, selectedMonth + 1)
                    }
                }
            ) {
                Icon(
                    Icons.Default.ChevronRight, 
                    contentDescription = "下一月",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}