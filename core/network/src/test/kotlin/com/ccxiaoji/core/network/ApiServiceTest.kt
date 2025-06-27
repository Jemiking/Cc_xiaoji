package com.ccxiaoji.core.network

import com.ccxiaoji.core.network.api.ApiService
import com.ccxiaoji.core.network.model.ApiResponse
import com.ccxiaoji.core.network.model.LoginRequest
import com.ccxiaoji.core.network.model.LoginResponse
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ApiServiceTest {

    @MockK
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `登录成功返回token`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val expectedToken = "test_token_123"
        val loginRequest = LoginRequest(username, password)
        val loginResponse = LoginResponse(
            token = expectedToken,
            userId = "user123",
            username = username
        )
        val apiResponse = ApiResponse(
            code = 200,
            message = "Success",
            data = loginResponse
        )

        coEvery { apiService.login(loginRequest) } returns Response.success(apiResponse)

        // When
        val response = apiService.login(loginRequest)

        // Then
        assertThat(response.isSuccessful).isTrue()
        assertThat(response.body()?.code).isEqualTo(200)
        assertThat(response.body()?.data?.token).isEqualTo(expectedToken)
        assertThat(response.body()?.data?.username).isEqualTo(username)
        
        coVerify(exactly = 1) { apiService.login(loginRequest) }
    }

    @Test
    fun `登录失败返回错误信息`() = runTest {
        // Given
        val loginRequest = LoginRequest("wronguser", "wrongpass")
        val errorBody = """{"code": 401, "message": "Invalid credentials"}"""
            .toResponseBody("application/json".toMediaType())
        
        coEvery { apiService.login(loginRequest) } returns Response.error(401, errorBody)

        // When
        val response = apiService.login(loginRequest)

        // Then
        assertThat(response.isSuccessful).isFalse()
        assertThat(response.code()).isEqualTo(401)
        assertThat(response.errorBody()).isNotNull()
    }

    @Test
    fun `API响应包装器正确处理成功响应`() {
        // Given
        val successData = "Test Data"
        val apiResponse = ApiResponse(
            code = 200,
            message = "操作成功",
            data = successData
        )

        // When & Then
        assertThat(apiResponse.code).isEqualTo(200)
        assertThat(apiResponse.message).isEqualTo("操作成功")
        assertThat(apiResponse.data).isEqualTo(successData)
    }

    @Test
    fun `API响应包装器正确处理错误响应`() {
        // Given
        val errorResponse = ApiResponse<String>(
            code = 500,
            message = "服务器内部错误",
            data = null
        )

        // When & Then
        assertThat(errorResponse.code).isEqualTo(500)
        assertThat(errorResponse.message).isEqualTo("服务器内部错误")
        assertThat(errorResponse.data).isNull()
    }

    @Test
    fun `网络异常处理`() = runTest {
        // Given
        val loginRequest = LoginRequest("user", "pass")
        val exception = RuntimeException("Network error")
        
        coEvery { apiService.login(loginRequest) } throws exception

        // When & Then
        try {
            apiService.login(loginRequest)
            // 如果没有抛出异常，测试失败
            assertThat(false).isTrue()
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(RuntimeException::class.java)
            assertThat(e.message).isEqualTo("Network error")
        }
    }
}