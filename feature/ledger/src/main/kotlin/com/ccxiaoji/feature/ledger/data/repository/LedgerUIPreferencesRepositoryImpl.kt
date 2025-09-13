package com.ccxiaoji.feature.ledger.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ccxiaoji.feature.ledger.domain.model.LedgerUIPreferences
import com.ccxiaoji.feature.ledger.domain.model.LedgerUIStyle
import com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode
import com.ccxiaoji.feature.ledger.domain.repository.LedgerUIPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LedgerUIPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : LedgerUIPreferencesRepository {
    
    companion object {
        private val UI_STYLE_KEY = stringPreferencesKey("ledger_ui_style")
        private val ANIMATION_DURATION_KEY = intPreferencesKey("ledger_animation_duration_ms")
        private val ICON_DISPLAY_MODE_KEY = stringPreferencesKey("ledger_icon_display_mode")
        private val SELECTED_LEDGER_ID_KEY = stringPreferencesKey("ledger_selected_ledger_id")
    }
    
    override fun getUIPreferences(): Flow<LedgerUIPreferences> {
        return dataStore.data.map { preferences ->
            LedgerUIPreferences(
                uiStyle = LedgerUIStyle.safeValueOf(
                    preferences[UI_STYLE_KEY] ?: LedgerUIStyle.BALANCED.name
                ),
                animationDurationMs = preferences[ANIMATION_DURATION_KEY] ?: 300,
                iconDisplayMode = IconDisplayMode.safeValueOf(
                    preferences[ICON_DISPLAY_MODE_KEY] ?: IconDisplayMode.EMOJI.name
                ),
                selectedLedgerId = preferences[SELECTED_LEDGER_ID_KEY]
            )
        }
    }
    
    override suspend fun updateUIStyle(style: LedgerUIStyle) {
        dataStore.edit { preferences ->
            preferences[UI_STYLE_KEY] = style.name
        }
    }
    
    
    override suspend fun updateAnimationDuration(durationMs: Int) {
        val validDuration = durationMs.coerceIn(100, 1000) // 限制在100ms-1000ms
        dataStore.edit { preferences ->
            preferences[ANIMATION_DURATION_KEY] = validDuration
        }
    }
    
    override suspend fun updateIconDisplayMode(mode: IconDisplayMode) {
        dataStore.edit { preferences ->
            preferences[ICON_DISPLAY_MODE_KEY] = mode.name
        }
    }
    
    override suspend fun updateSelectedLedgerId(ledgerId: String?) {
        dataStore.edit { preferences ->
            if (ledgerId != null) {
                preferences[SELECTED_LEDGER_ID_KEY] = ledgerId
            } else {
                preferences.remove(SELECTED_LEDGER_ID_KEY)
            }
        }
    }
    
    override suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences[UI_STYLE_KEY] = LedgerUIStyle.BALANCED.name
            preferences[ANIMATION_DURATION_KEY] = 300
            preferences[ICON_DISPLAY_MODE_KEY] = IconDisplayMode.EMOJI.name
            preferences.remove(SELECTED_LEDGER_ID_KEY) // 清除账本选择，让系统重新选择默认账本
        }
    }
}
