package com.ccxiaoji.feature.schedule.di

import com.ccxiaoji.feature.schedule.data.repository.ScheduleRepositoryImpl
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt模块 - 提供Repository依赖
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * 绑定排班Repository实现
     */
    @Binds
    @Singleton
    abstract fun bindScheduleRepository(
        scheduleRepositoryImpl: ScheduleRepositoryImpl
    ): ScheduleRepository
}