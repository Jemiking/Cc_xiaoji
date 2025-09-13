@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ccxiaoji.feature.ledger.presentation.screen.card

 
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
 
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Card
import com.ccxiaoji.feature.ledger.domain.model.CardType
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CardViewModel
import java.io.File

@Composable
fun CardManagementScreen(
    navController: NavHostController
) {
    val viewModel: CardViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 从弹窗编辑改为完整页面导航

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "卡片管理", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("card_add_edit")
            }) {
                Icon(Icons.Default.Add, contentDescription = "添加卡片")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.updateQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("搜索卡片名称或备注") }
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.cards, key = { it.id }) { card ->
                    CardRow(
                        card = card,
                        onClick = {
                            navController.navigate("card_add_edit?cardId=${card.id}")
                        },
                        onDelete = { viewModel.deleteCard(card.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CardRow(card: Card, onClick: () -> Unit, onDelete: () -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 类型图标
            val typeIcon = when (card.cardType) {
                CardType.BANK_DEBIT -> Icons.Filled.Wallet
                CardType.BANK_CREDIT -> Icons.Filled.CreditCard
                CardType.PASSBOOK -> Icons.Filled.Wallet
                CardType.SECURITIES -> Icons.Filled.Badge
                CardType.OTHER -> Icons.Filled.CreditCard
            }
            Icon(typeIcon, contentDescription = null, modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(card.name, style = MaterialTheme.typography.titleMedium)
                val subtitle = buildString {
                    append(card.cardType.name)
                    card.maskedNumber?.let { append(" · ").append(it) }
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onDelete) { Text("删除") }
        }
    }
}

// 弹窗版本已移除，改为完整页面 AddEditCardScreen
