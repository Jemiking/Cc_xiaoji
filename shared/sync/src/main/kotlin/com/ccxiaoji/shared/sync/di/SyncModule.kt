package com.ccxiaoji.shared.sync.di

import com.ccxiaoji.core.network.di.AuthorizedRetrofit
import com.ccxiaoji.shared.sync.data.remote.api.SyncService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {
    
    @Provides
    @Singleton
    fun provideSyncService(@AuthorizedRetrofit retrofit: Retrofit): SyncService {
        return retrofit.create(SyncService::class.java)
    }
}