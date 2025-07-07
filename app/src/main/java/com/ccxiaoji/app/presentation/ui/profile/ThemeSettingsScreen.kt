package com.ccxiaoji.app.presentation.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.ccxiaoji.app.presentation.ui.profile.theme.components.*
import com.ccxiaoji.app.presentation.viewmodel.ThemeSettingsViewModel
import com.ccxiaoji.ui.components.FlatChip
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 主题设置界面 - 扁平化设计
 */
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 主题模式选择
            ThemeModeSection(
                themeMode = uiState.themeMode,
                onThemeModeChange = viewModel::setThemeMode
            )
            
            // 主题颜色选择
            ThemeColorSection(
                selectedColor = uiState.themeColor,
                onColorSelect = viewModel::setThemeColor
            )
            
            // 动态主题
            if (uiState.supportsDynamicColor) {
                DynamicThemeCard(
                    useDynamicColor = uiState.useDynamicColor,
                    onToggle = viewModel::setUseDynamicColor
                )
            }
            
            // 预览卡片
            ThemePreviewCard()
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
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