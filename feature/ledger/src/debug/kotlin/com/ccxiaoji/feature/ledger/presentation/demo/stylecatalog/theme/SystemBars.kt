package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

@Composable
fun SetStatusBar(color: Color, darkIcons: Boolean? = null) {
    val ctx = LocalContext.current
    val darkBackground = isDarkForStatusBar(color)
    val lightIcons = darkIcons ?: !darkBackground
    SideEffect {
        val window = (ctx as? Activity)?.window ?: return@SideEffect
        window.statusBarColor = color.toArgb()
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = lightIcons
        // 统一交由 Compose Insets 处理系统栏：内容总是绘制到系统栏后面
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}

fun isDarkForStatusBar(color: Color): Boolean {
    val argb = color.toArgb()
    val r = (argb shr 16 and 0xFF) / 255.0
    val g = (argb shr 8 and 0xFF) / 255.0
    val b = (argb and 0xFF) / 255.0
    fun channel(c: Double): Double = if (c <= 0.03928) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
    val rl = channel(r)
    val gl = channel(g)
    val bl = channel(b)
    val luminance = (0.2126 * rl + 0.7152 * gl + 0.0722 * bl)
    return luminance < 0.5
}

