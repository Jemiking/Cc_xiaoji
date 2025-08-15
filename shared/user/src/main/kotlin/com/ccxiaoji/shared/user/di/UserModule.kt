package com.ccxiaoji.shared.user.di

import com.ccxiaoji.core.network.di.AuthorizedRetrofit
import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.shared.user.api.UserApiImpl
import com.ccxiaoji.shared.user.data.remote.api.AuthApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {
    
    @Provides
    @Singleton
    fun provideAuthApi(@AuthorizedRetrofit retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserBindModule {
    
    @Binds
    @Singleton
    abstract fun bindUserApi(
        userApiImpl: UserApiImpl
    ): UserApi
    
}