package com.ccxiaoji.feature.ledger.domain.parser

import com.ccxiaoji.shared.notification.domain.model.RawNotificationEvent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知解析器工厂
 * 
 * 负责根据通知来源选择合适的解析器，并提供解析服务
 */
@Singleton
class NotificationParserFactory @Inject constructor() {
    
    /**
     * 所有可用的解析器
     */
    private val parsers: List<BaseNotificationParser> = listOf(
        AlipayNotificationParser(),
        // WechatNotificationParser(),  // 将在Task 2.1实现
        // UnionPayNotificationParser() // 将在Task 3.1实现
    )
    
    /**
     * 包名到解析器的映射缓存
     */
    private val packageParserMap: Map<String, BaseNotificationParser> by lazy {
        buildMap {
            parsers.forEach { parser ->
                parser.supportedPackages.forEach { packageName ->
                    put(packageName, parser)
                }
            }
        }
    }
    
    /**
     * 获取支持的应用包名列表
     */
    fun getSupportedPackages(): Set<String> = packageParserMap.keys
    
    /**
     * 根据包名获取解析器
     */
    fun getParser(packageName: String): BaseNotificationParser? {
        return packageParserMap[packageName]
    }
    
    /**
     * 解析通知事件
     * 
     * @param event 原始通知事件
     * @return 解析结果，失败时返回null
     */
    fun parse(event: RawNotificationEvent): ParseResult {
        val parser = getParser(event.packageName)
            ?: return ParseResult.Unsupported("不支持的应用: ${event.packageName}")
        
        if (!parser.canParse(event)) {
            return ParseResult.Skipped("不符合解析条件")
        }
        
        return try {
            val result = parser.parse(event)
            if (result != null) {
                ParseResult.Success(result)
            } else {
                ParseResult.Failed("解析失败")
            }
        } catch (e: Exception) {
            ParseResult.Error("解析异常: ${e.message}", e)
        }
    }
    
    /**
     * 获取所有解析器的统计信息
     */
    fun getParserStats(): ParserStats {
        return ParserStats(
            totalParsers = parsers.size,
            supportedPackages = getSupportedPackages(),
            parserInfo = parsers.map { parser ->
                ParserInfo(
                    name = parser.parserName,
                    version = parser.version,
                    supportedPackages = parser.supportedPackages
                )
            }
        )
    }
}

/**
 * 解析结果密封类
 */
sealed class ParseResult {
    /**
     * 解析成功
     */
    data class Success(val notification: com.ccxiaoji.feature.ledger.domain.model.PaymentNotification) : ParseResult()
    
    /**
     * 不支持的应用
     */
    data class Unsupported(val reason: String) : ParseResult()
    
    /**
     * 跳过解析（不符合条件）
     */
    data class Skipped(val reason: String) : ParseResult()
    
    /**
     * 解析失败
     */
    data class Failed(val reason: String) : ParseResult()
    
    /**
     * 解析异常
     */
    data class Error(val message: String, val exception: Throwable) : ParseResult()
}

/**
 * 解析器统计信息
 */
data class ParserStats(
    val totalParsers: Int,
    val supportedPackages: Set<String>,
    val parserInfo: List<ParserInfo>
)

/**
 * 解析器信息
 */
data class ParserInfo(
    val name: String,
    val version: Int,
    val supportedPackages: Set<String>
)