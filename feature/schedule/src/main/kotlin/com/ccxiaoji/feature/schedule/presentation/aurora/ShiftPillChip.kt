package com.ccxiaoji.feature.schedule.presentation.aurora

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 班次胶囊组件 - Aurora Calm 设计
 *
 * 提供三种变体：
 * - Mini: 14dp高，用于日历单元格
 * - Standard: 28dp高，用于列表和选择器
 * - Card: 72dp高，用于班次管理页面
 */

/**
 * 班次类型枚举
 */
enum class ShiftType(
    val code: Int,
    val displayName: String,
    val shortName: String,
    val icon: ImageVector
) {
    REST(0, "休息", "休", Icons.Outlined.Park),
    MORNING(1, "早班", "早", Icons.Outlined.WbSunny),
    AFTERNOON(2, "中班", "中", Icons.Outlined.LightMode),
    NIGHT(3, "晚班", "晚", Icons.Outlined.Nightlight);

    companion object {
        fun fromCode(code: Int): ShiftType = entries.find { it.code == code } ?: REST
    }
}

/**
 * 迷你班次胶囊 - 用于日历单元格
 * 高度: 14dp, 图标: 10dp, 文字: 10sp
 */
@Composable
fun ShiftPillMini(
    shiftType: ShiftType,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    ShiftPillBase(
        shiftType = shiftType,
        height = 14.dp,
        iconSize = 10.dp,
        fontSize = 10.sp,
        horizontalPadding = 4.dp,
        iconTextSpacing = 2.dp,
        cornerRadius = 7.dp,
        showIcon = showIcon,
        useShortName = true,
        isDarkMode = isDarkMode,
        modifier = modifier
    )
}

/**
 * 标准班次胶囊 - 用于列表和选择器
 * 高度: 28dp, 图标: 16dp, 文字: 14sp
 */
@Composable
fun ShiftPillStandard(
    shiftType: ShiftType,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    ShiftPillBase(
        shiftType = shiftType,
        height = 28.dp,
        iconSize = 16.dp,
        fontSize = 14.sp,
        horizontalPadding = 10.dp,
        iconTextSpacing = 4.dp,
        cornerRadius = 14.dp,
        showIcon = showIcon,
        useShortName = false,
        isDarkMode = isDarkMode,
        modifier = modifier
    )
}

/**
 * 大号班次胶囊 - 用于底部Dock选择
 * 高度: 40dp, 图标: 20dp, 文字: 16sp
 */
@Composable
fun ShiftPillLarge(
    shiftType: ShiftType,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    ShiftPillBase(
        shiftType = shiftType,
        height = 40.dp,
        iconSize = 20.dp,
        fontSize = 16.sp,
        horizontalPadding = 14.dp,
        iconTextSpacing = 6.dp,
        cornerRadius = 20.dp,
        showIcon = showIcon,
        useShortName = false,
        isDarkMode = isDarkMode,
        modifier = modifier
    )
}

/**
 * 班次卡片 - 用于班次管理
 * 左侧带6dp色条的完整卡片
 */
@Composable
fun ShiftCard(
    shiftType: ShiftType,
    timeRange: String,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme(),
    onClick: (() -> Unit)? = null
) {
    val primaryColor = ScheduleAuroraColors.getShiftPrimaryColor(shiftType.code, isDarkMode)
    val cardColor = if (isDarkMode) {
        ScheduleAuroraColors.CardSurfaceDark
    } else {
        ScheduleAuroraColors.CardSurfaceLight
    }
    val textPrimary = if (isDarkMode) {
        ScheduleAuroraColors.TextPrimaryDark
    } else {
        ScheduleAuroraColors.TextPrimaryLight
    }
    val textSecondary = if (isDarkMode) {
        ScheduleAuroraColors.TextSecondaryDark
    } else {
        ScheduleAuroraColors.TextSecondaryLight
    }

    Row(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧色条
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                .background(primaryColor)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // 图标
        Icon(
            imageVector = shiftType.icon,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 文字信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = shiftType.displayName,
                color = textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = timeRange,
                color = textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
    }
}

/**
 * 基础胶囊组件 - 内部使用
 */
@Composable
private fun ShiftPillBase(
    shiftType: ShiftType,
    height: Dp,
    iconSize: Dp,
    fontSize: TextUnit,
    horizontalPadding: Dp,
    iconTextSpacing: Dp,
    cornerRadius: Dp,
    showIcon: Boolean,
    useShortName: Boolean,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = ScheduleAuroraColors.getShiftBackgroundColor(shiftType.code, isDarkMode)
    val contentColor = ScheduleAuroraColors.getShiftPrimaryColor(shiftType.code, isDarkMode)
    val displayText = if (useShortName) shiftType.shortName else shiftType.displayName

    Row(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (showIcon) {
            Icon(
                imageVector = shiftType.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(iconTextSpacing))
        }
        Text(
            text = displayText,
            color = contentColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            lineHeight = fontSize
        )
    }
}

// ============================================
// Previews
// ============================================

@Preview(showBackground = true, backgroundColor = 0xFFF6F7FB)
@Composable
private fun PreviewShiftPillMini() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        ShiftPillMini(ShiftType.MORNING)
        ShiftPillMini(ShiftType.AFTERNOON)
        ShiftPillMini(ShiftType.NIGHT)
        ShiftPillMini(ShiftType.REST)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F7FB)
@Composable
private fun PreviewShiftPillStandard() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        ShiftPillStandard(ShiftType.MORNING)
        ShiftPillStandard(ShiftType.NIGHT)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F7FB)
@Composable
private fun PreviewShiftCard() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        ShiftCard(
            shiftType = ShiftType.MORNING,
            timeRange = "09:00 - 18:00",
            modifier = Modifier.fillMaxWidth()
        )
        ShiftCard(
            shiftType = ShiftType.NIGHT,
            timeRange = "22:00 - 06:00",
            modifier = Modifier.fillMaxWidth()
        )
    }
}
