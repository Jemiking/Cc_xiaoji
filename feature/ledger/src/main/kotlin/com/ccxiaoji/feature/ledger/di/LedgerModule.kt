package com.ccxiaoji.feature.ledger.di

import android.content.Context
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.api.LedgerApiImpl
import com.ccxiaoji.feature.ledger.data.repository.*
import com.ccxiaoji.feature.ledger.domain.repository.*
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
import com.ccxiaoji.feature.ledger.debug.RuntimeDataFlowValidator
import com.ccxiaoji.feature.ledger.debug.DefaultLedgerMechanismValidator
import com.ccxiaoji.feature.ledger.debug.StatePersistenceValidator
import com.ccxiaoji.feature.ledger.debug.EdgeCaseValidator
import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.feature.ledger.worker.creditcard.PaymentReminderScheduler
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LedgerModule {
    
    @Binds
    @Singleton
    abstract fun bindLedgerApi(
        ledgerApiImpl: LedgerApiImpl
    ): LedgerApi
    
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository
    
    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        impl: AccountRepositoryImpl
    ): AccountRepository
    
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository
    
    @Binds
    @Singleton
    abstract fun bindLedgerRepository(
        impl: LedgerRepositoryImpl
    ): LedgerRepository
    
    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        impl: BudgetRepositoryImpl
    ): BudgetRepository
    
    @Binds
    @Singleton
    abstract fun bindCreditCardBillRepository(
        impl: CreditCardBillRepositoryImpl
    ): CreditCardBillRepository
    
    @Binds
    @Singleton
    abstract fun bindFilterRepository(
        impl: FilterRepositoryImpl
    ): FilterRepository
    
    @Binds
    @Singleton
    abstract fun bindLedgerUIPreferencesRepository(
        impl: LedgerUIPreferencesRepositoryImpl
    ): LedgerUIPreferencesRepository
    
    companion object {
        @Provides
        @Singleton
        fun providePaymentReminderScheduler(
            @ApplicationContext context: Context
        ): PaymentReminderScheduler {
            return PaymentReminderScheduler(context)
        }
        
        @Provides
        @Singleton
        fun provideRuntimeDataFlowValidator(
            defaultLedgerValidator: DefaultLedgerMechanismValidator,
            statePersistenceValidator: StatePersistenceValidator,
            edgeCaseValidator: EdgeCaseValidator
        ): RuntimeDataFlowValidator {
            return RuntimeDataFlowValidator(defaultLedgerValidator, statePersistenceValidator, edgeCaseValidator)
        }
        
        @Provides
        @Singleton
        fun provideDefaultLedgerMechanismValidator(
            manageLedgerUseCase: ManageLedgerUseCase,
            userApi: UserApi,
            ledgerUIPreferencesRepository: LedgerUIPreferencesRepository
        ): DefaultLedgerMechanismValidator {
            return DefaultLedgerMechanismValidator(
                manageLedgerUseCase = manageLedgerUseCase,
                userApi = userApi,
                ledgerUIPreferencesRepository = ledgerUIPreferencesRepository
            )
        }
        
        @Provides
        @Singleton
        fun provideStatePersistenceValidator(
            ledgerUIPreferencesRepository: LedgerUIPreferencesRepository,
            manageLedgerUseCase: ManageLedgerUseCase,
            userApi: UserApi
        ): StatePersistenceValidator {
            return StatePersistenceValidator(
                ledgerUIPreferencesRepository = ledgerUIPreferencesRepository,
                manageLedgerUseCase = manageLedgerUseCase,
                userApi = userApi
            )
        }
        
        @Provides
        @Singleton
        fun provideEdgeCaseValidator(
            manageLedgerUseCase: ManageLedgerUseCase,
            userApi: UserApi,
            ledgerUIPreferencesRepository: LedgerUIPreferencesRepository
        ): EdgeCaseValidator {
            return EdgeCaseValidator(
                manageLedgerUseCase = manageLedgerUseCase,
                userApi = userApi,
                ledgerUIPreferencesRepository = ledgerUIPreferencesRepository
            )
        }
    }
}