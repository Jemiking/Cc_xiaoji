package com.ccxiaoji.app.presentation.ui.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.runtime.Composable
import com.ccxiaoji.app.presentation.ui.common.FeatureUpgradeScreen

@Composable
fun DataExportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToImport: () -> Unit = {},
    onNavigateToGuide: () -> Unit = {},
) {
    FeatureUpgradeScreen(
        title = "数据导出",
        description = "我们正在升级数据导出功能，将支持更好的CSV格式（兼容钱迹）和更快的导出速度。",
        icon = Icons.Default.CloudUpload,
        onBack = onNavigateBack
    )
}