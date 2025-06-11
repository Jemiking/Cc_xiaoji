package com.ccxiaoji.shared.backup.data.di

import android.content.Context
import com.ccxiaoji.shared.backup.api.BackupApi
import com.ccxiaoji.shared.backup.data.BackupApiImpl
import com.ccxiaoji.shared.backup.data.DatabaseBackupManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {
    
    @Binds
    @Singleton
    abstract fun bindBackupApi(impl: BackupApiImpl): BackupApi
    
    companion object {
        @Provides
        @Singleton
        fun provideDatabaseBackupManager(
            @ApplicationContext context: Context
        ): DatabaseBackupManager {
            return DatabaseBackupManager(context)
        }
    }
}