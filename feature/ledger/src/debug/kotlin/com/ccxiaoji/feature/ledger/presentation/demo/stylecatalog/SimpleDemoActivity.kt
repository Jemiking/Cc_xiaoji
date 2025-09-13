package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.CcXiaoJiTheme

/**
 * 简化版风格演示Activity - 最小可运行版本
 * 展示11种风格的列表布局差异
 */
class SimpleDemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            CcXiaoJiTheme {
                SimpleDemoScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDemoScreen() {
    var selectedStyle by remember { mutableStateOf("Material You") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记账风格演示") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 风格选择器
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("当前风格: $selectedStyle", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 风格切换按钮
                    LazyColumn(
                        modifier = Modifier.height(100.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        item {
                            TextButton(onClick = { selectedStyle = "Material You" }) {
                                Text("H - Material You")
                            }
                        }
                        item {
                            TextButton(onClick = { selectedStyle = "High Contrast" }) {
                                Text("C - High Contrast")
                            }
                        }
                        item {
                            TextButton(onClick = { selectedStyle = "Neo-Brutalism" }) {
                                Text("K - Neo-Brutalism")
                            }
                        }
                    }
                }
            }
            
            // 演示列表
            when (selectedStyle) {
                "Material You" -> MaterialYouDemoList()
                "High Contrast" -> HighContrastDemoList()
                "Neo-Brutalism" -> NeoBrutalismDemoList()
                else -> MaterialYouDemoList()
            }
        }
    }
}

@Composable
fun MaterialYouDemoList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(5) { index ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("餐饮", style = MaterialTheme.typography.titleSmall)
                        Text("午餐", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("¥${25 + index * 10}", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun HighContrastDemoList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(5) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("餐饮 | 午餐", style = MaterialTheme.typography.bodyMedium)
                Text("¥${25 + index * 10}", style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun NeoBrutalismDemoList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) { index ->
            Box {
                // 阴影层
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .offset(x = 4.dp, y = 4.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.onSurface
                    ) {}
                }
                
                // 内容层
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    border = ButtonDefaults.outlinedButtonBorder,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("餐饮", style = MaterialTheme.typography.titleMedium)
                        Text("¥${25 + index * 10}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}