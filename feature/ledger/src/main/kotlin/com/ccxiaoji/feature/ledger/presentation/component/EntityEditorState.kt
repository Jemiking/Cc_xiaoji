package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * 通用编辑页面状态容器
 *
 * 用于管理编辑页面的各种状态，包括加载、保存、模式切换等。
 * 支持响应式更新，可与ViewModel配合使用。
 */
@Stable
class EntityEditorState(
    isEditMode: Boolean = false,
    isLoading: Boolean = false,
    isSaving: Boolean = false,
    saveEnabled: Boolean = true,
    hasUnsavedChanges: Boolean = false,
    showDiscardConfirm: Boolean = true,
    snackbarHostState: SnackbarHostState? = null
) {
    /** 是否为编辑模式（false为新增模式） */
    var isEditMode by mutableStateOf(isEditMode)

    /** 是否正在加载数据 */
    var isLoading by mutableStateOf(isLoading)

    /** 是否正在保存 */
    var isSaving by mutableStateOf(isSaving)

    /** 保存按钮是否可用 */
    var saveEnabled by mutableStateOf(saveEnabled)

    /** 是否有未保存的变更 */
    var hasUnsavedChanges by mutableStateOf(hasUnsavedChanges)

    /** 是否启用返回时的变更确认 */
    var showDiscardConfirm by mutableStateOf(showDiscardConfirm)

    /** SnackBar消息宿主 */
    private var _snackbarHostState by mutableStateOf(snackbarHostState ?: SnackbarHostState())
    val snackbarHostState: SnackbarHostState get() = _snackbarHostState

    /**
     * 内部方法：更新SnackbarHostState
     */
    internal fun updateSnackbarHostState(newState: SnackbarHostState) {
        _snackbarHostState = newState
    }

    /**
     * 显示成功消息
     */
    suspend fun showSuccess(message: String) {
        snackbarHostState.showSnackbar(message)
    }

    /**
     * 显示错误消息
     */
    suspend fun showError(message: String) {
        snackbarHostState.showSnackbar(message)
    }
}

/**
 * 记忆并同步外部参数的状态容器
 *
 * 在Composition生命周期内保持状态稳定，
 * 通过LaunchedEffect同步外部参数变化。
 */
@Composable
fun rememberEntityEditorState(
    isEditMode: Boolean = false,
    isLoading: Boolean = false,
    isSaving: Boolean = false,
    saveEnabled: Boolean = true,
    hasUnsavedChanges: Boolean = false,
    showDiscardConfirm: Boolean = true,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
): EntityEditorState {
    val state = remember {
        EntityEditorState(
            isEditMode = isEditMode,
            isLoading = isLoading,
            isSaving = isSaving,
            saveEnabled = saveEnabled,
            hasUnsavedChanges = hasUnsavedChanges,
            showDiscardConfirm = showDiscardConfirm,
            snackbarHostState = snackbarHostState
        )
    }

    // 同步外部参数变化（使用SideEffect避免协程开销）
    SideEffect {
        state.isEditMode = isEditMode
        state.isLoading = isLoading
        state.isSaving = isSaving
        state.saveEnabled = saveEnabled
        state.hasUnsavedChanges = hasUnsavedChanges
        state.showDiscardConfirm = showDiscardConfirm
        // 使用内部方法更新private字段
        if (state.snackbarHostState != snackbarHostState) {
            state.updateSnackbarHostState(snackbarHostState)
        }
    }

    return state
}

/**
 * UI测试标记常量
 */
object EditorTestTags {
    const val SCAFFOLD = "entity_editor_scaffold"
    const val BACK_BUTTON = "editor_back_button"
    const val SAVE_BUTTON = "editor_save_button"
    const val DISCARD_DIALOG = "editor_discard_dialog"
    const val LOADING_OVERLAY = "editor_loading_overlay"
    const val SNACKBAR_HOST = "editor_snackbar_host"
    const val CONTENT_AREA = "editor_content_area"
}