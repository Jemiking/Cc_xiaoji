package com.ccxiaoji.core.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ccxiaoji.core.database.migration.Migration_1_2
import com.ccxiaoji.core.database.migration.Migration_2_3
import com.ccxiaoji.core.database.migration.Migration_3_4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * 数据库迁移测试
 * 测试所有数据库版本迁移的正确性
 */
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    
    companion object {
        private const val TEST_DB = "migration-test"
    }
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        CcDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )
    
    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        // 创建版本1的数据库
        helper.createDatabase(TEST_DB, 1).apply {
            // 插入测试数据
            execSQL("""
                INSERT INTO users (id, email, created_at, updated_at, sync_status) 
                VALUES ('test_user', 'test@example.com', 1234567890, 1234567890, 'SYNCED')
            """.trimIndent())
            close()
        }
        
        // 执行迁移
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration_1_2)
        
        // 验证迁移后的数据完整性
        val cursor = db.query("SELECT * FROM users WHERE id = 'test_user'")
        assert(cursor.moveToFirst())
        assert(cursor.getString(cursor.getColumnIndex("email")) == "test@example.com")
        cursor.close()
        db.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        // 创建版本2的数据库
        helper.createDatabase(TEST_DB, 2).apply {
            // 插入测试数据
            execSQL("""
                INSERT INTO users (id, email, created_at, updated_at, sync_status) 
                VALUES ('test_user', 'test@example.com', 1234567890, 1234567890, 'SYNCED')
            """.trimIndent())
            
            // 插入账户数据
            execSQL("""
                INSERT INTO accounts (id, user_id, name, type, balance_cents, currency, is_default, created_at, updated_at, sync_status) 
                VALUES ('test_account', 'test_user', 'Test Account', 'CASH', 10000, 'CNY', 1, 1234567890, 1234567890, 'SYNCED')
            """.trimIndent())
            close()
        }
        
        // 执行迁移
        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration_2_3)
        
        // 验证新字段添加成功
        val cursor = db.query("SELECT * FROM accounts WHERE id = 'test_account'")
        assert(cursor.moveToFirst())
        assert(cursor.getColumnIndex("billing_day") != -1)
        assert(cursor.getColumnIndex("payment_due_day") != -1)
        assert(cursor.getColumnIndex("credit_limit_cents") != -1)
        cursor.close()
        db.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrate3To4() {
        // 创建版本3的数据库
        helper.createDatabase(TEST_DB, 3).apply {
            // 插入测试数据
            execSQL("""
                INSERT INTO users (id, email, created_at, updated_at, sync_status) 
                VALUES ('test_user', 'test@example.com', 1234567890, 1234567890, 'SYNCED')
            """.trimIndent())
            close()
        }
        
        // 执行迁移
        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, Migration_3_4)
        
        // 验证信用卡相关表创建成功
        val billCursor = db.query("SELECT * FROM credit_card_bills")
        assert(billCursor.columnCount > 0)
        billCursor.close()
        
        val paymentCursor = db.query("SELECT * FROM credit_card_payments")
        assert(paymentCursor.columnCount > 0)
        paymentCursor.close()
        
        db.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // 创建版本1的数据库
        helper.createDatabase(TEST_DB, 1).apply {
            close()
        }
        
        // 测试完整的迁移路径 1 -> 2 -> 3 -> 4
        val db = helper.runMigrationsAndValidate(
            TEST_DB, 
            4, 
            true, 
            Migration_1_2,
            Migration_2_3,
            Migration_3_4
        )
        db.close()
    }
    
    @Test
    fun testDataIntegrityAfterModularization() {
        // 使用Room创建数据库并验证所有表和字段都正确
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, CcDatabase::class.java)
            .build()
        
        // 验证所有DAO都能正常工作
        assert(db.userDao() != null)
        assert(db.accountDao() != null)
        assert(db.transactionDao() != null)
        assert(db.categoryDao() != null)
        assert(db.budgetDao() != null)
        assert(db.taskDao() != null)
        assert(db.habitDao() != null)
        assert(db.countdownDao() != null)
        assert(db.recurringTransactionDao() != null)
        assert(db.savingsGoalDao() != null)
        assert(db.changeLogDao() != null)
        assert(db.creditCardBillDao() != null)
        assert(db.creditCardPaymentDao() != null)
        
        db.close()
    }
}