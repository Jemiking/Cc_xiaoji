package com.ccxiaoji.feature.ledger.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.ccxiaoji.feature.ledger.domain.repository.AutoLedgerDeveloperSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoLedgerDeveloperSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AutoLedgerDeveloperSettingsRepository {

    private val EMIT_WITHOUT_KEYWORDS_KEY = booleanPreferencesKey("auto_ledger_emit_without_keywords")
    private val EMIT_GROUP_SUMMARY_KEY = booleanPreferencesKey("auto_ledger_emit_group_summary")
    private val LOG_UNMATCHED_NOTIFICATIONS_KEY = booleanPreferencesKey("auto_ledger_log_unmatched")
    private val DEDUP_ENABLED_KEY = booleanPreferencesKey("auto_ledger_dedup_enabled")
    private val DEDUP_WINDOW_SEC_KEY = intPreferencesKey("auto_ledger_dedup_window_sec")

    override suspend fun setEmitWithoutKeywords(enabled: Boolean) {
        dataStore.edit { it[EMIT_WITHOUT_KEYWORDS_KEY] = enabled }
    }

    override suspend fun setEmitGroupSummary(enabled: Boolean) {
        dataStore.edit { it[EMIT_GROUP_SUMMARY_KEY] = enabled }
    }

    override suspend fun setLogUnmatched(enabled: Boolean) {
        dataStore.edit { it[LOG_UNMATCHED_NOTIFICATIONS_KEY] = enabled }
    }

    override suspend fun setDedupEnabled(enabled: Boolean) {
        dataStore.edit { it[DEDUP_ENABLED_KEY] = enabled }
    }

    override suspend fun setDedupWindowSec(seconds: Int) {
        dataStore.edit { it[DEDUP_WINDOW_SEC_KEY] = seconds.coerceIn(1, 600) }
    }
}

