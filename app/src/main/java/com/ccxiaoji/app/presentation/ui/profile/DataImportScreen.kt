package com.ccxiaoji.app.presentation.ui.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.runtime.Composable
import com.ccxiaoji.app.presentation.ui.common.FeatureUpgradeScreen

@Composable
fun DataImportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGuide: () -> Unit = {},
) {
    FeatureUpgradeScreen(
        title = "数据导入",
        description = "我们正在升级数据导入功能，将支持钱迹CSV格式导入，让您轻松迁移数据。",
        icon = Icons.Default.CloudDownload,
        onBack = onNavigateBack
    )
}