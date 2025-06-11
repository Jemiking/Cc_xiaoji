package com.ccxiaoji.shared.user.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ccxiaoji.core.database.dao.UserDao
import com.ccxiaoji.core.database.entity.UserEntity
import com.ccxiaoji.shared.user.data.remote.api.AuthApi
import com.ccxiaoji.shared.user.data.remote.dto.LoginRequest
import com.ccxiaoji.shared.user.data.remote.dto.RefreshTokenRequest
import com.ccxiaoji.shared.user.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户数据仓库
 * 
 * 负责管理用户认证、用户信息和同步相关数据
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val authApi: AuthApi,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        private val KEY_SERVER_TIME = longPreferencesKey("server_time")
    }
    
    /**
     * 用户登录
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val loginResponse = response.body()!!
                
                // 保存Token
                dataStore.edit { preferences ->
                    preferences[KEY_ACCESS_TOKEN] = loginResponse.accessToken
                    preferences[KEY_REFRESH_TOKEN] = loginResponse.refreshToken
                    preferences[KEY_USER_ID] = loginResponse.user.id
                }
                
                // 保存用户到本地数据库
                val userEntity = UserEntity(
                    id = loginResponse.user.id,
                    email = loginResponse.user.email,
                    createdAt = loginResponse.user.createdAt,
                    updatedAt = System.currentTimeMillis()
                )
                userDao.insertUser(userEntity)
                
                Result.success(loginResponse.user.toDomainModel())
            } else {
                Result.failure(Exception("登录失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 用户登出
     */
    suspend fun logout() {
        // 清除本地Token
        dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_REFRESH_TOKEN)
            preferences.remove(KEY_USER_ID)
        }
        
        // 调用服务端登出接口（忽略结果）
        try {
            authApi.logout()
        } catch (e: Exception) {
            // 忽略网络错误
        }
        
        // 清除本地用户数据
        userDao.deleteAllUsers()
    }
    
    /**
     * 刷新Token
     */
    suspend fun refreshToken(): Result<Boolean> {
        return try {
            val refreshToken = getRefreshToken()
            if (refreshToken.isNullOrEmpty()) {
                return Result.failure(Exception("无刷新令牌"))
            }
            
            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful) {
                val refreshResponse = response.body()!!
                
                // 更新Token
                dataStore.edit { preferences ->
                    preferences[KEY_ACCESS_TOKEN] = refreshResponse.accessToken
                    preferences[KEY_REFRESH_TOKEN] = refreshResponse.refreshToken
                }
                
                Result.success(true)
            } else {
                Result.failure(Exception("刷新令牌失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取当前用户Flow
     */
    fun getCurrentUserFlow(): Flow<User?> {
        return userDao.getCurrentUser().map { entity ->
            entity?.toDomainModel()
        }
    }
    
    /**
     * 获取当前用户
     */
    suspend fun getCurrentUser(): User? {
        val userId = dataStore.data.map { it[KEY_USER_ID] }.firstOrNull()
        return userId?.let { id ->
            userDao.getUserById(id)?.toDomainModel()
        }
    }
    
    /**
     * 获取当前用户ID
     */
    suspend fun getCurrentUserId(): String {
        // 优先从DataStore获取
        val userId = dataStore.data.map { it[KEY_USER_ID] }.firstOrNull()
        return userId ?: "current_user_id" // 默认值
    }
    
    /**
     * 获取访问令牌
     */
    suspend fun getAccessToken(): String? {
        return dataStore.data.map { it[KEY_ACCESS_TOKEN] }.firstOrNull()
    }
    
    /**
     * 获取刷新令牌
     */
    suspend fun getRefreshToken(): String? {
        return dataStore.data.map { it[KEY_REFRESH_TOKEN] }.firstOrNull()
    }
    
    /**
     * 更新Token
     */
    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
        }
    }
    
    /**
     * 获取最后同步时间
     */
    suspend fun getLastSyncTime(): Long {
        return dataStore.data.map { it[KEY_LAST_SYNC_TIME] ?: 0L }.firstOrNull() ?: 0L
    }
    
    /**
     * 更新最后同步时间
     */
    suspend fun updateLastSyncTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIME] = timestamp
        }
    }
    
    /**
     * 更新服务器时间
     */
    suspend fun updateServerTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_SERVER_TIME] = timestamp
        }
    }
    
    /**
     * 检查是否已登录
     */
    suspend fun isLoggedIn(): Boolean {
        val token = getAccessToken()
        val userId = dataStore.data.map { it[KEY_USER_ID] }.firstOrNull()
        return !token.isNullOrEmpty() && !userId.isNullOrEmpty()
    }
}

/**
 * UserEntity转换为领域模型
 */
private fun UserEntity.toDomainModel(): User {
    return User(
        id = id,
        email = email,
        createdAt = createdAt
    )
}