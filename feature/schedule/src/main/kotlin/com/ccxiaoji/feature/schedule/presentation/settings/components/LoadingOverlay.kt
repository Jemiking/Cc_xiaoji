package com.ccxiaoji.feature.schedule.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * 加载状态覆盖层 - 扁平化设计
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.clip(MaterialTheme.shapes.medium),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(com.ccxiaoji.ui.theme.DesignTokens.Spacing.medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}