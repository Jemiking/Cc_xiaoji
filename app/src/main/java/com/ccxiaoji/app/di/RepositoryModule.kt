package com.ccxiaoji.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ccxiaoji.core.database.dao.*
import com.ccxiaoji.app.data.repository.CountdownRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    // UserRepository已迁移到shared:user模块
    // 使用UserApi代替

    @Provides
    @Singleton
    fun provideCountdownRepository(
        countdownDao: CountdownDao,
        changeLogDao: ChangeLogDao,
        gson: Gson
    ): CountdownRepository {
        return CountdownRepository(countdownDao, changeLogDao, gson)
    }

    // TaskRepository已迁移到feature:todo模块中提供
    
    // HabitRepository已迁移到feature:habit模块中提供
    
    // TransactionRepository已迁移到feature-ledger模块
    // 使用LedgerApi代替
    
    // AccountRepository已迁移到feature-ledger模块
    // 使用LedgerApi代替
    
    // BudgetRepository已迁移到feature-ledger模块
    // 使用LedgerApi代替

    // CategoryRepository已迁移到feature-ledger模块
    // 使用LedgerApi代替

    // RecurringTransactionRepository已迁移到feature-ledger模块
    // 使用LedgerApi代替

    // SavingsGoalRepository已迁移到feature-ledger模块
    // 使用LedgerApi代替
}