package com.ccxiaoji.shared.user.api

import kotlinx.coroutines.flow.Flow

/**
 * 用户模块对外API接口
 * 
 * 提供用户认证、用户信息管理和同步相关功能
 */
interface UserApi {
    
    // ========== 认证相关 ==========
    
    /**
     * 用户登录
     * @param email 邮箱
     * @param password 密码
     * @return 登录结果，成功返回用户信息
     */
    suspend fun login(email: String, password: String): Result<UserInfo>
    
    /**
     * 用户登出
     */
    suspend fun logout()
    
    /**
     * 刷新访问令牌
     * @return 刷新是否成功
     */
    suspend fun refreshToken(): Result<Boolean>
    
    // ========== 用户信息 ==========
    
    /**
     * 获取当前用户信息Flow
     * @return 用户信息Flow，未登录时为null
     */
    fun getCurrentUserFlow(): Flow<UserInfo?>
    
    /**
     * 获取当前用户信息
     * @return 用户信息，未登录时为null
     */
    suspend fun getCurrentUser(): UserInfo?
    
    /**
     * 获取当前用户ID
     * @return 用户ID，默认返回"current_user_id"
     */
    suspend fun getCurrentUserId(): String
    
    // ========== Token管理 ==========
    
    /**
     * 获取访问令牌
     * @return 访问令牌，未登录时为null
     */
    suspend fun getAccessToken(): String?
    
    /**
     * 检查用户是否已登录
     * @return 是否已登录
     */
    suspend fun isLoggedIn(): Boolean
    
    // ========== 同步管理 ==========
    
    /**
     * 获取最后同步时间
     * @return 时间戳，未同步时返回0
     */
    suspend fun getLastSyncTime(): Long
    
    /**
     * 更新最后同步时间
     * @param timestamp 时间戳
     */
    suspend fun updateLastSyncTime(timestamp: Long)
    
    /**
     * 更新服务器时间
     * @param timestamp 服务器时间戳
     */
    suspend fun updateServerTime(timestamp: Long)
}

/**
 * 用户信息数据类
 */
data class UserInfo(
    val id: String,
    val email: String,
    val createdAt: Long
)