package com.ccxiaoji.core.network

object NetworkConstants {
    const val DEFAULT_CONNECT_TIMEOUT = 30L
    const val DEFAULT_READ_TIMEOUT = 30L
    const val DEFAULT_WRITE_TIMEOUT = 30L
    
    const val AUTHORIZATION_HEADER = "Authorization"
    const val BEARER_PREFIX = "Bearer "
    
    // 基础URL应该通过BuildConfig或配置提供
    const val DEFAULT_BASE_URL = "https://api.ccxiaoji.com/"
}