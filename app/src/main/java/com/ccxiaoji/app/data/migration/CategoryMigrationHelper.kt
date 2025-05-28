package com.ccxiaoji.app.data.migration

import com.ccxiaoji.app.data.local.entity.CategoryType
import com.ccxiaoji.app.domain.model.TransactionCategory

object CategoryMigrationHelper {
    
    // 从旧的TransactionCategory枚举映射到新的分类系统
    fun mapLegacyCategoryToType(category: TransactionCategory): CategoryType {
        return when (category) {
            TransactionCategory.INCOME -> CategoryType.INCOME
            else -> CategoryType.EXPENSE
        }
    }
    
    // 获取旧分类对应的默认名称
    fun getLegacyCategoryName(category: TransactionCategory): String {
        return category.displayName
    }
    
    // 获取旧分类对应的默认图标
    fun getLegacyCategoryIcon(category: TransactionCategory): String {
        return category.icon
    }
    
    // 获取旧分类对应的默认颜色
    fun getLegacyCategoryColor(category: TransactionCategory): String {
        return when (category) {
            TransactionCategory.FOOD -> "#FF6B6B"
            TransactionCategory.TRANSPORT -> "#4ECDC4"
            TransactionCategory.SHOPPING -> "#45B7D1"
            TransactionCategory.ENTERTAINMENT -> "#F7DC6F"
            TransactionCategory.MEDICAL -> "#E74C3C"
            TransactionCategory.EDUCATION -> "#3498DB"
            TransactionCategory.HOUSING -> "#9B59B6"
            TransactionCategory.UTILITIES -> "#1ABC9C"
            TransactionCategory.COMMUNICATION -> "#34495E"
            TransactionCategory.INCOME -> "#27AE60"
            TransactionCategory.OTHER -> "#95A5A6"
        }
    }
    
    // 生成默认分类的ID（确保相同的旧分类总是映射到相同的新ID）
    fun generateCategoryId(userId: String, category: TransactionCategory): String {
        return "default_${userId}_${category.name.lowercase()}"
    }
}