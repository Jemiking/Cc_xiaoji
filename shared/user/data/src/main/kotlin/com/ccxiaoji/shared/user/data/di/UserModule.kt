package com.ccxiaoji.shared.user.data.di

import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.shared.user.data.UserApiImpl
import com.ccxiaoji.shared.user.data.remote.api.AuthApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * 用户模块依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {
    
    /**
     * 绑定UserApi实现
     */
    @Binds
    @Singleton
    abstract fun bindUserApi(impl: UserApiImpl): UserApi
    
    companion object {
        /**
         * 提供AuthApi
         */
        @Provides
        @Singleton
        fun provideAuthApi(retrofit: Retrofit): AuthApi {
            return retrofit.create(AuthApi::class.java)
        }
    }
}