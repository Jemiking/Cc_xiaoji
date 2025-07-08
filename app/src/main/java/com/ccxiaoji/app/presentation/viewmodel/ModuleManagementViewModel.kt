package com.ccxiaoji.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.app.data.preferences.ModulePreferencesRepository
import com.ccxiaoji.app.presentation.ui.navigation.ModuleInfo
import com.ccxiaoji.app.presentation.ui.navigation.defaultModules
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModuleManagementViewModel @Inject constructor(
    private val modulePreferences: ModulePreferencesRepository
) : ViewModel() {
    
    private val _modules = MutableStateFlow(defaultModules)
    val modules: StateFlow<List<ModuleInfo>> = _modules.asStateFlow()
    
    val hiddenModules = modulePreferences.hiddenModules.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )
    
    val useClassicLayout = modulePreferences.useClassicLayout.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    init {
        // 加载模块顺序
        viewModelScope.launch {
            modulePreferences.moduleOrder.collect { orderString ->
                if (!orderString.isNullOrEmpty()) {
                    val orderList = orderString.split(",")
                    val orderedModules = orderList.mapNotNull { id ->
                        defaultModules.find { it.id == id }
                    }
                    val remainingModules = defaultModules.filter { module ->
                        !orderList.contains(module.id)
                    }
                    _modules.value = orderedModules + remainingModules
                }
            }
        }
    }
    
    fun moveModule(from: Int, to: Int) {
        val list = _modules.value.toMutableList()
        val item = list.removeAt(from)
        list.add(to, item)
        _modules.value = list
    }
    
    fun toggleModuleVisibility(moduleId: String) {
        viewModelScope.launch {
            modulePreferences.toggleModuleVisibility(moduleId)
        }
    }
    
    fun toggleClassicLayout() {
        viewModelScope.launch {
            modulePreferences.toggleLayoutMode()
        }
    }
    
    fun saveChanges() {
        viewModelScope.launch {
            val moduleIds = _modules.value.map { it.id }
            modulePreferences.saveModuleOrder(moduleIds)
        }
    }
}