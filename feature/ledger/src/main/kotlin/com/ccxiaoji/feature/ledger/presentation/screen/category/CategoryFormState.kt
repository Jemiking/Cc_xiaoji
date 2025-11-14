package com.ccxiaoji.feature.ledger.presentation.screen.category

import com.ccxiaoji.feature.ledger.domain.model.Category

/**
 * 分类表单状态
 *
 * 仅包含业务数据字段，UI元状态由EntityEditorState管理
 * 遵循单一数据源原则，避免状态重复
 */
data class CategoryFormState(
    // 分类基础数据
    val category: Category? = null,
    val type: Category.Type = Category.Type.EXPENSE,
    val name: String = "",
    val selectedIcon: String = "",
    val selectedColor: String = "",

    // 系统标识
    val isSystemCategory: Boolean = false,

    // 验证和错误
    val nameError: String? = null,
    val generalError: String? = null,
    val loadError: String? = null,

    // 状态标识
    val saveSuccess: Boolean = false
) {
    /**
     * 检查表单是否有效
     */
    val isValid: Boolean get() =
        name.isNotBlank() &&
        selectedIcon.isNotBlank() &&
        selectedColor.isNotBlank() &&
        nameError == null

    /**
     * 是否可以保存（非系统分类且表单有效）
     */
    val canSave: Boolean get() = !isSystemCategory && isValid
}