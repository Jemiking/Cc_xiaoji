package com.ccxiaoji.feature.schedule.presentation.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

@Composable
fun CalendarUiDemoScreen(onNavigateBack: () -> Unit) {
    DemoScaffold("Calendar UI Demo", onNavigateBack)
}

@Composable
fun FlatScheduleDemoScreen(onNavigateBack: () -> Unit) {
    DemoScaffold("Flat Schedule Demo", onNavigateBack)
}

@Composable
fun StyleDemoScreen(onNavigateBack: () -> Unit) {
    DemoScaffold("Style Demo", onNavigateBack)
}

@Composable
fun HomeRedesignDemoScreen(onNavigateBack: () -> Unit) {
    DemoScaffold("Home Redesign A3 Demo", onNavigateBack)
}

@Composable
private fun DemoScaffold(title: String, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.padding(16.dp)) {
            Text(text = title, modifier = Modifier.padding(16.dp))
            Button(onClick = onBack, modifier = Modifier.padding(16.dp)) {
                Text("返回")
            }
        }
    }
}

