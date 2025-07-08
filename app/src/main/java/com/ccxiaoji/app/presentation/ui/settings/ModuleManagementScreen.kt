package com.ccxiaoji.app.presentation.ui.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.app.presentation.ui.navigation.defaultModules
import com.ccxiaoji.app.presentation.viewmodel.ModuleManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleManagementScreen(
    navController: NavController,
    viewModel: ModuleManagementViewModel = hiltViewModel()
) {
    val modules by viewModel.modules.collectAsState()
    val hiddenModules by viewModel.hiddenModules.collectAsState()
    val useClassicLayout by viewModel.useClassicLayout.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("模块管理") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveChanges()
                            navController.popBackStack()
                        }
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "使用经典布局",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "显示6个底部导航项",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useClassicLayout,
                            onCheckedChange = { viewModel.toggleClassicLayout() }
                        )
                    }
                }
            }
            
            item {
                Text(
                    "点击眼睛图标隐藏/显示模块",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(modules, key = { it.id }) { module ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = module.icon,
                                contentDescription = module.name,
                                tint = module.color
                            )
                            
                            Text(
                                text = module.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        IconButton(
                            onClick = { viewModel.toggleModuleVisibility(module.id) }
                        ) {
                            Icon(
                                imageVector = if (module.id in hiddenModules)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                                contentDescription = if (module.id in hiddenModules) "显示" else "隐藏",
                                tint = if (module.id in hiddenModules)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}