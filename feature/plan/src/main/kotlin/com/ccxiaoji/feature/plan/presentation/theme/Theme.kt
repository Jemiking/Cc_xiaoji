package com.ccxiaoji.feature.plan.presentation.theme

import android.app.Activity
import com.ccxiaoji.feature.plan.domain.model.ThemeMode
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorDark,
    onError = Color.White,
    outline = DividerDark
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorLight,
    onError = Color.White,
    outline = DividerLight
)

// 扩展颜色
data class ExtendedColors(
    val success: Color,
    val warning: Color,
    val info: Color,
    val notStarted: Color,
    val inProgress: Color,
    val completed: Color,
    val cancelled: Color,
    val overdue: Color,
    val divider: Color,
    val shadow: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = SuccessLight,
        warning = WarningLight,
        info = InfoLight,
        notStarted = NotStartedLight,
        inProgress = InProgressLight,
        completed = CompletedLight,
        cancelled = CancelledLight,
        overdue = OverdueLight,
        divider = DividerLight,
        shadow = ShadowLight
    )
}

private val ExtendedDarkColors = ExtendedColors(
    success = SuccessDark,
    warning = WarningDark,
    info = InfoDark,
    notStarted = NotStartedDark,
    inProgress = InProgressDark,
    completed = CompletedDark,
    cancelled = CancelledDark,
    overdue = OverdueDark,
    divider = DividerDark,
    shadow = ShadowDark
)

private val ExtendedLightColors = ExtendedColors(
    success = SuccessLight,
    warning = WarningLight,
    info = InfoLight,
    notStarted = NotStartedLight,
    inProgress = InProgressLight,
    completed = CompletedLight,
    cancelled = CancelledLight,
    overdue = OverdueLight,
    divider = DividerLight,
    shadow = ShadowLight
)

@Composable
fun PlanTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val extendedColors = if (darkTheme) ExtendedDarkColors else ExtendedLightColors
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// 扩展属性，方便使用扩展颜色
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current