package com.ccxiaoji.feature.ledger.domain.model

/**
 * 分类组 - 用于表示二级分类结构
 * 包含一个父分类和其所有子分类
 */
data class CategoryGroup(
    val parent: Category,        // 一级分类（父分类）
    val children: List<Category> // 二级分类列表（子分类）
) {
    /**
     * 获取所有分类（包括父分类和子分类）
     */
    fun getAllCategories(): List<Category> {
        return listOf(parent) + children
    }
    
    /**
     * 获取活跃的子分类
     */
    fun getActiveChildren(): List<Category> {
        return children.filter { it.isActive }
    }
    
    /**
     * 检查是否有子分类
     */
    fun hasChildren(): Boolean {
        return children.isNotEmpty()
    }
    
    /**
     * 根据ID查找分类
     */
    fun findCategoryById(categoryId: String): Category? {
        if (parent.id == categoryId) return parent
        return children.find { it.id == categoryId }
    }
}

/**
 * 分类选择信息 - 用于交易记录中保存选中的分类信息
 */
data class SelectedCategoryInfo(
    val categoryId: String,      // 分类ID（二级分类）
    val categoryName: String,    // 分类名称
    val parentId: String?,       // 父分类ID
    val parentName: String?,     // 父分类名称
    val fullPath: String,        // 完整路径，如"餐饮/早餐"
    val icon: String,            // 图标
    val color: String            // 颜色
) {
    /**
     * 获取显示名称（格式：父分类·子分类）
     */
    fun getDisplayName(): String {
        return if (parentName != null) {
            "$parentName·$categoryName"
        } else {
            categoryName
        }
    }
    
    /**
     * 获取简短显示名称（只显示子分类名）
     */
    fun getShortName(): String {
        return categoryName
    }
}