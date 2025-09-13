package com.ccxiaoji.feature.ledger.domain.model

data class Card(
    val id: String,
    val userId: String,
    val name: String,
    val cardType: CardType,
    val maskedNumber: String?,
    val frontImagePath: String?,
    val backImagePath: String?,
    val expiryMonth: Int?,
    val expiryYear: Int?,
    val holderName: String?,
    val institutionName: String?,
    val institutionType: InstitutionType?,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long
)

enum class CardType {
    BANK_DEBIT,   // 储蓄卡
    BANK_CREDIT,  // 信用卡
    PASSBOOK,     // 存折
    SECURITIES,   // 证券账户
    OTHER;

    companion object {
        fun fromString(value: String): CardType = runCatching { valueOf(value) }.getOrElse { OTHER }
    }
}

enum class InstitutionType { BANK, BROKER, NONE }
