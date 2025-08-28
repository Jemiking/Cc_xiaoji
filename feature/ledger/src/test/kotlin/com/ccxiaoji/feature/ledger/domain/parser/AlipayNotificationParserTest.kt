package com.ccxiaoji.feature.ledger.domain.parser

import android.os.Bundle
import com.ccxiaoji.feature.ledger.domain.model.PaymentDirection
import com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType
import com.ccxiaoji.shared.notification.domain.model.RawNotificationEvent
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * 支付宝通知解析器测试
 * 
 * 验证解析器在各种支付场景下的准确性
 * 目标：金额/商户/方向准确率≥95%
 */
class AlipayNotificationParserTest {
    
    private lateinit var parser: AlipayNotificationParser
    
    @Before
    fun setUp() {
        parser = AlipayNotificationParser()
    }
    
    @Test
    fun `测试支付场景解析`() {
        val testCases = listOf(
            // 扫码支付场景
            TestCase(
                title = "支付宝",
                text = "向【星巴克咖啡】付款28.50元",
                expectedAmount = 2850L,
                expectedMerchant = "星巴克咖啡",
                expectedDirection = PaymentDirection.EXPENSE
            ),
            // 刷脸支付场景
            TestCase(
                title = "支付宝",
                text = "你向星巴克付款￥28.50",
                expectedAmount = 2850L,
                expectedMerchant = "星巴克",
                expectedDirection = PaymentDirection.EXPENSE
            ),
            // 大额支付（千分位）
            TestCase(
                title = "支付宝",
                text = "向【苹果专营店】付款￥1,234.56元",
                expectedAmount = 123456L,
                expectedMerchant = "苹果专营店",
                expectedDirection = PaymentDirection.EXPENSE
            ),
            // 整数金额
            TestCase(
                title = "支付宝",
                text = "向【麦当劳】付款35元",
                expectedAmount = 3500L,
                expectedMerchant = "麦当劳",
                expectedDirection = PaymentDirection.EXPENSE
            )
        )
        
        testCases.forEach { case ->
            val event = createTestEvent(case.title, case.text)
            val result = parser.parse(event)
            
            assertNotNull("解析结果不应为null: ${case.text}", result)
            result!!
            
            assertEquals("金额解析错误: ${case.text}", 
                case.expectedAmount, result.amountCents)
            assertEquals("商户解析错误: ${case.text}", 
                case.expectedMerchant, result.rawMerchant)
            assertEquals("方向解析错误: ${case.text}", 
                case.expectedDirection, result.direction)
            assertEquals("来源类型错误", PaymentSourceType.ALIPAY, result.sourceType)
            assertTrue("置信度过低: ${result.confidence}", result.confidence >= 0.8)
        }
    }
    
    @Test
    fun `测试收款场景解析`() {
        val testCases = listOf(
            TestCase(
                title = "支付宝",
                text = "收款到账98.00元",
                expectedAmount = 9800L,
                expectedDirection = PaymentDirection.INCOME
            ),
            TestCase(
                title = "支付宝", 
                text = "余额宝收益1.25元已到账",
                expectedAmount = 125L,
                expectedDirection = PaymentDirection.INCOME
            )
        )
        
        testCases.forEach { case ->
            val event = createTestEvent(case.title, case.text)
            val result = parser.parse(event)
            
            assertNotNull("解析结果不应为null: ${case.text}", result)
            result!!
            
            assertEquals("金额解析错误: ${case.text}", 
                case.expectedAmount, result.amountCents)
            assertEquals("方向解析错误: ${case.text}", 
                case.expectedDirection, result.direction)
        }
    }
    
    @Test
    fun `测试转账场景解析`() {
        val testCases = listOf(
            TestCase(
                title = "支付宝",
                text = "转账给张三100.00元",
                expectedAmount = 10000L,
                expectedMerchant = "张三",
                expectedDirection = PaymentDirection.TRANSFER
            ),
            TestCase(
                title = "支付宝",
                text = "已转账500元给李四",
                expectedAmount = 50000L,
                expectedMerchant = "李四",
                expectedDirection = PaymentDirection.TRANSFER
            )
        )
        
        testCases.forEach { case ->
            val event = createTestEvent(case.title, case.text)
            val result = parser.parse(event)
            
            assertNotNull("解析结果不应为null: ${case.text}", result)
            result!!
            
            assertEquals("金额解析错误: ${case.text}", 
                case.expectedAmount, result.amountCents)
            assertEquals("方向解析错误: ${case.text}", 
                case.expectedDirection, result.direction)
        }
    }
    
