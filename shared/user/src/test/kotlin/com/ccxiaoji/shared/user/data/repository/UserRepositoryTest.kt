package com.ccxiaoji.shared.user.data.repository

import com.ccxiaoji.shared.user.data.local.dao.UserDao
import com.ccxiaoji.shared.user.data.local.entity.UserEntity
import com.ccxiaoji.shared.user.domain.model.User
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {

    @MockK
    private lateinit var userDao: UserDao

    private lateinit var userRepository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        userRepository = UserRepository(userDao)
    }

    @Test
    fun `获取当前用户信息`() = runTest {
        // Given
        val userId = "user123"
        val now = System.currentTimeMillis()
        val userEntity = UserEntity(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            avatarUrl = "https://example.com/avatar.jpg",
            nickname = "测试用户",
            phoneNumber = "13800138000",
            createdAt = now,
            updatedAt = now,
            syncStatus = "SYNCED"
        )

        coEvery { userDao.getUserById(userId) } returns flowOf(userEntity)
        coEvery { userRepository.getCurrentUserId() } returns userId

        // When
        val result = userRepository.getCurrentUser().first()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(userId)
        assertThat(result?.username).isEqualTo("testuser")
        assertThat(result?.email).isEqualTo("test@example.com")
        assertThat(result?.nickname).isEqualTo("测试用户")
        coVerify(exactly = 1) { userDao.getUserById(userId) }
    }

    @Test
    fun `更新用户个人信息`() = runTest {
        // Given
        val userId = "user123"
        val existingUser = UserEntity(
            id = userId,
            username = "testuser",
            email = "old@example.com",
            avatarUrl = null,
            nickname = "旧昵称",
            phoneNumber = null,
            createdAt = System.currentTimeMillis() - 86400000, // 一天前
            updatedAt = System.currentTimeMillis() - 86400000,
            syncStatus = "SYNCED"
        )

        val newNickname = "新昵称"
        val newEmail = "new@example.com"
        val newPhone = "13900139000"

        coEvery { userDao.getUserByIdSync(userId) } returns existingUser
        coEvery { userDao.updateUser(any()) } returns Unit
        coEvery { userRepository.getCurrentUserId() } returns userId

        // When
        userRepository.updateUserInfo(
            nickname = newNickname,
            email = newEmail,
            phoneNumber = newPhone
        )

        // Then
        coVerify(exactly = 1) { 
            userDao.updateUser(withArg { updatedUser ->
                assertThat(updatedUser.nickname).isEqualTo(newNickname)
                assertThat(updatedUser.email).isEqualTo(newEmail)
                assertThat(updatedUser.phoneNumber).isEqualTo(newPhone)
                assertThat(updatedUser.syncStatus).isEqualTo("PENDING_SYNC")
                assertThat(updatedUser.updatedAt).isGreaterThan(existingUser.updatedAt)
            })
        }
    }

    @Test
    fun `创建新用户`() = runTest {
        // Given
        val username = "newuser"
        val email = "newuser@example.com"
        val nickname = "新用户"

        coEvery { userDao.insertUser(any()) } returns Unit

        // When
        val user = userRepository.createUser(
            username = username,
            email = email,
            nickname = nickname
        )

        // Then
        assertThat(user.username).isEqualTo(username)
        assertThat(user.email).isEqualTo(email)
        assertThat(user.nickname).isEqualTo(nickname)
        assertThat(user.id).isNotEmpty()
        
        coVerify(exactly = 1) { 
            userDao.insertUser(withArg { entity ->
                assertThat(entity.username).isEqualTo(username)
                assertThat(entity.email).isEqualTo(email)
                assertThat(entity.nickname).isEqualTo(nickname)
                assertThat(entity.syncStatus).isEqualTo("PENDING_SYNC")
            })
        }
    }

    @Test
    fun `检查用户名是否存在`() = runTest {
        // Given
        val existingUsername = "existinguser"
        val newUsername = "newusername"

        coEvery { userDao.getUserByUsername(existingUsername) } returns mockk<UserEntity>()
        coEvery { userDao.getUserByUsername(newUsername) } returns null

        // When
        val existingResult = userRepository.isUsernameExists(existingUsername)
        val newResult = userRepository.isUsernameExists(newUsername)

        // Then
        assertThat(existingResult).isTrue()
        assertThat(newResult).isFalse()
        coVerify(exactly = 1) { userDao.getUserByUsername(existingUsername) }
        coVerify(exactly = 1) { userDao.getUserByUsername(newUsername) }
    }
}

// 假设的Repository扩展方法
suspend fun UserRepository.isUsernameExists(username: String): Boolean {
    return userDao.getUserByUsername(username) != null
}

suspend fun UserRepository.createUser(
    username: String,
    email: String,
    nickname: String
): User {
    val userId = java.util.UUID.randomUUID().toString()
    val now = System.currentTimeMillis()
    
    val entity = UserEntity(
        id = userId,
        username = username,
        email = email,
        avatarUrl = null,
        nickname = nickname,
        phoneNumber = null,
        createdAt = now,
        updatedAt = now,
        syncStatus = "PENDING_SYNC"
    )
    
    userDao.insertUser(entity)
    
    return User(
        id = userId,
        username = username,
        email = email,
        avatarUrl = null,
        nickname = nickname,
        phoneNumber = null,
        createdAt = Instant.fromEpochMilliseconds(now),
        updatedAt = Instant.fromEpochMilliseconds(now)
    )
}

// 假设的DAO接口扩展
interface UserDao {
    fun getUserById(userId: String): kotlinx.coroutines.flow.Flow<UserEntity?>
    suspend fun getUserByIdSync(userId: String): UserEntity?
    suspend fun getUserByUsername(username: String): UserEntity?
    suspend fun insertUser(user: UserEntity)
    suspend fun updateUser(user: UserEntity)
}