package com.ccxiaoji.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * 自动记账去重实体
 * 
 * 用于防止同一通知事件被重复记账
 * 通过事件指纹（eventKey）实现幂等性
 */
@Entity(
    tableName = "auto_ledger_dedup",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["packageName"]),
        Index(value = ["postTime"])
    ]
)
data class AutoLedgerDedupEntity(
    /**
     * 事件指纹，作为主键确保唯一性
     * 由包名、金额、时间段、商户等信息生成hash
     */
    @PrimaryKey
    val eventKey: String,
    
    /**
     * 创建时间（用于TTL清理）
     */
    val createdAt: Long,
    
    /**
     * 应用包名
     */
    val packageName: String,
    
    /**
     * 通知文本hash（用于快速比对）
     */
    val textHash: String,
    
    /**
     * 金额（分为单位）
     */
    val amountCents: Long,
    
    /**
     * 商户名hash（可能为null）
     */
    val merchantHash: String?,
    
    /**
     * 通知发布时间
     */
    val postTime: Long
)