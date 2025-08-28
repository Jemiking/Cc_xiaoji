package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库迁移 13 -> 14
 * 添加记账簿联动功能的数据表
 * 
 * 新增表：
 * 1. ledger_links - 记账簿联动关系表
 * 2. transaction_ledger_relations - 交易与记账簿关联关系表
 */
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建记账簿联动关系表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS ledger_links (
                id TEXT PRIMARY KEY NOT NULL,
                parent_ledger_id TEXT NOT NULL,
                child_ledger_id TEXT NOT NULL,
                sync_mode TEXT NOT NULL DEFAULT 'BIDIRECTIONAL',
                auto_sync_enabled INTEGER NOT NULL DEFAULT 1,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_active INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY(parent_ledger_id) REFERENCES ledgers(id) ON DELETE CASCADE,
                FOREIGN KEY(child_ledger_id) REFERENCES ledgers(id) ON DELETE CASCADE,
                UNIQUE(parent_ledger_id, child_ledger_id)
            )
        """.trimIndent())
        
        // 创建交易与记账簿关联关系表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS transaction_ledger_relations (
                id TEXT PRIMARY KEY NOT NULL,
                transaction_id TEXT NOT NULL,
                ledger_id TEXT NOT NULL,
                relation_type TEXT NOT NULL DEFAULT 'PRIMARY',
                sync_source_ledger_id TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY(transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
                FOREIGN KEY(ledger_id) REFERENCES ledgers(id) ON DELETE CASCADE,
                FOREIGN KEY(sync_source_ledger_id) REFERENCES ledgers(id) ON DELETE SET NULL,
                UNIQUE(transaction_id, ledger_id)
            )
        """.trimIndent())
        
        // 为联动关系表创建索引
        database.execSQL("CREATE INDEX idx_ledger_links_parent ON ledger_links(parent_ledger_id)")
        database.execSQL("CREATE INDEX idx_ledger_links_child ON ledger_links(child_ledger_id)")
        database.execSQL("CREATE INDEX idx_ledger_links_active ON ledger_links(is_active)")
        
        // 为交易关联表创建索引
        database.execSQL("CREATE INDEX idx_transaction_ledger_relations_transaction ON transaction_ledger_relations(transaction_id)")
        database.execSQL("CREATE INDEX idx_transaction_ledger_relations_ledger ON transaction_ledger_relations(ledger_id)")
        database.execSQL("CREATE INDEX idx_transaction_ledger_relations_type ON transaction_ledger_relations(relation_type)")
        database.execSQL("CREATE INDEX idx_transaction_ledger_relations_source ON transaction_ledger_relations(sync_source_ledger_id)")
        
        // 为现有的交易记录创建PRIMARY关系记录
        // 这确保了所有现有交易都有对应的记账簿关联记录
        database.execSQL("""
            INSERT INTO transaction_ledger_relations (
                id,
                transaction_id,
                ledger_id,
                relation_type,
                sync_source_ledger_id,
                created_at
            )
            SELECT 
                REPLACE(t.id, '-', '') || '-rel-' || REPLACE(t.ledgerId, '-', '') as id,
                t.id as transaction_id,
                t.ledgerId as ledger_id,
                'PRIMARY' as relation_type,
                NULL as sync_source_ledger_id,
                t.createdAt as created_at
            FROM transactions t
            WHERE t.ledgerId IS NOT NULL
        """.trimIndent())
    }
}
