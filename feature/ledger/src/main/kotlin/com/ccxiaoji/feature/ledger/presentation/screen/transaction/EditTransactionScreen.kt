package com.ccxiaoji.feature.ledger.presentation.screen.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.presentation.screen.ledger.components.CategoryChip
import com.ccxiaoji.feature.ledger.presentation.viewmodel.EditTransactionViewModel
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transactionId: String,
    navController: NavController,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: EditTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }
    
    // 系统返回
    BackHandler { onNavigateBack?.invoke() ?: navController.navigateUp() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.edit_transaction),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack?.invoke() ?: navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                
                // 收入/支出切换
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = !uiState.isIncome,
                        onClick = { viewModel.setIncomeType(false) },
                        label = { Text(stringResource(R.string.expense)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DesignTokens.BrandColors.Error.copy(alpha = 0.1f),
                            selectedLabelColor = DesignTokens.BrandColors.Error
                        )
                    )
                    FilterChip(
                        selected = uiState.isIncome,
                        onClick = { viewModel.setIncomeType(true) },
                        label = { Text(stringResource(R.string.income)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DesignTokens.BrandColors.Success.copy(alpha = 0.1f),
                            selectedLabelColor = DesignTokens.BrandColors.Success
                        )
                    )
                }
                
                // 金额输入
                OutlinedTextField(
                    value = uiState.amountText,
                    onValueChange = viewModel::updateAmount,
                    label = { Text(stringResource(R.string.amount_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (uiState.isIncome) {
                            DesignTokens.BrandColors.Success
                        } else {
                            DesignTokens.BrandColors.Error
                        }
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small),
                    singleLine = true,
                    isError = uiState.amountError != null,
                    supportingText = uiState.amountError?.let { { Text(it) } }
                )
                
                // 分类选择
                Text(
                    text = stringResource(R.string.select_category),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 分类网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(uiState.filteredCategories) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = uiState.selectedCategoryId == category.id,
                            onClick = { viewModel.selectCategory(category.id) }
                        )
                    }
                }
                
                // 备注输入
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = viewModel::updateNote,
                    label = { Text(stringResource(R.string.note_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 保存按钮
                FlatButton(
                    text = stringResource(R.string.save),
                    onClick = { 
                        scope.launch {
                            viewModel.saveTransaction {
                                onNavigateBack?.invoke() ?: navController.navigateUp()
                            }
                        }
                    },
                    enabled = uiState.canSave && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DesignTokens.BrandColors.Ledger
                )
            }
        }
    }
}
