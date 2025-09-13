package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class DemoStyle(
    val styleName: String,
    val displayName: String,
    val description: String,
    val baseStyle: BaseStyle
) {
    ModernMinimal(
        styleName = "A",
        displayName = "A 现代极简",
        description = "克制留白",
        baseStyle = BaseStyle.Balanced
    ),
    CardBased(
        styleName = "B", 
        displayName = "B 卡片化",
        description = "柔和阴影",
        baseStyle = BaseStyle.Balanced
    ),
    HighContrast(
        styleName = "C",
        displayName = "C 高对比",
        description = "信息密集",
        baseStyle = BaseStyle.Hierarchical
    ),
    Glassmorphism(
        styleName = "D",
        displayName = "D 玻璃拟态",
        description = "半透明渐变",
        baseStyle = BaseStyle.Balanced
    ),
    Macaron(
        styleName = "E",
        displayName = "E 马卡龙",
        description = "浅色圆润",
        baseStyle = BaseStyle.Balanced
    ),
    BookTexture(
        styleName = "F",
        displayName = "F 账本质感",
        description = "类纸张",
        baseStyle = BaseStyle.Balanced
    ),
    IOS18(
        styleName = "G",
        displayName = "G iOS 18",
        description = "大标题通透",
        baseStyle = BaseStyle.Balanced
    ),
    MaterialYou(
        styleName = "H",
        displayName = "H Material You",
        description = "Tonal胶囊",
        baseStyle = BaseStyle.Balanced
    ),
    Discord(
        styleName = "I",
        displayName = "I Discord",
        description = "深色层次",
        baseStyle = BaseStyle.Hierarchical
    ),
    NotionMinimal(
        styleName = "J",
        displayName = "J Notion极简",
        description = "黑白灰细分隔",
        baseStyle = BaseStyle.Hierarchical
    ),
    NeoBrutalism(
        styleName = "K",
        displayName = "K Neo-Brutalism",
        description = "高对比边框",
        baseStyle = BaseStyle.Hierarchical
    ),
    QianjiInspired(
        styleName = "L",
        displayName = "L 钱迹风格",
        description = "蓝色顶部红点标记",
        baseStyle = BaseStyle.Balanced
    )
}

enum class BaseStyle {
    Balanced,    // 平衡布局
    Hierarchical  // 层级布局
}

data class StyleColors(
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onPrimary: Color,
    val onPrimaryContainer: Color,
    val onSecondary: Color,
    val onSecondaryContainer: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val error: Color,
    val errorContainer: Color,
    val onError: Color,
    val onErrorContainer: Color
)

data class StyleShapes(
    val cornerRadiusSmall: Dp = 4.dp,
    val cornerRadiusMedium: Dp = 8.dp,
    val cornerRadiusLarge: Dp = 12.dp,
    val cornerRadiusExtraLarge: Dp = 16.dp
)

data class StyleElevations(
    val level0: Dp = 0.dp,
    val level1: Dp = 1.dp,
    val level2: Dp = 3.dp,
    val level3: Dp = 6.dp,
    val level4: Dp = 8.dp,
    val level5: Dp = 12.dp
)

