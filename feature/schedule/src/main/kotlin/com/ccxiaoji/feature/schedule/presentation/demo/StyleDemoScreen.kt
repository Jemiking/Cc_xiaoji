package com.ccxiaoji.feature.schedule.presentation.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign

/**
 * UI设计风格Demo主屏幕
 * 包含5种设计风格的切换展示
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleDemoScreen(
    onNavigateBack: () -> Unit = {}
) {
    android.util.Log.d("StyleDemoScreen", "StyleDemoScreen composable called!")
    var selectedStyle by remember { mutableStateOf(DesignStyle.FLAT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "UI设计风格Demo",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "当前风格：${selectedStyle.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 风格选择器
            StyleSelector(
                selectedStyle = selectedStyle,
                onStyleSelected = { selectedStyle = it },
                modifier = Modifier.padding(16.dp)
            )

            // 根据选中的风格显示相应的demo内容
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedStyle) {
                    DesignStyle.FLAT -> FlatStyleDemo()
                    DesignStyle.SKEUOMORPHISM -> SkeuomorphismStyleDemo()
                    DesignStyle.NEUMORPHISM -> NeumorphismStyleDemo()
                    DesignStyle.GLASSMORPHISM -> GlassmorphismStyleDemo()
                    DesignStyle.MINIMALISM -> MinimalismStyleDemo()
                }
            }
        }
    }
}

/**
 * 风格选择器组件
 */
@Composable
private fun StyleSelector(
    selectedStyle: DesignStyle,
    onStyleSelected: (DesignStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "选择设计风格",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 使用横向滚动的风格按钮
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DesignStyle.values()) { style ->
                    FilterChip(
                        onClick = { onStyleSelected(style) },
                        label = { 
                            Text(
                                text = style.displayName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        selected = selectedStyle == style,
                        leadingIcon = if (selectedStyle == style) {
                            { 
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

/**
 * 设计风格枚举
 */
enum class DesignStyle(val displayName: String, val description: String) {
    FLAT("扁平化", "纯色背景，无阴影和渐变效果，简洁明了"),
    SKEUOMORPHISM("拟物化", "模拟真实物体的质感和阴影，立体感强"),
    NEUMORPHISM("新拟物化", "柔和的内嵌和外凸效果，现代浮雕感"),
    GLASSMORPHISM("毛玻璃", "半透明磨砂玻璃质感，层次丰富"),
    MINIMALISM("极简主义", "大量留白，去除一切非必要装饰元素")
}