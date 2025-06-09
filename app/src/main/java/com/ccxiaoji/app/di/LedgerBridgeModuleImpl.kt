package com.ccxiaoji.app.di

import com.ccxiaoji.app.navigation.LedgerNavigatorImpl
import com.ccxiaoji.feature.ledger.api.LedgerNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Ledger模块桥接接口的实现绑定
 * 在app模块中提供feature-ledger模块所需的接口实现
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LedgerBridgeModuleImpl {
    
    @Binds
    @Singleton
    abstract fun bindLedgerNavigator(impl: LedgerNavigatorImpl): LedgerNavigator
}