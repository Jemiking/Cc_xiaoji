package com.ccxiaoji.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

abstract class BaseRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val CURRENT_USER_ID_KEY = stringPreferencesKey("current_user_id")
    }
    
    protected suspend fun getCurrentUserId(): String {
        return dataStore.data.map { preferences ->
            preferences[CURRENT_USER_ID_KEY] ?: "default_user"
        }.first()
    }
}