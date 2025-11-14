package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 金额显示头部组件
 *
 * 显示交易金额、货币单位、账户信息等
 *
 * @param amountText 金额文本（可以是数字或表达式）
 * @param currency 货币单位
 * @param isIncome 是否为收入
 * @param accountName 账户名称
 * @param errorMessage 错误消息
 * @param modifier 修饰符
 * @param onCurrencyClick 货币单位点击回调
 */
@Composable
fun AmountHeader(
    amountText: String,
    currency: String = "CNY",
    isIncome: Boolean = false,
    accountName: String? = null,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
    onCurrencyClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 金额显示区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 账户信息（可选）
            if (accountName != null) {
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f, fill = false)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f, fill = false))
            }

            // 金额和货币
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End
            ) {
                // 货币单位
                TextButton(
                    onClick = { onCurrencyClick?.invoke() },
                    enabled = onCurrencyClick != null,
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(
                        text = currency,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 金额
                Text(
                    text = if (amountText.isEmpty()) "0" else amountText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = when {
                        errorMessage != null -> MaterialTheme.colorScheme.error
                        isIncome -> DesignTokens.BrandColors.Success
                        else -> DesignTokens.BrandColors.Error
                    },
                    textAlign = TextAlign.End
                )
            }
        }

        // 错误消息
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * 金额显示配置
 */
data class AmountHeaderConfig(
    val amountTextSize: Float = 28f,
    val currencyTextSize: Float = 14f,
    val accountTextSize: Float = 12f,
    val horizontalPadding: Int = 16,
    val verticalPadding: Int = 12,
    val showCurrencySymbol: Boolean = true,
    val showAccountInfo: Boolean = true
)

/**
 * 格式化金额显示
 */
fun formatAmountDisplay(
    amount: Double,
    currency: String = "CNY",
    showSign: Boolean = false
): String {
    val formattedAmount = "%.2f".format(amount)
    val sign = when {
        showSign && amount > 0 -> "+"
        showSign && amount < 0 -> ""
        else -> ""
    }

    return when (currency) {
        "CNY" -> "$sign¥$formattedAmount"
        "USD" -> "$sign$$formattedAmount"
        "EUR" -> "$sign€$formattedAmount"
        "JPY" -> "$sign¥${formattedAmount.removeSuffix(".00")}"
        else -> "$sign$formattedAmount $currency"
    }
}