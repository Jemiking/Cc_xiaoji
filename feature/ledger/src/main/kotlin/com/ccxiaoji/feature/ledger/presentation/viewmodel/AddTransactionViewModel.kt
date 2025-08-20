package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.usecase.GetCategoryTreeUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.GetFrequentCategoriesUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.ManageCategoryUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
import com.ccxiaoji.shared.user.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

data class AddTransactionUiState(
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val ledgers: List<Ledger> = emptyList(),
    val selectedLedger: Ledger? = null,
    val categoryGroups: List<CategoryGroup> = emptyList(),
    val frequentCategories: List<Category> = emptyList(),
    val selectedCategoryInfo: SelectedCategoryInfo? = null,
    val isIncome: Boolean = false,
    val amountText: String = "",
    val note: String = "",
    val selectedDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val selectedTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,
    val selectedLocation: com.ccxiaoji.feature.ledger.domain.model.LocationData? = null,
    val isLoading: Boolean = false,
    val amountError: String? = null,
    val canSave: Boolean = false,
    val showCategoryPicker: Boolean = false,
    val showLedgerSelector: Boolean = false
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val getCategoryTree: GetCategoryTreeUseCase,
    private val getFrequentCategories: GetFrequentCategoriesUseCase,
    private val manageCategory: ManageCategoryUseCase,
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val userApi: UserApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()
    
    private val currentUserId = userApi.getCurrentUserId()
    private val preselectedAccountId: String? = savedStateHandle["accountId"]
    
    init {
        checkAndInitializeCategories()
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // 加载账户
            accountRepository.getAccounts().collect { accounts ->
                val selectedAccount = if (preselectedAccountId != null) {
                    accounts.find { it.id == preselectedAccountId }
                } else {
                    accounts.firstOrNull()
                }
                
                _uiState.update {
                    it.copy(
                        accounts = accounts,
                        selectedAccount = selectedAccount
                    )
                }
            }
        }
        
        viewModelScope.launch {
            // 加载记账簿
            manageLedgerUseCase.getUserLedgers(currentUserId).collect { ledgers ->
                val defaultLedger = ledgers.find { it.isDefault } ?: ledgers.firstOrNull()
                
                _uiState.update {
                    it.copy(
                        ledgers = ledgers,
                        selectedLedger = defaultLedger
                    )
                }
                updateCanSave()
            }
        }
        
        viewModelScope.launch {
            // 加载分类树和常用分类
            loadCategories()
        }
    }
    
    fun selectAccount(account: Account) {
        _uiState.update {
            it.copy(selectedAccount = account)
        }
        updateCanSave()
    }
    
    fun selectLedger(ledger: Ledger) {
        _uiState.update {
            it.copy(
                selectedLedger = ledger,
                showLedgerSelector = false
            )
        }
        updateCanSave()
    }
    
    fun showLedgerSelector() {
        _uiState.update {
            it.copy(showLedgerSelector = true)
        }
    }
    
    fun hideLedgerSelector() {
        _uiState.update {
            it.copy(showLedgerSelector = false)
        }
    }
    
    fun setIncomeType(isIncome: Boolean) {
        _uiState.update {
            it.copy(
                isIncome = isIncome,
                selectedCategoryInfo = null // 重置分类选择
            )
        }
        viewModelScope.launch {
            loadCategories()
        }
    }
    
    fun selectCategory(category: Category) {
        viewModelScope.launch {
            // 记录使用频率
            getFrequentCategories.recordCategoryUsage(category.id)
            
            // 获取完整的分类信息（包含父分类路径）
            val categoryInfo = categoryRepository.getCategoryFullInfo(category.id)
            
            _uiState.update {
                it.copy(
                    selectedCategoryInfo = categoryInfo,
                    showCategoryPicker = false
                )
            }
            updateCanSave()
        }
    }
    
    fun showCategoryPicker() {
        _uiState.update {
            it.copy(showCategoryPicker = true)
        }
    }
    
    fun hideCategoryPicker() {
        _uiState.update {
            it.copy(showCategoryPicker = false)
        }
    }
    
    fun updateAmount(amount: String) {
        val filteredAmount = amount.filter { it.isDigit() || it == '.' }
        val error = when {
            filteredAmount.isEmpty() -> null
            filteredAmount.toDoubleOrNull() == null -> "请输入有效金额"
            filteredAmount.toDouble() <= 0 -> "金额必须大于0"
            else -> null
        }
        
        _uiState.update {
            it.copy(
                amountText = filteredAmount,
                amountError = error
            )
        }
        updateCanSave()
    }
    
    fun updateNote(note: String) {
        _uiState.update {
            it.copy(note = note)
        }
    }
    
    fun updateDate(date: LocalDate) {
        _uiState.update {
            it.copy(selectedDate = date)
        }
    }
    
    fun updateTime(time: LocalTime) {
        _uiState.update {
            it.copy(selectedTime = time)
        }
    }
    
    fun updateLocation(location: com.ccxiaoji.feature.ledger.domain.model.LocationData?) {
        _uiState.update {
            it.copy(selectedLocation = location)
        }
    }
    
    private suspend fun loadCategories() {
        val userId = currentUserId
        val type = if (_uiState.value.isIncome) "INCOME" else "EXPENSE"
        
        // 加载分类树
        val categoryGroups = getCategoryTree(userId, type)
        
        // 加载常用分类
        val frequentCategories = getFrequentCategories(userId, type, 5)
        
        // 如果没有选中的分类，选择第一个常用分类或第一个可用分类
        val selectedInfo = _uiState.value.selectedCategoryInfo
        val newSelectedInfo = if (selectedInfo == null || selectedInfo.categoryId.isEmpty()) {
            // 优先选择常用分类
            frequentCategories.firstOrNull()?.let { category ->
                // 找到对应的完整信息
                categoryRepository.getCategoryFullInfo(category.id)
            } ?: 
            // 如果没有常用分类，选择第一个可用分类
            categoryGroups.firstOrNull()?.children?.firstOrNull()?.let { category ->
                categoryRepository.getCategoryFullInfo(category.id)
            }
        } else {
            selectedInfo
        }
        
        _uiState.update {
            it.copy(
                categoryGroups = categoryGroups,
                frequentCategories = frequentCategories,
                selectedCategoryInfo = newSelectedInfo
            )
        }
        updateCanSave()
    }
    
    private fun updateCanSave() {
        _uiState.update {
            it.copy(
                canSave = it.amountText.isNotEmpty() && 
                         it.amountError == null && 
                         it.amountText.toDoubleOrNull() != null &&
                         it.amountText.toDouble() > 0 &&
                         it.selectedCategoryInfo != null &&
                         it.selectedAccount != null &&
                         it.selectedLedger != null
            )
        }
    }
    
    fun saveTransaction(onSuccess: () -> Unit) {
        if (!_uiState.value.canSave) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val state = _uiState.value
                val amountCents = ((state.amountText.toDoubleOrNull() ?: 0.0) * 100).toInt()
                
                // 组合日期和时间为Instant
                val transactionDateTime = LocalDateTime(state.selectedDate, state.selectedTime)
                    .toInstant(TimeZone.currentSystemDefault())
                
                transactionRepository.addTransaction(
                    amountCents = amountCents,
                    categoryId = state.selectedCategoryInfo!!.categoryId,
                    note = state.note.ifBlank { null },
                    accountId = state.selectedAccount!!.id,
                    ledgerId = state.selectedLedger!!.id,
                    transactionDate = transactionDateTime,
                    location = state.selectedLocation
                )
                
                onSuccess()
            } catch (e: Exception) {
                // 处理错误
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private fun checkAndInitializeCategories() {
        viewModelScope.launch {
            try {
                // 检查并初始化默认分类
                manageCategory.checkAndInitializeDefaultCategories(currentUserId)
            } catch (e: Exception) {
                // 初始化失败不影响主流程，只记录日志
                android.util.Log.e("AddTransactionViewModel", "初始化默认分类失败", e)
            }
        }
    }
}