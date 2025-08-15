package com.ccxiaoji.feature.ledger.presentation.screen.budget

import androidx.compose.runtime.Composable
import com.ccxiaoji.feature.ledger.data.local.dao.BudgetWithSpent
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.feature.ledger.presentation.screen.budget.components.FlatBudgetDialog

// 为了兼容性，保留这个文件但重定向到新的FlatBudgetDialog
@Composable
fun AddEditBudgetDialog(
    editingBudget: BudgetWithSpent?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (budgetAmountCents: Int, categoryId: String?, alertThreshold: Float, note: String?) -> Unit
) {
    FlatBudgetDialog(
        editingBudget = editingBudget,
        categories = categories,
        onDismiss = onDismiss,
        onSave = onSave
    )
}