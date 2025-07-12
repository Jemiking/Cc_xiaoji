package com.ccxiaoji.app.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.ui.theme.DiscordColors

/**
 * Discord风格服务器图标
 */
@Composable
fun DiscordServerIcon(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val cornerRadius by animateFloatAsState(
        targetValue = if (isSelected) 12f else 16f,
        animationSpec = tween(300),
        label = "cornerRadius"
    )
    
    Box(
        modifier = modifier.width(48.dp),
        contentAlignment = Alignment.Center
    ) {
        // 左侧选中指示条
        AnimatedVisibility(
            visible = isSelected,
            enter = expandVertically(
                expandFrom = Alignment.CenterVertically,
                animationSpec = tween(200)
            ) + fadeIn(),
            exit = shrinkVertically(
                shrinkTowards = Alignment.CenterVertically,
                animationSpec = tween(200)
            ) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .offset(x = (-6).dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                    )
            )
        }
        
        // 图标按钮 - Discord风格的动态圆角
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(cornerRadius.dp))
                .background(backgroundColor)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Discord风格模块图标（旧版兼容）
 */
@Composable
fun DiscordModuleIcon(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    DiscordServerIcon(
        icon = icon,
        label = label,
        isSelected = isSelected,
        onClick = onClick,
        backgroundColor = backgroundColor,
        modifier = modifier
    )
}

/**
 * Discord风格卡片
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiscordCard(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val surfaceHover = if (isDarkTheme) DiscordColors.Dark.SurfaceHover else DiscordColors.Light.SurfaceHover
    val surfaceDefault = if (isDarkTheme) DiscordColors.Dark.SurfaceDefault else DiscordColors.Light.SurfaceDefault
    val divider = if (isDarkTheme) DiscordColors.Dark.Divider else DiscordColors.Light.Divider
    
    Surface(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onClick() }
                } else Modifier
            ),
        color = animateColorAsState(
            targetValue = if (isHovered) surfaceHover 
                          else surfaceDefault,
            animationSpec = tween(200),
            label = "cardColor"
        ).value,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = divider.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * Discord风格列表项
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiscordListItem(
    title: String,
    subtitle: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                animateColorAsState(
                    targetValue = if (isHovered) DiscordColors.SurfaceHover.copy(alpha = 0.3f)
                                  else Color.Transparent,
                    animationSpec = tween(150),
                    label = "itemBackground"
                ).value,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon?.invoke()
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = if (leadingIcon != null) 12.dp else 0.dp)
        ) {
            Text(
                text = title,
                color = DiscordColors.TextNormal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )
            subtitle?.let {
                Text(
                    text = it,
                    color = DiscordColors.TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
        
        trailingContent?.invoke()
    }
}

/**
 * Discord按钮类型
 */
enum class DiscordButtonType {
    Primary, Secondary, Success, Danger, Link
}

/**
 * Discord风格按钮
 */
@Composable
fun DiscordButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    type: DiscordButtonType = DiscordButtonType.Primary
) {
    val (backgroundColor, textColor) = when (type) {
        DiscordButtonType.Primary -> DiscordColors.Blurple to Color.White
        DiscordButtonType.Secondary -> DiscordColors.SurfaceDefault to DiscordColors.TextNormal
        DiscordButtonType.Success -> DiscordColors.Green to Color.Black
        DiscordButtonType.Danger -> DiscordColors.Red to Color.White
        DiscordButtonType.Link -> Color.Transparent to DiscordColors.TextLink
    }
    
    Button(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f),
            disabledContentColor = textColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp
        )
    }
}

/**
 * Discord风格输入框
 */
@Composable
fun DiscordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder?.let { {
            Text(
                text = it,
                color = DiscordColors.TextMuted,
                fontSize = 14.sp
            )
        } },
        leadingIcon = leadingIcon?.let { {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = DiscordColors.TextMuted,
                modifier = Modifier.size(20.dp)
            )
        } },
        singleLine = singleLine,
        textStyle = LocalTextStyle.current.copy(
            color = DiscordColors.TextNormal,
            fontSize = 14.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DiscordColors.BackgroundDeepest,
            unfocusedContainerColor = DiscordColors.BackgroundDeepest,
            focusedBorderColor = DiscordColors.Blurple,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = DiscordColors.Blurple
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

/**
 * Discord风格分隔线
 */
@Composable
fun DiscordDivider(
    modifier: Modifier = Modifier
) {
    Divider(
        modifier = modifier,
        color = DiscordColors.Divider,
        thickness = 1.dp
    )
}

/**
 * Discord风格徽章（用于显示未读数等）
 */
@Composable
fun DiscordBadge(
    count: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = DiscordColors.Red
) {
    if (count > 0) {
        Box(
            modifier = modifier
                .sizeIn(minWidth = 16.dp, minHeight = 16.dp)
                .background(
                    color = backgroundColor,
                    shape = CircleShape
                )
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Discord风格频道分类
 */
@Composable
fun DiscordChannelCategory(
    name: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = name.uppercase(),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

/**
 * Discord风格频道项
 */
@Composable
fun DiscordChannelItem(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    textColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = name,
            color = textColor,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

/**
 * Discord风格用户面板
 */
@Composable
fun DiscordUserPanel(
    username: String,
    status: String,
    avatar: ImageVector,
    backgroundColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(backgroundColor)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(DiscordColors.Blurple),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = avatar,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        
        // 用户信息
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = username,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = status,
                color = mutedTextColor,
                fontSize = 12.sp
            )
        }
        
        // 设置按钮
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "设置",
            tint = mutedTextColor,
            modifier = Modifier
                .size(20.dp)
                .clickable { }
        )
    }
}

/**
 * Discord风格消息组件
 */
@Composable
fun DiscordMessage(
    username: String,
    message: String,
    time: String,
    avatarColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 头像
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 消息内容
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // 用户名和时间
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = username,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = time,
                    color = mutedTextColor,
                    fontSize = 12.sp
                )
            }
            
            // 消息文本
            Text(
                text = message,
                color = textColor,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 2.dp),
                lineHeight = 20.sp
            )
        }
    }
}