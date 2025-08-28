package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 为升级用户确保默认记账簿存在
        // 这是为了修复早期版本中可能缺少默认记账簿的问题
        
        // 1. 检查是否有用户但没有对应的默认记账簿
        val cursor = database.query("""
            SELECT u.id as userId 
            FROM users u 
            LEFT JOIN ledgers l ON u.id = l.userId AND l.isDefault = 1 AND l.isActive = 1
            WHERE l.id IS NULL
        """.trimIndent())
        
        val usersWithoutDefaultLedger = mutableListOf<String>()
        
        while (cursor.moveToNext()) {
            val userId = cursor.getString(0)
            usersWithoutDefaultLedger.add(userId)
        }
        cursor.close()
        
        // 2. 为没有默认记账簿的用户创建默认记账簿
        val currentTime = System.currentTimeMillis()
        
        usersWithoutDefaultLedger.forEach { userId ->
            // 首先确保该用户没有其他默认记账簿（以防万一）
            database.execSQL(
                "UPDATE ledgers SET isDefault = 0, updatedAt = ? WHERE userId = ? AND isDefault = 1",
                arrayOf(currentTime, userId)
            )
            
            // 创建新的默认记账簿
            database.execSQL("""
                INSERT INTO ledgers (
                    id, userId, name, description, color, icon, 
                    isDefault, displayOrder, isActive, createdAt, updatedAt
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
                arrayOf(
                    UUID.randomUUID().toString(),
                    userId,
                    "总记账簿",
                    "默认记账簿，包含所有基本记账数据",
                    "#3A7AFE",
                    "book",
                    1, // isDefault = true
                    0, // displayOrder = 0
                    1, // isActive = true
                    currentTime,
                    currentTime
                )
            )
        }
        
        // 3. 对于default_user_id用户特殊处理（如果存在但没有默认记账簿）
        val defaultUserCursor = database.query(
            "SELECT COUNT(*) FROM users WHERE id = 'current_user_id'"
        )
        
        var hasDefaultUser = false
        if (defaultUserCursor.moveToFirst()) {
            hasDefaultUser = defaultUserCursor.getInt(0) > 0
        }
        defaultUserCursor.close()
        
        if (hasDefaultUser) {
            // 检查current_user_id是否有默认记账簿
            val ledgerCursor = database.query(
                "SELECT COUNT(*) FROM ledgers WHERE userId = 'current_user_id' AND isDefault = 1 AND isActive = 1"
            )
            
            var hasDefaultLedger = false
            if (ledgerCursor.moveToFirst()) {
                hasDefaultLedger = ledgerCursor.getInt(0) > 0
            }
            ledgerCursor.close()
            
            if (!hasDefaultLedger) {
                // 为current_user_id创建默认记账簿
                database.execSQL("""
                    INSERT INTO ledgers (
                        id, userId, name, description, color, icon, 
                        isDefault, displayOrder, isActive, createdAt, updatedAt
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                    arrayOf(
                        UUID.randomUUID().toString(),
                        "current_user_id",
                        "总记账簿",
                        "默认记账簿，包含所有基本记账数据",
                        "#3A7AFE",
                        "book",
                        1, // isDefault = true
                        0, // displayOrder = 0
                        1, // isActive = true
                        currentTime,
                        currentTime
                    )
                )
            }
        }
    }
}