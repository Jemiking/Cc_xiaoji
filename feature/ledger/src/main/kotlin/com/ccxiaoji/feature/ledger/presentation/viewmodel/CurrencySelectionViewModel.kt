package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.BasicSettings
import com.ccxiaoji.feature.ledger.presentation.screen.settings.CurrencyInfo
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CurrencySelectionUiState(
    val selectedCurrency: String = "CNY",
    val commonCurrencies: List<CurrencyInfo> = emptyList(),
    val otherCurrencies: List<CurrencyInfo> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CurrencySelectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CurrencySelectionUiState())
    val uiState: StateFlow<CurrencySelectionUiState> = _uiState.asStateFlow()
    
    companion object {
        private val BASIC_SETTINGS_KEY = stringPreferencesKey("basic_settings")
    }
    
    init {
        loadCurrencies()
        loadCurrentCurrency()
    }
    
    private fun loadCurrencies() {
        // 常用币种
        val commonCurrencies = listOf(
            CurrencyInfo("CNY", "人民币", "¥"),
            CurrencyInfo("USD", "美元", "$"),
            CurrencyInfo("EUR", "欧元", "€"),
            CurrencyInfo("JPY", "日元", "¥"),
            CurrencyInfo("GBP", "英镑", "£"),
            CurrencyInfo("HKD", "港币", "HK$")
        )
        
        // 其他币种
        val otherCurrencies = listOf(
            CurrencyInfo("TWD", "新台币", "NT$"),
            CurrencyInfo("KRW", "韩元", "₩"),
            CurrencyInfo("SGD", "新加坡元", "S$"),
            CurrencyInfo("AUD", "澳大利亚元", "A$"),
            CurrencyInfo("CAD", "加拿大元", "C$"),
            CurrencyInfo("THB", "泰铢", "฿"),
            CurrencyInfo("MYR", "马来西亚林吉特", "RM"),
            CurrencyInfo("PHP", "菲律宾比索", "₱"),
            CurrencyInfo("INR", "印度卢比", "₹"),
            CurrencyInfo("CHF", "瑞士法郎", "Fr")
        )
        
        _uiState.update { it.copy(
            commonCurrencies = commonCurrencies,
            otherCurrencies = otherCurrencies
        ) }
    }
    
    private fun loadCurrentCurrency() {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                val settingsJson = preferences[BASIC_SETTINGS_KEY] ?: return@map BasicSettings()
                gson.fromJson(settingsJson, BasicSettings::class.java)
            }.collect { settings ->
                _uiState.update { it.copy(selectedCurrency = settings.defaultCurrency) }
            }
        }
    }
    
    fun selectCurrency(currency: String) {
        _uiState.update { it.copy(selectedCurrency = currency) }
    }
}