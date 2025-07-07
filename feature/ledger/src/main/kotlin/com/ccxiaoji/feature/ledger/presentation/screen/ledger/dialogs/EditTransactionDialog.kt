package com.ccxiaoji.feature.ledger.presentation.screen.ledger.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryDetails
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.presentation.screen.ledger.components.CategoryChip
import com.ccxiaoji.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit
) {
    var amount by remember { mutableStateOf(transaction.amountYuan.toString()) }
    var selectedCategoryId by remember { mutableStateOf(transaction.categoryId) }
    var note by remember { mutableStateOf(transaction.note ?: "") }
    var isIncome by remember { mutableStateOf(
        transaction.categoryDetails?.type == "INCOME"
    ) }
    
    // Filter categories by type
    val filteredCategories = remember(isIncome, categories) {
        categories.filter { it.type == if (isIncome) Category.Type.INCOME else Category.Type.EXPENSE }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(R.string.edit_transaction),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // Income/Expense Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = !isIncome,
                        onClick = { 
                            isIncome = false
                            // Find a category in the new type
                            if (filteredCategories.none { it.id == selectedCategoryId }) {
                                selectedCategoryId = filteredCategories.firstOrNull()?.id ?: ""
                            }
                        },
                        label = { Text(stringResource(R.string.expense)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DesignTokens.BrandColors.Error.copy(alpha = 0.1f),
                            selectedLabelColor = DesignTokens.BrandColors.Error
                        )
                    )
                    FilterChip(
                        selected = isIncome,
                        onClick = { 
                            isIncome = true
                            // Find a category in the new type
                            if (filteredCategories.none { it.id == selectedCategoryId }) {
                                selectedCategoryId = filteredCategories.firstOrNull()?.id ?: ""
                            }
                        },
                        label = { Text(stringResource(R.string.income)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DesignTokens.BrandColors.Success.copy(alpha = 0.1f),
                            selectedLabelColor = DesignTokens.BrandColors.Success
                        )
                    )
                }
                
                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text(stringResource(R.string.amount_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isIncome) {
                            DesignTokens.BrandColors.Success
                        } else {
                            DesignTokens.BrandColors.Error
                        }
                    )
                )
                
                // Category Selection
                Text(
                    text = stringResource(R.string.select_category),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Category grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(filteredCategories) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id }
                        )
                    }
                }
                
                // Note Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountCents = ((amount.toDoubleOrNull() ?: 0.0) * 100).toInt()
                    val categoryId = selectedCategoryId
                    val updatedCategory = filteredCategories.find { it.id == categoryId }
                    val updatedTransaction = transaction.copy(
                        amountCents = amountCents,
                        categoryId = categoryId,
                        categoryDetails = updatedCategory?.let {
                            CategoryDetails(
                                id = it.id,
                                name = it.name,
                                icon = it.icon,
                                color = it.color,
                                type = it.type.name
                            )
                        },
                        note = note.ifEmpty { null }
                    )
                    onConfirm(updatedTransaction)
                },
                enabled = amount.isNotEmpty() && selectedCategoryId.isNotEmpty(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = DesignTokens.BrandColors.Ledger
                )
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.large),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    )
}