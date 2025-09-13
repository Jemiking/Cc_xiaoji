package com.ccxiaoji.feature.ledger.domain.usecase

import android.util.Log
import com.ccxiaoji.feature.ledger.data.manager.DeduplicationManager
import com.ccxiaoji.feature.ledger.data.manager.ProcessDecision
import com.ccxiaoji.feature.ledger.domain.model.PaymentDirection
import com.ccxiaoji.feature.ledger.domain.model.PaymentNotification
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.parser.NotificationParserFactory
import com.ccxiaoji.feature.ledger.domain.parser.ParseResult
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.service.AutoLedgerNotificationManager
import com.ccxiaoji.shared.notification.api.NotificationEventRepository
import com.ccxiaoji.shared.notification.domain.model.RawNotificationEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit

/**
 * 自动记账管理器
 * 
 * 负责整个自动记账流程的编排，包括：
 * 1. 监听系统通知事件
 * 2. 解析支付信息
 * 3. 去重验证
 * 4. 账户/分类推荐
 * 5. 创建交易记录
 * 6. 调试信息记录
 */
@Singleton
class AutoLedgerManager @Inject constructor(
    private val notificationEventRepository: NotificationEventRepository,
    private val notificationParserFactory: NotificationParserFactory,
    private val deduplicationManager: DeduplicationManager,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val accountCategoryRecommender: AccountCategoryRecommender,
    private val notificationManager: AutoLedgerNotificationManager,
    private val recordDebugUseCase: RecordAutoLedgerDebugUseCase,
    private val dataStore: DataStore<Preferences>,
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val userApi: com.ccxiaoji.shared.user.api.UserApi
) {
    
    companion object {
        private const val TAG = "AutoLedger_Manager"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    /**
     * 自动记账结果流
     */
    private val _autoLedgerResults = MutableSharedFlow<AutoLedgerResult>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val autoLedgerResults: Flow<AutoLedgerResult> = _autoLedgerResults.asSharedFlow()
    
    /** 运行时开关与参数 */
    private var isEnabled = false
    @Volatile private var autoCreateEnabled: Boolean = false
    @Volatile private var autoCreateConfidenceThreshold: Float = 0.85f
    @Volatile private var minAmountCents: Int = 100
    @Volatile private var dedupDebugParseOnSkip: Boolean = false
    @Volatile private var currentMode: String = "SEMI" // "SEMI" / "FULL"
    
    /**
     * 启动自动记账服务
     */
    fun start() {
        Log.i(TAG, "🚀 启动自动记账服务")
        
        if (isEnabled) {
            Log.w(TAG, "⚠️ 服务已启动，跳过重复启动")
            return
        }
        
        isEnabled = true
        Log.d(TAG, "✅ 设置服务状态为启用")

        // 同步两挡模式设置
        scope.launch {
            val MODE_KEY = androidx.datastore.preferences.core.stringPreferencesKey("auto_ledger_mode")
            dataStore.data.collect { prefs ->
                currentMode = prefs[MODE_KEY] ?: "SEMI"
                // 软关闭：即使用户选择了FULL，也强制仅半自动生效
                autoCreateEnabled = false
                autoCreateConfidenceThreshold = 0.85f
                minAmountCents = 100
                dedupDebugParseOnSkip = false
                Log.d(TAG, "⚙️ 模式同步: mode=$currentMode, autoCreate(forced)=false, th=$autoCreateConfidenceThreshold, min=${minAmountCents}分")
                if (currentMode.equals("FULL", true)) {
                    Log.i(TAG, "🛡️ 已启用‘全自动软关闭’：当前版本仅提供半自动体验，后续可恢复")
                }
            }
        }
        
        notificationEventRepository.events
            .filter { isEnabled } // 动态控制开关
            .onEach { event -> 
                Log.d(TAG, "📨 收到事件，开始处理: ${event.packageName}")
                processNotificationEvent(event) 
            }
            .catch { e -> 
                Log.e(TAG, "❌ 通知处理异常", e)
                _autoLedgerResults.emit(
                    AutoLedgerResult.Error("通知处理异常: ${e.message}")
                )
            }
            .launchIn(scope)
            
        Log.i(TAG, "🔗 已订阅通知事件流，等待支付通知...")
    }
    
    /**
     * 停止自动记账服务
     */
    fun stop() {
        Log.i(TAG, "🛑 停止自动记账服务")
        isEnabled = false
        scope.coroutineContext[Job]?.cancelChildren()
        Log.d(TAG, "✅ 已取消所有子协程，服务已停止")
    }
    
    /**
     * 处理单个通知事件
     */
    private suspend fun processNotificationEvent(event: RawNotificationEvent) {
        withContext(Dispatchers.Default) {
            val startTime = System.currentTimeMillis()
            Log.i(TAG, "🔄 开始处理通知事件: ${event.packageName}")
            Log.d(TAG, "📋 事件详情: key=${event.notificationKey}, title='${event.title}', text='${event.text}'")
            
            try {
                // 1. 去重检查
                Log.d(TAG, "🔍 开始去重检查...")
                when (val decision = deduplicationManager.shouldProcess(event)) {
                    is ProcessDecision.Skip -> {
                        val processingTime = System.currentTimeMillis() - startTime
                        Log.w(TAG, "⚠️ 事件被去重跳过: ${decision.reason} (耗时: ${processingTime}ms)")

                        // 可选：仅在调试开关开启时尝试解析（不入账，仅记录）
                        if (dedupDebugParseOnSkip) {
                            val parseResult = notificationParserFactory.parse(event)
                            if (parseResult is ParseResult.Success) {
                                Log.d(TAG, "📝 [DUP-DEBUG] 记录跳过的重复事件调试信息")
                                recordDebugUseCase.recordSkippedDuplicate(
                                    parseResult.notification, decision.reason, processingTime
                                )
                            }
                        }
                        
                        _autoLedgerResults.emit(
                            AutoLedgerResult.Skipped(event.packageName, decision.reason)
                        )
                        return@withContext
                    }
                    is ProcessDecision.Error -> {
                        val processingTime = System.currentTimeMillis() - startTime
                        Log.e(TAG, "❌ 去重检查失败: ${decision.message} (耗时: ${processingTime}ms)")
                        
                        // 记录处理错误
                        recordDebugUseCase.recordUnknownError(
                            mapPackageNameToSourceType(event.packageName),
                            event.title ?: "",
                            event.text ?: "",
                            "去重检查失败: ${decision.message}",
                            processingTime
                        )
                        
                        _autoLedgerResults.emit(
                            AutoLedgerResult.Error("去重检查失败: ${decision.message}")
                        )
                        return@withContext
                    }
                    is ProcessDecision.Process -> {
                        Log.i(TAG, "✅ 去重检查通过，继续处理事件: ${decision.eventKey}")
                        // 继续处理
                        processValidEvent(event, decision.eventKey, startTime)
                    }
                }
            } catch (e: Exception) {
                val processingTime = System.currentTimeMillis() - startTime
                Log.e(TAG, "💥 事件处理异常 (耗时: ${processingTime}ms)", e)
                
                // 记录未知异常
                recordDebugUseCase.recordUnknownError(
                    mapPackageNameToSourceType(event.packageName),
                    event.title ?: "",
                    event.text ?: "",
                    "事件处理异常: ${e.message}",
                    processingTime
                )
                
                _autoLedgerResults.emit(
                    AutoLedgerResult.Error("事件处理异常: ${e.message}")
                )
            }
        }
    }
    
    /**
     * 处理验证通过的事件
     */
    private suspend fun processValidEvent(event: RawNotificationEvent, eventKey: String, startTime: Long) {
        Log.i(TAG, "⚡ 开始处理验证通过的事件")
        
        // 2. 解析通知内容
        Log.d(TAG, "🔧 开始解析通知内容...")
        when (val parseResult = notificationParserFactory.parse(event)) {
            is ParseResult.Success -> {
                val notification = parseResult.notification
                Log.i(TAG, "🎉 解析成功！")
                Log.d(TAG, "📊 解析结果: 金额=${notification.amountCents}分, 商户='${notification.normalizedMerchant}', 置信度=${notification.confidence}")

                // 记录去重信息
                Log.d(TAG, "📝 记录去重信息: eventKey=$eventKey")
                deduplicationManager.recordProcessed(notification, eventKey)

                // 3. 推荐账户和分类
                Log.d(TAG, "🤖 开始推荐账户和分类...")
                val recommendations = accountCategoryRecommender.recommend(notification)
                Log.d(TAG, "💡 推荐结果: 账户=${recommendations.accountId}, 分类=${recommendations.categoryId}, 推荐置信度=${recommendations.confidence}")
                
                // 4. 根据阈值/金额/开关决定处理方式
                val meetsMinAmount = notification.amountCents >= minAmountCents
                val threshold = autoCreateConfidenceThreshold
                val directionBlockAuto = notification.direction == PaymentDirection.REFUND || notification.direction == PaymentDirection.TRANSFER || notification.direction == PaymentDirection.UNKNOWN
                val canAutoCreate = autoCreateEnabled && meetsMinAmount && notification.confidence >= threshold && !directionBlockAuto
                Log.d(TAG, "🎯 决策: conf=${notification.confidence}, th=$threshold, min_ok=$meetsMinAmount, auto_on=$autoCreateEnabled")
                if (canAutoCreate) {
                    Log.i(TAG, "🚀 满足自动创建条件（conf>=${threshold} 且 金额>=${minAmountCents}分），自动创建交易")
                    createTransactionFromNotification(notification, recommendations, startTime, true)
                } else {
                    Log.i(TAG, "🤔 不满足自动创建条件（或手动关闭），发送确认")
                    val processingTime = System.currentTimeMillis() - startTime
                    recordDebugUseCase.recordSkippedLowConfidence(notification, processingTime)
                    val primaryTx = buildRecommendedTransaction(notification, recommendations)
                    // 生成备用推荐：默认账户/默认分类 组合，作为TopN补充
                    val altTransactions = mutableListOf<Transaction>()
                    try {
                        val defaultAccount = getDefaultAccountId()
                        if (defaultAccount != primaryTx.accountId) {
                            altTransactions += primaryTx.copy(accountId = defaultAccount)
                        }
                    } catch (_: Exception) {}
                    try {
                        val defaultCategory = getDefaultCategoryId(notification.direction)
                        if (defaultCategory != primaryTx.categoryId) {
                            altTransactions += primaryTx.copy(categoryId = defaultCategory)
                        }
                    } catch (_: Exception) {}

                    val txList = listOf(primaryTx) + altTransactions.take(2)
                    notificationManager.showSemiAutoConfirmNotification(notification, txList)
                    _autoLedgerResults.emit(
                        AutoLedgerResult.NeedConfirmation(notification, recommendations)
                    )
                    Log.d(TAG, "📤 已发送确认通知请求")
                }
            }
            is ParseResult.Skipped -> {
                val processingTime = System.currentTimeMillis() - startTime
                Log.i(TAG, "ℹ️ 解析跳过: ${parseResult.reason} (耗时: ${processingTime}ms)")
                // 将跳过视为非交易或条件不满足的场景，记录解析失败以便统计
                recordDebugUseCase.recordParseFailure(
                    mapPackageNameToSourceType(event.packageName),
                    event.title ?: "",
                    event.text ?: "",
                    "解析跳过: ${parseResult.reason}",
                    processingTime
                )
                _autoLedgerResults.emit(
                    AutoLedgerResult.Skipped(event.packageName, parseResult.reason)
                )
            }
            is ParseResult.Failed -> {
                val processingTime = System.currentTimeMillis() - startTime
                Log.w(TAG, "❌ 解析失败: ${parseResult.reason} (耗时: ${processingTime}ms)")
                
                // 记录解析失败
                recordDebugUseCase.recordParseFailure(
                    mapPackageNameToSourceType(event.packageName),
                    event.title ?: "",
                    event.text ?: "",
                    parseResult.reason,
                    processingTime
                )
                
                _autoLedgerResults.emit(
                    AutoLedgerResult.ParseFailed(event.packageName, parseResult.reason)
                )
            }
            is ParseResult.Unsupported -> {
                val processingTime = System.currentTimeMillis() - startTime
                Log.w(TAG, "⚠️ 不支持的通知类型: ${parseResult.reason} (耗时: ${processingTime}ms)")
                
                // 记录不支持的类型
                recordDebugUseCase.recordParseFailure(
                    mapPackageNameToSourceType(event.packageName),
                    event.title ?: "",
                    event.text ?: "",
                    "不支持的通知类型: ${parseResult.reason}",
                    processingTime
                )
                
                _autoLedgerResults.emit(
                    AutoLedgerResult.Skipped(event.packageName, parseResult.reason)
                )
            }
            is ParseResult.Error -> {
                val processingTime = System.currentTimeMillis() - startTime
                Log.e(TAG, "💥 解析异常: ${parseResult.message} (耗时: ${processingTime}ms)")
                
                // 记录解析异常
                recordDebugUseCase.recordParseFailure(
                    mapPackageNameToSourceType(event.packageName),
                    event.title ?: "",
                    event.text ?: "",
                    "解析异常: ${parseResult.message}",
                    processingTime
                )
                
                _autoLedgerResults.emit(
                    AutoLedgerResult.Error("解析异常: ${parseResult.message}")
                )
            }
            
        }
    }
    
    /**
     * 根据支付通知创建交易记录
     */
    private suspend fun createTransactionFromNotification(
        notification: PaymentNotification,
        recommendations: AccountCategoryRecommendation,
        startTime: Long,
        isAutomatic: Boolean
    ) {
        try {
            // 基于“上一次→固定→系统默认”的回退链解析账户/分类/账本
            val resolvedAccount = resolvePreferredAccountId(notification) ?: recommendations.accountId ?: getDefaultAccountId()
            val resolvedCategory = resolvePreferredCategoryId(notification) ?: recommendations.categoryId ?: getDefaultCategoryId(notification.direction)
            val targetLedgerId = resolveLedgerIdSafely()

            // 调用现有的添加交易用例
            // 标准化来源元数据（存入备注，便于撤销识别与排查）
            val sourceMeta = mapOf(
                "sourceApp" to notification.sourceApp,
                "sourceType" to notification.sourceType.name,
                "postedTime" to notification.postedTime,
                "confidence" to notification.confidence,
                "parserVersion" to notification.parserVersion,
                "direction" to notification.direction.name,
                "merchant" to (notification.normalizedMerchant ?: notification.rawMerchant ?: "")
            )
            val metaJson = runCatching { com.google.gson.Gson().toJson(sourceMeta) }.getOrDefault("{}")
            val autoNote = "[AUTO]$metaJson #auto"

            val transactionId = addTransactionUseCase(
                amountCents = notification.amountCents.toInt(),
                categoryId = resolvedCategory,
                note = "自动记账: ${notification.normalizedMerchant ?: "未知商户"} $autoNote",
                accountId = resolvedAccount,
                ledgerId = targetLedgerId
            )
            
            val processingTime = System.currentTimeMillis() - startTime
            
            // 记录成功的调试信息
            recordDebugUseCase.recordSuccess(
                notification, recommendations, transactionId, processingTime, isAutomatic
            )
            
            // 创建简化的Transaction对象用于通知显示
            val transaction = Transaction(
                id = transactionId,
                accountId = resolvedAccount,
                amountCents = notification.amountCents.toInt(),
                categoryId = resolvedCategory,
                note = "自动记账: ${notification.normalizedMerchant ?: "未知商户"} $autoNote",
                ledgerId = targetLedgerId,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                updatedAt = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            )
            
            // 发送自动记账成功通知（全自动模式）
            notificationManager.showAutoLedgerSuccessNotification(transaction, notification)
            
            _autoLedgerResults.emit(
                AutoLedgerResult.Success(transaction, notification, recommendations)
            )

            // 写入“上一次使用”
            saveLastUsed(notification, resolvedAccount, resolvedCategory)
            
        } catch (e: Exception) {
            val processingTime = System.currentTimeMillis() - startTime
            
            // 记录处理失败的调试信息
            recordDebugUseCase.recordProcessFailure(
                notification, "创建交易失败: ${e.message}", processingTime
            )
            
            _autoLedgerResults.emit(
                AutoLedgerResult.Error("创建交易失败: ${e.message}")
            )
        }
    }
    
    /**
     * 映射支付方向到支付类型
     */
    private fun mapDirectionToPaymentType(direction: PaymentDirection): String {
        return when (direction) {
            PaymentDirection.EXPENSE -> "EXPENSE"
            PaymentDirection.INCOME -> "INCOME" 
            PaymentDirection.TRANSFER -> "TRANSFER"
            PaymentDirection.REFUND -> "REFUND"
            PaymentDirection.UNKNOWN -> "EXPENSE" // 默认为支出
        }
    }
    
    /**
     * 获取默认账户ID
     * TODO: 实现正确的账户获取逻辑
     */
    private suspend fun getDefaultAccountId(): String {
        // 优先取仓库的默认账户；否则取第一个可用账户；若仍无则抛出异常
        accountRepository.getDefaultAccount()?.let { return it.id }
        val all = accountRepository.getAccounts().firstOrNull().orEmpty()
        val firstActive = all.firstOrNull()
        return firstActive?.id
            ?: throw IllegalStateException("未找到任何账户，请先创建账户")
    }
    
    /**
     * 根据交易方向获取默认分类ID
     * TODO: 实现正确的分类获取逻辑
     */
    private suspend fun getDefaultCategoryId(direction: PaymentDirection): String {
        // 根据方向选择类型
        val type = when (direction) {
            PaymentDirection.INCOME -> com.ccxiaoji.feature.ledger.domain.model.Category.Type.INCOME
            else -> com.ccxiaoji.feature.ledger.domain.model.Category.Type.EXPENSE
        }
        // 优先选择活跃的叶子（二级）分类；若不存在则回退到第一个活跃父分类
        val list = categoryRepository.getCategoriesByType(type).firstOrNull().orEmpty()
            .filter { it.isActive }
        val leaf = list.firstOrNull { it.level == 2 }
        val fallbackParent = list.firstOrNull { it.level == 1 }
        return (leaf ?: fallbackParent)?.id
            ?: throw IllegalStateException("未找到任何${if (type==com.ccxiaoji.feature.ledger.domain.model.Category.Type.INCOME) "收入" else "支出"}分类")
    }
    
    /**
     * 构建推荐的交易记录（用于半自动模式）
     */
    private suspend fun buildRecommendedTransaction(
        notification: PaymentNotification,
        recommendations: AccountCategoryRecommendation
    ): Transaction {
        return Transaction(
            id = "placeholder", // 占位符，实际创建时由Repository生成
            accountId = recommendations.accountId ?: getDefaultAccountId(),
            amountCents = notification.amountCents.toInt(),
            categoryId = recommendations.categoryId ?: getDefaultCategoryId(notification.direction),
            // 备注不预填，交由用户自行输入
            note = null,
            ledgerId = recommendations.ledgerId,
            createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            updatedAt = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        )
    }
    
    /**
     * 处理半自动模式的手动确认
     */
    suspend fun processManualConfirmation(
        paymentNotification: PaymentNotification,
        recommendedTransaction: Transaction
    ) {
        val startTime = System.currentTimeMillis()
        try {
            // 创建交易记录，使用正确的参数
            val transactionId = addTransactionUseCase(
                amountCents = recommendedTransaction.amountCents,
                categoryId = recommendedTransaction.categoryId,
                note = recommendedTransaction.note,
                accountId = recommendedTransaction.accountId,
                ledgerId = recommendedTransaction.ledgerId
            )
            
            val processingTime = System.currentTimeMillis() - startTime
            
            // 记录手动确认成功
            val recommendations = AccountCategoryRecommendation(
                accountId = recommendedTransaction.accountId,
                categoryId = recommendedTransaction.categoryId,
                ledgerId = recommendedTransaction.ledgerId,
                confidence = 1.0, // 手动确认，置信度为1.0
                reason = "手动确认"
            )
            
            recordDebugUseCase.recordSuccess(
                paymentNotification, recommendations, transactionId, processingTime, false
            )
            
            // 更新Transaction对象的ID
            val confirmedTransaction = recommendedTransaction.copy(id = transactionId)
            
            // 写入“上一次使用”
            saveLastUsed(paymentNotification, recommendedTransaction.accountId, recommendedTransaction.categoryId)

            // 发送确认成功通知
            notificationManager.showAutoLedgerSuccessNotification(confirmedTransaction, paymentNotification)
            
            _autoLedgerResults.emit(
                AutoLedgerResult.ManualConfirmed(confirmedTransaction, paymentNotification)
            )
        } catch (e: Exception) {
            val processingTime = System.currentTimeMillis() - startTime
            
            // 记录手动确认失败
            recordDebugUseCase.recordProcessFailure(
                paymentNotification, "手动确认失败: ${e.message}", processingTime
            )
            
            _autoLedgerResults.emit(
                AutoLedgerResult.Error("手动确认失败: ${e.message}")
            )
        }
    }
    
    /**
     * 将包名映射为支付来源类型
     */
    private fun mapPackageNameToSourceType(packageName: String): com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType {
        return when (packageName) {
            "com.eg.android.AlipayGphone" -> com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.ALIPAY
            "com.tencent.mm" -> com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.WECHAT
            "com.unionpay" -> com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.UNIONPAY
            else -> com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.UNKNOWN
        }
    }

    private fun getDefaultLedgerIdSafe(): String {
        return try {
            val userId = userApi.getCurrentUserId()
            val res = kotlinx.coroutines.runBlocking { manageLedgerUseCase.getDefaultLedger(userId) }
            if (res is com.ccxiaoji.common.base.BaseResult.Success) res.data.id else ""
        } catch (_: Exception) { "" }
    }

    private fun resolveLedgerIdSafely(): String = getDefaultLedgerIdSafe()

    private suspend fun resolvePreferredAccountId(notification: PaymentNotification): String? {
        val source = when (notification.sourceType) {
            com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.ALIPAY -> "alipay"
            com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.WECHAT -> "wechat"
            else -> return null
        }
        val dir = when (notification.direction) {
            PaymentDirection.INCOME -> "INCOME"
            PaymentDirection.EXPENSE -> "EXPENSE"
            else -> return null
        }
        // 细化记忆：按来源+方向+金额段+时段
        val amtBucket = computeAmountBucket(notification.amountCents)
        val timeBucket = computeTimeBucket(notification.postedTime)
        val keyFine = androidx.datastore.preferences.core.stringPreferencesKey(
            "auto_ledger_last_account_${source}_${dir}_${amtBucket}_${timeBucket}"
        )
        val keyCoarse = androidx.datastore.preferences.core.stringPreferencesKey(
            "auto_ledger_last_account_${source}_${dir}"
        )
        val prefs = try { dataStore.data.first() } catch (_: Exception) { null }
        val lastFine = prefs?.get(keyFine)
        if (!lastFine.isNullOrBlank()) return lastFine
        val lastCoarse = prefs?.get(keyCoarse)
        if (!lastCoarse.isNullOrBlank()) return lastCoarse
        return null
    }

    private suspend fun resolvePreferredCategoryId(notification: PaymentNotification): String? {
        val source = when (notification.sourceType) {
            com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.ALIPAY -> "alipay"
            com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.WECHAT -> "wechat"
            else -> return null
        }
        val dir = when (notification.direction) {
            PaymentDirection.INCOME -> "INCOME"
            PaymentDirection.EXPENSE -> "EXPENSE"
            else -> return null
        }
        // 细化记忆：按来源+方向+金额段+时段
        val amtBucket = computeAmountBucket(notification.amountCents)
        val timeBucket = computeTimeBucket(notification.postedTime)
        val keyFine = androidx.datastore.preferences.core.stringPreferencesKey(
            "auto_ledger_last_category_${source}_${dir}_${amtBucket}_${timeBucket}"
        )
        val keyCoarse = androidx.datastore.preferences.core.stringPreferencesKey(
            "auto_ledger_last_category_${source}_${dir}"
        )
        val prefs = try { dataStore.data.first() } catch (_: Exception) { null }
        val lastFine = prefs?.get(keyFine)
        if (!lastFine.isNullOrBlank()) return lastFine
        val lastCoarse = prefs?.get(keyCoarse)
        if (!lastCoarse.isNullOrBlank()) return lastCoarse
        return null
    }

    private fun saveLastUsed(notification: PaymentNotification, accountId: String, categoryId: String) {
        try {
            val source = when (notification.sourceType) {
                com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.ALIPAY -> "alipay"
                com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.WECHAT -> "wechat"
                else -> return
            }
            val dir = when (notification.direction) {
                PaymentDirection.INCOME -> "INCOME"
                PaymentDirection.EXPENSE -> "EXPENSE"
                else -> return
            }
            val amtBucket = computeAmountBucket(notification.amountCents)
            val timeBucket = computeTimeBucket(notification.postedTime)
            val keyAccFine = androidx.datastore.preferences.core.stringPreferencesKey(
                "auto_ledger_last_account_${source}_${dir}_${amtBucket}_${timeBucket}"
            )
            val keyCatFine = androidx.datastore.preferences.core.stringPreferencesKey(
                "auto_ledger_last_category_${source}_${dir}_${amtBucket}_${timeBucket}"
            )
            val keyAccCoarse = androidx.datastore.preferences.core.stringPreferencesKey(
                "auto_ledger_last_account_${source}_${dir}"
            )
            val keyCatCoarse = androidx.datastore.preferences.core.stringPreferencesKey(
                "auto_ledger_last_category_${source}_${dir}"
            )
            kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                dataStore.edit { prefs ->
                    // 同时写细粒度与粗粒度，保证兼容回退
                    prefs[keyAccFine] = accountId
                    prefs[keyCatFine] = categoryId
                    prefs[keyAccCoarse] = accountId
                    prefs[keyCatCoarse] = categoryId
                }
            }
        } catch (_: Exception) { }
    }

    private fun computeAmountBucket(amountCents: Long): String = when {
        amountCents < 5_000L -> "S"      // < ¥50
        amountCents < 50_000L -> "M"     // ¥50–¥500
        else -> "L"                       // ≥ ¥500
    }

    private fun computeTimeBucket(epochMs: Long): String {
        return try {
            val hour = java.util.Calendar.getInstance().apply { timeInMillis = epochMs }.get(java.util.Calendar.HOUR_OF_DAY)
            when (hour) {
                in 6..11 -> "MORNING"
                in 12..17 -> "AFTERNOON"
                in 18..22 -> "EVENING"
                else -> "NIGHT"
            }
        } catch (_: Exception) { "ANY" }
    }
}

/**
 * 自动记账结果密封类
 */
sealed class AutoLedgerResult {
    /**
     * 成功创建交易
     */
    data class Success(
        val transaction: Transaction,
        val notification: PaymentNotification,
        val recommendations: AccountCategoryRecommendation
    ) : AutoLedgerResult()
    
    /**
     * 需要用户确认（低置信度）
     */
    data class NeedConfirmation(
        val notification: PaymentNotification,
        val recommendations: AccountCategoryRecommendation
    ) : AutoLedgerResult()
    
    /**
     * 手动确认成功
     */
    data class ManualConfirmed(
        val transaction: Transaction,
        val notification: PaymentNotification
    ) : AutoLedgerResult()
    
    /**
     * 跳过处理
     */
    data class Skipped(val packageName: String, val reason: String) : AutoLedgerResult()
    
    /**
     * 解析失败
     */
    data class ParseFailed(val packageName: String, val reason: String) : AutoLedgerResult()
    
    /**
     * 处理错误
     */
    data class Error(val message: String) : AutoLedgerResult()
}

/**
 * 账户分类推荐结果
 */
data class AccountCategoryRecommendation(
    val accountId: String?,
    val categoryId: String?,
    val ledgerId: String,
    val confidence: Double,
    val reason: String
)
