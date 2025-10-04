package com.ccxiaoji.app.presentation.widget.ledger

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class LedgerWidgetPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) {
    private fun keyForWidget(widgetId: Int) = stringPreferencesKey("widget_ledger_$widgetId")

    suspend fun setLedgerId(widgetId: Int, ledgerId: String) {
        dataStore.edit { prefs ->
            prefs[keyForWidget(widgetId)] = ledgerId
        }
    }

    suspend fun getLedgerId(widgetId: Int): String? {
        val prefs = dataStore.data.first()
        return prefs[keyForWidget(widgetId)]
    }

    suspend fun remove(widgetId: Int) {
        dataStore.edit { prefs ->
            prefs.remove(keyForWidget(widgetId))
        }
    }
}

