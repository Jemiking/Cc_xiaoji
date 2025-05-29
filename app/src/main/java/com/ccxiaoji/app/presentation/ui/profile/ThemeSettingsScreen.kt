package com.ccxiaoji.app.presentation.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.app.presentation.viewmodel.ThemeSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ThemeSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("主题设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 主题模式选择
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "主题模式",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        modifier = Modifier.selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOption(
                            icon = Icons.Default.LightMode,
                            title = "浅色模式",
                            description = "始终使用浅色主题",
                            selected = uiState.themeMode == ThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                        )
                        
                        ThemeOption(
                            icon = Icons.Default.DarkMode,
                            title = "深色模式",
                            description = "始终使用深色主题",
                            selected = uiState.themeMode == ThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
                        )
                        
                        ThemeOption(
                            icon = Icons.Default.Brightness6,
                            title = "跟随系统",
                            description = "根据系统设置自动切换",
                            selected = uiState.themeMode == ThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                        )
                    }
                }
            }
            
            // 主题颜色选择
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "主题颜色",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ThemeColor.values()) { color ->
                            ColorOption(
                                color = color,
                                selected = uiState.themeColor == color,
                                onClick = { viewModel.setThemeColor(color) }
                            )
                        }
                    }
                }
            }
            
            // 动态主题
            if (uiState.supportsDynamicColor) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "动态主题",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "根据壁纸自动调整主题颜色",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.useDynamicColor,
                            onCheckedChange = { viewModel.setUseDynamicColor(it) }
                        )
                    }
                }
            }
            
            // 预览卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "预览",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "这是当前主题的预览效果",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text("芯片") }
                        )
                        Button(
                            onClick = { },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("按钮")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        RadioButton(
            selected = selected,
            onClick = null
        )
    }
}

@Composable
private fun ColorOption(
    color: ThemeColor,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = color.primaryColor,
        onClick = onClick,
        border = if (selected) {
            BorderStroke(3.dp, MaterialTheme.colorScheme.outline)
        } else null
    ) {
        if (selected) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = color.onPrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class ThemeColor(
    val primaryColor: androidx.compose.ui.graphics.Color,
    val onPrimaryColor: androidx.compose.ui.graphics.Color
) {
    BLUE(
        androidx.compose.ui.graphics.Color(0xFF1976D2),
        androidx.compose.ui.graphics.Color.White
    ),
    GREEN(
        androidx.compose.ui.graphics.Color(0xFF388E3C),
        androidx.compose.ui.graphics.Color.White
    ),
    ORANGE(
        androidx.compose.ui.graphics.Color(0xFFF57C00),
        androidx.compose.ui.graphics.Color.White
    ),
    PURPLE(
        androidx.compose.ui.graphics.Color(0xFF7B1FA2),
        androidx.compose.ui.graphics.Color.White
    ),
    RED(
        androidx.compose.ui.graphics.Color(0xFFD32F2F),
        androidx.compose.ui.graphics.Color.White
    ),
    PINK(
        androidx.compose.ui.graphics.Color(0xFFC2185B),
        androidx.compose.ui.graphics.Color.White
    )
}