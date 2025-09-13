package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.model.LedgerLink
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.usecase.GetCategoryTreeUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.GetFrequentCategoriesUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.ManageCategoryUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerLinkUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.CreateLinkedTransactionUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.CreateTransferUseCase
import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
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
    // 转账相关状态
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val fromAccount: Account? = null,      // 转出账户
    val toAccount: Account? = null,        // 转入账户
    val amountText: String = "",
    val note: String = "",
    val selectedDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val selectedTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,
    val selectedLocation: com.ccxiaoji.feature.ledger.domain.model.LocationData? = null,
    val isLoading: Boolean = false,
    val amountError: String? = null,
    val canSave: Boolean = false,
    val showCategoryPicker: Boolean = false,
    val showLedgerSelector: Boolean = false,
    val showDateTimePicker: Boolean = false,
    val showFromAccountPicker: Boolean = false,     // 转出账户选择器
    val showToAccountPicker: Boolean = false,       // 转入账户选择器
    // 编辑模式相关状态
    val isEditMode: Boolean = false,
    val editingTransactionId: String? = null,
    // 联动功能相关状态
    val availableLinkTargets: List<Ledger> = emptyList(),
    val selectedSyncTargets: Set<String> = emptySet(),
    val showLinkTargetSelector: Boolean = false,
    val hasLinkOptions: Boolean = false,
    // 时间记录设置
    val enableTimeRecording: Boolean = false
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
    private val manageLedgerLinkUseCase: ManageLedgerLinkUseCase,
    private val createLinkedTransactionUseCase: CreateLinkedTransactionUseCase,
    private val createTransferUseCase: CreateTransferUseCase,
    private val userApi: UserApi,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()
    private val currentUserId = userApi.getCurrentUserId()
    private val preselectedAccountId: String? = savedStateHandle["accountId"]
    private val transactionId: String? = savedStateHandle["transactionId"]
    // 自动记账预填参数（DeepLink）
    // IntType 不支持可空：NavGraph 使用 Int.MIN_VALUE 作为哨兵，这里映射为 null
    private val prefillAmountCents: Int? = savedStateHandle.get<Int>("amountCents")?.takeIf { it != Int.MIN_VALUE }
    private val prefillDirection: String? = savedStateHandle["direction"]
    private val prefillMerchant: String? = savedStateHandle["merchant"]
    private val prefillCategoryId: String? = savedStateHandle["categoryId"]
    private val prefillNote: String? = savedStateHandle["note"]
    
    init {
        
        checkAndInitializeCategories()
        loadData()
        loadSettings()

        
        // 应用自动预填
        applyAutoLedgerPrefill()
        // 如果有 transactionId，则进入编辑模式并加载交易数据
        transactionId?.let { id ->
            loadTransactionForEdit(id)
        }
    }
    private fun applyAutoLedgerPrefill() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                // 方向优先（触发分类加权）
                prefillDirection?.let { dir ->
                    val type = if (dir.equals("INCOME", ignoreCase = true)) TransactionType.INCOME else TransactionType.EXPENSE
                    setTransactionType(type)
                }

                // 金额（分为单位的字符串）
                prefillAmountCents?.takeIf { it > 0 }?.let { cents ->
                    val yuan = cents.toDouble() / 100.0
                    updateAmount("" + (if (yuan % 1.0 == 0.0) yuan.toInt() else String.format(java.util.Locale.getDefault(),"%.2f", yuan)))
                }

                // 备注：仅在 DeepLink 明确提供 note 时设置，否则保持为空，由用户填写
                prefillNote?.takeIf { it.isNotBlank() }?.let { provided ->
                    updateNote(provided)
                }

                // 指定分类（可选）
                prefillCategoryId?.let { cid ->
                    try {
                        val info = categoryRepository.getCategoryFullInfo(cid)
                        if (info != null) {
                            _uiState.update { it.copy(selectedCategoryInfo = info) }
                        }
                    } catch (_: Exception) { }
                }

                // 最后校验一次保存条件
                updateCanSave()
            } catch (_: Exception) {
                // 忽略预填异常，保持页面可编辑
            }
        }
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
            // 加载账本
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
            // 加载分类树与常用分类
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
        // 当选择账本时，加载可用的联动目标
        loadLinkTargets(ledger.id)
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
    
    // 设置交易类型（新方法）
    fun setTransactionType(type: TransactionType) {
        _uiState.update {
            it.copy(
                transactionType = type,
                isIncome = type == TransactionType.INCOME,
                selectedCategoryInfo = if (type == TransactionType.TRANSFER) null else it.selectedCategoryInfo,
                // 转账模式下重置账户选择
                fromAccount = if (type == TransactionType.TRANSFER) it.selectedAccount else null,
                toAccount = if (type == TransactionType.TRANSFER) null else null
            )
        }
        // 转账模式不需要分类，其他模式需要重新加载分类
        if (type != TransactionType.TRANSFER) {
            viewModelScope.launch {
                loadCategories()
            }
        }
    }
    
    // 设置转出账户
    fun setFromAccount(account: Account) {
        _uiState.update {
            it.copy(
                fromAccount = account,
                showFromAccountPicker = false
            )
        }
        updateCanSave()
    }
    
    // 设置转入账户
    fun setToAccount(account: Account) {
        _uiState.update {
            it.copy(
                toAccount = account,
                showToAccountPicker = false
            )
        }
        updateCanSave()
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
    
    // 显示转出账户选择器
    fun showFromAccountPicker() {
        _uiState.update {
            it.copy(showFromAccountPicker = true)
        }
    }
    
    fun hideFromAccountPicker() {
        _uiState.update {
            it.copy(showFromAccountPicker = false)
        }
    }
    
    fun showToAccountPicker() {
        _uiState.update {
            it.copy(showToAccountPicker = true)
        }
    }
    
    fun hideToAccountPicker() {
        _uiState.update {
            it.copy(showToAccountPicker = false)
        }
    }
    
    fun showDateTimePicker() {
        _uiState.update {
            it.copy(showDateTimePicker = true)
        }
    }
    
    fun hideDateTimePicker() {
        _uiState.update {
            it.copy(showDateTimePicker = false)
        }
    }
    
    fun updateAmount(amount: String) {
        val filteredAmount = run {
            val cleaned = amount.filter { it.isDigit() || it == '.' }
            val parts = cleaned.split('.')
            val normalized = when {
                parts.size > 2 -> parts[0] + "." + parts[1]
                parts.size == 2 -> parts[0] + "." + parts[1].take(2)
                else -> cleaned
            }
            normalized.trimStart('.')
        }
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
        android.util.Log.d("AddTxn_DefaultSelection", "加载分类树 ${categoryGroups.size} 组，当前类型=$type")
        categoryGroups.forEachIndexed { idx, g ->
            android.util.Log.d(
                "AddTxn_DefaultSelection",
                "组$idx: parent='${g.parent.name}' (id=${g.parent.id}, children=${g.children.size}, order=${g.parent.displayOrder})"
            )
        }
        
        // 鍔犺浇甯哥敤鍒嗙被
        // 加载常用分类
        val frequentCategories = getFrequentCategories(userId, type, 5)
        android.util.Log.d("AddTxn_DefaultSelection", "常用分类数量=${frequentCategories.size}")
        
        val selectedInfo = _uiState.value.selectedCategoryInfo
        val newSelectedInfo = if (selectedInfo == null || selectedInfo.categoryId.isEmpty()) {
            // 优先从常用分类中选择（跳过“其他/未分类”等兜底项）
            var picked: SelectedCategoryInfo? = null
            for (c in frequentCategories) {
                val info = categoryRepository.getCategoryFullInfo(c.id)
                val parent = info?.parentName?.trim()
                val name = info?.categoryName?.trim()
                val isOtherBucket = parent != null && (parent.contains("其他") || parent.equals("Other", ignoreCase = true))
                val isFallbackName = name != null && (name.equals("Other", ignoreCase = true) || name.equals("Uncategorized", ignoreCase = true))
                if (isOtherBucket || isFallbackName) continue
                if (info != null) { picked = info; break }
            }
            if (picked != null) {
                android.util.Log.d("AddTxn_DefaultSelection", "默认选择=常用分类 ${picked.categoryName} (${picked.categoryId})")
                picked
            } else {
                // 没有常用分类，回退到分类树
                val groupWithChildren = categoryGroups.firstOrNull { it.children.isNotEmpty() }
                if (groupWithChildren != null) {
                    val firstChild = groupWithChildren.children.first()
                    categoryRepository.getCategoryFullInfo(firstChild.id)
                } else {
                    val nonOtherParent = categoryGroups.firstOrNull {
                        val n = it.parent.name.trim()
                        !(n.contains("其他") || n.equals("Other", ignoreCase = true))
                    }?.parent
                    if (nonOtherParent != null) {
                        categoryRepository.getCategoryFullInfo(nonOtherParent.id)
                    } else {
                        categoryGroups.firstOrNull()?.let { group ->
                            categoryRepository.getCategoryFullInfo(group.parent.id)
                        }
                    }
                }
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
        if (newSelectedInfo != null) {
            android.util.Log.d(
                "AddTxn_DefaultSelection",
                "最终默认选择: ${newSelectedInfo.fullPath ?: newSelectedInfo.categoryName} (${newSelectedInfo.categoryId})"
            )
        } else {
            android.util.Log.d("AddTxn_DefaultSelection", "最终默认选择: null (保持空)")
        }
        updateCanSave()
    }
    
    private fun updateCanSave() {
        _uiState.update { state ->
            val baseValid = state.amountText.isNotEmpty() && 
                           state.amountError == null && 
                           state.amountText.toDoubleOrNull() != null &&
                           state.amountText.toDouble() > 0 &&
                           state.selectedLedger != null
            
            val canSave = if (state.transactionType == TransactionType.TRANSFER) {
                // 转账模式：需要转出账户与转入账户，不需要分类
                baseValid &&
                state.fromAccount != null &&
                state.toAccount != null &&
                 state.fromAccount != state.toAccount // 转出与转入账户不能相同
            } else {
                // 普通模式：需要分类与账户
                baseValid &&
                state.selectedCategoryInfo != null &&
                state.selectedAccount != null
            }
            
            state.copy(canSave = canSave)
        }
    }
    
    private fun loadTransactionForEdit(transactionId: String) {
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // 根据ID获取交易数据
                val transaction = transactionRepository.getTransactionById(transactionId)
                if (transaction != null) {
                    // 获取分类信息
                    val category = categoryRepository.getCategoryById(transaction.categoryId)
                    val selectedCategoryInfo = category?.let { cat ->
                        SelectedCategoryInfo(
                            categoryId = cat.id,
                            categoryName = cat.name,
                            parentId = cat.parentId,
                            parentName = cat.parentId?.let { parentId ->
                                categoryRepository.getCategoryById(parentId)?.name
                            },
                            fullPath = if (cat.parentId != null) {
                                val parentName = categoryRepository.getCategoryById(cat.parentId!!)?.name ?: ""
                                "$parentName/${cat.name}"
                            } else {
                                cat.name
                            },
                            icon = cat.icon,
                            color = cat.color
                        )
                    }
                    
                    // 更新 UI 状态为编辑模式
                    _uiState.update { state ->
                        state.copy(
                            isEditMode = true,
                            editingTransactionId = transactionId,
                            isIncome = transaction.categoryDetails?.type == "INCOME",
                            amountText = transaction.amountYuan.toString(),
                            note = transaction.note ?: "",
                            selectedDate = transaction.transactionDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
                                ?: transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date,
                            selectedTime = transaction.transactionDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.time
                                ?: transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).time,
                            selectedCategoryInfo = selectedCategoryInfo,
                            selectedAccount = state.accounts.find { it.id == transaction.accountId },
                            isLoading = false
                        )
                    }
                    
                    // 閲嶆柊璁＄畻canSave鐘舵€?                    updateCanSave()
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    fun saveTransaction(onSuccess: () -> Unit) {
        val canSave = _uiState.value.canSave
        if (!canSave) {
            if (_uiState.value.selectedCategoryInfo == null && _uiState.value.transactionType != TransactionType.TRANSFER) {
                _uiState.update { it.copy(showCategoryPicker = true) }
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val state = _uiState.value
                val amountCents = ((state.amountText.toDoubleOrNull() ?: 0.0) * 100).toInt()
                val transactionDateTime = LocalDateTime(state.selectedDate, state.selectedTime)
                    .toInstant(TimeZone.currentSystemDefault())

                if (state.selectedLedger == null) {
                    _uiState.update { it.copy(showLedgerSelector = true, amountError = "Please select a ledger") }
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }
                if (amountCents <= 0) throw IllegalStateException("Amount must be greater than 0")

                if (state.transactionType == TransactionType.TRANSFER) {
                    if (state.fromAccount == null) {
                        _uiState.update { it.copy(showFromAccountPicker = true, amountError = "Please select a source account") }
                        _uiState.update { it.copy(isLoading = false) }
                        return@launch
                    }
                    if (state.toAccount == null) {
                        _uiState.update { it.copy(showToAccountPicker = true, amountError = "Please select a destination account") }
                        _uiState.update { it.copy(isLoading = false) }
                        return@launch
                    }
                    if (state.fromAccount == state.toAccount) {
                        throw IllegalStateException("转出和转入账户不能相同")
                    }
                } else {
                    if (state.selectedAccount == null) throw IllegalStateException("未选择账户")
                    if (state.selectedCategoryInfo == null) throw IllegalStateException("未选择分类")
                }

                if (state.isEditMode && state.editingTransactionId != null) {
                    val updated = com.ccxiaoji.feature.ledger.domain.model.Transaction(
                        id = state.editingTransactionId!!,
                        accountId = state.selectedAccount!!.id,
                        amountCents = amountCents,
                        categoryId = state.selectedCategoryInfo!!.categoryId,
                        categoryDetails = null,
                        note = state.note.ifBlank { null },
                        ledgerId = state.selectedLedger!!.id,
                        createdAt = transactionDateTime,
                        updatedAt = Clock.System.now(),
                        transactionDate = transactionDateTime,
                        location = state.selectedLocation
                    )
                    transactionRepository.updateTransaction(updated)
                    onSuccess()
                } else {
                    if (state.transactionType == TransactionType.TRANSFER) {
                        val result = createTransferUseCase.createTransfer(
                            fromAccountId = state.fromAccount!!.id,
                            toAccountId = state.toAccount!!.id,
                            amountCents = amountCents,
                            note = state.note.ifBlank { null },
                            ledgerId = state.selectedLedger!!.id,
                            transactionDate = transactionDateTime,
                            location = state.selectedLocation,
                            checkBalance = false
                        )
                        when (result) {
                            is com.ccxiaoji.common.base.BaseResult.Success -> onSuccess()
                            is com.ccxiaoji.common.base.BaseResult.Error -> throw result.exception
                        }
                    } else {
                        val result = if (state.selectedSyncTargets.isNotEmpty()) {
                            createLinkedTransactionUseCase.createLinkedTransaction(
                                primaryLedgerId = state.selectedLedger!!.id,
                                accountId = state.selectedAccount!!.id,
                                amountCents = amountCents,
                                categoryId = state.selectedCategoryInfo!!.categoryId,
                                note = state.note.ifBlank { null },
                                transactionDate = transactionDateTime,
                                location = state.selectedLocation,
                                autoSync = false,
                                specificTargetLedgers = state.selectedSyncTargets.toList()
                            )
                        } else {
                            createLinkedTransactionUseCase.createLinkedTransaction(
                                primaryLedgerId = state.selectedLedger!!.id,
                                accountId = state.selectedAccount!!.id,
                                amountCents = amountCents,
                                categoryId = state.selectedCategoryInfo!!.categoryId,
                                note = state.note.ifBlank { null },
                                transactionDate = transactionDateTime,
                                location = state.selectedLocation,
                                autoSync = true,
                                specificTargetLedgers = emptyList()
                            )
                        }
                        when (result) {
                            is com.ccxiaoji.common.base.BaseResult.Success -> onSuccess()
                            is com.ccxiaoji.common.base.BaseResult.Error -> throw result.exception
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AddTransactionViewModel", "保存交易失败", e)
                _uiState.update { it.copy(amountError = e.message) }
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
                // 初始化失败不影响主流程，仅记录日志
                android.util.Log.e("AddTransactionViewModel", "初始化默认分类失败", e)
            }
        }
    }
    
    // =============================================================================
    // 联动功能管理方法
    // =============================================================================
    
    /**
     * 加载指定账本的可用联动目标
     */
    private fun loadLinkTargets(ledgerId: String) {
        viewModelScope.launch {
            try {
                // 获取该账本的联动关系
                val linksFlow = manageLedgerLinkUseCase.getLedgerLinks(ledgerId)
                
                linksFlow.collect { links ->
                    // 根据联动关系确定可用的目标账本
                    val currentLedgers = _uiState.value.ledgers
                    val availableTargets = mutableListOf<Ledger>()
                    
                    for (link in links) {
                        val targetLedgerId = link.getOtherLedgerId(ledgerId)
                        val targetLedger = currentLedgers.find { it.id == targetLedgerId }
                        if (targetLedger != null && targetLedger.isActive) {
                            availableTargets.add(targetLedger)
                        }
                    }
                    
                    _uiState.update {
                        it.copy(
                            availableLinkTargets = availableTargets,
                            hasLinkOptions = availableTargets.isNotEmpty(),
                            selectedSyncTargets = emptySet() // 重置选择
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AddTransactionViewModel", "加载联动目标失败", e)
                // 出错时清空联动选项
                _uiState.update {
                    it.copy(
                        availableLinkTargets = emptyList(),
                        hasLinkOptions = false,
                        selectedSyncTargets = emptySet()
                    )
                }
            }
        }
    }
    
    /**
     * 显示联动目标选择器
     */
    fun showLinkTargetSelector() {
        _uiState.update {
            it.copy(showLinkTargetSelector = true)
        }
    }
    
    /**
     * 隐藏联动目标选择器
     */
    fun hideLinkTargetSelector() {
        _uiState.update {
            it.copy(showLinkTargetSelector = false)
        }
    }
    
    /**
     * 切换联动目标的选择状态
     */
    fun toggleSyncTarget(ledgerId: String) {
        _uiState.update { state ->
            val currentTargets = state.selectedSyncTargets.toMutableSet()
            if (currentTargets.contains(ledgerId)) {
                currentTargets.remove(ledgerId)
            } else {
                currentTargets.add(ledgerId)
            }
            state.copy(selectedSyncTargets = currentTargets)
        }
    }
    
    /**
     * 清除所有联动目标选择
     */
    fun clearAllSyncTargets() {
        _uiState.update {
            it.copy(selectedSyncTargets = emptySet())
        }
    }
    
    /**
     * 选择所有可用的联动目标
     */
    fun selectAllSyncTargets() {
        _uiState.update { state ->
            val allTargetIds = state.availableLinkTargets.map { it.id }.toSet()
            state.copy(selectedSyncTargets = allTargetIds)
        }
    }
    
    /**
     * 加载设置
     */
    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                val enableTimeRecording = preferences[ENABLE_TIME_RECORDING_KEY] ?: false
                _uiState.update { it.copy(enableTimeRecording = enableTimeRecording) }
            }
        }
    }
    
    companion object {
        private val ENABLE_TIME_RECORDING_KEY = booleanPreferencesKey("ledger_enable_time_recording")
    }
}
