package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val LocalDemoStyle = compositionLocalOf { DemoStyle.CardBased }
val LocalDemoDensity = compositionLocalOf { DemoDensity.Medium }
val LocalDemoColors = compositionLocalOf { getStyleColors(DemoStyle.CardBased, false) }
val LocalDemoShapes = compositionLocalOf { getStyleShapes(DemoStyle.CardBased) }
val LocalDemoElevations = compositionLocalOf { getStyleElevations(DemoStyle.CardBased) }
val LocalDemoDensitySettings = compositionLocalOf { getDensitySettings(DemoDensity.Medium) }

@Composable
fun DemoTheme(
    style: DemoStyle = DemoStyle.CardBased,
    density: DemoDensity = DemoDensity.Medium,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = remember(style, darkTheme) {
        getStyleColors(style, darkTheme)
    }
    
    val shapes = remember(style) {
        getStyleShapes(style)
    }
    
    val elevations = remember(style) {
        getStyleElevations(style)
    }
    
    val densitySettings = remember(density) {
        getDensitySettings(density)
    }
    
    // 转换为Material3颜色方案
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            secondaryContainer = colors.secondaryContainer,
            onSecondaryContainer = colors.onSecondaryContainer,
            background = colors.background,
            onBackground = colors.onBackground,
            surface = colors.surface,
            onSurface = colors.onSurface,
            surfaceVariant = colors.surfaceVariant,
            onSurfaceVariant = colors.onSurfaceVariant,
            outline = colors.outline,
            outlineVariant = colors.outlineVariant,
            error = colors.error,
            onError = colors.onError,
            errorContainer = colors.errorContainer,
            onErrorContainer = colors.onErrorContainer
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            secondaryContainer = colors.secondaryContainer,
            onSecondaryContainer = colors.onSecondaryContainer,
            background = colors.background,
            onBackground = colors.onBackground,
            surface = colors.surface,
            onSurface = colors.onSurface,
            surfaceVariant = colors.surfaceVariant,
            onSurfaceVariant = colors.onSurfaceVariant,
            outline = colors.outline,
            outlineVariant = colors.outlineVariant,
            error = colors.error,
            onError = colors.onError,
            errorContainer = colors.errorContainer,
            onErrorContainer = colors.onErrorContainer
        )
    }
    
    // 创建自定义字体样式
    val typography = createDemoTypography(densitySettings)
    
    CompositionLocalProvider(
        LocalDemoStyle provides style,
        LocalDemoDensity provides density,
        LocalDemoColors provides colors,
        LocalDemoShapes provides shapes,
        LocalDemoElevations provides elevations,
        LocalDemoDensitySettings provides densitySettings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

@Composable
private fun createDemoTypography(densitySettings: DensitySettings): Typography {
    val baseSize = densitySettings.fontSize.value
    
    return Typography(
        displayLarge = TextStyle(
            fontSize = (baseSize * 2.25f).sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (baseSize * 2.5f).sp
        ),
        displayMedium = TextStyle(
            fontSize = (baseSize * 1.875f).sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (baseSize * 2.125f).sp
        ),
        displaySmall = TextStyle(
            fontSize = (baseSize * 1.5f).sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (baseSize * 1.75f).sp
        ),
        headlineLarge = TextStyle(
            fontSize = (baseSize * 1.375f).sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (baseSize * 1.625f).sp
        ),
        headlineMedium = TextStyle(
            fontSize = (baseSize * 1.25f).sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (baseSize * 1.5f).sp
        ),
        headlineSmall = TextStyle(
            fontSize = (baseSize * 1.125f).sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (baseSize * 1.375f).sp
        ),
        titleLarge = TextStyle(
            fontSize = (baseSize * 1.125f).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (baseSize * 1.375f).sp
        ),
        titleMedium = TextStyle(
            fontSize = baseSize.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (baseSize * 1.25f).sp
        ),
        titleSmall = TextStyle(
            fontSize = (baseSize * 0.875f).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (baseSize * 1.125f).sp
        ),
        bodyLarge = TextStyle(
            fontSize = baseSize.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (baseSize * 1.5f).sp
        ),
        bodyMedium = TextStyle(
            fontSize = (baseSize * 0.875f).sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (baseSize * 1.25f).sp
        ),
        bodySmall = TextStyle(
            fontSize = (baseSize * 0.75f).sp,
            fontWeight = FontWeight.Normal,
            lineHeight = baseSize.sp
        ),
        labelLarge = TextStyle(
            fontSize = (baseSize * 0.875f).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (baseSize * 1.25f).sp
        ),
        labelMedium = TextStyle(
            fontSize = (baseSize * 0.75f).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = baseSize.sp
        ),
        labelSmall = TextStyle(
            fontSize = (baseSize * 0.6875f).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = baseSize.sp
        )
    )
}