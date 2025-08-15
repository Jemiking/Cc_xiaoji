package com.ccxiaoji.shared.user.api

import com.ccxiaoji.shared.user.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * 用户模块对外API接口
 */
interface UserApi {
    /**
     * 登录
     */
    suspend fun login(email: String, password: String): Result<User>
    
    /**
     * 登出
     */
    suspend fun logout()
    
    /**
     * 获取当前用户
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * 获取当前用户流
     */
    fun getCurrentUserFlow(): Flow<User?>
    
    /**
     * 获取当前用户ID
     */
    fun getCurrentUserId(): String
    
    /**
     * 获取访问令牌
     */
    suspend fun getAccessToken(): String?
    
    /**
     * 获取刷新令牌
     */
    suspend fun getRefreshToken(): String?
    
    /**
     * 更新令牌
     */
    suspend fun updateTokens(accessToken: String, refreshToken: String)
    
    /**
     * 获取最后同步时间
     */
    suspend fun getLastSyncTime(): Long
    
    /**
     * 更新最后同步时间
     */
    suspend fun updateLastSyncTime(timestamp: Long)
    
    /**
     * 更新服务器时间
     */
    suspend fun updateServerTime(timestamp: Long)
}