fun getStyleColors(style: DemoStyle, isDarkMode: Boolean): StyleColors {
    return when (style) {
        DemoStyle.ModernMinimal -> if (isDarkMode) {
            StyleColors(
                primary = Color(0xFF4CAF50),
                primaryContainer = Color(0xFF1B5E20),
                secondary = Color(0xFF607D8B),
                secondaryContainer = Color(0xFF263238),
                background = Color(0xFF121212),
                surface = Color(0xFF1E1E1E),
                surfaceVariant = Color(0xFF2C2C2C),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFFC8E6C9),
                onSecondary = Color.White,
                onSecondaryContainer = Color(0xFFCFD8DC),
                onBackground = Color(0xFFE0E0E0),
                onSurface = Color(0xFFE0E0E0),
                onSurfaceVariant = Color(0xFFBDBDBD),
                outline = Color(0xFF424242),
                outlineVariant = Color(0xFF303030),
                error = Color(0xFFCF6679),
                errorContainer = Color(0xFF93000A),
                onError = Color.Black,
                onErrorContainer = Color(0xFFFFDAD6)
            )
        } else {
            StyleColors(
                primary = Color(0xFF4CAF50),
                primaryContainer = Color(0xFFC8E6C9),
                secondary = Color(0xFF607D8B),
                secondaryContainer = Color(0xFFCFD8DC),
                background = Color(0xFFFAFAFA),
                surface = Color.White,
                surfaceVariant = Color(0xFFF5F5F5),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFF1B5E20),
                onSecondary = Color.White,
                onSecondaryContainer = Color(0xFF263238),
                onBackground = Color(0xFF1C1C1C),
                onSurface = Color(0xFF1C1C1C),
                onSurfaceVariant = Color(0xFF424242),
                outline = Color(0xFFE0E0E0),
                outlineVariant = Color(0xFFF0F0F0),
                error = Color(0xFFBA1A1A),
                errorContainer = Color(0xFFFFDAD6),
                onError = Color.White,
                onErrorContainer = Color(0xFF410002)
            )
        }
        
        DemoStyle.CardBased -> if (isDarkMode) {
            StyleColors(
                primary = Color(0xFF6750A4),
                primaryContainer = Color(0xFF4F378B),
                secondary = Color(0xFF625B71),
                secondaryContainer = Color(0xFF4A4458),
                background = Color(0xFF1C1B1F),
                surface = Color(0xFF2B2930),
                surfaceVariant = Color(0xFF49454F),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFFEADDFF),
                onSecondary = Color.White,
                onSecondaryContainer = Color(0xFFE8DEF8),
                onBackground = Color(0xFFE6E1E5),
                onSurface = Color(0xFFE6E1E5),
                onSurfaceVariant = Color(0xFFCAC4D0),
                outline = Color(0xFF938F99),
                outlineVariant = Color(0xFF49454F),
                error = Color(0xFFF2B8B5),
                errorContainer = Color(0xFF8C1D18),
                onError = Color(0xFF601410),
                onErrorContainer = Color(0xFFF9DEDC)
            )
        } else {
            StyleColors(
                primary = Color(0xFF6750A4),
                primaryContainer = Color(0xFFEADDFF),
                secondary = Color(0xFF625B71),
                secondaryContainer = Color(0xFFE8DEF8),
                background = Color(0xFFFEF7FF),
                surface = Color.White,
                surfaceVariant = Color(0xFFE7E0EC),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFF21005D),
                onSecondary = Color.White,
                onSecondaryContainer = Color(0xFF1D192B),
                onBackground = Color(0xFF1C1B1F),
                onSurface = Color(0xFF1C1B1F),
                onSurfaceVariant = Color(0xFF49454F),
                outline = Color(0xFF79747E),
                outlineVariant = Color(0xFFCAC4D0),
                error = Color(0xFFBA1A1A),
                errorContainer = Color(0xFFFFDAD6),
                onError = Color.White,
                onErrorContainer = Color(0xFF410002)
            )
        }
        
        DemoStyle.HighContrast -> if (isDarkMode) {
            StyleColors(
                primary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFE0E0E0),
                secondary = Color(0xFFBDBDBD),
                secondaryContainer = Color(0xFF757575),
                background = Color.Black,
                surface = Color(0xFF121212),
                surfaceVariant = Color(0xFF1E1E1E),
                onPrimary = Color.Black,
                onPrimaryContainer = Color.Black,
                onSecondary = Color.Black,
                onSecondaryContainer = Color.White,
                onBackground = Color.White,
                onSurface = Color.White,
                onSurfaceVariant = Color(0xFFE0E0E0),
                outline = Color.White,
                outlineVariant = Color(0xFF757575),
                error = Color(0xFFFF5252),
                errorContainer = Color(0xFFB71C1C),
                onError = Color.Black,
                onErrorContainer = Color.White
            )
        } else {
            StyleColors(
                primary = Color.Black,
                primaryContainer = Color(0xFF212121),
                secondary = Color(0xFF424242),
                secondaryContainer = Color(0xFF757575),
                background = Color.White,
                surface = Color(0xFFF5F5F5),
                surfaceVariant = Color(0xFFE0E0E0),
                onPrimary = Color.White,
                onPrimaryContainer = Color.White,
                onSecondary = Color.White,
                onSecondaryContainer = Color.Black,
                onBackground = Color.Black,
                onSurface = Color.Black,
                onSurfaceVariant = Color(0xFF212121),
                outline = Color.Black,
                outlineVariant = Color(0xFF9E9E9E),
                error = Color(0xFFD32F2F),
                errorContainer = Color(0xFFFFCDD2),
                onError = Color.White,
                onErrorContainer = Color.Black
            )
        }
        
        DemoStyle.Glassmorphism -> if (isDarkMode) {
            StyleColors(
                primary = Color(0xFF64B5F6).copy(alpha = 0.9f),
                primaryContainer = Color(0xFF1976D2).copy(alpha = 0.7f),
                secondary = Color(0xFF81C784).copy(alpha = 0.9f),
                secondaryContainer = Color(0xFF388E3C).copy(alpha = 0.7f),
                background = Color(0xFF0D1117),
                surface = Color(0xFF1C2128).copy(alpha = 0.8f),
                surfaceVariant = Color(0xFF30363D).copy(alpha = 0.6f),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFFBBDEFB),
                onSecondary = Color.White,
                onSecondaryContainer = Color(0xFFC8E6C9),
                onBackground = Color(0xFFE0E0E0),
                onSurface = Color(0xFFE0E0E0),
                onSurfaceVariant = Color(0xFFBDBDBD),
                outline = Color(0xFF484F58).copy(alpha = 0.5f),
                outlineVariant = Color(0xFF30363D).copy(alpha = 0.3f),
                error = Color(0xFFEF5350),
                errorContainer = Color(0xFFC62828).copy(alpha = 0.7f),
                onError = Color.White,
                onErrorContainer = Color(0xFFFFCDD2)
            )
        } else {
            StyleColors(
                primary = Color(0xFF2196F3).copy(alpha = 0.9f),
                primaryContainer = Color(0xFFBBDEFB).copy(alpha = 0.7f),
                secondary = Color(0xFF4CAF50).copy(alpha = 0.9f),
                secondaryContainer = Color(0xFFC8E6C9).copy(alpha = 0.7f),
                background = Color(0xFFF5F5F5),
                surface = Color.White.copy(alpha = 0.8f),
                surfaceVariant = Color(0xFFF0F0F0).copy(alpha = 0.6f),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFF0D47A1),
                onSecondary = Color.White,
                onSecondaryContainer = Color(0xFF1B5E20),
                onBackground = Color(0xFF212121),
                onSurface = Color(0xFF212121),
                onSurfaceVariant = Color(0xFF424242),
                outline = Color(0xFFBDBDBD).copy(alpha = 0.5f),
                outlineVariant = Color(0xFFE0E0E0).copy(alpha = 0.3f),
                error = Color(0xFFE53935),
                errorContainer = Color(0xFFFFCDD2).copy(alpha = 0.7f),
                onError = Color.White,
                onErrorContainer = Color(0xFFB71C1C)
            )
        }
        
        DemoStyle.Macaron -> if (isDarkMode) {
            StyleColors(
                primary = Color(0xFFCE93D8),
                primaryContainer = Color(0xFF8E24AA),
                secondary = Color(0xFFF48FB1),
                secondaryContainer = Color(0xFFC2185B),
                background = Color(0xFF2E1A2E),
                surface = Color(0xFF3F2A3F),
                surfaceVariant = Color(0xFF4F3A4F),
                onPrimary = Color(0xFF4A148C),
                onPrimaryContainer = Color(0xFFF3E5F5),
                onSecondary = Color(0xFF880E4F),
                onSecondaryContainer = Color(0xFFFCE4EC),
                onBackground = Color(0xFFF3E5F5),
                onSurface = Color(0xFFF3E5F5),
                onSurfaceVariant = Color(0xFFE1BEE7),
                outline = Color(0xFF9C27B0),
                outlineVariant = Color(0xFF6A1B9A),
                error = Color(0xFFFF8A80),
                errorContainer = Color(0xFFD32F2F),
                onError = Color(0xFF5F0A0A),
                onErrorContainer = Color(0xFFFFCDD2)
            )
        } else {
            StyleColors(
                primary = Color(0xFFE91E63),
                primaryContainer = Color(0xFFFCE4EC),
                secondary = Color(0xFFFF4081),
                secondaryContainer = Color(0xFFF8BBD0),
                background = Color(0xFFFFF0F5),
                surface = Color(0xFFFFE4E1),
                surfaceVariant = Color(0xFFFAD4D0),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFF880E4F),
                onSecondary = Color.White,
                onSecondaryContainer = Color(0xFFC2185B),
                onBackground = Color(0xFF4A148C),
                onSurface = Color(0xFF4A148C),
                onSurfaceVariant = Color(0xFF6A1B9A),
                outline = Color(0xFFE91E63),
                outlineVariant = Color(0xFFF8BBD0),
                error = Color(0xFFD32F2F),
                errorContainer = Color(0xFFFFCDD2),
                onError = Color.White,
                onErrorContainer = Color(0xFF5F0A0A)
            )
        }
        
        DemoStyle.BookTexture -> if (isDarkMode) {
            StyleColors(
                primary = Color(0xFF8D6E63),
                primaryContainer = Color(0xFF5D4037),
                secondary = Color(0xFFBCAAA4),
                secondaryContainer = Color(0xFF6D4C41),
                background = Color(0xFF2E2621),
                surface = Color(0xFF3E342E),
                surfaceVariant = Color(0xFF4E423C),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFFD7CCC8),
                onSecondary = Color(0xFF3E2723),
                onSecondaryContainer = Color(0xFFEFEBE9),
                onBackground = Color(0xFFEFEBE9),
                onSurface = Color(0xFFEFEBE9),
                onSurfaceVariant = Color(0xFFD7CCC8),
                outline = Color(0xFF795548),
                outlineVariant = Color(0xFF5D4037),
                error = Color(0xFFEF5350),
                errorContainer = Color(0xFFC62828),
                onError = Color.White,
                onErrorContainer = Color(0xFFFFCDD2)
            )
        } else {
            StyleColors(
                primary = Color(0xFF795548),
                primaryContainer = Color(0xFFD7CCC8),
                secondary = Color(0xFF8D6E63),
                secondaryContainer = Color(0xFFEFEBE9),
                background = Color(0xFFFFFBF5),
                surface = Color(0xFFFFF8E1),
                surfaceVariant = Color(0xFFF5F0E8),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFF3E2723),
                onSecondary = Color.White,
                onSecondaryContainer = Color(0xFF4E342E),
                onBackground = Color(0xFF3E2723),
                onSurface = Color(0xFF3E2723),
                onSurfaceVariant = Color(0xFF5D4037),
                outline = Color(0xFFBCAAA4),
                outlineVariant = Color(0xFFD7CCC8),
                error = Color(0xFFD32F2F),
                errorContainer = Color(0xFFFFCDD2),
                onError = Color.White,
                onErrorContainer = Color(0xFF5F0A0A)
            )
        }
        
        DemoStyle.IOS18 -> if (isDarkMode) {
            StyleColors(
                primary = Color(0xFF007AFF),
                primaryContainer = Color(0xFF0051D5),
                secondary = Color(0xFF5AC8FA),
                secondaryContainer = Color(0xFF0A84FF),
                background = Color.Black,
                surface = Color(0xFF1C1C1E),
                surfaceVariant = Color(0xFF2C2C2E),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFFB3D7FF),
                onSecondary = Color.Black,
                onSecondaryContainer = Color(0xFFD1EEFF),
                onBackground = Color.White,
                onSurface = Color.White,
                onSurfaceVariant = Color(0xFFAEAEB2),
                outline = Color(0xFF3A3A3C),
                outlineVariant = Color(0xFF48484A),
                error = Color(0xFFFF453A),
                errorContainer = Color(0xFFFF6961),
                onError = Color.White,
                onErrorContainer = Color(0xFFFFD4D2)
            )
        } else {
            StyleColors(
                primary = Color(0xFF007AFF),
                primaryContainer = Color(0xFFD1E4FF),
                secondary = Color(0xFF5AC8FA),
                secondaryContainer = Color(0xFFE5F4FF),
                background = Color(0xFFF2F2F7),
                surface = Color.White,
                surfaceVariant = Color(0xFFF2F2F7),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFF001D36),
                onSecondary = Color.White,
                onSecondaryContainer = Color(0xFF001F2D),
                onBackground = Color.Black,
                onSurface = Color.Black,
                onSurfaceVariant = Color(0xFF3C3C43).copy(alpha = 0.6f),
                outline = Color(0xFFC6C6C8),
                outlineVariant = Color(0xFFE5E5EA),
                error = Color(0xFFFF3B30),
                errorContainer = Color(0xFFFFD4D2),
                onError = Color.White,
                onErrorContainer = Color(0xFF8B0000)
            )
        }
        
        DemoStyle.MaterialYou -> if (isDarkMode) {
            StyleColors(
                primary = Color(0xFFD0BCFF),
                primaryContainer = Color(0xFF4F378B),
                secondary = Color(0xFFCCC2DC),
                secondaryContainer = Color(0xFF4A4458),
                background = Color(0xFF1C1B1F),
                surface = Color(0xFF2B2930),
                surfaceVariant = Color(0xFF49454F),
                onPrimary = Color(0xFF381E72),
                onPrimaryContainer = Color(0xFFEADDFF),
                onSecondary = Color(0xFF332D41),
                onSecondaryContainer = Color(0xFFE8DEF8),
                onBackground = Color(0xFFE6E1E5),
                onSurface = Color(0xFFE6E1E5),
                onSurfaceVariant = Color(0xFFCAC4D0),
                outline = Color(0xFF938F99),
                outlineVariant = Color(0xFF49454F),
                error = Color(0xFFF2B8B5),
                errorContainer = Color(0xFF8C1D18),
                onError = Color(0xFF601410),
                onErrorContainer = Color(0xFFF9DEDC)
            )
        } else {
            StyleColors(
                primary = Color(0xFF6750A4),
                primaryContainer = Color(0xFFEADDFF),
                secondary = Color(0xFF625B71),
                secondaryContainer = Color(0xFFE8DEF8),
                background = Color(0xFFFEF7FF),
                surface = Color(0xFFFEF7FF),
                surfaceVariant = Color(0xFFE7E0EC),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFF21005D),
                onSecondary = Color.White,
                onSecondaryContainer = Color(0xFF1D192B),
                onBackground = Color(0xFF1C1B1F),
                onSurface = Color(0xFF1C1B1F),
                onSurfaceVariant = Color(0xFF49454F),
                outline = Color(0xFF79747E),
                outlineVariant = Color(0xFFCAC4D0),
                error = Color(0xFFBA1A1A),
                errorContainer = Color(0xFFFFDAD6),
                onError = Color.White,
                onErrorContainer = Color(0xFF410002)
            )
        }
        
        DemoStyle.Discord -> if (isDarkMode) {
            StyleColors(
                primary = Color(0xFF5865F2),
                primaryContainer = Color(0xFF4752C4),
                secondary = Color(0xFF57F287),
                secondaryContainer = Color(0xFF3BA55D),
                background = Color(0xFF2F3136),
                surface = Color(0xFF36393F),
                surfaceVariant = Color(0xFF40444B),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFFDEE0FF),
                onSecondary = Color.Black,
                onSecondaryContainer = Color(0xFFE3FFED),
                onBackground = Color(0xFFDCDDDE),
                onSurface = Color(0xFFDCDDDE),
                onSurfaceVariant = Color(0xFF96989D),
                outline = Color(0xFF202225),
                outlineVariant = Color(0xFF292B2F),
                error = Color(0xFFED4245),
                errorContainer = Color(0xFFA12D2D),
                onError = Color.White,
                onErrorContainer = Color(0xFFFFD7D7)
            )
        } else {
            StyleColors(
                primary = Color(0xFF5865F2),
                primaryContainer = Color(0xFFDEE0FF),
                secondary = Color(0xFF3BA55D),
                secondaryContainer = Color(0xFFE3FFED),
                background = Color.White,
                surface = Color(0xFFF2F3F5),
                surfaceVariant = Color(0xFFE3E5E8),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFF1A1F63),
                onSecondary = Color.White,
                onSecondaryContainer = Color(0xFF0C2818),
                onBackground = Color(0xFF2E3338),
                onSurface = Color(0xFF2E3338),
                onSurfaceVariant = Color(0xFF4F5660),
                outline = Color(0xFFDCDDDE),
                outlineVariant = Color(0xFFE3E5E8),
                error = Color(0xFFED4245),
                errorContainer = Color(0xFFFFD7D7),
                onError = Color.White,
                onErrorContainer = Color(0xFF5F0A0A)
            )
        }
        
        DemoStyle.NotionMinimal -> if (isDarkMode) {
            StyleColors(
                primary = Color(0xFFE0E0E0),
                primaryContainer = Color(0xFF757575),
                secondary = Color(0xFF9E9E9E),
                secondaryContainer = Color(0xFF616161),
                background = Color(0xFF191919),
                surface = Color(0xFF252525),
                surfaceVariant = Color(0xFF2F2F2F),
                onPrimary = Color.Black,
                onPrimaryContainer = Color.White,
                onSecondary = Color.Black,
                onSecondaryContainer = Color(0xFFE0E0E0),
                onBackground = Color(0xFFE0E0E0),
                onSurface = Color(0xFFE0E0E0),
                onSurfaceVariant = Color(0xFF9E9E9E),
                outline = Color(0xFF424242),
                outlineVariant = Color(0xFF303030),
                error = Color(0xFFEB5757),
                errorContainer = Color(0xFF93000A),
                onError = Color.White,
                onErrorContainer = Color(0xFFFFDAD6)
            )
        } else {
            StyleColors(
                primary = Color(0xFF37352F),
                primaryContainer = Color(0xFFE0E0E0),
                secondary = Color(0xFF787774),
                secondaryContainer = Color(0xFFF7F6F3),
                background = Color.White,
                surface = Color.White,
                surfaceVariant = Color(0xFFF7F6F3),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFF37352F),
                onSecondary = Color.White,
                onSecondaryContainer = Color(0xFF37352F),
                onBackground = Color(0xFF37352F),
                onSurface = Color(0xFF37352F),
                onSurfaceVariant = Color(0xFF787774),
                outline = Color(0xFFE0E0E0),
                outlineVariant = Color(0xFFF0F0F0),
                error = Color(0xFFEB5757),
                errorContainer = Color(0xFFFFDAD6),
                onError = Color.White,
                onErrorContainer = Color(0xFF5F0A0A)
            )
        }
        
        DemoStyle.NeoBrutalism -> if (isDarkMode) {
            StyleColors(
                primary = Color(0xFFFFFF00),
                primaryContainer = Color(0xFFCCCC00),
                secondary = Color(0xFF00FFFF),
                secondaryContainer = Color(0xFF00CCCC),
                background = Color.Black,
                surface = Color(0xFF1A1A1A),
                surfaceVariant = Color(0xFF2D2D2D),
                onPrimary = Color.Black,
                onPrimaryContainer = Color.Black,
                onSecondary = Color.Black,
                onSecondaryContainer = Color.Black,
                onBackground = Color.White,
                onSurface = Color.White,
                onSurfaceVariant = Color(0xFFCCCCCC),
                outline = Color.White,
                outlineVariant = Color(0xFF808080),
                error = Color(0xFFFF0000),
                errorContainer = Color(0xFFCC0000),
                onError = Color.Black,
                onErrorContainer = Color.White
            )
        } else {
            StyleColors(
                primary = Color.Black,
                primaryContainer = Color(0xFFFFFF00),
                secondary = Color(0xFFFF00FF),
                secondaryContainer = Color(0xFF00FFFF),
                background = Color.White,
                surface = Color(0xFFF0F0F0),
                surfaceVariant = Color(0xFFE0E0E0),
                onPrimary = Color.White,
                onPrimaryContainer = Color.Black,
                onSecondary = Color.White,
                onSecondaryContainer = Color.Black,
                onBackground = Color.Black,
                onSurface = Color.Black,
                onSurfaceVariant = Color(0xFF333333),
                outline = Color.Black,
                outlineVariant = Color(0xFF666666),
                error = Color(0xFFFF0000),
                errorContainer = Color(0xFFFFCCCC),
                onError = Color.White,
                onErrorContainer = Color.Black
            )
        }
        
        DemoStyle.QianjiInspired -> if (isDarkMode) {
            StyleColors(
                primary = Color(0xFF3B82F6),            // 钱迹蓝色
                primaryContainer = Color(0xFF1E3A8A),
                secondary = Color(0xFFEF4444),          // 钱迹红色
                secondaryContainer = Color(0xFFFECACA),
                background = Color(0xFF111827),         // 深色背景
                surface = Color(0xFF1F2937),            // 深色表面
                surfaceVariant = Color(0xFF374151),
                onPrimary = Color.White,
                onPrimaryContainer = Color(0xFFDBEAFE),
                onSecondary = Color.White,
                onSecondaryContainer = Color(0xFF7F1D1D),
                onBackground = Color(0xFFF3F4F6),
                onSurface = Color(0xFFF3F4F6),
                onSurfaceVariant = Color(0xFF9CA3AF),
                outline = Color(0xFF6B7280),
                outlineVariant = Color(0xFF374151),
                error = Color(0xFFEF4444),
                errorContainer = Color(0xFF991B1B),
                onError = Color.White,
                onErrorContainer = Color(0xFFFECACA)
            )
        } else {
            StyleColors(
                primary = Color(0xFF3B82F6),            // bg-blue-500 钱迹标准蓝
                primaryContainer = Color(0xFFDBEAFE),   // bg-blue-100
                secondary = Color(0xFFEF4444),          // bg-red-500 钱迹标准红
                secondaryContainer = Color(0xFFFECACA), // bg-red-100
                background = Color(0xFFF3F4F6),         // bg-gray-100 页面背景
                surface = Color(0xFFFFFFFF),            // bg-white 卡片背景
                surfaceVariant = Color(0xFFF9FAFB),     // bg-gray-50
                onPrimary = Color.White,                // text-white
                onPrimaryContainer = Color(0xFF1E3A8A), // text-blue-800
                onSecondary = Color.White,              // text-white
                onSecondaryContainer = Color(0xFF7F1D1D), // text-red-800
                onBackground = Color(0xFF111827),       // text-gray-900 主文字
                onSurface = Color(0xFF111827),          // text-gray-900 主文字
                onSurfaceVariant = Color(0xFF6B7280),   // text-gray-500 次要文字
                outline = Color(0xFFE5E7EB),            // border-gray-200
                outlineVariant = Color(0xFFF3F4F6),     // border-gray-100
                error = Color(0xFFEF4444),              // text-red-500
                errorContainer = Color(0xFFFEF2F2),     // bg-red-50
                onError = Color.White,
                onErrorContainer = Color(0xFF991B1B)    // text-red-800
            )
        }
    }
}

