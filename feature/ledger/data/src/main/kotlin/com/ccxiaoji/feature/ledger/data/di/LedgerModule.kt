package com.ccxiaoji.feature.ledger.data.di

import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.data.LedgerApiImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Ledger模块的依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LedgerModule {
    
    @Binds
    @Singleton
    abstract fun bindLedgerApi(impl: LedgerApiImpl): LedgerApi
}