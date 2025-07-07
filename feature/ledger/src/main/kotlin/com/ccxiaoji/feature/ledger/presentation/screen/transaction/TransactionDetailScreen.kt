package com.ccxiaoji.feature.ledger.presentation.screen.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerViewModel
import com.ccxiaoji.feature.ledger.presentation.screen.transaction.components.*
import com.ccxiaoji.feature.ledger.presentation.navigation.LedgerNavigation
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    navController: NavController,
    viewModel: LedgerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val transaction = uiState.transactions.find { it.id == transactionId }
    
    if (transaction == null) {
        // 交易未找到，导航返回
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }
    
    // 监听删除结果
    LaunchedEffect(key1 = navController.currentBackStackEntry) {
        navController.currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("transaction_deleted")
            ?.observeForever { deleted ->
                if (deleted == true) {
                    navController.popBackStack()
                }
            }
    }
    
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交易详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        navController.navigate(LedgerNavigation.editTransactionRoute(transactionId))
                    }) {
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { 
                        navController.navigate(LedgerNavigation.deleteTransaction(transactionId))
                    }) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "删除",
                            tint = DesignTokens.BrandColors.Error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 交易金额卡片
            TransactionAmountCard(
                categoryIcon = transaction.categoryDetails?.icon ?: "📝",
                amount = transaction.amountYuan,
                categoryName = transaction.categoryDetails?.name ?: "其他",
                isIncome = transaction.categoryDetails?.type == "INCOME"
            )
            
            // 交易详情
            TransactionDetailCard {
                // 日期和时间
                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "日期时间",
                    value = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                        .toJavaLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                
                // 备注
                DetailRow(
                    icon = Icons.Default.Note,
                    label = "备注",
                    value = transaction.note ?: "无备注"
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                
                // 交易ID
                DetailRow(
                    icon = Icons.Default.Tag,
                    label = "交易ID",
                    value = transaction.id.take(8) + "..."
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                
                // 最后更新
                DetailRow(
                    icon = Icons.Default.Update,
                    label = "最后更新",
                    value = transaction.updatedAt.toLocalDateTime(TimeZone.currentSystemDefault())
                        .toJavaLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                )
            }
            
            // 即将推出的功能
            FeatureComingSoonCard(
                title = "即将推出",
                features = "• 交易标签功能\n• 上传收据照片\n• 关联预算项目\n• 分期付款支持"
            )
        }
    }
    
}