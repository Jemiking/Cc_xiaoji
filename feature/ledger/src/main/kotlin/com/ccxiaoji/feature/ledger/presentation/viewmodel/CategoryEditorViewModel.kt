package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.presentation.component.EntityEditorState
import com.ccxiaoji.feature.ledger.presentation.screen.category.CategoryFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 统一的分类编辑器 ViewModel
 *
 * 采用状态分离模式：
 * - formState: 业务数据状态（CategoryFormState）
 * - editorState: UI元状态（EntityEditorState）
 *
 * 支持新增和编辑两种模式：
 * - 新增模式：categoryId 为 null，需要提供 categoryType
 * - 编辑模式：categoryId 不为 null，从数据库加载现有分类
 *
 * 主要功能：
 * - 参数化初始化，自动识别模式
 * - 分类数据的增删改查
 * - 表单验证和错误处理
 * - 未保存更改检测
 * - 系统分类保护
 */
@HiltViewModel
class CategoryEditorViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var initialized = false
    private var originalState: CategoryState? = null

    // 从 SavedStateHandle 获取导航参数
    private var categoryIdArg: String? = savedStateHandle.get<String>("categoryId")
    private var categoryTypeArg: String? = savedStateHandle.get<String>("categoryType")

    // 业务数据状态
    private val _formState = MutableStateFlow(CategoryFormState())
    val formState: StateFlow<CategoryFormState> = _formState.asStateFlow()

    // UI元状态
    private val _editorState = MutableStateFlow(EntityEditorState())
    val editorState: StateFlow<EntityEditorState> = _editorState.asStateFlow()

    init {
        // 监听表单状态变化，自动更新编辑器状态
        viewModelScope.launch {
            formState.collect { form ->
                updateEditorStateFromForm(form)
            }
        }

        // 仅从SavedStateHandle初始化（导航参数场景）
        if (categoryIdArg != null || categoryTypeArg != null) {
            initializeInternal(categoryIdArg, categoryTypeArg)
        }
    }

    /**
     * 初始化方法，支持从 Composable 参数传入
     * 允许新增模式下重新设置类型
     */
    fun initialize(categoryId: String?, categoryType: String?) {
        // 编辑模式下，如果已初始化则不再重复
        if (initialized && !categoryId.isNullOrBlank()) return

        // 新增模式下，允许重新设置类型
        val needsUpdate = when {
            !initialized -> true
            categoryId.isNullOrBlank() && categoryType != categoryTypeArg -> true
            categoryId != categoryIdArg -> true
            else -> false
        }

        if (!needsUpdate) return

        // 更新参数到 SavedStateHandle
        categoryIdArg = categoryId
        categoryTypeArg = categoryType
        savedStateHandle["categoryId"] = categoryId
        savedStateHandle["categoryType"] = categoryType

        initializeInternal(categoryIdArg, categoryTypeArg)
    }

    private fun initializeInternal(categoryId: String?, categoryType: String?) {
        val isEditMode = !categoryId.isNullOrBlank()

        // 如果已经初始化过
        if (initialized) {
            // 新增模式下，如果类型改变，重新初始化
            if (!isEditMode && categoryType != null) {
                val newType = parseType(categoryType)
                if (newType != _formState.value.type) {
                    initializeNewCategory(categoryType)
                }
            }
            return
        }

        initialized = true
        _editorState.value = EntityEditorState(isEditMode = isEditMode)

        if (isEditMode) {
            loadCategory(categoryId!!)
        } else {
            initializeNewCategory(categoryType)
        }
    }

    /**
     * 加载现有分类（编辑模式）
     */
    private fun loadCategory(categoryId: String) {
        viewModelScope.launch {
            _editorState.value.isLoading = true

            try {
                val category = categoryRepository.getCategoryById(categoryId)
                if (category != null) {
                    // 保存原始状态用于检测更改
                    originalState = CategoryState(
                        name = category.name,
                        icon = category.icon,
                        color = category.color
                    )

                    _formState.update {
                        it.copy(
                            category = category,
                            type = category.type,
                            name = category.name,
                            selectedIcon = category.icon,
                            selectedColor = category.color,
                            isSystemCategory = category.isSystem
                        )
                    }

                    _editorState.value.isLoading = false
                    _editorState.value.saveEnabled = !category.isSystem
                } else {
                    _formState.update {
                        it.copy(loadError = "分类不存在")
                    }
                    _editorState.value.isLoading = false
                }
            } catch (e: Exception) {
                _formState.update {
                    it.copy(loadError = e.message ?: "加载分类失败")
                }
                _editorState.value.isLoading = false
            }
        }
    }

    /**
     * 初始化新分类（新增模式）
     */
    private fun initializeNewCategory(categoryType: String?) {
        val type = parseType(categoryType)
        val defaultIcon = when (type) {
            Category.Type.EXPENSE -> Category.DEFAULT_EXPENSE_ICONS.firstOrNull() ?: "💰"
            Category.Type.INCOME -> Category.DEFAULT_INCOME_ICONS.firstOrNull() ?: "💵"
        }
        val defaultColor = Category.DEFAULT_COLORS.firstOrNull() ?: "#FF6B6B"

        // 保存原始状态（保留名称，只更新图标和颜色的默认值）
        val currentName = _formState.value.name
        originalState = CategoryState(
            name = currentName,
            icon = defaultIcon,
            color = defaultColor
        )

        _formState.update {
            it.copy(
                type = type,
                name = currentName, // 保留用户已输入的名称
                selectedIcon = defaultIcon,
                selectedColor = defaultColor
            )
        }

        _editorState.value.isLoading = false
        _editorState.value.saveEnabled = currentName.trim().isNotEmpty()
        _editorState.value.hasUnsavedChanges = currentName.trim().isNotEmpty()
    }

    /**
     * 更新分类名称
     */
    fun updateName(name: String) {
        val trimmedName = name.trim()
        val error = when {
            trimmedName.isEmpty() -> "请输入分类名称"
            trimmedName.length > 20 -> "分类名称不能超过20个字符"
            else -> null
        }

        _formState.update {
            it.copy(
                name = name,
                nameError = error
            )
        }
    }

    /**
     * 更新图标
     */
    fun updateIcon(icon: String) {
        _formState.update {
            it.copy(selectedIcon = icon)
        }
    }

    /**
     * 更新颜色
     */
    fun updateColor(color: String) {
        _formState.update {
            it.copy(selectedColor = color)
        }
    }

    /**
     * 根据表单状态更新编辑器状态
     */
    private fun updateEditorStateFromForm(form: CategoryFormState) {
        val hasChanges = originalState?.let { original ->
            form.name.trim() != original.name ||
            form.selectedIcon != original.icon ||
            form.selectedColor != original.color
        } ?: false

        val canSave = form.nameError == null &&
                     form.name.trim().isNotEmpty() &&
                     form.selectedIcon.isNotEmpty() &&
                     form.selectedColor.isNotEmpty() &&
                     !form.isSystemCategory

        _editorState.value.hasUnsavedChanges = hasChanges
        _editorState.value.saveEnabled = canSave && hasChanges
    }

    /**
     * 保存分类
     */
    fun saveCategory() {
        val form = _formState.value
        val editor = _editorState.value

        // 编辑模式下检查是否有实际更改
        if (editor.isEditMode && !editor.hasUnsavedChanges) {
            // 没有更改，直接标记成功返回
            _formState.update { it.copy(saveSuccess = true) }
            return
        }

        // 验证必填项
        val trimmedName = form.name.trim()
        when {
            trimmedName.isEmpty() -> {
                _formState.update { it.copy(nameError = "请输入分类名称") }
                return
            }
            form.selectedIcon.isEmpty() -> {
                _formState.update { it.copy(generalError = "请选择图标") }
                return
            }
            form.selectedColor.isEmpty() -> {
                _formState.update { it.copy(generalError = "请选择颜色") }
                return
            }
        }

        viewModelScope.launch {
            _editorState.value.isSaving = true
            _formState.update { it.copy(generalError = null) }

            try {
                if (editor.isEditMode) {
                    updateExistingCategory(form, trimmedName)
                } else {
                    createNewCategory(form, trimmedName)
                }

                _editorState.value.isSaving = false
                _formState.update { it.copy(saveSuccess = true) }
            } catch (e: Exception) {
                handleSaveError(e)
            }
        }
    }

    /**
     * 更新现有分类
     */
    private suspend fun updateExistingCategory(form: CategoryFormState, name: String) {
        val category = form.category ?: throw IllegalStateException("分类数据未加载")

        // 检查是否有实际更改
        if (!_editorState.value.hasUnsavedChanges) {
            return
        }

        // 构建更新后的分类
        val updatedCategory = category.copy(
            name = if (category.isSystem) category.name else name,
            icon = form.selectedIcon,
            color = form.selectedColor
        )

        // 检查同级重名
        if (!category.isSystem && name != category.name) {
            // 使用 findCategoryByName 检查是否存在同名分类
            val existingCategory = categoryRepository.findCategoryByName(name)
            if (existingCategory != null &&
                existingCategory.id != category.id &&
                existingCategory.type == category.type &&
                existingCategory.parentId == category.parentId) {
                throw IllegalArgumentException("同级分类中已存在相同名称")
            }
        }

        categoryRepository.updateCategory(updatedCategory)
    }

    /**
     * 创建新分类
     */
    private suspend fun createNewCategory(form: CategoryFormState, name: String) {
        // 检查同级重名
        val existingCategory = categoryRepository.findCategoryByName(name)
        if (existingCategory != null &&
            existingCategory.type == form.type &&
            existingCategory.parentId == null) {
            throw IllegalArgumentException("同级分类中已存在相同名称")
        }

        categoryRepository.createCategory(
            name = name,
            type = form.type.name,
            icon = form.selectedIcon,
            color = form.selectedColor,
            parentId = null
        )
    }

    /**
     * 处理保存错误
     */
    private fun handleSaveError(e: Exception) {
        val errorMessage = when {
            e.message?.contains("重名") == true -> "分类名称已存在"
            e.message?.contains("duplicate", ignoreCase = true) == true -> "分类名称已存在"
            e.message?.contains("UNIQUE constraint failed") == true -> "分类名称已存在"
            e.message?.contains("constraint failed") == true -> "分类名称已存在"
            e.message?.contains("FOREIGN KEY") == true -> "该分类正在使用中，无法修改"
            else -> e.message ?: "保存失败，请重试"
        }

        _editorState.value.isSaving = false
        _formState.update {
            it.copy(generalError = errorMessage)
        }
    }

    // 以下方法已被EntityEditorScaffold替代，保留作为兼容性考虑
    // 待CategoryEditorScreen完成改造后可删除
    @Deprecated("使用EntityEditorScaffold内置的未保存变更处理")
    fun showUnsavedChangesDialog() {
        // No-op: 由EntityEditorScaffold处理
    }

    @Deprecated("使用EntityEditorScaffold内置的未保存变更处理")
    fun dismissUnsavedChangesDialog() {
        // No-op: 由EntityEditorScaffold处理
    }

    /**
     * 检查是否有未保存的更改
     */
    private fun checkHasChanges(name: String, icon: String, color: String): Boolean {
        val original = originalState ?: return false
        return name != original.name || icon != original.icon || color != original.color
    }

    /**
     * 解析分类类型
     */
    private fun parseType(typeString: String?): Category.Type {
        return when (typeString?.uppercase()) {
            "INCOME" -> Category.Type.INCOME
            else -> Category.Type.EXPENSE
        }
    }

    /**
     * 分类状态数据类，用于检测更改
     */
    private data class CategoryState(
        val name: String,
        val icon: String,
        val color: String
    )
}