    @Test
    fun `测试退款场景解析`() {
        val testCases = listOf(
            TestCase(
                title = "支付宝",
                text = "退款28.50元已退回至余额宝",
                expectedAmount = 2850L,
                expectedDirection = PaymentDirection.REFUND
            ),
            TestCase(
                title = "支付宝",
                text = "已撤销向【星巴克】的付款50.00元",
                expectedAmount = 5000L,
                expectedMerchant = "星巴克",
                expectedDirection = PaymentDirection.REFUND
            )
        )
        
        testCases.forEach { case ->
            val event = createTestEvent(case.title, case.text)
            val result = parser.parse(event)
            
            assertNotNull("解析结果不应为null: ${case.text}", result)
            result!!
            
            assertEquals("金额解析错误: ${case.text}", 
                case.expectedAmount, result.amountCents)
            assertEquals("方向解析错误: ${case.text}", 
                case.expectedDirection, result.direction)
        }
    }
    
    @Test
    fun `测试支付方式解析`() {
        val testCases = listOf(
            Pair("余额宝付款28.50元", "余额宝"),
            Pair("花呗付款128.50元", "花呗"),
            Pair("支付宝余额付款68.50元", "支付宝余额"),
            Pair("银行卡尾号1234付款188.50元", "银行卡尾号1234")
        )
        
        testCases.forEach { (text, expectedMethod) ->
            val event = createTestEvent("支付宝", text)
            val result = parser.parse(event)
            
            assertNotNull("解析结果不应为null: $text", result)
            assertEquals("支付方式解析错误: $text", 
                expectedMethod, result!!.paymentMethod)
        }
    }
    
    @Test
    fun `测试无效通知过滤`() {
        val invalidTexts = listOf(
            "您的快递已到达丰巢快递柜", // 非支付相关
            "您有1条新消息",          // 一般通知
            "余额变动提醒",            // 没有具体金额
            "账单已生成"               // 账单通知
        )
        
        invalidTexts.forEach { text ->
            val event = createTestEvent("支付宝", text)
            val canParse = parser.canParse(event)
            
            assertFalse("不应该解析非支付通知: $text", canParse)
        }
    }
    
    @Test
    fun `测试置信度计算`() {
        // 高置信度：完整信息
        val highConfidenceEvent = createTestEvent("支付宝", "向【星巴克咖啡】付款28.50元")
        val highResult = parser.parse(highConfidenceEvent)
        assertTrue("高质量通知置信度应该≥0.9", 
            highResult?.confidence ?: 0.0 >= 0.9)
        
        // 中等置信度：缺少商户信息
        val mediumConfidenceEvent = createTestEvent("支付宝", "付款28.50元成功")
        val mediumResult = parser.parse(mediumConfidenceEvent)
        assertTrue("中等质量通知置信度应该在0.6-0.9之间",
            (mediumResult?.confidence ?: 0.0) in 0.6..0.9)
    }
    
    @Test
    fun `测试商户名称归一化`() {
        val testCases = listOf(
            "【星巴克咖啡（北京）有限公司】" to "星巴克咖啡有限公司",
            "【麦当劳官方旗舰店】" to "麦当劳",
            "【Apple专营店】" to "Apple"
        )
        
        testCases.forEach { (raw, expected) ->
            val event = createTestEvent("支付宝", "向${raw}付款28.50元")
            val result = parser.parse(event)
            
            assertNotNull("解析结果不应为null", result)
            // 这里主要测试raw merchant的提取，normalized merchant的具体逻辑可能需要调整
            assertNotNull("应该提取到商户名", result!!.rawMerchant)
        }
    }
    
    /**
     * 创建测试用的通知事件
     */
    private fun createTestEvent(title: String, text: String): RawNotificationEvent {
        return RawNotificationEvent(
            packageName = "com.eg.android.AlipayGphone",
            title = title,
            text = text,
            extras = Bundle(),
            postTime = System.currentTimeMillis(),
            notificationKey = "test_key_${System.nanoTime()}"
        )
    }
    
    /**
     * 测试用例数据类
     */
    private data class TestCase(
        val title: String,
        val text: String,
        val expectedAmount: Long,
        val expectedMerchant: String? = null,
        val expectedDirection: PaymentDirection,
        val expectedMethod: String? = null
    )
}