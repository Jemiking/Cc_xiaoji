package com.ccxiaoji.app.di

import com.ccxiaoji.app.presentation.ui.export.adapter.LedgerExportAdapter
import com.ccxiaoji.feature.ledger.api.LedgerApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 导出功能依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ExportModule {
    
    @Provides
    @Singleton
    fun provideLedgerExportAdapter(
        ledgerApi: LedgerApi
    ): LedgerExportAdapter {
        return LedgerExportAdapter(ledgerApi)
    }
}