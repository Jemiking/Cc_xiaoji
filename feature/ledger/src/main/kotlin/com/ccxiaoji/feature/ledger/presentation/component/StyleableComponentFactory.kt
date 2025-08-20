package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.LedgerUIStyle
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 可风格化的UI组件工厂
 * 提供统一的接口来创建支持不同风格的UI组件
 */
object StyleableComponentFactory {
    
    /**
     * 创建总览卡片组件
     */
    @Composable
    fun OverviewCard(
        monthlyIncome: Double,
        monthlyExpense: Double,
        style: LedgerUIStyle,
        modifier: Modifier = Modifier
    ) {
        when (style) {
            LedgerUIStyle.BALANCED -> BalancedOverviewCard(
                monthlyIncome = monthlyIncome,
                monthlyExpense = monthlyExpense,
                modifier = modifier
            )
            LedgerUIStyle.HIERARCHICAL -> HierarchicalOverviewCard(
                monthlyIncome = monthlyIncome,
                monthlyExpense = monthlyExpense,
                modifier = modifier
            )
        }
    }
    
    /**
     * 创建日期标题组件
     */
    @Composable
    fun DateHeader(
        date: LocalDate,
        style: LedgerUIStyle,
        modifier: Modifier = Modifier
    ) {
        StyleableDateHeader(
            date = date,
            style = style,
            modifier = modifier
        )
    }
    
    /**
     * 创建交易项组件
     */
    @Composable
    fun TransactionItem(
        transaction: Transaction,
        style: LedgerUIStyle,
        isSelected: Boolean = false,
        isSelectionMode: Boolean = false,
        onItemClick: () -> Unit = {},
        onItemLongClick: () -> Unit = {},
        onEdit: () -> Unit = {},
        onDelete: () -> Unit = {},
        onCopy: () -> Unit = {},
        modifier: Modifier = Modifier
    ) {
        StyleableTransactionItem(
            transaction = transaction,
            style = style,
            isSelected = isSelected,
            isSelectionMode = isSelectionMode,
            onItemClick = onItemClick,
            onItemLongClick = onItemLongClick,
            onEdit = onEdit,
            onDelete = onDelete,
            onCopy = onCopy,
            modifier = modifier
        )
    }
}

/**
 * 交易分组数据结构
 * 用于按日期分组显示交易
 */
data class TransactionGroup(
    val date: LocalDate,
    val transactions: List<Transaction>
)

/**
 * 按日期分组交易数据的扩展函数
 */
fun List<Transaction>.groupByDate(): List<TransactionGroup> {
    return this
        .groupBy { 
            it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date 
        }
        .map { (date, transactions) ->
            TransactionGroup(date, transactions.sortedByDescending { it.createdAt })
        }
        .sortedByDescending { it.date }
}

/**
 * LazyListScope扩展函数，用于渲染分组的交易列表
 */
fun LazyListScope.transactionItems(
    transactionGroups: List<TransactionGroup>,
    style: LedgerUIStyle,
    isSelectionMode: Boolean = false,
    selectedTransactionIds: Set<String> = emptySet(),
    onItemClick: (Transaction) -> Unit = {},
    onItemLongClick: (Transaction) -> Unit = {},
    onEdit: (Transaction) -> Unit = {},
    onDelete: (Transaction) -> Unit = {},
    onCopy: (Transaction) -> Unit = {},
    animationDurationMs: Int = 300
) {
    when (style) {
        LedgerUIStyle.BALANCED -> {
            // 方案一：日期分组显示
            items(transactionGroups.size) { index ->
                val group = transactionGroups[index]
                
                // 日期标题
                StyleableComponentFactory.DateHeader(
                    date = group.date,
                    style = style,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // 该日期下的所有交易
                group.transactions.forEachIndexed { transactionIndex, transaction ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = animationDurationMs,
                                delayMillis = transactionIndex * 50 // 错开动画时间
                            )
                        ) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = animationDurationMs,
                                delayMillis = transactionIndex * 50
                            )
                        ),
                        exit = fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(animationDurationMs / 2)
                        ) + slideOutVertically(
                            targetOffsetY = { -it / 4 },
                            animationSpec = androidx.compose.animation.core.tween(animationDurationMs / 2)
                        )
                    ) {
                        StyleableComponentFactory.TransactionItem(
                            transaction = transaction,
                            style = style,
                            isSelected = selectedTransactionIds.contains(transaction.id),
                            isSelectionMode = isSelectionMode,
                            onItemClick = { onItemClick(transaction) },
                            onItemLongClick = { onItemLongClick(transaction) },
                            onEdit = { onEdit(transaction) },
                            onDelete = { onDelete(transaction) },
                            onCopy = { onCopy(transaction) }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        LedgerUIStyle.HIERARCHICAL -> {
            // 方案三：日期分组显示
            items(transactionGroups.size) { index ->
                val group = transactionGroups[index]
                
                // 日期标题
                StyleableComponentFactory.DateHeader(
                    date = group.date,
                    style = style,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // 该日期下的所有交易
                group.transactions.forEachIndexed { transactionIndex, transaction ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = animationDurationMs,
                                delayMillis = transactionIndex * 30 // 更快的错开时间，层次化风格更简洁
                            )
                        ) + slideInVertically(
                            initialOffsetY = { it / 6 },
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = animationDurationMs,
                                delayMillis = transactionIndex * 30
                            )
                        ),
                        exit = fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(animationDurationMs / 2)
                        ) + slideOutVertically(
                            targetOffsetY = { -it / 6 },
                            animationSpec = androidx.compose.animation.core.tween(animationDurationMs / 2)
                        )
                    ) {
                        StyleableComponentFactory.TransactionItem(
                            transaction = transaction,
                            style = style,
                            isSelected = selectedTransactionIds.contains(transaction.id),
                            isSelectionMode = isSelectionMode,
                            onItemClick = { onItemClick(transaction) },
                            onItemLongClick = { onItemLongClick(transaction) },
                            onEdit = { onEdit(transaction) },
                            onDelete = { onDelete(transaction) },
                            onCopy = { onCopy(transaction) }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}