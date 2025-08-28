package com.ccxiaoji.feature.ledger.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.ccxiaoji.feature.ledger.domain.repository.AutoLedgerSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 自动记账设置仓库实现（DataStore）
 */
@Singleton
class AutoLedgerSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AutoLedgerSettingsRepository {

    companion object {
        private val GLOBAL_ENABLED_KEY = booleanPreferencesKey("auto_ledger_global_enabled")
    }

    override fun globalEnabled(): Flow<Boolean> =
        dataStore.data.map { it[GLOBAL_ENABLED_KEY] ?: false }

    override suspend fun setGlobalEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[GLOBAL_ENABLED_KEY] = enabled
        }
    }
}

