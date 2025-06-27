package com.ccxiaoji.core.network.di

import com.ccxiaoji.core.network.NetworkConstants
import com.ccxiaoji.core.network.client.NetworkClientConfig
import com.ccxiaoji.core.network.converter.GsonConfiguration
import com.ccxiaoji.core.network.interceptor.AuthInterceptor
import com.ccxiaoji.core.network.interceptor.TokenProvider
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * 核心网络模块
 * 提供基础的网络配置
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreNetworkModule {
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonConfiguration.create()
    }
    
    @Provides
    @Singleton
    @AuthorizedClient
    fun provideAuthorizedOkHttpClient(
        tokenProvider: TokenProvider,
        @IsDebug isDebug: Boolean
    ): OkHttpClient {
        val authInterceptor = AuthInterceptor(tokenProvider)
        return NetworkClientConfig.createClient(
            interceptors = listOf(authInterceptor),
            isDebug = isDebug
        )
    }
    
    @Provides
    @Singleton
    @BaseClient
    fun provideBaseOkHttpClient(
        @IsDebug isDebug: Boolean
    ): OkHttpClient {
        return NetworkClientConfig.createClient(
            isDebug = isDebug
        )
    }
    
    @Provides
    @Singleton
    @AuthorizedRetrofit
    fun provideAuthorizedRetrofit(
        @AuthorizedClient okHttpClient: OkHttpClient,
        gson: Gson,
        @BaseUrl baseUrl: String
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    @BaseRetrofit
    fun provideBaseRetrofit(
        @BaseClient okHttpClient: OkHttpClient,
        gson: Gson,
        @BaseUrl baseUrl: String
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}

/**
 * 限定符：带认证的OkHttpClient
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthorizedClient

/**
 * 限定符：基础OkHttpClient（无认证）
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseClient

/**
 * 限定符：带认证的Retrofit
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthorizedRetrofit

/**
 * 限定符：基础Retrofit（无认证）
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseRetrofit

/**
 * 限定符：基础URL
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

/**
 * 限定符：是否为调试模式
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IsDebug