package com.ccxiaoji.core.network.client

import com.ccxiaoji.core.network.NetworkConstants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * 网络客户端配置
 * 提供OkHttpClient的构建和配置
 */
object NetworkClientConfig {
    
    /**
     * 创建基础的OkHttpClient
     * @param interceptors 要添加的拦截器列表
     * @param isDebug 是否开启调试模式（影响日志级别）
     */
    fun createClient(
        interceptors: List<Interceptor> = emptyList(),
        isDebug: Boolean = false,
        connectTimeout: Long = NetworkConstants.DEFAULT_CONNECT_TIMEOUT,
        readTimeout: Long = NetworkConstants.DEFAULT_READ_TIMEOUT,
        writeTimeout: Long = NetworkConstants.DEFAULT_WRITE_TIMEOUT
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // 添加自定义拦截器
            interceptors.forEach { addInterceptor(it) }
            
            // 添加日志拦截器（仅在调试模式下开启详细日志）
            addInterceptor(createLoggingInterceptor(isDebug))
            
            // 设置超时时间
            connectTimeout(connectTimeout, TimeUnit.SECONDS)
            readTimeout(readTimeout, TimeUnit.SECONDS)
            writeTimeout(writeTimeout, TimeUnit.SECONDS)
        }.build()
    }
    
    /**
     * 创建日志拦截器
     */
    private fun createLoggingInterceptor(isDebug: Boolean): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (isDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
}