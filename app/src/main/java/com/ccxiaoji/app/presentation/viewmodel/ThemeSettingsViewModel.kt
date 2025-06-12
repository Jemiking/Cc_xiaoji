package com.ccxiaoji.app.presentation.viewmodel

import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.app.presentation.ui.profile.ThemeColor
import com.ccxiaoji.app.presentation.ui.profile.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_THEME_COLOR = stringPreferencesKey("theme_color")
        private val KEY_USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
    }
    
    private val _uiState = MutableStateFlow(ThemeSettingsUiState())
    val uiState: StateFlow<ThemeSettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadThemeSettings()
    }
    
    private fun loadThemeSettings() {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                val themeMode = preferences[KEY_THEME_MODE]?.let { 
                    ThemeMode.valueOf(it) 
                } ?: ThemeMode.SYSTEM
                
                val themeColor = preferences[KEY_THEME_COLOR]?.let { 
                    ThemeColor.valueOf(it) 
                } ?: ThemeColor.BLUE
                
                val useDynamicColor = preferences[KEY_USE_DYNAMIC_COLOR] ?: false
                
                _uiState.update {
                    it.copy(
                        themeMode = themeMode,
                        themeColor = themeColor,
                        useDynamicColor = useDynamicColor,
                        supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    )
                }
            }
        }
    }
    
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_THEME_MODE] = mode.name
            }
        }
    }
    
    fun setThemeColor(color: ThemeColor) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_THEME_COLOR] = color.name
            }
            // 如果选择了自定义颜色，自动关闭动态主题
            if (_uiState.value.useDynamicColor) {
                setUseDynamicColor(false)
            }
        }
    }
    
    fun setUseDynamicColor(use: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_USE_DYNAMIC_COLOR] = use
            }
        }
    }
}

data class ThemeSettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themeColor: ThemeColor = ThemeColor.BLUE,
    val useDynamicColor: Boolean = false,
    val supportsDynamicColor: Boolean = false
)