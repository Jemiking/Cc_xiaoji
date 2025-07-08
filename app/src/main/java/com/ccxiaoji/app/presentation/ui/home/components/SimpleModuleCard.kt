package com.ccxiaoji.app.presentation.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ccxiaoji.app.presentation.ui.navigation.ModuleInfo

@Composable
fun SimpleModuleCard(
    module: ModuleInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .background(
                color = module.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp) // 使用项目标准的medium圆角
            )
            .border(
                width = 1.dp,
                color = module.color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // 图标 - 直接使用主题色，无背景
                Icon(
                    imageVector = module.icon,
                    contentDescription = module.name,
                    tint = module.color,
                    modifier = Modifier.size(32.dp) // 稍大的图标
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 模块名称
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = module.color.copy(alpha = 0.9f) // 使用模块主题色
                )
            }
            
            // 角标（如果有）
            if (module.badge > 0) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = if (module.badge > 99) "99+" else module.badge.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}