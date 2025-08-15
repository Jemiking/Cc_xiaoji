package com.ccxiaoji.feature.ledger.di

import com.ccxiaoji.feature.ledger.data.importer.CsvLedgerImporter
import com.ccxiaoji.feature.ledger.data.importer.CsvParser
import com.ccxiaoji.feature.ledger.data.importer.converter.*
import com.ccxiaoji.feature.ledger.data.importer.validator.DataValidator
import com.ccxiaoji.feature.ledger.data.importer.resolver.ConflictResolver
import com.ccxiaoji.feature.ledger.domain.importer.LedgerImporter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ImportModule {
    
    @Binds
    @Singleton
    abstract fun bindLedgerImporter(
        csvLedgerImporter: CsvLedgerImporter
    ): LedgerImporter
    
    companion object {
        @Provides
        @Singleton
        fun provideCsvParser(): CsvParser {
            return CsvParser()
        }
        
        @Provides
        @Singleton
        fun provideAccountConverter(): AccountConverter {
            return AccountConverter()
        }
        
        @Provides
        @Singleton
        fun provideCategoryConverter(): CategoryConverter {
            return CategoryConverter()
        }
        
        @Provides
        @Singleton
        fun provideTransactionConverter(): TransactionConverter {
            return TransactionConverter()
        }
        
        @Provides
        @Singleton
        fun provideBudgetConverter(): BudgetConverter {
            return BudgetConverter()
        }
        
        @Provides
        @Singleton
        fun provideSavingsGoalConverter(): SavingsGoalConverter {
            return SavingsGoalConverter()
        }
        
        @Provides
        @Singleton
        fun provideDataValidator(): DataValidator {
            return DataValidator()
        }
        
        @Provides
        @Singleton
        fun provideConflictResolver(
            accountDao: com.ccxiaoji.feature.ledger.data.local.dao.AccountDao,
            categoryDao: com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao,
            transactionDao: com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
        ): ConflictResolver {
            return ConflictResolver(accountDao, categoryDao, transactionDao)
        }
    }
}