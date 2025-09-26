package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OverviewCard(
    state: OverviewState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ReportTokens.Palette.Card),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ReportTokens.Metrics.CardCorner),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ReportTokens.Metrics.CardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题左上角，样式与“分类报表”一致
            Text(
                text = "收支总览",
                color = ReportTokens.Palette.TextPrimary,
                fontSize = ReportTokens.Type.Title,
                fontWeight = FontWeight.Medium
            )

            // 第一行：支出 / 收入
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewItem(label = "支出", value = state.expense, valueColor = ReportTokens.Palette.ExpenseRed, modifier = Modifier.weight(1f))
                OverviewItem(label = "收入", value = state.income, valueColor = ReportTokens.Palette.IncomeGreen, modifier = Modifier.weight(1f))
            }

            // 第二行：结余 / 日均支出（均为黑色）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewItem(label = "结余", value = state.balance, modifier = Modifier.weight(1f))
                state.averageDailyExpense?.let {
                    OverviewItem(label = "日均支出", value = it, modifier = Modifier.weight(1f))
                } ?: run {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // 第三行：将首个额外指标放入左列，右列留空，保持 2×3 栅格
            if (state.extraMetrics.isNotEmpty()) {
                val m = state.extraMetrics.first()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OverviewItem(label = m.label, value = m.value, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun OverviewItem(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = ReportTokens.Palette.TextPrimary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(text = label, color = ReportTokens.Palette.TextMuted, fontSize = ReportTokens.Type.Caption)
        Text(text = value, color = valueColor, fontSize = ReportTokens.Type.NumberLarge, fontWeight = FontWeight.Medium)
    }
}

data class OverviewState(
    val expense: String,
    val income: String,
    val balance: String,
    val averageDailyExpense: String? = null,
    val extraMetrics: List<Metric> = emptyList()
)

data class Metric(
    val label: String,
    val value: String,
    val color: androidx.compose.ui.graphics.Color? = null
)
