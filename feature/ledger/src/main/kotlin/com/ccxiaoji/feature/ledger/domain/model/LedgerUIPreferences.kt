package com.ccxiaoji.feature.ledger.domain.model

/**
 * 记账UI风格偏好设置
 */
data class LedgerUIPreferences(
    // UI风格类型
    val uiStyle: LedgerUIStyle = LedgerUIStyle.BALANCED,
    // 动画持续时间（毫秒）
    val animationDurationMs: Int = 300,
    // 分类图标显示模式
    val iconDisplayMode: IconDisplayMode = IconDisplayMode.EMOJI,
    // 最后选择的记账簿ID（用于恢复用户选择）
    val selectedLedgerId: String? = null
)

/**
 * 记账UI风格枚举
 * 基于设计方案Demo中的选择
 */
enum class LedgerUIStyle(
    val displayName: String,
    val description: String
) {
    BALANCED(
        displayName = "平衡增强设计", 
        description = "增强总览卡片视觉权重，适度紧凑交易列表，支持日期分组"
    ),
    HIERARCHICAL(
        displayName = "层次化设计",
        description = "品牌色彩的极简设计，突出重要信息，清晰的视觉层次"
    ),
    HYBRID(
        displayName = "混合设计",
        description = "层次化概览卡片 + 平衡增强交易列表"
    );
    
    companion object {
        /**
         * 安全的值转换，如果无效则返回默认值
         */
        fun safeValueOf(name: String): LedgerUIStyle {
            return try {
                valueOf(name)
            } catch (e: IllegalArgumentException) {
                BALANCED // 默认使用平衡设计
            }
        }
        
        /**
         * 获取所有可用的风格选项
         */
        fun getAllStyles(): List<LedgerUIStyle> {
            return values().toList()
        }
    }
}

/**
 * 分类图标显示模式枚举
 * 控制分类图标的显示方式
 */
enum class IconDisplayMode(
    val displayName: String,
    val description: String
) {
    EMOJI(
        displayName = "Emoji图标",
        description = "使用传统的emoji表情符号作为分类图标"
    ),
    MATERIAL(
        displayName = "Material图标",
        description = "使用现代的Material Design矢量图标"
    );
    
    companion object {
        /**
         * 安全的值转换，如果无效则返回默认值
         */
        fun safeValueOf(name: String): IconDisplayMode {
            return try {
                valueOf(name)
            } catch (e: IllegalArgumentException) {
                EMOJI // 默认使用emoji图标
            }
        }
        
        /**
         * 获取所有可用的图标模式
         */
        fun getAllModes(): List<IconDisplayMode> {
            return values().toList()
        }
    }
}
