package com.ccxiaoji.feature.ledger.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.presentation.component.EntityEditorState
import com.ccxiaoji.feature.ledger.presentation.component.rememberEntityEditorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

/**
 * 统一的账户编辑器ViewModel
 *
 * 整合新增和编辑功能，使用EntityEditorState管理元状态，
 * 避免UiState与EntityEditorState字段重复问题
 */
@HiltViewModel
class AccountEditorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AccountEditorViewModel"
        const val ARG_ACCOUNT_ID = "accountId"
    }

    /**
     * 账户表单状态（仅包含可编辑字段）
     */
    data class AccountFormState(
        val name: String = "",
        val selectedType: AccountType = AccountType.BANK,
        val balance: String = "",
        val nameError: String? = null,
        val balanceError: String? = null,

        // 信用卡专用字段
        val creditLimit: String = "",
        val billingDay: String = "",
        val paymentDueDay: String = "",
        val creditLimitError: String? = null,
        val billingDayError: String? = null,
        val paymentDueDayError: String? = null
    )

    // 账户ID（null表示新增模式）
    private val accountId: String? = savedStateHandle.get<String>(ARG_ACCOUNT_ID)

    // 是否为编辑模式
    val isEditMode: Boolean = accountId != null

    // 原始账户数据（用于编辑模式）
    private var originalAccount: Account? = null

    // 表单状态
    private val _formState = MutableStateFlow(AccountFormState())
    val formState: StateFlow<AccountFormState> = _formState.asStateFlow()

    // 编辑器元状态
    private val _editorState = EntityEditorState(
        isEditMode = isEditMode,
        isLoading = isEditMode, // 编辑模式需要加载数据
        isSaving = false,
        hasUnsavedChanges = false,
        saveEnabled = false
    )
    val editorState: EntityEditorState = _editorState

    // 操作成功事件
    private val _savedEvent = MutableSharedFlow<Unit>()
    val savedEvent: SharedFlow<Unit> = _savedEvent.asSharedFlow()

    // 初始表单状态（用于脏检查）
    private var initialFormState: AccountFormState = AccountFormState()

    init {
        if (isEditMode && accountId != null) {
            loadAccount(accountId)
        }

        // 监听表单变化，更新编辑器状态
        viewModelScope.launch {
            formState.collect { form ->
                updateEditorState(form)
            }
        }
    }

    /**
     * 加载账户数据（编辑模式）
     */
    private fun loadAccount(accountId: String) {
        Log.d(TAG, "开始加载账户，ID: $accountId")
        viewModelScope.launch {
            _editorState.isLoading = true

            try {
                val account = accountRepository.getAccountById(accountId)
                if (account != null) {
                    Log.d(TAG, "成功获取账户: ${account.name}")
                    originalAccount = account

                    val formState = AccountFormState(
                        name = account.name,
                        selectedType = account.type,
                        balance = account.balanceYuan.toString(),
                        // 信用卡字段处理（如果有）
                        creditLimit = if (account.type == AccountType.CREDIT_CARD) {
                            // TODO: 从account中获取信用额度
                            ""
                        } else "",
                        billingDay = if (account.type == AccountType.CREDIT_CARD) {
                            // TODO: 从account中获取账单日
                            ""
                        } else "",
                        paymentDueDay = if (account.type == AccountType.CREDIT_CARD) {
                            // TODO: 从account中获取还款日
                            ""
                        } else ""
                    )

                    initialFormState = formState
                    _formState.value = formState
                    _editorState.isLoading = false

                } else {
                    Log.e(TAG, "未找到账户，ID: $accountId")
                    _editorState.isLoading = false
                    _editorState.showError("未找到指定账户")
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载账户时异常", e)
                _editorState.isLoading = false
                viewModelScope.launch {
                    _editorState.showError("加载账户失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 更新编辑器状态（基于表单状态）
     */
    private fun updateEditorState(form: AccountFormState) {
        val hasErrors = form.nameError != null ||
                       form.balanceError != null ||
                       (form.selectedType == AccountType.CREDIT_CARD && (
                           form.creditLimitError != null ||
                           form.billingDayError != null ||
                           form.paymentDueDayError != null
                       ))

        val isNameValid = form.name.isNotBlank()
        val isBalanceValid = form.balance.isEmpty() || form.balance.toDoubleOrNull() != null
        val isCreditCardValid = if (form.selectedType == AccountType.CREDIT_CARD) {
            form.creditLimit.isNotEmpty() && form.creditLimit.toDoubleOrNull() != null &&
            form.billingDay.isNotEmpty() && form.billingDay.toIntOrNull()?.let { it in 1..28 } == true &&
            form.paymentDueDay.isNotEmpty() && form.paymentDueDay.toIntOrNull()?.let { it in 1..28 } == true
        } else true

        val canSave = isNameValid && isBalanceValid && isCreditCardValid && !hasErrors
        val hasChanges = form != initialFormState

        _editorState.hasUnsavedChanges = hasChanges
        _editorState.saveEnabled = canSave
    }

    /**
     * 更新账户名称
     */
    fun updateName(name: String) {
        val error = when {
            name.isEmpty() -> null
            name.isBlank() -> "账户名称不能为空"
            name.length > 50 -> "账户名称不能超过50个字符"
            else -> null
        }

        _formState.update {
            it.copy(
                name = name,
                nameError = error
            )
        }
    }

    /**
     * 选择账户类型
     */
    fun selectType(type: AccountType) {
        _formState.update {
            it.copy(
                selectedType = type,
                // 切换到非信用卡类型时清空信用卡字段
                creditLimit = if (type != AccountType.CREDIT_CARD) "" else it.creditLimit,
                billingDay = if (type != AccountType.CREDIT_CARD) "" else it.billingDay,
                paymentDueDay = if (type != AccountType.CREDIT_CARD) "" else it.paymentDueDay,
                creditLimitError = if (type != AccountType.CREDIT_CARD) null else it.creditLimitError,
                billingDayError = if (type != AccountType.CREDIT_CARD) null else it.billingDayError,
                paymentDueDayError = if (type != AccountType.CREDIT_CARD) null else it.paymentDueDayError
            )
        }
    }

    /**
     * 更新余额
     */
    fun updateBalance(balance: String) {
        val filteredBalance = balance.filter { char ->
            char.isDigit() || char == '.' || char == '-'
        }

        val error = when {
            filteredBalance.isEmpty() -> null
            filteredBalance.toDoubleOrNull() == null -> "请输入有效金额"
            else -> null
        }

        _formState.update {
            it.copy(
                balance = filteredBalance,
                balanceError = error
            )
        }
    }

    /**
     * 更新信用额度
     */
    fun updateCreditLimit(limit: String) {
        val filteredLimit = limit.filter { char ->
            char.isDigit() || char == '.'
        }

        val error = when {
            filteredLimit.isEmpty() && _formState.value.selectedType == AccountType.CREDIT_CARD -> "请输入信用额度"
            filteredLimit.isNotEmpty() && filteredLimit.toDoubleOrNull() == null -> "请输入有效金额"
            filteredLimit.toDoubleOrNull()?.let { it <= 0 } == true -> "信用额度必须大于0"
            else -> null
        }

        _formState.update {
            it.copy(
                creditLimit = filteredLimit,
                creditLimitError = error
            )
        }
    }

    /**
     * 更新账单日
     */
    fun updateBillingDay(day: String) {
        val filteredDay = day.filter { it.isDigit() }

        val error = when {
            filteredDay.isEmpty() && _formState.value.selectedType == AccountType.CREDIT_CARD -> "请输入账单日"
            filteredDay.toIntOrNull()?.let { it < 1 || it > 28 } == true -> "账单日必须在1-28之间"
            else -> null
        }

        _formState.update {
            it.copy(
                billingDay = filteredDay,
                billingDayError = error
            )
        }
    }

    /**
     * 更新还款日
     */
    fun updatePaymentDueDay(day: String) {
        val filteredDay = day.filter { it.isDigit() }

        val error = when {
            filteredDay.isEmpty() && _formState.value.selectedType == AccountType.CREDIT_CARD -> "请输入还款日"
            filteredDay.toIntOrNull()?.let { it < 1 || it > 28 } == true -> "还款日必须在1-28之间"
            else -> null
        }

        _formState.update {
            it.copy(
                paymentDueDay = filteredDay,
                paymentDueDayError = error
            )
        }
    }

    /**
     * 保存账户
     */
    fun saveAccount() {
        val form = _formState.value

        // 验证必填字段
        if (form.name.isBlank()) {
            _formState.update { it.copy(nameError = "账户名称不能为空") }
            return
        }

        val balanceValue = if (form.balance.isNotEmpty()) {
            form.balance.toDoubleOrNull() ?: run {
                _formState.update { it.copy(balanceError = "请输入有效的金额") }
                return
            }
        } else 0.0

        // 信用卡字段验证
        if (form.selectedType == AccountType.CREDIT_CARD) {
            if (form.creditLimit.isEmpty() || form.creditLimit.toDoubleOrNull() == null) {
                _formState.update { it.copy(creditLimitError = "请输入有效的信用额度") }
                return
            }
            if (form.billingDay.isEmpty() || form.billingDay.toIntOrNull()?.let { it !in 1..28 } != false) {
                _formState.update { it.copy(billingDayError = "请输入有效的账单日(1-28)") }
                return
            }
            if (form.paymentDueDay.isEmpty() || form.paymentDueDay.toIntOrNull()?.let { it !in 1..28 } != false) {
                _formState.update { it.copy(paymentDueDayError = "请输入有效的还款日(1-28)") }
                return
            }
        }

        viewModelScope.launch {
            _editorState.isSaving = true

            try {
                if (isEditMode && originalAccount != null) {
                    // 编辑模式：更新账户
                    val updatedAccount = originalAccount!!.copy(
                        name = form.name.trim(),
                        type = form.selectedType,
                        balanceCents = (balanceValue * 100).toLong(),
                        updatedAt = Clock.System.now()
                    )

                    accountRepository.updateAccount(updatedAccount)
                    Log.d(TAG, "成功更新账户: ${updatedAccount.name}")
                } else {
                    // 新增模式：创建账户
                    val creditLimitCents = if (form.selectedType == AccountType.CREDIT_CARD) {
                        form.creditLimit.toDoubleOrNull()?.let { (it * 100).toLong() }
                    } else null

                    val billingDayInt = if (form.selectedType == AccountType.CREDIT_CARD) {
                        form.billingDay.toIntOrNull()
                    } else null

                    val paymentDueDayInt = if (form.selectedType == AccountType.CREDIT_CARD) {
                        form.paymentDueDay.toIntOrNull()
                    } else null

                    accountRepository.createAccount(
                        name = form.name.trim(),
                        type = form.selectedType,
                        initialBalanceCents = (balanceValue * 100).toLong(),
                        creditLimitCents = creditLimitCents,
                        billingDay = billingDayInt,
                        paymentDueDay = paymentDueDayInt
                    )
                    Log.d(TAG, "成功创建账户: ${form.name.trim()}")
                }

                _editorState.isSaving = false
                _savedEvent.emit(Unit)

            } catch (e: Exception) {
                Log.e(TAG, "保存账户失败", e)
                _editorState.isSaving = false
                viewModelScope.launch {
                    _editorState.showError("保存失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 删除账户（仅编辑模式可用）
     */
    fun deleteAccount() {
        if (!isEditMode || originalAccount == null) return

        viewModelScope.launch {
            _editorState.isSaving = true

            try {
                accountRepository.deleteAccount(originalAccount!!.id)
                Log.d(TAG, "成功删除账户: ${originalAccount!!.name}")

                _editorState.isSaving = false
                _savedEvent.emit(Unit)

            } catch (e: Exception) {
                Log.e(TAG, "删除账户失败", e)
                _editorState.isSaving = false
                viewModelScope.launch {
                    _editorState.showError("删除失败: ${e.message}")
                }
            }
        }
    }
}