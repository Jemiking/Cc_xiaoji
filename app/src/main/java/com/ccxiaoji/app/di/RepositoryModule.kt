package com.ccxiaoji.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ccxiaoji.core.database.dao.*
import com.ccxiaoji.app.data.repository.*
import com.ccxiaoji.app.data.remote.api.AuthApi
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        authApi: AuthApi,
        dataStore: DataStore<Preferences>
    ): UserRepository {
        return UserRepository(userDao, authApi, dataStore)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao,
        changeLogDao: ChangeLogDao,
        userRepository: UserRepository,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        gson: Gson
    ): TransactionRepository {
        return TransactionRepository(transactionDao, changeLogDao, userRepository, accountDao, categoryDao, gson)
    }

    // TaskRepository已迁移到feature:todo模块中提供
    
    // HabitRepository已迁移到feature:habit模块中提供

    @Provides
    @Singleton
    fun provideCountdownRepository(
        countdownDao: CountdownDao,
        changeLogDao: ChangeLogDao,
        gson: Gson
    ): CountdownRepository {
        return CountdownRepository(countdownDao, changeLogDao, gson)
    }

    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao,
        changeLogDao: ChangeLogDao,
        creditCardPaymentDao: CreditCardPaymentDao,
        creditCardBillDao: CreditCardBillDao,
        transactionDao: TransactionDao,
        gson: Gson
    ): AccountRepository {
        return AccountRepository(accountDao, changeLogDao, creditCardPaymentDao, creditCardBillDao, transactionDao, gson)
    }

    @Provides
    @Singleton
    fun provideBudgetRepository(
        budgetDao: BudgetDao
    ): BudgetRepository {
        return BudgetRepository(budgetDao)
    }

    // CategoryRepository已迁移到feature-ledger模块
    // 使用LedgerApi代替

    @Provides
    @Singleton
    fun provideRecurringTransactionRepository(
        recurringTransactionDao: RecurringTransactionDao,
        transactionDao: TransactionDao
    ): RecurringTransactionRepository {
        return RecurringTransactionRepository(recurringTransactionDao, transactionDao)
    }

    @Provides
    @Singleton
    fun provideSavingsGoalRepository(
        savingsGoalDao: SavingsGoalDao
    ): SavingsGoalRepository {
        return SavingsGoalRepository(savingsGoalDao)
    }
}