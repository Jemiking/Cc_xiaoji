package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * 分类验证UseCase
 * 用于验证分类的合法性和业务规则
 */
class ValidateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    /**
     * 验证分类是否可以被删除
     * @param categoryId 分类ID
     * @return 验证结果
     */
    suspend fun canDeleteCategory(categoryId: String): ValidationResult {
        val category = categoryRepository.getCategoryById(categoryId)
            ?: return ValidationResult.Error("分类不存在")
        
        // 系统分类不能删除
        if (category.isSystem) {
            return ValidationResult.Error("系统分类不能删除")
        }
        
        // 检查是否有子分类（如果是父分类）
        if (category.level == 1) {
            val tree = categoryRepository.getCategoryTree(category.id, category.type.name)
            val hasChildren = tree.any { it.parent.id == categoryId && it.children.isNotEmpty() }
            if (hasChildren) {
                return ValidationResult.Error("该分类下还有子分类，请先删除子分类")
            }
        }
        
        // TODO: 检查是否有关联的交易记录
        // 这部分需要TransactionRepository支持，暂时跳过
        
        return ValidationResult.Success
    }
    
    /**
     * 验证分类名称是否有效
     * @param name 分类名称
     * @param parentId 父分类ID（如果是子分类）
     * @param userId 用户ID
     * @param type 分类类型
     * @return 验证结果
     */
    suspend fun validateCategoryName(
        name: String,
        parentId: String?,
        userId: String,
        type: String
    ): ValidationResult {
        // 验证名称长度
        if (name.isBlank()) {
            return ValidationResult.Error("分类名称不能为空")
        }
        
        if (name.length > 20) {
            return ValidationResult.Error("分类名称不能超过20个字符")
        }
        
        // 验证名称是否包含特殊字符
        if (name.contains("/") || name.contains("\\")) {
            return ValidationResult.Error("分类名称不能包含斜杠")
        }
        
        // 验证名称是否重复
        if (parentId == null) {
            // 一级分类名称不重复
            val parents = categoryRepository.getParentCategories(userId, type)
            if (parents.any { it.name == name }) {
                return ValidationResult.Error("该分类名称已存在")
            }
        } else {
            // 二级分类在同一父分类下名称不重复
            val tree = categoryRepository.getCategoryTree(userId, type)
            val parentGroup = tree.find { it.parent.id == parentId }
            if (parentGroup?.children?.any { it.name == name } == true) {
                return ValidationResult.Error("该子分类名称已存在")
            }
        }
        
        return ValidationResult.Success
    }
    
    /**
     * 验证分类层级
     * 确保不超过2级
     */
    suspend fun validateCategoryLevel(categoryId: String, newParentId: String?): ValidationResult {
        if (newParentId == null) {
            // 设为一级分类，总是允许
            return ValidationResult.Success
        }
        
        val parent = categoryRepository.getCategoryById(newParentId)
            ?: return ValidationResult.Error("父分类不存在")
        
        // 父分类必须是一级分类
        if (parent.level != 1) {
            return ValidationResult.Error("只支持两级分类结构")
        }
        
        // 不能将分类设为自己的子分类
        if (categoryId == newParentId) {
            return ValidationResult.Error("分类不能作为自己的子分类")
        }
        
        return ValidationResult.Success
    }
    
    /**
     * 验证结果
     */
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
        
        val isValid: Boolean
            get() = this is Success
        
        val errorMessage: String?
            get() = (this as? Error)?.message
    }
}