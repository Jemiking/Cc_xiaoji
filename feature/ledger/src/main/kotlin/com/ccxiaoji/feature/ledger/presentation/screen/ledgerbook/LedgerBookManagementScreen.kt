package com.ccxiaoji.feature.ledger.presentation.screen.ledgerbook

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.presentation.screen.ledgerbook.components.LedgerBookItem
import com.ccxiaoji.feature.ledger.presentation.screen.ledgerbook.dialogs.AddEditLedgerBookDialog
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerBookManagementViewModel
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.coroutines.launch

/**
 * 记账簿管理页面
 * 提供完整的记账簿CRUD功能
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LedgerBookManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: LedgerBookManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingLedger by remember { mutableStateOf<com.ccxiaoji.feature.ledger.domain.model.Ledger?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadLedgers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "记账簿管理",
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // 添加新记账簿按钮
                    IconButton(
                        onClick = { showAddDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加记账簿",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "添加记账簿",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.ledgers.isEmpty() -> {
                // 空状态界面
                EmptyLedgerBookState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onAddLedgerBook = { showAddDialog = true }
                )
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        horizontal = DesignTokens.Spacing.medium,
                        vertical = DesignTokens.Spacing.small
                    ),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    // 统计信息卡片
                    item {
                        LedgerBookStatsCard(
                            totalLedgers = uiState.ledgers.size,
                            defaultLedger = uiState.ledgers.find { it.isDefault }?.name ?: "无"
                        )
                    }
                    
                    // 记账簿列表
                    items(
                        items = uiState.ledgers,
                        key = { it.id }
                    ) { ledger ->
                        LedgerBookItem(
                            ledger = ledger,
                            onEdit = { editingLedger = ledger },
                            onDelete = { 
                                scope.launch {
                                    val result = viewModel.deleteLedger(ledger.id)
                                    if (result.isSuccess) {
                                        snackbarHostState.showSnackbar("记账簿「${ledger.name}」已删除")
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            result.exceptionOrNull()?.message ?: "删除失败"
                                        )
                                    }
                                }
                            },
                            onSetDefault = {
                                scope.launch {
                                    val result = viewModel.setDefaultLedger(ledger.id)
                                    if (result.isSuccess) {
                                        snackbarHostState.showSnackbar("已设置「${ledger.name}」为默认记账簿")
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            result.exceptionOrNull()?.message ?: "设置失败"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                    
                    // 底部空间
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
    
    // 添加记账簿对话框
    if (showAddDialog) {
        AddEditLedgerBookDialog(
            ledger = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, description, color, icon ->
                scope.launch {
                    val result = viewModel.createLedger(name, description, color, icon)
                    showAddDialog = false
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar("记账簿「$name」创建成功")
                    } else {
                        snackbarHostState.showSnackbar(
                            result.exceptionOrNull()?.message ?: "创建失败"
                        )
                    }
                }
            }
        )
    }
    
    // 编辑记账簿对话框
    editingLedger?.let { ledger ->
        AddEditLedgerBookDialog(
            ledger = ledger,
            onDismiss = { editingLedger = null },
            onSave = { name, description, color, icon ->
                scope.launch {
                    val result = viewModel.updateLedger(ledger.id, name, description, color, icon)
                    editingLedger = null
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar("记账簿「$name」更新成功")
                    } else {
                        snackbarHostState.showSnackbar(
                            result.exceptionOrNull()?.message ?: "更新失败"
                        )
                    }
                }
            }
        )
    }
}

/**
 * 空状态界面
 */
@Composable
private fun EmptyLedgerBookState(
    modifier: Modifier = Modifier,
    onAddLedgerBook: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        
        Text(
            text = "暂无记账簿",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
        
        Text(
            text = "创建您的第一个记账簿来开始管理财务",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
        
        FlatButton(
            text = "创建记账簿",
            onClick = onAddLedgerBook,
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * 记账簿统计卡片
 */
@Composable
private fun LedgerBookStatsCard(
    totalLedgers: Int,
    defaultLedger: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "记账簿概览",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "总数：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "$totalLedgers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "默认：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = defaultLedger,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}