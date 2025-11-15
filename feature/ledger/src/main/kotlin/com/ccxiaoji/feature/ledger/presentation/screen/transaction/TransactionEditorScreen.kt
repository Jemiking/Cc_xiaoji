package com.ccxiaoji.feature.ledger.presentation.screen.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.component.EntityEditorScaffold
import com.ccxiaoji.feature.ledger.presentation.component.EntityEditorState
import com.ccxiaoji.feature.ledger.presentation.component.transaction.AccountSelectionSection
import com.ccxiaoji.feature.ledger.presentation.component.transaction.TransferAccountSection
import com.ccxiaoji.feature.ledger.presentation.component.transaction.AmountInputSection
import com.ccxiaoji.feature.ledger.presentation.component.transaction.CategorySelectionSection
import com.ccxiaoji.feature.ledger.presentation.component.transaction.DateTimeSection
import com.ccxiaoji.feature.ledger.presentation.component.transaction.LedgerSelectionSection
import com.ccxiaoji.feature.ledger.presentation.component.transaction.LinkTargetsSection
import com.ccxiaoji.feature.ledger.presentation.component.transaction.NoteInputSection
import com.ccxiaoji.feature.ledger.presentation.component.transaction.TransactionTypeSelector
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AddTransactionViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionType

/**
 * 统一的交易编辑器界面
 *
 * 支持新增和编辑两种模式：
 * - 新增模式：transactionId 为 null
 * - 编辑模式：transactionId 不为 null
 *
 * 设计原则：
 * - 参数化设计，通过 transactionId 区分模式
 * - 复用现有的 AddTransactionViewModel（后续可考虑统一为 TransactionEditorViewModel）
 * - 组件化架构，便于测试和维护
 * - 利用EntityEditorScaffold提供的统一功能
 *
 * @param navController 导航控制器
 * @param transactionId 交易ID，null 表示新增模式
 * @param onNavigateBack 返回导航回调
 * @param viewModel 交易视图模型
 */
@Composable
fun TransactionEditorScreen(
    navController: NavController,
    transactionId: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    // 使用新的状态分离模式，直接访问formState和editorState
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()

    // 初始化编辑模式（如果有transactionId）
    LaunchedEffect(transactionId) {
        transactionId?.let {
            // ViewModel的init块已处理，这里可以添加额外逻辑
        }
    }

    // 监听保存成功事件
    LaunchedEffect(viewModel) {
        viewModel.saveSuccessEvent.collect {
            // 保存成功后返回
            onNavigateBack?.invoke() ?: navController.popBackStack()
        }
    }

    // 根据交易类型和模式决定标题
    val title = when {
        editorState.isEditMode -> "编辑交易"
        formState.transactionType == TransactionType.INCOME -> "记收入"
        formState.transactionType == TransactionType.EXPENSE -> "记支出"
        formState.transactionType == TransactionType.TRANSFER -> "记转账"
        else -> "记一笔"
    }

    // 使用EntityEditorScaffold提供统一的页面框架
    EntityEditorScaffold(
        title = title,
        state = editorState,
        onBack = { onNavigateBack?.invoke() ?: navController.popBackStack() },
        onSave = {
            viewModel.saveTransaction(
                onSuccess = {
                    // 保存成功后通过saveSuccessEvent自动触发返回
                }
            )
        }
    ) {
        // 使用新的状态分离模式传递参数
        TransactionEditorContent(
            formState = formState,
            editorState = editorState,
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * 交易编辑器内容组件
 *
 * 使用新的状态分离模式，直接访问formState和editorState
 * 避免通过兼容层uiState访问数据，提升性能和可维护性
 */
@Composable
private fun TransactionEditorContent(
    formState: TransactionFormState,
    editorState: EntityEditorState,
    viewModel: AddTransactionViewModel,
    modifier: Modifier = Modifier
) {
    // 完整实现交易编辑器内容，直接使用formState的字段
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 交易类型选择（收入/支出/转账）
        TransactionTypeSelector(
            selectedType = formState.transactionType,
            onTypeSelected = viewModel::setTransactionType
        )

        // 金额输入区域
        AmountInputSection(
            amountText = formState.amountText,
            evaluatedAmount = formState.evaluatedAmount,
            amountError = formState.amountError,
            onAmountChanged = viewModel::updateAmount
        )

        // 分类选择（转账模式不显示）
        if (formState.transactionType != TransactionType.TRANSFER) {
            CategorySelectionSection(
                selectedCategoryInfo = formState.selectedCategoryInfo,
                onCategoryClick = { viewModel.showCategoryPicker() }
            )
        }

        // 账户选择（普通交易）或转账账户选择
        if (formState.transactionType == TransactionType.TRANSFER) {
            TransferAccountSection(
                fromAccount = formState.fromAccount,
                toAccount = formState.toAccount,
                onFromAccountClick = { viewModel.showFromAccountPicker() },
                onToAccountClick = { viewModel.showToAccountPicker() }
            )
        } else {
            AccountSelectionSection(
                selectedAccount = formState.selectedAccount,
                onAccountClick = { viewModel.showAccountPicker() }
            )
        }

        // 日期时间选择
        DateTimeSection(
            selectedDate = formState.selectedDate,
            selectedTime = formState.selectedTime,
            enableTimeRecording = formState.enableTimeRecording,
            onDateTimeClick = { viewModel.showDateTimePicker() }
        )

        // 备注输入
        NoteInputSection(
            note = formState.note,
            onNoteChanged = viewModel::updateNote
        )

        // 账本选择
        LedgerSelectionSection(
            selectedLedger = formState.selectedLedger,
            onLedgerClick = { viewModel.showLedgerSelector() }
        )

        // 联动目标选择（如果有）
        if (formState.hasLinkOptions) {
            LinkTargetsSection(
                selectedTargets = formState.selectedSyncTargets,
                availableTargets = formState.availableLinkTargets,
                onTargetsChanged = { targetId -> viewModel.toggleSyncTarget(targetId) }
            )
        }
    }

    // 显示账户选择对话框
    if (formState.showAccountPicker) {
        com.ccxiaoji.feature.ledger.presentation.component.AccountPickerDialog(
            title = "选择账户",
            accounts = formState.accounts,
            selectedAccount = formState.selectedAccount,
            onAccountSelected = viewModel::selectAccount,
            onDismiss = { viewModel.hideAccountPicker() }
        )
    }
}


