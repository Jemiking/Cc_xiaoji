package com.ccxiaoji.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 为所有 category 为 null 但有 categoryId 的记录补充 category 值
        database.execSQL("""
            UPDATE transactions 
            SET category = (
                SELECT CASE 
                    WHEN c.type = 'EXPENSE' THEN
                        CASE c.name 
                            WHEN '餐饮' THEN 'FOOD'
                            WHEN '交通' THEN 'TRANSPORT'
                            WHEN '购物' THEN 'SHOPPING'
                            WHEN '娱乐' THEN 'ENTERTAINMENT'
                            WHEN '医疗' THEN 'MEDICAL'
                            WHEN '教育' THEN 'EDUCATION'
                            WHEN '居住' THEN 'HOUSING'
                            WHEN '水电' THEN 'UTILITIES'
                            WHEN '通讯' THEN 'COMMUNICATION'
                            ELSE 'OTHER'
                        END
                    ELSE 'INCOME'
                END
                FROM categories c 
                WHERE c.id = transactions.categoryId
            )
            WHERE category IS NULL AND categoryId IS NOT NULL
        """)
    }
}