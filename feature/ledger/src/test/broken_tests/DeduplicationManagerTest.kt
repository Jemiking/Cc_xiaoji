package com.ccxiaoji.feature.ledger.data.manager

import android.os.Bundle
import com.ccxiaoji.core.database.dao.AppAutoLedgerConfigDao
import com.ccxiaoji.core.database.dao.AutoLedgerDedupDao
import com.ccxiaoji.core.database.entity.AppAutoLedgerConfigEntity
import com.ccxiaoji.core.database.entity.AutoLedgerDedupEntity
import com.ccxiaoji.feature.ledger.domain.model.PaymentDirection
import com.ccxiaoji.feature.ledger.domain.model.PaymentNotification
import com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType
import com.ccxiaoji.feature.ledger.domain.usecase.GenerateEventKeyUseCase
import com.ccxiaoji.shared.notification.domain.model.RawNotificationEvent
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * 去重管理器测试
 * 
 * 验证自动记账的去重逻辑，防止重复记账
 * 测试覆盖：黑名单过滤、事件指纹去重、时间窗口检查
 */
class DeduplicationManagerTest {
    
    private lateinit var deduplicationManager: DeduplicationManager
    private val dedupDao = mockk<AutoLedgerDedupDao>()
    private val configDao = mockk<AppAutoLedgerConfigDao>()
    private val generateEventKeyUseCase = mockk<GenerateEventKeyUseCase>()
    
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        deduplicationManager = DeduplicationManager(
            dedupDao = dedupDao,
            configDao = configDao,
            generateEventKeyUseCase = generateEventKeyUseCase
        )
    }
    
    @Test
    fun `测试电商应用黑名单过滤`() = runTest {
        // Given - 电商应用通知
        val ecommercePackages = listOf(
            "com.taobao.taobao",      // 淘宝
            "com.tmall.wireless",     // 天猫
            "com.jingdong.app.mall",  // 京东
            "com.xunmeng.pinduoduo"   // 拼多多
        )
        
        ecommercePackages.forEach { packageName ->
            val event = createTestEvent(
                packageName = packageName,
                title = "订单通知",
                text = "您的订单已确认，金额28.50元"
            )
            
            // When
            val decision = deduplicationManager.shouldProcess(event)
            
            // Then
            assertTrue("电商应用应被过滤", decision is ProcessDecision.Skip)
            assertEquals("过滤原因应为电商应用", 
                "电商应用，忽略订单通知", 
                (decision as ProcessDecision.Skip).reason)
        }
    }
    
    @Test
    fun `测试电商订单关键词过滤`() = runTest {
        // Given - 包含电商关键词的通知
        val orderKeywords = listOf(
            "订单确认28.50元",
            "购买成功，商品已下单",
            "支付完成，发货通知",
            "您的商品已发货"
        )
        
        orderKeywords.forEach { text ->
            val event = createTestEvent(
                packageName = "com.example.app",
                title = "支付通知",
                text = text
            )
            
            // When
            val decision = deduplicationManager.shouldProcess(event)
            
            // Then
            assertTrue("包含订单关键词应被过滤: $text", decision is ProcessDecision.Skip)
            assertEquals("过滤原因应为电商订单关键词", 
                "包含电商订单关键词", 
                (decision as ProcessDecision.Skip).reason)
        }
    }
    
    @Test
    fun `测试群组摘要通知过滤`() = runTest {
        // Given - 群组摘要通知
        val event = createTestEvent(
            packageName = "com.eg.android.AlipayGphone",
            title = "支付宝",
            text = "您收到3条新消息",
            isGroupSummary = true
        )
        
        // When
        val decision = deduplicationManager.shouldProcess(event)
        
        // Then
        assertTrue("群组摘要通知应被过滤", decision is ProcessDecision.Skip)
        assertEquals("过滤原因应为群组摘要", 
            "群组摘要通知，忽略", 
            (decision as ProcessDecision.Skip).reason)
    }
    
    @Test
    fun `测试事件指纹去重`() = runTest {
        // Given - 相同的通知事件
        val event = createTestEvent(
            packageName = "com.eg.android.AlipayGphone",
            title = "支付宝",
            text = "向【星巴克咖啡】付款28.50元"
        )
        
        val eventKey = "test_event_key_12345"
        
        coEvery { generateEventKeyUseCase.generateForRawEvent(event) } returns eventKey
        coEvery { dedupDao.exists(eventKey) } returns true
        
        // When
        val decision = deduplicationManager.shouldProcess(event)
        
        // Then
        assertTrue("已存在的事件应被去重", decision is ProcessDecision.Skip)
        assertEquals("去重原因应为事件已处理", 
            "事件已处理，去重命中", 
            (decision as ProcessDecision.Skip).reason)
        
        verify { generateEventKeyUseCase.generateForRawEvent(event) }
        verify { dedupDao.exists(eventKey) }
    }
    
    @Test
    fun `测试应用禁用自动记账`() = runTest {
        // Given - 应用禁用自动记账配置
        val event = createTestEvent(
            packageName = "com.tencent.mm",
            title = "微信支付",
            text = "向商家付款28.50元"
        )
        
        val eventKey = "test_event_key_12345"
        val disabledConfig = AppAutoLedgerConfigEntity(
            appPkg = "com.tencent.mm",
            mode = 0, // 禁用模式
            blacklist = null,
            whitelist = null,
            amountWindowSec = 300,
            confidenceThreshold = 0.85,
            accountRules = null,
            categoryRules = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { generateEventKeyUseCase.generateForRawEvent(event) } returns eventKey
        coEvery { dedupDao.exists(eventKey) } returns false
        coEvery { configDao.getByPackage("com.tencent.mm") } returns disabledConfig
        
        // When
        val decision = deduplicationManager.shouldProcess(event)
        
        // Then
        assertTrue("禁用应用应被跳过", decision is ProcessDecision.Skip)
        assertEquals("跳过原因应为应用禁用", 
            "应用已禁用自动记账", 
            (decision as ProcessDecision.Skip).reason)
    }
    
    @Test
    fun `测试自定义黑名单过滤`() = runTest {
        // Given - 应用配置自定义黑名单
        val event = createTestEvent(
            packageName = "com.example.payment",
            title = "支付通知",
            text = "系统维护通知，请稍后再试"
        )
        
        val eventKey = "test_event_key_12345"
        val configWithBlacklist = AppAutoLedgerConfigEntity(
            appPkg = "com.example.payment",
            mode = 1, // 启用模式
            blacklist = "[\"系统维护\", \"维护通知\", \"稍后再试\"]",
            whitelist = null,
            amountWindowSec = 300,
            confidenceThreshold = 0.85,
            accountRules = null,
            categoryRules = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { generateEventKeyUseCase.generateForRawEvent(event) } returns eventKey
        coEvery { dedupDao.exists(eventKey) } returns false
        coEvery { configDao.getByPackage("com.example.payment") } returns configWithBlacklist
        
        // When
        val decision = deduplicationManager.shouldProcess(event)
        
        // Then
        assertTrue("自定义黑名单应被过滤", decision is ProcessDecision.Skip)
        assertEquals("过滤原因应为自定义黑名单", 
            "命中应用自定义黑名单", 
            (decision as ProcessDecision.Skip).reason)
    }
    
    @Test
    fun `测试时间窗口内事件过多过滤`() = runTest {
        // Given - 时间窗口内有太多事件
        val event = createTestEvent(
            packageName = "com.eg.android.AlipayGphone",
            title = "支付宝",
            text = "向【商户】付款28.50元"
        )
        
        val eventKey = "test_event_key_12345"
        val windowSec = 300
        val windowStart = event.postTime - (windowSec * 1000)
        val windowEnd = event.postTime + (windowSec * 1000)
        
        // 模拟时间窗口内有3个或更多事件
        val recentEvents = listOf(
            mockk<AutoLedgerDedupEntity>(),
            mockk<AutoLedgerDedupEntity>(),
            mockk<AutoLedgerDedupEntity>()
        )
        
        coEvery { generateEventKeyUseCase.generateForRawEvent(event) } returns eventKey
        coEvery { dedupDao.exists(eventKey) } returns false
        coEvery { configDao.getByPackage(event.packageName) } returns null
        coEvery { dedupDao.findByPackageAndTimeRange(
            event.packageName, windowStart, windowEnd
        ) } returns recentEvents
        
        // When
        val decision = deduplicationManager.shouldProcess(event)
        
        // Then
        assertTrue("时间窗口内事件过多应被过滤", decision is ProcessDecision.Skip)
        assertEquals("过滤原因应为事件过多", 
            "时间窗口内事件过多，可能异常", 
            (decision as ProcessDecision.Skip).reason)
    }
    
    @Test
    fun `测试正常支付事件应该处理`() = runTest {
        // Given - 正常的支付通知
        val event = createTestEvent(
            packageName = "com.eg.android.AlipayGphone",
            title = "支付宝",
            text = "向【星巴克咖啡】付款28.50元"
        )
        
        val eventKey = "test_event_key_12345"
        val windowSec = 300
        val windowStart = event.postTime - (windowSec * 1000)
        val windowEnd = event.postTime + (windowSec * 1000)
        
        coEvery { generateEventKeyUseCase.generateForRawEvent(event) } returns eventKey
        coEvery { dedupDao.exists(eventKey) } returns false
        coEvery { configDao.getByPackage(event.packageName) } returns null
        coEvery { dedupDao.findByPackageAndTimeRange(
            event.packageName, windowStart, windowEnd
        ) } returns emptyList()
        
        // When
        val decision = deduplicationManager.shouldProcess(event)
        
        // Then
        assertTrue("正常支付事件应该被处理", decision is ProcessDecision.Process)
        assertEquals("应返回事件key", eventKey, (decision as ProcessDecision.Process).eventKey)
    }
    
    @Test
    fun `测试记录已处理事件`() = runTest {
        // Given - 支付通知和事件key
        val notification = PaymentNotification(
            sourceApp = "com.eg.android.AlipayGphone",
            sourceType = PaymentSourceType.ALIPAY,
            direction = PaymentDirection.EXPENSE,
            amountCents = 2850L,
            rawMerchant = "星巴克咖啡",
            normalizedMerchant = "星巴克",
            paymentMethod = "余额宝",
            postedTime = System.currentTimeMillis(),
            notificationKey = "test_notification_key",
            confidence = 0.95,
            rawText = "向【星巴克咖啡】付款28.50元",
            originalTitle = "支付宝",
            originalText = "向【星巴克咖啡】付款28.50元",
            fingerprint = "test_fingerprint"
        )
        
        val eventKey = "test_event_key_12345"
        val insertResult = 1L // 成功插入
        
        coEvery { generateEventKeyUseCase.generateTextHash(notification.rawText!!) } returns "text_hash_123"
        coEvery { generateEventKeyUseCase.generateMerchantHash(notification.normalizedMerchant!!) } returns "merchant_hash_456"
        coEvery { dedupDao.insert(any<AutoLedgerDedupEntity>()) } returns insertResult
        
        // When
        val result = deduplicationManager.recordProcessed(notification, eventKey)
        
        // Then
        assertTrue("应成功记录已处理事件", result)
        
        verify { dedupDao.insert(match<AutoLedgerDedupEntity> { entity ->
            entity.eventKey == eventKey &&
            entity.packageName == notification.sourceApp &&
            entity.amountCents == notification.amountCents &&
            entity.postTime == notification.postedTime
        }) }
    }
    
    @Test
    fun `测试清理过期记录`() = runTest {
        // Given - 清理30天前的记录
        val daysToKeep = 30
        val deletedCount = 15
        
        coEvery { dedupDao.cleanup(any()) } returns deletedCount
        
        // When
        val result = deduplicationManager.cleanupExpiredRecords(daysToKeep)
        
        // Then
        assertEquals("应返回删除的记录数", deletedCount, result)
        
        coVerify { dedupDao.cleanup(match { timestamp ->
            val expectedTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
            // 允许一定的时间误差（1秒）
            kotlin.math.abs(timestamp - expectedTime) < 1000L
        }) }
    }
    
    @Test
    fun `测试获取去重统计信息`() = runTest {
        // Given - 统计信息
        val totalCount = 100
        val mockPackageStats = listOf(
            mockk<AutoLedgerDedupDao.PackageStats>().apply {
                every { packageName } returns "com.eg.android.AlipayGphone"
                every { count } returns 60
            },
            mockk<AutoLedgerDedupDao.PackageStats>().apply {
                every { packageName } returns "com.tencent.mm"
                every { count } returns 40
            }
        )
        
        coEvery { dedupDao.count() } returns totalCount
        coEvery { dedupDao.getStatsByPackage() } returns mockPackageStats
        
        // When
        val stats = deduplicationManager.getStatistics()
        
        // Then
        assertEquals("总记录数应正确", totalCount, stats.totalRecords)
        assertEquals("应包含2个应用统计", 2, stats.packageStats.size)
        
        val alipayStats = stats.packageStats.find { it.packageName == "com.eg.android.AlipayGphone" }
        assertNotNull("应包含支付宝统计", alipayStats)
        assertEquals("支付宝记录数应正确", 60, alipayStats!!.recordCount)
        
        val wechatStats = stats.packageStats.find { it.packageName == "com.tencent.mm" }
        assertNotNull("应包含微信统计", wechatStats)
        assertEquals("微信记录数应正确", 40, wechatStats!!.recordCount)
    }
    
    @Test
    fun `测试异常处理`() = runTest {
        // Given - DAO抛出异常
        val event = createTestEvent(
            packageName = "com.eg.android.AlipayGphone",
            title = "支付宝",
            text = "向【星巴克咖啡】付款28.50元"
        )
        
        coEvery { generateEventKeyUseCase.generateForRawEvent(event) } throws RuntimeException("Database error")
        
        // When
        val decision = deduplicationManager.shouldProcess(event)
        
        // Then
        assertTrue("异常时应返回错误决策", decision is ProcessDecision.Error)
        assertTrue("错误信息应包含异常信息", 
            (decision as ProcessDecision.Error).message.contains("去重检查异常"))
    }
    
    /**
     * 创建测试用的原始通知事件
     */
    private fun createTestEvent(
        packageName: String,
        title: String,
        text: String,
        isGroupSummary: Boolean = false
    ): RawNotificationEvent {
        return RawNotificationEvent(
            packageName = packageName,
            title = title,
            text = text,
            extras = Bundle(),
            postTime = System.currentTimeMillis(),
            notificationKey = "test_key_${System.nanoTime()}",
            isGroupSummary = isGroupSummary
        )
    }
}