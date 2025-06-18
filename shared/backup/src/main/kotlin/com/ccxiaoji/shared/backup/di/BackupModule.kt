package com.ccxiaoji.shared.backup.di

import com.ccxiaoji.shared.backup.api.BackupApi
import com.ccxiaoji.shared.backup.data.BackupApiImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 备份模块的依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {
    
    @Binds
    @Singleton
    abstract fun bindBackupApi(impl: BackupApiImpl): BackupApi
}