package com.ccxiaoji.shared.user.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ccxiaoji.shared.user.data.local.dao.UserDao
import com.ccxiaoji.shared.user.data.local.entity.UserEntity
import com.ccxiaoji.shared.user.data.remote.api.AuthApi
import com.ccxiaoji.shared.user.data.remote.dto.LoginRequest
import com.ccxiaoji.shared.user.data.remote.dto.LoginResponse
import com.ccxiaoji.shared.user.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

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
    
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val loginResponse = response.body()!!
                
                // Save tokens
                dataStore.edit { preferences ->
                    preferences[KEY_ACCESS_TOKEN] = loginResponse.accessToken
                    preferences[KEY_REFRESH_TOKEN] = loginResponse.refreshToken
                    preferences[KEY_USER_ID] = loginResponse.user.id
                }
                
                // Save user to local database
                val userEntity = UserEntity(
                    id = loginResponse.user.id,
                    email = loginResponse.user.email,
                    createdAt = loginResponse.user.createdAt,
                    updatedAt = System.currentTimeMillis()
                )
                userDao.insertUser(userEntity)
                
                Result.success(loginResponse.user.toDomainModel())
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_REFRESH_TOKEN)
            preferences.remove(KEY_USER_ID)
        }
        userDao.deleteAllUsers()
    }
    
    fun getCurrentUserFlow(): Flow<User?> {
        return userDao.getCurrentUser().map { entity ->
            entity?.toDomainModel()
        }
    }
    
    suspend fun getCurrentUser(): User? {
        val userId = dataStore.data.map { it[KEY_USER_ID] }.firstOrNull()
        return userId?.let { id ->
            userDao.getUserById(id)?.toDomainModel()
        }
    }
    
    suspend fun getAccessToken(): String? {
        return dataStore.data.map { it[KEY_ACCESS_TOKEN] }.firstOrNull()
    }
    
    suspend fun getRefreshToken(): String? {
        return dataStore.data.map { it[KEY_REFRESH_TOKEN] }.firstOrNull()
    }
    
    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
        }
    }
    
    suspend fun getLastSyncTime(): Long {
        return dataStore.data.map { it[KEY_LAST_SYNC_TIME] ?: 0L }.firstOrNull() ?: 0L
    }
    
    suspend fun updateLastSyncTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIME] = timestamp
        }
    }
    
    suspend fun updateServerTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_SERVER_TIME] = timestamp
        }
    }
    
    fun getCurrentUserId(): String {
        // For now, return a fixed user ID
        // In a real app, this would get the actual logged-in user ID
        return "current_user_id"
    }
    
    
}

private fun UserEntity.toDomainModel(): User {
    return User(
        id = id,
        email = email,
        createdAt = createdAt
    )
}

private fun LoginResponse.User.toDomainModel(): User {
    return User(
        id = id,
        email = email,
        createdAt = createdAt
    )
}