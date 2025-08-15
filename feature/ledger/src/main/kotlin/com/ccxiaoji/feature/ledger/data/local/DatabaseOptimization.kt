package com.ccxiaoji.feature.ledger.data.local

import androidx.room.Index

/**
 * 数据库优化配置
 * 定义索引以提升查询性能
 */
object DatabaseOptimization {
    
    /**
     * 交易表索引定义
     * 优化常见查询场景
     */
    val TRANSACTION_INDICES = arrayOf(
        // 用户查询优化
        Index(value = arrayOf("userId", "createdAt"), name = "idx_transaction_user_date"),
        
        // 账户查询优化
        Index(value = arrayOf("accountId", "createdAt"), name = "idx_transaction_account_date"),
        
        // 分类查询优化
        Index(value = arrayOf("categoryId", "createdAt"), name = "idx_transaction_category_date"),
        
        // 日期范围查询优化
        Index(value = arrayOf("createdAt", "isDeleted"), name = "idx_transaction_date_deleted"),
        
        // 同步状态查询优化
        Index(value = arrayOf("syncStatus", "isDeleted"), name = "idx_transaction_sync_deleted"),
        
        // 复合查询优化
        Index(value = arrayOf("userId", "accountId", "createdAt"), name = "idx_transaction_user_account_date"),
        Index(value = arrayOf("userId", "categoryId", "createdAt"), name = "idx_transaction_user_category_date")
    )
    
    /**
     * 账户表索引定义
     */
    val ACCOUNT_INDICES = arrayOf(
        Index(value = arrayOf("userId", "isDeleted"), name = "idx_account_user_deleted"),
        Index(value = arrayOf("type", "isDeleted"), name = "idx_account_type_deleted")
    )
    
    /**
     * 分类表索引定义
     */
    val CATEGORY_INDICES = arrayOf(
        Index(value = arrayOf("userId", "type", "isDeleted"), name = "idx_category_user_type_deleted"),
        Index(value = arrayOf("parentId", "isDeleted"), name = "idx_category_parent_deleted")
    )
    
    /**
     * 预算表索引定义
     */
    val BUDGET_INDICES = arrayOf(
        Index(value = arrayOf("userId", "categoryId", "isDeleted"), name = "idx_budget_user_category_deleted"),
        Index(value = arrayOf("userId", "period", "isDeleted"), name = "idx_budget_user_period_deleted")
    )
    
    /**
     * 信用卡账单表索引定义
     */
    val CREDIT_CARD_BILL_INDICES = arrayOf(
        Index(value = arrayOf("accountId", "billDate"), name = "idx_bill_account_date"),
        Index(value = arrayOf("status", "dueDate"), name = "idx_bill_status_due"),
        Index(value = arrayOf("accountId", "status"), name = "idx_bill_account_status")
    )
    
    /**
     * 查询优化建议
     */
    object QueryOptimization {
        /**
         * 批量查询建议大小
         */
        const val BATCH_SIZE = 100
        
        /**
         * 默认分页大小
         */
        const val DEFAULT_PAGE_SIZE = 20
        
        /**
         * 最大分页大小
         */
        const val MAX_PAGE_SIZE = 100
        
        /**
         * 缓存过期时间（毫秒）
         */
        const val CACHE_EXPIRY_MS = 5 * 60 * 1000L // 5分钟
        
        /**
         * 获取优化的分页大小
         */
        fun getOptimalPageSize(totalCount: Int): Int {
            return when {
                totalCount < 100 -> DEFAULT_PAGE_SIZE
                totalCount < 500 -> 30
                totalCount < 1000 -> 50
                else -> BATCH_SIZE
            }
        }
        
        /**
         * 是否应该使用缓存
         */
        fun shouldUseCache(lastQueryTime: Long): Boolean {
            return System.currentTimeMillis() - lastQueryTime < CACHE_EXPIRY_MS
        }
    }
    
    /**
     * SQL查询优化模板
     */
    object OptimizedQueries {
        /**
         * 优化的月度统计查询
         * 使用索引和聚合函数减少查询时间
         */
        const val MONTHLY_STATISTICS = """
            SELECT 
                CASE 
                    WHEN c.type = 'INCOME' THEN 'income'
                    ELSE 'expense'
                END as type,
                SUM(t.amountCents) as total
            FROM transactions t
            INNER JOIN categories c ON t.categoryId = c.id
            WHERE t.userId = :userId
            AND t.createdAt >= :startTime
            AND t.createdAt < :endTime
            AND t.isDeleted = 0
            GROUP BY c.type
        """
        
        /**
         * 优化的账户余额查询
         * 避免全表扫描
         */
        const val ACCOUNT_BALANCE_BATCH = """
            SELECT 
                a.id as accountId,
                a.initialBalance + COALESCE(SUM(
                    CASE 
                        WHEN c.type = 'INCOME' THEN t.amountCents
                        ELSE -t.amountCents
                    END
                ), 0) as balance
            FROM accounts a
            LEFT JOIN transactions t ON a.id = t.accountId AND t.isDeleted = 0
            LEFT JOIN categories c ON t.categoryId = c.id
            WHERE a.userId = :userId
            AND a.isDeleted = 0
            GROUP BY a.id
        """
        
        /**
         * 优化的趋势查询
         * 按天聚合数据
         */
        const val DAILY_TREND = """
            SELECT 
                DATE(t.createdAt / 1000, 'unixepoch') as date,
                c.type,
                SUM(t.amountCents) as total
            FROM transactions t
            INNER JOIN categories c ON t.categoryId = c.id
            WHERE t.userId = :userId
            AND t.createdAt >= :startTime
            AND t.createdAt < :endTime
            AND t.isDeleted = 0
            GROUP BY date, c.type
            ORDER BY date
        """
    }
}