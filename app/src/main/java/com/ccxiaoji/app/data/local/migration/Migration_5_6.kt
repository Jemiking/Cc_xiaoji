package com.ccxiaoji.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create savings_goals table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS savings_goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                targetAmount REAL NOT NULL,
                currentAmount REAL NOT NULL DEFAULT 0.0,
                targetDate TEXT,
                description TEXT,
                color TEXT NOT NULL DEFAULT '#4CAF50',
                iconName TEXT NOT NULL DEFAULT 'savings',
                isActive INTEGER NOT NULL DEFAULT 1,
                createdAt TEXT NOT NULL,
                updatedAt TEXT NOT NULL,
                syncStatus TEXT NOT NULL DEFAULT 'PENDING'
            )
        """)
        
        // Create savings_contributions table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS savings_contributions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                goalId INTEGER NOT NULL,
                amount REAL NOT NULL,
                note TEXT,
                createdAt TEXT NOT NULL,
                syncStatus TEXT NOT NULL DEFAULT 'PENDING',
                FOREIGN KEY(goalId) REFERENCES savings_goals(id) ON DELETE CASCADE
            )
        """)
        
        // Create index on goalId for better query performance
        db.execSQL("CREATE INDEX IF NOT EXISTS index_savings_contributions_goalId ON savings_contributions(goalId)")
    }
}