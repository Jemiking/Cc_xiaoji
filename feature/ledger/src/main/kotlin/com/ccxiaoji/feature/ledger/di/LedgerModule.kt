package com.ccxiaoji.feature.ledger.di

import com.ccxiaoji.shared.sync.data.local.dao.ChangeLogDao
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.api.LedgerApiImpl
import com.ccxiaoji.feature.ledger.data.local.dao.*
import com.ccxiaoji.feature.ledger.data.repository.*
import com.ccxiaoji.shared.user.api.UserApi
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LedgerModule {
    
    @Binds
    abstract fun bindLedgerApi(
        ledgerApiImpl: LedgerApiImpl
    ): LedgerApi
    
    companion object {

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao,
        changeLogDao: ChangeLogDao,
        userApi: UserApi,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        gson: Gson
    ): TransactionRepository {
        return TransactionRepository(transactionDao, changeLogDao, userApi, accountDao, categoryDao, gson)
    }

    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao,
        changeLogDao: ChangeLogDao,
        creditCardPaymentDao: CreditCardPaymentDao,
        creditCardBillDao: CreditCardBillDao,
        transactionDao: TransactionDao,
        userApi: UserApi,
        gson: Gson
    ): AccountRepository {
        return AccountRepository(accountDao, changeLogDao, creditCardPaymentDao, creditCardBillDao, transactionDao, userApi, gson)
    }

    @Provides
    @Singleton
    fun provideBudgetRepository(
        budgetDao: BudgetDao,
        userApi: UserApi
    ): BudgetRepository {
        return BudgetRepository(budgetDao, userApi)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao,
        userApi: UserApi
    ): CategoryRepository {
        return CategoryRepository(categoryDao, userApi)
    }

    @Provides
    @Singleton
    fun provideRecurringTransactionRepository(
        recurringTransactionDao: RecurringTransactionDao,
        transactionDao: TransactionDao,
        userApi: UserApi
    ): RecurringTransactionRepository {
        return RecurringTransactionRepository(recurringTransactionDao, transactionDao, userApi)
    }

    @Provides
    @Singleton
    fun provideSavingsGoalRepository(
        savingsGoalDao: SavingsGoalDao
    ): SavingsGoalRepository {
        return SavingsGoalRepository(savingsGoalDao)
    }
    }
}