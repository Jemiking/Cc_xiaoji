package com.ccxiaoji.core.network.interceptor

import com.ccxiaoji.core.network.NetworkConstants
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 认证拦截器
 * 为请求添加认证令牌
 */
class AuthInterceptor(
    private val tokenProvider: TokenProvider
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider.getToken()
        
        val request = if (token != null) {
            original.newBuilder()
                .header(NetworkConstants.AUTHORIZATION_HEADER, "${NetworkConstants.BEARER_PREFIX}$token")
                .build()
        } else {
            original
        }
        
        return chain.proceed(request)
    }
}

/**
 * 令牌提供者接口
 * 用于解耦认证拦截器与具体的令牌存储实现
 */
interface TokenProvider {
    /**
     * 获取当前的访问令牌
     * @return 访问令牌，如果没有则返回null
     */
    fun getToken(): String?
}