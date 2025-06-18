package com.ccxiaoji.feature.habit.di

import com.ccxiaoji.feature.habit.api.HabitApi
import com.ccxiaoji.feature.habit.data.HabitApiImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HabitModule {
    
    @Binds
    @Singleton
    abstract fun bindHabitApi(impl: HabitApiImpl): HabitApi
}