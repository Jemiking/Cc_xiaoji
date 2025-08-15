package com.ccxiaoji.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ccxiaoji.app.data.local.dao.*
import com.ccxiaoji.app.data.repository.*
import com.ccxiaoji.shared.sync.data.local.dao.ChangeLogDao
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
    fun provideCountdownRepository(
        countdownDao: CountdownDao,
        changeLogDao: ChangeLogDao,
        gson: Gson
    ): CountdownRepository {
        return CountdownRepository(countdownDao, changeLogDao, gson)
    }
}