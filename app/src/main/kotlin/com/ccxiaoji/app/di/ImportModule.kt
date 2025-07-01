package com.ccxiaoji.app.di

import com.ccxiaoji.app.data.importer.ImportManager
import com.ccxiaoji.app.data.importer.ImportService
import com.ccxiaoji.shared.backup.api.BackupApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 导入功能的依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ImportModule {
    
    // ImportManager和ImportService会通过构造函数注入自动提供
    // 这里不需要显式提供，只要确保它们的依赖可用即可
    
    // 如果需要特殊配置，可以在这里添加@Provides方法
}