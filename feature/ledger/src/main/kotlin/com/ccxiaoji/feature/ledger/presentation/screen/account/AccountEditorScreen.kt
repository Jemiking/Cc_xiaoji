package com.ccxiaoji.feature.ledger.presentation.screen.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.component.EntityEditorScaffold
import com.ccxiaoji.feature.ledger.presentation.screen.account.components.AccountForm
import com.ccxiaoji.feature.ledger.presentation.screen.account.components.CreditCardFields
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AccountEditorViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * 统一的账户编辑器界面
 *
 * 使用EntityEditorScaffold框架实现，整合新增和编辑功能
 *
 * 支持功能：
 * - 新增/编辑账户（通过accountId区分）
 * - 信用卡特殊字段支持
 * - 未保存变更拦截
 * - 统一的保存/删除操作
 *
 * @param navController 导航控制器
 * @param accountId 账户ID，null 表示新增模式
 * @param onNavigateBack 返回导航回调
 * @param accountType 账户类型（仅新增模式有效）
 */
@Composable
fun AccountEditorScreen(
    navController: NavController,
    accountId: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    accountType: String? = null,
    viewModel: AccountEditorViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val editorState = viewModel.editorState  // EntityEditorState 不是 StateFlow
    val scrollState = rememberScrollState()

    // 显示删除确认对话框
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 处理保存成功事件
    LaunchedEffect(viewModel) {
        viewModel.savedEvent.collectLatest {
            onNavigateBack?.invoke() ?: navController.popBackStack()
        }
    }

    EntityEditorScaffold(
        title = if (viewModel.isEditMode) "编辑账户" else "添加账户",
        state = editorState,
        onBack = {
            onNavigateBack?.invoke() ?: navController.popBackStack()
        },
        onSave = {
            viewModel.saveAccount()
        },
        actions = {
            // 编辑模式显示删除按钮
            if (viewModel.isEditMode) {
                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除账户",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 使用AccountForm组件
            AccountForm(
                name = formState.name,
                type = formState.selectedType,
                balance = formState.balance,
                onNameChange = viewModel::updateName,
                onTypeChange = viewModel::selectType,
                onBalanceChange = viewModel::updateBalance,
                isEditMode = viewModel.isEditMode,
                typeReadOnly = viewModel.isEditMode, // 编辑模式不允许修改类型
                nameError = formState.nameError,
                balanceError = formState.balanceError,
                creditCardFields = if (formState.selectedType == com.ccxiaoji.feature.ledger.domain.model.AccountType.CREDIT_CARD) {
                    CreditCardFields(
                        creditLimit = formState.creditLimit,
                        billingDay = formState.billingDay,
                        dueDay = formState.paymentDueDay,
                        onCreditLimitChange = viewModel::updateCreditLimit,
                        onBillingDayChange = viewModel::updateBillingDay,
                        onDueDayChange = viewModel::updatePaymentDueDay
                    )
                } else null,
                enabled = !editorState.isLoading && !editorState.isSaving
            )

            // 表单错误提示（如果有）
            val errorMessages = listOfNotNull(
                formState.nameError,
                formState.balanceError,
                formState.creditLimitError,
                formState.billingDayError,
                formState.paymentDueDayError
            )

            if (errorMessages.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        errorMessages.forEach { error ->
                            Text(
                                text = "• $error",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除账户") },
            text = { Text("确定要删除这个账户吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount()
                    }
                ) {
                    Text(
                        "删除",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 账户编辑模式枚举
 */
enum class AccountEditorMode {
    ADD,    // 新增模式
    EDIT    // 编辑模式
}

/**
 * 账户编辑器配置
 */
data class AccountEditorConfig(
    val mode: AccountEditorMode = AccountEditorMode.ADD,
    val typeReadOnly: Boolean = false,              // 编辑模式下是否允许修改类型
    val showCreditCardFields: Boolean = false,      // 是否显示信用卡特殊字段
    val showInitialBalance: Boolean = true,         // 是否显示初始余额
    val showAccountOrder: Boolean = false,          // 是否显示账户排序
    val allowDelete: Boolean = true                 // 是否允许删除
)

/**
 * 账户表单标签配置
 */
data class AccountFormLabels(
    val titleAdd: String = "添加账户",
    val titleEdit: String = "编辑账户",
    val saveButtonAdd: String = "创建",
    val saveButtonEdit: String = "保存",
    val deleteButton: String = "删除账户",
    val nameLabel: String = "账户名称",
    val typeLabel: String = "账户类型",
    val balanceLabel: String = "当前余额",
    val creditLimitLabel: String = "信用额度",
    val billingDayLabel: String = "账单日",
    val dueDayLabel: String = "还款日"
)