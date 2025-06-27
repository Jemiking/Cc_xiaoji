package com.ccxiaoji.feature.plan.di

import com.ccxiaoji.feature.plan.data.repository.PlanRepositoryImpl
import com.ccxiaoji.feature.plan.data.repository.TemplateRepositoryImpl
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import com.ccxiaoji.feature.plan.domain.repository.TemplateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindPlanRepository(
        impl: PlanRepositoryImpl
    ): PlanRepository
    
    @Binds
    @Singleton
    abstract fun bindTemplateRepository(
        impl: TemplateRepositoryImpl
    ): TemplateRepository
}