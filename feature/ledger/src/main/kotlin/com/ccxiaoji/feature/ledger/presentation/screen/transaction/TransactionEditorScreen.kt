package com.ccxiaoji.feature.ledger.presentation.screen.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import com.ccxiaoji.feature.ledger.presentation.component.EntityEditorScaffold
import com.ccxiaoji.feature.ledger.presentation.component.EntityEditorState
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
 * 编辑模式枚举
 */
enum class EditorMode {
    ADD,    // 新增模式
    EDIT    // 编辑模式
}

/**
 * 交易编辑器配置
 */
data class TransactionEditorConfig(
    val mode: EditorMode = EditorMode.ADD,
    val allowedTypes: Set<TransactionType> = setOf(
        TransactionType.EXPENSE,
        TransactionType.INCOME,
        TransactionType.TRANSFER
    ),
    val amountInputMode: AmountInputMode = AmountInputMode.EXPRESSION,
    val showTransferSection: Boolean = true,
    val enableLinkTargets: Boolean = true,
    val gridColumns: Int = 6
)

/**
 * 金额输入模式
 */
enum class AmountInputMode {
    EXPRESSION,  // 支持表达式（如 100+50）
    DECIMAL      // 仅数字输入
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
                onAccountClick = { /* TODO: 实现账户选择 */ }
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
}

/**
 * 交易类型选择器组件
 */
@Composable
private fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    // TODO: 实现交易类型选择器UI
    // 这里可以使用Tab或SegmentedButton
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(
            TransactionType.EXPENSE,
            TransactionType.INCOME,
            TransactionType.TRANSFER
        ).forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        when (type) {
                            TransactionType.EXPENSE -> "支出"
                            TransactionType.INCOME -> "收入"
                            TransactionType.TRANSFER -> "转账"
                            else -> ""  // 添加else分支确保exhaustive
                        }
                    )
                }
            )
        }
    }
}

/**
 * 金额输入区域组件
 */
@Composable
private fun AmountInputSection(
    amountText: String,
    evaluatedAmount: Double?,
    amountError: String?,
    onAmountChanged: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = amountText,
            onValueChange = onAmountChanged,
            label = { Text("金额") },
            isError = amountError != null,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                if (amountError != null) {
                    Text(amountError)
                } else if (evaluatedAmount != null && amountText.contains(Regex("[+\\-*/]"))) {
                    Text("= $evaluatedAmount")
                }
            }
        )
    }
}

/**
 * 分类选择区域组件
 */
@Composable
private fun CategorySelectionSection(
    selectedCategoryInfo: SelectedCategoryInfo?,
    onCategoryClick: () -> Unit
) {
    Card(
        onClick = onCategoryClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedCategoryInfo?.let {
                    if (it.parentName != null) {
                        "${it.parentName} / ${it.categoryName}"
                    } else {
                        it.categoryName
                    }
                } ?: "选择分类",
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null
            )
        }
    }
}

/**
 * 账户选择区域组件
 */
@Composable
private fun AccountSelectionSection(
    selectedAccount: Account?,
    onAccountClick: () -> Unit
) {
    Card(
        onClick = onAccountClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedAccount?.name ?: "选择账户",
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null
            )
        }
    }
}

/**
 * 转账账户选择区域组件
 */
@Composable
private fun TransferAccountSection(
    fromAccount: Account?,
    toAccount: Account?,
    onFromAccountClick: () -> Unit,
    onToAccountClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(
            onClick = onFromAccountClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("从: ${fromAccount?.name ?: "选择转出账户"}")
                Icon(Icons.Default.ArrowForward, null)
            }
        }

        Card(
            onClick = onToAccountClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("到: ${toAccount?.name ?: "选择转入账户"}")
                Icon(Icons.Default.ArrowForward, null)
            }
        }
    }
}

/**
 * 日期时间选择区域组件
 */
@Composable
private fun DateTimeSection(
    selectedDate: kotlinx.datetime.LocalDate,
    selectedTime: kotlinx.datetime.LocalTime,
    enableTimeRecording: Boolean,
    onDateTimeClick: () -> Unit
) {
    Card(
        onClick = onDateTimeClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dateTimeText = if (enableTimeRecording) {
                "$selectedDate $selectedTime"
            } else {
                selectedDate.toString()
            }
            Text(
                text = dateTimeText,
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null
            )
        }
    }
}

/**
 * 备注输入区域组件
 */
@Composable
private fun NoteInputSection(
    note: String,
    onNoteChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChanged,
        label = { Text("备注") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 4
    )
}

/**
 * 账本选择区域组件
 */
@Composable
private fun LedgerSelectionSection(
    selectedLedger: Ledger?,
    onLedgerClick: () -> Unit
) {
    Card(
        onClick = onLedgerClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedLedger?.name ?: "选择账本",
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null
            )
        }
    }
}

/**
 * 联动目标选择区域组件
 */
@Composable
private fun LinkTargetsSection(
    selectedTargets: Set<String>,
    availableTargets: List<Ledger>,
    onTargetsChanged: (String) -> Unit  // 改为接受单个targetId
) {
    Column {
        Text(
            "同步到其他账本",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        availableTargets.forEach { target ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onTargetsChanged(target.id)  // 直接传递targetId
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(target.name)
                Checkbox(
                    checked = target.id in selectedTargets,
                    onCheckedChange = null // 由Row的clickable处理
                )
            }
        }
    }
}

