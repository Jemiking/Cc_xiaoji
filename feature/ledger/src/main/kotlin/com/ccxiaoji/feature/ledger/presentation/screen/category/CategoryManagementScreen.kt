package com.ccxiaoji.feature.ledger.presentation.screen.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryWithStats
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CategoryTab
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    navController: NavController,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类管理") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    viewModel.toggleAddDialog(
                        if (uiState.selectedTab == CategoryTab.EXPENSE) 
                            Category.Type.EXPENSE 
                        else 
                            Category.Type.INCOME
                    )
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加分类")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = if (uiState.selectedTab == CategoryTab.EXPENSE) 0 else 1
            ) {
                Tab(
                    selected = uiState.selectedTab == CategoryTab.EXPENSE,
                    onClick = { viewModel.setSelectedTab(CategoryTab.EXPENSE) },
                    text = { Text("支出分类") }
                )
                Tab(
                    selected = uiState.selectedTab == CategoryTab.INCOME,
                    onClick = { viewModel.setSelectedTab(CategoryTab.INCOME) },
                    text = { Text("收入分类") }
                )
            }
            
            // Category List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = if (uiState.selectedTab == CategoryTab.EXPENSE) {
                    uiState.expenseCategories
                } else {
                    uiState.incomeCategories
                }
                
                items(categories.size) { index ->
                    val categoryWithStats = categories[index]
                    CategoryItem(
                        categoryWithStats = categoryWithStats,
                        onEdit = { viewModel.setEditingCategory(categoryWithStats.category) },
                        onDelete = { viewModel.deleteCategory(categoryWithStats.category.id) }
                    )
                }
            }
        }
    }
    
    // Add Category Dialog
    if (uiState.showAddDialog) {
        AddCategoryDialog(
            categoryType = uiState.addingCategoryType,
            onDismiss = { viewModel.toggleAddDialog() },
            onConfirm = { name, icon, color ->
                viewModel.createCategory(
                    name = name,
                    type = uiState.addingCategoryType,
                    icon = icon,
                    color = color
                )
                viewModel.toggleAddDialog()
            }
        )
    }
    
    // Edit Category Dialog
    uiState.editingCategory?.let { category ->
        EditCategoryDialog(
            category = category,
            onDismiss = { viewModel.setEditingCategory(null) },
            onConfirm = { name, icon, color ->
                viewModel.updateCategory(
                    categoryId = category.id,
                    name = name,
                    icon = icon,
                    color = color
                )
                viewModel.setEditingCategory(null)
            }
        )
    }
    
    // Error Dialog
    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("提示") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
fun CategoryItem(
    categoryWithStats: CategoryWithStats,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEdit() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon with background color
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(categoryWithStats.category.color)).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = categoryWithStats.category.icon,
                        fontSize = 24.sp
                    )
                }
                
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = categoryWithStats.category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (categoryWithStats.category.isSystem) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "系统",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "已使用 ${categoryWithStats.transactionCount} 次",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多选项")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        onClick = {
                            onEdit()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    if (!categoryWithStats.category.isSystem && categoryWithStats.transactionCount == 0) {
                        DropdownMenuItem(
                            text = { Text("删除") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(
    categoryType: Category.Type,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { 
        mutableStateOf(
            if (categoryType == Category.Type.EXPENSE) 
                Category.DEFAULT_EXPENSE_ICONS.first() 
            else 
                Category.DEFAULT_INCOME_ICONS.first()
        )
    }
    var selectedColor by remember { mutableStateOf(Category.DEFAULT_COLORS.first()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = if (categoryType == Category.Type.EXPENSE) "添加支出分类" else "添加收入分类"
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Icon Selection
                Column {
                    Text(
                        text = "选择图标",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val icons = if (categoryType == Category.Type.EXPENSE) {
                            Category.DEFAULT_EXPENSE_ICONS
                        } else {
                            Category.DEFAULT_INCOME_ICONS
                        }
                        items(icons) { icon ->
                            Surface(
                                shape = CircleShape,
                                color = if (icon == selectedIcon) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                border = if (icon == selectedIcon) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    null
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { selectedIcon = icon }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = icon,
                                        fontSize = 24.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Color Selection
                Column {
                    Text(
                        text = "选择颜色",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(Category.DEFAULT_COLORS) { color ->
                            Surface(
                                shape = CircleShape,
                                color = Color(android.graphics.Color.parseColor(color)),
                                border = if (color == selectedColor) {
                                    BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                                } else {
                                    BorderStroke(1.dp, Color.Gray)
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { selectedColor = color }
                            ) {
                                if (color == selectedColor) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedIcon, selectedColor)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryDialog(
    category: Category,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    var selectedIcon by remember { mutableStateOf(category.icon) }
    var selectedColor by remember { mutableStateOf(category.color) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑分类") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !category.isSystem
                )
                
                if (category.isSystem) {
                    Text(
                        text = "系统分类名称不可修改",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Icon Selection
                Column {
                    Text(
                        text = "选择图标",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val icons = if (category.type == Category.Type.EXPENSE) {
                            Category.DEFAULT_EXPENSE_ICONS
                        } else {
                            Category.DEFAULT_INCOME_ICONS
                        }
                        items(icons) { icon ->
                            Surface(
                                shape = CircleShape,
                                color = if (icon == selectedIcon) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                border = if (icon == selectedIcon) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    null
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { selectedIcon = icon }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = icon,
                                        fontSize = 24.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Color Selection
                Column {
                    Text(
                        text = "选择颜色",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(Category.DEFAULT_COLORS) { color ->
                            Surface(
                                shape = CircleShape,
                                color = Color(android.graphics.Color.parseColor(color)),
                                border = if (color == selectedColor) {
                                    BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                                } else {
                                    BorderStroke(1.dp, Color.Gray)
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { selectedColor = color }
                            ) {
                                if (color == selectedColor) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedIcon, selectedColor)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(width, color)
}