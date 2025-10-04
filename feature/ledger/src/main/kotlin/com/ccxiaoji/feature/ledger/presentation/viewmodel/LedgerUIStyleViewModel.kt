package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.LedgerUIPreferences
import com.ccxiaoji.feature.ledger.domain.model.LedgerUIStyle
import com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode
import com.ccxiaoji.feature.ledger.domain.repository.LedgerUIPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI风格设置状态
 */
data class LedgerUIStyleUiState(
    val uiStyle: LedgerUIStyle = LedgerUIStyle.BALANCED,
    val animationDurationMs: Int = 300,
    val iconDisplayMode: IconDisplayMode = IconDisplayMode.EMOJI,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LedgerUIStyleViewModel @Inject constructor(
    private val uiPreferencesRepository: LedgerUIPreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LedgerUIStyleUiState())
    val uiState: StateFlow<LedgerUIStyleUiState> = _uiState.asStateFlow()
    
    // 提供给组件直接使用的UI偏好设置流
    val uiPreferences: StateFlow<LedgerUIPreferences> = uiPreferencesRepository.getUIPreferences()
        .onEach { preferences ->
            println("🎨 [LedgerUIStyleViewModel] UI偏好设置更新:")
            println("   - UI风格: ${preferences.uiStyle}")
            println("   - 图标模式: ${preferences.iconDisplayMode}")
            println("   - 动画时长: ${preferences.animationDurationMs}ms")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LedgerUIPreferences()
        )
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            uiPreferencesRepository.getUIPreferences().map { preferences ->
                LedgerUIStyleUiState(
                    uiStyle = preferences.uiStyle,
                    animationDurationMs = preferences.animationDurationMs,
                    iconDisplayMode = preferences.iconDisplayMode
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    /**
     * 更新UI风格
     */
    fun updateUIStyle(style: LedgerUIStyle) {
        _uiState.update { it.copy(uiStyle = style) }
        viewModelScope.launch {
            try {
                uiPreferencesRepository.updateUIStyle(style)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "保存设置失败: ${e.message}") }
            }
        }
    }
    
    /**
     * 切换到下一个UI风格（用于快速切换）
     */
    fun toggleUIStyle() {
        val currentStyle = _uiState.value.uiStyle
        val nextStyle = when (currentStyle) {
            LedgerUIStyle.BALANCED -> LedgerUIStyle.HYBRID
            LedgerUIStyle.HYBRID -> LedgerUIStyle.HIERARCHICAL
            LedgerUIStyle.HIERARCHICAL -> LedgerUIStyle.BALANCED
        }
        updateUIStyle(nextStyle)
    }
    
    
    /**
     * 更新动画持续时间
     */
    fun updateAnimationDuration(durationMs: Int) {
        val validDuration = durationMs.coerceIn(100, 1000) // 限制在100ms-1000ms
        _uiState.update { it.copy(animationDurationMs = validDuration) }
        viewModelScope.launch {
            try {
                uiPreferencesRepository.updateAnimationDuration(validDuration)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "保存设置失败: ${e.message}") }
            }
        }
    }
    
    /**
     * 更新图标显示模式
     */
    fun updateIconDisplayMode(mode: IconDisplayMode) {
        println("🔄 [LedgerUIStyleViewModel] 更新图标显示模式:")
        println("   - 当前模式: ${_uiState.value.iconDisplayMode}")
        println("   - 新模式: $mode")
        
        _uiState.update { it.copy(iconDisplayMode = mode) }
        viewModelScope.launch {
            try {
                uiPreferencesRepository.updateIconDisplayMode(mode)
                println("   ✅ 图标模式已保存到Repository")
            } catch (e: Exception) {
                println("   ❌ 保存图标模式失败: ${e.message}")
                _uiState.update { it.copy(error = "保存设置失败: ${e.message}") }
            }
        }
    }
    
    
    /**
     * 获取UI偏好设置对象
     */
    fun getUIPreferences(): LedgerUIPreferences {
        val state = _uiState.value
        return LedgerUIPreferences(
            uiStyle = state.uiStyle,
            animationDurationMs = state.animationDurationMs,
            iconDisplayMode = state.iconDisplayMode
        )
    }
    
    /**
     * 重置所有设置
     */
    fun resetAllSettings() {
        viewModelScope.launch {
            try {
                uiPreferencesRepository.resetToDefaults()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "重置设置失败: ${e.message}") }
            }
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
