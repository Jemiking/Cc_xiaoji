package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DemoNavHost
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DEMO_ROUTE_EXPENSE_PREVIEW
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.DemoThemeProvider
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.DemoViewModel
import androidx.compose.ui.graphics.Color

/**
 * 风格目录演示Activity
 * 完全独立的Activity，包含独立的导航系统和数据
 */
class StyleCatalogDemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            StyleCatalogDemoApp()
        }
    }
}

/**
 * Demo应用主入口
 * 使用DemoThemeProvider包装，提供风格切换能力
 */
@Composable
fun StyleCatalogDemoApp() {
    val viewModel: DemoViewModel = viewModel()
    val currentStyle by viewModel.currentStyle.collectAsState()
    val currentDensity by viewModel.currentDensity.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    // 使用DemoThemeProvider包装整个应用
    DemoThemeProvider(
        style = currentStyle,
        density = currentDensity,
        darkMode = isDarkMode
    ) {
        // 独立的导航系统 - 传递共享的viewModel
        DemoNavHost(
            viewModel = viewModel,
            // 恢复为以预览页为起点，回到改前行为
            startDestination = DEMO_ROUTE_EXPENSE_PREVIEW
        )
    }
}
