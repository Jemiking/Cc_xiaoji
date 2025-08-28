package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库迁移 14 -> 15
 * 添加自动记账功能所需的数据表和字段
 * 
 * 新增表：
 * 1. auto_ledger_dedup - 自动记账去重表
 * 2. app_auto_ledger_config - 应用自动记账配置表
 * 
 * 扩展表：
 * 1. transactions - 添加自动记账相关字段
 */
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        
        // 1. 为transactions表添加自动记账相关字段
        database.execSQL("ALTER TABLE transactions ADD COLUMN sourceApp TEXT")
        database.execSQL("ALTER TABLE transactions ADD COLUMN sourceType TEXT")
        database.execSQL("ALTER TABLE transactions ADD COLUMN postedTime INTEGER")
        database.execSQL("ALTER TABLE transactions ADD COLUMN parserVersion INTEGER DEFAULT 1")
        database.execSQL("ALTER TABLE transactions ADD COLUMN confidence REAL")
        database.execSQL("ALTER TABLE transactions ADD COLUMN accountGuess TEXT")
        
        // 2. 创建自动记账去重表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS auto_ledger_dedup (
                eventKey TEXT PRIMARY KEY NOT NULL,
                createdAt INTEGER NOT NULL,
                packageName TEXT NOT NULL,
                textHash TEXT NOT NULL,
                amountCents INTEGER NOT NULL,
                merchantHash TEXT,
                postTime INTEGER NOT NULL
            )
        """.trimIndent())
        
        // 3. 创建应用自动记账配置表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS app_auto_ledger_config (
                appPkg TEXT PRIMARY KEY NOT NULL,
                mode INTEGER NOT NULL DEFAULT 1,
                blacklist TEXT,
                whitelist TEXT,
                amountWindowSec INTEGER NOT NULL DEFAULT 300,
                confidenceThreshold REAL NOT NULL DEFAULT 0.85,
                accountRules TEXT,
                categoryRules TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent())
        
        // 4. 为transactions表新增字段创建索引
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_source_time ON transactions(sourceApp, postedTime)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_source_type ON transactions(sourceType)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_confidence ON transactions(confidence)")
        
        // 5. 为去重表创建索引
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_auto_ledger_dedup_created ON auto_ledger_dedup(createdAt)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_auto_ledger_dedup_package ON auto_ledger_dedup(packageName)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_auto_ledger_dedup_post_time ON auto_ledger_dedup(postTime)")
        
        // 6. 为配置表创建索引
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_app_auto_ledger_config_mode ON app_auto_ledger_config(mode)")
        
        // 7. 插入默认的支付应用配置
        val currentTime = System.currentTimeMillis()
        
        // 支付宝默认配置
        database.execSQL("""
            INSERT OR IGNORE INTO app_auto_ledger_config (
                appPkg, mode, confidenceThreshold, createdAt, updatedAt
            ) VALUES ('com.eg.android.AlipayGphone', 1, 0.85, ?, ?)
        """.trimIndent(), arrayOf(currentTime, currentTime))
        
        // 微信默认配置
        database.execSQL("""
            INSERT OR IGNORE INTO app_auto_ledger_config (
                appPkg, mode, confidenceThreshold, createdAt, updatedAt
            ) VALUES ('com.tencent.mm', 1, 0.85, ?, ?)
        """.trimIndent(), arrayOf(currentTime, currentTime))
        
        // 云闪付默认配置
        database.execSQL("""
            INSERT OR IGNORE INTO app_auto_ledger_config (
                appPkg, mode, confidenceThreshold, createdAt, updatedAt
            ) VALUES ('com.unionpay', 1, 0.85, ?, ?)
        """.trimIndent(), arrayOf(currentTime, currentTime))
        
        // 8. 电商应用黑名单配置
        val ecommerceBlacklist = listOf(
            "com.taobao.taobao",      // 淘宝
            "com.tmall.wireless",     // 天猫
            "com.jingdong.app.mall",  // 京东
            "com.suning.mobile.ebuy", // 苏宁
            "com.xunmeng.pinduoduo"   // 拼多多
        )
        
        ecommerceBlacklist.forEach { pkg ->
            database.execSQL("""
                INSERT OR IGNORE INTO app_auto_ledger_config (
                    appPkg, mode, blacklist, createdAt, updatedAt
                ) VALUES (?, 0, '["订单", "购买成功", "下单成功", "支付完成", "商品"]', ?, ?)
            """.trimIndent(), arrayOf(pkg, currentTime, currentTime))
        }
    }
}