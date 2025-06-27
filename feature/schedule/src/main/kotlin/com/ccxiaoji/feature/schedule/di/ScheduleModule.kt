package com.ccxiaoji.feature.schedule.di

import com.ccxiaoji.feature.schedule.api.ScheduleApi
import com.ccxiaoji.feature.schedule.data.ScheduleApiImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 排班管理模块的依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ScheduleModule {
    
    @Binds
    @Singleton
    abstract fun bindScheduleApi(impl: ScheduleApiImpl): ScheduleApi
}