fun getStyleShapes(style: DemoStyle): StyleShapes {
    return when (style) {
        DemoStyle.ModernMinimal -> StyleShapes(
            cornerRadiusSmall = 2.dp,
            cornerRadiusMedium = 4.dp,
            cornerRadiusLarge = 8.dp,
            cornerRadiusExtraLarge = 12.dp
        )
        DemoStyle.CardBased -> StyleShapes(
            cornerRadiusSmall = 8.dp,
            cornerRadiusMedium = 12.dp,
            cornerRadiusLarge = 16.dp,
            cornerRadiusExtraLarge = 24.dp
        )
        DemoStyle.HighContrast -> StyleShapes(
            cornerRadiusSmall = 0.dp,
            cornerRadiusMedium = 0.dp,
            cornerRadiusLarge = 0.dp,
            cornerRadiusExtraLarge = 0.dp
        )
        DemoStyle.Glassmorphism -> StyleShapes(
            cornerRadiusSmall = 12.dp,
            cornerRadiusMedium = 16.dp,
            cornerRadiusLarge = 20.dp,
            cornerRadiusExtraLarge = 28.dp
        )
        DemoStyle.Macaron -> StyleShapes(
            cornerRadiusSmall = 16.dp,
            cornerRadiusMedium = 20.dp,
            cornerRadiusLarge = 24.dp,
            cornerRadiusExtraLarge = 32.dp
        )
        DemoStyle.BookTexture -> StyleShapes(
            cornerRadiusSmall = 2.dp,
            cornerRadiusMedium = 4.dp,
            cornerRadiusLarge = 6.dp,
            cornerRadiusExtraLarge = 8.dp
        )
        DemoStyle.IOS18 -> StyleShapes(
            cornerRadiusSmall = 10.dp,
            cornerRadiusMedium = 14.dp,
            cornerRadiusLarge = 20.dp,
            cornerRadiusExtraLarge = 30.dp
        )
        DemoStyle.MaterialYou -> StyleShapes(
            cornerRadiusSmall = 12.dp,
            cornerRadiusMedium = 16.dp,
            cornerRadiusLarge = 28.dp,
            cornerRadiusExtraLarge = 32.dp
        )
        DemoStyle.Discord -> StyleShapes(
            cornerRadiusSmall = 4.dp,
            cornerRadiusMedium = 8.dp,
            cornerRadiusLarge = 8.dp,
            cornerRadiusExtraLarge = 12.dp
        )
        DemoStyle.NotionMinimal -> StyleShapes(
            cornerRadiusSmall = 3.dp,
            cornerRadiusMedium = 3.dp,
            cornerRadiusLarge = 6.dp,
            cornerRadiusExtraLarge = 6.dp
        )
        DemoStyle.NeoBrutalism -> StyleShapes(
            cornerRadiusSmall = 0.dp,
            cornerRadiusMedium = 0.dp,
            cornerRadiusLarge = 0.dp,
            cornerRadiusExtraLarge = 0.dp
        )
        DemoStyle.QianjiInspired -> StyleShapes(
            cornerRadiusSmall = 4.dp,               // rounded
            cornerRadiusMedium = 8.dp,              // rounded-lg Tailwind标准
            cornerRadiusLarge = 12.dp,              // rounded-xl
            cornerRadiusExtraLarge = 16.dp          // rounded-2xl
        )
    }
}

