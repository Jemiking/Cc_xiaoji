package com.ccxiaoji.app.presentation.widget.ledger

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.ccxiaoji.app.BuildConfig

@AndroidEntryPoint
class LedgerWidgetConfigActivity : ComponentActivity() {

    @Inject lateinit var manageLedgerUseCase: ManageLedgerUseCase
    @Inject lateinit var userApi: UserApi
    @Inject lateinit var widgetPrefs: LedgerWidgetPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        Log.d("CCXJ/WIDGET", "ConfigActivity onCreate appWidgetId=$appWidgetId, pkg=${BuildConfig.APPLICATION_ID}")
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "Config start id=$appWidgetId", Toast.LENGTH_SHORT).show()
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LedgerPicker(
                        getLedgers = { manageLedgerUseCase.getUserLedgers(userApi.getCurrentUserId()) },
                        onSelected = { ledgerId -> handleSelected(ledgerId, appWidgetId) }
                    )
                }
            }
        }
    }

    private fun handleSelected(ledgerId: String, appWidgetId: Int) {
        lifecycleScope.launch {
            try {
                widgetPrefs.setLedgerId(appWidgetId, ledgerId)
                val result = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                setResult(Activity.RESULT_OK, result)
                WidgetUpdateScheduler.enqueueForIds(this@LedgerWidgetConfigActivity, intArrayOf(appWidgetId))
            } finally {
                finish()
            }
        }
    }
}

@Composable
private fun LedgerPicker(
    getLedgers: () -> kotlinx.coroutines.flow.Flow<List<com.ccxiaoji.feature.ledger.domain.model.Ledger>>,
    onSelected: (String) -> Unit
) {
    var ledgers by remember { mutableStateOf<List<com.ccxiaoji.feature.ledger.domain.model.Ledger>>(emptyList()) }

    LaunchedEffect(Unit) {
        getLedgers().collectLatest { list ->
            ledgers = list
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "选择账本", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(ledgers, key = { it.id }) { ledger ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelected(ledger.id) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = ledger.name, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
