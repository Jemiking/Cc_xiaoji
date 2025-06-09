package com.ccxiaoji.core.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ccxiaoji.core.database.CcDatabase
import com.ccxiaoji.core.database.TestDatabaseFactory
import com.ccxiaoji.core.database.entity.UserEntity
import com.ccxiaoji.core.database.model.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UserDao测试
 * 验证用户相关数据库操作的正确性
 */
@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    
    private lateinit var database: CcDatabase
    private lateinit var userDao: UserDao
    
    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = TestDatabaseFactory.createInMemoryDatabase(context)
        userDao = database.userDao()
    }
    
    @After
    fun tearDown() {
        TestDatabaseFactory.cleanupDatabase(database)
    }
    
    @Test
    fun insertAndGetUser() = runBlocking {
        // 创建测试用户
        val user = UserEntity(
            id = "test_user_1",
            email = "test1@example.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        
        // 插入用户
        userDao.insertUser(user)
        
        // 获取用户
        val retrievedUser = userDao.getUserById(user.id)
        
        // 验证
        assert(retrievedUser != null)
        assert(retrievedUser?.id == user.id)
        assert(retrievedUser?.email == user.email)
        assert(retrievedUser?.syncStatus == user.syncStatus)
    }
    
    @Test
    fun updateUser() = runBlocking {
        // 创建并插入用户
        val user = UserEntity(
            id = "test_user_2",
            email = "test2@example.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        userDao.insertUser(user)
        
        // 更新用户
        val updatedUser = user.copy(
            email = "updated@example.com",
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
        userDao.updateUser(updatedUser)
        
        // 验证更新
        val retrievedUser = userDao.getUserById(user.id)
        assert(retrievedUser?.email == "updated@example.com")
        assert(retrievedUser?.syncStatus == SyncStatus.PENDING)
    }
    
    @Test
    fun observeCurrentUser() = runBlocking {
        // 创建并插入用户
        val user = UserEntity(
            id = "current_user_id",
            email = "current@example.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        userDao.insertUser(user)
        
        // 观察当前用户
        val currentUser = userDao.observeCurrentUser().first()
        
        // 验证
        assert(currentUser != null)
        assert(currentUser?.id == "current_user_id")
        assert(currentUser?.email == "current@example.com")
    }
    
    @Test
    fun getAllUsers() = runBlocking {
        // 插入多个用户
        val users = listOf(
            UserEntity(
                id = "user1",
                email = "user1@example.com",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.SYNCED
            ),
            UserEntity(
                id = "user2",
                email = "user2@example.com",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING
            ),
            UserEntity(
                id = "user3",
                email = "user3@example.com",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.FAILED
            )
        )
        
        users.forEach { userDao.insertUser(it) }
        
        // 获取所有用户
        val allUsers = userDao.getAllUsers()
        
        // 验证
        assert(allUsers.size == 3)
        assert(allUsers.map { it.id }.containsAll(listOf("user1", "user2", "user3")))
    }
}