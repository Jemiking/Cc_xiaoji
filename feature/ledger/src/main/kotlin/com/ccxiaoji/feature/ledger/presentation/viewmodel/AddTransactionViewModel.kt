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
import com.ccxiaoji.feature.ledger.domain.usecase.LoadCategoriesForDirectionUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.LoadCategoriesResult
import com.ccxiaoji.feature.ledger.domain.usecase.ManageCategoryUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerLinkUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.EvaluateAmountExpressionUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.SaveTransactionUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.SaveTransactionParams
import com.ccxiaoji.feature.ledger.domain.usecase.SaveTransactionResult
import com.ccxiaoji.feature.ledger.domain.usecase.SaveErrorFocus
import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionType
import com.ccxiaoji.feature.ledger.presentation.component.EntityEditorState
import com.ccxiaoji.feature.ledger.presentation.screen.transaction.TransactionFormState
import com.ccxiaoji.feature.ledger.presentation.screen.transaction.TransactionSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.datetime.*
import javax.inject.Inject

// 临时兼容数据类，仅供legacy代码使用
@Deprecated("仅供legacy代码使用", level = DeprecationLevel.WARNING)
data class LegacyUiState(
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val ledgers: List<Ledger> = emptyList(),
    val selectedLedger: Ledger? = null,
    val categoryGroups: List<CategoryGroup> = emptyList(),
    val frequentCategories: List<Category> = emptyList(),
    val selectedCategoryInfo: SelectedCategoryInfo? = null,
    val isIncome: Boolean = false,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val fromAccount: Account? = null,
    val toAccount: Account? = null,
    val amountText: String = "",
    val evaluatedAmount: Double? = null,
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
    val showFromAccountPicker: Boolean = false,
    val showToAccountPicker: Boolean = false,
    val isEditMode: Boolean = false,
    val editingTransactionId: String? = null,
    val availableLinkTargets: List<Ledger> = emptyList(),
    val selectedSyncTargets: Set<String> = emptySet(),
    val showLinkTargetSelector: Boolean = false,
    val hasLinkOptions: Boolean = false,
    val enableTimeRecording: Boolean = false
)

