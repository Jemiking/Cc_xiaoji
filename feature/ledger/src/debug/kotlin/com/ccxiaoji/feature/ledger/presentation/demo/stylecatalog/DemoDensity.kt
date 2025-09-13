package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DemoDensity(
    val displayName: String,
    val rowHeight: Dp,
    val cellPadding: Dp,
    val cardPadding: Dp,
    val itemSpacing: Dp,
    val fontSize: TextUnit,
    val iconSize: Dp,
    val minTouchTarget: Dp
) {
    Compact(
        displayName = "紧凑",
        rowHeight = 36.dp,
        cellPadding = 8.dp,
        cardPadding = 12.dp,
        itemSpacing = 4.dp,
        fontSize = 14.sp,
        iconSize = 20.dp,
        minTouchTarget = 36.dp
    ),
    Medium(
        displayName = "中等",
        rowHeight = 44.dp,
        cellPadding = 12.dp,
        cardPadding = 16.dp,
        itemSpacing = 8.dp,
        fontSize = 16.sp,
        iconSize = 24.dp,
        minTouchTarget = 48.dp
    )
}

data class DensitySettings(
    val rowHeight: Dp,
    val cellPadding: Dp,
    val cardPadding: Dp,
    val itemSpacing: Dp,
    val fontSize: TextUnit,
    val iconSize: Dp,
    val minTouchTarget: Dp,
    val headerHeight: Dp,
    val buttonHeight: Dp,
    val chipHeight: Dp,
    val dividerThickness: Dp
)

fun getDensitySettings(density: DemoDensity): DensitySettings {
    return when (density) {
        DemoDensity.Compact -> DensitySettings(
            rowHeight = 36.dp,
            cellPadding = 8.dp,
            cardPadding = 12.dp,
            itemSpacing = 4.dp,
            fontSize = 14.sp,
            iconSize = 20.dp,
            minTouchTarget = 36.dp,
            headerHeight = 32.dp,
            buttonHeight = 32.dp,
            chipHeight = 24.dp,
            dividerThickness = 0.5.dp
        )
        DemoDensity.Medium -> DensitySettings(
            rowHeight = 44.dp,
            cellPadding = 12.dp,
            cardPadding = 16.dp,
            itemSpacing = 8.dp,
            fontSize = 16.sp,
            iconSize = 24.dp,
            minTouchTarget = 48.dp,
            headerHeight = 40.dp,
            buttonHeight = 40.dp,
            chipHeight = 32.dp,
            dividerThickness = 1.dp
        )
    }
}