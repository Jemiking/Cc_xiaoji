package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.LedgerUIStyle
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * 支持多种风格的日期标题组件
 */
@Composable
fun StyleableDateHeader(
    date: LocalDate,
    style: LedgerUIStyle,
    modifier: Modifier = Modifier
) {
    when (style) {
        LedgerUIStyle.BALANCED -> BalancedDateHeader(
            date = date,
            modifier = modifier
        )
        LedgerUIStyle.HIERARCHICAL -> HierarchicalDateHeader(
            date = date,
            modifier = modifier
        )
    }
}

/**
 * 平衡风格的日期标题
 */
@Composable
private fun BalancedDateHeader(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val dateText = when {
        date == now -> "今天"
        date == now.minus(DatePeriod(days = 1)) -> "昨天"
        date == now.minus(DatePeriod(days = 2)) -> "前天"
        else -> "${date.monthNumber}月${date.dayOfMonth}日"
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = DesignTokens.BrandColors.Ledger.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        DesignTokens.BrandColors.Ledger,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = DesignTokens.BrandColors.Ledger
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = date.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 层次化风格的日期标题
 */
@Composable
private fun HierarchicalDateHeader(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val dateText = when {
        date == now -> "今天"
        date == now.minus(DatePeriod(days = 1)) -> "昨天"
        date == now.minus(DatePeriod(days = 2)) -> "前天"
        else -> "${date.monthNumber}月${date.dayOfMonth}日"
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
        )
    }
}