fun getStyleElevations(style: DemoStyle): StyleElevations {
    return when (style) {
        DemoStyle.ModernMinimal -> StyleElevations(
            level0 = 0.dp,
            level1 = 0.dp,
            level2 = 0.dp,
            level3 = 1.dp,
            level4 = 2.dp,
            level5 = 4.dp
        )
        DemoStyle.CardBased -> StyleElevations(
            level0 = 0.dp,
            level1 = 2.dp,
            level2 = 4.dp,
            level3 = 8.dp,
            level4 = 12.dp,
            level5 = 16.dp
        )
        DemoStyle.HighContrast -> StyleElevations(
            level0 = 0.dp,
            level1 = 0.dp,
            level2 = 0.dp,
            level3 = 0.dp,
            level4 = 0.dp,
            level5 = 0.dp
        )
        DemoStyle.Glassmorphism -> StyleElevations(
            level0 = 0.dp,
            level1 = 8.dp,
            level2 = 12.dp,
            level3 = 16.dp,
            level4 = 20.dp,
            level5 = 24.dp
        )
        DemoStyle.NeoBrutalism -> StyleElevations(
            level0 = 0.dp,
            level1 = 4.dp,
            level2 = 6.dp,
            level3 = 8.dp,
            level4 = 10.dp,
            level5 = 12.dp
        )
        DemoStyle.QianjiInspired -> StyleElevations(
            level0 = 0.dp,              // 无阴影
            level1 = 0.dp,              // 几乎无阴影（钱迹风格主要用颜色区分）
            level2 = 1.dp,              // 非常轻微
            level3 = 2.dp,              // 轻微阴影
            level4 = 4.dp,              // 中等阴影
            level5 = 6.dp               // 最大阴影
        )
        else -> StyleElevations() // 其他风格使用默认值
    }
}