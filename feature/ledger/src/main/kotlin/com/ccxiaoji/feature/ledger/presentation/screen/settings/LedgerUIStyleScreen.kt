package com.ccxiaoji.feature.ledger.presentation.screen.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.domain.model.LedgerUIStyle
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * UI风格设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerUIStyleScreen(
    navController: NavController,
    viewModel: LedgerUIStyleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 错误提示
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // 这里可以显示SnackBar或其他错误提示
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("界面风格设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            
            // 风格选择区域
            StyleSelectionSection(
                currentStyle = uiState.uiStyle,
                onStyleSelected = { viewModel.updateUIStyle(it) },
                animationDuration = uiState.animationDurationMs
            )
            
            // 设置选项
            SettingsSection(
                animationDuration = uiState.animationDurationMs,
                onAnimationDurationChanged = { viewModel.updateAnimationDuration(it) }
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
        }
    }
}


/**
 * 风格选择区域
 */
@Composable
private fun StyleSelectionSection(
    currentStyle: LedgerUIStyle,
    onStyleSelected: (LedgerUIStyle) -> Unit,
    animationDuration: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium)
        ) {
            Text(
                text = "选择界面风格",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 风格选项
            LedgerUIStyle.getAllStyles().forEach { style ->
                StyleOption(
                    style = style,
                    isSelected = currentStyle == style,
                    onSelected = { onStyleSelected(style) },
                    animationDuration = animationDuration
                )
                
                if (style != LedgerUIStyle.getAllStyles().last()) {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                }
            }
        }
    }
}

/**
 * 单个风格选项
 */
@Composable
private fun StyleOption(
    style: LedgerUIStyle,
    isSelected: Boolean,
    onSelected: () -> Unit,
    animationDuration: Int
) {
    AnimatedContent(
        targetState = isSelected,
        transitionSpec = {
            fadeIn(
                animationSpec = androidx.compose.animation.core.tween(animationDuration)
            ) togetherWith fadeOut(
                animationSpec = androidx.compose.animation.core.tween(animationDuration)
            )
        },
        label = "StyleOptionAnimation"
    ) { selected ->
        Card(
            onClick = onSelected,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (selected) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                else 
                    MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = style.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = style.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "已选择",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * 设置选项区域
 */
@Composable
private fun SettingsSection(
    animationDuration: Int,
    onAnimationDurationChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium)
        ) {
            Text(
                text = "高级设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 动画持续时间
            Column {
                Text(
                    text = "切换动画速度",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "当前: ${animationDuration}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                
                Slider(
                    value = animationDuration.toFloat(),
                    onValueChange = { onAnimationDurationChanged(it.toInt()) },
                    valueRange = 100f..1000f,
                    steps = 8, // 100, 200, 300, ..., 1000
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}