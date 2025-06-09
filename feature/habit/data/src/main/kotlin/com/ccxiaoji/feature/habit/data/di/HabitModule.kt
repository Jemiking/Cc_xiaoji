package com.ccxiaoji.feature.habit.data.di

import com.ccxiaoji.feature.habit.api.HabitApi
import com.ccxiaoji.feature.habit.data.HabitApiImpl
import com.ccxiaoji.feature.habit.data.repository.HabitRepository
import com.ccxiaoji.core.database.dao.HabitDao
import com.ccxiaoji.core.database.dao.ChangeLogDao
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HabitModule {
    
    @Binds
    abstract fun bindHabitApi(impl: HabitApiImpl): HabitApi
}

@Module
@InstallIn(SingletonComponent::class)
object HabitDataModule {
    
    @Provides
    @Singleton
    fun provideHabitRepository(
        habitDao: HabitDao,
        changeLogDao: ChangeLogDao,
        gson: Gson
    ): HabitRepository {
        return HabitRepository(habitDao, changeLogDao, gson)
    }
}

/**
 * Habit模块的桥接接口实现
 * 这些接口需要在app模块中提供具体实现
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HabitBridgeModule {
    
    // HabitNavigator需要在app模块中实现
    // HabitReminderScheduler需要在app模块中实现
}