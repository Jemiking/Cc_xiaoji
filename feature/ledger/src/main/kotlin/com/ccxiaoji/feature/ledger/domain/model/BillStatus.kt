package com.ccxiaoji.feature.ledger.domain.model

/**
 * 信用卡账单状态
 */
enum class BillStatus {
    PENDING,      // 未出账
    GENERATED,    // 已出账
    PAID,         // 已还清
    PARTIAL_PAID, // 部分还款
    OVERDUE       // 逾期
}