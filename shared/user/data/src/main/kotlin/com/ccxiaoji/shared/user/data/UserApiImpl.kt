package com.ccxiaoji.shared.user.data

import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.shared.user.api.UserInfo
import com.ccxiaoji.shared.user.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserApi实现类
 * 
 * 将内部的UserRepository适配为对外的UserApi接口
 */
@Singleton
class UserApiImpl @Inject constructor(
    private val userRepository: UserRepository
) : UserApi {
    
    override suspend fun login(email: String, password: String): Result<UserInfo> {
        return userRepository.login(email, password).map { user ->
            user.toUserInfo()
        }
    }
    
    override suspend fun logout() {
        userRepository.logout()
    }
    
    override suspend fun refreshToken(): Result<Boolean> {
        return userRepository.refreshToken()
    }
    
    override fun getCurrentUserFlow(): Flow<UserInfo?> {
        return userRepository.getCurrentUserFlow().map { user ->
            user?.toUserInfo()
        }
    }
    
    override suspend fun getCurrentUser(): UserInfo? {
        return userRepository.getCurrentUser()?.toUserInfo()
    }
    
    override suspend fun getCurrentUserId(): String {
        return userRepository.getCurrentUserId()
    }
    
    override suspend fun getAccessToken(): String? {
        return userRepository.getAccessToken()
    }
    
    override suspend fun isLoggedIn(): Boolean {
        return userRepository.isLoggedIn()
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