package com.ccxiaoji.app.presentation.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.Transaction

@Composable
fun BarChart(
    transactions: List<Transaction>,
    isExpense: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isExpense) Color(0xFFF44336) else Color(0xFF4CAF50)
    val maxAmount = transactions.maxOfOrNull { it.amountCents } ?: 1
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        transactions.forEach { transaction ->
            val percentage = transaction.amountCents.toFloat() / maxAmount
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category name
                Text(
                    text = transaction.categoryDetails?.name ?: "未分类",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(80.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Amount
                Text(
                    text = "¥${transaction.amountCents / 100f}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(60.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}