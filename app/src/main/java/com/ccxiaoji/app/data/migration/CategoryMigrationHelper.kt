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
    
    /**
     * 根据新系统的分类名称获取旧系统的枚举值
     * @param categoryName 新系统中的分类名称
     * @param isExpense 是否为支出类型
     * @return 对应的旧系统枚举值字符串
     */
    fun getCategoryEnumFromName(categoryName: String, isExpense: Boolean): String? {
        return if (isExpense) {
            when (categoryName) {
                "餐饮" -> "FOOD"
                "交通" -> "TRANSPORT"
                "购物" -> "SHOPPING"
                "娱乐" -> "ENTERTAINMENT"
                "医疗" -> "MEDICAL"
                "教育" -> "EDUCATION"
                "居住" -> "HOUSING"
                "水电" -> "UTILITIES"
                "通讯" -> "COMMUNICATION"
                "其他" -> "OTHER"
                else -> "OTHER"
            }
        } else {
            // 收入类型统一映射为 INCOME
            "INCOME"
        }
    }
    
    /**
     * 检查是否需要迁移
     * @param categoryId 新系统的分类ID
     * @return 如果有categoryId说明使用了新系统，需要进行迁移
     */
    fun needsMigration(categoryId: String?): Boolean {
        return categoryId != null
    }
}