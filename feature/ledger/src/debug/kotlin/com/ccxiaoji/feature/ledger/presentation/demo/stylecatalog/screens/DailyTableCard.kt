package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DailyRow(
    val date: String,
    val income: String,
    val expense: String,
    val balance: String
)

@Composable
fun DailyTableCard(
    rows: List<DailyRow>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ReportTokens.Palette.Card),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ReportTokens.Metrics.CardCorner),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(ReportTokens.Metrics.CardPadding)) {
            Text(text = "日报表", color = ReportTokens.Palette.TextPrimary, fontSize = ReportTokens.Type.Title, fontWeight = FontWeight.Medium)
            Text(text = "日期为本月统计，单位：元", color = ReportTokens.Palette.TextMuted, fontSize = ReportTokens.Type.Caption)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ReportTokens.Metrics.TableHeaderHeight),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableCell(text = "日期", weight = 0.25f, colorHex = 0xFF808080)
                TableCell(text = "收入", weight = 0.25f, alignEnd = true, colorHex = 0xFF808080)
                TableCell(text = "支出", weight = 0.25f, alignEnd = true, colorHex = 0xFF808080)
                TableCell(text = "结余", weight = 0.25f, alignEnd = true, colorHex = 0xFF808080)
            }
            Divider(thickness = ReportTokens.Metrics.DividerThickness, color = ReportTokens.Palette.Divider)

            Column {
                rows.forEachIndexed { index, r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell(text = r.date, weight = 0.25f)
                        TableCell(text = r.income, weight = 0.25f, alignEnd = true, color = ReportTokens.Palette.IncomeGreen)
                        TableCell(text = r.expense, weight = 0.25f, alignEnd = true, color = ReportTokens.Palette.ExpenseRed)
                        val balColor = if (r.balance.startsWith("-")) ReportTokens.Palette.ExpenseRed else ReportTokens.Palette.IncomeGreen
                        TableCell(text = r.balance, weight = 0.25f, alignEnd = true, color = balColor)
                    }
                    if (index != rows.lastIndex) {
                        Divider(thickness = ReportTokens.Metrics.DividerThickness, color = ReportTokens.Palette.Divider)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    alignEnd: Boolean = false,
    color: androidx.compose.ui.graphics.Color = ReportTokens.Palette.TextPrimary,
    colorHex: Long? = null
) {
    val c = colorHex?.let { androidx.compose.ui.graphics.Color(it.toULong()) } ?: color
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 2.dp),
        contentAlignment = if (alignEnd) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = c,
            fontSize = 13.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

