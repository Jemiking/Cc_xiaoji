package com.ccxiaoji.app.presentation.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.app.presentation.ui.navigation.ModuleInfo

@Composable
fun ModuleGrid(
    modules: List<ModuleInfo>,
    onModuleClick: (ModuleInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),  // 改为2列布局
        horizontalArrangement = Arrangement.spacedBy(16.dp),  // 增加水平间距
        verticalArrangement = Arrangement.spacedBy(16.dp),    // 增加垂直间距
        modifier = modifier
            .fillMaxWidth()
            .height(((modules.filter { it.enabled }.size + 1) / 2 * 120).dp),  // 调整高度计算
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            items = modules.filter { it.enabled },
            key = { it.id }
        ) { module ->
            SimpleModuleCard(
                module = module,
                onClick = { onModuleClick(module) }
            )
        }
    }
}