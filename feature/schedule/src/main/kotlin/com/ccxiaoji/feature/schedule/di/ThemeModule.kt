package com.ccxiaoji.feature.schedule.di

import android.content.Context
import com.ccxiaoji.feature.schedule.presentation.theme.ThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt模块 - 提供主题相关依赖
 * 
 * 注意：当前排班模块使用独立的主题系统
 * 后续需要与主应用的主题系统进行整合
 */
@Module
@InstallIn(SingletonComponent::class)
object ThemeModule {
    
    /**
     * 提供主题管理器实例
     */
    @Provides
    @Singleton
    fun provideThemeManager(
        @ApplicationContext context: Context
    ): ThemeManager {
        return ThemeManager(context)
    }
}