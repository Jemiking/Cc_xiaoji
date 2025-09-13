package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec

import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoStyle
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoDensity
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl.*

/**
 * 风格规格注册中心 - 将每个风格映射到具体的布局实现
 * 这是实现真正布局差异的核心
 */
object SpecsRegistry {
    
    /**
     * 根据风格返回对应的布局规格集合
     */
    fun getSpecs(style: DemoStyle): StyleSpecs {
        return when (style) {
            DemoStyle.ModernMinimal -> ModernMinimalSpecs()          // A 现代极简
            DemoStyle.CardBased -> CardBasedSpecs()                  // B 卡片化
            DemoStyle.HighContrast -> HighContrastSpecs()            // C 高对比
            DemoStyle.Glassmorphism -> GlassmorphismBeautifulSpecs() // D 玻璃拟态 (美化版)
            DemoStyle.Macaron -> MacaronSpecs()                      // E 马卡龙
            DemoStyle.BookTexture -> BookTextureSpecs()              // F 账本质感
            DemoStyle.IOS18 -> IOS18Specs()                          // G iOS 18
            DemoStyle.MaterialYou -> MaterialYouSpecs()              // H Material You
            DemoStyle.Discord -> DiscordSpecs()                      // I Discord
            DemoStyle.NotionMinimal -> NotionMinimalSpecs()          // J Notion极简
            DemoStyle.NeoBrutalism -> NeoBrutalismSpecs()            // K Neo-Brutalism
            DemoStyle.QianjiInspired -> QianjiInspiredSpecs()        // L 钱迹风格
        }
    }
    
    /**
     * 风格规格集合 - 包含一个风格的所有布局规格
     */
    abstract class StyleSpecs {
        abstract val listSpec: ListSpec
        abstract val itemSpec: ItemSpec
        abstract val headerSpec: HeaderSpec
        abstract val filterSpec: FilterSpec
        abstract val formSpec: FormSpec
        abstract val dialogSpec: DialogSpec
        abstract val chartsSpec: ChartsSpec
        abstract val settingsSpec: SettingsSpec
        
        // 基底风格类型
        enum class BaseStyle {
            BALANCED,       // 平衡式布局（A/B/D/E/F/G/H）
            HIERARCHICAL    // 层次式布局（C/I/J/K）
        }
        
        abstract val baseStyle: BaseStyle
        
        // 风格特征描述
        abstract val description: String
        
        // 推荐的密度设置
        abstract val recommendedDensity: DemoDensity
    }
}