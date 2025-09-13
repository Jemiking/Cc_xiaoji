package com.ccxiaoji.feature.ledger.domain.parser

import android.util.Log
import com.ccxiaoji.feature.ledger.domain.model.PaymentDirection
import com.ccxiaoji.feature.ledger.domain.model.PaymentNotification
import com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType
import com.ccxiaoji.shared.notification.domain.model.RawNotificationEvent

/**
 * 微信通知解析器（基础版）
 *
 * 目标：尽快支持常见“收/付”场景，满足两挡模式与撤销闭环。
 * 策略：以金额+方向为核心判定，辅以简单商户与方式提取，低置信度直接跳过。
 */
class WechatNotificationParser : AbstractNotificationParser() {

    companion object { private const val TAG = "AutoLedger_Parser_WeChat" }

    override val supportedPackages = setOf("com.tencent.mm")
    override val parserName = "WechatNotificationParser"
    override val version = 1

    /**
     * 放宽 canParse：只要是微信包名即可进入 parse，由 parse 再基于金额/方向决定有效性
     */
    override fun canParse(event: RawNotificationEvent): Boolean {
        return event.packageName in supportedPackages
    }

    // 关键词扩充：覆盖更多常见变体
    private val expenseKeywords = setOf(
        "付款", "支付成功", "已支付", "扣款", "扣费", "支付¥", "支付￥", "消费", "支出"
    )
    private val incomeKeywords = setOf(
        "收款", "已收款", "到账", "入账", "转入", "已收钱", "零钱入账", "红包入账"
    )
    private val transferKeywords = setOf(
        "转账", "转出", "向.*?转账", "收到.*?转账"
    )
    private val refundKeywords = setOf(
        "退款", "已退回", "已退款", "退款成功", "退回银行卡", "退回零钱"
    )

    override fun parse(event: RawNotificationEvent): PaymentNotification? {
        val title = event.title.orEmpty()
        val text = event.text.orEmpty()
        val fullText = "$title $text"

        Log.d(TAG, "[WeChat] 输入: title='$title', text='$text'")

        // 快速过滤明显非交易通知
        if (isInvalidWeChatNotification(fullText)) {
            Log.v(TAG, "[WeChat] 非交易类通知，跳过")
            return null
        }

        // 金额（支持¥/￥/元与千分位）
        val amountCents = extractAmount(fullText)
        if (amountCents == null || amountCents <= 0) {
            Log.v(TAG, "[WeChat] 未提取到金额，跳过")
            return null
        }

        // 商户/对方
        val rawMerchant = extractMerchant(fullText) ?: extractWeChatPeer(fullText)
        val normalizedMerchant = normalizeMerchant(rawMerchant)

        // 方向
        val direction = inferWeChatDirection(fullText)

        // 方式（零钱/银行卡等）
        val method = inferPaymentMethod(fullText)

        // 置信度（金额+方向为核心，商户与方式加分）
        val confidence = calculateConfidence(
            hasAmount = amountCents > 0,
            hasMerchant = !normalizedMerchant.isNullOrBlank(),
            hasDirection = direction != PaymentDirection.UNKNOWN,
            hasMethod = method != null
        )

        if (confidence < 0.6) {
            Log.v(TAG, "[WeChat] 置信度过低($confidence)，跳过")
            return null
        }

        return PaymentNotification(
            sourceApp = event.packageName,
            sourceType = PaymentSourceType.WECHAT,
            direction = direction,
            amountCents = amountCents,
            rawMerchant = rawMerchant,
            normalizedMerchant = normalizedMerchant,
            paymentMethod = method,
            postedTime = event.postTime,
            notificationKey = event.notificationKey,
            parserVersion = version,
            confidence = confidence,
            rawText = if (confidence < 0.8) fullText else null,
            originalTitle = title,
            originalText = text,
            fingerprint = generateFingerprint(event),
            tags = emptySet()
        )
    }

    private fun generateFingerprint(event: RawNotificationEvent): String {
        val content = "${event.title ?: ""}_${event.text ?: ""}"
        return "${event.packageName}_${event.postTime}_${content.hashCode()}"
    }

    private fun isInvalidWeChatNotification(fullText: String): Boolean {
        val t = fullText.lowercase()
        // 常见非交易类
        val exclude = setOf("红包封面", "活动", "快递", "物流", "系统维护", "消息", "邀请", "天气", "新闻", "权限")
        return exclude.any { t.contains(it) }
    }

    private fun inferWeChatDirection(text: String): PaymentDirection {
        val t = text
        val dir = when {
            refundKeywords.any { t.contains(it) } -> PaymentDirection.REFUND
            incomeKeywords.any { t.contains(it) } -> PaymentDirection.INCOME
            transferKeywords.any { t.contains(it) } -> PaymentDirection.TRANSFER
            expenseKeywords.any { t.contains(it) } -> PaymentDirection.EXPENSE
            else -> null
        }
        return dir ?: inferDirection(null, text)
    }

    /**
     * 常见微信通知对方/商户提取
     * 例：向[张三]转账/已收款[李四]/向【肯德基】付款
     */
    private fun extractWeChatPeer(text: String): String? {
        val patterns = listOf(
            """向(.+?)转账""".toRegex(),
            """收到(.+?)转账""".toRegex(),
            """向(.+?)付款""".toRegex(),
            """已收款(?:来自)?(.+?)\b""".toRegex(),
            // 方括号/书名号风格
            """[【\[](.+?)[】\]]""".toRegex()
        )
        for (p in patterns) {
            val m = p.find(text)
            if (m != null) return m.groupValues[1].trim()
        }
        return null
    }
}
