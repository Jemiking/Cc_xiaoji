package com.ccxiaoji.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ccxiaoji.core.database.CcDatabase
import com.ccxiaoji.core.database.DatabaseModuleDebugHelper
import com.ccxiaoji.core.database.migrations.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreDatabaseModule {
    
    @Provides
    @Singleton
    fun provideCcDatabase(@ApplicationContext context: Context): CcDatabase {
        val databaseName = DatabaseModuleDebugHelper.getDatabaseName(context)
        
        return Room.databaseBuilder(
            context,
            CcDatabase::class.java,
            databaseName
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // 创建默认数据
                CoroutineScope(Dispatchers.IO).launch {
                    val currentTime = System.currentTimeMillis()
                    
                    // 插入默认用户
                    db.execSQL(
                        "INSERT INTO users (id, email, createdAt, updatedAt, isDeleted) VALUES (?, ?, ?, ?, ?)",
                        arrayOf("current_user_id", "default@ccxiaoji.com", currentTime, currentTime, 0)
                    )
                    
                    // 插入默认账户
                    db.execSQL(
                        "INSERT INTO accounts (id, userId, name, type, balanceCents, currency, isDefault, creditLimitCents, billingDay, paymentDueDay, gracePeriodDays, createdAt, updatedAt, isDeleted, syncStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        arrayOf("default_account_id", "current_user_id", "现金账户", "CASH", 0L, "CNY", 1, null, null, null, null, currentTime, currentTime, 0, "SYNCED")
                    )
                    
                    // 创建默认分类
                    val expenseCategories = listOf(
                        Triple("餐饮", "🍜", "#FF6B6B"),
                        Triple("交通", "🚇", "#4ECDC4"),
                        Triple("购物", "🛍️", "#45B7D1"),
                        Triple("娱乐", "🎮", "#F7DC6F"),
                        Triple("医疗", "🏥", "#E74C3C"),
                        Triple("教育", "📚", "#3498DB"),
                        Triple("居住", "🏠", "#9B59B6"),
                        Triple("水电", "💡", "#1ABC9C"),
                        Triple("通讯", "📱", "#34495E"),
                        Triple("其他", "📌", "#95A5A6")
                    )
                    
                    val incomeCategories = listOf(
                        Triple("工资", "💰", "#27AE60"),
                        Triple("奖金", "🎁", "#F39C12"),
                        Triple("投资", "📈", "#8E44AD"),
                        Triple("兼职", "💼", "#2980B9"),
                        Triple("其他", "💸", "#16A085")
                    )
                    
                    // 插入支出分类
                    expenseCategories.forEachIndexed { index, (name, icon, color) ->
                        db.execSQL(
                            "INSERT INTO categories (id, userId, name, type, icon, color, parentId, displayOrder, isSystem, usageCount, createdAt, updatedAt, isDeleted, syncStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            arrayOf(UUID.randomUUID().toString(), "current_user_id", name, "EXPENSE", icon, color, null, index, 1, 0, currentTime, currentTime, 0, "SYNCED")
                        )
                    }
                    
                    // 插入收入分类
                    incomeCategories.forEachIndexed { index, (name, icon, color) ->
                        db.execSQL(
                            "INSERT INTO categories (id, userId, name, type, icon, color, parentId, displayOrder, isSystem, usageCount, createdAt, updatedAt, isDeleted, syncStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            arrayOf(UUID.randomUUID().toString(), "current_user_id", name, "INCOME", icon, color, null, index, 1, 0, currentTime, currentTime, 0, "SYNCED")
                        )
                    }
                }
            }
        })
        .addMigrations(*DatabaseMigrations.getAllMigrations())
        .build()
    }
}