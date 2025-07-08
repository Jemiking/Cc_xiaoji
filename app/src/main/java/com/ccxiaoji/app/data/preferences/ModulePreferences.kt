package com.ccxiaoji.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.moduleDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "module_preferences"
)

@Singleton
class ModulePreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.moduleDataStore
    
    companion object {
        val HIDDEN_MODULES = stringSetPreferencesKey("hidden_modules")
        val MODULE_ORDER = stringPreferencesKey("module_order")
        val FAVORITE_MODULES = stringSetPreferencesKey("favorite_modules")
        val USE_CLASSIC_LAYOUT = booleanPreferencesKey("use_classic_layout")
    }
    
    // 获取隐藏的模块
    val hiddenModules: Flow<Set<String>> = dataStore.data
        .map { preferences ->
            preferences[HIDDEN_MODULES] ?: emptySet()
        }
    
    // 获取模块排序
    val moduleOrder: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[MODULE_ORDER]
        }
    
    // 是否使用经典布局
    val useClassicLayout: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[USE_CLASSIC_LAYOUT] ?: false
        }
    
    // 切换模块显示/隐藏
    suspend fun toggleModuleVisibility(moduleId: String) {
        dataStore.edit { preferences ->
            val current = preferences[HIDDEN_MODULES] ?: emptySet()
            preferences[HIDDEN_MODULES] = if (moduleId in current) {
                current - moduleId
            } else {
                current + moduleId
            }
        }
    }
    
    // 保存模块顺序
    suspend fun saveModuleOrder(moduleIds: List<String>) {
        dataStore.edit { preferences ->
            preferences[MODULE_ORDER] = moduleIds.joinToString(",")
        }
    }
    
    // 切换布局模式
    suspend fun toggleLayoutMode() {
        dataStore.edit { preferences ->
            val current = preferences[USE_CLASSIC_LAYOUT] ?: false
            preferences[USE_CLASSIC_LAYOUT] = !current
        }
    }
}