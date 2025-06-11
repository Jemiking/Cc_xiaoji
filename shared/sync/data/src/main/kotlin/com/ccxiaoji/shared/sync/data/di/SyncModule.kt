package com.ccxiaoji.shared.sync.data.di

import com.ccxiaoji.shared.sync.api.SyncApi
import com.ccxiaoji.shared.sync.data.SyncApiImpl
import com.ccxiaoji.shared.sync.data.remote.SyncService
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * 同步模块的Hilt依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    
    /**
     * 绑定SyncApi接口到其实现
     */
    @Binds
    abstract fun bindSyncApi(impl: SyncApiImpl): SyncApi
    
    companion object {
        /**
         * 提供SyncService网络接口
         */
        @Provides
        @Singleton
        fun provideSyncService(retrofit: Retrofit): SyncService {
            return retrofit.create(SyncService::class.java)
        }
    }
}