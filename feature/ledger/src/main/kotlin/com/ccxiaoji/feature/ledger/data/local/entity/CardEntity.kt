package com.ccxiaoji.feature.ledger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 卡片信息实体（非资金账户）：用于保存银行卡、会员卡、门禁卡等信息与图片。
 * 注意：敏感字段（如完整卡号）仅在必要时保存，默认使用掩码展示。
 */
@Entity(
    tableName = "cards",
    indices = [
        Index(value = ["userId", "name"], unique = false),
        Index(value = ["userId", "cardType"], unique = false)
    ]
)
data class CardEntity(
    @PrimaryKey val id: String,
    val userId: String,

    // 基本信息
    val name: String,              // 卡片名称（如 招商信用卡、星巴克会员卡）
    val cardType: String,          // 类型：BANK_DEBIT/BANK_CREDIT/MEMBER/ACCESS/OTHER

    // 展示信息
    val maskedNumber: String?,     // 掩码卡号（如 **** **** **** 1234）
    val frontImagePath: String?,   // 正面图片本地路径（app私有目录）
    val backImagePath: String?,    // 背面图片本地路径（可选）

    // 业务扩展
    val expiryMonth: Int?,         // 1-12
    val expiryYear: Int?,          // 四位年份
    val holderName: String?,       // 户名
    val institutionName: String?,  // 银行/券商名称（可空）
    val institutionType: String?,  // BANK/BROKER/NONE（可空）
    val note: String?,             // 备注

    // 元信息
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
