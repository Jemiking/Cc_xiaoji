package com.ccxiaoji.app.data.backup

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {
    
    @Provides
    @Singleton
    fun provideDatabaseBackupManager(
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): DatabaseBackupManager {
        return DatabaseBackupManager(context)
    }
}