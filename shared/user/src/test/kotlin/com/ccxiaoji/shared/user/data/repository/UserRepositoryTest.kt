package com.ccxiaoji.shared.user.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ccxiaoji.shared.user.data.local.dao.UserDao
import com.ccxiaoji.shared.user.data.local.entity.UserEntity
import com.ccxiaoji.shared.user.data.remote.api.AuthApi
import com.ccxiaoji.shared.user.data.remote.dto.LoginRequest
import com.ccxiaoji.shared.user.data.remote.dto.LoginResponse
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import retrofit2.Response
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {

    @MockK
    private lateinit var userDao: UserDao

    @MockK
    private lateinit var authApi: AuthApi

    @MockK
    private lateinit var dataStore: DataStore<Preferences>

    @MockK
    private lateinit var preferences: Preferences

    private lateinit var userRepository: UserRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        userRepository = UserRepository(userDao, authApi, dataStore)
    }

    @Test
    fun `登录成功`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val userId = "user-123"
        val accessToken = "access-token"
        val refreshToken = "refresh-token"
        
        val loginResponse = LoginResponse(
            user = LoginResponse.User(
                id = userId,
                email = email,
                createdAt = System.currentTimeMillis()
            ),
            accessToken = accessToken,
            refreshToken = refreshToken
        )
        
        coEvery { authApi.login(any()) } returns Response.success(loginResponse)
        coEvery { dataStore.edit(any<suspend (Preferences) -> Unit>()) } coAnswers {
            // 模拟 DataStore 编辑操作
        }
        
        val userEntitySlot = slot<UserEntity>()
        coEvery { userDao.insertUser(capture(userEntitySlot)) } returns Unit

        // When
        val result = userRepository.login(email, password)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.id).isEqualTo(userId)
        assertThat(result.getOrNull()?.email).isEqualTo(email)
        
        val capturedEntity = userEntitySlot.captured
        assertThat(capturedEntity.id).isEqualTo(userId)
        assertThat(capturedEntity.email).isEqualTo(email)
        
        coVerify(exactly = 1) { authApi.login(LoginRequest(email, password)) }
        coVerify(exactly = 1) { userDao.insertUser(any()) }
        coVerify(exactly = 1) { dataStore.edit(any()) }
    }

    @Test
    fun `登录失败 - 服务器返回错误`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrong-password"
        
        coEvery { authApi.login(any()) } returns Response.error(401, mockk(relaxed = true))

        // When
        val result = userRepository.login(email, password)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Login failed: 401")
        
        coVerify(exactly = 1) { authApi.login(LoginRequest(email, password)) }
        coVerify(exactly = 0) { userDao.insertUser(any()) }
        coVerify(exactly = 0) { dataStore.edit(any()) }
    }

    @Test
    fun `登录失败 - 网络异常`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val exception = Exception("Network error")
        
        coEvery { authApi.login(any()) } throws exception

        // When
        val result = userRepository.login(email, password)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        
        coVerify(exactly = 1) { authApi.login(LoginRequest(email, password)) }
        coVerify(exactly = 0) { userDao.insertUser(any()) }
        coVerify(exactly = 0) { dataStore.edit(any()) }
    }

    @Test
    fun `登出`() = runTest {
        // Given
        coEvery { dataStore.edit(any<suspend (Preferences) -> Unit>()) } coAnswers {
            // 模拟 DataStore 编辑操作
        }
        coEvery { userDao.deleteAllUsers() } returns Unit

        // When
        userRepository.logout()

        // Then
        coVerify(exactly = 1) { dataStore.edit(any()) }
        coVerify(exactly = 1) { userDao.deleteAllUsers() }
    }

    @Test
    fun `获取当前用户流`() = runTest {
        // Given
        val userEntity = UserEntity(
            id = "user-123",
            email = "test@example.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { userDao.getCurrentUser() } returns flowOf(userEntity)

        // When
        val result = userRepository.getCurrentUserFlow().first()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo("user-123")
        assertThat(result?.email).isEqualTo("test@example.com")
        coVerify(exactly = 1) { userDao.getCurrentUser() }
    }

    @Test
    fun `获取当前用户流 - 无用户`() = runTest {
        // Given
        coEvery { userDao.getCurrentUser() } returns flowOf(null)

        // When
        val result = userRepository.getCurrentUserFlow().first()

        // Then
        assertThat(result).isNull()
        coVerify(exactly = 1) { userDao.getCurrentUser() }
    }

    @Test
    fun `获取当前用户`() = runTest {
        // Given
        val userId = "user-123"
        val userEntity = UserEntity(
            id = userId,
            email = "test@example.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { dataStore.data } returns flowOf(mockk {
            every { get(any()) } returns userId
        })
        coEvery { userDao.getUserById(userId) } returns userEntity

        // When
        val result = userRepository.getCurrentUser()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(userId)
        assertThat(result?.email).isEqualTo("test@example.com")
    }

    @Test
    fun `获取访问令牌`() = runTest {
        // Given
        val accessToken = "test-access-token"
        coEvery { dataStore.data } returns flowOf(mockk {
            every { get(any()) } returns accessToken
        })

        // When
        val result = userRepository.getAccessToken()

        // Then
        assertThat(result).isEqualTo(accessToken)
    }

    @Test
    fun `更新令牌`() = runTest {
        // Given
        val newAccessToken = "new-access-token"
        val newRefreshToken = "new-refresh-token"
        coEvery { dataStore.edit(any<suspend (Preferences) -> Unit>()) } coAnswers {
            // 模拟 DataStore 编辑操作
        }

        // When
        userRepository.updateTokens(newAccessToken, newRefreshToken)

        // Then
        coVerify(exactly = 1) { dataStore.edit(any()) }
    }

    @Test
    fun `获取最后同步时间`() = runTest {
        // Given
        val lastSyncTime = 1704067200000L // 2024-01-01
        coEvery { dataStore.data } returns flowOf(mockk {
            every { get(any()) } returns lastSyncTime
        })

        // When
        val result = userRepository.getLastSyncTime()

        // Then
        assertThat(result).isEqualTo(lastSyncTime)
    }

    @Test
    fun `获取最后同步时间 - 默认值`() = runTest {
        // Given
        coEvery { dataStore.data } returns flowOf(mockk {
            every { get(any()) } returns null
        })

        // When
        val result = userRepository.getLastSyncTime()

        // Then
        assertThat(result).isEqualTo(0L)
    }

    @Test
    fun `更新最后同步时间`() = runTest {
        // Given
        val newSyncTime = System.currentTimeMillis()
        coEvery { dataStore.edit(any<suspend (Preferences) -> Unit>()) } coAnswers {
            // 模拟 DataStore 编辑操作
        }

        // When
        userRepository.updateLastSyncTime(newSyncTime)

        // Then
        coVerify(exactly = 1) { dataStore.edit(any()) }
    }

    @Test
    fun `获取当前用户ID`() {
        // When
        val userId = userRepository.getCurrentUserId()

        // Then
        assertThat(userId).isEqualTo("current_user_id")
    }
}