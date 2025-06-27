package com.ccxiaoji.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ccxiaoji.common.constants.DataStoreKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

abstract class BaseRepository(
    private val dataStore: DataStore<Preferences>
) {
    
    protected suspend fun getCurrentUserId(): String {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.CURRENT_USER_ID_KEY] ?: "default_user"
        }.first()
    }
}