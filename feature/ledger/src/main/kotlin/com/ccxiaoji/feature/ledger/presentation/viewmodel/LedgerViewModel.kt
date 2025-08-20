package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.domain.usecase.*
import com.ccxiaoji.feature.ledger.data.cache.LedgerCacheManager
import com.ccxiaoji.feature.ledger.data.migration.DataMigrationTool
import com.ccxiaoji.feature.ledger.data.diagnostic.LedgerDiagnosticTool
import com.ccxiaoji.feature.ledger.data.diagnostic.ImportMappingAnalyzer
import com.ccxiaoji.feature.ledger.data.diagnostic.DefaultAccountAnalyzer
import com.ccxiaoji.feature.ledger.data.repair.DataRepairTool
import com.ccxiaoji.feature.ledger.data.diagnostic.TransactionFlowTracker
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.shared.user.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getPaginatedTransactionsUseCase: GetPaginatedTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getMonthlyStatsUseCase: GetMonthlyStatsUseCase,
    private val checkBudgetUseCase: CheckBudgetUseCase,
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val cacheManager: LedgerCacheManager,
    private val dataMigrationTool: DataMigrationTool,
    private val diagnosticTool: LedgerDiagnosticTool,
    private val mappingAnalyzer: ImportMappingAnalyzer,
    private val defaultAccountAnalyzer: DefaultAccountAnalyzer,
    private val dataRepairTool: DataRepairTool,
    private val flowTracker: TransactionFlowTracker,
    private val transactionDao: TransactionDao,
    private val userApi: UserApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState = _uiState.asStateFlow()
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth = _selectedMonth.asStateFlow()
    private var currentPage = 0
    private val pageSize = 10000  // 设置足够大的数值，一次获取所有交易
    private var hasMoreData = true
    private var isLoadingMore = false
    
    init {
        // 首次启动时运行诊断和修复
        viewModelScope.launch {
            val userId = userApi.getCurrentUserId()
            // 运行完整诊断
            android.util.Log.e("LEDGER_DEBUG", "运行启动诊断...")
            diagnosticTool.runFullDiagnostic(userId)
            
            // 运行映射错误分析
            android.util.Log.e("LEDGER_DEBUG", "运行映射错误分析...")
            val report = mappingAnalyzer.analyzeMappingErrors(userId)
            
            // 分析默认账户数据
            android.util.Log.e("LEDGER_DEBUG", "分析默认账户数据...")
            defaultAccountAnalyzer.analyzeDefaultAccount(userId)
            
            // 如果发现问题，自动执行修复
            if (report.transferPartyAccounts.isNotEmpty()) {
                android.util.Log.e("LEDGER_DEBUG", "发现${report.transferPartyAccounts.size}个问题账户，自动执行修复...")
                dataRepairTool.executeRepair(userId)
            }
        }
        
        // 自动修复数据
        fixOrphanAccountData()
        loadAccounts()
        loadCategories()
        loadLedgers()
        loadTransactions()
        loadMonthlySummary()
    }
    
    /**
     * 修复孤儿账户数据
     * 将导入时创建的临时账户交易迁移到默认账户
     */
    fun fixOrphanAccountData() = viewModelScope.launch {
        try {
            android.util.Log.e("LEDGER_DEBUG", "开始修复孤儿账户数据...")
            val userId = userApi.getCurrentUserId()
            dataMigrationTool.fixOrphanAccountTransactions(userId)
            android.util.Log.e("LEDGER_DEBUG", "数据修复完成，重新加载账户和交易")
            // 修复后重新加载数据
            loadAccounts()
            loadTransactions()
            loadMonthlySummary()
        } catch (e: Exception) {
            android.util.Log.e("LEDGER_DEBUG", "数据修复失败: ${e.message}", e)
        }
    }
    
    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        currentPage = 0  // 总是从第0页开始，一次加载所有数据
        loadTransactions()
        loadMonthlySummary()
    }
    
    private fun getMonthDateRange(month: YearMonth): Pair<Long, Long> {
        val start = LocalDate(month.year, month.monthValue, 1)
            .atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val nextMonth = month.plusMonths(1)
        val end = LocalDate(nextMonth.year, nextMonth.monthValue, 1)
            .atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        return start to end
    }
    
    private fun loadTransactions() {
        viewModelScope.launch {
            android.util.Log.e("LEDGER_DEBUG", "========== 加载交易记录 ==========")
            android.util.Log.e("LEDGER_DEBUG", "一次加载所有数据，pageSize: $pageSize")
            
            currentPage = 0  // 总是从第0页开始
            _uiState.update { it.copy(isLoading = true) }
            
            val (start, end) = getMonthDateRange(_selectedMonth.value)
            android.util.Log.e("LEDGER_DEBUG", "查询月份: ${_selectedMonth.value}")
            android.util.Log.e("LEDGER_DEBUG", "日期范围: $start - $end")
            android.util.Log.e("LEDGER_DEBUG", "时间转换: 开始=${java.util.Date(start)}, 结束=${java.util.Date(end)}")
            // 如果选择了特定账户ID，并且不是默认账户（default_account_开头），则使用该ID
            // 否则不指定账户ID，查询所有账户
            val accountIdForQuery = _uiState.value.selectedAccountId?.let { id ->
                if (id.startsWith("default_account_")) {
                    null // 默认账户查询所有交易
                } else {
                    id // 特定账户只查询该账户
                }
            }
            android.util.Log.e("LEDGER_DEBUG", "账户ID: ${_uiState.value.selectedAccountId}")
            android.util.Log.e("LEDGER_DEBUG", "查询账户ID: $accountIdForQuery")
            android.util.Log.e("LEDGER_DEBUG", "分页: page=$currentPage, size=$pageSize")
            
            // 运行全链路数据流追踪
            val userId = userApi.getCurrentUserId()
            viewModelScope.launch {
                flowTracker.trackTransactionFlow(
                    userId = userId,
                    selectedAccountId = accountIdForQuery,
                    startDate = start,
                    endDate = end,
                    currentPage = currentPage,
                    pageSize = pageSize
                )
                
                // 验证当前月份数据
                flowTracker.verifyMonthData(
                    userId = userId,
                    year = _selectedMonth.value.year,
                    month = _selectedMonth.value.monthValue
                )
            }
            
            // 运行查询逻辑诊断（每次加载时都运行）
            viewModelScope.launch {
                diagnosticTool.analyzeQueryLogic(
                    userId = userId,
                    selectedAccountId = accountIdForQuery,
                    startDate = start,
                    endDate = end
                )
            }
            
            getPaginatedTransactionsUseCase(
                currentPage, pageSize, accountIdForQuery, start, end
            ).collect { result ->
                android.util.Log.e("LEDGER_DEBUG", "查询结果类型: ${result::class.simpleName}")
                handleTransactionResult(result)
            }
        }
    }
    
    private fun handleTransactionResult(result: BaseResult<GetPaginatedTransactionsUseCase.PaginatedResult>) {
        when (result) {
            is BaseResult.Success -> {
                val data = result.data
                android.util.Log.e("LEDGER_DEBUG", "查询成功: 返回 ${data.transactions.size} 条记录, 总数: ${data.totalCount}")
                android.util.Log.e("LEDGER_DEBUG", "一次加载完成，显示所有 ${data.transactions.size} 条交易")
                if (data.transactions.isNotEmpty()) {
                    val first = data.transactions.first()
                    val last = data.transactions.last()
                    android.util.Log.e("LEDGER_DEBUG", "第一条记录: ID=${first.id}, Date=${first.createdAt}")
                    android.util.Log.e("LEDGER_DEBUG", "最后一条记录: ID=${last.id}, Date=${last.createdAt}")
                } else {
                    android.util.Log.e("LEDGER_DEBUG", "警告：查询返回空列表！")
                }
                _uiState.update {
                    it.copy(
                        transactions = data.transactions,  // 直接设置所有交易
                        isLoading = false,
                        isLoadingMore = false,
                        hasMoreData = false  // 一次加载所有，不需要更多
                    )
                }
                cacheManager.updateRecentTransactionsCache(data.transactions)
            }
            is BaseResult.Error -> {
                android.util.Log.e("LEDGER_DEBUG", "查询失败: ${result.exception.message}", result.exception)
                _uiState.update { it.copy(isLoading = false, isLoadingMore = false) }
            }
        }
    }
    
    fun addTransaction(
        amountCents: Int, categoryId: String, note: String?, 
        accountId: String? = null, onBudgetAlert: (String, Boolean) -> Unit = { _, _ -> }
    ) = launch {
        val finalAccountId = accountId ?: _uiState.value.selectedAccountId 
            ?: _uiState.value.accounts.firstOrNull()?.id ?: return@launch
        runCatching {
            addTransactionUseCase(amountCents, categoryId, note, finalAccountId)
            checkBudget(categoryId, onBudgetAlert)
            loadMonthlySummary()
            loadTransactions()  // 刷新交易列表
        }
    }
    
    private suspend fun checkBudget(categoryId: String, onAlert: (String, Boolean) -> Unit) = runCatching {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        when (val status = checkBudgetUseCase.checkBudgetStatus(now.year, now.monthNumber, categoryId)) {
            is BudgetStatus.Exceeded -> onAlert("该分类预算已超支！", true)
            is BudgetStatus.Alert -> onAlert("该分类预算即将用完！", false)
            else -> Unit
        }
    }
    
    fun updateTransaction(transaction: Transaction) = launch {
        runCatching { updateTransactionUseCase(transaction); loadMonthlySummary() }
    }
    
    fun deleteTransaction(transactionId: String) = launch {
        runCatching { deleteTransactionUseCase(transactionId); loadMonthlySummary() }
    }
    
    fun copyTransaction(transaction: Transaction) = launch {
        runCatching {
            addTransactionUseCase(
                transaction.amountCents, transaction.categoryId,
                "[复制] ${transaction.note ?: ""}", transaction.accountId
            )
            loadTransactions()
            loadMonthlySummary()
        }
    }
    
    private fun loadMonthlySummary() = launch {
        runCatching {
            val month = _selectedMonth.value
            val (income, expense) = getMonthlyStatsUseCase(month.year, month.monthValue)
            _uiState.update { it.copy(monthlyIncome = income / 100.0, monthlyExpense = expense / 100.0) }
        }
    }
    
    private fun loadAccounts() = launch {
        getAccountsUseCase().collect { accounts ->
            android.util.Log.e("LEDGER_DEBUG", "")
            android.util.Log.e("LEDGER_DEBUG", "========== 账户加载与选择逻辑 ==========")
            android.util.Log.e("LEDGER_DEBUG", "加载账户列表: ${accounts.size} 个账户")
            
            // 打印所有账户详情
            accounts.forEach { account ->
                android.util.Log.e("LEDGER_DEBUG", "账户: ${account.name}")
                android.util.Log.e("LEDGER_DEBUG", "  ID: ${account.id}")
                android.util.Log.e("LEDGER_DEBUG", "  类型: ${account.type}")
                android.util.Log.e("LEDGER_DEBUG", "  是否默认: ${account.isDefault}")
                
                // 统计该账户的交易数
                viewModelScope.launch {
                    val userId = userApi.getCurrentUserId()
                    val accountTransactions = transactionDao.getTransactionsByUserSync(userId)
                        .filter { it.accountId == account.id && !it.isDeleted }
                    android.util.Log.e("LEDGER_DEBUG", "  交易数: ${accountTransactions.size}")
                    
                    // 如果是现金账户，显示更多信息
                    if (account.id.startsWith("default_account_") || account.name == "现金") {
                        android.util.Log.e("LEDGER_DEBUG", "  ⚠️ 这是现金账户（原默认账户）")
                        android.util.Log.e("LEDGER_DEBUG", "  包含钱迹空账户名的交易")
                    }
                }
            }
            
            // 智能选择默认账户
            val previousSelectedId = _uiState.value.selectedAccountId
            android.util.Log.e("LEDGER_DEBUG", "")
            android.util.Log.e("LEDGER_DEBUG", "当前选中账户: ${previousSelectedId ?: "无"}")
            
            val selectedAccountId = when {
                accounts.isEmpty() -> {
                    android.util.Log.e("LEDGER_DEBUG", "无账户，selectedAccountId = null")
                    null
                }
                // 优先保持当前选择
                previousSelectedId != null && accounts.any { it.id == previousSelectedId } -> {
                    android.util.Log.e("LEDGER_DEBUG", "保持当前选择: $previousSelectedId")
                    previousSelectedId
                }
                // 选择默认账户
                accounts.any { it.isDefault } -> {
                    val defaultAccount = accounts.first { it.isDefault }
                    android.util.Log.e("LEDGER_DEBUG", "选择默认账户: ${defaultAccount.name} (${defaultAccount.id})")
                    defaultAccount.id
                }
                // 选择交易最多的账户
                else -> {
                    // 这里先选择第一个，后续可以优化为选择交易最多的
                    val firstAccount = accounts.first()
                    android.util.Log.e("LEDGER_DEBUG", "选择第一个账户: ${firstAccount.name} (${firstAccount.id})")
                    firstAccount.id
                }
            }
            
            android.util.Log.e("LEDGER_DEBUG", "最终选择账户: $selectedAccountId")
            android.util.Log.e("LEDGER_DEBUG", "========================================")
            android.util.Log.e("LEDGER_DEBUG", "")
            
            _uiState.update {
                it.copy(accounts = accounts, selectedAccountId = selectedAccountId)
            }
            cacheManager.updateAccountsCache(accounts)
            
            // 如果账户选择变化，重新加载交易
            if (previousSelectedId != selectedAccountId) {
                android.util.Log.e("LEDGER_DEBUG", "账户选择变化，重新加载交易")
                loadTransactions()
            }
        }
    }
    
    private fun loadCategories() = launch {
        getCategoriesUseCase().collect { categories ->
            _uiState.update { it.copy(categories = categories) }
            cacheManager.updateCategoriesCache(categories)
        }
    }
    
    // ==================== 记账簿管理方法 ====================
    
    /**
     * 加载记账簿列表
     */
    private fun loadLedgers() = launch {
        try {
            _uiState.update { it.copy(isLedgerLoading = true) }
            
            val userId = userApi.getCurrentUserId()
            val result = manageLedgerUseCase.getLedgers(userId)
            
            when (result) {
                is BaseResult.Success -> {
                    val ledgers = result.data
                    val defaultLedger = ledgers.find { it.isDefault }
                    
                    _uiState.update { 
                        it.copy(
                            ledgers = ledgers,
                            currentLedger = defaultLedger,
                            selectedLedgerId = defaultLedger?.id,
                            isLedgerLoading = false
                        )
                    }
                    
                    android.util.Log.d("LEDGER_DEBUG", "记账簿加载成功: ${ledgers.size}个记账簿")
                }
                is BaseResult.Error -> {
                    android.util.Log.e("LEDGER_DEBUG", "记账簿加载失败: ${result.exception.message}")
                    _uiState.update { it.copy(isLedgerLoading = false) }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LEDGER_DEBUG", "记账簿加载异常", e)
            _uiState.update { it.copy(isLedgerLoading = false) }
        }
    }
    
    /**
     * 选择记账簿
     */
    fun selectLedger(ledgerId: String) = launch {
        try {
            val currentLedgers = _uiState.value.ledgers
            val selectedLedger = currentLedgers.find { it.id == ledgerId }
            
            if (selectedLedger != null) {
                _uiState.update { 
                    it.copy(
                        currentLedger = selectedLedger,
                        selectedLedgerId = ledgerId
                    )
                }
                
                // 重新加载基于记账簿的数据
                loadTransactions()
                loadMonthlySummary()
                
                android.util.Log.d("LEDGER_DEBUG", "切换到记账簿: ${selectedLedger.name}")
            } else {
                android.util.Log.e("LEDGER_DEBUG", "未找到记账簿: $ledgerId")
            }
        } catch (e: Exception) {
            android.util.Log.e("LEDGER_DEBUG", "记账簿选择失败", e)
        }
    }
    
    /**
     * 获取当前记账簿
     */
    fun getCurrentLedger(): Ledger? {
        return _uiState.value.currentLedger
    }
    
    /**
     * 刷新记账簿列表
     */
    fun refreshLedgers() {
        loadLedgers()
    }
    
    // ==================== 账户管理方法 ====================
    
    fun selectAccount(accountId: String?) {
        _uiState.update { it.copy(selectedAccountId = accountId) }
        loadTransactions()  // 重新加载所有交易
    }
    
    fun refreshTransactions() {
        loadTransactions()  // 刷新所有交易
        loadMonthlySummary()
    }
    
    /**
     * 运行导入映射错误分析
     * 精确定位导入时的字段映射问题
     */
    fun analyzeMappingErrors() = viewModelScope.launch {
        android.util.Log.e("LEDGER_DEBUG", "手动触发映射错误分析...")
        val userId = userApi.getCurrentUserId()
        val report = mappingAnalyzer.analyzeMappingErrors(userId)
        
        // 输出分析结果摘要
        android.util.Log.e("LEDGER_DEBUG", "")
        android.util.Log.e("LEDGER_DEBUG", "【映射分析结果】")
        android.util.Log.e("LEDGER_DEBUG", "转账对象误判: ${report.transferPartyAccounts.size}个")
        android.util.Log.e("LEDGER_DEBUG", "正常支付账户: ${report.paymentMethodAccounts.size}个")
        android.util.Log.e("LEDGER_DEBUG", "可疑账户: ${report.suspiciousAccounts.size}个")
        android.util.Log.e("LEDGER_DEBUG", "")
        
        // 如果发现问题，提示用户
        if (report.transferPartyAccounts.isNotEmpty()) {
            android.util.Log.e("LEDGER_DEBUG", "⚠️ 发现导入映射错误，需要修复")
            android.util.Log.e("LEDGER_DEBUG", "建议查看 MAPPING_ANALYZER 标签的详细日志")
        }
    }
    
    /**
     * 执行数据修复
     * 修复导入时产生的问题数据
     */
    fun executeDataRepair() = viewModelScope.launch {
        android.util.Log.e("LEDGER_DEBUG", "开始执行数据修复...")
        val userId = userApi.getCurrentUserId()
        
        try {
            // 执行修复
            dataRepairTool.executeRepair(userId)
            
            android.util.Log.e("LEDGER_DEBUG", "数据修复完成")
            
            // 重新加载数据
            loadAccounts()
            loadTransactions()
            loadMonthlySummary()
            
            android.util.Log.e("LEDGER_DEBUG", "已重新加载数据")
        } catch (e: Exception) {
            android.util.Log.e("LEDGER_DEBUG", "数据修复失败: ${e.message}", e)
        }
    }
    
    private fun launch(block: suspend () -> Unit) = viewModelScope.launch { block() }
}

// UI状态 - 核心数据
data class LedgerUiState(
    // 交易相关数据
    val transactions: List<Transaction> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    
    // 账户相关数据
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: String? = null,
    
    // 分类相关数据
    val categories: List<Category> = emptyList(),
    
    // 记账簿相关数据
    val ledgers: List<Ledger> = emptyList(),
    val currentLedger: Ledger? = null,
    val selectedLedgerId: String? = null,
    val isLedgerLoading: Boolean = false,
    
    // 加载状态
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreData: Boolean = true
)