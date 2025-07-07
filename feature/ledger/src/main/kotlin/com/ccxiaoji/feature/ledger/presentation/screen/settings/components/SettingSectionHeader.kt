package com.ccxiaoji.feature.ledger.presentation.screen.settings.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.ccxiaoji.ui.components.SectionHeader

/**
 * 设置部分标题
 */
@Composable
fun SettingSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    SectionHeader(
        title = title,
        modifier = modifier,
        titleColor = MaterialTheme.colorScheme.primary
    )
}