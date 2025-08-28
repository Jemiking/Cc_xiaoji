package com.ccxiaoji.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * 应用自动记账配置实体
 * 
 * 存储每个应用的自动记账配置，包括模式、黑白名单、规则等
 */
@Entity(
    tableName = "app_auto_ledger_config",
    indices = [
        Index(value = ["mode"])
    ]
)
data class AppAutoLedgerConfigEntity(
    /**
     * 应用包名，作为主键
     */
    @PrimaryKey
    val appPkg: String,
    
    /**
     * 自动记账模式
     * 0: 禁用
     * 1: 半自动（需要用户确认）
     * 2: 全自动
     */
    val mode: Int = 1,
    
    /**
     * 黑名单关键词（JSON格式）
     * 包含这些关键词的通知将被忽略
     */
    val blacklist: String? = null,
    
    /**
     * 白名单关键词（JSON格式）
     * 仅包含这些关键词的通知才会处理
     */
    val whitelist: String? = null,
    
    /**
     * 金额窗口期（秒）
     * 同一金额在此时间段内只记账一次
     */
    val amountWindowSec: Int = 300,
    
    /**
     * 置信度阈值
     * 低于此阈值的解析结果将降级处理
     */
    val confidenceThreshold: Double = 0.85,
    
    /**
     * 账户映射规则（JSON格式）
     * 根据支付方式自动选择账户
     */
    val accountRules: String? = null,
    
    /**
     * 分类映射规则（JSON格式）
     * 根据商户名自动选择分类
     */
    val categoryRules: String? = null,
    
    /**
     * 创建时间
     */
    val createdAt: Long,
    
    /**
     * 更新时间
     */
    val updatedAt: Long
)