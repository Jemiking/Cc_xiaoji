package com.ccxiaoji.feature.ledger.presentation.quickadd

import android.os.Bundle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.usecase.AddTransactionUseCase
import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickLedgerUiState(
    val amountCents: Int = 0,
    val direction: String = "EXPENSE",
    val merchant: String? = null,
    val accountId: String? = null,
    val categoryId: String? = null,
    val note: String? = null,
    val accounts: List<AccountOption> = emptyList(),
    val categories: List<CategoryOption> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class QuickLedgerViewModel @Inject constructor(
    private val addTransaction: AddTransactionUseCase,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val userApi: UserApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuickLedgerUiState())
    val uiState: StateFlow<QuickLedgerUiState> = _uiState

    fun initFromIntent(extras: Bundle?) {
        val amount = extras?.getInt("pn_amount_cents") ?: 0
        val direction = extras?.getString("pn_direction") ?: "EXPENSE"
        val merchant = extras?.getString("pn_merchant")
        val recAccount = extras?.getString("rec_account_id")
        val recCategory = extras?.getString("rec_category_id")
        val note = extras?.getString("rec_note") ?: merchant?.let { "自动解析: $it" }

        _uiState.value = QuickLedgerUiState(
            amountCents = amount,
            direction = direction,
            merchant = merchant,
            accountId = recAccount,
            categoryId = recCategory,
            note = note
        )

        // 若无推荐，异步获取默认账户与类别
        viewModelScope.launch {
            ensureDefaults(direction)
            subscribeOptions(direction)
        }
    }

    private suspend fun ensureDefaults(direction: String) {
        val userId = userApi.getCurrentUserId()
        val current = _uiState.value
        val accountId = current.accountId ?: run {
            val def = accountDao.getDefaultAccount(userId) ?: accountDao.getAccountsByUserSync(userId).firstOrNull()
            def?.id
        }
        val type = if (direction == "INCOME") "INCOME" else "EXPENSE"
        val categoryId = current.categoryId ?: run {
            val cats = categoryDao.getCategoriesByTypeWithLevels(userId, type)
            cats.firstOrNull()?.id
        }
        _uiState.value = current.copy(accountId = accountId, categoryId = categoryId)
    }

    private fun subscribeOptions(direction: String) {
        val userId = userApi.getCurrentUserId()
        // 账户列表
        viewModelScope.launch {
            accountDao.getAccountsByUser(userId).collect { list ->
                val opts = list.map { AccountOption(it.id, it.name ?: it.id) }
                _uiState.value = _uiState.value.copy(accounts = opts)
            }
        }
        // 分类列表（按方向）
        val type = if (direction == "INCOME") "INCOME" else "EXPENSE"
        viewModelScope.launch {
            categoryDao.getCategoriesByType(userId, type).collect { list ->
                val opts = list.map { CategoryOption(it.id, it.name) }
                _uiState.value = _uiState.value.copy(categories = opts)
            }
        }
    }

    fun selectAccount(accountId: String) { _uiState.value = _uiState.value.copy(accountId = accountId) }
    fun selectCategory(categoryId: String) { _uiState.value = _uiState.value.copy(categoryId = categoryId) }
    fun updateNote(note: String) { _uiState.value = _uiState.value.copy(note = note) }

    fun confirm(state: QuickLedgerUiState, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                if (state.amountCents <= 0) {
                    _uiState.value = state.copy(error = "金额必须大于0")
                    return@launch
                }
                val categoryId = state.categoryId ?: run {
                    _uiState.value = state.copy(error = "请选择分类")
                    return@launch
                }
                val accountId = state.accountId ?: run {
                    _uiState.value = state.copy(error = "请选择账户")
                    return@launch
                }
                _uiState.value = state.copy(loading = true, error = null)
                val id = addTransaction(
                    amountCents = state.amountCents,
                    categoryId = categoryId,
                    note = state.note,
                    accountId = accountId,
                    ledgerId = null
                )
                _uiState.value = state.copy(loading = false)
                onDone()
            } catch (e: Exception) {
                _uiState.value = state.copy(loading = false, error = e.message ?: "保存失败")
            }
        }
    }
}

data class AccountOption(val id: String, val name: String)
data class CategoryOption(val id: String, val name: String)
