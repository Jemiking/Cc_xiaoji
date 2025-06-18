package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant

data class Category(
    val id: String,
    val name: String,
    val type: Type,
    val icon: String = "📝",
    val color: String = "#6200EE",
    val parentId: String? = null,
    val displayOrder: Int = 0,
    val isSystem: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    enum class Type {
        INCOME,
        EXPENSE
    }
    
    companion object {
        // Default icons for quick selection
        val DEFAULT_EXPENSE_ICONS = listOf(
            "🍔", "🍕", "🍜", "🍱", "☕", // Food
            "🚗", "🚌", "🚇", "✈️", "🚕", // Transport
            "🛍️", "👕", "👗", "👠", "💄", // Shopping
            "🎮", "🎬", "🎵", "📚", "🎯", // Entertainment
            "🏥", "💊", "🩺", "🏃", "💪", // Health
            "🏠", "💡", "💧", "📱", "💻", // Home/Utilities
            "🎓", "📖", "✏️", "🖊️", "📝", // Education
            "🎁", "💐", "🎂", "🥳", "💝", // Gifts
            "✂️", "🧴", "🧼", "🪒", "💈", // Personal Care
            "📋", "💼", "🔧", "⚙️", "📊"  // Other
        )
        
        val DEFAULT_INCOME_ICONS = listOf(
            "💰", "💵", "💴", "💶", "💷", // Money
            "💼", "🏢", "👔", "📈", "📊", // Work
            "🎁", "🏆", "🥇", "🎖️", "🏅", // Bonus/Awards
            "📈", "💹", "📊", "💱", "🏦", // Investment
            "🏪", "🛒", "🏬", "🏭", "🏗️", // Business
            "🎯", "✅", "📋", "📑", "📄"  // Other
        )
        
        val DEFAULT_COLORS = listOf(
            "#F44336", "#E91E63", "#9C27B0", "#673AB7",
            "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
            "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
            "#FFEB3B", "#FFC107", "#FF9800", "#FF5722",
            "#795548", "#9E9E9E", "#607D8B", "#000000"
        )
    }
}