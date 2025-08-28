package com.ccxiaoji.feature.ledger.presentation.quickadd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuickLedgerActivity : ComponentActivity() {
    private val viewModel: QuickLedgerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 VM 输入
        viewModel.initFromIntent(intent?.extras)

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    QuickLedgerDialog(
                        uiStateFlow = viewModel.uiState,
                        onConfirm = { vmState -> viewModel.confirm(vmState) { finish() } },
                        onCancel = { finish() },
                        onSelectAccount = { viewModel.selectAccount(it) },
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onEditNote = { viewModel.updateNote(it) }
                    )
                }
            }
        }
    }
}

