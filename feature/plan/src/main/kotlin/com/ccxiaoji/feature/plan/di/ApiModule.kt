package com.ccxiaoji.feature.plan.di

import com.ccxiaoji.feature.plan.api.PlanApi
import com.ccxiaoji.feature.plan.api.PlanApiImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * API依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ApiModule {
    
    @Binds
    @Singleton
    abstract fun bindPlanApi(
        impl: PlanApiImpl
    ): PlanApi
}