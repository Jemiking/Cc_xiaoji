package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.CcXiaoJiTheme
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoStyle
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoDensity
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.getStyleColors
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.getStyleShapes

/**
 * Demo主题提供器：仅处理主题色、圆角和密度，不负责系统状态栏颜色。
 */
@Composable
fun DemoThemeProvider(
    style: DemoStyle,
    density: DemoDensity,
    darkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val demoColors = getStyleColors(style, darkMode)
    val demoShapes = getStyleShapes(style)

    CompositionLocalProvider(
        LocalDemoStyle provides style,
        LocalDemoDensity provides density,
        LocalDemoColors provides demoColors,
        LocalDemoShapes provides demoShapes
    ) {
        CcXiaoJiTheme(
            darkTheme = darkMode,
            dynamicColor = false
        ) {
            val materialShapes = Shapes(
                extraSmall = RoundedCornerShape(demoShapes.cornerRadiusSmall),
                small = RoundedCornerShape(demoShapes.cornerRadiusSmall),
                medium = RoundedCornerShape(demoShapes.cornerRadiusMedium),
                large = RoundedCornerShape(demoShapes.cornerRadiusLarge),
                extraLarge = RoundedCornerShape(demoShapes.cornerRadiusExtraLarge)
            )

            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme,
                typography = MaterialTheme.typography,
                shapes = materialShapes
            ) { content() }
        }
    }
}

/** CompositionLocal: 当前 Demo 风格 **/
val LocalDemoStyle = compositionLocalOf { DemoStyle.MaterialYou }

/** CompositionLocal: 当前 Demo 密度 **/
val LocalDemoDensity = compositionLocalOf { DemoDensity.Medium }

/** CompositionLocal: 当前 Demo 颜色 **/
val LocalDemoColors = staticCompositionLocalOf { getStyleColors(DemoStyle.MaterialYou, false) }

/** CompositionLocal: 当前 Demo 圆角 **/
val LocalDemoShapes = staticCompositionLocalOf { getStyleShapes(DemoStyle.MaterialYou) }