/**
 * 交易编辑器 ViewModel
 *
 * 采用状态分离模式：
 * - formState: 业务数据状态（TransactionFormState）
 * - editorState: UI元状态（EntityEditorState）
 *
 * 支持新增和编辑两种模式，以及转账功能
 */
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val getCategoryTree: GetCategoryTreeUseCase,
    private val getFrequentCategories: GetFrequentCategoriesUseCase,
    private val loadCategoriesForDirection: LoadCategoriesForDirectionUseCase,
    private val manageCategory: ManageCategoryUseCase,
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val manageLedgerLinkUseCase: ManageLedgerLinkUseCase,
    private val evaluateAmountExpression: EvaluateAmountExpressionUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase,
    private val userApi: UserApi,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    // ========== 协程管理架构 ==========

    /**
     * SupervisorScope: 子协程失败不会传播到父协程
     * 使用场景: 所有数据加载、保存操作
     */
    private val supervisorScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    /**
     * Job管理器: 跟踪可取消的长期任务
     */
    private var dataLoadJob: Job? = null
    private var saveTransactionJob: Job? = null
    private var categoryLoadJob: Job? = null
    private var linkTargetsJob: Job? = null
    private var settingsJob: Job? = null

    // 业务数据状态
    private val _formState = MutableStateFlow(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState.asStateFlow()

    // UI元状态
    private val _editorState = MutableStateFlow(EntityEditorState())
    val editorState: StateFlow<EntityEditorState> = _editorState.asStateFlow()

    // 最小兼容层：仅供legacy/AddTransactionScreen使用
    // 将在完全移除legacy代码时删除
    @Deprecated("仅供legacy代码使用，请使用formState和editorState", level = DeprecationLevel.WARNING)
    val uiState: StateFlow<LegacyUiState> = combine(formState, editorState) { form, editor ->
        LegacyUiState(
            accounts = form.accounts,
            selectedAccount = form.selectedAccount,
            ledgers = form.ledgers,
            selectedLedger = form.selectedLedger,
            categoryGroups = form.categoryGroups,
            frequentCategories = form.frequentCategories,
            selectedCategoryInfo = form.selectedCategoryInfo,
            isIncome = form.isIncome,
            transactionType = form.transactionType,
            fromAccount = form.fromAccount,
            toAccount = form.toAccount,
            amountText = form.amountText,
            evaluatedAmount = form.evaluatedAmount,
            note = form.note,
            selectedDate = form.selectedDate,
            selectedTime = form.selectedTime,
            selectedLocation = form.selectedLocation,
            isLoading = editor.isLoading,
            amountError = form.amountError,
            canSave = editor.saveEnabled,
            showCategoryPicker = form.showCategoryPicker,
            showLedgerSelector = form.showLedgerSelector,
            showDateTimePicker = form.showDateTimePicker,
            showFromAccountPicker = form.showFromAccountPicker,
            showToAccountPicker = form.showToAccountPicker,
            isEditMode = editor.isEditMode,
            editingTransactionId = form.editingTransactionId,
            availableLinkTargets = form.availableLinkTargets,
            selectedSyncTargets = form.selectedSyncTargets,
            showLinkTargetSelector = form.showLinkTargetSelector,
            hasLinkOptions = form.hasLinkOptions,
            enableTimeRecording = form.enableTimeRecording
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, LegacyUiState())

    private val currentUserId = userApi.getCurrentUserId()
    private val preselectedAccountId: String? = savedStateHandle["accountId"]
    private val transactionId: String? = savedStateHandle["transactionId"]

    // 用于脏值判断的初始快照
    private var initialSnapshot: TransactionSnapshot? = null

    // 保存成功事件（用于UI层监听）
    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

    // 自动记账预填参数（DeepLink）
    // IntType 不支持可空：NavGraph 使用 Int.MIN_VALUE 作为哨兵，这里映射为 null
    private val prefillAmountCents: Int? = savedStateHandle.get<Int>("amountCents")?.takeIf { it != Int.MIN_VALUE }
    private val prefillDirection: String? = savedStateHandle["direction"]
    private val prefillMerchant: String? = savedStateHandle["merchant"]
    private val prefillCategoryId: String? = savedStateHandle["categoryId"]
    private val prefillNote: String? = savedStateHandle["note"]
    
    init {
        // 监听表单状态变化，自动更新编辑器状态
        supervisorScope.launch {
            formState.collect { form ->
                updateEditorStateFromForm(form)
            }
        }

        checkAndInitializeCategories()
        loadData()
        loadSettings()

        // 应用自动预填
        applyAutoLedgerPrefill()
        // 如果有 transactionId，则进入编辑模式并加载交易数据
        transactionId?.let { id ->
            _editorState.value.isEditMode = true
            loadTransactionForEdit(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 取消所有协程
        supervisorScope.cancel()
        // 取消特定任务
        dataLoadJob?.cancel()
        saveTransactionJob?.cancel()
        categoryLoadJob?.cancel()
        linkTargetsJob?.cancel()
        settingsJob?.cancel()
    }

    // 更新初始快照（在数据加载完成后调用）
    private fun updateInitialSnapshot() {
        if (initialSnapshot == null) {
            initialSnapshot = _formState.value.createSnapshot()
        }
    }

    // 判断是否有未保存的更改
    fun hasUnsavedChanges(): Boolean {
        val currentSnapshot = _formState.value.createSnapshot()
        return initialSnapshot?.let { it != currentSnapshot } ?: false
    }
    private fun applyAutoLedgerPrefill() {
        supervisorScope.launch(Dispatchers.Default) {
            try {
                // 方向优先（触发分类加权）
                prefillDirection?.let { dir ->
                    val type = if (dir.equals("INCOME", ignoreCase = true)) TransactionType.INCOME else TransactionType.EXPENSE
                    withContext(Dispatchers.Main.immediate) {
                        setTransactionType(type)
                    }
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
                            _formState.update { it.copy(selectedCategoryInfo = info) }
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
    
    /**
     * 结构化并发加载数据: 账户、账本、分类
     * 三个任务并行执行，统一管理生命周期
     */
    private fun loadData() {
        // 取消之前的加载任务
        dataLoadJob?.cancel()

        dataLoadJob = supervisorScope.launch {
            // 使用 coroutineScope 确保子任务结构化
            coroutineScope {
                // 任务1: 加载账户 (数据库查询,使用IO)
                launch(Dispatchers.IO) {
                    accountRepository.getAccounts().collect { accounts ->
                        val selectedAccount = if (preselectedAccountId != null) {
                            accounts.find { it.id == preselectedAccountId }
                        } else {
                            accounts.firstOrNull()
                        }

                        withContext(Dispatchers.Main.immediate) {
                            _formState.update {
                                it.copy(
                                    accounts = accounts,
                                    selectedAccount = selectedAccount
                                )
                            }
                            // 新增模式下，设置初始快照
                            if (!_editorState.value.isEditMode) {
                                updateInitialSnapshot()
                            }
                        }
                    }
                }

                // 任务2: 加载账本 (数据库查询,使用IO)
                launch(Dispatchers.IO) {
                    manageLedgerUseCase.getUserLedgers(currentUserId).collect { ledgers ->
                        val defaultLedger = ledgers.find { it.isDefault } ?: ledgers.firstOrNull()

                        withContext(Dispatchers.Main.immediate) {
                            _formState.update {
                                it.copy(
                                    ledgers = ledgers,
                                    selectedLedger = defaultLedger
                                )
                            }
                            updateCanSave()
                            // 新增模式下，设置初始快照
                            if (!_editorState.value.isEditMode) {
                                updateInitialSnapshot()
                            }
                        }
                    }
                }

                // 任务3: 加载分类树 (数据库查询 + 计算,使用IO)
                launch(Dispatchers.IO) {
                    loadCategoriesInternal()
                }
            }
        }
    }

    /**
     * 内部分类加载方法(suspend函数,供结构化并发调用)
     */
    private suspend fun loadCategoriesInternal() {
        loadCategories()
    }
    
    fun selectAccount(account: Account) {
        _formState.update {
            it.copy(
                selectedAccount = account,
                showAccountPicker = false  // 选择后关闭选择器
            )
        }
        updateCanSave()
    }

    fun showAccountPicker() {
        _formState.update {
            it.copy(showAccountPicker = true)
        }
    }

    fun hideAccountPicker() {
        _formState.update {
            it.copy(showAccountPicker = false)
        }
    }

    fun selectLedger(ledger: Ledger) {
        _formState.update {
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
        _formState.update {
            it.copy(showLedgerSelector = true)
        }
    }

    fun hideLedgerSelector() {
        _formState.update {
            it.copy(showLedgerSelector = false)
        }
    }
    
    fun setIncomeType(isIncome: Boolean) {
        _formState.update {
            it.copy(
                transactionType = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                selectedCategoryInfo = null // 重置分类选择
            )
        }
        categoryLoadJob = supervisorScope.launch(Dispatchers.IO) {
            loadCategories()
        }
    }

    // 设置交易类型（新方法）
    fun setTransactionType(type: TransactionType) {
        _formState.update {
            it.copy(
                transactionType = type,
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
        _formState.update {
            it.copy(
                fromAccount = account,
                showFromAccountPicker = false
            )
        }
        updateCanSave()
    }

    // 设置转入账户
    fun setToAccount(account: Account) {
        _formState.update {
            it.copy(
                toAccount = account,
                showToAccountPicker = false
            )
        }
        updateCanSave()
    }

    fun selectCategory(category: Category) {
        android.util.Log.d("AddTransactionVM", "selectCategory called with: ${category.id}, ${category.name}")
        supervisorScope.launch(Dispatchers.IO) {
            // 记录使用频率
            getFrequentCategories.recordCategoryUsage(category.id)

            // 获取完整的分类信息（包含父分类路径）
            val categoryInfo = categoryRepository.getCategoryFullInfo(category.id)
            android.util.Log.d("AddTransactionVM", "getCategoryFullInfo returned: $categoryInfo")

            withContext(Dispatchers.Main.immediate) {
                _formState.update {
                    android.util.Log.d("AddTransactionVM", "Updating formState with categoryInfo: $categoryInfo")
                    it.copy(
                        selectedCategoryInfo = categoryInfo,
                        showCategoryPicker = false
                    )
                }
                android.util.Log.d("AddTransactionVM", "formState updated, selectedCategoryInfo: ${_formState.value.selectedCategoryInfo}")
                updateCanSave()
            }
        }
    }

    fun showCategoryPicker() {
        _formState.update {
            it.copy(showCategoryPicker = true)
        }
    }

    fun hideCategoryPicker() {
        _formState.update {
            it.copy(showCategoryPicker = false)
        }
    }

    // 显示转出账户选择器
    fun showFromAccountPicker() {
        _formState.update {
            it.copy(showFromAccountPicker = true)
        }
    }

    fun hideFromAccountPicker() {
        _formState.update {
            it.copy(showFromAccountPicker = false)
        }
    }

    fun showToAccountPicker() {
        _formState.update {
            it.copy(showToAccountPicker = true)
        }
    }

    fun hideToAccountPicker() {
        _formState.update {
            it.copy(showToAccountPicker = false)
        }
    }

    fun showDateTimePicker() {
        _formState.update {
            it.copy(showDateTimePicker = true)
        }
    }

    fun hideDateTimePicker() {
        _formState.update {
            it.copy(showDateTimePicker = false)
        }
    }
    
    fun updateAmount(amount: String) {
        // 允许输入数字、点、小加号与小减号，按"表达式"解析并评估
        val cleaned = amount.filter { it.isDigit() || it == '.' || it == '+' || it == '-' }

        val result = evaluateAmountExpression(cleaned)

        _formState.update {
            it.copy(
                amountText = cleaned,
                amountError = result.error,
                evaluatedAmount = result.evaluatedAmount
            )
        }
        updateCanSave()
    }

    fun updateNote(note: String) {
        _formState.update {
            it.copy(note = note)
        }
    }

    fun updateDate(date: LocalDate) {
        _formState.update {
            it.copy(selectedDate = date)
        }
    }

    fun updateTime(time: LocalTime) {
        _formState.update {
            it.copy(selectedTime = time)
        }
    }

    fun updateLocation(location: com.ccxiaoji.feature.ledger.domain.model.LocationData?) {
        _formState.update {
            it.copy(selectedLocation = location)
        }
    }


    // 保存成功后：留在当前页并清空部分字段
    fun resetForNextEntry() {
        _formState.update { state ->
            state.copy(
                amountText = "",
                evaluatedAmount = null,
                amountError = null,
                note = ""
            )
        }
        _editorState.value.saveEnabled = false
        _editorState.value.isLoading = false
    }
    
    private suspend fun loadCategories() {
        val userId = currentUserId
        val currentSelected = _formState.value.selectedCategoryInfo
        val isIncome = _formState.value.isIncome

        // 使用LoadCategoriesForDirectionUseCase加载分类并智能选择默认值
        val result = loadCategoriesForDirection(
            userId = userId,
            isIncome = isIncome,
            currentSelectedCategory = currentSelected
        )

        // 更新表单状态
        _formState.update {
            it.copy(
                categoryGroups = result.categoryGroups,
                frequentCategories = result.frequentCategories,
                selectedCategoryInfo = result.defaultSelectedCategory
            )
        }

        updateCanSave()
    }
    
    /**
     * 根据表单状态更新编辑器状态
     */
    private fun updateEditorStateFromForm(form: TransactionFormState) {
        val hasChanges = hasUnsavedChanges()

        val amountValid = form.evaluatedAmount != null && form.evaluatedAmount > 0.0
        val baseValid = form.amountText.isNotEmpty() &&
                       form.amountError == null &&
                       amountValid &&
                       form.selectedLedger != null

        val canSave = if (form.transactionType == TransactionType.TRANSFER) {
            // 转账模式：需要转出账户与转入账户，不需要分类
            baseValid &&
            form.fromAccount != null &&
            form.toAccount != null &&
            form.fromAccount != form.toAccount // 转出与转入账户不能相同
        } else {
            // 普通模式：需要分类与账户
            baseValid &&
            form.selectedCategoryInfo != null &&
            form.selectedAccount != null
        }

        _editorState.value.hasUnsavedChanges = hasChanges
        _editorState.value.saveEnabled = canSave
    }

    // 兼容性方法，后续可以移除
    @Deprecated("使用updateEditorStateFromForm替代")
    private fun updateCanSave() {
        updateEditorStateFromForm(_formState.value)
    }
    
    private fun loadTransactionForEdit(transactionId: String) {

        supervisorScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main.immediate) {
                    _editorState.value.isLoading = true
                }

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

                    // 更新表单状态
                    val amountResult = evaluateAmountExpression(transaction.amountYuan.toString())
                    _formState.update { state ->
                        state.copy(
                            editingTransactionId = transactionId,
                            transactionType = when (transaction.categoryDetails?.type) {
                                "INCOME" -> TransactionType.INCOME
                                else -> TransactionType.EXPENSE
                            },
                            amountText = transaction.amountYuan.toString(),
                            evaluatedAmount = amountResult.evaluatedAmount,
                            note = transaction.note ?: "",
                            selectedDate = transaction.transactionDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
                                ?: transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date,
                            selectedTime = transaction.transactionDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.time
                                ?: transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).time,
                            selectedCategoryInfo = selectedCategoryInfo,
                            selectedAccount = state.accounts.find { it.id == transaction.accountId }
                        )
                    }

                    // 更新编辑器状态
                    _editorState.value.isEditMode = true
                    _editorState.value.isLoading = false

                    // 重新计算canSave状态
                    updateCanSave()
                    // 编辑模式下，设置初始快照
                    updateInitialSnapshot()
                } else {
                    withContext(Dispatchers.Main.immediate) {
                        _editorState.value.isLoading = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main.immediate) {
                    _editorState.value.isLoading = false
                }
            }
        }
    }
    
    fun saveTransaction(onSuccess: () -> Unit) {
        val canSave = _editorState.value.saveEnabled
        if (!canSave) {
            if (_formState.value.selectedCategoryInfo == null && _formState.value.transactionType != TransactionType.TRANSFER) {
                _formState.update { it.copy(showCategoryPicker = true) }
            }
            return
        }

        // 取消之前的保存任务
        saveTransactionJob?.cancel()

        saveTransactionJob = supervisorScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main.immediate) {
                _editorState.value.isSaving = true
            }
            try {
                val state = _formState.value
                val evaluated = state.evaluatedAmount ?: 0.0
                val amountCents = kotlin.math.round(evaluated * 100.0).toInt()
                val transactionDateTime = LocalDateTime(state.selectedDate, state.selectedTime)
                    .toInstant(TimeZone.currentSystemDefault())

                // 构造保存参数
                val params = SaveTransactionParams(
                    isEditMode = _editorState.value.isEditMode,
                    transactionId = state.editingTransactionId,
                    transactionType = when (state.transactionType) {
                        TransactionType.INCOME -> "INCOME"
                        TransactionType.EXPENSE -> "EXPENSE"
                        TransactionType.TRANSFER -> "TRANSFER"
                        TransactionType.ALL -> "EXPENSE" // ALL不应该出现在保存时，默认为EXPENSE
                    },
                    selectedLedgerId = state.selectedLedger?.id,
                    selectedAccountId = state.selectedAccount?.id,
                    fromAccountId = state.fromAccount?.id,
                    toAccountId = state.toAccount?.id,
                    selectedCategoryInfo = state.selectedCategoryInfo,
                    amountCents = amountCents,
                    note = state.note.ifBlank { null },
                    transactionInstant = transactionDateTime,
                    location = state.selectedLocation,
                    selectedSyncTargets = state.selectedSyncTargets
                )

                // 调用UseCase并处理结果
                when (val result = saveTransactionUseCase(params)) {
                    is SaveTransactionResult.Success -> {
                        withContext(Dispatchers.Main.immediate) {
                            _saveSuccessEvent.emit(Unit)
                            _editorState.value.isSaving = false
                            onSuccess()
                        }
                    }
                    is SaveTransactionResult.ValidationError -> {
                        // 根据错误焦点显示对应的选择器
                        withContext(Dispatchers.Main.immediate) {
                            _editorState.value.isSaving = false
                            handleSaveValidationError(result)
                        }
                    }
                    is SaveTransactionResult.Error -> {
                        android.util.Log.e("AddTransactionViewModel", "保存交易失败", result.exception)
                        withContext(Dispatchers.Main.immediate) {
                            _formState.update { it.copy(amountError = result.exception.message) }
                            _editorState.value.isSaving = false
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AddTransactionViewModel", "保存交易失败", e)
                withContext(Dispatchers.Main.immediate) {
                    _formState.update { it.copy(amountError = e.message) }
                    _editorState.value.isSaving = false
                }
            }
        }
    }

    /**
     * 处理保存验证错误，根据错误焦点显示相应UI
     */
    private fun handleSaveValidationError(error: SaveTransactionResult.ValidationError) {
        when (error.focus) {
            SaveErrorFocus.LEDGER -> {
                _formState.update { it.copy(showLedgerSelector = true, amountError = error.message) }
            }
            SaveErrorFocus.CATEGORY -> {
                _formState.update { it.copy(showCategoryPicker = true, amountError = error.message) }
            }
            SaveErrorFocus.ACCOUNT -> {
                _formState.update { it.copy(showFromAccountPicker = true, amountError = error.message) }
            }
            SaveErrorFocus.FROM_ACCOUNT -> {
                _formState.update { it.copy(showFromAccountPicker = true, amountError = error.message) }
            }
            SaveErrorFocus.TO_ACCOUNT -> {
                _formState.update { it.copy(showToAccountPicker = true, amountError = error.message) }
            }
            SaveErrorFocus.AMOUNT -> {
                _formState.update { it.copy(amountError = error.message) }
            }
        }
    }
    
    private fun checkAndInitializeCategories() {
        supervisorScope.launch(Dispatchers.IO) {
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
        // 取消之前的任务
        linkTargetsJob?.cancel()

        linkTargetsJob = supervisorScope.launch(Dispatchers.IO) {
            try {
                // 获取该账本的联动关系
                val linksFlow = manageLedgerLinkUseCase.getLedgerLinks(ledgerId)

                linksFlow.collect { links ->
                    // 根据联动关系确定可用的目标账本
                    val currentLedgers = _formState.value.ledgers
                    val availableTargets = mutableListOf<Ledger>()

                    for (link in links) {
                        val targetLedgerId = link.getOtherLedgerId(ledgerId)
                        val targetLedger = currentLedgers.find { it.id == targetLedgerId }
                        if (targetLedger != null && targetLedger.isActive) {
                            availableTargets.add(targetLedger)
                        }
                    }

                    withContext(Dispatchers.Main.immediate) {
                        _formState.update {
                            it.copy(
                                availableLinkTargets = availableTargets,
                                hasLinkOptions = availableTargets.isNotEmpty(),
                                selectedSyncTargets = emptySet() // 重置选择
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AddTransactionViewModel", "加载联动目标失败", e)
                // 出错时清空联动选项
                withContext(Dispatchers.Main.immediate) {
                    _formState.update {
                        it.copy(
                            availableLinkTargets = emptyList(),
                            hasLinkOptions = false,
                            selectedSyncTargets = emptySet()
                        )
                    }
                }
            }
        }
    }

    /**
     * 显示联动目标选择器
     */
    fun showLinkTargetSelector() {
        _formState.update {
            it.copy(showLinkTargetSelector = true)
        }
    }

    /**
     * 隐藏联动目标选择器
     */
    fun hideLinkTargetSelector() {
        _formState.update {
            it.copy(showLinkTargetSelector = false)
        }
    }

    /**
     * 切换联动目标的选择状态
     */
    fun toggleSyncTarget(ledgerId: String) {
        _formState.update { state ->
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
        _formState.update {
            it.copy(selectedSyncTargets = emptySet())
        }
    }

    /**
     * 选择所有可用的联动目标
     */
    fun selectAllSyncTargets() {
        _formState.update { state ->
            val allTargetIds = state.availableLinkTargets.map { it.id }.toSet()
            state.copy(selectedSyncTargets = allTargetIds)
        }
    }
    
    /**
     * 加载设置
     */
    private fun loadSettings() {
        // 取消之前的任务
        settingsJob?.cancel()

        settingsJob = supervisorScope.launch(Dispatchers.IO) {
            dataStore.data.collect { preferences ->
                val enableTimeRecording = preferences[ENABLE_TIME_RECORDING_KEY] ?: false
                withContext(Dispatchers.Main.immediate) {
                    _formState.update { it.copy(enableTimeRecording = enableTimeRecording) }
                }
            }
        }
    }
    
    companion object {
        private val ENABLE_TIME_RECORDING_KEY = booleanPreferencesKey("ledger_enable_time_recording")
    }
}
