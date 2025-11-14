package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionType
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 交易类型选择器标签页
 *
 * 用于切换收入、支出、转账三种交易类型
 *
 * @param selectedType 当前选中的交易类型
 * @param onTypeChange 类型变更回调
 * @param allowedTypes 允许的交易类型集合
 * @param modifier 修饰符
 * @param enabled 是否启用
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionTypeTabs(
    selectedType: TransactionType,
    onTypeChange: (TransactionType) -> Unit,
    allowedTypes: Set<TransactionType> = setOf(
        TransactionType.EXPENSE,
        TransactionType.INCOME,
        TransactionType.TRANSFER
    ),
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val tabs = remember(allowedTypes) {
        allowedTypes.filter { it != TransactionType.ALL }.sortedBy { it.ordinal }
    }

    if (tabs.size <= 1) {
        // 如果只有一种类型，不显示标签页
        return
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        TabRow(
            selectedTabIndex = tabs.indexOf(selectedType).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                if (tabs.indexOf(selectedType) >= 0) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            tabPositions[tabs.indexOf(selectedType)]
                        ),
                        color = when (selectedType) {
                            TransactionType.EXPENSE -> DesignTokens.BrandColors.Error
                            TransactionType.INCOME -> DesignTokens.BrandColors.Success
                            TransactionType.TRANSFER -> DesignTokens.BrandColors.Ledger
                            TransactionType.ALL -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        ) {
            tabs.forEach { type ->
                Tab(
                    selected = selectedType == type,
                    onClick = {
                        if (enabled) {
                            onTypeChange(type)
                        }
                    },
                    enabled = enabled,
                    text = {
                        Text(
                            text = when (type) {
                                TransactionType.EXPENSE -> stringResource(R.string.expense)
                                TransactionType.INCOME -> stringResource(R.string.income)
                                TransactionType.TRANSFER -> stringResource(R.string.transfer)
                                TransactionType.ALL -> stringResource(R.string.all)
                            },
                            color = when {
                                !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                selectedType == type -> when (type) {
                                    TransactionType.EXPENSE -> DesignTokens.BrandColors.Error
                                    TransactionType.INCOME -> DesignTokens.BrandColors.Success
                                    TransactionType.TRANSFER -> DesignTokens.BrandColors.Ledger
                                    TransactionType.ALL -> MaterialTheme.colorScheme.primary
                                }
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                )
            }
        }
    }
}

/**
 * 交易类型标签配置
 */
data class TransactionTypeTabConfig(
    val tabHeight: Dp = 40.dp,
    val tabMinWidth: Dp = 0.dp,
    val cornerRadius: Dp = 8.dp,
    val showIndicator: Boolean = true,
    val animationEnabled: Boolean = true
)