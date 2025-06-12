package com.ccxiaoji.core.data.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Core Data 模块的依赖注入配置
 * 提供跨模块共享的数据层基础设施
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreDataModule {
    
    /**
     * 提供 Gson 实例
     * 用于 JSON 序列化和反序列化
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }
}