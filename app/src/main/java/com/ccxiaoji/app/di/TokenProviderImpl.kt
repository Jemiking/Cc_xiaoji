package com.ccxiaoji.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ccxiaoji.core.network.interceptor.TokenProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TokenProvider的实现
 * 从DataStore中获取访问令牌
 */
@Singleton
class TokenProviderImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TokenProvider {
    
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    }
    
    override fun getToken(): String? {
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[ACCESS_TOKEN_KEY]
            }.first()
        }
    }
}