package com.ccxiaoji.app.presentation.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleTopBar(
    title: String,
    isRootScreen: Boolean,
    onNavigationClick: () -> Unit,
    onCloseClick: () -> Unit,
    navigationIcon: ImageVector = if (isRootScreen) Icons.Default.Menu else Icons.Default.ArrowBack,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { 
            Text(text = title) 
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = navigationIcon,
                    contentDescription = if (isRootScreen) "菜单" else "返回"
                )
            }
        },
        actions = {
            actions()
            if (isRootScreen) {
                TextButton(onClick = onCloseClick) {
                    Text("完成")
                }
            }
        }
    )
}