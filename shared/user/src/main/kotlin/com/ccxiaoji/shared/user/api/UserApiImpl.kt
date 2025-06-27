package com.ccxiaoji.shared.user.api

import com.ccxiaoji.shared.user.data.repository.UserRepository
import com.ccxiaoji.shared.user.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserApiImpl @Inject constructor(
    private val userRepository: UserRepository
) : UserApi {
    
    override suspend fun login(email: String, password: String): Result<User> {
        return userRepository.login(email, password)
    }
    
    override suspend fun logout() {
        userRepository.logout()
    }
    
    override suspend fun getCurrentUser(): User? {
        return userRepository.getCurrentUser()
    }
    
    override fun getCurrentUserFlow(): Flow<User?> {
        return userRepository.getCurrentUserFlow()
    }
    
    override fun getCurrentUserId(): String {
        return userRepository.getCurrentUserId()
    }
    
    override suspend fun getAccessToken(): String? {
        return userRepository.getAccessToken()
    }
    
    override suspend fun getRefreshToken(): String? {
        return userRepository.getRefreshToken()
    }
    
    override suspend fun updateTokens(accessToken: String, refreshToken: String) {
        userRepository.updateTokens(accessToken, refreshToken)
    }
    
    override suspend fun getLastSyncTime(): Long {
        return userRepository.getLastSyncTime()
    }
    
    override suspend fun updateLastSyncTime(timestamp: Long) {
        userRepository.updateLastSyncTime(timestamp)
    }
    
    override suspend fun updateServerTime(timestamp: Long) {
        userRepository.updateServerTime(timestamp)
    }
}