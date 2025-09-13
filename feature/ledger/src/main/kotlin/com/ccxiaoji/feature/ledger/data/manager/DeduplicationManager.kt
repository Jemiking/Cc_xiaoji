package com.ccxiaoji.feature.ledger.data.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.ccxiaoji.core.database.dao.AppAutoLedgerConfigDao
import com.ccxiaoji.core.database.dao.AutoLedgerDedupDao
import com.ccxiaoji.core.database.entity.AutoLedgerDedupEntity
import com.ccxiaoji.feature.ledger.domain.model.PaymentNotification
import com.ccxiaoji.feature.ledger.domain.usecase.GenerateEventKeyUseCase
import com.ccxiaoji.shared.notification.domain.model.RawNotificationEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 去重管理器
 * 
 * 负责处理自动记账的去重逻辑，防止重复记账
 */
@Singleton
class DeduplicationManager @Inject constructor(
    private val dedupDao: AutoLedgerDedupDao,
    private val configDao: AppAutoLedgerConfigDao,
    private val generateEventKeyUseCase: GenerateEventKeyUseCase,
    private val dataStore: DataStore<Preferences>
) {
    
    /**
     * 电商应用黑名单
     * 这些应用的通知通常是订单确认，不应该记账
     */
    private val ecommerceBlacklist = setOf(
        "com.taobao.taobao",      // 淘宝
        "com.tmall.wireless",     // 天猫
        "com.jingdong.app.mall",  // 京东
        "com.suning.mobile.ebuy", // 苏宁
        "com.xunmeng.pinduoduo",  // 拼多多
        "com.amazon.mShop.android.shopping", // 亚马逊
        "com.dangdang.buy2"       // 当当
    )
    
    /**
     * 电商订单关键词黑名单
     */
    private val ecommerceOrderKeywords = setOf(
        "订单", "下单", "已下单",
        "商品", "订单确认", "购物", "发货", "物流", "包裹", "配送", "签收"
    )

    // 强支付关键词白名单：命中则不因电商关键词而拦截
    private val strongPaymentKeywords = setOf(
        "支付", "付款", "扣款", "支付成功", "已支付", "收款", "已收款", "到账", "入账"
    )
    
    /**
     * 检查原始通知事件是否应该被处理
     * 
     * @param event 原始通知事件
     * @return 处理决策
     */
    suspend fun shouldProcess(event: RawNotificationEvent): ProcessDecision = withContext(Dispatchers.IO) {
        try {
            // 0. 读取运行时去重配置
            val prefs = try { dataStore.data.first() } catch (_: Exception) { null }
            val dedupEnabled = prefs?.get(booleanPreferencesKey("auto_ledger_dedup_enabled")) ?: true
            val windowSec = (prefs?.get(intPreferencesKey("auto_ledger_dedup_window_sec")) ?: 20).coerceIn(1, 600)

            // 1. 检查是否在电商黑名单中
            if (event.packageName in ecommerceBlacklist) {
                return@withContext ProcessDecision.Skip("电商应用，忽略订单通知")
            }
            
            // 2. 检查是否包含电商订单关键词（强支付关键词白名单兜底）
            val content = "${event.title.orEmpty()} ${event.text.orEmpty()}".lowercase()
            val hasEcommerce = ecommerceOrderKeywords.any { content.contains(it) }
            val hasStrongPay = strongPaymentKeywords.any { content.contains(it) }
            if (hasEcommerce && !hasStrongPay) {
                return@withContext ProcessDecision.Skip("包含电商订单关键词")
            }
            
            // 3. 检查群组摘要通知（WeChat强支付关键词例外）
            if (event.isGroupSummary) {
                val strongWeChatKeywords = setOf("红包", "转账", "收款", "已收款")
                val allowWeChatGroup = event.packageName == "com.tencent.mm" &&
                        strongWeChatKeywords.any { (event.title.orEmpty() + " " + event.text.orEmpty()).contains(it) }
                if (!allowWeChatGroup) {
                    return@withContext ProcessDecision.Skip("群组摘要通知，忽略")
                }
            }

            // 4. 原始文本短窗去重（双阶段第一步）
            if (dedupEnabled) {
                val content = (event.title.orEmpty() + " " + event.text.orEmpty()).trim().lowercase()
                val textHash = generateEventKeyUseCase.generateTextHash(content)
                val windowMs = windowSec * 1000L
                val windowStart = event.postTime - windowMs
                val windowEnd = event.postTime + windowMs
                val recent = dedupDao.findByPackageAndTimeRange(event.packageName, windowStart, windowEnd)
                val hit = recent.firstOrNull { it.textHash == textHash }
                if (hit != null) {
                    val delta = kotlin.math.abs(event.postTime - hit.postTime)
                    return@withContext ProcessDecision.Skip("dedup_within_window(delta=${delta}ms,window=${windowSec}s)")
                }
                // 同时基于可配置桶生成raw事件键，避免重复插入
                val eventKeyRaw = generateEventKeyUseCase.generateForRawEvent(event, windowSec)
                if (dedupDao.exists(eventKeyRaw)) {
                    return@withContext ProcessDecision.Skip("dedup_event_key_exists(window=${windowSec}s)")
                }
            }
            
            // 5. 检查应用配置
            val config = configDao.getByPackage(event.packageName)
            if (config?.mode == 0) {
                return@withContext ProcessDecision.Skip("应用已禁用自动记账")
            }
            
            // 6. 检查应用自定义黑名单
            config?.blacklist?.let { blacklistJson ->
                if (matchesBlacklist(content, blacklistJson)) {
                    return@withContext ProcessDecision.Skip("命中应用自定义黑名单")
                }
            }
            
            // 7. 简单频控：在窗口内若事件过多则跳过（防抖保护，使用同一窗口配置）
            if (dedupEnabled) {
                val windowMs = windowSec * 1000L
                val start = event.postTime - windowMs
                val end = event.postTime + windowMs
                val recentEvents = dedupDao.findByPackageAndTimeRange(event.packageName, start, end)
                if (recentEvents.size >= 10) {
                    return@withContext ProcessDecision.Skip("too_many_events_in_window(window=${windowSec}s,count=${recentEvents.size})")
                }
            }

            // 8. 放行，附带一个预生成的raw事件键（供记录时参考）
            val eventKey = generateEventKeyUseCase.generateForRawEvent(event, windowSec)
            ProcessDecision.Process(eventKey)
            
        } catch (e: Exception) {
            ProcessDecision.Error("去重检查异常: ${e.message}")
        }
    }
    
    /**
     * 记录已处理的支付通知
     * 
     * @param notification 支付通知
     * @param eventKey 事件指纹
     * @return 是否成功记录
     */
    suspend fun recordProcessed(
        notification: PaymentNotification,
        @Suppress("UNUSED_PARAMETER") eventKey: String // 兼容旧签名，内部使用解析后指纹
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val prefs = try { dataStore.data.first() } catch (_: Exception) { null }
            val windowSec = (prefs?.get(intPreferencesKey("auto_ledger_dedup_window_sec")) ?: 20).coerceIn(1, 600)
            // 解析后指纹（双阶段第二步）
            val parsedKey = generateEventKeyUseCase.generateForPaymentNotification(notification, windowSec)
            val dedupEntity = AutoLedgerDedupEntity(
                eventKey = parsedKey,
                createdAt = System.currentTimeMillis(),
                packageName = notification.sourceApp,
                textHash = generateEventKeyUseCase.generateTextHash(notification.rawText),
                amountCents = notification.amountCents,
                merchantHash = generateEventKeyUseCase.generateMerchantHash(notification.normalizedMerchant),
                postTime = notification.postedTime
            )
            
            val result = dedupDao.insert(dedupEntity)
            result > 0 // 返回是否插入成功
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 清理过期的去重记录
     * 
     * @param daysToKeep 保留天数
     * @return 清理的记录数
     */
    suspend fun cleanupExpiredRecords(daysToKeep: Int = 30): Int = withContext(Dispatchers.IO) {
        val expiredBefore = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        dedupDao.cleanup(expiredBefore)
    }

    /**
     * 清空去重缓存
     */
    suspend fun clearAll(): Int = withContext(Dispatchers.IO) {
        dedupDao.clearAll()
    }
    
    /**
     * 获取去重统计信息
     */
    suspend fun getStatistics(): DeduplicationStats = withContext(Dispatchers.IO) {
        try {
            val totalCount = dedupDao.count()
            val packageStats = dedupDao.getStatsByPackage()
            
            DeduplicationStats(
                totalRecords = totalCount,
                packageStats = packageStats.map { 
                    PackageDeduplicationStats(it.packageName, it.count) 
                }
            )
        } catch (e: Exception) {
            DeduplicationStats(0, emptyList())
        }
    }
    
    /**
     * 检查文本是否匹配黑名单
     */
    private fun matchesBlacklist(content: String, blacklistJson: String): Boolean {
        try {
            // 简化的JSON解析，实际可能需要使用JSON库
            val keywords = blacklistJson
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .split(",")
                .map { it.trim() }
            
            return keywords.any { keyword -> 
                keyword.isNotBlank() && content.contains(keyword.lowercase()) 
            }
        } catch (e: Exception) {
            return false
        }
    }
}

/**
 * 处理决策密封类
 */
sealed class ProcessDecision {
    /**
     * 应该处理
     */
    data class Process(val eventKey: String) : ProcessDecision()
    
    /**
     * 跳过处理
     */
    data class Skip(val reason: String) : ProcessDecision()
    
    /**
     * 处理错误
     */
    data class Error(val message: String) : ProcessDecision()
}

/**
 * 去重统计信息
 */
data class DeduplicationStats(
    val totalRecords: Int,
    val packageStats: List<PackageDeduplicationStats>
)

/**
 * 应用包名去重统计
 */
data class PackageDeduplicationStats(
    val packageName: String,
    val recordCount: Int
)
