package com.ccxiaoji.app.di

import com.ccxiaoji.core.network.di.AuthorizedRetrofit
import com.ccxiaoji.core.network.di.BaseUrl
import com.ccxiaoji.core.network.di.IsDebug
import com.ccxiaoji.core.network.interceptor.TokenProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "https://api.ccxiaoji.com/" // Replace with actual API URL
    
    @Provides
    @BaseUrl
    fun provideBaseUrl(): String = BASE_URL
    
    @Provides
    @IsDebug
    fun provideIsDebug(): Boolean = true // 临时使用true，实际应该使用BuildConfig.DEBUG
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindModule {
    
    @Binds
    @Singleton
    abstract fun bindTokenProvider(
        tokenProviderImpl: TokenProviderImpl
    ): TokenProvider
}