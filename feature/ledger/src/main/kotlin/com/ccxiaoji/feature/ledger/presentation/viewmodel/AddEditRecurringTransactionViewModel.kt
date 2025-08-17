package com.ccxiaoji.feature.ledger.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.model.RecurringFrequency
import com.ccxiaoji.feature.ledger.data.repository.RecurringTransactionRepository
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.shared.user.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditRecurringTransactionUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val amount: String = "",
    val accountId: String = "",
    val categoryId: String = "",
    val note: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val dayOfWeek: Int? = null,
    val dayOfMonth: Int? = 1,
    val monthOfYear: Int? = 1,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val nameError: String? = null,
    val amountError: String? = null,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    // 账户选择相关状态
    val availableAccounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val accountError: String? = null,
    // 分类选择相关状态
    val categoryGroups: List<CategoryGroup> = emptyList(),
    val selectedCategory: Category? = null,
    val showCategoryPicker: Boolean = false,
    val categoryError: String? = null
)

@HiltViewModel
class AddEditRecurringTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val userApi: UserApi
) : ViewModel() {
    
    companion object {
        private const val TAG = "AddEditRecurringTransactionViewModel"
    }
    
    private val recurringId: String? = savedStateHandle.get<String>("recurringId")
    
    private val _uiState = MutableStateFlow(AddEditRecurringTransactionUiState())
    val uiState: StateFlow<AddEditRecurringTransactionUiState> = _uiState.asStateFlow()
    
    init {
        Log.d(TAG, "ViewModel初始化，recurringId: $recurringId")
        try {
            // 并行加载所需数据
            viewModelScope.launch {
                launch { loadAccounts() }
                launch { loadCategories() }
            }
            
            if (recurringId != null) {
                Log.d(TAG, "编辑模式，加载定期交易数据")
                loadRecurringTransaction(recurringId)
            } else {
                Log.d(TAG, "新增模式")
            }
        } catch (e: Exception) {
            Log.e(TAG, "ViewModel初始化异常", e)
            _uiState.update { it.copy(errorMessage = "初始化失败: ${e.message}") }
        }
    }
    
    private fun loadRecurringTransaction(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val transaction = recurringTransactionRepository.getRecurringTransactionById(id)
                if (transaction != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = transaction.name,
                            amount = (transaction.amountCents / 100.0).toString(),
                            accountId = transaction.accountId,
                            categoryId = transaction.categoryId,
                            note = transaction.note ?: "",
                            frequency = transaction.frequency,
                            dayOfWeek = transaction.dayOfWeek,
                            dayOfMonth = transaction.dayOfMonth,
                            monthOfYear = transaction.monthOfYear,
                            startDate = transaction.startDate,
                            endDate = transaction.endDate
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "定期交易不存在"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载失败：${e.message}"
                    )
                }
            }
        }
    }
    
    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = null
            )
        }
    }
    
    fun updateAmount(amount: String) {
        val filtered = amount.filter { char -> char.isDigit() || char == '.' }
        _uiState.update {
            it.copy(
                amount = filtered,
                amountError = null
            )
        }
    }
    
    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }
    
    fun updateFrequency(frequency: RecurringFrequency) {
        _uiState.update { it.copy(frequency = frequency) }
    }
    
    fun updateDayOfWeek(dayOfWeek: Int?) {
        _uiState.update { it.copy(dayOfWeek = dayOfWeek) }
    }
    
    fun updateDayOfMonth(dayOfMonth: Int?) {
        _uiState.update { it.copy(dayOfMonth = dayOfMonth) }
    }
    
    fun updateMonthOfYear(monthOfYear: Int?) {
        _uiState.update { it.copy(monthOfYear = monthOfYear) }
    }
    
    // 账户选择相关方法
    private suspend fun loadAccounts() {
        try {
            Log.d(TAG, "开始加载账户列表")
            accountRepository.getAccounts().collect { accounts ->
                Log.d(TAG, "获取到${accounts.size}个账户")
                _uiState.update { 
                    it.copy(
                        availableAccounts = accounts,
                        selectedAccount = accounts.find { acc -> acc.isDefault }
                    )
                }
                
                // 自动选择默认账户的ID
                val defaultAccount = accounts.find { it.isDefault }
                if (defaultAccount != null && _uiState.value.accountId.isBlank()) {
                    Log.d(TAG, "自动选择默认账户: ${defaultAccount.name}")
                    updateAccountId(defaultAccount.id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载账户列表失败", e)
            _uiState.update { it.copy(accountError = "加载账户失败: ${e.message}") }
        }
    }
    
    private suspend fun loadCategories() {
        try {
            Log.d(TAG, "开始加载分类列表")
            val currentUser = userApi.getCurrentUser()
            val userId = currentUser?.id ?: "default_user"
            
            // 同时加载收入和支出分类
            val incomeGroups = categoryRepository.getCategoryTree(userId, "income")
            val expenseGroups = categoryRepository.getCategoryTree(userId, "expense")
            val allGroups = incomeGroups + expenseGroups
            
            Log.d(TAG, "获取到${allGroups.size}个分类组")
            _uiState.update { it.copy(categoryGroups = allGroups) }
        } catch (e: Exception) {
            Log.e(TAG, "加载分类列表失败", e)
            _uiState.update { it.copy(categoryError = "加载分类失败: ${e.message}") }
        }
    }
    
    fun selectAccount(account: Account) {
        Log.d(TAG, "选择账户: ${account.name}")
        _uiState.update { 
            it.copy(
                selectedAccount = account,
                accountId = account.id,
                accountError = null
            )
        }
    }
    
    fun selectCategory(category: Category) {
        Log.d(TAG, "选择分类: ${category.name}")
        _uiState.update { 
            it.copy(
                selectedCategory = category,
                categoryId = category.id,
                showCategoryPicker = false,
                categoryError = null
            )
        }
    }
    
    fun showCategoryPicker() {
        _uiState.update { it.copy(showCategoryPicker = true) }
    }
    
    fun hideCategoryPicker() {
        _uiState.update { it.copy(showCategoryPicker = false) }
    }
    
    private fun updateAccountId(accountId: String) {
        _uiState.update { it.copy(accountId = accountId) }
    }
    
    fun saveRecurringTransaction() {
        Log.d(TAG, "开始保存定期交易")
        viewModelScope.launch {
            // 验证输入
            var hasError = false
            
            Log.d(TAG, "验证输入 - 名称: '${_uiState.value.name}', 金额: '${_uiState.value.amount}'")
            
            if (_uiState.value.name.isBlank()) {
                Log.e(TAG, "名称为空")
                _uiState.update { it.copy(nameError = "请输入名称") }
                hasError = true
            }
            
            val amountValue = _uiState.value.amount.toDoubleOrNull()
            if (amountValue == null || amountValue <= 0) {
                Log.e(TAG, "金额无效: ${_uiState.value.amount}")
                _uiState.update { it.copy(amountError = "请输入有效金额") }
                hasError = true
            }
            
            if (hasError) {
                Log.e(TAG, "输入验证失败，停止保存")
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            
            Log.d(TAG, "输入验证通过，开始保存")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val currentUser = userApi.getCurrentUser()
                val userId = currentUser?.id ?: "default_user"
                val amountCents = (amountValue!! * 100).toInt()
                
                Log.d(TAG, "用户ID: $userId, 金额(分): $amountCents")
                
                // 验证账户ID和分类ID
                val accountId = _uiState.value.accountId
                val categoryId = _uiState.value.categoryId
                
                if (accountId.isBlank()) {
                    Log.e(TAG, "账户ID为空")
                    _uiState.update { it.copy(accountError = "请选择账户") }
                    hasError = true
                }
                
                if (categoryId.isBlank()) {
                    Log.e(TAG, "分类ID为空")
                    _uiState.update { it.copy(categoryError = "请选择分类") }
                    hasError = true
                }
                
                Log.d(TAG, "验证通过 - 账户ID: $accountId, 分类ID: $categoryId")
                
                if (recurringId == null) {
                    Log.d(TAG, "创建新的定期交易")
                    // 创建新的定期交易
                    recurringTransactionRepository.createRecurringTransaction(
                        name = _uiState.value.name,
                        accountId = accountId,
                        amountCents = amountCents,
                        categoryId = categoryId,
                        note = _uiState.value.note.ifBlank { null },
                        frequency = _uiState.value.frequency,
                        startDate = _uiState.value.startDate,
                        endDate = _uiState.value.endDate,
                        dayOfWeek = _uiState.value.dayOfWeek,
                        dayOfMonth = _uiState.value.dayOfMonth,
                        monthOfYear = _uiState.value.monthOfYear
                    )
                    Log.d(TAG, "创建定期交易成功")
                } else {
                    Log.d(TAG, "更新现有的定期交易，ID: $recurringId")
                    // 更新现有的定期交易
                    recurringTransactionRepository.updateRecurringTransaction(
                        id = recurringId,
                        name = _uiState.value.name,
                        accountId = accountId,
                        amountCents = amountCents,
                        categoryId = categoryId,
                        note = _uiState.value.note.ifBlank { null },
                        frequency = _uiState.value.frequency,
                        startDate = _uiState.value.startDate,
                        endDate = _uiState.value.endDate,
                        dayOfWeek = _uiState.value.dayOfWeek,
                        dayOfMonth = _uiState.value.dayOfMonth,
                        monthOfYear = _uiState.value.monthOfYear
                    )
                    Log.d(TAG, "更新定期交易成功")
                }
                
                Log.d(TAG, "保存操作完成，更新UI状态为成功")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        saveSuccess = true
                    )
                }
                Log.d(TAG, "定期交易保存成功")
            } catch (e: Exception) {
                Log.e(TAG, "保存定期交易时发生异常", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "保存失败：${e.message}"
                    )
                }
                Log.e(TAG, "定期交易保存失败: ${e.message}")
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}