package com.ccxiaoji.app.presentation.widget.ledger

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ccxiaoji.app.R

class LedgerWidgetPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    PreviewList()
                }
            }
        }
    }
}

@Composable
private fun PreviewList() {
    val ctx = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Ledger Widget - 预览", style = MaterialTheme.typography.titleLarge)

        Card { PreviewWidgetSample(ctx, dark = false, showMonth = true, showQuickAdd = true, title = "浅色 · 默认") }
        Card { PreviewWidgetSample(ctx, dark = true, showMonth = true, showQuickAdd = true, title = "深色 · 默认") }
        Card { PreviewWidgetSample(ctx, dark = false, showMonth = false, showQuickAdd = true, title = "浅色 · 窄宽（隐藏本月）") }
        Card { PreviewWidgetSample(ctx, dark = true, showMonth = true, showQuickAdd = false, title = "深色 · 矮高（隐藏＋记一笔）") }
    }
}

@Composable
private fun PreviewWidgetSample(
    context: android.content.Context,
    dark: Boolean,
    showMonth: Boolean,
    showQuickAdd: Boolean,
    title: String
) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        val inflater = LayoutInflater.from(context)
        val root = inflater.inflate(R.layout.ledger_widget, null) as LinearLayout

        // 填充示例数据
        root.findViewById<TextView>(R.id.tvLedgerName).text = "家庭账本"
        root.findViewById<TextView>(R.id.tvTodayIncome).text = "200.00"
        root.findViewById<TextView>(R.id.tvTodayExpense).text = "150.00"
        root.findViewById<TextView>(R.id.tvMonthIncome).text = "6,800.00"
        root.findViewById<TextView>(R.id.tvMonthExpense).text = "5,450.00"

        // 可见性控制
        root.findViewById<View>(R.id.colMonth).visibility = if (showMonth) View.VISIBLE else View.GONE
        root.findViewById<View>(R.id.btnQuickAdd).visibility = if (showQuickAdd) View.VISIBLE else View.GONE

        // 主题色适配（与 Worker 保持一致）
        val bg = if (dark) Color.parseColor("#121212") else Color.WHITE
        val textPrimary = if (dark) Color.WHITE else Color.BLACK
        root.setBackgroundColor(bg)
        listOf(R.id.tvLedgerName, R.id.tvTodayIncome, R.id.tvTodayExpense, R.id.tvMonthIncome, R.id.tvMonthExpense).forEach { id ->
            (root.findViewById<View>(id) as? TextView)?.setTextColor(textPrimary)
        }

        androidx.compose.ui.viewinterop.AndroidView(factory = { root })
    }
}

