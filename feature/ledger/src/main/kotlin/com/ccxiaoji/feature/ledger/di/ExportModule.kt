package com.ccxiaoji.feature.ledger.di

import com.ccxiaoji.feature.ledger.data.export.CsvLedgerExporter
import com.ccxiaoji.feature.ledger.domain.export.LedgerExporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExportModule {
    
    @Binds
    @Singleton
    abstract fun bindLedgerExporter(
        csvLedgerExporter: CsvLedgerExporter
    ): LedgerExporter
}