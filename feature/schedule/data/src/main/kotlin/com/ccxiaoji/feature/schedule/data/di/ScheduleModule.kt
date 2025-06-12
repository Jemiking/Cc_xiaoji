package com.ccxiaoji.feature.schedule.data.di

import com.ccxiaoji.core.database.CcDatabase
import com.ccxiaoji.core.database.dao.ScheduleDao
import com.ccxiaoji.core.database.dao.ScheduleExportHistoryDao
import com.ccxiaoji.core.database.dao.ShiftDao
import com.ccxiaoji.feature.schedule.api.ScheduleApi
import com.ccxiaoji.feature.schedule.data.ScheduleApiImpl
import com.ccxiaoji.feature.schedule.data.repository.ScheduleRepositoryImpl
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 排班模块依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ScheduleModule {
    
    @Binds
    abstract fun bindScheduleRepository(
        impl: ScheduleRepositoryImpl
    ): ScheduleRepository
    
    @Binds
    abstract fun bindScheduleApi(
        impl: ScheduleApiImpl
    ): ScheduleApi
    
    companion object {
        @Provides
        @Singleton
        fun provideShiftDao(database: CcDatabase): ShiftDao {
            return database.shiftDao()
        }
        
        @Provides
        @Singleton
        fun provideScheduleDao(database: CcDatabase): ScheduleDao {
            return database.scheduleDao()
        }
        
        @Provides
        @Singleton
        fun provideScheduleExportHistoryDao(database: CcDatabase): ScheduleExportHistoryDao {
            return database.scheduleExportHistoryDao()
        }
    }
}